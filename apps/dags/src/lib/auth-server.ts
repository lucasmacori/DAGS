import { deleteCookie, getCookie, setCookie } from '@tanstack/react-start/server'

import { getAiToolsApiConfig } from './ai-tools-api'
import type { AuthState, AuthUser, CredentialsInput, TokenResponse } from './auth'

const ACCESS_TOKEN_COOKIE = 'dags_access_token'
const REFRESH_TOKEN_COOKIE = 'dags_refresh_token'
const JWT_REFRESH_WINDOW_SECONDS = 30

function isRegistrationEnabled() {
  return process.env.AUTH_REGISTRATION_ENABLED?.trim().toLowerCase() === 'true'
}

function authCookieOptions(maxAge: number) {
  return {
    httpOnly: true,
    maxAge,
    path: '/',
    sameSite: 'lax' as const,
    secure: process.env.NODE_ENV === 'production',
  }
}

function clearAuthCookies() {
  deleteCookie(ACCESS_TOKEN_COOKIE, { path: '/' })
  deleteCookie(REFRESH_TOKEN_COOKIE, { path: '/' })
}

function storeAuthCookies(tokens: TokenResponse) {
  setCookie(
    ACCESS_TOKEN_COOKIE,
    tokens.accessToken,
    authCookieOptions(tokens.accessTokenExpiresInSeconds),
  )
  setCookie(
    REFRESH_TOKEN_COOKIE,
    tokens.refreshToken,
    authCookieOptions(tokens.refreshTokenExpiresInSeconds),
  )
}

function normalizeCredentials(input: CredentialsInput) {
  const email = input.email?.trim().toLowerCase() ?? ''
  const password = input.password?.trim() ?? ''

  if (!email || !password) {
    throw new Error('Email and password are required.')
  }

  return { email, password }
}

function parseJwtExpiry(token: string) {
  try {
    const [, payload] = token.split('.')
    if (!payload) {
      return null
    }

    const normalizedPayload = payload.replace(/-/g, '+').replace(/_/g, '/')
    const paddedPayload = normalizedPayload.padEnd(Math.ceil(normalizedPayload.length / 4) * 4, '=')
    const parsedPayload = JSON.parse(Buffer.from(paddedPayload, 'base64').toString('utf-8')) as { exp?: number }
    return typeof parsedPayload.exp === 'number' ? parsedPayload.exp : null
  } catch {
    return null
  }
}

function shouldRefreshAccessToken(token: string) {
  const expiry = parseJwtExpiry(token)
  if (!expiry) {
    return false
  }

  const now = Math.floor(Date.now() / 1000)
  return expiry - now <= JWT_REFRESH_WINDOW_SECONDS
}

async function fetchBackend(path: string, init?: RequestInit) {
  const { apiBaseUrl } = getAiToolsApiConfig()
  return fetch(`${apiBaseUrl}${path}`, init)
}

async function refreshTokens() {
  const refreshToken = getCookie(REFRESH_TOKEN_COOKIE)

  if (!refreshToken) {
    clearAuthCookies()
    return null
  }

  const response = await fetchBackend('/auth/refresh', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ refreshToken }),
  })

  if (!response.ok) {
    clearAuthCookies()
    return null
  }

  const payload = (await response.json()) as TokenResponse
  storeAuthCookies(payload)
  return payload.accessToken
}

async function getAccessToken() {
  const currentAccessToken = getCookie(ACCESS_TOKEN_COOKIE)

  if (!currentAccessToken) {
    return refreshTokens()
  }

  if (shouldRefreshAccessToken(currentAccessToken)) {
    return (await refreshTokens()) ?? currentAccessToken
  }

  return currentAccessToken
}

export async function authenticatedApiFetch(path: string, init?: RequestInit) {
  const accessToken = await getAccessToken()

  if (!accessToken) {
    return new Response('Unauthorized', { status: 401 })
  }

  const headers = new Headers(init?.headers)
  headers.set('Authorization', `Bearer ${accessToken}`)

  let response = await fetchBackend(path, {
    ...init,
    headers,
  })

  if (response.status !== 401) {
    return response
  }

  const refreshedAccessToken = await refreshTokens()
  if (!refreshedAccessToken) {
    return response
  }

  const retryHeaders = new Headers(init?.headers)
  retryHeaders.set('Authorization', `Bearer ${refreshedAccessToken}`)

  response = await fetchBackend(path, {
    ...init,
    headers: retryHeaders,
  })

  if (response.status === 401) {
    clearAuthCookies()
  }

  return response
}

async function getCurrentUser() {
  const response = await authenticatedApiFetch('/auth/me', {
    method: 'GET',
  })

  if (response.status === 401) {
    clearAuthCookies()
    return null
  }

  if (!response.ok) {
    throw new Error((await response.text()).trim() || 'Could not read the current session.')
  }

  return (await response.json()) as AuthUser
}

export async function getAuthState() {
  return {
    registrationEnabled: isRegistrationEnabled(),
    user: await getCurrentUser(),
  } satisfies AuthState
}

export async function login(input: CredentialsInput) {
  const credentials = normalizeCredentials(input)

  const response = await fetchBackend('/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(credentials),
  })

  if (response.status === 401) {
    throw new Error('Invalid email or password.')
  }

  if (!response.ok) {
    throw new Error((await response.text()).trim() || 'Invalid email or password.')
  }

  const payload = (await response.json()) as TokenResponse
  storeAuthCookies(payload)
  return payload.user
}

export async function register(input: CredentialsInput) {
  if (!isRegistrationEnabled()) {
    throw new Error('Account creation is disabled.')
  }

  const credentials = normalizeCredentials(input)

  const response = await fetchBackend('/auth/register', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(credentials),
  })

  if (!response.ok) {
    throw new Error((await response.text()).trim() || 'Could not create account.')
  }

  const payload = (await response.json()) as TokenResponse
  storeAuthCookies(payload)
  return payload.user
}

export async function logout() {
  const refreshToken = getCookie(REFRESH_TOKEN_COOKIE)

  if (refreshToken) {
    await fetchBackend('/auth/logout', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ refreshToken }),
    })
  }

  clearAuthCookies()
  return true
}

export async function updateEmail(input: { email: string, password: string }) {
  const accessToken = await getAccessToken()

  if (!accessToken) {
    throw new Error('Unauthorized')
  }

  const response = await fetchBackend('/auth/me/email', {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${accessToken}`,
    },
    body: JSON.stringify(input),
  })

  if (response.status === 401) {
    throw new Error('Invalid password.')
  }

  if (response.status === 409) {
    throw new Error('Email is already in use.')
  }

  if (!response.ok) {
    throw new Error((await response.text()).trim() || 'Could not update email.')
  }

  const payload = (await response.json()) as TokenResponse
  storeAuthCookies(payload)
  return payload.user
}

export async function changePassword(input: { currentPassword: string, newPassword: string }) {
  const accessToken = await getAccessToken()

  if (!accessToken) {
    throw new Error('Unauthorized')
  }

  const response = await fetchBackend('/auth/me/password', {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${accessToken}`,
    },
    body: JSON.stringify(input),
  })

  if (response.status === 401) {
    throw new Error('Current password is incorrect.')
  }

  if (!response.ok) {
    throw new Error((await response.text()).trim() || 'Could not change password.')
  }

  const payload = (await response.json()) as TokenResponse
  storeAuthCookies(payload)
  return payload.user
}
