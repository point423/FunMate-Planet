// src/api/dazi.ts
import request from './index'

export const getRecommendations = (params?: {
  radius?: number
  tags?: string[]
  pageNum?: number
  pageSize?: number
}) => request.get('/dazi/recommend', { params })

export const getLeaderboard = (type: 'score' | 'active' = 'score') =>
  request.get('/dazi/leaderboard', { params: { type } })
