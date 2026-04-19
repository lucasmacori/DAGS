import { createFileRoute } from '@tanstack/react-router'

import { archiveItems } from '../lib/workspace-mocks'

export const Route = createFileRoute('/archive')({
  component: ArchivePage,
})

function ArchivePage() {
  return (
    <section className="workspace-page">
      <header className="workspace-header">
        <div>
          <p className="workspace-header__eyebrow">Archive</p>
          <h1 className="workspace-header__title">Retained workspace assets</h1>
          <p className="workspace-header__description">
            Archived prompts, translations, and snapshots kept for recall, audits, and recovery.
          </p>
        </div>

        <div className="workspace-header__meta">
          <span className="status-chip status-chip--primary">Retention controls</span>
          <span className="status-chip">3 archived bundles</span>
        </div>
      </header>

      <div className="content-grid">
        <section className="panel-card panel-card--stretch">
          <div className="section-heading">
            <div>
              <p className="section-heading__eyebrow">Archive List</p>
              <h2 className="section-heading__title">Stored artifacts</h2>
            </div>

            <p className="section-heading__description">
              Mocked entries derived from the Stitch archive navigation pattern and workspace language.
            </p>
          </div>

          <div className="stack-list">
            {archiveItems.map((item) => (
              <article key={item.id} className="stack-list__item archive-card">
                <div>
                  <div className="archive-card__topline">
                    <h3 className="stack-list__title">{item.title}</h3>
                    <span className="status-chip">{item.type}</span>
                  </div>

                  <p className="stack-list__meta">
                    Archived {item.archivedAt} · {item.retention}
                  </p>
                  <p className="stack-list__description">{item.description}</p>
                </div>

                <div className="button-row">
                  <button className="ghost-button" type="button">
                    Restore
                  </button>
                  <button className="ghost-button" type="button">
                    Inspect
                  </button>
                </div>
              </article>
            ))}
          </div>
        </section>

        <aside className="side-stack">
          <section className="panel-card stat-card">
            <p className="stat-card__label">Retention policy</p>
            <p className="stat-card__value stat-card__value--small">Tiered storage</p>
            <p className="stat-card__hint">
              Snapshots move to long-term storage automatically after 30 days.
            </p>
          </section>

          <section className="panel-card stat-card">
            <p className="stat-card__label">Compliance</p>
            <p className="stat-card__value">OK</p>
            <p className="stat-card__hint">
              No pending archive deletions or unreviewed retention exceptions.
            </p>
          </section>
        </aside>
      </div>
    </section>
  )
}
