import type { FormEvent, KeyboardEvent, RefObject } from 'react'

import type { UploadedChatDocument } from '../../lib/chat-document-types'

type ChatComposerProps = {
  composerRef: RefObject<HTMLTextAreaElement | null>
  disabled: boolean
  isUploadingDocuments: boolean
  message: string
  model: string
  modelOptions: { value: string; label: string }[]
  onChange: (value: string) => void
  onDocumentsSelected: (files: File[]) => void
  onModelChange: (value: string) => void
  onRemoveDocument: (documentId: string) => void
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
  uploadedDocuments: UploadedChatDocument[]
}

export function ChatComposer({
  composerRef,
  disabled,
  isUploadingDocuments,
  message,
  model,
  modelOptions,
  onChange,
  onDocumentsSelected,
  onModelChange,
  onRemoveDocument,
  onSubmit,
  uploadedDocuments,
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
        {(uploadedDocuments.length > 0 || isUploadingDocuments) ? (
          <div className="chat-composer-attachments">
            {uploadedDocuments.map((document) => (
              <div key={document.documentId} className="chat-document-chip">
                <span className="material-symbols-outlined chat-document-chip__icon">description</span>
                <div className="chat-document-chip__content">
                  <span className="chat-document-chip__name">{document.filename}</span>
                  <span className="chat-document-chip__meta">{document.characterCount} chars</span>
                </div>
                <button
                  className="chat-document-chip__remove"
                  type="button"
                  aria-label={`Remove ${document.filename}`}
                  onClick={() => {
                    onRemoveDocument(document.documentId)
                  }}
                >
                  <span className="material-symbols-outlined">close</span>
                </button>
              </div>
            ))}

            {isUploadingDocuments ? (
              <div className="chat-document-chip chat-document-chip--uploading">
                <span className="material-symbols-outlined chat-document-chip__icon">upload</span>
                <div className="chat-document-chip__content">
                  <span className="chat-document-chip__name">Uploading document...</span>
                </div>
              </div>
            ) : null}
          </div>
        ) : null}

        <div className="chat-composer-main">
          <input
            name="chatDocuments"
            className="chat-composer-file-input"
            type="file"
            multiple
            accept=".txt,.md,.pdf,text/plain,text/markdown,application/pdf"
            onChange={(event) => {
              const files = Array.from(event.target.files ?? [])

              if (files.length > 0) {
                onDocumentsSelected(files)
              }

              event.currentTarget.value = ''
            }}
          />

          <button
            className="icon-button"
            type="button"
            aria-label="Add attachment"
            onClick={(event) => {
              const fileInput = event.currentTarget.form?.elements.namedItem(
                'chatDocuments',
              ) as HTMLInputElement | null

              fileInput?.click()
            }}
          >
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
