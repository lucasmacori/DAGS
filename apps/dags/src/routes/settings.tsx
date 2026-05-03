import { createFileRoute, useRouter } from '@tanstack/react-router'
import { useState } from 'react'

import { Topbar } from '../components/layout/Topbar'
import { changePasswordFn, getAuthStateFn, updateEmailFn } from '../lib/auth'
import { appearanceOptions } from '../lib/workspace-mocks'

export const Route = createFileRoute('/settings')({
  component: SettingsPage,
})

function SettingsPage() {
  const { user } = Route.useRouteContext()
  const navigate = Route.useNavigate()
  const displayName = user?.email ?? 'Signed-in user'

  const [email, setEmail] = useState(user?.email ?? '')
  const [emailPassword, setEmailPassword] = useState('')
  const [emailLoading, setEmailLoading] = useState(false)
  const [emailMessage, setEmailMessage] = useState<{ type: 'success' | 'error', text: string } | null>(null)

  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [passwordLoading, setPasswordLoading] = useState(false)
  const [passwordMessage, setPasswordMessage] = useState<{ type: 'success' | 'error', text: string } | null>(null)

  async function handleUpdateEmail(e: React.FormEvent) {
    e.preventDefault()
    setEmailMessage(null)

    if (!email.trim() || !emailPassword) {
      setEmailMessage({ type: 'error', text: 'Email and password are required.' })
      return
    }

    setEmailLoading(true)
    try {
      const updatedUser = await updateEmailFn({ data: { email: email.trim(), password: emailPassword } })
      setEmailMessage({ type: 'success', text: 'Email updated successfully.' })
      setEmailPassword('')
      await getAuthStateFn()
      await navigate({ to: '/settings', replace: true })
    } catch (err) {
      setEmailMessage({ type: 'error', text: err instanceof Error ? err.message : 'Could not update email.' })
    } finally {
      setEmailLoading(false)
    }
  }

  async function handleChangePassword(e: React.FormEvent) {
    e.preventDefault()
    setPasswordMessage(null)

    if (!currentPassword || !newPassword || !confirmPassword) {
      setPasswordMessage({ type: 'error', text: 'All fields are required.' })
      return
    }

    if (newPassword !== confirmPassword) {
      setPasswordMessage({ type: 'error', text: 'New passwords do not match.' })
      return
    }

    if (newPassword.length < 8) {
      setPasswordMessage({ type: 'error', text: 'Password must contain at least 8 characters.' })
      return
    }

    setPasswordLoading(true)
    try {
      await changePasswordFn({ data: { currentPassword, newPassword } })
      setPasswordMessage({ type: 'success', text: 'Password updated successfully.' })
      setCurrentPassword('')
      setNewPassword('')
      setConfirmPassword('')
    } catch (err) {
      setPasswordMessage({ type: 'error', text: err instanceof Error ? err.message : 'Could not change password.' })
    } finally {
      setPasswordLoading(false)
    }
  }

  return (
    <section className="settings-screen">
      <Topbar
        title="Settings"
        actions={
          <>
            <button className="icon-button icon-button--topbar" type="button" aria-label="Notifications">
              <span className="material-symbols-outlined">notifications</span>
            </button>
            <button className="icon-button icon-button--topbar" type="button" aria-label="Help">
              <span className="material-symbols-outlined">help</span>
            </button>
          </>
        }
      />

      <div className="settings-content">
        <section className="settings-section-grid">
          <div className="settings-section-grid__intro">
            <h2 className="settings-section-grid__title">Profile</h2>
            <p className="settings-section-grid__description">
              Manage your public identity and contact information.
            </p>
          </div>

          <div className="settings-section-grid__main settings-stack-exact">
            <div className="settings-profile-card">
              <div className="settings-avatar-group">
                <div className="settings-avatar-wrap">
                  <div className="settings-avatar-image settings-avatar-image--icon" aria-hidden="true">
                    <span className="material-symbols-outlined">person</span>
                  </div>
                </div>

                <div>
                  <h3 className="settings-profile-card__name">{displayName}</h3>
                  <p className="settings-profile-card__hint">Account information is loaded from your current session.</p>
                </div>
              </div>
            </div>

            <div className="settings-input-list">
              <label className="settings-input-group">
                <span>User ID</span>
                <input className="settings-input" type="text" value={user?.userId ?? ''} readOnly />
              </label>
            </div>

            <form className="settings-form" onSubmit={handleUpdateEmail}>
              <h3 className="settings-form__title">Update Email</h3>
              <div className="settings-input-list">
                <label className="settings-input-group">
                  <span>New Email Address</span>
                  <input
                    className="settings-input"
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="you@example.com"
                  />
                </label>
                <label className="settings-input-group">
                  <span>Current Password</span>
                  <input
                    className="settings-input"
                    type="password"
                    value={emailPassword}
                    onChange={(e) => setEmailPassword(e.target.value)}
                    placeholder="Enter your current password"
                  />
                </label>
              </div>
              {emailMessage && (
                <p className={`settings-form__message settings-form__message--${emailMessage.type}`}>
                  {emailMessage.text}
                </p>
              )}
              <button
                className="settings-form__button"
                type="submit"
                disabled={emailLoading}
              >
                {emailLoading ? 'Updating...' : 'Update Email'}
              </button>
            </form>

            <form className="settings-form" onSubmit={handleChangePassword}>
              <h3 className="settings-form__title">Change Password</h3>
              <div className="settings-input-list">
                <label className="settings-input-group">
                  <span>Current Password</span>
                  <input
                    className="settings-input"
                    type="password"
                    value={currentPassword}
                    onChange={(e) => setCurrentPassword(e.target.value)}
                    placeholder="Enter your current password"
                  />
                </label>
                <label className="settings-input-group">
                  <span>New Password</span>
                  <input
                    className="settings-input"
                    type="password"
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    placeholder="Enter new password (min 8 characters)"
                  />
                </label>
                <label className="settings-input-group">
                  <span>Confirm New Password</span>
                  <input
                    className="settings-input"
                    type="password"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    placeholder="Confirm new password"
                  />
                </label>
              </div>
              {passwordMessage && (
                <p className={`settings-form__message settings-form__message--${passwordMessage.type}`}>
                  {passwordMessage.text}
                </p>
              )}
              <button
                className="settings-form__button"
                type="submit"
                disabled={passwordLoading}
              >
                {passwordLoading ? 'Updating...' : 'Change Password'}
              </button>
            </form>
          </div>
        </section>

        <section className="settings-section-grid">
          <div className="settings-section-grid__intro">
            <h2 className="settings-section-grid__title">Appearance</h2>
            <p className="settings-section-grid__description">
              Customize how DAGS feels in your workspace.
            </p>
          </div>

          <div className="settings-section-grid__main settings-theme-grid">
            {appearanceOptions.map((option) => (
              <button
                key={option.name}
                className={`settings-theme-card${option.isSelected ? ' settings-theme-card--selected' : ''}`}
                type="button"
              >
                <div className={`settings-theme-preview${option.isSelected ? '' : ' settings-theme-preview--light'}`}>
                  <div className="settings-theme-preview__line settings-theme-preview__line--short" />
                  <div className="settings-theme-preview__line" />
                  <div className="settings-theme-preview__line settings-theme-preview__line--medium" />
                </div>

                <div className="settings-theme-card__footer">
                  <span>{option.name}</span>
                  {option.isSelected ? (
                    <span className="material-symbols-outlined settings-theme-card__check">check_circle</span>
                  ) : (
                    <span className="settings-theme-card__radio" />
                  )}
                </div>
              </button>
            ))}
          </div>
        </section>

      </div>
    </section>
  )
}
