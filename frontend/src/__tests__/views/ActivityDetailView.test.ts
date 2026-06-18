import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import * as VueRouter from 'vue-router'
import ActivityDetailView from '@/views/activity/ActivityDetailView.vue'

const {
  completeActivityMock,
  deleteActivityMock,
  getActivityDetailMock,
  getEvaluationsByEvaluatorMock,
  getMyDiariesMock,
  reviewParticipantMock,
  confirmMock,
  successMock,
  errorMock,
} = vi.hoisted(() => ({
  completeActivityMock: vi.fn(),
  deleteActivityMock: vi.fn(),
  getActivityDetailMock: vi.fn(),
  getEvaluationsByEvaluatorMock: vi.fn(),
  getMyDiariesMock: vi.fn(),
  reviewParticipantMock: vi.fn(),
  confirmMock: vi.fn(),
  successMock: vi.fn(),
  errorMock: vi.fn(),
}))

vi.mock('@/api/activity', () => ({
  completeActivity: completeActivityMock,
  deleteActivity: deleteActivityMock,
  getActivityDetail: getActivityDetailMock,
  getEvaluationsByEvaluator: getEvaluationsByEvaluatorMock,
  getMyDiaries: getMyDiariesMock,
  reviewParticipant: reviewParticipantMock,
}))

vi.mock('element-plus', () => ({
  ElMessage: {
    success: successMock,
    error: errorMock,
  },
  ElMessageBox: {
    confirm: confirmMock,
  },
}))

vi.mock('@/stores/user', () => ({
  useUserStore: () => ({
    userInfo: {
      id: 1,
    },
  }),
}))

vi.mock('vue-router')

const StartActivityModalStub = {
  props: ['modelValue'],
  template: '<div v-if="modelValue" class="edit-modal-stub"></div>',
}

const DiaryEditorStub = {
  props: ['modelValue', 'activityId'],
  template: '<div v-if="modelValue" class="diary-editor-stub">{{ activityId }}</div>',
}

describe('ActivityDetailView.vue', () => {
  const push = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(VueRouter.useRoute).mockReturnValue({
      params: { activityId: '7' },
    } as any)
    vi.mocked(VueRouter.useRouter).mockReturnValue({ push } as any)

    getActivityDetailMock.mockResolvedValue({
      activity: {
        id: 7,
        creatorId: 1,
        title: 'Hike',
        description: 'Mountain trail',
        plan: 'Meet at gate',
        activityTime: '2026-06-16T10:00:00Z',
        location: 'West Lake',
        maxParticipants: 3,
        status: 1,
        createTime: '',
      },
      participants: [
        { id: 1, nickname: 'Me', avatar: '/me.png' },
        { id: 2, nickname: 'Amy', avatar: '/amy.png' },
      ],
      participantCount: 2,
      hasJournal: false,
    })
    getMyDiariesMock.mockResolvedValue({ content: [{ id: 21, activityId: 7 }] })
    getEvaluationsByEvaluatorMock.mockResolvedValue([])
    completeActivityMock.mockResolvedValue(undefined)
    reviewParticipantMock.mockResolvedValue(undefined)
    deleteActivityMock.mockResolvedValue(undefined)
    confirmMock.mockResolvedValue(undefined)
  })

  it('loads and renders activity detail on mount', async () => {
    const wrapper = mount(ActivityDetailView, {
      global: {
        stubs: {
          StartActivityModal: StartActivityModalStub,
          DiaryEditor: DiaryEditorStub,
          'el-button': { emits: ['click'], template: '<button @click="$emit(\'click\')"><slot /></button>' },
          'el-rate': { props: ['modelValue'], template: '<div class="rate-stub"></div>' },
        },
        directives: {
          loading: () => undefined,
        },
      },
    })

    await flushPromises()

    expect(getActivityDetailMock).toHaveBeenCalledWith(7)
    expect(wrapper.text()).toContain('Hike')
    expect(wrapper.text()).toContain('Mountain trail')
    expect(wrapper.findAll('.participant-row')).toHaveLength(2)
  })

  it('completes the activity, reveals review section and opens diary editor', async () => {
    const wrapper = mount(ActivityDetailView, {
      global: {
        stubs: {
          StartActivityModal: StartActivityModalStub,
          DiaryEditor: DiaryEditorStub,
          'el-button': { emits: ['click'], template: '<button @click="$emit(\'click\')"><slot /></button>' },
          'el-rate': { props: ['modelValue'], template: '<div class="rate-stub"></div>' },
        },
        directives: {
          loading: () => undefined,
        },
      },
    })

    await flushPromises()
    await (wrapper.vm as any).completeCurrentActivity()

    expect(completeActivityMock).toHaveBeenCalledWith(7)
    expect(wrapper.find('.diary-editor-stub').text()).toContain('7')
  })

  it('submits partner review and deletes the activity after confirmation', async () => {
    const wrapper = mount(ActivityDetailView, {
      global: {
        stubs: {
          StartActivityModal: StartActivityModalStub,
          DiaryEditor: DiaryEditorStub,
          'el-button': { emits: ['click'], template: '<button @click="$emit(\'click\')"><slot /></button>' },
          'el-rate': { props: ['modelValue'], template: '<div class="rate-stub"></div>' },
        },
        directives: {
          loading: () => undefined,
        },
      },
    })

    await flushPromises()
    await (wrapper.vm as any).handleRate(2, 3)
    expect(reviewParticipantMock).toHaveBeenCalledWith(7, {
      revieweeId: 2,
      rating: 3,
      comment: 'Submitted from activity detail view',
    })

    await (wrapper.vm as any).removeCurrentActivity()
    expect(confirmMock).toHaveBeenCalled()
    expect(deleteActivityMock).toHaveBeenCalledWith(7)
    expect(push).toHaveBeenCalledWith('/activity/all')
  })
})
