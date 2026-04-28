import { Link, useNavigate, useRouterState } from '@tanstack/react-router'
import { useEffect, useState } from 'react'
import { createPortal } from 'react-dom'

import { ConversationRenameDialog } from '../chat/ConversationRenameDialog'
import { useConversations } from '../../lib/conversations'
import { sidebarUsers } from '../../lib/workspace-mocks'

const navigationItems = [
  { to: '/chat', label: 'Chat', icon: 'forum' },
  { to: '/translate', label: 'Translate', icon: 'translate' },
  { to: '/briefing', label: 'Briefing', icon: 'summarize' },
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
  const {
    conversations,
    activeConversation,
    deleteConversation,
    renameConversation,
    setActiveConversation,
  } = useConversations()
  const [contextMenu, setContextMenu] = useState<{
    conversationId: string
    x: number
    y: number
  } | null>(null)
  const [renameConversationTarget, setRenameConversationTarget] = useState<{
    conversationId: string
    conversationName: string
  } | null>(null)

  useEffect(() => {
    function handleCloseContextMenu() {
      setContextMenu(null)
    }

    window.addEventListener('click', handleCloseContextMenu)
    window.addEventListener('blur', handleCloseContextMenu)

    return () => {
      window.removeEventListener('click', handleCloseContextMenu)
      window.removeEventListener('blur', handleCloseContextMenu)
    }
  }, [])

  async function handleRenameConversation(conversationId: string, nextName: string) {
    await renameConversation(conversationId, { name: nextName })
    setContextMenu(null)
  }

  async function handleDeleteConversation(conversationId: string) {
    const isActiveConversation = activeConversation?.conversationId === conversationId

    await deleteConversation(conversationId)
    setContextMenu(null)

    if (isActiveConversation) {
      navigate({ to: '/chat' })
      onClose()
    }
  }

  const currentUser = pathname.startsWith('/settings')
    ? sidebarUsers.settings
    : pathname.startsWith('/translate')
      ? sidebarUsers.translate
      : pathname.startsWith('/briefing')
        ? sidebarUsers.briefing
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
                    onContextMenu={(event) => {
                      event.preventDefault()
                      setContextMenu({
                        conversationId: conversation.conversationId,
                        x: event.clientX,
                        y: event.clientY,
                      })
                    }}
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

      {contextMenu && typeof document !== 'undefined'
        ? createPortal(
            <div
              className="briefing-card__menu-panel sidebar__context-menu"
              style={{ 
                left: contextMenu.x, 
                top: contextMenu.y
              }}
            >
              {(() => {
                const conversation = conversations.find(
                  (item) => item.conversationId === contextMenu.conversationId,
                )

                if (!conversation) {
                  return null
                }

                return (
                  <>
                    <button
                      className="briefing-card__menu-button"
                      type="button"
                      onClick={() => {
                        setRenameConversationTarget({
                          conversationId: conversation.conversationId,
                          conversationName: conversation.conversationName,
                        })
                        setContextMenu(null)
                      }}
                    >
                      <span className="material-symbols-outlined">edit</span>
                      Rename Conversation
                    </button>
                    <button
                      className="briefing-card__menu-button briefing-card__menu-button--danger"
                      type="button"
                      onClick={async () => {
                        try {
                          await handleDeleteConversation(conversation.conversationId)
                        } catch (caughtError) {
                          window.alert(
                            caughtError instanceof Error
                              ? caughtError.message
                              : 'Could not delete conversation.',
                          )
                        }
                      }}
                    >
                      <span className="material-symbols-outlined">delete</span>
                      Delete Conversation
                    </button>
                  </>
                )
              })()}
            </div>,
            document.body,
          )
        : null}

      {renameConversationTarget ? (
        <ConversationRenameDialog
          initialName={renameConversationTarget.conversationName}
          isOpen={Boolean(renameConversationTarget)}
          onClose={() => {
            setRenameConversationTarget(null)
          }}
          onSave={async (nextName) => {
            try {
              await handleRenameConversation(renameConversationTarget.conversationId, nextName)
              setRenameConversationTarget(null)
            } catch (caughtError) {
              window.alert(
                caughtError instanceof Error
                  ? caughtError.message
                  : 'Could not rename conversation.',
              )
              throw caughtError
            }
          }}
        />
      ) : null}
    </aside>
  )
}
