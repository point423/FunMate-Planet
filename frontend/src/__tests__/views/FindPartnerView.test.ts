import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import * as VueRouter from 'vue-router'
import FindPartnerView from '@/views/activity/FindPartnerView.vue'

const getNearbyUsersMock = vi.hoisted(() => vi.fn())
const getLeaderboardMock = vi.hoisted(() => vi.fn())
const getFriendsMock = vi.hoisted(() => vi.fn())

vi.mock('@/api/dazi', () => ({
  getNearbyUsers: getNearbyUsersMock,
  getLeaderboard: getLeaderboardMock,
}))

vi.mock('@/api/user', () => ({
  getFriends: getFriendsMock,
}))

vi.mock('@/stores/user', () => ({
  useUserStore: () => ({
    userInfo: {
      id: 1,
      activities: 12,
      score: 92,
      reviewCount: 8,
      ranking: 3,
    },
  }),
}))

vi.mock('vue-router')

const UserCardStub = {
  props: ['user'],
  emits: ['select'],
  template: '<button class="user-card-stub" @click="$emit(\'select\', user)">{{ user.nickname }}</button>',
}

describe('FindPartnerView.vue', () => {
  const push = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(VueRouter.useRouter).mockReturnValue({ push } as any)
  })

  it('loads nearby users and leaderboard on mount', async () => {
    getNearbyUsersMock.mockResolvedValue([
      { id: 1, nickname: 'Me', tags: ['read'], avatar: '/me.png' },
      { id: 2, nickname: 'Amy', tags: ['travel'], avatar: '/amy.png', activities: 4, score: 90, ranking: 2 },
      { id: 3, nickname: 'Bob', tags: ['music'], avatar: '/bob.png', activities: 6, score: 88, ranking: 4 },
    ])
    getFriendsMock.mockResolvedValue([{ id: 3 }])
    getLeaderboardMock.mockResolvedValue([
      { id: 2, nickname: 'Amy', tags: ['travel'], avatar: '/amy.png', activities: 4, score: 90, ranking: 2 },
      { id: 4, nickname: 'Cara', tags: ['read'], avatar: '/cara.png', activities: 9, score: 95, ranking: 1 },
      { id: 5, nickname: 'Drew', tags: ['music'], avatar: '/drew.png', activities: 7, score: 89, ranking: 3 },
      { id: 6, nickname: 'Eve', tags: ['travel'], avatar: '/eve.png', activities: 5, score: 87, ranking: 4 },
    ])

    const wrapper = mount(FindPartnerView, {
      global: {
        stubs: {
          UserCard: UserCardStub,
          'el-icon': { template: '<i><slot /></i>' },
        },
      },
    })

    await flushPromises()

    expect(wrapper.text()).toContain('Partner Finder')
    expect(wrapper.text()).toContain('Top rated partners')
    expect(wrapper.findAll('.user-card-stub')).toHaveLength(1)
    expect(wrapper.text()).toContain('Amy')
    expect(wrapper.text()).toContain('Cara')
  })

  it('filters by tag and keyword and routes to the selected profile', async () => {
    getNearbyUsersMock.mockResolvedValue([
      { id: 2, nickname: 'Amy', tags: ['travel'], avatar: '/amy.png', activities: 4, score: 90, ranking: 2 },
      { id: 3, nickname: 'Bob', tags: ['music'], avatar: '/bob.png', activities: 6, score: 88, ranking: 4 },
    ])
    getFriendsMock.mockResolvedValue([])
    getLeaderboardMock.mockResolvedValue([])

    const wrapper = mount(FindPartnerView, {
      global: {
        stubs: {
          UserCard: UserCardStub,
          'el-icon': { template: '<i><slot /></i>' },
        },
      },
    })

    await flushPromises()
    const travelChip = wrapper.findAll('.tag-filter-chip').find((node) => node.text().includes('travel'))
    expect(travelChip).toBeTruthy()
    await travelChip!.trigger('click')
    await flushPromises()
    expect(getNearbyUsersMock).toHaveBeenLastCalledWith({ radius: 10, tags: 'travel' })

    const input = wrapper.find('.search-input')
    await input.setValue('amy')
    expect(wrapper.findAll('.user-card-stub')).toHaveLength(1)

    await wrapper.find('.user-card-stub').trigger('click')
    expect(push).toHaveBeenCalledWith('/user/2')
  })

  it('shows the empty state when no user matches and keeps leaderboard when nearby loading fails', async () => {
    getNearbyUsersMock.mockRejectedValue(new Error('network'))
    getFriendsMock.mockResolvedValue([])
    getLeaderboardMock.mockResolvedValue([
      { id: 4, nickname: 'Cara', tags: ['read'], avatar: '/cara.png', activities: 9, score: 95, ranking: 1 },
    ])

    const wrapper = mount(FindPartnerView, {
      global: {
        stubs: {
          UserCard: UserCardStub,
          'el-icon': { template: '<i><slot /></i>' },
        },
      },
    })

    await flushPromises()

    expect(wrapper.text()).toContain('No matching partner yet')
    expect(wrapper.text()).toContain('Cara')
  })
})
