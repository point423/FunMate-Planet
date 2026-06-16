import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import * as VueRouter from 'vue-router'
import JournalView from '@/views/activity/JournalView.vue'

const fetchDiariesMock = vi.hoisted(() => vi.fn())
const fetchDiaryDetailMock = vi.hoisted(() => vi.fn())
const deleteDiaryMock = vi.hoisted(() => vi.fn())
const shareMySharedDiaryEntryMock = vi.hoisted(() => vi.fn())
const updateMySharedDiaryEntryMock = vi.hoisted(() => vi.fn())
const uploadImageMock = vi.hoisted(() => vi.fn())

const storeState = {
  diaries: [] as any[],
  activeDiary: null as any,
}

vi.mock('@/stores/activity', () => ({
  useActivityStore: () => ({
    get diaries() {
      return storeState.diaries
    },
    get activeDiary() {
      return storeState.activeDiary
    },
    fetchDiaries: fetchDiariesMock,
    fetchDiaryDetail: fetchDiaryDetailMock,
    deleteDiary: deleteDiaryMock,
  }),
}))

vi.mock('@/stores/user', () => ({
  useUserStore: () => ({
    userInfo: {
      id: 1,
      nickname: 'Amy',
    },
  }),
}))

vi.mock('@/api/activity', () => ({
  getDiaryDetail: vi.fn(),
  shareMySharedDiaryEntry: shareMySharedDiaryEntryMock,
  updateMySharedDiaryEntry: updateMySharedDiaryEntryMock,
  uploadImage: uploadImageMock,
}))

vi.mock('vue-router')

const DiaryEditorStub = {
  props: ['modelValue'],
  emits: ['update:modelValue', 'saved'],
  template: '<div class="diary-editor-stub"></div>',
}

describe('JournalView.vue', () => {
  const replace = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()
    storeState.diaries = [
      {
        id: 10,
        title: 'First Journal',
        coverImage: '/cover.png',
        createdAt: '2026-06-16T00:00:00Z',
        participants: [{ userId: 1, avatar: '/amy.png', nickname: 'Amy' }],
        sharedEntries: [
          {
            user: { id: 1, nickname: 'Amy', avatar: '/amy.png' },
            entry: {
              content: 'My note',
              images: ['/cover.png'],
              createTime: '2026-06-16T00:00:00Z',
              updateTime: '2026-06-16T00:00:00Z',
            },
          },
        ],
      },
    ]
    storeState.activeDiary = null

    fetchDiariesMock.mockResolvedValue(undefined)
    fetchDiaryDetailMock.mockImplementation(async (id: number) => {
      storeState.activeDiary = {
        id,
        title: 'First Journal',
        coverImage: '/cover.png',
        createdAt: '2026-06-16T00:00:00Z',
        location: 'West Lake',
        userId: 1,
        participants: [{ userId: 1, avatar: '/amy.png', nickname: 'Amy' }],
        sharedEntries: [
          {
            user: { id: 1, nickname: 'Amy', avatar: '/amy.png' },
            entry: {
              content: 'My note',
              images: ['/cover.png'],
              createTime: '2026-06-16T00:00:00Z',
              updateTime: '2026-06-16T00:00:00Z',
            },
          },
        ],
      }
      return storeState.activeDiary
    })

    vi.mocked(VueRouter.useRoute).mockReturnValue({
      query: {},
      params: {},
    } as any)
    vi.mocked(VueRouter.useRouter).mockReturnValue({
      replace,
    } as any)
  })

  it('loads diaries on mount and selects the first journal', async () => {
    const wrapper = mount(JournalView, {
      global: {
        stubs: {
          DiaryEditor: DiaryEditorStub,
          'el-image': { template: '<img />' },
          'el-popconfirm': { template: '<div><slot name="reference" /></div>' },
          'el-button': { template: '<button><slot /></button>' },
        },
      },
    })

    await flushPromises()

    expect(fetchDiariesMock).toHaveBeenCalledTimes(1)
    expect(fetchDiaryDetailMock).toHaveBeenCalledWith(10)
    expect(wrapper.text()).toContain('Shared Journal')
    expect(wrapper.find('.journal-card').exists()).toBe(true)
  })

  it('opens the editor, saves shared entries and shares to profile', async () => {
    updateMySharedDiaryEntryMock.mockResolvedValue(undefined)
    shareMySharedDiaryEntryMock.mockResolvedValue(undefined)

    const wrapper = mount(JournalView, {
      global: {
        stubs: {
          DiaryEditor: DiaryEditorStub,
          'el-image': { template: '<img />' },
          'el-popconfirm': { template: '<div><slot name="reference" /></div>' },
          'el-button': { template: '<button><slot /></button>' },
        },
      },
    })

    await flushPromises()
    await wrapper.find('.new-card').trigger('click')
    expect(wrapper.find('.diary-editor-stub').exists()).toBe(true)

    await (wrapper.vm as any).saveMyEntry(1)
    expect(updateMySharedDiaryEntryMock).toHaveBeenCalledWith(10, {
      content: 'My note',
      images: ['/cover.png'],
    })

    await (wrapper.vm as any).shareMyEntry(1)
    expect(shareMySharedDiaryEntryMock).toHaveBeenCalledWith(10)
  })

  it('uploads entry images, removes draft images and deletes journals', async () => {
    uploadImageMock.mockResolvedValue({ url: '/new.png' })
    deleteDiaryMock.mockResolvedValue(undefined)

    const wrapper = mount(JournalView, {
      global: {
        stubs: {
          DiaryEditor: DiaryEditorStub,
          'el-image': { template: '<img />' },
          'el-popconfirm': { template: '<div><slot name="reference" /></div>' },
          'el-button': { template: '<button><slot /></button>' },
        },
      },
    })

    await flushPromises()
    const vm = wrapper.vm as any

    const file = new File(['x'], 'cover.png', { type: 'image/png' })
    await vm.onEntryFileChange({ target: { files: [file], value: '' } }, 1)
    expect(uploadImageMock).toHaveBeenCalledWith(file)

    vm.removeDraftImage(1, 0)
    expect(vm.entryDrafts[1].images).toEqual(['/new.png'])

    await vm.handleDelete()
    expect(deleteDiaryMock).toHaveBeenCalledWith(10)
    expect(replace).toHaveBeenCalled()
  })
})
