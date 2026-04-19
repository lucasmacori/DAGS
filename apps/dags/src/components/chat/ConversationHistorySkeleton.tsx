export function ConversationHistorySkeleton() {
  return (
    <>
      <article className="chat-row chat-row--assistant" aria-hidden="true">
        <div className="chat-assistant-block chat-skeleton-block">
          <div className="chat-assistant-label chat-skeleton-label">
            <span className="chat-assistant-badge chat-skeleton-badge" />
            <span className="chat-skeleton-line chat-skeleton-line--label" />
          </div>

          <div className="chat-assistant-bubble chat-skeleton-bubble">
            <span className="chat-skeleton-line chat-skeleton-line--wide" />
            <span className="chat-skeleton-line chat-skeleton-line--medium" />
            <span className="chat-skeleton-line chat-skeleton-line--narrow" />
          </div>
        </div>
      </article>

      <article className="chat-row chat-row--user" aria-hidden="true">
        <div className="chat-user-block chat-skeleton-block">
          <div className="chat-user-bubble chat-skeleton-bubble chat-skeleton-bubble--user">
            <span className="chat-skeleton-line chat-skeleton-line--medium" />
            <span className="chat-skeleton-line chat-skeleton-line--short" />
          </div>
        </div>
      </article>

      <article className="chat-row chat-row--assistant" aria-hidden="true">
        <div className="chat-assistant-block chat-skeleton-block">
          <div className="chat-assistant-label chat-skeleton-label">
            <span className="chat-assistant-badge chat-skeleton-badge" />
            <span className="chat-skeleton-line chat-skeleton-line--label" />
          </div>

          <div className="chat-assistant-bubble chat-skeleton-bubble">
            <span className="chat-skeleton-line chat-skeleton-line--wide" />
            <span className="chat-skeleton-line chat-skeleton-line--wide" />
            <span className="chat-skeleton-line chat-skeleton-line--short" />
          </div>
        </div>
      </article>
    </>
  )
}
