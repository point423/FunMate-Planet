import { describe, expect, it } from 'vitest'
import { formatActivityDate, normalizeDetailTags } from '@/views/user/user-detail-helpers'

describe('user detail helpers', () => {
  it('normalizes detail tags from arrays and strings', () => {
    expect(normalizeDetailTags(['read', 'travel']).map((tag) => tag.value)).toEqual(['read', 'travel'])
    expect(normalizeDetailTags('read, travel;music').map((tag) => tag.value)).toEqual(['read', 'travel', 'music'])
    expect(normalizeDetailTags(undefined)).toEqual([])
  })

  it('formats activity dates while keeping invalid values unchanged', () => {
    expect(formatActivityDate('')).toBe('')
    expect(formatActivityDate('invalid-date')).toBe('invalid-date')
    expect(formatActivityDate('2026-06-16T00:00:00Z')).toBeTruthy()
  })
})
