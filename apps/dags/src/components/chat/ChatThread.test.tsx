/** @vitest-environment jsdom */

import { render, screen } from '@testing-library/react'
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
    expect(screen.queryByText(/alert\("xss"\)/)).toBeNull()
  })
})
