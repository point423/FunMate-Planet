import { beforeEach, describe, expect, it, vi } from 'vitest'

const createRouterMock = vi.fn()
const createWebHistoryMock = vi.fn()
const useUserStoreMock = vi.fn()

vi.mock('vue-router', () => ({
  createRouter: createRouterMock,
  createWebHistory: createWebHistoryMock,
}))

vi.mock('@/stores/user', () => ({
  useUserStore: useUserStoreMock,
}))

describe('router', () => {
  beforeEach(() => {
    vi.resetModules()
    vi.clearAllMocks()
    createWebHistoryMock.mockReturnValue('history')
  })

  it('registers activity child routes and redirects unknown paths', async () => {
    const beforeEachGuard = vi.fn()
    createRouterMock.mockReturnValue({ beforeEach: beforeEachGuard })
    useUserStoreMock.mockReturnValue({ isLoggedIn: false })

    const module = await import('@/router/index')
    expect(module.default).toBeDefined()

    const routerConfig = createRouterMock.mock.calls[0][0]
    const activityRoute = routerConfig.routes.find((route: { name: string }) => route.name === 'Activity')

    expect(createWebHistoryMock).toHaveBeenCalledTimes(1)
    expect(activityRoute.children.map((child: { name?: string }) => child.name)).toEqual([
      undefined,
      'AllActivities',
      'Journal',
      'FindPartner',
      'ActivityDetail',
    ])
    expect(routerConfig.routes.at(-1)).toEqual({ path: '/:pathMatch(.*)*', redirect: '/' })
    expect(beforeEachGuard).toHaveBeenCalledTimes(1)
  })

  it('redirects anonymous users visiting protected routes to login', async () => {
    let guard: (to: any, from: any, next: (value?: unknown) => void) => void = () => {}
    createRouterMock.mockImplementation(() => ({
      beforeEach: (cb: typeof guard) => {
        guard = cb
      },
    }))
    useUserStoreMock.mockReturnValue({ isLoggedIn: false })

    await import('@/router/index')

    const next = vi.fn()
    guard(
      { meta: { requiresAuth: true }, fullPath: '/profile', name: 'MyProfile' },
      {},
      next,
    )

    expect(next).toHaveBeenCalledWith({ name: 'Login', query: { redirect: '/profile' } })
  })

  it('redirects logged-in users away from auth pages and otherwise allows navigation', async () => {
    let guard: (to: any, from: any, next: (value?: unknown) => void) => void = () => {}
    createRouterMock.mockImplementation(() => ({
      beforeEach: (cb: typeof guard) => {
        guard = cb
      },
    }))
    useUserStoreMock.mockReturnValue({ isLoggedIn: true })

    await import('@/router/index')

    const next = vi.fn()
    guard(
      { meta: { requiresAuth: false }, fullPath: '/login', name: 'Login' },
      {},
      next,
    )
    expect(next).toHaveBeenCalledWith({ name: 'Home' })

    next.mockClear()
    guard(
      { meta: { requiresAuth: true }, fullPath: '/activity/all', name: 'AllActivities' },
      {},
      next,
    )
    expect(next).toHaveBeenCalledWith()
  })
})
