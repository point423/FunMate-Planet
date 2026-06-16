import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import * as VueRouter from 'vue-router'
import ChatView from '@/views/chat/ChatView.vue'

const {
  getMyActivitiesMock,
  getActivityDetailMock,
  getActivityInvitationsMock,
  handleActivityInvitationMock,
  createActivityMock,
  getChatConversationsMock,
  getChatMessagesMock,
  sendChatMessageMock,
  getFriendApplicationsMock,
  getFriendsMock,
  getUserByIdMock,
  getUserByUsernameMock,
  handleFriendRequestMock,
  sendFriendRequestMock,
} = vi.hoisted(() => ({
  getMyActivitiesMock: vi.fn(),
  getActivityDetailMock: vi.fn(),
  getActivityInvitationsMock: vi.fn(),
  handleActivityInvitationMock: vi.fn(),
  createActivityMock: vi.fn(),
  getChatConversationsMock: vi.fn(),
  getChatMessagesMock: vi.fn(),
  sendChatMessageMock: vi.fn(),
  getFriendApplicationsMock: vi.fn(),
  getFriendsMock: vi.fn(),
  getUserByIdMock: vi.fn(),
  getUserByUsernameMock: vi.fn(),
  handleFriendRequestMock: vi.fn(),
  sendFriendRequestMock: vi.fn(),
}))

vi.mock('@/api/activity', () => ({
  createActivity: createActivityMock,
  getActivityDetail: getActivityDetailMock,
  getActivityInvitations: getActivityInvitationsMock,
  getMyActivities: getMyActivitiesMock,
  handleActivityInvitation: handleActivityInvitationMock,
}))

vi.mock('@/api/chat', () => ({
  getChatConversations: getChatConversationsMock,
  getChatMessages: getChatMessagesMock,
  sendChatMessage: sendChatMessageMock,
}))

vi.mock('@/api/user', () => ({
  getFriendApplications: getFriendApplicationsMock,
  getFriends: getFriendsMock,
  getUserById: getUserByIdMock,
  getUserByUsername: getUserByUsernameMock,
  handleFriendRequest: handleFriendRequestMock,
  sendFriendRequest: sendFriendRequestMock,
}))

vi.mock('@/stores/user', () => ({
  useUserStore: () => ({
    userInfo: {
      id: 1,
      nickname: 'Me',
      avatar: '/me.png',
    },
    isLoggedIn: true,
    fetchUserInfo: vi.fn(),
  }),
}))

vi.mock('vue-router')

const DialogStub = {
  props: ['modelValue'],
  template: '<div v-if="modelValue"><slot /></div>',
}

