import { useEffect, useState } from 'react'

import { AppDialog } from '../common/AppDialog'

type ConversationRenameDialogProps = {
  initialName: string
  isOpen: boolean
  onClose: () => void
  onSave: (nextName: string) => Promise<void>
}

export function ConversationRenameDialog({
  initialName,
  isOpen,
  onClose,
  onSave,
}: ConversationRenameDialogProps) {
  const [name, setName] = useState(initialName)
  const [error, setError] = useState<string | null>(null)
  const [isSaving, setIsSaving] = useState(false)

  useEffect(() => {
    if (!isOpen) {
      return
    }

    setName(initialName)
    setError(null)
  }, [initialName, isOpen])

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault()

    if (!name.trim()) {
      setError('Title is required.')
      return
    }

    setIsSaving(true)
    setError(null)

    try {
      await onSave(name)
      onClose()
    } catch (caughtError) {
      setError(
        caughtError instanceof Error ? caughtError.message : 'Could not rename conversation.',
      )
    } finally {
      setIsSaving(false)
    }
  }

  return (
    <AppDialog
      isOpen={isOpen}
      onClose={onClose}
      title="Rename conversation"
      footer={
        <>
          <button className="ghost-button" type="button" onClick={onClose} disabled={isSaving}>
            Cancel
          </button>
          <button className="primary-button" type="submit" form="conversation-rename-form" disabled={isSaving}>
            {isSaving ? 'Saving...' : 'Save'}
          </button>
        </>
      }
    >
      <form id="conversation-rename-form" className="app-dialog__form" onSubmit={handleSubmit}>
        <label className="settings-input-group">
          <span>Title</span>
          <input
            autoFocus
            className="settings-input"
            type="text"
            value={name}
            onChange={(event) => {
              setName(event.target.value)
            }}
          />
        </label>

        {error ? <p className="translate-error-banner">{error}</p> : null}
      </form>
    </AppDialog>
  )
}
