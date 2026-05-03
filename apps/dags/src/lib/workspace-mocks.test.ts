import { describe, expect, it } from 'vitest'

import {
  appearanceOptions,
  archiveItems,
  historyItems,
  translationHighlights,
} from './workspace-mocks'

describe('workspace mocks', () => {
  it('marks exactly one appearance preset as selected', () => {
    const selectedOptions = appearanceOptions.filter((option) => option.isSelected)

    expect(selectedOptions).toHaveLength(1)
    expect(selectedOptions[0]?.name).toBe('Dark Loom')
  })

  it('includes prototype-backed content for history, archive, and translate', () => {
    expect(historyItems.length).toBeGreaterThan(0)
    expect(archiveItems.length).toBeGreaterThan(0)
    expect(translationHighlights.length).toBeGreaterThan(0)
  })
})
