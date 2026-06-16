import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import MyProfileView from '@/views/profile/MyProfileView.vue'

const { uploadImageMock, updateProfileMock, fetchUserInfoMock } = vi.hoisted(() => ({
  uploadImageMock: vi.fn(),
  updateProfileMock: vi.fn(),
  fetchUserInfoMock: vi.fn(),
}))
const userStoreMock = {
  userInfo: {
    nickname: 'Amy',
    avatar: '/amy.png',
    bio: 'Reader',
    tags: ['read', 'travel'],
    activities: 6,
    score: 92,
    reviewCount: 5,
    ranking: 3,
    publicJournals: [{ id: 1, coverImage: '/cover.png', title: 'Trip', excerpt: 'Great day' }],
    recentActivities: [{ id: 1, icon: 'H', name: 'Hike', date: '2026-06-16T00:00:00Z', participants: ['/amy.png'] }],
  },
  fetchUserInfo: fetchUserInfoMock,
}

vi.mock('@/api/activity', () => ({
  uploadImage: uploadImageMock,
}))

vi.mock('@/api/user', () => ({
  updateProfile: updateProfileMock,
}))

vi.mock('@/stores/user', () => ({
  useUserStore: () => userStoreMock,
}))

const DialogStub = {
  props: ['modelValue'],
  template: '<div><slot /><slot name="footer" /></div>',
}

describe('MyProfileView.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders profile information and fetches fresh user info on mount', async () => {
    const wrapper = mount(MyProfileView, {
      global: {
        stubs: {
          'el-dialog': DialogStub,
          'el-form': { template: '<form><slot /></form>' },
          'el-form-item': { template: '<div><slot /></div>' },
          'el-input': { template: '<input />' },
          'el-button': { template: '<button><slot /></button>' },
          TagSelector: { template: '<div class="tag-selector-stub"></div>' },
          'el-image': { template: '<img />' },
        },
      },
    })

    await flushPromises()

    expect(fetchUserInfoMock).toHaveBeenCalledTimes(1)
    expect(wrapper.text()).toContain('Amy')
    expect(wrapper.text()).toContain('Reader')
    expect(wrapper.findAll('.jw-thumb')).toHaveLength(1)
  })

  it('opens edit dialog and saves profile updates', async () => {
    updateProfileMock.mockResolvedValue(undefined)

    const wrapper = mount(MyProfileView, {
      global: {
        stubs: {
          'el-dialog': DialogStub,
          'el-form': { template: '<form><slot /></form>' },
          'el-form-item': { template: '<div><slot /></div>' },
          'el-input': { template: '<input />' },
          'el-button': { template: '<button><slot /></button>' },
          TagSelector: { template: '<div class="tag-selector-stub"></div>' },
          'el-image': { template: '<img />' },
        },
      },
    })

    await wrapper.find('.btn-edit').trigger('click')
    await (wrapper.vm as any).saveProfile()

    expect(updateProfileMock).toHaveBeenCalled()
    expect(fetchUserInfoMock).toHaveBeenCalled()
  })

  it('uploads avatar files and rejects oversized images', async () => {
    uploadImageMock.mockResolvedValue({ url: '/uploaded.png' })
    updateProfileMock.mockResolvedValue(undefined)

    const wrapper = mount(MyProfileView, {
      global: {
        stubs: {
          'el-dialog': DialogStub,
          'el-form': { template: '<form><slot /></form>' },
          'el-form-item': { template: '<div><slot /></div>' },
          'el-input': { template: '<input />' },
          'el-button': { template: '<button><slot /></button>' },
          TagSelector: { template: '<div class="tag-selector-stub"></div>' },
          'el-image': { template: '<img />' },
        },
      },
    })

    const vm = wrapper.vm as any
    const bigFile = new File(['x'], 'big.png', { type: 'image/png' })
    Object.defineProperty(bigFile, 'size', { value: 6 * 1024 * 1024 })
    await vm.onAvatarChange({ target: { files: [bigFile] } })
    expect(uploadImageMock).not.toHaveBeenCalled()

    const smallFile = new File(['x'], 'small.png', { type: 'image/png' })
    Object.defineProperty(smallFile, 'size', { value: 1024 })
    await vm.onAvatarChange({ target: { files: [smallFile] } })

    expect(uploadImageMock).toHaveBeenCalledWith(smallFile)
    expect(updateProfileMock).toHaveBeenCalledWith({ avatar: '/uploaded.png' })
  })
})
