import { useEffect, useRef, useState, type FormEvent, type KeyboardEvent } from 'react'
import { createFileRoute } from '@tanstack/react-router'

import { getAiToolsApiConfig } from '../lib/ai-tools-api'
import { useConversations } from '../lib/conversations'

type ChatMessage = {
  role: 'user' | 'assistant'
  text: string
  timestamp: string
  id?: string
  title?: string
  trailingText?: string
  codeTitle?: string
  code?: string
}

type ConversationHistoryResponse = {
  page: number
  size: number
  messages: Array<{
    message_id: string
    conversation_id: string
    role: 'USER' | 'ASSISTANT'
    content: string
    created_at: string
  }>
}

const chatModel = 'gemma4:e2b'

function extractStreamText(buffer: string) {
  const normalizedBuffer = buffer.replace(/\r\n/g, '\n')
  const events = normalizedBuffer.split('\n\n')
  const trailingLine = events.pop() ?? ''
  let extractedText = ''

  for (const event of events) {
    const lines = event.split('\n')
    const dataLines: string[] = []

    for (const line of lines) {
      if (line.startsWith('data:')) {
        const value = line.slice(5)
        if (value !== '[DONE]') {
          dataLines.push(value)
        }
      }
    }

    if (dataLines.length > 0) {
      extractedText += dataLines.join('\n')
    }
  }

  return {
    extractedText,
    trailingLine,
  }
}

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
          message: string
          model?: string
        }>

        const payload = {
          chat_id: body.chat_id?.trim() ?? '',
          message: body.message?.trim() ?? '',
          model: body.model?.trim() || 'gemma4:e2b',
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
  const { activeConversation, setActiveConversation, refreshConversations } = useConversations()
  const [message, setMessage] = useState('')
  const [messages, setMessages] = useState<Array<ChatMessage>>([])
  const [isStartingChat, setIsStartingChat] = useState(false)
  const [isSendingMessage, setIsSendingMessage] = useState(false)
  const [isLoadingHistory, setIsLoadingHistory] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const threadRef = useRef<HTMLDivElement | null>(null)
  const composerRef = useRef<HTMLTextAreaElement | null>(null)

  function getTimestamp() {
    return new Intl.DateTimeFormat('en-US', {
      hour: 'numeric',
      minute: '2-digit',
    }).format(new Date())
  }

  function handleComposerKeyDown(event: KeyboardEvent<HTMLTextAreaElement>) {
    if (event.key !== 'Enter' || event.shiftKey) {
      return
    }

    event.preventDefault()
    event.currentTarget.form?.requestSubmit()
  }

  function formatTimestamp(value: string) {
    const date = new Date(value)

    if (Number.isNaN(date.getTime())) {
      return value
    }

    return new Intl.DateTimeFormat('en-US', {
      hour: 'numeric',
      minute: '2-digit',
    }).format(date)
  }

  function mapHistoryMessages(payload: ConversationHistoryResponse): Array<ChatMessage> {
    return [...payload.messages]
      .reverse()
      .map((item) => ({
        id: item.message_id,
        role: item.role === 'ASSISTANT' ? 'assistant' : 'user',
        text: item.content,
        timestamp: formatTimestamp(item.created_at),
        title: item.role === 'ASSISTANT' ? 'DAGS AI' : undefined,
      }))
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
          setMessages(mapHistoryMessages(payload))
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
          createdAt: new Date().toISOString()
        }
        setActiveConversation(newConv)
        await refreshConversations()
        setIsStartingChat(false)
      }

      setMessages((currentMessages) => [
        ...currentMessages,
        { role: 'user', text: nextMessage, timestamp: getTimestamp() },
      ])
      setMessage('')

      const chatResponse = await fetch('/chat', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          chat_id: nextChatId,
          message: nextMessage,
          model: chatModel,
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

  return (
    <section className="chat-screen">
      <header className="topbar">
        <div className="topbar__title-group">
          <button className="topbar__menu-button" type="button" aria-label="Open menu">
            <span className="material-symbols-outlined">menu</span>
          </button>
          <h1 className="topbar__title">{displayTitle}</h1>
          <span className="topbar__pill">Gemma4:e2b</span>
        </div>
      </header>

      <div ref={threadRef} className="chat-thread" aria-label="Chat messages">
        {isLoadingHistory ? (
          <ConversationHistorySkeleton />
        ) : null}

        {messages.map((messageItem, index) => (
          <article
            key={messageItem.id ?? `${messageItem.role}-${index}`}
            className={`chat-row chat-row--${messageItem.role}`}
          >
            {messageItem.role === 'assistant' ? (
              <div className="chat-assistant-block">
                <div className="chat-assistant-label">
                  <span className="chat-assistant-badge material-symbols-outlined">smart_toy</span>
                  <span>{messageItem.title}</span>
                </div>

                <div className="chat-assistant-bubble">
                  {parseMessageContent(messageItem.text).map((block, i) =>
                    block.type === 'text' ? (
                      <p key={i} className="chat-bubble__text">
                        {block.content}
                      </p>
                    ) : (
                      <CodeCard key={i} title={block.language} code={block.content} />
                    )
                  )}

                  {messageItem.code ? (
                    <CodeCard title={messageItem.codeTitle || 'Code'} code={messageItem.code} />
                  ) : null}

                  {messageItem.trailingText ? (
                    <p className="chat-bubble__text">{messageItem.trailingText}</p>
                  ) : null}
                </div>

                <div className="chat-assistant-meta">
                  <span>{messageItem.timestamp}</span>
                  <div className="chat-assistant-actions">
                    <span className="material-symbols-outlined">thumb_up</span>
                    <span className="material-symbols-outlined">thumb_down</span>
                    <span className="material-symbols-outlined">refresh</span>
                  </div>
                </div>
              </div>
            ) : (
              <div className="chat-user-block">
                <div className="chat-user-bubble">
                  <p className="chat-bubble__text">{messageItem.text}</p>
                </div>
                <span className="chat-user-time">{messageItem.timestamp}</span>
              </div>
            )}
          </article>
        ))}

        {isSendingMessage && messages.at(-1)?.role === 'user' ? (
          <article className="chat-row chat-row--assistant">
            <div className="chat-assistant-block">
              <div className="chat-assistant-label">
                <span className="chat-assistant-badge material-symbols-outlined">smart_toy</span>
                <span>DAGS AI</span>
              </div>

              <div className="chat-assistant-bubble chat-assistant-bubble--thinking">
                <p className="chat-bubble__text">Processing your message...</p>
              </div>
            </div>
          </article>
        ) : null}
      </div>

      <footer className="chat-composer-shell">
        <form className="chat-composer-panel" onSubmit={handleSubmit}>
          <div className="chat-composer-main">
            <button className="icon-button" type="button" aria-label="Add attachment">
              <span className="material-symbols-outlined">add_circle</span>
            </button>

            <textarea
              ref={composerRef}
              className="chat-composer-input"
              aria-label="Chat message"
              placeholder="Message DAGS..."
              value={message}
              onKeyDown={handleComposerKeyDown}
              onChange={(event) => {
                setMessage(event.target.value)
              }}
            />

            <div className="chat-composer-actions">
              <button className="icon-button" type="button" aria-label="Use microphone">
                <span className="material-symbols-outlined">mic</span>
              </button>
              <button
                className="send-button"
                type="submit"
                aria-label="Send message"
                disabled={isStartingChat || isSendingMessage}
              >
                <span className="material-symbols-outlined">send</span>
              </button>
            </div>
          </div>

          <div className="chat-composer-meta">
            <div className="chat-composer-model">
              <span className="material-symbols-outlined">auto_awesome</span>
              <span>{chatModel}</span>
            </div>
            <span>DAGS v4.2.0-stable</span>
          </div>
        </form>

        <p className="chat-disclaimer">
          {error ?? (activeConversation?.conversationId ? `Chat ID: ${activeConversation.conversationId}` : 'DAGS can make mistakes. Verify critical technical information.')}
        </p>
      </footer>
    </section>
  )
}

