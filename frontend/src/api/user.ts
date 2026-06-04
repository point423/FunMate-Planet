// // src/api/user.ts
// import request from './index'
// import type { UserInfo } from '@/types/user'

// export const getUserById = (id: number) =>
//   request.get<UserInfo>(`/users/${id}`)

// export const updateProfile = (data: Partial<UserInfo>) =>
//   request.put('/users/me', data)

// export const updateTags = (tags: string[]) =>
//   request.put('/users/me/tags', { tags })

// export const sendFriendRequest = (toUserId: number) =>
//   request.post('/friends/apply', { toUserId })

// export const acceptFriendRequest = (applicationId: number) =>
//   request.post(`/friends/accept/${applicationId}`)

// export const declineFriendRequest = (applicationId: number) =>
//   request.post(`/friends/decline/${applicationId}`)

// export const getFriends = () =>
//   request.get('/friends')

// export const getFriendApplications = () =>
//   request.get('/friends/applications')

// export const ratePartner = (userId: number, rating: 'good' | 'neutral' | 'bad') =>
//   request.post('/users/rate', { userId, rating })

// src/api/user.ts
import request from './index'
import type { UserInfo } from '@/types/user'

export const getUserById = (id: number) =>
  request.get<UserInfo>(`/users/${id}`)

export const getUserByUsername = (username: string) =>
  request.get<UserInfo>('/users/by-username', { params: { username } })

export const updateProfile = (data: Partial<UserInfo>) => {
  const payload = {
    ...data,
    tags: Array.isArray(data.tags) ? data.tags.join(',') : data.tags,
  }

  return request.put('/users/me', payload)
}

// ✅ 改为：发送 tags 作为逗号分隔字符串
export const updateTags = (tags: string[]) =>
  request.put('/users/me', { tags: tags.join(',') })

// ✅ 改路径：/friends/requests 而非 /friends/apply
export const sendFriendRequest = (targetUserId: number) =>
  request.post('/friends/requests', { targetUserId })

// ✅ 统一处理好友请求（接受/拒绝）
export const handleFriendRequest = (requestId: number, accept: boolean) =>
  request.post(`/friends/requests/${requestId}/handle`, { accept })

// 向后兼容
export const acceptFriendRequest = (requestId: number) =>
  handleFriendRequest(requestId, true)

export const declineFriendRequest = (requestId: number) =>
  handleFriendRequest(requestId, false)

// ✅ 改路径：/friends/requests 而非 /friends/applications
export const getFriendApplications = () =>
  request.get('/friends/requests')

export const getFriends = () =>
  request.get('/friends')

// ✅ 改为：调用 /evaluations 而非 /users/rate，使用 scoreLevel (1-3) 而非 rating
export const ratePartner = (userId: number, score: 1 | 2 | 3) =>
  request.post('/evaluations', { targetUserId: userId, scoreLevel: score })