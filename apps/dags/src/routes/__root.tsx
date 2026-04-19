import { TanStackDevtools } from '@tanstack/react-devtools'
import {
  HeadContent,
  Link,
  Scripts,
  createRootRoute,
  useRouterState,
  useNavigate,
} from '@tanstack/react-router'
import { TanStackRouterDevtoolsPanel } from '@tanstack/react-router-devtools'

import { sidebarUsers } from '../lib/workspace-mocks'
import { ConversationProvider, useConversations } from '../lib/conversations'
import appCss from '../styles/app.scss?url'

const navigationItems = [
  { to: '/chat', label: 'Chat', icon: 'forum' },
  { to: '/translate', label: 'Translate', icon: 'translate' },
  { to: '/settings', label: 'Settings', icon: 'settings' },
] as const

const secondaryNavigationItems = [
  { to: '/history', label: 'History', icon: 'history' },
  { to: '/archive', label: 'Archive', icon: 'inventory_2' },
] as const

export const Route = createRootRoute({
  head: () => ({
    meta: [
      {
        charSet: 'utf-8',
      },
      {
        name: 'viewport',
        content: 'width=device-width, initial-scale=1',
      },
      {
        title: 'DAGS',
      },
    ],
    links: [
      {
        rel: 'stylesheet',
        href: appCss,
      },
      {
        rel: 'preconnect',
        href: 'https://fonts.googleapis.com',
      },
      {
        rel: 'preconnect',
        href: 'https://fonts.gstatic.com',
        crossOrigin: 'anonymous',
      },
      {
        rel: 'stylesheet',
        href: 'https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&family=JetBrains+Mono:wght@400;500&display=swap',
      },
      {
        rel: 'stylesheet',
        href: 'https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap',
      },
    ],
  }),
  shellComponent: RootDocument,
})

function Sidebar() {
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
    <aside className="sidebar">
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
            >
              <span className="material-symbols-outlined sidebar__icon">{item.icon}</span>
              {item.label}
            </Link>
            
            {item.to === '/chat' && conversations.length > 0 && (
              <div className="sidebar__sublist">
                {conversations.map((conv) => (
                  <button
                    key={conv.conversationId}
                    type="button"
                    className={`sidebar__sublink ${activeConversation?.conversationId === conv.conversationId ? 'sidebar__sublink--active' : ''}`}
                    onClick={() => {
                      setActiveConversation(conv)
                      navigate({ to: '/chat' })
                    }}
                  >
                    {conv.conversationName}
                  </button>
                ))}
              </div>
            )}
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

function RootDocument({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <head>
        <HeadContent />
      </head>
      <body>
        <ConversationProvider>
          <div className="app-shell">
            <Sidebar />
            <main className="app-content">{children}</main>
          </div>
        </ConversationProvider>
        <TanStackDevtools
          config={{
            position: 'bottom-right',
          }}
          plugins={[
            {
              name: 'Tanstack Router',
              render: <TanStackRouterDevtoolsPanel />,
            },
          ]}
        />
        <Scripts />
      </body>
    </html>
  )
}
