// // src/api/activity.ts
// import request from './index'
// import type { ActivityForm } from '@/types/activity'

// export const createActivity = (data: ActivityForm) =>
//   request.post('/activities', data)

// export const getActivityDetail = (id: number) =>
//   request.get(`/activities/${id}`)

// export const completeActivity = (id: number) =>
//   request.post(`/activities/${id}/complete`)

// export const getMyDiaries = () =>
//   request.get('/diaries')

// export const getDiaryDetail = (id: number) =>
//   request.get(`/diaries/${id}`)

// export const createDiary = (data: FormData) =>
//   request.post('/diaries', data, {
//     headers: { 'Content-Type': 'multipart/form-data' },
//   })

// export const addDiaryEntry = (diaryId: number, content: string) =>
//   request.post(`/diaries/${diaryId}/entries`, { content })

// export const uploadImage = (file: File) => {
//   const form = new FormData()
//   form.append('file', file)
//   return request.post<string>('/upload/image', form, {
//     headers: { 'Content-Type': 'multipart/form-data' },
//   })
// }

// src/api/activity.ts
import request from './index'
import type { ActivityForm } from '@/types/activity'

export const createActivity = (data: ActivityForm) =>
  request.post('/activities', data)

export const getActivityDetail = (_id: number) =>
  request.get(`/activities/${_id}`)

// ✅ 改路径：/activities/{id}/end 而非 /activities/{id}/complete
export const endActivity = (id: number) =>
  request.post(`/activities/${id}/end`, {})

// 向后兼容
export const completeActivity = endActivity

// ✅ 新增：活动更新接口
export const updateActivity = (id: number, data: Partial<ActivityForm>) =>
  request.put(`/activities/${id}`, data)

// ✅ 新增：参与活动接口
export const joinActivity = (id: number) =>
  request.post(`/activities/${id}/join`, {})

export const getMyDiaries = (pageNum?: number, pageSize?: number) =>
  request.get('/diaries', { params: { pageNum, pageSize } })

export const getDiaryDetail = (id: number) =>
  request.get(`/diaries/${id}`)

export const createDiary = (data: FormData) =>
  request.post('/diaries', data, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })

// ✅ 移除：/diaries/{id}/entries 此接口后端未实现，改用创建新日记
// export const addDiaryEntry = (diaryId: number, content: string) =>
//   request.post(`/diaries/${diaryId}/entries`, { content })

export const uploadImage = (file: File) => {
  const form = new FormData()
  form.append('file', file)
  return request.post<string>('/upload/image', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}