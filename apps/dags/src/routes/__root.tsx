import { TanStackDevtools } from '@tanstack/react-devtools'
import {
  HeadContent,
  Scripts,
  createRootRoute,
  redirect,
} from '@tanstack/react-router'
import { TanStackRouterDevtoolsPanel } from '@tanstack/react-router-devtools'
import { useState, type ReactNode } from 'react'

import { Sidebar } from '../components/layout/Sidebar'
import { getAuthStateFn } from '../lib/auth'
import { ConversationProvider } from '../lib/conversations'
import appCss from '../styles/app.scss?url'

export const Route = createRootRoute({
  beforeLoad: async ({ location }) => {
    const auth = await getAuthStateFn()

    if (!auth.user && location.pathname !== '/login') {
      throw redirect({
        search: {
          redirect: location.href,
        },
        to: '/login',
      })
    }

    if (auth.user && location.pathname === '/login') {
      throw redirect({ to: '/chat' })
    }

    return auth
  },
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

function RootDocument({ children }: { children: ReactNode }) {
  const [isSidebarOpen, setIsSidebarOpen] = useState(false)
  const { user } = Route.useRouteContext()

  if (!user) {
    return (
      <html lang="en">
        <head>
          <HeadContent />
        </head>
        <body>
          {children}
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

  return (
    <html lang="en">
      <head>
        <HeadContent />
      </head>
      <body>
        <ConversationProvider>
          <div className={`app-shell${isSidebarOpen ? ' app-shell--sidebar-open' : ''}`}>
            <Sidebar
              isOpen={isSidebarOpen}
              onClose={() => {
                setIsSidebarOpen(false)
              }}
            />
            <button
              className="sidebar-toggle"
              type="button"
              aria-label={isSidebarOpen ? 'Collapse sidebar' : 'Expand sidebar'}
              aria-expanded={isSidebarOpen}
              onClick={() => {
                setIsSidebarOpen((currentValue) => !currentValue)
              }}
            >
              <span className="material-symbols-outlined">
                {isSidebarOpen ? 'close' : 'menu'}
              </span>
            </button>
            {isSidebarOpen ? (
              <button
                className="sidebar-backdrop sidebar-backdrop--visible"
                type="button"
                aria-label="Close sidebar"
                onClick={() => {
                  setIsSidebarOpen(false)
                }}
              />
            ) : null}
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
