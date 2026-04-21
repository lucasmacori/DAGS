import { useEffect, useId, type ReactNode } from 'react'

type AppDialogProps = {
  children: ReactNode
  footer?: ReactNode
  isOpen: boolean
  onClose: () => void
  title: string
}

export function AppDialog({ children, footer, isOpen, onClose, title }: AppDialogProps) {
  const titleId = useId()

  useEffect(() => {
    if (!isOpen) {
      return
    }

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape') {
        onClose()
      }
    }

    window.addEventListener('keydown', handleKeyDown)

    return () => {
      window.removeEventListener('keydown', handleKeyDown)
    }
  }, [isOpen, onClose])

  if (!isOpen) {
    return null
  }

  return (
    <div
      className="app-dialog-overlay"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) {
          onClose()
        }
      }}
    >
      <div className="app-dialog" role="dialog" aria-modal="true" aria-labelledby={titleId}>
        <header className="app-dialog__header">
          <h2 id={titleId} className="app-dialog__title">
            {title}
          </h2>
          <button className="icon-button" type="button" onClick={onClose} aria-label="Close dialog">
            <span className="material-symbols-outlined">close</span>
          </button>
        </header>

        <div className="app-dialog__body">{children}</div>

        {footer ? <footer className="app-dialog__footer">{footer}</footer> : null}
      </div>
    </div>
  )
}
