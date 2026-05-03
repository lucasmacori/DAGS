/** @vitest-environment jsdom */

import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { createRef } from 'react'
import { afterEach, describe, expect, it, vi } from 'vitest'

import { ChatComposer } from './ChatComposer'

afterEach(() => {
  cleanup()
})

describe('ChatComposer', () => {
  function createChatComposerElement(
    composerRef: ReturnType<typeof createRef<HTMLTextAreaElement>>,
    message: string,
  ) {
    return (
      <ChatComposer
        composerRef={composerRef}
        disabled={false}
        isUploadingDocuments={false}
        message={message}
        model="gemma4:e4b"
        modelOptions={[{ value: 'gemma4:e4b', label: 'Gemma4:e4b' }]}
        onChange={() => {}}
        onDocumentsSelected={() => {}}
        onModelChange={() => {}}
        onRemoveDocument={() => {}}
        onSubmit={(event) => {
          event.preventDefault()
        }}
        onWebSearchChange={() => {}}
        uploadedDocuments={[]}
        webSearch={false}
      />
    )
  }

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
        isUploadingDocuments={false}
        message="Hello"
        model="gemma4:e4b"
        modelOptions={[{ value: 'gemma4:e4b', label: 'Gemma4:e4b' }]}
        onChange={onChange}
        onDocumentsSelected={() => {}}
        onModelChange={() => {}}
        onRemoveDocument={() => {}}
        onSubmit={onSubmit}
        onWebSearchChange={() => {}}
        uploadedDocuments={[]}
        webSearch={false}
      />,
    )

    const textarea = screen.getByLabelText('Chat message')

    fireEvent.keyDown(textarea, { key: 'Enter' })
    expect(onSubmit).toHaveBeenCalledTimes(1)

    fireEvent.keyDown(textarea, { key: 'Enter', shiftKey: true })
    expect(onSubmit).toHaveBeenCalledTimes(1)
    expect(screen.getByText(`DAGS v${import.meta.env.VITE_APP_VERSION}`)).not.toBeNull()

    requestSubmitSpy.mockRestore()
  })

  it('expands the prompt input until the maximum height then scrolls', () => {
    const composerRef = createRef<HTMLTextAreaElement>()
    const { rerender } = render(createChatComposerElement(composerRef, 'Line one'))
    const textarea = screen.getByLabelText('Chat message') as HTMLTextAreaElement

    Object.defineProperty(textarea, 'scrollHeight', {
      configurable: true,
      value: 48,
    })

    rerender(createChatComposerElement(composerRef, 'Line one\nLine two'))

    expect(textarea.style.height).toBe('48px')
    expect(textarea.style.overflowY).toBe('hidden')

    Object.defineProperty(textarea, 'scrollHeight', {
      configurable: true,
      value: 180,
    })

    rerender(createChatComposerElement(composerRef, 'Line one\nLine two\nLine three'))

    expect(textarea.style.height).toBe('128px')
    expect(textarea.style.overflowY).toBe('auto')
  })

  it('toggles web search manually', () => {
    const onWebSearchChange = vi.fn()

    render(
      <ChatComposer
        composerRef={createRef()}
        disabled={false}
        isUploadingDocuments={false}
        message="Hello"
        model="gemma4:e4b"
        modelOptions={[{ value: 'gemma4:e4b', label: 'Gemma4:e4b' }]}
        onChange={() => {}}
        onDocumentsSelected={() => {}}
        onModelChange={() => {}}
        onRemoveDocument={() => {}}
        onSubmit={(event) => {
          event.preventDefault()
        }}
        onWebSearchChange={onWebSearchChange}
        uploadedDocuments={[]}
        webSearch={false}
      />,
    )

    fireEvent.click(screen.getByLabelText('Web search'))

    expect(onWebSearchChange).toHaveBeenCalledWith(true)
  })
})
