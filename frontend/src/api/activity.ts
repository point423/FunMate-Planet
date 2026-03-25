// src/api/activity.ts
import request from './index'
import type { ActivityForm } from '@/types/activity'

export const createActivity = (data: ActivityForm) =>
  request.post('/activities', data)

export const getActivityDetail = (id: number) =>
  request.get(`/activities/${id}`)

export const completeActivity = (id: number) =>
  request.post(`/activities/${id}/complete`)

export const getMyDiaries = () =>
  request.get('/diaries')

export const getDiaryDetail = (id: number) =>
  request.get(`/diaries/${id}`)

export const createDiary = (data: FormData) =>
  request.post('/diaries', data, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })

export const addDiaryEntry = (diaryId: number, content: string) =>
  request.post(`/diaries/${diaryId}/entries`, { content })

export const uploadImage = (file: File) => {
  const form = new FormData()
  form.append('file', file)
  return request.post<string>('/upload/image', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}
