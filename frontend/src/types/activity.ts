export type ActivityStatus = 0 | 1 | 2 | 3

export interface Activity {
  id: number
  creatorId: number
  title: string
  description: string
  plan?: string | null
  activityTime: string
  location: string
  maxParticipants: number
  status: ActivityStatus
  createTime: string
}

export interface ActivityUser {
  id: number
  nickname: string
  avatar: string
}

export interface ActivityDetailResponse {
  activity: Activity
  participants: ActivityUser[]
  participantCount: number
  hasJournal: boolean
}

export interface GroupedActivitiesResponse {
  pending: Activity[]
  active: Activity[]
  completed: Activity[]
  cancelled: Activity[]
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
  plan?: string | null
  activityTime: string
  location: string
  maxParticipants: number
  inviteeId?: number
}

export type ActivityInvitationStatus = 'pending' | 'accepted' | 'declined' | 'cancelled' | 'expired'

export interface ActivityInvitation {
  id: number
  activityId: number
  senderId: number
  receiverId: number
  status: ActivityInvitationStatus
  createTime: string
  handleTime?: string | null
}

export interface ActivityInvitationPayload {
  invitation: ActivityInvitation
  activity: Activity | null
  sender: ActivityUser | null
  receiver: ActivityUser | null
}

export interface ActivityInvitationListResponse {
  incoming: ActivityInvitationPayload[]
  outgoing: ActivityInvitationPayload[]
}
