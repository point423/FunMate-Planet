import { beforeEach, describe, expect, it, vi } from 'vitest'
import * as daziApi from '@/api/dazi'
import request from '@/api/index'

vi.mock('@/api/index', () => ({
  default: {
    get: vi.fn(),
  },
}))

describe('API - dazi.ts', () => {
  beforeEach(() => {
    vi.clearAllMocks()

    const storage = new Map<string, string>()
    Object.defineProperty(globalThis, 'localStorage', {
      value: {
        getItem: vi.fn((key: string) => storage.get(key) ?? null),
        setItem: vi.fn((key: string, value: string) => {
          storage.set(key, value)
        }),
        removeItem: vi.fn((key: string) => {
          storage.delete(key)
        }),
        clear: vi.fn(() => {
          storage.clear()
        }),
      },
      configurable: true,
    })
  })

  it('uses explicit nearby-user parameters when coordinates are provided', async () => {
    vi.mocked(request.get).mockResolvedValue([])

    await daziApi.getNearbyUsers({
      radius: 20,
      pageNum: 3,
      pageSize: 8,
      tags: 'read,travel',
      longitude: 120.1,
      latitude: 30.2,
    })

    expect(request.get).toHaveBeenCalledWith('/discover/nearby', {
      params: {
        radius: 20,
        pageSize: 8,
        pageNum: 3,
        tags: 'read,travel',
        longitude: 120.1,
        latitude: 30.2,
      },
    })
  })

  it('falls back to saved coordinates and default pagination values', async () => {
    localStorage.setItem('user_latitude', '30.3')
    localStorage.setItem('user_longitude', '120.3')
    vi.mocked(request.get).mockResolvedValue([])

    await daziApi.getNearbyUsers()

    expect(request.get).toHaveBeenCalledWith('/discover/nearby', {
      params: {
        radius: 10,
        pageSize: 6,
        latitude: 30.3,
        longitude: 120.3,
      },
    })
  })

  it('omits coordinates when neither params nor localStorage provide them', async () => {
    vi.mocked(request.get).mockResolvedValue([])

    await daziApi.getRecommendations({ tags: 'music' })

    expect(request.get).toHaveBeenCalledWith('/discover/nearby', {
      params: {
        radius: 10,
        pageSize: 6,
        tags: 'music',
      },
    })
  })

  it('loads random users and the discover leaderboard', async () => {
    vi.mocked(request.get).mockResolvedValue([])

    await daziApi.getRandomUser()
    await daziApi.getLeaderboard()

    expect(request.get).toHaveBeenNthCalledWith(1, '/discover/random')
    expect(request.get).toHaveBeenNthCalledWith(2, '/discover/ranking')
  })
})
