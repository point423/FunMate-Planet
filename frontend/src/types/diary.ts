// src/types/diary.ts

export interface Diary {
  id: number
  activityId: number
  title: string
  coverImage: string
  photos: string[]
  entries: DiaryEntry[]
  participants: DiaryParticipant[]
  sharedEntries?: SharedDiaryEntryPayload[]
  location: string
  endTime: string
  createdAt: string
  createTime?: string
  images?: string[] | string
  content?: string
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

export interface SharedDiaryEntry {
  id: number
  diaryId: number
  userId: number
  content: string
  images: string[] | string
  createTime: string
  updateTime: string
}

export interface SharedDiaryEntryPayload {
  user: {
    id: number
    nickname: string
    avatar: string
  }
  entry: SharedDiaryEntry
}

export interface DiaryCreateForm {
  activityId: number
  title: string
  photos: File[]
  content: string
}
