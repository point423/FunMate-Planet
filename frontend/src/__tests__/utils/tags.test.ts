import { describe, expect, it } from 'vitest'
import { TAG_EMOJI_MAP, TAG_META_LIST, getTagMeta } from '@/utils/tags'

describe('tags utils', () => {
  it('exposes a consistent tag metadata list and emoji map', () => {
    expect(TAG_META_LIST).toHaveLength(12)
    expect(TAG_EMOJI_MAP.read).toBe(getTagMeta('read').emoji)
    expect(TAG_EMOJI_MAP.kungfu).toBe(getTagMeta('kungfu').emoji)
  })

  it('normalizes known tag values', () => {
    const meta = getTagMeta('  CLIMB ')

    expect(meta.value).toBe('climb')
    expect(meta.label).toBe('climb')
  })

  it('returns a fallback tag meta for unknown values', () => {
    const meta = getTagMeta('unknown-tag')

    expect(meta).toEqual({
      value: 'unknown-tag',
      emoji: '🏷',
      label: 'unknown-tag',
    })
  })
})
