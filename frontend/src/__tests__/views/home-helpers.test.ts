import { describe, expect, it } from 'vitest'
import {
  buildOrbitUsers,
  DEFAULT_LOCATION,
  getCurrentPosition,
  normalizeNearbyUser,
  normalizeTags,
} from '@/views/home/home-helpers'

describe('home helpers', () => {
  it('normalizes tag payloads and nearby users', () => {
    expect(normalizeTags(['read', '', 'travel'])).toEqual(['read', 'travel'])
    expect(normalizeTags('read, travel')).toEqual(['read', 'travel'])
    expect(normalizeTags(undefined)).toEqual([])

    expect(
      normalizeNearbyUser({
        id: 1,
        tags: 'read,travel',
        averageScore: { parsedValue: '92' },
        reviewCount: '3',
        distance: '1.5',
      }),
    ).toMatchObject({
      id: 1,
      avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=1',
      tags: ['read', 'travel'],
      score: 92,
      reviewCount: 3,
      distance: 1.5,
    })
  })

  it('builds orbit users from arrays or paginated payloads and filters current user', () => {
    const users = buildOrbitUsers(
      {
        content: [
          { id: 1, tags: [] },
          { id: 2, tags: [] },
          { id: 3, tags: [] },
        ],
      },
      2,
    )

    expect(users.map((user) => user.id)).toEqual([1, 3])
  })

  it('returns the current geolocation or a fallback location', async () => {
    const position = await getCurrentPosition({
      getCurrentPosition: (success) => {
        success({ coords: { longitude: 120.1, latitude: 30.2 } } as GeolocationPosition)
      },
    })
    expect(position).toEqual({ longitude: 120.1, latitude: 30.2 })

    await expect(
      getCurrentPosition({
        getCurrentPosition: (_success, failure) => {
          failure?.(new Error('denied') as never)
        },
      }),
    ).resolves.toEqual(DEFAULT_LOCATION)

    await expect(getCurrentPosition(undefined)).resolves.toEqual(DEFAULT_LOCATION)
  })
})
