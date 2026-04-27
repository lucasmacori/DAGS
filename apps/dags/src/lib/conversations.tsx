import { createContext, useContext, useState, useEffect, type ReactNode } from 'react'

export type Conversation = {
  conversationId: string
  conversationName: string
  createdAt: string
}

type ConversationContextType = {
	conversations: Conversation[]
	activeConversation: Conversation | null
	renameConversation: (conversationId: string, updates: { name?: string }) => Promise<void>
	setActiveConversation: (c: Conversation | null) => void
	refreshConversations: () => Promise<Conversation[]>
}

const ConversationContext = createContext<ConversationContextType | null>(null)

export function ConversationProvider({ children }: { children: ReactNode }) {
  const [conversations, setConversations] = useState<Conversation[]>([])
  const [activeConversation, setActiveConversation] = useState<Conversation | null>(null)

	async function renameConversation(conversationId: string, updates: { name?: string }) {
		const nextName = updates.name?.trim()

    if (!nextName) {
      return
    }

    const response = await fetch(`/conversation/${conversationId}`, {
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ name: nextName }),
    })

    if (!response.ok) {
      throw new Error((await response.text()).trim() || 'Failed to rename conversation.')
    }

    setConversations((currentConversations) =>
      currentConversations.map((conversation) =>
        conversation.conversationId === conversationId
          ? { ...conversation, conversationName: nextName }
          : conversation,
      ),
    )

    setActiveConversation((currentConversation) =>
      currentConversation?.conversationId === conversationId
        ? { ...currentConversation, conversationName: nextName }
        : currentConversation,
    )
  }

  async function refreshConversations() {
    try {
      const response = await fetch('/conversation')
      if (response.ok) {
        const data = (await response.json()) as Conversation[]
        // Sort newest first
        data.sort((a: Conversation, b: Conversation) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
        setConversations(data)
			return data
      }
    } catch (err) {
      console.error('Failed to load conversations:', err)
    }

		return []
  }

  useEffect(() => {
    refreshConversations()
  }, [])

  return (
    <ConversationContext.Provider
      value={{
        conversations,
        activeConversation,
        renameConversation,
        setActiveConversation,
        refreshConversations,
      }}
    >
      {children}
    </ConversationContext.Provider>
  )
}

export function useConversations() {
  const context = useContext(ConversationContext)
  if (!context) {
    throw new Error('useConversations must be used within a ConversationProvider')
  }
  return context
}
