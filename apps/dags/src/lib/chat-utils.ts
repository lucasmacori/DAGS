export type ChatMessage = {
  role: 'user' | 'assistant'
  text: string
  timestamp: string
  id?: string
  title?: string
  trailingText?: string
  codeTitle?: string
  code?: string
}

export type ParsedBlock =
  | { type: 'text'; content: string }
  | { type: 'code'; language: string; content: string }

export type ConversationHistoryResponse = {
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

type ScrollPosition = {
  clientHeight: number
  scrollHeight: number
  scrollTop: number
}

export function isScrolledNearBottom(scrollPosition: ScrollPosition, threshold = 80) {
  return (
    scrollPosition.scrollHeight - scrollPosition.scrollTop - scrollPosition.clientHeight <=
    threshold
  )
}

export function extractStreamText(buffer: string) {
  const normalizedBuffer = buffer.replace(/\r\n/g, '\n')
  const events = normalizedBuffer.split('\n\n')
  const trailingLine = events.pop() ?? ''
  let extractedText = ''

  for (const event of events) {
    const lines = event.split('\n')
    const dataLines: string[] = []

    for (const line of lines) {
      if (!line.startsWith('data:')) {
        continue
      }

      const value = line.slice(5)

      if (value !== '[DONE]') {
        dataLines.push(value)
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

export function parseMessageContent(text: string): ParsedBlock[] {
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

export function formatChatTimestamp(value: string) {
  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return value
  }

  return new Intl.DateTimeFormat('en-US', {
    hour: 'numeric',
    minute: '2-digit',
  }).format(date)
}

export function mapConversationHistoryMessages(
  payload: ConversationHistoryResponse,
): Array<ChatMessage> {
  return [...payload.messages].reverse().map((item) => ({
    id: item.message_id,
    role: item.role === 'ASSISTANT' ? 'assistant' : 'user',
    text: item.content,
    timestamp: formatChatTimestamp(item.created_at),
    title: item.role === 'ASSISTANT' ? 'DAGS AI' : undefined,
  }))
}
