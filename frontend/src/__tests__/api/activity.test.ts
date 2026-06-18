import { beforeEach, describe, expect, it, vi } from 'vitest'
import * as activityApi from '@/api/activity'
import request from '@/api/index'

vi.mock('@/api/index', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}))

describe('API - activity.ts', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('creates, updates and fetches activities through the request client', async () => {
    vi.mocked(request.post).mockResolvedValue({ id: 1 })
    vi.mocked(request.put).mockResolvedValue({ id: 1 })
    vi.mocked(request.get).mockResolvedValue({ id: 1 })

    await activityApi.createActivity({
      title: 'Book Club',
      description: 'Weekly meetup',
      activityTime: '2026-06-20T18:00:00Z',
      location: 'Library',
      maxParticipants: 6,
    })
    await activityApi.updateActivity(1, { title: 'Updated title' })
    await activityApi.getActivityDetail(1)
    await activityApi.getActivities({ pageNum: 2, pageSize: 10, status: 1 })

    expect(request.post).toHaveBeenCalledWith('/activities', expect.objectContaining({ title: 'Book Club' }))
    expect(request.put).toHaveBeenCalledWith('/activities/1', { title: 'Updated title' })
    expect(request.get).toHaveBeenCalledWith('/activities/1')
    expect(request.get).toHaveBeenCalledWith('/activities', {
      params: { pageNum: 2, pageSize: 10, status: 1 },
    })
  })

  it('handles activity participation and invitations', async () => {
    vi.mocked(request.post).mockResolvedValue(undefined)
    vi.mocked(request.get).mockResolvedValue({ incoming: [], outgoing: [] })

    await activityApi.getMyActivities()
    await activityApi.getCompletableActivities()
    await activityApi.completeActivity(3)
    await activityApi.endActivity(4)
    await activityApi.joinActivity(5)
    await activityApi.inviteActivityFriend(6, 8)
    await activityApi.getActivityInvitations()
    await activityApi.createActivityInvitation(6, 8)
    await activityApi.handleActivityInvitation(9, true)

    expect(request.get).toHaveBeenCalledWith('/activities/my')
    expect(request.get).toHaveBeenCalledWith('/activities/completable')
    expect(request.post).toHaveBeenCalledWith('/activities/3/complete', {})
    expect(request.post).toHaveBeenCalledWith('/activities/4/complete', {})
    expect(request.post).toHaveBeenCalledWith('/activities/5/join', {})
    expect(request.post).toHaveBeenCalledWith('/activities/6/invite', { inviteeId: 8 })
    expect(request.get).toHaveBeenCalledWith('/activity-invitations')
    expect(request.post).toHaveBeenCalledWith('/activity-invitations', { activityId: 6, receiverId: 8 })
    expect(request.post).toHaveBeenCalledWith('/activity-invitations/9/handle', { accept: true })
  })

  it('normalizes review scores before submitting participant reviews', async () => {
    vi.mocked(request.post).mockResolvedValue(undefined)

    await activityApi.reviewParticipant(2, {
      revieweeId: 11,
      rating: 2.6,
      comment: 'Great teammate',
    })
    await activityApi.reviewParticipant(2, {
      revieweeId: 12,
      rating: 0,
      comment: 'Needs improvement',
    })

    expect(request.post).toHaveBeenNthCalledWith(1, '/evaluations', {
      targetId: 11,
      activityId: 2,
      scoreLevel: 3,
    })
    expect(request.post).toHaveBeenNthCalledWith(2, '/evaluations', {
      targetId: 12,
      activityId: 2,
      scoreLevel: 1,
    })
  })

  it('loads evaluations, leaderboard and diary resources', async () => {
    vi.mocked(request.get).mockResolvedValue([])
    vi.mocked(request.put).mockResolvedValue({ ok: true })
    vi.mocked(request.post).mockResolvedValue({ ok: true })
    vi.mocked(request.delete).mockResolvedValue(undefined)

    await activityApi.getEvaluationsByEvaluator(7)
    await activityApi.getLeaderboard()
    await activityApi.createDiary(new FormData())
    await activityApi.getMyDiaries({ pageNum: 1, pageSize: 5 })
    await activityApi.getDiaryDetail(10)
    await activityApi.updateMySharedDiaryEntry(10, { content: 'Updated entry', images: ['cover.png'] })
    await activityApi.shareMySharedDiaryEntry(10)
    await activityApi.deleteDiary(10)
    await activityApi.deleteActivity(11)

    expect(request.get).toHaveBeenCalledWith('/evaluations/evaluator/7')
    expect(request.get).toHaveBeenCalledWith('/activities/leaderboard')
    expect(request.post).toHaveBeenCalledWith('/diaries', expect.any(FormData))
    expect(request.get).toHaveBeenCalledWith('/diaries', { params: { pageNum: 1, pageSize: 5 } })
    expect(request.get).toHaveBeenCalledWith('/diaries/10')
    expect(request.put).toHaveBeenCalledWith('/diaries/10/entries/me', {
      content: 'Updated entry',
      images: ['cover.png'],
    })
    expect(request.post).toHaveBeenCalledWith('/diaries/10/share-me', {})
    expect(request.delete).toHaveBeenCalledWith('/diaries/10')
    expect(request.delete).toHaveBeenCalledWith('/activities/11')
  })

  it('wraps uploads in FormData before posting images', async () => {
    vi.mocked(request.post).mockResolvedValue({ url: '/uploads/image.png' })

    const file = new File(['binary'], 'image.png', { type: 'image/png' })
    await activityApi.uploadImage(file)

    const [path, payload] = vi.mocked(request.post).mock.calls[0]
    expect(path).toBe('/upload/image')
    expect(payload).toBeInstanceOf(FormData)
    expect((payload as FormData).get('file')).toBe(file)
  })
})
