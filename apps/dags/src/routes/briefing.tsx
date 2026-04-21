import { createFileRoute } from '@tanstack/react-router'
import { useState, useEffect } from 'react'
import { Topbar } from '../components/layout/Topbar'
import { SourceModal } from '../components/briefing/SourceModal'
import type { Source, SourceType } from '../lib/briefing-types'
import { formatSyncedAt } from '../lib/briefing-types'

export const Route = createFileRoute('/briefing')({
  component: BriefingPage,
})

function getSourceDisplayDetails(type: SourceType) {
  switch (type) {
    case 'ARTICLE_URL':
      return {
        badgeClass: 'briefing-card__badge--primary',
        label: 'Article Link',
        icon: 'link',
        dotClass: 'briefing-card__sync-dot--emerald',
      }
    case 'RSS_FEED':
      return {
        badgeClass: 'briefing-card__badge--tertiary',
        label: 'RSS Feed',
        icon: 'rss_feed',
        dotClass: 'briefing-card__sync-dot--emerald',
      }
    case 'PLAIN_TEXT':
      return {
        badgeClass: 'briefing-card__badge--default',
        label: 'Plain Text',
        icon: 'description',
        dotClass: 'briefing-card__sync-dot--zinc',
      }
    default:
      return {
        badgeClass: 'briefing-card__badge--default',
        label: 'Unknown',
        icon: 'data_object',
        dotClass: 'briefing-card__sync-dot--zinc',
      }
  }
}

