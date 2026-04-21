import type { FormEvent, KeyboardEvent, RefObject } from 'react'

type ChatComposerProps = {
  composerRef: RefObject<HTMLTextAreaElement | null>
  disabled: boolean
  message: string
  model: string
  modelOptions: { value: string; label: string }[]
  onChange: (value: string) => void
  onModelChange: (value: string) => void
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
}

export function ChatComposer({
  composerRef,
  disabled,
  message,
  model,
  modelOptions,
  onChange,
  onModelChange,
  onSubmit,
}: ChatComposerProps) {
  function handleComposerKeyDown(event: KeyboardEvent<HTMLTextAreaElement>) {
    if (event.key !== 'Enter' || event.shiftKey) {
      return
    }

    event.preventDefault()
    event.currentTarget.form?.requestSubmit()
  }

  return (
    <footer className="chat-composer-shell">
      <form className="chat-composer-panel" onSubmit={onSubmit}>
        <div className="chat-composer-main">
          <button className="icon-button" type="button" aria-label="Add attachment">
            <span className="material-symbols-outlined">add_circle</span>
          </button>

          <textarea
            ref={composerRef}
            className="chat-composer-input"
            aria-label="Chat message"
            placeholder="Message DAGS..."
            value={message}
            onKeyDown={handleComposerKeyDown}
            onChange={(event) => {
              onChange(event.target.value)
            }}
          />

          <div className="chat-composer-actions">
            <button className="icon-button" type="button" aria-label="Use microphone">
              <span className="material-symbols-outlined">mic</span>
            </button>
            <button
              className="send-button"
              type="submit"
              aria-label="Send message"
              disabled={disabled}
            >
              <span className="material-symbols-outlined">send</span>
            </button>
          </div>
        </div>

        <div className="chat-composer-meta">
          <div className="chat-composer-model">
            <span className="material-symbols-outlined">auto_awesome</span>
            <div className="source-modal__select-wrap">
              <select
                className="chat-composer-model__select"
                value={model}
                onChange={(e) => {
                  onModelChange(e.target.value)
                }}
              >
                {modelOptions.map((opt) => (
                  <option key={opt.value} value={opt.value}>
                    {opt.label}
                  </option>
                ))}
              </select>
            </div>
          </div>
          <span>DAGS v4.2.0-stable</span>
        </div>
      </form>
    </footer>
  )
}
