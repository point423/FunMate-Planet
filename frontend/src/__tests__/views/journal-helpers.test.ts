import { describe, expect, it } from 'vitest'
import {
  coverFromImages,
  detailCoverImageFromDiary,
  fallbackAvatar,
  findMySharedEntry,
  getMyUploadedCoverFromDiary,
  isDiaryOwner,
  mergeDiaryList,
  normalizeDiary,
  normalizeDiaryDetailPayload,
  parseImages,
  resolveDiaryCover,
} from '@/views/activity/journal-helpers'

describe('journal helpers', () => {
  it('parses image payloads from arrays, json strings and csv strings', () => {
    expect(parseImages(['a.png', '', 'b.png'])).toEqual(['a.png', 'b.png'])
    expect(parseImages('["a.png","b.png"]')).toEqual(['a.png', 'b.png'])
    expect(parseImages('a.png,b.png')).toEqual(['a.png', 'b.png'])
    expect(parseImages(null)).toEqual([])
  })

  it('normalizes diary payloads and detail payloads', () => {
    expect(
      normalizeDiary({
        id: 1,
        content: 'A very long diary content snippet',
        images: '["cover.png"]',
      }),
    ).toMatchObject({
      id: 1,
      title: 'A very long diary co...',
      images: ['cover.png'],
      coverImage: 'cover.png',
      participants: [],
      sharedEntries: [],
    })

    expect(
      normalizeDiaryDetailPayload({
        diary: { id: 2, title: 'Trip', images: ['trip.png'] },
        participants: [{ userId: 1 }],
        sharedEntries: [{ entry: { id: 1 } }],
      }),
    ).toMatchObject({
      id: 2,
      title: 'Trip',
      participants: [{ userId: 1 }],
      sharedEntries: [{ entry: { id: 1 } }],
    })
  })

  it('merges diary lists with cache and injected diaries', () => {
    const result = mergeDiaryList(
      [{ diary: { id: 1, title: 'Raw', images: [] }, participants: [{ userId: 1 }] }],
      { 1: { id: 1, title: 'Cached' } },
      { id: 2, title: 'Injected' },
    )

    expect(result).toEqual([{ id: 2, title: 'Injected' }, { id: 1, title: 'Cached' }])

    const replaced = mergeDiaryList(
      [{ id: 2, title: 'Old injected', images: [] }],
      {},
      { id: 2, title: 'New injected' },
    )
    expect(replaced).toEqual([{ id: 2, title: 'New injected' }])
  })

  it('resolves cover images from detail data, uploads and fallbacks', () => {
    expect(detailCoverImageFromDiary({ images: ['detail.png'] })).toBe('detail.png')
    expect(detailCoverImageFromDiary({ images: [] })).toContain('unsplash')
    expect(coverFromImages('["grid.png"]')).toBe('grid.png')
    expect(coverFromImages(undefined)).toContain('unsplash')
  })

  it('finds the current user entry and prefers draft cover images when requested', () => {
    const diary = {
      coverImage: 'existing.png',
      images: ['existing.png'],
      sharedEntries: [
        {
          user: { id: 10, nickname: 'Amy', avatar: '' },
          entry: { images: ['uploaded.png'], userId: 10 },
        },
      ],
    }
    const drafts = {
      10: { content: 'draft', images: ['draft.png'] },
    }

    expect(findMySharedEntry(diary, 10)?.user.id).toBe(10)
    expect(findMySharedEntry(diary, 0)).toBeNull()
    expect(getMyUploadedCoverFromDiary(diary, 10, drafts, true)).toBe('draft.png')
    expect(getMyUploadedCoverFromDiary(diary, 10, drafts)).toBe('uploaded.png')
    expect(resolveDiaryCover(diary, 10, drafts)).toBe('uploaded.png')
    expect(resolveDiaryCover({ coverImage: 'fallback.png', images: [] }, 99, {})).toBe('fallback.png')
  })

  it('provides deterministic avatar and owner helpers', () => {
    expect(fallbackAvatar(7)).toContain('seed=7')
    expect(isDiaryOwner('12', 12)).toBe(true)
    expect(isDiaryOwner('12', 13)).toBe(false)
  })
})
