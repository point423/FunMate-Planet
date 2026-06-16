import { describe, expect, it, vi } from 'vitest'
import { formatDate, formatDistance, formatScore, timeAgo } from '@/utils/format'

describe('format utils', () => {
  it('formats dates with the default and custom pattern', () => {
    expect(formatDate('2026-06-16T00:00:00Z')).toBe('16/06/2026')
    expect(formatDate('2026-06-16T00:00:00Z', 'yyyy-MM')).toBe('2026-06')
  })

  it('formats relative time strings', () => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2026-06-16T12:00:00Z'))

    expect(timeAgo('2026-06-16T11:00:00Z')).toContain('ago')

    vi.useRealTimers()
  })

  it('formats distances in meters, kilometers and invalid cases', () => {
    expect(formatDistance(undefined)).toBe('--')
    expect(formatDistance(Number.NaN)).toBe('--')
    expect(formatDistance(0.35)).toBe('350m')
    expect(formatDistance(1.26)).toBe('1.3km')
  })

  it('formats scores with and without review counts', () => {
    expect(formatScore(92, 10)).toBe('92% positive')
    expect(formatScore(91.6, 2)).toBe('92% positive')
    expect(formatScore(50, 0)).toBe('No reviews yet')
    expect(formatScore(Number.NaN, 3)).toBe('0% positive')
  })
})
