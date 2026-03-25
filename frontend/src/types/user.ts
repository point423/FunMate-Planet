// src/types/user.ts

export interface UserInfo {
  id: number
  username: string
  nickname: string
  avatar: string
  bio: string
  tags: string[]
  activities: number
  score: number
  ranking: number
  longitude?: number
  latitude?: number
  createdAt: string
}

export interface LoginForm {
  username: string
  password: string
}

export interface RegisterForm {
  username: string
  password: string
  confirmPassword: string
  nickname: string
}

export interface NearbyUser extends UserInfo {
  distance: number  // km
  publicJournals: JournalThumb[]
  recentActivities: ActivityItem[]
}

export interface JournalThumb {
  id: number
  coverImage: string
  title: string
}

export interface ActivityItem {
  id: number
  icon: string
  name: string
  date: string
  participants: string[]  // avatar URLs
}

export interface FriendApplication {
  id: number
  fromUser: NearbyUser
  toUserId: number
  status: 'pending' | 'accepted' | 'declined'
  createdAt: string
}
