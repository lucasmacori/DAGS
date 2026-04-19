import { useState, type FormEvent } from 'react'
import { createFileRoute } from '@tanstack/react-router'

type ChatMessage = {
  role: 'user' | 'assistant'
  text: string
}

const chatModel = 'gemma4:e2b'

function extractStreamText(buffer: string) {
  const normalizedBuffer = buffer.replace(/\r\n/g, '\n')
  const lines = normalizedBuffer.split('\n')
  const trailingLine = normalizedBuffer.endsWith('\n') ? '' : (lines.pop() ?? '')
  let extractedText = ''

  for (const line of lines) {
    if (!line.startsWith('data:')) {
      continue
    }

    const value = line.slice(5)

    if (value === '[DONE]') {
      continue
    }

    extractedText += value
  }

  return {
    extractedText,
    trailingLine,
  }
}

export const Route = createFileRoute('/chat')({
  component: ChatPage,
})

function ChatPage() {
  const [message, setMessage] = useState('')
  const [chatId, setChatId] = useState<string | null>(null)
  const [messages, setMessages] = useState<Array<ChatMessage>>([])
  const [isStartingChat, setIsStartingChat] = useState(false)
  const [isSendingMessage, setIsSendingMessage] = useState(false)
  const [error, setError] = useState<string | null>(null)

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
      let nextChatId = chatId

      if (!nextChatId) {
        setIsStartingChat(true)

        const createChatResponse = await fetch('/api/generate-chat', {
          method: 'POST',
        })

        if (!createChatResponse.ok) {
          const createChatMessage = (await createChatResponse.text()).trim()
          throw new Error(createChatMessage || 'Could not create chat.')
        }

        const createChatPayload = (await createChatResponse.json()) as {
          chat_id?: string
        }

        nextChatId = createChatPayload.chat_id?.trim() ?? ''

        if (!nextChatId) {
          throw new Error('Chat service did not return a chat_id.')
        }

        setChatId(nextChatId)
        setIsStartingChat(false)
      }

      setMessages((currentMessages) => [
        ...currentMessages,
        { role: 'user', text: nextMessage },
      ])
      setMessage('')

      const chatResponse = await fetch('/api/chat', {
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
          { role: 'assistant', text: '' },
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
            { role: 'assistant', text: assistantText },
          ])
          continue
        }

        setMessages((currentMessages) => {
          const nextMessages = [...currentMessages]
          const lastMessage = nextMessages.at(-1)

          if (lastMessage?.role === 'assistant') {
            nextMessages[nextMessages.length - 1] = {
              role: 'assistant',
              text: assistantText,
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
          { role: 'assistant', text: assistantText },
        ])
      } else if (finalText) {
        setMessages((currentMessages) => {
          const nextMessages = [...currentMessages]
          const lastMessage = nextMessages.at(-1)

          if (lastMessage?.role === 'assistant') {
            nextMessages[nextMessages.length - 1] = {
              role: 'assistant',
              text: assistantText,
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

  return (
    <section className="chat-page">
      <div className="chat-panel">
        <header className="chat-panel__header">
          <div>
            <p className="chat-panel__eyebrow">Chat</p>
            <h1 className="chat-panel__title">Chatbot interface</h1>
            <p className="chat-panel__meta">Model: {chatModel}</p>
          </div>

          <p className="chat-panel__subtitle">
            A simple conversation workspace for your AI applications.
          </p>
        </header>

        <div className="chat-messages" aria-label="Chat messages">
          {messages.length === 0 ? (
            <div className="chat-empty-state">
              <p className="chat-empty-state__title">Start a new conversation</p>
              <p className="chat-empty-state__text">
                Send a message to create a chat and begin streaming replies from
                your AI application.
              </p>
            </div>
          ) : (
            messages.map((messageItem, index) => (
              <article
                key={`${messageItem.role}-${index}`}
                className={`chat-message chat-message--${messageItem.role}`}
              >
                <p className="chat-message__role">{messageItem.role}</p>
                <p className="chat-message__text">{messageItem.text}</p>
              </article>
            ))
          )}
        </div>

        <form className="chat-composer" onSubmit={handleSubmit}>
          <textarea
            className="chat-composer__textarea"
            aria-label="Chat message"
            placeholder="Ask something about your AI applications..."
            value={message}
            onChange={(event) => {
              setMessage(event.target.value)
            }}
          />

          <div className="chat-composer__footer">
            <div className="chat-composer__status">
              <p className="chat-composer__hint">
                {chatId ? `Chat ID: ${chatId}` : 'No active chat yet.'}
              </p>

              {error ? <p className="chat-composer__error">{error}</p> : null}
            </div>

            <button
              className="chat-composer__button"
              type="submit"
              disabled={isStartingChat || isSendingMessage}
            >
              {isStartingChat
                ? 'Starting chat...'
                : isSendingMessage
                  ? 'Sending...'
                  : 'Send message'}
            </button>
          </div>
        </form>
      </div>
    </section>
  )
}
