/** @vitest-environment jsdom */

import { fireEvent, render, screen } from '@testing-library/react'
import { createRef } from 'react'
import { describe, expect, it, vi } from 'vitest'

import { ChatComposer } from './ChatComposer'

describe('ChatComposer', () => {
  it('submits on Enter and keeps newline on Shift+Enter', () => {
    const onSubmit = vi.fn((event) => event.preventDefault())
    const onChange = vi.fn()
    const requestSubmitSpy = vi
      .spyOn(HTMLFormElement.prototype, 'requestSubmit')
      .mockImplementation(function requestSubmitMock() {
        fireEvent.submit(this)
      })

    render(
      <ChatComposer
        composerRef={createRef()}
        disabled={false}
        message="Hello"
        model="gemma4:e2b"
        onChange={onChange}
        onSubmit={onSubmit}
      />,
    )

    const textarea = screen.getByLabelText('Chat message')

    fireEvent.keyDown(textarea, { key: 'Enter' })
    expect(onSubmit).toHaveBeenCalledTimes(1)

    fireEvent.keyDown(textarea, { key: 'Enter', shiftKey: true })
    expect(onSubmit).toHaveBeenCalledTimes(1)

    requestSubmitSpy.mockRestore()
  })
})
