import request from './index'
import type { ActivityForm, Activity } from '@/types/activity'

/**
 * 创建活动
 */
export const createActivity = (data: ActivityForm) =>
  request.post('/activities', data)

/**
 * 修改活动
 */
export const updateActivity = (id: number, data: ActivityForm | any) =>
  request.put(`/activities/${id}`, data)

/**
 * 获取活动详情 (包含参与者信息)
 */
export const getActivityDetail = (id: number) =>
  request.get<any>(`/activities/${id}`)

/**
 * 分页获取活动列表
 */
export const getActivities = (params: { pageNum?: number, pageSize?: number, status?: number }) =>
  request.get<any>('/activities', { params })

/**
 * 结束活动
 */
export const endActivity = (id: number) =>
  request.post(`/activities/${id}/end`, {})

/**
 * 加入活动
 */
export const joinActivity = (id: number) =>
  request.post(`/activities/${id}/join`, {})

/**
 * 提交对他人的评价
 */
export const reviewParticipant = (activityId: number, data: { revieweeId: number, rating: number, comment: string }) =>
  request.post(`/activities/${activityId}/review`, data)

/**
 * 获取活动的 AI 总结
 */
export const getActivityAiSummary = (activityId: number) =>
  request.get<string>(`/activities/${activityId}/ai-summary`)

/**
 * 获取社交排行榜
 */
export const getLeaderboard = () =>
  request.get<any[]>('/activities/leaderboard')

/**
 * 创建日记/活动回顾
 */
export const createDiary = (data: FormData) =>
  request.post('/diaries', data)


/**
 * 获取我的日记列表
 */
export const getMyDiaries = (params?: { pageNum?: number, pageSize?: number }) =>
  request.get('/diaries', { params })

/**
 * 获取日记详情
 */
export const getDiaryDetail = (id: number) =>
  request.get(`/diaries/${id}`)

/**
 * 删除日记
 */
export const deleteDiary = (id: number) =>
  request.delete(`/diaries/${id}`)


/**
 * 上传图片
 */
export const uploadImage = (file: File) => {
  const fd = new FormData()
  fd.append('file', file)
  return request.post<{ url: string }>('/upload/image', fd)
}

