// src/types/diary.ts

export interface Diary {
  id: number
  activityId: number
  title: string
  coverImage: string
  photos: string[]
  entries: DiaryEntry[]
  participants: DiaryParticipant[]
  location: string
  endTime: string
  createdAt: string
}

export interface DiaryEntry {
  id: number
  authorId: number
  authorNickname: string
  authorAvatar: string
  content: string
  createdAt: string
}

export interface DiaryParticipant {
  userId: number
  nickname: string
  avatar: string
  rating?: 'good' | 'neutral' | 'bad'
}

export interface DiaryCreateForm {
  activityId: number
  title: string
  photos: File[]
  content: string
}