type ParsedBlock =
  | { type: 'text'; content: string }
  | { type: 'code'; language: string; content: string }

function parseMessageContent(text: string): ParsedBlock[] {
  const blocks: ParsedBlock[] = []
  const codeBlockRegex = /```([\w-]*)\n([\s\S]*?)(```|$)/g
  let lastIndex = 0
  let match

  while ((match = codeBlockRegex.exec(text)) !== null) {
    if (match.index > lastIndex) {
      blocks.push({
        type: 'text',
        content: text.slice(lastIndex, match.index),
      })
    }

    blocks.push({
      type: 'code',
      language: match[1] || 'Code',
      content: match[2].trim(),
    })

    lastIndex = match.index + match[0].length
  }

  if (lastIndex < text.length) {
    blocks.push({
      type: 'text',
      content: text.slice(lastIndex),
    })
  }

  return blocks
}

function CodeCard({ code, title }: { code: string; title: string }) {
  const [copied, setCopied] = useState(false)

  function handleCopy() {
    navigator.clipboard.writeText(code)
    setCopied(true)
    setTimeout(() => {
      setCopied(false)
    }, 2000)
  }

  return (
    <section className="chat-code-card">
      <header className="chat-code-card__header">
        <span>{title || 'Code'}</span>
        <button
          className="icon-button"
          type="button"
          aria-label="Copy code"
          onClick={handleCopy}
        >
          <span className="material-symbols-outlined">
            {copied ? 'check' : 'content_copy'}
          </span>
        </button>
      </header>
      <pre className="chat-code-card__body">{code}</pre>
    </section>
  )
}

function ConversationHistorySkeleton() {
  return (
    <>
      <article className="chat-row chat-row--assistant" aria-hidden="true">
        <div className="chat-assistant-block chat-skeleton-block">
          <div className="chat-assistant-label chat-skeleton-label">
            <span className="chat-assistant-badge chat-skeleton-badge" />
            <span className="chat-skeleton-line chat-skeleton-line--label" />
          </div>

          <div className="chat-assistant-bubble chat-skeleton-bubble">
            <span className="chat-skeleton-line chat-skeleton-line--wide" />
            <span className="chat-skeleton-line chat-skeleton-line--medium" />
            <span className="chat-skeleton-line chat-skeleton-line--narrow" />
          </div>
        </div>
      </article>

      <article className="chat-row chat-row--user" aria-hidden="true">
        <div className="chat-user-block chat-skeleton-block">
          <div className="chat-user-bubble chat-skeleton-bubble chat-skeleton-bubble--user">
            <span className="chat-skeleton-line chat-skeleton-line--medium" />
            <span className="chat-skeleton-line chat-skeleton-line--short" />
          </div>
        </div>
      </article>

      <article className="chat-row chat-row--assistant" aria-hidden="true">
        <div className="chat-assistant-block chat-skeleton-block">
          <div className="chat-assistant-label chat-skeleton-label">
            <span className="chat-assistant-badge chat-skeleton-badge" />
            <span className="chat-skeleton-line chat-skeleton-line--label" />
          </div>

          <div className="chat-assistant-bubble chat-skeleton-bubble">
            <span className="chat-skeleton-line chat-skeleton-line--wide" />
            <span className="chat-skeleton-line chat-skeleton-line--wide" />
            <span className="chat-skeleton-line chat-skeleton-line--short" />
          </div>
        </div>
      </article>
    </>
  )
}
