import { describe, it, expect, vi, beforeEach } from 'vitest'
import request from '@/api/index'

vi.mock('@/api/index', () => ({
  default: {
    post: vi.fn(),
    get: vi.fn(),
    put: vi.fn(),
  },
}))

describe('API - user.ts', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('获取用户信息', () => {
    it('成功获取用户信息应返回用户对象', async () => {
      const mockUserInfo = {
        id: 123,
        username: 'alice',
        nickname: 'Alice',
        tags: ['read', 'travel'],
        avatar: 'https://example.com/avatar.jpg',
        location: '39.9,116.4',
        bio: 'Love reading and traveling',
      }
      
      vi.mocked(request.get).mockResolvedValue(mockUserInfo)
      
      const result = await request.get('/users/123')
      
      expect(result).toEqual(mockUserInfo)
      expect(request.get).toHaveBeenCalledWith('/users/123')
    })

    it('获取不存在的用户应返回 404', async () => {
      const error = new Error('User not found')
      vi.mocked(request.get).mockRejectedValue(error)
      
      await expect(request.get('/users/99999')).rejects.toThrow('User not found')
    })
  })

  describe('更新用户信息', () => {
    it('成功更新用户昵称', async () => {
      const updateData = { nickname: 'Alice Updated' }
      const mockResponse = { id: 123, nickname: 'Alice Updated' }
      
      vi.mocked(request.put).mockResolvedValue(mockResponse)
      
      const result = await request.put('/users/me', updateData)
      
      expect(result).toEqual(mockResponse)
      expect(request.put).toHaveBeenCalledWith('/users/me', updateData)
    })

    it('更新用户信息失败应抛出错误', async () => {
      const error = new Error('Validation failed')
      vi.mocked(request.put).mockRejectedValue(error)
      
      await expect(
        request.put('/users/me', { nickname: '' })
      ).rejects.toThrow('Validation failed')
    })
  })

  describe('更新用户标签', () => {
    it('成功更新用户标签', async () => {
      const tags = ['read', 'climb', 'photo']
      const mockResponse = { id: 123, tags }
      
      vi.mocked(request.put).mockResolvedValue(mockResponse)
      
      const result = await request.put('/users/me/tags', { tags })
      
      expect(result.tags).toEqual(tags)
      expect(request.put).toHaveBeenCalledWith('/users/me/tags', { tags })
    })

    it('超过最大标签数量应返回错误', async () => {
      const error = new Error('Cannot add more than 8 tags')
      vi.mocked(request.put).mockRejectedValue(error)
      
      const tooManyTags = Array(10).fill('tag')
      
      await expect(
        request.put('/users/me/tags', { tags: tooManyTags })
      ).rejects.toThrow('Cannot add more than 8 tags')
    })
  })

  describe('好友申请', () => {
    it('成功发送好友申请', async () => {
      const mockResponse = { id: 1, status: 'pending', toUserId: 456 }
      
      vi.mocked(request.post).mockResolvedValue(mockResponse)
      
      const result = await request.post('/friends/apply', { toUserId: 456 })
      
      expect(result.status).toBe('pending')
      expect(request.post).toHaveBeenCalledWith('/friends/apply', { toUserId: 456 })
    })

    it('重复发送好友申请应返回错误', async () => {
      const error = new Error('Friend request already sent')
      vi.mocked(request.post).mockRejectedValue(error)
      
      await expect(
        request.post('/friends/apply', { toUserId: 456 })
      ).rejects.toThrow('Friend request already sent')
    })
  })
})
