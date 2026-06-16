import { beforeEach, describe, expect, it, vi } from 'vitest'

const useUserStoreMock = vi.fn()
const useRouterMock = vi.fn()

vi.mock('@/stores/user', () => ({
  useUserStore: useUserStoreMock,
}))

vi.mock('vue-router', () => ({
  useRouter: useRouterMock,
}))

describe('useAuth', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('routes new users to tag setup after login', async () => {
    const push = vi.fn()
    useRouterMock.mockReturnValue({
      push,
      currentRoute: { value: { query: {} } },
    })
    useUserStoreMock.mockReturnValue({
      loginAction: vi.fn().mockResolvedValue({ token: 'abc', needsTagSetup: true }),
      registerAction: vi.fn(),
      logout: vi.fn(),
    })

    const { useAuth } = await import('@/composables/useAuth')
    const auth = useAuth()
    const result = await auth.login({ username: 'new', password: 'secret' })

    expect(result).toEqual({ token: 'abc', needsTagSetup: true })
    expect(push).toHaveBeenCalledWith({ name: 'TagSetup' })
  })

  it('redirects returning users to the requested path or home', async () => {
    const push = vi.fn()
    useRouterMock.mockReturnValue({
      push,
      currentRoute: { value: { query: { redirect: '/activity/journal' } } },
    })
    useUserStoreMock.mockReturnValue({
      loginAction: vi.fn().mockResolvedValue({ token: 'xyz', needsTagSetup: false }),
      registerAction: vi.fn(),
      logout: vi.fn(),
    })

    const { useAuth } = await import('@/composables/useAuth')
    const auth = useAuth()
    await auth.login({ username: 'old', password: 'secret' })
    expect(push).toHaveBeenCalledWith('/activity/journal')

    push.mockClear()
    useRouterMock.mockReturnValue({
      push,
      currentRoute: { value: { query: {} } },
    })

    const authNoRedirect = useAuth()
    await authNoRedirect.login({ username: 'old', password: 'secret' })
    expect(push).toHaveBeenCalledWith('/')
  })

  it('navigates after register and logout', async () => {
    const push = vi.fn()
    const registerAction = vi.fn().mockResolvedValue(undefined)
    const logout = vi.fn()

    useRouterMock.mockReturnValue({
      push,
      currentRoute: { value: { query: {} } },
    })
    useUserStoreMock.mockReturnValue({
      loginAction: vi.fn(),
      registerAction,
      logout,
    })

    const { useAuth } = await import('@/composables/useAuth')
    const auth = useAuth()

    await auth.register({
      username: 'jane',
      password: 'secret',
      confirmPassword: 'secret',
      nickname: 'Jane',
    })
    auth.logout()

    expect(registerAction).toHaveBeenCalledWith({
      username: 'jane',
      password: 'secret',
      confirmPassword: 'secret',
      nickname: 'Jane',
    })
    expect(logout).toHaveBeenCalledTimes(1)
    expect(push).toHaveBeenNthCalledWith(1, { name: 'Login' })
    expect(push).toHaveBeenNthCalledWith(2, { name: 'Login' })
  })
})
