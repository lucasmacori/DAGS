import { Link, useNavigate, useRouterState } from '@tanstack/react-router'

import { useConversations } from '../../lib/conversations'
import { sidebarUsers } from '../../lib/workspace-mocks'

const navigationItems = [
  { to: '/chat', label: 'Chat', icon: 'forum' },
  { to: '/translate', label: 'Translate', icon: 'translate' },
  { to: '/settings', label: 'Settings', icon: 'settings' },
] as const

const secondaryNavigationItems = [
  { to: '/history', label: 'History', icon: 'history' },
  { to: '/archive', label: 'Archive', icon: 'inventory_2' },
] as const

type SidebarProps = {
  isOpen: boolean
  onClose: () => void
}

export function Sidebar({ isOpen, onClose }: SidebarProps) {
  const pathname = useRouterState({
    select: (state) => state.location.pathname,
  })
  const navigate = useNavigate()
  const { conversations, activeConversation, setActiveConversation } = useConversations()

  const currentUser = pathname.startsWith('/settings')
    ? sidebarUsers.settings
    : pathname.startsWith('/translate')
      ? sidebarUsers.translate
      : sidebarUsers.chat

  return (
    <aside className={`sidebar${isOpen ? ' sidebar--open' : ''}`}>
      <div className="sidebar__brand-block">
        <p className="sidebar__eyebrow">DAGS</p>
        <div className="sidebar__brand">Dialogue Assistant Gateway Service</div>
      </div>

      <nav className="sidebar__nav" aria-label="Primary navigation">
        {navigationItems.map((item) => (
          <div key={item.to} className="sidebar__nav-group">
            <Link
              to={item.to}
              className="sidebar__link"
              activeProps={{ className: 'sidebar__link sidebar__link--active' }}
              onClick={() => {
                if (item.to === '/chat') {
                  setActiveConversation(null)
                }
                onClose()
              }}
            >
              <span className="material-symbols-outlined sidebar__icon">{item.icon}</span>
              {item.label}
            </Link>

            {item.to === '/chat' && conversations.length > 0 ? (
              <div className="sidebar__sublist">
                {conversations.map((conversation) => (
                  <button
                    key={conversation.conversationId}
                    type="button"
                    className={`sidebar__sublink ${activeConversation?.conversationId === conversation.conversationId ? 'sidebar__sublink--active' : ''}`}
                    onClick={() => {
                      setActiveConversation(conversation)
                      navigate({ to: '/chat' })
                      onClose()
                    }}
                  >
                    {conversation.conversationName}
                  </button>
                ))}
              </div>
            ) : null}
          </div>
        ))}
      </nav>

      <div className="sidebar__footer">
        <nav className="sidebar__secondary-nav" aria-label="Secondary navigation">
          {secondaryNavigationItems.map((item) => (
            <Link
              key={item.to}
              to={item.to}
              className="sidebar__link sidebar__link--secondary"
              activeProps={{ className: 'sidebar__link sidebar__link--secondary sidebar__link--active' }}
              onClick={onClose}
            >
              <span className="material-symbols-outlined sidebar__icon">{item.icon}</span>
              {item.label}
            </Link>
          ))}
        </nav>

        <div className="sidebar__profile">
          <div className="sidebar__avatar-frame">
            <img
              className="sidebar__avatar-image"
              src={currentUser.avatar}
              alt="Workspace profile avatar"
            />
          </div>
          <div>
            <p className="sidebar__profile-name">{currentUser.name}</p>
            <p className="sidebar__profile-plan">{currentUser.role}</p>
          </div>
        </div>
      </div>
    </aside>
  )
}
