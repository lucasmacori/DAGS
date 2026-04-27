import { useEffect, useRef, useState, type FormEvent } from 'react'
import { createFileRoute } from '@tanstack/react-router'

import { ChatComposer } from '../components/chat/ChatComposer'
import { ConversationRenameDialog } from '../components/chat/ConversationRenameDialog'
import { ChatThread } from '../components/chat/ChatThread'
import { Topbar } from '../components/layout/Topbar'
import { getAiToolsApiConfig } from '../lib/ai-tools-api'
import type {
  UploadChatDocumentsResponse,
  UploadedChatDocument,
} from '../lib/chat-document-types'
import { mapUploadedChatDocumentsResponse } from '../lib/chat-document-types'
import { useConversations } from '../lib/conversations'
import {
  extractStreamText,
  mapConversationHistoryMessages,
  type ChatMessage,
  type ConversationHistoryResponse,
} from '../lib/chat-utils'

const modelOptions = [
  { value: '', label: 'Default Model' },
  { value: 'gemma4:e2b', label: 'gemma4:e2b' },
  { value: 'gemma:e4b', label: 'gemma:e4b' },
]

export const Route = createFileRoute('/chat')({
  server: {
    handlers: {
      POST: async ({ request }) => {
        let apiBaseUrl: string
        let authorization: string

        try {
          const config = getAiToolsApiConfig()
          apiBaseUrl = config.apiBaseUrl
          authorization = config.authorization
        } catch (error) {
          return new Response(
            error instanceof Error ? error.message : 'API configuration is invalid.',
            {
              status: 500,
              headers: {
                'Content-Type': 'text/plain; charset=utf-8',
              },
            },
          )
        }

        const body = (await request.json()) as Partial<{
          chat_id: string
          document_ids?: string[]
          message: string
          model?: string
        }>

        const payload: {
          chat_id: string
          document_ids?: string[]
          message: string
          model?: string
        } = {
          chat_id: body.chat_id?.trim() ?? '',
          message: body.message?.trim() ?? '',
        }
        
        if (body.model?.trim()) {
          payload.model = body.model.trim()
        }

        if (body.document_ids?.length) {
          payload.document_ids = body.document_ids
        }

        if (!payload.chat_id || !payload.message) {
          return new Response('chat_id and message are required.', {
            status: 400,
            headers: {
              'Content-Type': 'text/plain; charset=utf-8',
            },
          })
        }

        const upstreamResponse = await fetch(`${apiBaseUrl}/chat`, {
          method: 'POST',
          headers: {
            Authorization: authorization,
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(payload),
        })

        if (!upstreamResponse.ok) {
          const message = await upstreamResponse.text()

          return new Response(message || 'Could not send chat message.', {
            status: upstreamResponse.status,
            headers: {
              'Content-Type': 'text/plain; charset=utf-8',
            },
          })
        }

        return new Response(upstreamResponse.body, {
          status: upstreamResponse.status,
          headers: {
            'Cache-Control': 'no-store',
            'Content-Type':
              upstreamResponse.headers.get('content-type') ??
              'text/event-stream; charset=utf-8',
          },
        })
      },
    },
  },
  component: ChatPage,
})

function ChatPage() {
  const { activeConversation, renameConversation, setActiveConversation, refreshConversations } = useConversations()
  const [message, setMessage] = useState('')
  const [messages, setMessages] = useState<Array<ChatMessage>>([])
  const [isStartingChat, setIsStartingChat] = useState(false)
  const [isSendingMessage, setIsSendingMessage] = useState(false)
  const [isLoadingHistory, setIsLoadingHistory] = useState(false)
  const [isRenameDialogOpen, setIsRenameDialogOpen] = useState(false)
  const [isUploadingDocuments, setIsUploadingDocuments] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [chatModel, setChatModel] = useState('')
  const [uploadedDocuments, setUploadedDocuments] = useState<UploadedChatDocument[]>([])
  const threadRef = useRef<HTMLDivElement | null>(null)
  const composerRef = useRef<HTMLTextAreaElement | null>(null)

  function getTimestamp() {
    return new Intl.DateTimeFormat('en-US', {
      hour: 'numeric',
      minute: '2-digit',
    }).format(new Date())
  }

  useEffect(() => {
    let isCancelled = false

    async function loadHistory() {
      if (!activeConversation?.conversationId) {
        setMessages([])
        setError(null)
        composerRef.current?.focus()
        return
      }

      setMessages([])
      setError(null)
      setIsLoadingHistory(true)

      try {
        const response = await fetch(
          `/conversation/${activeConversation.conversationId}/history?page=0`,
        )

        if (!response.ok) {
          const message = (await response.text()).trim()
          throw new Error(message || 'Could not load conversation history.')
        }

        const payload = (await response.json()) as ConversationHistoryResponse

        if (!isCancelled) {
          setMessages(mapConversationHistoryMessages(payload))
        }
      } catch (caughtError) {
        if (!isCancelled) {
          setError(
            caughtError instanceof Error
              ? caughtError.message
              : 'Could not load conversation history.',
          )
        }
      } finally {
        if (!isCancelled) {
          setIsLoadingHistory(false)
          composerRef.current?.focus()
        }
      }
    }

    void loadHistory()

    return () => {
      isCancelled = true
    }
  }, [activeConversation])

  useEffect(() => {
    const threadElement = threadRef.current

    if (!threadElement) {
      return
    }

    threadElement.scrollTop = threadElement.scrollHeight
  }, [messages, isSendingMessage])

  async function handleDocumentsSelected(files: File[]) {
    if (files.length === 0) {
      return
    }

    setIsUploadingDocuments(true)
    setError(null)

    try {
      const formData = new FormData()

      for (const file of files) {
        formData.append('files', file)
      }

      const response = await fetch('/chat/document', {
        method: 'POST',
        body: formData,
      })

      if (!response.ok) {
        const message = (await response.text()).trim()
        throw new Error(message || 'Could not upload document.')
      }

      const payload = (await response.json()) as UploadChatDocumentsResponse
      const mappedDocuments = mapUploadedChatDocumentsResponse(payload)

      setUploadedDocuments((currentDocuments) => [
        ...currentDocuments,
        ...mappedDocuments,
      ])
    } catch (caughtError) {
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : 'Could not upload document.',
      )
    } finally {
      setIsUploadingDocuments(false)
    }
  }

  async function handleRemoveDocument(documentId: string) {
    try {
      const response = await fetch(`/chat/document/${documentId}`, {
        method: 'DELETE',
      })

      if (!response.ok) {
        const message = (await response.text()).trim()
        throw new Error(message || 'Could not delete document.')
      }

      setUploadedDocuments((currentDocuments) =>
        currentDocuments.filter((document) => document.documentId !== documentId),
      )
    } catch (caughtError) {
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : 'Could not delete document.',
      )
    }
  }

  async function cleanupUploadedDocuments(documentIds: string[]) {
    await Promise.allSettled(
      documentIds.map((documentId) =>
        fetch(`/chat/document/${documentId}`, {
          method: 'DELETE',
        }),
      ),
    )
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (isStartingChat || isSendingMessage) {
      return
    }

    const nextMessage = message.trim()

    if (!nextMessage) {
      setError('Enter a message to start chatting.')
      return
    }

    setError(null)
    setIsSendingMessage(true)

    try {
      let nextChatId = activeConversation?.conversationId

      if (!nextChatId) {
        setIsStartingChat(true)

        const createChatResponse = await fetch('/conversation', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ name: 'Conversation' }),
        })

        if (!createChatResponse.ok) {
          const createChatMessage = (await createChatResponse.text()).trim()
          throw new Error(createChatMessage || 'Could not create chat.')
        }

        const createChatPayload = (await createChatResponse.json()) as {
          conversationId?: string
          chat_id?: string
        }

        nextChatId = (createChatPayload.conversationId || createChatPayload.chat_id)?.trim() ?? ''

        if (!nextChatId) {
          throw new Error('Chat service did not return a conversationId.')
        }

        const newConv = {
          conversationId: nextChatId,
          conversationName: 'Conversation',
          createdAt: new Date().toISOString(),
        }
        setActiveConversation(newConv)
        const refreshedConversations = await refreshConversations()
        const createdConversation = refreshedConversations.find(
          (conversation) => conversation.conversationId === nextChatId,
        )

        if (createdConversation) {
          setActiveConversation(createdConversation)
        }
        setIsStartingChat(false)
      }

      setMessages((currentMessages) => [
        ...currentMessages,
        { role: 'user', text: nextMessage, timestamp: getTimestamp() },
      ])
      setMessage('')
      const sentDocumentIds = uploadedDocuments.map((document) => document.documentId)

      const chatResponse = await fetch('/chat', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          chat_id: nextChatId,
          message: nextMessage,
          ...(chatModel ? { model: chatModel } : {}),
          ...(sentDocumentIds.length > 0 ? { document_ids: sentDocumentIds } : {}),
        }),
      })

      if (!chatResponse.ok) {
        const chatErrorMessage = (await chatResponse.text()).trim()
        throw new Error(chatErrorMessage || 'Could not send chat message.')
      }

      if (!chatResponse.body) {
        setMessages((currentMessages) => [
          ...currentMessages,
          { role: 'assistant', title: 'DAGS AI', text: '', timestamp: getTimestamp() },
        ])
        setUploadedDocuments([])
        await cleanupUploadedDocuments(sentDocumentIds)
        return
      }

      const reader = chatResponse.body.getReader()
      const decoder = new TextDecoder()
      let assistantText = ''
      let hasAddedAssistantMessage = false
      let chunkBuffer = ''

      while (true) {
        const { value, done } = await reader.read()

        if (done) {
          break
        }

        const chunk = decoder.decode(value, { stream: true })

        if (!chunk) {
          continue
        }

        chunkBuffer += chunk

        const { extractedText, trailingLine } = extractStreamText(chunkBuffer)
        chunkBuffer = trailingLine

        if (!extractedText) {
          continue
        }

        assistantText += extractedText

        if (!hasAddedAssistantMessage) {
          hasAddedAssistantMessage = true
          setMessages((currentMessages) => [
            ...currentMessages,
            {
              role: 'assistant',
              title: 'DAGS AI',
              text: assistantText,
              timestamp: getTimestamp(),
            },
          ])
          continue
        }

        setMessages((currentMessages) => {
          const nextMessages = [...currentMessages]
          const lastMessage = nextMessages.at(-1)

          if (lastMessage?.role === 'assistant') {
            nextMessages[nextMessages.length - 1] = {
              role: 'assistant',
              title: 'DAGS AI',
              text: assistantText,
              timestamp: lastMessage.timestamp,
            }
          }

          return nextMessages
        })
      }

      chunkBuffer += decoder.decode()

      const { extractedText: finalText } = extractStreamText(chunkBuffer)

      if (finalText) {
        assistantText += finalText
      }

      if (!hasAddedAssistantMessage) {
        setMessages((currentMessages) => [
          ...currentMessages,
          {
            role: 'assistant',
            title: 'DAGS AI',
            text: assistantText,
            timestamp: getTimestamp(),
          },
        ])
      } else if (finalText) {
        setMessages((currentMessages) => {
          const nextMessages = [...currentMessages]
          const lastMessage = nextMessages.at(-1)

          if (lastMessage?.role === 'assistant') {
            nextMessages[nextMessages.length - 1] = {
              role: 'assistant',
              title: 'DAGS AI',
              text: assistantText,
              timestamp: lastMessage.timestamp,
            }
          }

          return nextMessages
        })
      }

      setUploadedDocuments([])
      await cleanupUploadedDocuments(sentDocumentIds)
    } catch (caughtError) {
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : 'Could not send chat message.',
      )
    } finally {
      setIsStartingChat(false)
      setIsSendingMessage(false)
    }
  }

  const displayTitle = activeConversation?.conversationName || 'Conversation'

  async function handleRenameConversation(nextName: string) {
    if (!activeConversation) {
      return
    }

    await renameConversation(activeConversation.conversationId, { name: nextName })
  }

  return (
    <section className="chat-screen">
      <Topbar
        title={displayTitle}
        pill={chatModel || 'Default Model'}
        titleActions={
          activeConversation ? (
            <button
              className="topbar__title-action"
              type="button"
              aria-label="Rename conversation"
              onClick={() => {
                setIsRenameDialogOpen(true)
              }}
            >
              <span className="material-symbols-outlined">edit</span>
            </button>
          ) : null
        }
      />

      <ChatThread
        isLoadingHistory={isLoadingHistory}
        isSendingMessage={isSendingMessage}
        messages={messages}
        threadRef={threadRef}
      />

      <ChatComposer
        composerRef={composerRef}
        disabled={isStartingChat || isSendingMessage}
        isUploadingDocuments={isUploadingDocuments}
        message={message}
        model={chatModel}
        modelOptions={modelOptions}
        onChange={setMessage}
        onDocumentsSelected={handleDocumentsSelected}
        onModelChange={setChatModel}
        onRemoveDocument={handleRemoveDocument}
        onSubmit={handleSubmit}
        uploadedDocuments={uploadedDocuments}
      />

      <p className="chat-disclaimer">
        {error ?? (activeConversation?.conversationId ? `Chat ID: ${activeConversation.conversationId}` : 'DAGS can make mistakes. Verify critical technical information.')}
      </p>

      {activeConversation ? (
        <ConversationRenameDialog
          initialName={activeConversation.conversationName}
          isOpen={isRenameDialogOpen}
          onClose={() => {
            setIsRenameDialogOpen(false)
          }}
          onSave={async (nextName) => {
            try {
              await handleRenameConversation(nextName)
            } catch (caughtError) {
              setError(
                caughtError instanceof Error
                  ? caughtError.message
                  : 'Could not rename conversation.',
              )
              throw caughtError
            }
          }}
        />
      ) : null}
    </section>
  )
}
