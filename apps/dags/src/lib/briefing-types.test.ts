import { describe, expect, it } from 'vitest'
import { formatSyncedAt } from './briefing-types'

describe('briefing-types', () => {
  it('formats synced at correctly for minutes', () => {
    const date = new Date(Date.now() - 5 * 60000).toISOString()
    expect(formatSyncedAt(date)).toBe('Synced 5m ago')
  })

  it('formats synced at correctly for hours', () => {
    const date = new Date(Date.now() - 2 * 60 * 60000).toISOString()
    expect(formatSyncedAt(date)).toBe('Synced 2h ago')
  })

  it('formats synced at correctly for days', () => {
    const date = new Date(Date.now() - 3 * 24 * 60 * 60000).toISOString()
    expect(formatSyncedAt(date)).toBe('Synced 3d ago')
  })

  it('returns Unknown for invalid date', () => {
    expect(formatSyncedAt('invalid-date')).toBe('Unknown')
  })
})