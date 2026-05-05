import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import * as authApi from '@/api/auth'
import request from '@/api/index'

vi.mock('@/api/index', () => ({
  default: {
    post: vi.fn(),
    get: vi.fn(),
  },
}))

describe('API - auth.ts', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.clearAllMocks()
  })

  describe('login', () => {
    it('成功登录应返回 token 和 isNewUser 标志', async () => {
      const mockResponse = {
        token: 'mock-token-abc123',
        isNewUser: false,
      }
      
      vi.mocked(request.post).mockResolvedValue(mockResponse)
      
      const result = await authApi.login({
        username: 'testuser',
        password: 'password123',
      })
      
      expect(result).toEqual(mockResponse)
      expect(request.post).toHaveBeenCalledWith(
        '/auth/login',
        { username: 'testuser', password: 'password123' }
      )
    })

    it('新用户登录应返回 isNewUser=true', async () => {
      const mockResponse = {
        token: 'mock-token-xyz789',
        isNewUser: true,
      }
      
      vi.mocked(request.post).mockResolvedValue(mockResponse)
      
      const result = await authApi.login({
        username: 'newuser',
        password: 'password456',
      })
      
      expect(result.isNewUser).toBe(true)
      expect(result.token).toBeDefined()
    })

    it('登录失败应抛出错误', async () => {
      const error = new Error('Invalid credentials')
      vi.mocked(request.post).mockRejectedValue(error)
      
      await expect(
        authApi.login({
          username: 'wronguser',
          password: 'wrongpass',
        })
      ).rejects.toThrow('Invalid credentials')
    })
  })

  describe('register', () => {
    it('成功注册应调用 register 端点', async () => {
      const mockResponse = { id: 1, username: 'newuser' }
      vi.mocked(request.post).mockResolvedValue(mockResponse)
      
      await authApi.register({
        username: 'newuser',
        password: 'password123',
        nickname: 'New User',
        confirmPassword: 'password123',
      })
      
      expect(request.post).toHaveBeenCalledWith(
        '/auth/register',
        expect.objectContaining({
          username: 'newuser',
          password: 'password123',
        })
      )
    })

    it('注册失败应抛出错误', async () => {
      const error = new Error('Username already exists')
      vi.mocked(request.post).mockRejectedValue(error)
      
      await expect(
        authApi.register({
          username: 'existinguser',
          password: 'password123',
          nickname: 'User',
          confirmPassword: 'password123',
        })
      ).rejects.toThrow('Username already exists')
    })
  })

  describe('getUserInfo', () => {
    it('成功获取当前用户信息', async () => {
      const mockUserInfo = {
        id: 1,
        username: 'testuser',
        nickname: 'Test User',
        tags: ['read', 'climb'],
      }
      
      vi.mocked(request.get).mockResolvedValue(mockUserInfo)
      
      const result = await authApi.getUserInfo()
      
      expect(result).toEqual(mockUserInfo)
      expect(request.get).toHaveBeenCalledWith('/users/me')
    })

    it('未登录时获取用户信息应返回 401', async () => {
      const error = new Error('Unauthorized')
      vi.mocked(request.get).mockRejectedValue(error)
      
      await expect(authApi.getUserInfo()).rejects.toThrow('Unauthorized')
    })
  })
})
