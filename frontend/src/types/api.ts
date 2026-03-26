// src/types/api.ts

export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
}

export interface ChatMessage {
  id: number
  senderId: number
  receiverId: number
  content: string
  createdAt: string
}
