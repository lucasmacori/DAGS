import { describe, expect, it } from 'vitest'

import {
  extractStreamText,
  isScrolledNearBottom,
  mapConversationHistoryMessages,
  parseMessageContent,
} from './chat-utils'

describe('chat utils', () => {
  it('preserves SSE newlines across data events', () => {
    const result = extractStreamText(
      'data:Hello\n\ndata:\n\ndata:```\ndata:python\ndata:print("hi")\n\n',
    )

    expect(result.extractedText).toBe('Hello```\npython\nprint("hi")')
    expect(result.trailingLine).toBe('')
  })

  it('parses text and code blocks separately', () => {
    const blocks = parseMessageContent('Intro\n```python\nprint("hi")\n```\nDone')

    expect(blocks).toEqual([
      { type: 'text', content: 'Intro\n' },
      { type: 'code', language: 'python', content: 'print("hi")' },
      { type: 'text', content: '\nDone' },
    ])
  })

  it('detects when a scroll container is close enough to the bottom', () => {
    expect(
      isScrolledNearBottom({ clientHeight: 100, scrollHeight: 500, scrollTop: 320 }),
    ).toBe(true)
    expect(
      isScrolledNearBottom({ clientHeight: 100, scrollHeight: 500, scrollTop: 200 }),
    ).toBe(false)
  })

  it('maps conversation history to oldest-first chat messages', () => {
    const messages = mapConversationHistoryMessages({
      page: 0,
      size: 20,
      messages: [
        {
          message_id: 'assistant-1',
          conversation_id: 'conversation-1',
          role: 'ASSISTANT',
          content: 'Assistant reply',
          created_at: '2026-04-19T23:12:11.256908',
        },
        {
          message_id: 'user-1',
          conversation_id: 'conversation-1',
          role: 'USER',
          content: 'User prompt',
          created_at: '2026-04-19T23:10:57.590122',
        },
      ],
    })

    expect(messages[0]?.role).toBe('user')
    expect(messages[1]?.role).toBe('assistant')
    expect(messages[1]?.title).toBe('DAGS AI')
  })
})
