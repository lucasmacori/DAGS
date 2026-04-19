import { createFileRoute } from '@tanstack/react-router'

import { Topbar } from '../components/layout/Topbar'
import {
  appearanceOptions,
  settingsApiKeys,
  settingsProfileAvatar,
  workspaceUser,
} from '../lib/workspace-mocks'

export const Route = createFileRoute('/settings')({
  component: SettingsPage,
})

function SettingsPage() {
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
                  <img
                    className="settings-avatar-image"
                    src={settingsProfileAvatar}
                    alt="Profile avatar"
                  />
                  <button className="settings-avatar-overlay" type="button" aria-label="Change photo">
                    <span className="material-symbols-outlined">photo_camera</span>
                  </button>
                </div>

                <div>
                  <h3 className="settings-profile-card__name">{workspaceUser.name}</h3>
                  <p className="settings-profile-card__hint">PNG or JPG. Max 2MB.</p>
                  <div className="settings-text-actions">
                    <button type="button">Change Photo</button>
                    <button className="settings-text-actions__danger" type="button">
                      Remove
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <div className="settings-input-list">
              <label className="settings-input-group">
                <span>Full Name</span>
                <input className="settings-input" type="text" value={workspaceUser.name} readOnly />
              </label>
              <label className="settings-input-group">
                <span>Email Address</span>
                <input className="settings-input" type="email" value="alex.river@gateway.ai" readOnly />
              </label>
            </div>
          </div>
        </section>

        <section className="settings-section-grid">
          <div className="settings-section-grid__intro">
            <h2 className="settings-section-grid__title">API Access</h2>
            <p className="settings-section-grid__description">
              Connect your external LLM providers to power DAGS nodes.
            </p>
          </div>

          <div className="settings-section-grid__main settings-stack-exact">
            {settingsApiKeys.map((apiKey) => (
              <article key={apiKey.name} className="settings-key-card">
                <div>
                  <div className="settings-key-card__header">
                    <span className="settings-key-card__name">{apiKey.name}</span>
                    <span
                      className={`settings-key-card__status${apiKey.status === 'Active' ? ' settings-key-card__status--active' : ''}`}
                    >
                      {apiKey.status}
                    </span>
                  </div>
                  <code className="settings-key-card__value">{apiKey.value.replace(/\./g, '•')}</code>
                </div>

                <div className="settings-key-card__actions">
                  <button type="button" aria-label="Copy key">
                    <span className="material-symbols-outlined">content_copy</span>
                  </button>
                  <button type="button" aria-label="Delete key">
                    <span className="material-symbols-outlined">delete</span>
                  </button>
                </div>
              </article>
            ))}

            <button className="settings-generate-key" type="button">
              <span className="material-symbols-outlined">add</span>
              <span>Generate New Key</span>
            </button>
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

        <div className="settings-footer-actions">
          <button className="settings-footer-actions__ghost" type="button">
            Discard changes
          </button>
          <button className="settings-footer-actions__primary" type="button">
            Save Preferences
          </button>
        </div>
      </div>

      <aside className="settings-tip">
        <span className="material-symbols-outlined settings-tip__icon">auto_awesome</span>
        <div>
          <p className="settings-tip__title">Pro Tip</p>
          <p className="settings-tip__text">
            Sync your API keys across multiple gateways to maintain persistent context in the DAGS ecosystem.
          </p>
        </div>
      </aside>
    </section>
  )
}
