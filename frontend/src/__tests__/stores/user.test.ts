import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

const loginMock = vi.fn()
const registerMock = vi.fn()
const getUserInfoMock = vi.fn()

vi.mock('@/api/auth', () => ({
  login: loginMock,
  register: registerMock,
  getUserInfo: getUserInfoMock,
}))

describe('useUserStore', () => {
  beforeEach(() => {
    vi.resetModules()
    vi.doUnmock('@/stores/user')
    setActivePinia(createPinia())
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

  it('hydrates login state from localStorage', async () => {
    localStorage.setItem('token', 'persisted-token')

    const { useUserStore } = await import('@/stores/user')
    const store = useUserStore()

    expect(store.token).toBe('persisted-token')
    expect(store.isLoggedIn).toBe(true)
  })

  it('logs in returning users, persists the token and loads normalized tags', async () => {
    loginMock.mockResolvedValue({ token: 'fresh-token', isNewUser: false })
    getUserInfoMock.mockResolvedValue({
      id: 1,
      username: 'alice',
      nickname: 'Alice',
      avatar: '/avatar.png',
      bio: 'reader',
      tags: 'reading, hiking',
      activities: 3,
      score: 98,
      ranking: 2,
      createdAt: '2026-06-16T00:00:00Z',
    })

    const { useUserStore } = await import('@/stores/user')
    const store = useUserStore()

    const result = await store.loginAction({ username: 'alice', password: 'secret' })

    expect(result).toEqual({ token: 'fresh-token', needsTagSetup: false })
    expect(store.token).toBe('fresh-token')
    expect(localStorage.getItem('token')).toBe('fresh-token')
    expect(getUserInfoMock).toHaveBeenCalledTimes(1)
    expect(store.userInfo?.tags).toEqual(['reading', 'hiking'])
  })

  it('routes new users to tag setup without fetching profile data', async () => {
    loginMock.mockResolvedValue({ token: 'new-user-token', isNewUser: true })

    const { useUserStore } = await import('@/stores/user')
    const store = useUserStore()

    const result = await store.loginAction({ username: 'new-user', password: 'secret' })

    expect(result).toEqual({ token: 'new-user-token', needsTagSetup: true })
    expect(store.token).toBe('new-user-token')
    expect(store.userInfo).toBeNull()
    expect(getUserInfoMock).not.toHaveBeenCalled()
  })

  it('registers users and normalizes JSON or invalid tag payloads on fetch', async () => {
    registerMock.mockResolvedValue(undefined)
    getUserInfoMock
      .mockResolvedValueOnce({
        id: 2,
        username: 'bob',
        nickname: 'Bob',
        avatar: '/avatar2.png',
        bio: 'runner',
        tags: '["run"," swim "]',
        activities: 5,
        score: 88,
        ranking: 6,
        createdAt: '2026-06-16T00:00:00Z',
      })
      .mockResolvedValueOnce({
        id: 3,
        username: 'carol',
        nickname: 'Carol',
        avatar: '/avatar3.png',
        bio: 'traveler',
        tags: '[not-json]',
        activities: 8,
        score: 76,
        ranking: 12,
        createdAt: '2026-06-16T00:00:00Z',
      })

    const { useUserStore } = await import('@/stores/user')
    const store = useUserStore()

    await store.registerAction({
      username: 'bob',
      password: 'secret',
      confirmPassword: 'secret',
      nickname: 'Bob',
    })
    expect(registerMock).toHaveBeenCalledWith({
      username: 'bob',
      password: 'secret',
      confirmPassword: 'secret',
      nickname: 'Bob',
    })

    const first = await store.fetchUserInfo()
    const second = await store.fetchUserInfo()

    expect(first.tags).toEqual(['run', 'swim'])
    expect(second.tags).toEqual(['[not-json]'])
  })

  it('logs out by clearing user state and removing the token', async () => {
    loginMock.mockResolvedValue({ token: 'logout-token', isNewUser: true })

    const { useUserStore } = await import('@/stores/user')
    const store = useUserStore()
    await store.loginAction({ username: 'jane', password: 'secret' })

    store.logout()

    expect(store.token).toBe('')
    expect(store.userInfo).toBeNull()
    expect(store.isLoggedIn).toBe(false)
    expect(localStorage.getItem('token')).toBeNull()
  })
})
