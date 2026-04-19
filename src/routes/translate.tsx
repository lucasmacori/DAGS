import { useMemo, useState, type FormEvent } from 'react'
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
    <section className="translate-page">
      <form className="translate-form" onSubmit={handleSubmit}>
        <div className="translate-form__languages">
          <SearchableSelect
            label="Base language"
            name="sourceLanguage"
            options={languageOptions}
            value={sourceLanguage}
            onChange={setSourceLanguage}
          />

          <SearchableSelect
            label="Target language"
            name="targetLanguage"
            options={languageOptions}
            value={targetLanguage}
            onChange={setTargetLanguage}
          />
        </div>

        <textarea
          className="translate-form__textarea"
          name="sourceText"
          aria-label="Text to translate"
          placeholder="Enter text to translate"
          value={sourceText}
          onChange={(event) => {
            setSourceText(event.target.value)
          }}
        />

        <div className="translate-form__actions">
          <button
            className="translate-form__button"
            type="submit"
            disabled={isTranslating}
          >
            {isTranslating ? 'Translating...' : 'Translate'}
          </button>

          {error ? <p className="translate-form__error">{error}</p> : null}
        </div>

        <textarea
          className="translate-form__textarea translate-form__textarea--output"
          name="translatedText"
          aria-label="Translated text"
          placeholder="Translation will appear here"
          value={translatedText}
          readOnly
        />
      </form>
    </section>
  )
}

function SearchableSelect({
  label,
  name,
  options,
  value,
  onChange,
}: {
  label: string
  name: string
  options: Array<LanguageOption>
  value: string
  onChange: (value: string) => void
}) {
  const [query, setQuery] = useState('')

  const filteredOptions = useMemo(() => {
    const normalizedQuery = query.trim().toLowerCase()

    if (!normalizedQuery) {
      return options
    }

    return options.filter((option) => {
      return (
        option.label.toLowerCase().includes(normalizedQuery) ||
        option.value.toLowerCase().includes(normalizedQuery)
      )
    })
  }, [options, query])

  return (
    <label className="language-select">
      <span className="language-select__label">{label}</span>
      <input type="hidden" name={name} value={value} />

      <div className="language-select__box">
        <input
          className="language-select__search"
          type="search"
          value={query}
          onChange={(event) => {
            setQuery(event.target.value)
          }}
          placeholder="Search language"
          aria-label={`${label} search`}
        />

        <select
          className="language-select__control"
          value={value}
          onChange={(event) => {
            onChange(event.target.value)
          }}
          aria-label={label}
        >
          {filteredOptions.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </div>
    </label>
  )
}
