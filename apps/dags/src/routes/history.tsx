import { createFileRoute } from '@tanstack/react-router'

import { historyItems } from '../lib/workspace-mocks'

export const Route = createFileRoute('/history')({
  component: HistoryPage,
})

function HistoryPage() {
  return (
    <section className="workspace-page">
      <header className="workspace-header">
        <div>
          <p className="workspace-header__eyebrow">History</p>
          <h1 className="workspace-header__title">Recent workspace activity</h1>
          <p className="workspace-header__description">
            Browse recent chats and translation runs with model metadata, timestamps, and status.
          </p>
        </div>

        <div className="workspace-header__meta">
          <span className="status-chip status-chip--primary">7-day view</span>
          <span className="status-chip">Auto-saved</span>
        </div>
      </header>

      <div className="content-grid">
        <section className="panel-card panel-card--stretch">
          <div className="section-heading">
            <div>
              <p className="section-heading__eyebrow">Timeline</p>
              <h2 className="section-heading__title">Latest runs</h2>
            </div>

            <p className="section-heading__description">
              Prototype-backed mock data for recent orchestration and translation sessions.
            </p>
          </div>

          <div className="stack-list">
            {historyItems.map((item) => (
              <article key={item.id} className="stack-list__item history-card">
                <div className="history-card__main">
                  <div className="history-card__topline">
                    <h3 className="stack-list__title">{item.title}</h3>
                    <span className="status-chip">{item.status}</span>
                  </div>

                  <p className="stack-list__meta">
                    {item.model} · {item.timestamp}
                  </p>
                  <p className="stack-list__description">{item.summary}</p>
                </div>

                <button className="ghost-button" type="button">
                  Open Session
                </button>
              </article>
            ))}
          </div>
        </section>

        <aside className="side-stack">
          <section className="panel-card stat-card">
            <p className="stat-card__label">Conversations</p>
            <p className="stat-card__value">18</p>
            <p className="stat-card__hint">Across chat and translation tools this week.</p>
          </section>

          <section className="panel-card stat-card">
            <p className="stat-card__label">Most used model</p>
            <p className="stat-card__value stat-card__value--small">gemma4:e2b</p>
            <p className="stat-card__hint">Primary orchestration assistant for active workflows.</p>
          </section>

          <section className="panel-card filter-card">
            <p className="section-heading__eyebrow">Filters</p>
            <div className="tag-row">
              <span className="status-chip status-chip--primary">Chat</span>
              <span className="status-chip">Translate</span>
              <span className="status-chip">Needs review</span>
            </div>
          </section>
        </aside>
      </div>
    </section>
  )
}