describe('ChatView.vue', () => {
  const push = vi.fn()
  const replace = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(VueRouter.useRouter).mockReturnValue({ push, replace } as any)

    getFriendsMock.mockResolvedValue([{ id: 2 }])
    getUserByIdMock.mockResolvedValue({
      id: 2,
      username: 'amy',
      nickname: 'Amy',
      avatar: '/amy.png',
      bio: 'Reader',
      tags: ['read'],
      activities: 3,
      score: 90,
      ranking: 2,
      createdAt: '',
      publicJournals: [{ id: 11, coverImage: '/cover.png', title: 'Trip', excerpt: 'Great' }],
      recentActivities: [{ id: 1, icon: 'H', name: 'Hike', date: '2026-06-16', participants: ['/amy.png'] }],
    })
    getChatConversationsMock.mockResolvedValue([
      { userId: 2, lastMessage: 'Hello there', lastMessageTime: '2026-06-16T10:00:00Z' },
    ])
    getChatMessagesMock.mockResolvedValue({
      list: [
        { id: '1', senderId: 2, receiverId: 1, content: 'Hello there', createTime: '2026-06-16T10:00:00Z', isRead: true },
      ],
    })
    getFriendApplicationsMock.mockResolvedValue({ incoming: [] })
    getActivityInvitationsMock.mockResolvedValue({ incoming: [] })
    getMyActivitiesMock.mockResolvedValue({ active: [], pending: [], completed: [], cancelled: [] })
    createActivityMock.mockResolvedValue({ id: 99, activityTime: '2026-06-16T12:00:00Z' })
    sendChatMessageMock.mockResolvedValue(undefined)

    const storage = new Map<string, string>()
    Object.defineProperty(globalThis, 'localStorage', {
      value: {
        getItem: vi.fn((key: string) => storage.get(key) ?? null),
        setItem: vi.fn((key: string, value: string) => {
          storage.set(key, value)
        }),
        removeItem: vi.fn((key: string) => {
          storage.delete(key)
        }),
        key: vi.fn((index: number) => Array.from(storage.keys())[index] ?? null),
        get length() {
          return storage.size
        },
      },
      configurable: true,
    })
  })

  it('loads partner conversations on mount and renders the sidebar list', async () => {
    const wrapper = mount(ChatView, {
      global: {
        stubs: {
          ActivityCalendar: { template: '<div class="calendar-stub"></div>' },
          'el-dialog': DialogStub,
          'el-image': { template: '<img />' },
          'el-icon': { template: '<i><slot /></i>' },
        },
      },
    })

    await flushPromises()

    expect(getFriendsMock).toHaveBeenCalledTimes(1)
    expect(getChatConversationsMock).toHaveBeenCalledTimes(1)
    expect(wrapper.text()).toContain('Chat with your partner')
    expect(wrapper.findAll('.partner-item')).toHaveLength(1)
    expect(wrapper.text()).toContain('Amy')
  })

  it('selects a partner, sends a message and opens the activity dialog flow', async () => {
    const wrapper = mount(ChatView, {
      global: {
        stubs: {
          ActivityCalendar: { template: '<div class="calendar-stub"></div>' },
          'el-dialog': DialogStub,
          'el-image': { template: '<img />' },
          'el-icon': { template: '<i><slot /></i>' },
        },
      },
    })

    await flushPromises()
    await wrapper.find('.partner-item').trigger('click')
    await flushPromises()

    const vm = wrapper.vm as any
    vm.inputText = 'Ping'
    await vm.sendMessage()
    expect(sendChatMessageMock).toHaveBeenCalledWith(2, 'Ping')

    vm.startActivityVisible = true
    vm.activityDraft.name = 'Sunset Ride'
    vm.activityDraft.location = 'West Lake'
    vm.activityDraft.description = 'Go together'
    vm.activityDraft.startTime = '2026-06-16T12:00'
    vm.activityDraft.maxParticipants = 3
    await vm.confirmStartActivity()

    expect(createActivityMock).toHaveBeenCalled()
  })

  it('searches and adds a new partner candidate', async () => {
    getUserByUsernameMock.mockResolvedValue({
      id: 5,
      username: 'rose',
      nickname: 'Rose',
      avatar: '/rose.png',
      bio: 'Runner',
      tags: ['travel'],
      activities: 4,
      score: 88,
      ranking: 5,
      createdAt: '',
      publicJournals: [],
      recentActivities: [],
    })
    sendFriendRequestMock.mockResolvedValue(undefined)

    const wrapper = mount(ChatView, {
      global: {
        stubs: {
          ActivityCalendar: { template: '<div class="calendar-stub"></div>' },
          'el-dialog': DialogStub,
          'el-image': { template: '<img />' },
          'el-icon': { template: '<i><slot /></i>' },
        },
      },
    })

    await flushPromises()
    const vm = wrapper.vm as any
    vm.openAddPartnerDialog()
    vm.partnerSearchQ = 'rose'
    await vm.searchPartnerByUsername()
    expect(getUserByUsernameMock).toHaveBeenCalledWith('rose')

    await vm.addSearchedPartner()
    expect(sendFriendRequestMock).toHaveBeenCalledWith(5)
  })
})