function BriefingPage() {
  const [sources, setSources] = useState<Source[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [activeFilter, setActiveFilter] = useState<SourceType | 'ALL'>('ALL')
  const [activeActionMenuId, setActiveActionMenuId] = useState<string | null>(null)

  const [isModalOpen, setIsModalOpen] = useState(false)
  const [editingSource, setEditingSource] = useState<Source | null>(null)

  const fetchSources = async () => {
    setIsLoading(true)
    setError(null)
    try {
      const res = await fetch('/source')
      if (!res.ok) {
        throw new Error('Failed to load sources.')
      }
      const data = await res.json() as Source[]
      // Sort newest first by updated_at
      data.sort((a, b) => new Date(b.updated_at).getTime() - new Date(a.updated_at).getTime())
      setSources(data)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load sources.')
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    fetchSources()
  }, [])

  const handleSaveSource = async (sourceData: { type: string; title: string; content: string }) => {
    const url = editingSource ? `/source/${editingSource.source_id}` : '/source'
    const method = editingSource ? 'PATCH' : 'POST'
    
    const res = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(sourceData),
    })

    if (!res.ok) {
      throw new Error(await res.text() || 'Failed to save source.')
    }
    
    await fetchSources()
  }

  const handleDeleteSource = async (sourceId: string) => {
    if (!window.confirm('Are you sure you want to delete this source?')) return

    try {
      const res = await fetch(`/source/${sourceId}`, { method: 'DELETE' })
      if (!res.ok) {
        throw new Error('Failed to delete source.')
      }
      setSources((prev) => prev.filter(s => s.source_id !== sourceId))
      setActiveActionMenuId(null)
    } catch (err) {
      alert(err instanceof Error ? err.message : 'Error deleting source.')
    }
  }

  const filteredSources = activeFilter === 'ALL' 
    ? sources 
    : sources.filter(s => s.type === activeFilter)

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
            <button 
              className="primary-button briefing-header__add-btn" 
              type="button" 
              onClick={() => {
                setEditingSource(null)
                setIsModalOpen(true)
              }}
            >
              <span className="material-symbols-outlined briefing-header__add-icon">add_circle</span>
              Add Source
            </button>
          </div>
        </header>

        <div className="briefing-dashboard">
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
                  defaultValue={"Summarize the following sources : ${sources.text} ${sources.links} ${sources.feeds}"}
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

          <div className="briefing-dashboard__wide">
            <section className="briefing-sources">
              <div className="briefing-sources__header">
                <div className="briefing-sources__title-group">
                  <h3 className="briefing-sources__title">
                    <span className="material-symbols-outlined">data_object</span>
                    Active Sources
                  </h3>
                  <span className="briefing-sources__count">{sources.length} Connected</span>
                </div>
                
                <div className="briefing-filters">
                  <button 
                    className={`briefing-filter ${activeFilter === 'ALL' ? 'briefing-filter--active' : ''}`}
                    onClick={() => setActiveFilter('ALL')}
                  >
                    All Sources
                  </button>
                  <button 
                    className={`briefing-filter ${activeFilter === 'ARTICLE_URL' ? 'briefing-filter--active' : ''}`}
                    onClick={() => setActiveFilter('ARTICLE_URL')}
                  >
                    Article Links
                  </button>
                  <button 
                    className={`briefing-filter ${activeFilter === 'RSS_FEED' ? 'briefing-filter--active' : ''}`}
                    onClick={() => setActiveFilter('RSS_FEED')}
                  >
                    RSS Feeds
                  </button>
                  <button 
                    className={`briefing-filter ${activeFilter === 'PLAIN_TEXT' ? 'briefing-filter--active' : ''}`}
                    onClick={() => setActiveFilter('PLAIN_TEXT')}
                  >
                    Plain Text
                  </button>
                </div>
              </div>

              <div className="briefing-cards-grid">
                {isLoading && sources.length === 0 && (
                  <p className="briefing-sources__loading">Loading sources...</p>
                )}
                {error && (
                  <p className="briefing-sources__error">{error}</p>
                )}
                
                {filteredSources.map((source) => {
                  const details = getSourceDisplayDetails(source.type)

                  return (
                    <article key={source.source_id} className="briefing-card group">
                      <div className="briefing-card__top">
                        <span className={`briefing-card__badge ${details.badgeClass}`}>{details.label}</span>

                        <div className="briefing-card__actions">
                          <button
                            className="briefing-card__menu-trigger"
                            type="button"
                            aria-label="Source actions"
                            onClick={() => {
                              setActiveActionMenuId((currentValue) =>
                                currentValue === source.source_id ? null : source.source_id,
                              )
                            }}
                          >
                            <span className="material-symbols-outlined briefing-card__menu">more_vert</span>
                          </button>

                          {activeActionMenuId === source.source_id ? (
                            <div className="briefing-card__menu-panel">
                              <button
                                className="briefing-card__menu-button"
                                type="button"
                                onClick={() => {
                                  setEditingSource(source)
                                  setIsModalOpen(true)
                                  setActiveActionMenuId(null)
                                }}
                              >
                                <span className="material-symbols-outlined">edit</span>
                                Edit Source
                              </button>

                              <button
                                className="briefing-card__menu-button briefing-card__menu-button--danger"
                                type="button"
                                onClick={() => {
                                  handleDeleteSource(source.source_id)
                                }}
                              >
                                <span className="material-symbols-outlined">delete</span>
                                Delete Source
                              </button>
                            </div>
                          ) : null}
                        </div>
                      </div>
                      <h4 className="briefing-card__title">{source.title}</h4>
                      <p className={`briefing-card__url ${source.type === 'PLAIN_TEXT' ? 'italic' : ''}`}>{source.content}</p>
                      <div className="briefing-card__footer">
                        <div className="briefing-card__sync">
                          <div className={`briefing-card__sync-dot ${details.dotClass}`}></div>
                          <span>{formatSyncedAt(source.updated_at)}</span>
                        </div>
                        <span className="material-symbols-outlined briefing-card__icon">{details.icon}</span>
                      </div>
                    </article>
                  )
                })}

                <button 
                  className="briefing-card briefing-card--add" 
                  type="button"
                  onClick={() => {
                    setEditingSource(null)
                    setIsModalOpen(true)
                  }}
                >
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
              <button className="briefing-update-btn briefing-update-btn--spaced">Update Configuration</button>
            </div>
          </div>
        </div>
      </div>

      <SourceModal 
        isOpen={isModalOpen}
        source={editingSource}
        onClose={() => setIsModalOpen(false)}
        onSave={handleSaveSource}
      />
    </section>
  )
}
