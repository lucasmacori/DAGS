import { useState, type FormEvent } from 'react'
import { createFileRoute } from '@tanstack/react-router'

type LanguageOption = {
  value: string
  label: string
}

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

const languageOptions: Array<LanguageOption> = [
  { value: 'auto', label: 'Detect' },
  { value: 'en-GB', label: 'English (UK)' },
  { value: 'en-US', label: 'English (US)' },
  { value: 'fr-FR', label: 'French' },
  { value: 'es-ES', label: 'Spanish (Spain)' },
  { value: 'es-MX', label: 'Spanish (Mexico)' },
  { value: 'pt-BR', label: 'Portuguese (Brazil)' },
  { value: 'pt-PT', label: 'Portuguese (Portugal)' },
  { value: 'de-DE', label: 'German' },
  { value: 'it-IT', label: 'Italian' },
  { value: 'nl-NL', label: 'Dutch' },
  { value: 'ja-JP', label: 'Japanese' },
  { value: 'ko-KR', label: 'Korean' },
  { value: 'zh-CN', label: 'Chinese (Simplified)' },
  { value: 'zh-TW', label: 'Chinese (Traditional)' },
]

export const Route = createFileRoute('/translate')({
  component: TranslatePage,
})

function TranslatePage() {
  const [sourceLanguage, setSourceLanguage] = useState('auto')
  const [targetLanguage, setTargetLanguage] = useState('fr-FR')
  const [sourceText, setSourceText] = useState('')
  const [translatedText, setTranslatedText] = useState('')
  const [isTranslating, setIsTranslating] = useState(false)
  const [error, setError] = useState<string | null>(null)

  function handleSwapLanguages() {
    if (sourceLanguage === 'auto') {
      return
    }

    setSourceLanguage(targetLanguage)
    setTargetLanguage(sourceLanguage)
    setSourceText(translatedText || sourceText)
    setTranslatedText(sourceText)
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (isTranslating) {
      return
    }

    if (!sourceText.trim()) {
      setError('Enter text to translate.')
      return
    }

    setError(null)
    setIsTranslating(true)

    try {
      const response = await fetch('/api/translate', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          base_language: sourceLanguage === 'auto' ? null : sourceLanguage,
          target_language: targetLanguage,
          text: sourceText,
        }),
      })

      if (!response.ok) {
        const message = (await response.text()).trim()
        throw new Error(message || 'Translation failed.')
      }

      if (!response.body) {
        setTranslatedText('')
        return
      }

      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      let hasReceivedChunk = false
      let nextOutput = ''
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

        if (!hasReceivedChunk) {
          hasReceivedChunk = true
          nextOutput = extractedText
        } else {
          nextOutput += extractedText
        }

        setTranslatedText(nextOutput)
      }

      chunkBuffer += decoder.decode()

      const { extractedText: finalText } = extractStreamText(chunkBuffer)

      if (finalText) {
        const finalOutput = hasReceivedChunk ? `${nextOutput}${finalText}` : finalText
        setTranslatedText(finalOutput)
      }
    } catch (caughtError) {
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : 'Translation failed.',
      )
    } finally {
      setIsTranslating(false)
    }
  }

  return (
    <section className="translate-screen">
      <header className="topbar">
        <div className="topbar__title-group">
          <h1 className="topbar__title topbar__title--simple">Translate</h1>
        </div>
      </header>

      <form className="translate-canvas" onSubmit={handleSubmit}>
        <div className="translate-controls-row">
          <label className="translate-language-chip">
            <div className="translate-language-chip__main">
              <span className="material-symbols-outlined">language</span>
              <select
                className="translate-language-chip__select"
                value={sourceLanguage}
                onChange={(event) => {
                  setSourceLanguage(event.target.value)
                }}
              >
                {languageOptions.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            </div>
            <span className="translate-language-chip__label">Source</span>
          </label>

          <button
            className="translate-swap-button"
            type="button"
            aria-label="Swap languages"
            onClick={handleSwapLanguages}
            disabled={sourceLanguage === 'auto'}
          >
            <span className="material-symbols-outlined">swap_horiz</span>
          </button>

          <label className="translate-language-chip translate-language-chip--target">
            <div className="translate-language-chip__main">
              <span className="material-symbols-outlined">translate</span>
              <select
                className="translate-language-chip__select"
                value={targetLanguage}
                onChange={(event) => {
                  setTargetLanguage(event.target.value)
                }}
              >
                {languageOptions.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            </div>
            <span className="translate-language-chip__label">Target</span>
          </label>
        </div>

        <div className="translate-panels">
          <section className="translate-editor translate-editor--source">
            <textarea
              className="translate-editor__input"
              name="sourceText"
              aria-label="Text to translate"
              placeholder="Type or paste content to translate..."
              value={sourceText}
              onChange={(event) => {
                setSourceText(event.target.value)
              }}
            />

            <footer className="translate-editor__footer">
              <div className="translate-editor__tools">
                <button className="icon-button" type="button" aria-label="Use microphone">
                  <span className="material-symbols-outlined">mic</span>
                </button>
                <button className="icon-button" type="button" aria-label="Read source text">
                  <span className="material-symbols-outlined">volume_up</span>
                </button>
              </div>

              <span className="translate-editor__counter">{sourceText.length} / 5000</span>
            </footer>
          </section>

          <section className="translate-editor translate-editor--output">
            <div className="translate-editor__output">
              {translatedText}
            </div>

            <footer className="translate-editor__footer translate-editor__footer--output">
              <div className="translate-editor__tools">
                <button className="icon-button" type="button" aria-label="Read translated text">
                  <span className="material-symbols-outlined">volume_up</span>
                </button>
                <button className="icon-button" type="button" aria-label="Favorite translation">
                  <span className="material-symbols-outlined">star</span>
                </button>
              </div>

              <div className="translate-output-actions">
                <button className="translate-copy-button" type="button">
                  <span className="material-symbols-outlined">content_copy</span>
                  Copy
                </button>
                <button className="translate-submit-button" type="submit" disabled={isTranslating}>
                  {isTranslating ? 'Translating...' : 'Translate Now'}
                </button>
              </div>
            </footer>
          </section>
        </div>

        {error ? <p className="translate-error-banner">{error}</p> : null}
      </form>
    </section>
  )
}
