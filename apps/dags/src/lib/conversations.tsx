import { createContext, useContext, useState, useEffect, type ReactNode } from 'react'

export type Conversation = {
  conversationId: string
  conversationName: string
  createdAt: string
}

type ConversationContextType = {
  conversations: Conversation[]
  activeConversation: Conversation | null
  setActiveConversation: (c: Conversation | null) => void
  refreshConversations: () => Promise<void>
}

const ConversationContext = createContext<ConversationContextType | null>(null)

export function ConversationProvider({ children }: { children: ReactNode }) {
  const [conversations, setConversations] = useState<Conversation[]>([])
  const [activeConversation, setActiveConversation] = useState<Conversation | null>(null)

  async function refreshConversations() {
    try {
      const response = await fetch('/conversation')
      if (response.ok) {
        const data = await response.json()
        // Sort newest first
        data.sort((a: Conversation, b: Conversation) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
        setConversations(data)
      }
    } catch (err) {
      console.error('Failed to load conversations:', err)
    }
  }

  useEffect(() => {
    refreshConversations()
  }, [])

  return (
    <ConversationContext.Provider
      value={{
        conversations,
        activeConversation,
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
