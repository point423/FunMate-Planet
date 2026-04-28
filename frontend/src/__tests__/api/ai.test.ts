import { describe, it, expect, vi, beforeEach } from 'vitest'
import type { SuggestionRequest } from '@/api/ai'
import * as aiApi from '@/api/ai'
import request from '@/api/index'

vi.mock('@/api/index', () => ({
  default: {
    post: vi.fn(),
  },
}))

describe('API - ai.ts', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getSuggestion', () => {
    it('成功获取 AI 建议', async () => {
      const query: SuggestionRequest = {
        tags: 'read,travel',
        location: '39.9,116.4',
        query: '找附近的读书爱好者',
      }
      
      const mockResponse = {
        suggestion: '推荐在朝阳公园参加周末读书会，有几位参与者也喜欢旅游。',
      }
      
      vi.mocked(request.post).mockResolvedValue(mockResponse)
      
      const result = await aiApi.getSuggestion(query)
      
      expect(result.suggestion).toBeDefined()
      expect(result.suggestion).toContain('朝阳公园')
      expect(request.post).toHaveBeenCalledWith('/ai/suggest', query)
    })

    it('获取建议时发生网络错误应返回错误', async () => {
      const error = new Error('Network error')
      vi.mocked(request.post).mockRejectedValue(error)
      
      const query: SuggestionRequest = {
        tags: 'read',
        location: '39.9,116.4',
        query: 'find partners',
      }
      
      await expect(aiApi.getSuggestion(query)).rejects.toThrow('Network error')
    })

    it('空查询应返回错误', async () => {
      const error = new Error('Query cannot be empty')
      vi.mocked(request.post).mockRejectedValue(error)
      
      const emptyQuery: SuggestionRequest = {
        tags: '',
        location: '',
        query: '',
      }
      
      await expect(aiApi.getSuggestion(emptyQuery)).rejects.toThrow('Query cannot be empty')
    })

    it('不同的查询应返回不同的建议', async () => {
      vi.mocked(request.post)
        .mockResolvedValueOnce({
          suggestion: '推荐 AI 驱动的读书俱乐部',
        })
        .mockResolvedValueOnce({
          suggestion: '推荐户外登山活动',
        })
      
      const readingQuery: SuggestionRequest = {
        tags: 'read',
        location: '39.9,116.4',
        query: '想找读书伙伴',
      }
      
      const climbingQuery: SuggestionRequest = {
        tags: 'climb',
        location: '39.9,116.4',
        query: '想找登山伙伴',
      }
      
      const reading = await aiApi.getSuggestion(readingQuery)
      const climbing = await aiApi.getSuggestion(climbingQuery)
      
      expect(reading.suggestion).toContain('读书')
      expect(climbing.suggestion).toContain('登山')
      expect(reading.suggestion).not.toBe(climbing.suggestion)
    })
  })
})
