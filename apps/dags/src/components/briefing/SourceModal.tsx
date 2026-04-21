import { useState, useEffect } from 'react'
import type { Source, SourceType } from '../../lib/briefing-types'

import { AppDialog } from '../common/AppDialog'

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
    <AppDialog
      isOpen={isOpen}
      onClose={onClose}
      title={source ? 'Edit Source' : 'Connect New Source'}
      footer={
        <>
          <button className="ghost-button" type="button" onClick={onClose} disabled={isSaving}>
            Cancel
          </button>
          <button className="primary-button" type="submit" form="source-modal-form" disabled={isSaving}>
            {isSaving ? 'Saving...' : 'Save Source'}
          </button>
        </>
      }
    >
      <form id="source-modal-form" onSubmit={handleSubmit} className="app-dialog__form">
        <div className="app-dialog__fields">
            <label className="settings-input-group">
              <span>Source Type</span>
              <div className="source-modal__select-wrap">
                <select 
                  className="settings-input source-modal__select" 
                  value={type} 
                  onChange={(e) => {
                    setType(e.target.value as SourceType)
                  }}
                >
                  <option value="ARTICLE_URL">Article Link</option>
                  <option value="RSS_FEED">RSS Feed</option>
                  <option value="PLAIN_TEXT">Plain Text</option>
                </select>
                <span className="material-symbols-outlined source-modal__select-icon">expand_more</span>
              </div>
            </label>

            <label className="settings-input-group">
              <span>Title</span>
              <input 
                autoFocus
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
                  className="settings-input source-modal__textarea" 
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

      </form>
    </AppDialog>
  )
}
