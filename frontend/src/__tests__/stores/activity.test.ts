import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

const getMyDiariesMock = vi.fn()
const getDiaryDetailMock = vi.fn()
const deleteDiaryApiMock = vi.fn()

vi.mock('@/api/activity', () => ({
  getMyDiaries: getMyDiariesMock,
  getDiaryDetail: getDiaryDetailMock,
  deleteDiary: deleteDiaryApiMock,
}))

describe('useActivityStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('loads diaries from paginated content responses', async () => {
    getMyDiariesMock.mockResolvedValue({
      content: [
        {
          id: 1,
          activityId: 10,
          title: 'Weekend hike',
          coverImage: '',
          photos: [],
          entries: [],
          participants: [],
          location: 'Hill',
          endTime: '2026-06-16',
          createdAt: '2026-06-16',
        },
      ],
    })

    const { useActivityStore } = await import('@/stores/activity')
    const store = useActivityStore()

    await store.fetchDiaries()

    expect(getMyDiariesMock).toHaveBeenCalledWith({ pageNum: 1, pageSize: 100 })
    expect(store.diaries).toHaveLength(1)
    expect(store.diaries[0].title).toBe('Weekend hike')
  })

  it('accepts plain diary arrays and falls back to an empty list for invalid payloads', async () => {
    const { useActivityStore } = await import('@/stores/activity')
    const store = useActivityStore()

    getMyDiariesMock.mockResolvedValueOnce([
      {
        id: 2,
        activityId: 20,
        title: 'Book fair',
        coverImage: '',
        photos: [],
        entries: [],
        participants: [],
        location: 'Downtown',
        endTime: '2026-06-17',
        createdAt: '2026-06-17',
      },
    ])
    await store.fetchDiaries()
    expect(store.diaries).toHaveLength(1)

    getMyDiariesMock.mockResolvedValueOnce({ items: [] })
    await store.fetchDiaries()
    expect(store.diaries).toEqual([])
  })

  it('normalizes diary detail responses with participants and shared entries', async () => {
    getDiaryDetailMock.mockResolvedValue({
      diary: {
        id: 3,
        activityId: 30,
        title: 'Camp night',
        coverImage: '',
        photos: [],
        entries: [],
        participants: [],
        location: 'Forest',
        endTime: '2026-06-18',
        createdAt: '2026-06-18',
      },
      participants: [{ userId: 1, nickname: 'Amy', avatar: '' }],
      sharedEntries: [{ entry: { id: 1 } }],
    })

    const { useActivityStore } = await import('@/stores/activity')
    const store = useActivityStore()

    const result = await store.fetchDiaryDetail(3)

    expect(getDiaryDetailMock).toHaveBeenCalledWith(3)
    expect(result?.participants).toEqual([{ userId: 1, nickname: 'Amy', avatar: '' }])
    expect(result?.sharedEntries).toEqual([{ entry: { id: 1 } }])
    expect(store.activeDiary?.id).toBe(3)
  })

  it('keeps plain diary detail payloads intact and deletes active diaries from state', async () => {
    const plainDiary = {
      id: 4,
      activityId: 40,
      title: 'City walk',
      coverImage: '',
      photos: [],
      entries: [],
      participants: [],
      location: 'City',
      endTime: '2026-06-19',
      createdAt: '2026-06-19',
    }
    getDiaryDetailMock.mockResolvedValue(plainDiary)
    deleteDiaryApiMock.mockResolvedValue(undefined)

    const { useActivityStore } = await import('@/stores/activity')
    const store = useActivityStore()
    store.diaries = [plainDiary, { ...plainDiary, id: 5, title: 'Museum' }]

    await store.fetchDiaryDetail(4)
    await store.deleteDiary(4)

    expect(deleteDiaryApiMock).toHaveBeenCalledWith(4)
    expect(store.activeDiary).toBeNull()
    expect(store.diaries.map((diary) => diary.id)).toEqual([5])
  })
})
