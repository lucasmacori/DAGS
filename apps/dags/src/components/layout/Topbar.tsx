import type { ReactNode } from 'react'

type TopbarProps = {
  actions?: ReactNode
  pill?: string
  titleActions?: ReactNode
  title: string
}

export function Topbar({ actions, pill, title, titleActions }: TopbarProps) {
  return (
    <header className="topbar">
      <div className="topbar__title-group">
        <h1 className="topbar__title">{title}</h1>
        {titleActions ? <div className="topbar__title-actions">{titleActions}</div> : null}
        {pill ? <span className="topbar__pill">{pill}</span> : null}
      </div>

      {actions ? <div className="topbar__actions">{actions}</div> : null}
    </header>
  )
}
