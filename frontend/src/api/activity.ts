import request from './index'
import type {
  Activity,
  ActivityDetailResponse,
  ActivityForm,
  ActivityInvitation,
  ActivityInvitationListResponse,
  GroupedActivitiesResponse,
} from '@/types/activity'

export const createActivity = (data: ActivityForm) =>
    request.post<Activity>('/activities', data) as unknown as Promise<Activity>

export const updateActivity = (id: number, data: ActivityForm | Partial<Activity>) =>
    request.put<Activity>(`/activities/${id}`, data) as unknown as Promise<Activity>

export const getActivityDetail = (id: number) =>
    request.get<ActivityDetailResponse>(`/activities/${id}`) as unknown as Promise<ActivityDetailResponse>

export const getActivities = (params: { pageNum?: number; pageSize?: number; status?: number }) =>
    request.get<any>('/activities', { params }) as unknown as Promise<any>

export const getMyActivities = () =>
    request.get<GroupedActivitiesResponse>('/activities/my') as unknown as Promise<GroupedActivitiesResponse>

export const getCompletableActivities = () =>
    request.get<Activity[]>('/activities/completable') as unknown as Promise<Activity[]>

export const completeActivity = (id: number) =>
    request.post(`/activities/${id}/complete`, {}) as unknown as Promise<void>

export const endActivity = (id: number) =>
    completeActivity(id)

export const joinActivity = (id: number) =>
    request.post(`/activities/${id}/join`, {}) as unknown as Promise<void>

export const inviteActivityFriend = (activityId: number, inviteeId: number) =>
    request.post<Activity>(`/activities/${activityId}/invite`, { inviteeId }) as unknown as Promise<Activity>

export const getActivityInvitations = () =>
    request.get<ActivityInvitationListResponse>('/activity-invitations') as unknown as Promise<ActivityInvitationListResponse>

export const createActivityInvitation = (activityId: number, receiverId: number) =>
    request.post<ActivityInvitation>('/activity-invitations', { activityId, receiverId }) as unknown as Promise<ActivityInvitation>

export const handleActivityInvitation = (invitationId: number, accept: boolean) =>
    request.post(`/activity-invitations/${invitationId}/handle`, { accept }) as unknown as Promise<void>

export const reviewParticipant = (
    activityId: number,
    data: { revieweeId: number; rating: number; comment: string },
) => {
  const normalizedRating = Math.max(1, Math.min(3, Math.round(data.rating)))
  return request.post('/evaluations', {
    targetId: data.revieweeId,
    activityId,
    scoreLevel: normalizedRating,
  }) as unknown as Promise<void>
}

export interface UserEvaluationRecord {
  id: number
  evaluatorId: number
  targetId: number
  activityId: number
  scoreLevel: number
  createTime: string
}

export const getEvaluationsByEvaluator = (evaluatorId: number) =>
    request.get<UserEvaluationRecord[]>(`/evaluations/evaluator/${evaluatorId}`) as unknown as Promise<UserEvaluationRecord[]>

export const getLeaderboard = () =>
    request.get<any[]>('/activities/leaderboard') as unknown as Promise<any[]>

export const createDiary = (data: FormData) =>
    request.post('/diaries', data) as unknown as Promise<any>

export const getMyDiaries = (params?: { pageNum?: number; pageSize?: number }) =>
    request.get('/diaries', { params }) as unknown as Promise<any>

export const getDiaryDetail = (id: number) =>
    request.get(`/diaries/${id}`) as unknown as Promise<any>

export const updateMySharedDiaryEntry = (diaryId: number, data: { content?: string; images?: string[] | string }) =>
    request.put(`/diaries/${diaryId}/entries/me`, data) as unknown as Promise<any>

export const shareMySharedDiaryEntry = (diaryId: number) =>
    request.post(`/diaries/${diaryId}/share-me`, {}) as unknown as Promise<any>

export const deleteDiary = (id: number) =>
    request.delete(`/diaries/${id}`) as unknown as Promise<void>

export const deleteActivity = (id: number) =>
    request.delete(`/activities/${id}`) as unknown as Promise<void>

export const uploadImage = (file: File) => {
  const fd = new FormData()
  fd.append('file', file)
  return request.post<{ url: string }>('/upload/image', fd) as unknown as Promise<{ url: string }>
}
