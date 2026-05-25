// src/types/activity.ts

export interface Activity {
  id: number
  creatorId: number
  title: string
  description: string
  activityTime: string
  location: string
  maxParticipants: number
  status: number // 0:招募中, 1:进行中, 2:已结束
  createTime: string
  participants?: ActivityParticipant[]
}

export interface ActivityParticipant {
  userId: number
  nickname: string
  avatar: string
  status: number
}

export interface ActivityForm {
  title: string
  description: string
  activityTime: string
  location: string
  maxParticipants: number
}
