import request from './index'

export interface ChatMessageDto {
  id: string
  senderId: number
  receiverId: number
  content: string
  createTime: string
  isRead: boolean
}

export interface ChatMessagePage {
  total: number
  list: ChatMessageDto[]
  targetUserId: number
  pageNum: number
  pageSize: number
}

export const getChatMessages = (targetUserId: number, pageNum = 1, pageSize = 50) =>
  request.get<ChatMessagePage, ChatMessagePage>('/chat/messages', {
    params: { targetUserId, pageNum, pageSize },
  })

export const sendChatMessage = (receiverId: number, content: string) =>
  request.post<string, string>('/chat/messages', { receiverId, content })
