import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import * as VueRouter from 'vue-router'
import UserDetailView from '@/views/user/UserDetailView.vue'

const { getUserByIdMock, sendFriendRequestMock } = vi.hoisted(() => ({
  getUserByIdMock: vi.fn(),
  sendFriendRequestMock: vi.fn(),
}))

vi.mock('@/api/user', () => ({
  getUserById: getUserByIdMock,
  sendFriendRequest: sendFriendRequestMock,
}))

vi.mock('vue-router')

const DialogStub = {
  props: ['modelValue'],
  template: '<div><slot /></div>',
}

describe('UserDetailView.vue', () => {
  const back = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(VueRouter.useRouter).mockReturnValue({ back } as any)
    vi.mocked(VueRouter.useRoute).mockReturnValue({
      params: { id: '9' },
    } as any)
  })

  it('loads and renders a user profile on mount', async () => {
    getUserByIdMock.mockResolvedValue({
      id: 9,
      nickname: 'Nora',
      avatar: '/nora.png',
      bio: 'Climber',
      tags: ['climb', 'travel'],
      activities: 4,
      score: 90,
      reviewCount: 6,
      ranking: 2,
      publicJournals: [{ id: 1, coverImage: '/cover.png', title: 'Trip', excerpt: 'Nice' }],
      recentActivities: [{ id: 1, icon: 'T', name: 'Trip', date: '2026-06-16T00:00:00Z', participants: ['/nora.png'] }],
    })

    const wrapper = mount(UserDetailView, {
      global: {
        stubs: {
          'el-dialog': DialogStub,
          'el-image': { template: '<img />' },
          'el-skeleton': { template: '<div class="skeleton"></div>' },
        },
      },
    })

    await flushPromises()

    expect(getUserByIdMock).toHaveBeenCalledWith(9)
    expect(wrapper.text()).toContain('Nora')
    expect(wrapper.findAll('.jw-thumb')).toHaveLength(1)
  })

  it('applies for friendship and navigates back', async () => {
    getUserByIdMock.mockResolvedValue({
      id: 9,
      nickname: 'Nora',
      avatar: '/nora.png',
      bio: '',
      tags: ['climb'],
      activities: 4,
      score: 90,
      reviewCount: 6,
      ranking: 2,
      publicJournals: [],
      recentActivities: [],
    })
    sendFriendRequestMock.mockResolvedValue(undefined)

    const wrapper = mount(UserDetailView, {
      global: {
        stubs: {
          'el-dialog': DialogStub,
          'el-image': { template: '<img />' },
          'el-skeleton': { template: '<div class="skeleton"></div>' },
        },
      },
    })

    await flushPromises()
    await wrapper.find('.btn-apply').trigger('click')

    expect(sendFriendRequestMock).toHaveBeenCalledWith(9)
    expect(back).toHaveBeenCalled()
  })

  it('shows the empty state when loading the user fails', async () => {
    getUserByIdMock.mockRejectedValue(new Error('not found'))

    const wrapper = mount(UserDetailView, {
      global: {
        stubs: {
          'el-dialog': DialogStub,
          'el-image': { template: '<img />' },
          'el-skeleton': { template: '<div class="skeleton"></div>' },
        },
      },
    })

    await flushPromises()

    expect(wrapper.text()).toContain('User not found.')
  })
})
