import { describe, expect, it } from 'vitest'

import {
  appearanceOptions,
  archiveItems,
  historyItems,
  settingsApiKeys,
  translationHighlights,
  workspaceUser,
} from './workspace-mocks'

describe('workspace mocks', () => {
  it('exposes a named workspace user', () => {
    expect(workspaceUser.name).toBeTruthy()
    expect(workspaceUser.plan).toBeTruthy()
  })

  it('marks exactly one appearance preset as selected', () => {
    const selectedOptions = appearanceOptions.filter((option) => option.isSelected)

    expect(selectedOptions).toHaveLength(1)
    expect(selectedOptions[0]?.name).toBe('Dark Loom')
  })

  it('includes prototype-backed content for settings, history, archive, and translate', () => {
    expect(settingsApiKeys.length).toBeGreaterThan(0)
    expect(historyItems.length).toBeGreaterThan(0)
    expect(archiveItems.length).toBeGreaterThan(0)
    expect(translationHighlights.length).toBeGreaterThan(0)
  })
})
