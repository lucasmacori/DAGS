import type { RefObject } from 'react'

import type { ChatMessage } from '../../lib/chat-utils'
import { parseMessageContent } from '../../lib/chat-utils'
import { CodeCard } from './CodeCard'
import { ConversationHistorySkeleton } from './ConversationHistorySkeleton'

type ChatThreadProps = {
  isLoadingHistory: boolean
  isSendingMessage: boolean
  messages: Array<ChatMessage>
  threadRef: RefObject<HTMLDivElement | null>
}

export function ChatThread({
  isLoadingHistory,
  isSendingMessage,
  messages,
  threadRef,
}: ChatThreadProps) {
  return (
    <div ref={threadRef} className="chat-thread" aria-label="Chat messages">
      {isLoadingHistory ? <ConversationHistorySkeleton /> : null}

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
                {parseMessageContent(messageItem.text).map((block, blockIndex) =>
                  block.type === 'text' ? (
                    <p key={blockIndex} className="chat-bubble__text">
                      {block.content}
                    </p>
                  ) : (
                    <CodeCard key={blockIndex} title={block.language} code={block.content} />
                  ),
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
  )
}
