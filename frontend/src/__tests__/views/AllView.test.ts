import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import * as VueRouter from 'vue-router'
import AllView from '@/views/activity/AllView.vue'

const {
  getMyActivitiesMock,
  getActivityDetailMock,
  deleteActivityMock,
  confirmMock,
} = vi.hoisted(() => ({
  getMyActivitiesMock: vi.fn(),
  getActivityDetailMock: vi.fn(),
  deleteActivityMock: vi.fn(),
  confirmMock: vi.fn(),
}))

vi.mock('@/api/activity', () => ({
  deleteActivity: deleteActivityMock,
  getActivityDetail: getActivityDetailMock,
  getMyActivities: getMyActivitiesMock,
}))

vi.mock('@/stores/user', () => ({
  useUserStore: () => ({
    userInfo: { id: 1 },
  }),
}))

vi.mock('element-plus', () => ({
  ElMessageBox: {
    confirm: confirmMock,
  },
}))

vi.mock('vue-router')

const StartActivityModalStub = {
  props: ['modelValue'],
  template: '<div v-if="modelValue" class="start-activity-modal-stub"></div>',
}

const DiaryEditorStub = {
  props: ['modelValue', 'activityId'],
  template: '<div v-if="modelValue" class="diary-editor-stub">{{ activityId }}</div>',
}

describe('AllView.vue', () => {
  const push = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(VueRouter.useRouter).mockReturnValue({ push } as any)

    getMyActivitiesMock.mockResolvedValue({
      pending: [
        { id: 1, creatorId: 1, title: 'Pending', description: '', activityTime: '2026-06-16T10:00:00Z', location: 'Lake', maxParticipants: 2, status: 0, createTime: '' },
      ],
      active: [
        { id: 2, creatorId: 1, title: 'Active', description: '', activityTime: '2026-06-16T11:00:00Z', location: 'Hill', maxParticipants: 3, status: 1, createTime: '' },
      ],
      completed: [
        { id: 3, creatorId: 1, title: 'Completed', description: '', activityTime: '2026-06-16T12:00:00Z', location: 'City', maxParticipants: 4, status: 2, createTime: '' },
      ],
      cancelled: [
        { id: 4, creatorId: 1, title: 'Cancelled', description: '', activityTime: '2026-06-16T13:00:00Z', location: 'Park', maxParticipants: 2, status: 3, createTime: '' },
      ],
    })

    getActivityDetailMock.mockImplementation(async (id: number) => ({
      activity: {} as any,
      participants: [{ id, nickname: `User ${id}`, avatar: `/u${id}.png` }],
      participantCount: 1,
      hasJournal: id === 4 ? true : false,
    }))
  })

  it('loads and renders grouped activities on mount', async () => {
    const wrapper = mount(AllView, {
      global: {
        stubs: {
          StartActivityModal: StartActivityModalStub,
          DiaryEditor: DiaryEditorStub,
          'el-button': { emits: ['click'], template: '<button @click="$emit(\'click\')"><slot /></button>' },
        },
        directives: {
          loading: () => undefined,
        },
      },
    })

    await flushPromises()

    expect(getMyActivitiesMock).toHaveBeenCalledTimes(1)
    expect(getActivityDetailMock).toHaveBeenCalledTimes(4)
    expect(wrapper.findAll('.group-section')).toHaveLength(4)
    expect(wrapper.text()).toContain('Your activity board')
    expect(wrapper.text()).toContain('Pending')
    expect(wrapper.text()).toContain('Active')
  })

  it('navigates active cards and opens the diary editor for completed activities without journals', async () => {
    const wrapper = mount(AllView, {
      global: {
        stubs: {
          StartActivityModal: StartActivityModalStub,
          DiaryEditor: DiaryEditorStub,
          'el-button': { emits: ['click'], template: '<button @click="$emit(\'click\')"><slot /></button>' },
        },
        directives: {
          loading: () => undefined,
        },
      },
    })

    await flushPromises()

    await wrapper.find('.status-active').trigger('click')
    expect(push).toHaveBeenCalledWith('/activity/2')

    await wrapper.find('.status-completed .inline-action.create').trigger('click')
    expect(wrapper.find('.diary-editor-stub').text()).toContain('3')
  })

  it('opens the create modal and deletes activities after confirmation', async () => {
    confirmMock.mockResolvedValue(undefined)
    deleteActivityMock.mockResolvedValue(undefined)

    const wrapper = mount(AllView, {
      global: {
        stubs: {
          StartActivityModal: StartActivityModalStub,
          DiaryEditor: DiaryEditorStub,
          'el-button': { emits: ['click'], template: '<button @click="$emit(\'click\')"><slot /></button>' },
        },
        directives: {
          loading: () => undefined,
        },
      },
    })

    await flushPromises()
    await wrapper.find('.page-header button').trigger('click')
    expect(wrapper.find('.start-activity-modal-stub').exists()).toBe(true)

    await wrapper.find('.status-pending .inline-action.danger').trigger('click')
    await flushPromises()

    expect(confirmMock).toHaveBeenCalled()
    expect(deleteActivityMock).toHaveBeenCalledWith(1)
    expect(getMyActivitiesMock).toHaveBeenCalledTimes(2)
  })
})
