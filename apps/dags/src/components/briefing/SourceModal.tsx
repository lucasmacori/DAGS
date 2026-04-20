import { useState, useEffect } from 'react'
import type { Source, SourceType } from '../../lib/briefing-types'

type SourceModalProps = {
  isOpen: boolean
  onClose: () => void
  source?: Source | null
  onSave: (source: { type: string; title: string; content: string }) => Promise<void>
}

export function SourceModal({ isOpen, onClose, source, onSave }: SourceModalProps) {
  const [type, setType] = useState<SourceType>('ARTICLE_URL')
  const [title, setTitle] = useState('')
  const [content, setContent] = useState('')
  const [isSaving, setIsSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (isOpen) {
      if (source) {
        setType(source.type)
        setTitle(source.title)
        setContent(source.content)
      } else {
        setType('ARTICLE_URL')
        setTitle('')
        setContent('')
      }
      setError(null)
    }
  }, [isOpen, source])

  if (!isOpen) return null

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!title.trim() || !content.trim()) {
      setError('Title and content are required.')
      return
    }

    setIsSaving(true)
    setError(null)
    try {
      await onSave({ type, title, content })
      onClose()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An error occurred.')
    } finally {
      setIsSaving(false)
    }
  }

  return (
    <div className="source-modal-overlay">
      <div className="source-modal" role="dialog" aria-modal="true">
        <header className="source-modal__header">
          <h2 className="source-modal__title">{source ? 'Edit Source' : 'Connect New Source'}</h2>
          <button className="icon-button" type="button" onClick={onClose} aria-label="Close">
            <span className="material-symbols-outlined">close</span>
          </button>
        </header>

        <form onSubmit={handleSubmit} className="source-modal__form">
          <div className="source-modal__fields">
            <label className="settings-input-group">
              <span>Source Type</span>
              <div style={{ position: 'relative' }}>
                <select 
                  className="settings-input" 
                  value={type} 
                  onChange={(e) => {
                    setType(e.target.value as SourceType)
                  }}
                  style={{ appearance: 'none', paddingRight: '2.5rem' }}
                >
                  <option value="ARTICLE_URL">Article Link</option>
                  <option value="RSS_FEED">RSS Feed</option>
                  <option value="PLAIN_TEXT">Plain Text</option>
                </select>
                <span className="material-symbols-outlined" style={{ position: 'absolute', right: '1rem', top: '50%', transform: 'translateY(-50%)', pointerEvents: 'none', color: '#c7c4d7' }}>expand_more</span>
              </div>
            </label>

            <label className="settings-input-group">
              <span>Title</span>
              <input 
                className="settings-input" 
                type="text" 
                placeholder="Enter a descriptive title" 
                value={title} 
                onChange={(e) => {
                  setTitle(e.target.value)
                }}
              />
            </label>

            <label className="settings-input-group">
              <span>Content / URL</span>
              {type === 'PLAIN_TEXT' ? (
                <textarea 
                  className="settings-input" 
                  style={{ minHeight: '6rem', resize: 'vertical' }}
                  placeholder="Enter the text content..." 
                  value={content} 
                  onChange={(e) => {
                    setContent(e.target.value)
                  }}
                />
              ) : (
                <input 
                  className="settings-input" 
                  type="text" 
                  placeholder="https://..." 
                  value={content} 
                  onChange={(e) => {
                    setContent(e.target.value)
                  }}
                />
              )}
            </label>
          </div>

          {error ? <p className="translate-error-banner">{error}</p> : null}

          <footer className="source-modal__footer">
            <button className="ghost-button" type="button" onClick={onClose} disabled={isSaving}>Cancel</button>
            <button className="primary-button" type="submit" disabled={isSaving}>
              {isSaving ? 'Saving...' : 'Save Source'}
            </button>
          </footer>
        </form>
      </div>
    </div>
  )
}