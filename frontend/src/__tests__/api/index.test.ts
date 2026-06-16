import { beforeEach, describe, expect, it, vi } from 'vitest'

const createMock = vi.fn()
const messageErrorMock = vi.fn()

type RequestSuccessHandler = (config: any) => any
type RequestErrorHandler = (error: any) => Promise<never>
type ResponseSuccessHandler = (response: any) => any
type ResponseErrorHandler = (error: any) => Promise<never>

let requestSuccessHandler: RequestSuccessHandler
let requestErrorHandler: RequestErrorHandler
let responseSuccessHandler: ResponseSuccessHandler
let responseErrorHandler: ResponseErrorHandler

const mockService = {
  interceptors: {
    request: {
      use: vi.fn((success: RequestSuccessHandler, error: RequestErrorHandler) => {
        requestSuccessHandler = success
        requestErrorHandler = error
      }),
    },
    response: {
      use: vi.fn((success: ResponseSuccessHandler, error: ResponseErrorHandler) => {
        responseSuccessHandler = success
        responseErrorHandler = error
      }),
    },
  },
}

vi.mock('axios', () => ({
  default: {
    create: createMock,
  },
}))

vi.mock('element-plus', () => ({
  ElMessage: {
    error: messageErrorMock,
  },
}))

describe('API - index.ts', () => {
  beforeEach(() => {
    vi.resetModules()
    vi.doUnmock('@/api/index')
    vi.clearAllMocks()
    requestSuccessHandler = undefined as unknown as RequestSuccessHandler
    requestErrorHandler = undefined as unknown as RequestErrorHandler
    responseSuccessHandler = undefined as unknown as ResponseSuccessHandler
    responseErrorHandler = undefined as unknown as ResponseErrorHandler
    createMock.mockReturnValue(mockService)

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

    Object.defineProperty(globalThis, 'window', {
      value: {
        location: { href: '/' },
      },
      configurable: true,
    })
  })

  it('creates the axios client with the expected defaults and exposes the service', async () => {
    const module = await import('@/api/index')

    expect(module.default).toBe(mockService)
    expect(createMock).toHaveBeenCalledWith({
      baseURL: '/api',
      timeout: 600000,
      headers: { 'Content-Type': 'application/json' },
    })
    expect(mockService.interceptors.request.use).toHaveBeenCalledTimes(1)
    expect(mockService.interceptors.response.use).toHaveBeenCalledTimes(1)
  })

  it('adds bearer tokens and strips form-data content type headers', async () => {
    await import('@/api/index')
    localStorage.setItem('token', 'jwt-token')

    const config = {
      headers: {
        Authorization: '',
        'Content-Type': 'application/json',
        'content-type': 'application/json',
        common: { 'Content-Type': 'application/json', 'content-type': 'application/json' },
        post: { 'Content-Type': 'application/json', 'content-type': 'application/json' },
      },
      data: new FormData(),
    }

    const result = requestSuccessHandler(config)

    expect(result.headers.Authorization).toBe('Bearer jwt-token')
    expect('Content-Type' in result.headers).toBe(false)
    expect('content-type' in result.headers).toBe(false)
    expect('Content-Type' in result.headers.common).toBe(false)
    expect('content-type' in result.headers.common).toBe(false)
    expect('Content-Type' in result.headers.post).toBe(false)
    expect('content-type' in result.headers.post).toBe(false)
  })

  it('passes through request errors', async () => {
    await import('@/api/index')

    const error = new Error('request failed')
    await expect(requestErrorHandler(error)).rejects.toThrow('request failed')
  })

  it('unwraps successful business responses', async () => {
    await import('@/api/index')

    expect(responseSuccessHandler({ data: { code: 200, data: { ok: true } } })).toEqual({ ok: true })
    expect(responseSuccessHandler({ data: { code: 201, data: { created: true } } })).toEqual({ created: true })
    expect(responseSuccessHandler({ data: { code: 0, data: { legacy: true } } })).toEqual({ legacy: true })
  })

  it('handles business 401 responses by clearing auth and redirecting to login', async () => {
    await import('@/api/index')
    localStorage.setItem('token', 'jwt-token')

    await expect(
      responseSuccessHandler({ data: { code: 401, message: 'Session expired', data: null } }),
    ).rejects.toThrow('Session expired')

    expect(localStorage.getItem('token')).toBeNull()
    expect(window.location.href).toBe('/login')
    expect(messageErrorMock).toHaveBeenCalledWith('Session expired')
  })

  it('shows fallback errors for failed business responses', async () => {
    await import('@/api/index')

    await expect(
      responseSuccessHandler({ data: { code: 500, message: '', data: null } }),
    ).rejects.toThrow()

    expect(messageErrorMock).toHaveBeenCalled()
  })

  it('handles network 401 responses and generic network failures', async () => {
    await import('@/api/index')
    localStorage.setItem('token', 'jwt-token')

    const unauthorized = { response: { status: 401 }, message: 'Unauthorized' }
    await expect(responseErrorHandler(unauthorized)).rejects.toBe(unauthorized)
    expect(localStorage.getItem('token')).toBeNull()
    expect(window.location.href).toBe('/login')
    expect(messageErrorMock).toHaveBeenCalledWith('Unauthorized')

    messageErrorMock.mockClear()
    const generic = { message: '' }
    await expect(responseErrorHandler(generic)).rejects.toBe(generic)
    expect(messageErrorMock).toHaveBeenCalledWith('网络错误')
  })
})
