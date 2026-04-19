import type { ReactNode } from 'react'

type TopbarProps = {
  actions?: ReactNode
  pill?: string
  title: string
}

export function Topbar({ actions, pill, title }: TopbarProps) {
  return (
    <header className="topbar">
      <div className="topbar__title-group">
        <h1 className="topbar__title">{title}</h1>
        {pill ? <span className="topbar__pill">{pill}</span> : null}
      </div>

      {actions ? <div className="topbar__actions">{actions}</div> : null}
    </header>
  )
}
