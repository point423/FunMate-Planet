// // src/api/dazi.ts
// import request from './index'

// export const getRecommendations = (params?: {
//   radius?: number
//   tags?: string[]
//   pageNum?: number
//   pageSize?: number
// }) => request.get('/dazi/recommend', { params })

// export const getLeaderboard = (type: 'score' | 'active' = 'score') =>
//   request.get('/dazi/leaderboard', { params: { type } })

// src/api/dazi.ts
import request from './index'
import type { NearbyUser } from '@/types/user'

export const getNearbyUsers = (params?: {
  radius?: number
  tags?: string
  pageNum?: number
  pageSize?: number
}) => request.get('/discover/nearby', { params })

export const getRandomUser = () =>
  request.get('/discover/random')

export const getLeaderboard = () =>
  request.get('/discover/ranking')

// 向后兼容（页面中现有的调用）
export const getRecommendations = getNearbyUsers