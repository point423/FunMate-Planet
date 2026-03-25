// src/api/user.ts
import request from './index'
import type { UserInfo } from '@/types/user'

export const getUserById = (id: number) =>
  request.get<UserInfo>(`/users/${id}`)

export const updateProfile = (data: Partial<UserInfo>) =>
  request.put('/users/me', data)

export const updateTags = (tags: string[]) =>
  request.put('/users/me/tags', { tags })

export const sendFriendRequest = (toUserId: number) =>
  request.post('/friends/apply', { toUserId })

export const acceptFriendRequest = (applicationId: number) =>
  request.post(`/friends/accept/${applicationId}`)

export const declineFriendRequest = (applicationId: number) =>
  request.post(`/friends/decline/${applicationId}`)

export const getFriends = () =>
  request.get('/friends')

export const getFriendApplications = () =>
  request.get('/friends/applications')

export const ratePartner = (userId: number, rating: 'good' | 'neutral' | 'bad') =>
  request.post('/users/rate', { userId, rating })
