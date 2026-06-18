import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import * as VueRouter from 'vue-router'
import HomeView from '@/views/home/HomeView.vue'

const {
  getNearbyUsersMock,
  getRandomUserMock,
  sendFriendRequestMock,
  updateLocationMock,
  useUserStoreMock,
} = vi.hoisted(() => ({
  getNearbyUsersMock: vi.fn(),
  getRandomUserMock: vi.fn(),
  sendFriendRequestMock: vi.fn(),
  updateLocationMock: vi.fn(),
  useUserStoreMock: vi.fn(),
}))

vi.mock('@/api/dazi', () => ({
  getNearbyUsers: getNearbyUsersMock,
  getRandomUser: getRandomUserMock,
}))

vi.mock('@/api/user', () => ({
  sendFriendRequest: sendFriendRequestMock,
}))

vi.mock('@/composables/useLocation', () => ({
  useLocation: () => ({
    updateLocation: updateLocationMock,
  }),
}))

vi.mock('@/stores/user', () => ({
  useUserStore: useUserStoreMock,
}))

vi.mock('vue-router')

const DialogStub = {
  props: ['modelValue'],
  template: '<div><slot v-if="modelValue" /></div>',
}

describe('HomeView.vue', () => {
  const push = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(VueRouter.useRouter).mockReturnValue({ push } as any)
    useUserStoreMock.mockReturnValue({
      userInfo: { id: 1 },
    })

    Object.defineProperty(globalThis.navigator, 'geolocation', {
      value: {
        getCurrentPosition: (success: (value: GeolocationPosition) => void) => {
          success({
            coords: {
              longitude: 120.15,
              latitude: 30.27,
            },
          } as GeolocationPosition)
        },
      },
      configurable: true,
    })
  })

  it('loads orbit users on mount and excludes the current user', async () => {
    getNearbyUsersMock.mockResolvedValue([
      { id: 1, nickname: 'Me', tags: ['read'] },
      { id: 2, nickname: 'Amy', tags: ['travel'], avatar: '/amy.png' },
      { id: 3, nickname: 'Bob', tags: ['music'], avatar: '/bob.png' },
    ])

    const wrapper = mount(HomeView, {
      global: {
        stubs: {
          'el-dialog': DialogStub,
        },
      },
    })

    await flushPromises()

    expect(updateLocationMock).toHaveBeenCalledTimes(1)
    expect(getNearbyUsersMock).toHaveBeenCalled()
    expect(wrapper.findAll('.orbit-user')).toHaveLength(2)
    expect(wrapper.text()).toContain('Auto Match')
  })

  it('opens the match dialog, cycles matches and applies for friendship', async () => {
    getNearbyUsersMock.mockResolvedValue([
      { id: 2, nickname: 'Amy', tags: ['travel'], avatar: '/amy.png', distance: 1.2, score: 92 },
      { id: 3, nickname: 'Bob', tags: ['music'], avatar: '/bob.png', distance: 2.5, score: 88 },
    ])
    sendFriendRequestMock.mockResolvedValue(undefined)

    const wrapper = mount(HomeView, {
      global: {
        stubs: {
          'el-dialog': DialogStub,
        },
      },
    })

    await flushPromises()
    await wrapper.find('.btn-green').trigger('click')
    await flushPromises()

    expect(wrapper.text()).toContain('Amy')

    await wrapper.findAll('.btn-outline')[1].trigger('click')
    await flushPromises()
    expect(wrapper.text()).toContain('Bob')

    await wrapper.findAll('.btn-green')[1].trigger('click')
    expect(sendFriendRequestMock).toHaveBeenCalledWith(3)
  })

  it('falls back to a random user when no nearby candidates exist', async () => {
    getNearbyUsersMock.mockResolvedValue([])
    getRandomUserMock.mockResolvedValue({ id: 9, nickname: 'Solo', tags: ['read'], avatar: '/solo.png', distance: 0, score: 80 })

    const wrapper = mount(HomeView, {
      global: {
        stubs: {
          'el-dialog': DialogStub,
        },
      },
    })

    await flushPromises()
    await wrapper.find('.btn-green').trigger('click')
    await flushPromises()

    expect(getRandomUserMock).toHaveBeenCalledTimes(1)
    expect(wrapper.text()).toContain('Solo')
  })
})
