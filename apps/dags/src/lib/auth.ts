import { createServerFn } from '@tanstack/react-start'

export type AuthUser = {
  userId: string
  email: string
}

export type AuthState = {
  registrationEnabled: boolean
  user: AuthUser | null
}

export type CredentialsInput = {
  email: string
  password: string
}

export type TokenResponse = {
  accessToken: string
  accessTokenExpiresInSeconds: number
  refreshToken: string
  refreshTokenExpiresInSeconds: number
  user: AuthUser
}

export async function authenticatedApiFetch(path: string, init?: RequestInit) {
  const authServer = await import('./auth-server')
  return authServer.authenticatedApiFetch(path, init)
}

export const getAuthStateFn = createServerFn({ method: 'GET' }).handler(async () => {
  const authServer = await import('./auth-server')
  return authServer.getAuthState()
})

export const loginFn = createServerFn({ method: 'POST' })
  .handler(async ({ data }: { data: CredentialsInput }) => {
    const authServer = await import('./auth-server')
    return authServer.login(data)
  })

export const registerFn = createServerFn({ method: 'POST' })
  .handler(async ({ data }: { data: CredentialsInput }) => {
    const authServer = await import('./auth-server')
    return authServer.register(data)
  })

export const logoutFn = createServerFn({ method: 'POST' }).handler(async () => {
  const authServer = await import('./auth-server')
  return authServer.logout()
})
