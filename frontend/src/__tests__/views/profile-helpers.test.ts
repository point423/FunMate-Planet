import { describe, expect, it } from 'vitest'
import {
  buildProfileUser,
  formatActivityDate,
  getDisplayTags,
  getProfileActivities,
  getProfileDiaries,
  MAX_AVATAR_BYTES,
  resolveUploadUrl,
  syncEditForm,
} from '@/views/profile/profile-helpers'

describe('profile helpers', () => {
  it('builds a profile-safe user snapshot with defaults', () => {
    expect(buildProfileUser(undefined)).toEqual({
      nickname: '',
      avatar: '',
      bio: '',
      tags: [],
      activities: 0,
      score: 0,
      reviewCount: 0,
      ranking: '--',
      publicJournals: [],
      recentActivities: [],
    })
  })

  it('limits visible diaries and activities', () => {
    const user = buildProfileUser({
      nickname: 'Amy',
      username: 'amy',
      avatar: '',
      bio: '',
      tags: [],
      activities: 0,
      score: 0,
      ranking: 1,
      createdAt: '',
      publicJournals: Array.from({ length: 8 }, (_, index) => ({ id: index, coverImage: '', title: '' })),
      recentActivities: Array.from({ length: 7 }, (_, index) => ({ id: index, icon: '', name: '', date: '', participants: [] })),
    } as any)

    expect(getProfileDiaries(user)).toHaveLength(6)
    expect(getProfileActivities(user)).toHaveLength(5)
  })

  it('maps display tags and syncs edit form data', () => {
    expect(getDisplayTags(['read'])[0]).toMatchObject({ value: 'read', label: 'read' })

    const form = { nickname: '', bio: '', tags: [] as string[] }
    syncEditForm(form, buildProfileUser({
      nickname: 'Amy',
      username: 'amy',
      avatar: '',
      bio: 'reader',
      tags: ['read'],
      activities: 0,
      score: 0,
      ranking: 1,
      createdAt: '',
    } as any))

    expect(form).toEqual({ nickname: 'Amy', bio: 'reader', tags: ['read'] })
  })

  it('resolves upload urls and formats activity dates', () => {
    expect(resolveUploadUrl('plain-url')).toBe('plain-url')
    expect(resolveUploadUrl({ url: 'top-level.png' })).toBe('top-level.png')
    expect(resolveUploadUrl({ data: { url: 'nested.png' } })).toBe('nested.png')
    expect(resolveUploadUrl({})).toBe('')

    expect(formatActivityDate('')).toBe('')
    expect(formatActivityDate('invalid')).toBe('invalid')
    expect(formatActivityDate('2026-06-16T00:00:00Z')).toBeTruthy()
    expect(MAX_AVATAR_BYTES).toBe(5 * 1024 * 1024)
  })
})
