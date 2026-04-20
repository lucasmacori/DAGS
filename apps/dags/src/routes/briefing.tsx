import { createFileRoute } from '@tanstack/react-router'
import { Topbar } from '../components/layout/Topbar'
import { briefingSources } from '../lib/workspace-mocks'

export const Route = createFileRoute('/briefing')({
  component: BriefingPage,
})

function BriefingPage() {
  return (
    <section className="briefing-screen">
      <Topbar title="Briefing" />

      <div className="briefing-content">
        <header className="briefing-header">
          <div className="briefing-header__intro">
            <div className="briefing-search">
              <span className="material-symbols-outlined briefing-search__icon">search</span>
              <input 
                className="briefing-search__input" 
                placeholder="Search sources or schedules..." 
                type="text"
              />
            </div>
            <p className="briefing-header__description">
              Manage your intelligence streams and orchestrate how DAGS synthesizes your daily knowledge base.
            </p>
          </div>
          <div className="briefing-header__actions">
            <button className="ghost-button" type="button">
              Preview Summary
            </button>
            <button className="primary-button" type="button" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <span className="material-symbols-outlined" style={{ fontSize: '1.25rem' }}>add_circle</span>
              Add Source
            </button>
          </div>
        </header>

        <div className="briefing-dashboard">
          {/* Row 1: Template (Wide) and Notifications (Narrow) */}
          <div className="briefing-dashboard__wide">
            <details className="briefing-template-card h-full" open>
              <summary className="briefing-template-card__summary">
                <h3 className="briefing-section-title">
                  <span className="material-symbols-outlined">terminal</span>
                  Template
                </h3>
                <span className="material-symbols-outlined expand-icon">expand_more</span>
              </summary>
              <div className="briefing-template-card__content">
                <textarea 
                  className="briefing-template-card__textarea" 
                  spellCheck="false"
                  defaultValue="Summarize the following sources : ${sources.text} ${sources.links} ${sources.feeds}"
                />
                <p className="briefing-template-card__hint">
                  <span className="material-symbols-outlined">info</span>
                  Variables are injected dynamically at runtime.
                </p>
              </div>
            </details>
          </div>

          <div className="briefing-dashboard__narrow">
            <div className="briefing-config-section h-full">
              <h3 className="briefing-section-title">
                <span className="material-symbols-outlined">notifications_active</span>
                Notifications
              </h3>
              <div className="briefing-card-box briefing-card-box--compact h-full">
                <div className="briefing-notification-item briefing-notification-item--active">
                  <div className="briefing-notification-item__info">
                    <span className="material-symbols-outlined">mail</span>
                    <div>
                      <p className="briefing-notification-item__title">Email Delivery</p>
                      <p className="briefing-notification-item__desc">alex@company.net</p>
                    </div>
                  </div>
                  <label className="briefing-toggle briefing-toggle--small">
                    <input type="checkbox" className="briefing-toggle__input" defaultChecked />
                    <span className="briefing-toggle__slider"></span>
                  </label>
                </div>

                <div className="briefing-notification-item">
                  <div className="briefing-notification-item__info">
                    <span className="material-symbols-outlined">send</span>
                    <div>
                      <p className="briefing-notification-item__title">Telegram Bot</p>
                      <p className="briefing-notification-item__desc">Not configured</p>
                    </div>
                  </div>
                  <button className="briefing-notification-item__setup" type="button">Setup</button>
                </div>
              </div>
            </div>
          </div>

          {/* Row 2: Active Sources (Wide) and Routine Schedule (Narrow) */}
          <div className="briefing-dashboard__wide">
            <section className="briefing-sources">
              <div className="briefing-sources__header">
                <div className="briefing-sources__title-group">
                  <h3 className="briefing-sources__title">
                    <span className="material-symbols-outlined">data_object</span>
                    Active Sources
                  </h3>
                  <span className="briefing-sources__count">3 Connected</span>
                </div>
                
                <div className="briefing-filters">
                  <button className="briefing-filter briefing-filter--active">All Sources</button>
                  <button className="briefing-filter">Article Links</button>
                  <button className="briefing-filter">RSS Feeds</button>
                  <button className="briefing-filter">Plain Text</button>
                </div>
              </div>

              <div className="briefing-cards-grid">
                {briefingSources.map((source) => {
                  let badgeClass = 'briefing-card__badge--default'
                  if (source.type === 'Article Link') badgeClass = 'briefing-card__badge--primary'
                  else if (source.type === 'RSS Feed') badgeClass = 'briefing-card__badge--tertiary'
                  
                  let dotColor = '#10b981' // emerald-500
                  if (source.type === 'Plain Text') dotColor = '#52525b' // zinc-600

                  return (
                    <article key={source.id} className="briefing-card">
                      <div className="briefing-card__top">
                        <span className={`briefing-card__badge ${badgeClass}`}>{source.type}</span>
                        <span className="material-symbols-outlined briefing-card__menu">more_vert</span>
                      </div>
                      <h4 className="briefing-card__title">{source.title}</h4>
                      <p className={`briefing-card__url ${source.type === 'Plain Text' ? 'italic' : ''}`}>{source.url}</p>
                      <div className="briefing-card__footer">
                        <div className="briefing-card__sync">
                          <div className="briefing-card__sync-dot" style={{ backgroundColor: dotColor }}></div>
                          <span>{source.syncedAt}</span>
                        </div>
                        <span className="material-symbols-outlined briefing-card__icon">{source.icon}</span>
                      </div>
                    </article>
                  )
                })}

                <button className="briefing-card briefing-card--add" type="button">
                  <span className="material-symbols-outlined">add_circle</span>
                  <span>Connect New Source</span>
                </button>
              </div>
            </section>
          </div>

          <div className="briefing-dashboard__narrow">
            <div className="briefing-config-section">
              <h3 className="briefing-section-title">
                <span className="material-symbols-outlined">schedule</span>
                Routine Schedule
              </h3>
              
              <div className="briefing-card-box">
                <div className="briefing-toggle-row">
                  <div>
                    <p className="briefing-toggle-row__title">Enable Auto-Briefing</p>
                    <p className="briefing-toggle-row__description">Daily orchestration of synthesis.</p>
                  </div>
                  <label className="briefing-toggle">
                    <input type="checkbox" className="briefing-toggle__input" defaultChecked />
                    <span className="briefing-toggle__slider"></span>
                  </label>
                </div>

                <div className="briefing-schedule-group">
                  <span className="briefing-schedule-label">Frequency</span>
                  <div className="briefing-frequency-grid">
                    <button className="briefing-frequency-btn briefing-frequency-btn--active">Daily</button>
                    <button className="briefing-frequency-btn">Weekly</button>
                    <button className="briefing-frequency-btn">Custom</button>
                  </div>
                </div>

                <div className="briefing-schedule-group">
                  <span className="briefing-schedule-label">Delivery Time</span>
                  <div className="briefing-time-picker">
                    <span className="material-symbols-outlined briefing-time-picker__icon">schedule</span>
                    <div className="briefing-time-picker__value">
                      <span className="briefing-time-picker__time">08:00</span>
                      <span className="briefing-time-picker__ampm">AM</span>
                    </div>
                    <div className="briefing-time-picker__controls">
                      <button className="material-symbols-outlined">expand_less</button>
                      <button className="material-symbols-outlined">expand_more</button>
                    </div>
                  </div>
                  <p className="briefing-schedule-hint">
                    Summaries will be delivered to your central 'Briefings' hub and via encrypted email.
                  </p>
                </div>
              </div>
              <button className="briefing-update-btn" style={{ marginTop: '0.5rem' }}>Update Configuration</button>
            </div>
          </div>
        </div>
      </div>
    </section>
  )
}
