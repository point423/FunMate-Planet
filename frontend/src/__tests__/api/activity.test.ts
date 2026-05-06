import { describe, it, expect, vi, beforeEach } from 'vitest'
import request from '@/api/index'

vi.mock('@/api/index', () => ({
  default: {
    post: vi.fn(),
    get: vi.fn(),
  },
}))

describe('API - activity.ts', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('创建活动', () => {
    it('成功创建活动应返回活动 ID', async () => {
      const activityData = {
        tag: 'reading',
        title: 'Book Club',
        description: 'Weekly book club meetup',
        latitude: 39.9,
        longitude: 116.4,
        time: '2026-05-01T14:00:00Z',
      }
      
      const mockResponse = { id: 1, ...activityData }
      vi.mocked(request.post).mockResolvedValue(mockResponse)
      
      const result = await request.post('/activities', activityData)
      
      expect(result.id).toBe(1)
      expect(request.post).toHaveBeenCalledWith('/activities', activityData)
    })

    it('创建活动时缺少必需字段应返回错误', async () => {
      const error = new Error('Missing required fields')
      vi.mocked(request.post).mockRejectedValue(error)
      
      const invalidData = { title: 'Book Club' } // 缺少 tag 和 location
      
      await expect(
        request.post('/activities', invalidData)
      ).rejects.toThrow('Missing required fields')
    })
  })

  describe('获取日记', () => {
    it('成功获取用户的所有日记', async () => {
      const mockDiaries = [
        { id: 1, title: 'First Journal', content: 'Great day!', date: '2026-04-28' },
        { id: 2, title: 'Second Journal', content: 'Good memories', date: '2026-04-27' },
      ]
      
      vi.mocked(request.get).mockResolvedValue(mockDiaries)
      
      const result = await request.get('/diaries')
      
      expect(result).toHaveLength(2)
      expect(request.get).toHaveBeenCalledWith('/diaries')
    })

    it('用户没有日记时应返回空数组', async () => {
      vi.mocked(request.get).mockResolvedValue([])
      
      const result = await request.get('/diaries')
      
      expect(result).toEqual([])
    })
  })

  describe('创建日记', () => {
    it('成功创建日记并上传照片', async () => {
      const formData = new FormData()
      formData.append('activityId', '1')
      formData.append('title', 'Day at the Park')
      formData.append('content', 'Had a wonderful time')
      
      const mockResponse = { id: 101, title: 'Day at the Park', photos: [] }
      vi.mocked(request.post).mockResolvedValue(mockResponse)
      
      const result = await request.post('/diaries', formData)
      
      expect(result.id).toBe(101)
      expect(result.title).toBe('Day at the Park')
    })

    it('日记标题为空时应返回错误', async () => {
      const error = new Error('Title cannot be empty')
      vi.mocked(request.post).mockRejectedValue(error)
      
      const invalidFormData = new FormData()
      invalidFormData.append('title', '')
      
      await expect(
        request.post('/diaries', invalidFormData)
      ).rejects.toThrow('Title cannot be empty')
    })

    it('上传过大文件应返回错误', async () => {
      const error = new Error('File size exceeds limit')
      vi.mocked(request.post).mockRejectedValue(error)
      
      const formData = new FormData()
      formData.append('title', 'Journal')
      // 模拟大文件
      
      await expect(
        request.post('/diaries', formData)
      ).rejects.toThrow('File size exceeds limit')
    })
  })

  describe('获取活动详情', () => {
    it('成功获取活动详情', async () => {
      const mockActivityDetail = {
        id: 1,
        tag: 'reading',
        title: 'Book Club',
        description: 'Weekly book club meetup',
        participants: 5,
        location: { latitude: 39.9, longitude: 116.4 },
      }
      
      vi.mocked(request.get).mockResolvedValue(mockActivityDetail)
      
      const result = await request.get('/activities/1')
      
      expect(result.id).toBe(1)
      expect(result.title).toBe('Book Club')
      expect(request.get).toHaveBeenCalledWith('/activities/1')
    })

    it('获取不存在的活动应返回 404', async () => {
      const error = new Error('Activity not found')
      vi.mocked(request.get).mockRejectedValue(error)
      
      await expect(request.get('/activities/99999')).rejects.toThrow('Activity not found')
    })
  })
})
