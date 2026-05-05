// src/api/dazi.ts
import request from './index'

interface NearbyUsersParams {
  radius?: number
  tags?: string
  pageNum?: number
  pageSize?: number
  longitude?: number
  latitude?: number
}

export const getNearbyUsers = (params?: NearbyUsersParams) => {
  // 如果没有提供经纬度，尝试从 localStorage 获取
  const defaultParams: NearbyUsersParams = {
    radius: params?.radius || 10,
    pageSize: params?.pageSize || 6,
  }

  // 如果提供了经纬度，使用提供的值
  if (params?.longitude && params?.latitude) {
    defaultParams.longitude = params.longitude
    defaultParams.latitude = params.latitude
  } else {
    // 否则尝试从 localStorage 获取（如果之前保存过）
    const savedLat = localStorage.getItem('user_latitude')
    const savedLng = localStorage.getItem('user_longitude')
    if (savedLat && savedLng) {
      defaultParams.latitude = parseFloat(savedLat)
      defaultParams.longitude = parseFloat(savedLng)
    }
    // 如果还是没有，就不传，后端会返回空列表
  }

  if (params?.tags) defaultParams.tags = params.tags
  if (params?.pageNum) defaultParams.pageNum = params.pageNum

  return request.get('/discover/nearby', { params: defaultParams })
}

export const getRandomUser = () =>
  request.get('/discover/random')

export const getLeaderboard = () =>
  request.get('/discover/ranking')

// 向后兼容（页面中现有的调用）
export const getRecommendations = getNearbyUsers
