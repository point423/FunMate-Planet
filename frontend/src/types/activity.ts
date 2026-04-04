// src/types/activity.ts

export interface Activity {
  id: number
  name: string
  location: string
  participants: ActivityParticipant[]
  startTime: string
  endTime?: string
  status: 'active' | 'completed'
  journalId?: number
}

export interface ActivityParticipant {
  userId: number
  nickname: string
  avatar: string
}

export interface ActivityForm {
  name: string
  location: string
  startTime: string
  participantIds: number[]
}
