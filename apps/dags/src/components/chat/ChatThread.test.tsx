/** @vitest-environment jsdom */

import { fireEvent, render, screen } from '@testing-library/react'
import { createRef } from 'react'
import { describe, expect, it } from 'vitest'

import { ChatThread } from './ChatThread'

describe('ChatThread', () => {
  it('renders assistant Markdown from API responses', () => {
    render(
      <ChatThread
        isLoadingHistory={false}
        isSendingMessage={false}
        messages={[
          {
            role: 'assistant',
            title: 'DAGS AI',
            text: '**Important**\n\n- First item\n\n[Docs](https://example.com)\n\n<script>alert("xss")</script>',
            timestamp: '10:30 AM',
          },
        ]}
        threadRef={createRef()}
      />,
    )

    expect(screen.getByText('Important').tagName).toBe('STRONG')
    expect(screen.getByText('First item').closest('ul')).not.toBeNull()

    const link = screen.getByRole('link', { name: 'Docs' })
    expect(link.getAttribute('href')).toBe('https://example.com')
    expect(link.getAttribute('target')).toBe('_blank')
    expect(link.getAttribute('rel')).toBe('noreferrer')
    expect(screen.getByText(/<script>alert\("xss"\)<\/script>/)).toBeTruthy()
  })

  it('renders assistant sources collapsed by default', () => {
    render(
      <ChatThread
        isLoadingHistory={false}
        isSendingMessage={false}
        messages={[
          {
            role: 'assistant',
            title: 'DAGS AI',
            text: 'Answer with sources.',
            timestamp: '10:30 AM',
            sources: [
              {
                title: 'Source title',
                url: 'https://example.com/source',
                content: 'Source content',
              },
            ],
          },
        ]}
        threadRef={createRef()}
      />,
    )

    const summary = screen.getByText('Sources (1)')
    const details = summary.closest('details')

    expect(details?.hasAttribute('open')).toBe(false)
    fireEvent.click(summary)
    expect(details?.hasAttribute('open')).toBe(true)

    const link = screen.getByRole('link', { name: 'Source title' })
    expect(link.getAttribute('href')).toBe('https://example.com/source')
  })
})
