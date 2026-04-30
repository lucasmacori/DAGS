import { createFileRoute, useNavigate } from '@tanstack/react-router'
import { useState, type FormEvent } from 'react'

import { loginFn, registerFn } from '../lib/auth'

type LoginSearch = {
  redirect?: string
}

export const Route = createFileRoute('/login')({
  component: LoginPage,
  validateSearch: (search): LoginSearch => ({
    redirect: typeof search.redirect === 'string' ? search.redirect : undefined,
  }),
})

function LoginPage() {
  const { registrationEnabled } = Route.useRouteContext()
  const { redirect } = Route.useSearch()
  const navigate = useNavigate()
  const [mode, setMode] = useState<'login' | 'register'>('login')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (isSubmitting) {
      return
    }

    setIsSubmitting(true)
    setError(null)

    try {
      if (mode === 'register') {
        await registerFn({ data: { email, password } })
      } else {
        await loginFn({ data: { email, password } })
      }

      if (redirect?.startsWith('/')) {
        await navigate({ to: redirect })
      } else {
        await navigate({ to: '/chat' })
      }
    } catch (caughtError) {
      setError(caughtError instanceof Error ? caughtError.message : 'Authentication failed.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <section className="auth-screen">
      <div className="auth-card">
        <div className="auth-card__eyebrow">DAGS</div>
        <h1 className="auth-card__title">Sign in to your workspace</h1>
        <p className="auth-card__description">
          Authenticate before accessing conversations, translations, and briefings.
        </p>

        <div className="auth-card__mode-switch">
          <button
            className={`auth-card__mode-button${mode === 'login' ? ' auth-card__mode-button--active' : ''}`}
            type="button"
            onClick={() => {
              setMode('login')
              setError(null)
            }}
          >
            Login
          </button>
          {registrationEnabled ? (
            <button
              className={`auth-card__mode-button${mode === 'register' ? ' auth-card__mode-button--active' : ''}`}
              type="button"
              onClick={() => {
                setMode('register')
                setError(null)
              }}
            >
              Create account
            </button>
          ) : null}
        </div>

        <form className="auth-form" onSubmit={handleSubmit}>
          <label className="auth-form__field">
            <span>Email</span>
            <input
              autoComplete="email"
              className="auth-form__input"
              type="email"
              value={email}
              onChange={(event) => {
                setEmail(event.target.value)
              }}
            />
          </label>

          <label className="auth-form__field">
            <span>Password</span>
            <input
              autoComplete={mode === 'register' ? 'new-password' : 'current-password'}
              className="auth-form__input"
              type="password"
              value={password}
              onChange={(event) => {
                setPassword(event.target.value)
              }}
            />
          </label>

          {error ? <p className="auth-form__error">{error}</p> : null}

          {!registrationEnabled ? (
            <p className="auth-form__hint">
              Account creation is currently disabled. Ask an administrator to enable `AUTH_REGISTRATION_ENABLED` temporarily if you need to create the first account.
            </p>
          ) : null}

          <button className="primary-button auth-form__submit" disabled={isSubmitting} type="submit">
            {isSubmitting
              ? mode === 'register'
                ? 'Creating account...'
                : 'Signing in...'
              : mode === 'register'
                ? 'Create account'
                : 'Sign in'}
          </button>
        </form>
      </div>
    </section>
  )
}
