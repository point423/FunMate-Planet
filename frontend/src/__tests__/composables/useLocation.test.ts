import { beforeEach, describe, expect, it, vi } from 'vitest'

const reportLocationMock = vi.fn()

vi.mock('@/api/auth', () => ({
  reportLocation: reportLocationMock,
}))

describe('useLocation', () => {
  beforeEach(() => {
    vi.resetModules()
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

  it('reports coordinates when geolocation succeeds', async () => {
    reportLocationMock.mockResolvedValue(undefined)
    Object.defineProperty(globalThis.navigator, 'geolocation', {
      value: {
        getCurrentPosition: (success: (position: GeolocationPosition) => void) => {
          success({
            coords: {
              latitude: 30.2741,
              longitude: 120.1551,
            },
          } as GeolocationPosition)
        },
      },
      configurable: true,
    })

    const { useLocation } = await import('@/composables/useLocation')
    const location = useLocation()
    await location.updateLocation()

    expect(location.lat.value).toBe(30.2741)
    expect(location.lng.value).toBe(120.1551)
    expect(localStorage.getItem('user_latitude')).toBe('30.2741')
    expect(localStorage.getItem('user_longitude')).toBe('120.1551')
    expect(reportLocationMock).toHaveBeenCalledWith({
      latitude: 30.2741,
      longitude: 120.1551,
    })
  })

  it('stores an error when the browser denies geolocation', async () => {
    Object.defineProperty(globalThis.navigator, 'geolocation', {
      value: {
        getCurrentPosition: (
          _success: (position: GeolocationPosition) => void,
          error: (reason: GeolocationPositionError) => void,
        ) => {
          error({ message: 'Permission denied' } as GeolocationPositionError)
        },
      },
      configurable: true,
    })

    const { useLocation } = await import('@/composables/useLocation')
    const location = useLocation()
    await location.updateLocation()

    expect(location.error.value).toBe('Permission denied')
    expect(reportLocationMock).not.toHaveBeenCalled()
  })

  it('handles browsers without geolocation support', async () => {
    Object.defineProperty(globalThis.navigator, 'geolocation', {
      value: undefined,
      configurable: true,
    })

    const { useLocation } = await import('@/composables/useLocation')
    const location = useLocation()
    await location.updateLocation()

    expect(location.error.value).toBe('Geolocation not supported')
    expect(reportLocationMock).not.toHaveBeenCalled()
  })
})
