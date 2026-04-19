import { useState } from 'react'

export function CodeCard({ code, title }: { code: string; title: string }) {
  const [copied, setCopied] = useState(false)

  async function handleCopy() {
    await navigator.clipboard.writeText(code)
    setCopied(true)
    setTimeout(() => {
      setCopied(false)
    }, 2000)
  }

  return (
    <section className="chat-code-card">
      <header className="chat-code-card__header">
        <span>{title || 'Code'}</span>
        <button
          className="icon-button"
          type="button"
          aria-label="Copy code"
          onClick={handleCopy}
        >
          <span className="material-symbols-outlined">
            {copied ? 'check' : 'content_copy'}
          </span>
        </button>
      </header>
      <pre className="chat-code-card__body">{code}</pre>
    </section>
  )
}
