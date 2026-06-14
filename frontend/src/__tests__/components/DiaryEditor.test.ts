import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import DiaryEditor from '@/components/activity/DiaryEditor.vue'

vi.mock('@/api/activity', () => ({
  createDiary: vi.fn(),
  getCompletableActivities: vi.fn(),
  getActivityDetail: vi.fn(),
}))

import { createDiary, getActivityDetail, getCompletableActivities } from '@/api/activity'

describe('DiaryEditor.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(getCompletableActivities).mockResolvedValue([
      {
        id: 1,
        creatorId: 1,
        title: 'Museum Walk',
        description: '',
        plan: '',
        activityTime: '2026-06-11T18:00:00',
        location: 'Downtown',
        maxParticipants: 2,
        status: 2,
        createTime: '2026-06-11T10:00:00',
      },
    ] as any)
    vi.mocked(getActivityDetail).mockResolvedValue({
      activity: {
        id: 1,
        creatorId: 1,
        title: 'Museum Walk',
        description: '',
        plan: '',
        activityTime: '2026-06-11T18:00:00',
        location: 'Downtown',
        maxParticipants: 2,
        status: 2,
        createTime: '2026-06-11T10:00:00',
      },
      participants: [{ id: 2, nickname: 'Alex', avatar: '' }],
      participantCount: 1,
      hasJournal: false,
    } as any)
  })

  const mountEditor = () =>
    mount(DiaryEditor, {
      props: {
        modelValue: true,
      },
      global: {
        stubs: {
          'el-dialog': { template: '<div class="diary-dialog"><slot /><slot name="footer" /></div>' },
          'el-form': { template: '<form><slot /></form>' },
          'el-form-item': { template: '<div><slot /></div>' },
          'el-input': { template: '<input />' },
          'el-button': { template: '<button><slot /></button>' },
          'el-select': { template: '<div><slot /></div>' },
          'el-option': { template: '<div><slot /></div>' },
        },
      },
    })

  it('renders the diary dialog shell', () => {
    const wrapper = mountEditor()
    expect(wrapper.find('.diary-dialog').exists()).toBe(true)
  })

  it('shows a warning when submitting without a title', async () => {
    const wrapper = mountEditor()
    await nextTick()

    const vm = wrapper.vm as any
    vm.selectedActivityId = 1
    vm.form.title = ''

    await vm.submit()

    expect(ElMessage.warning).toHaveBeenCalledWith('Please enter a title')
  })

  it('updates previews when files are selected', async () => {
    const wrapper = mountEditor()
    const vm = wrapper.vm as any

    const file = new File(['test'], 'test.jpg', { type: 'image/jpeg' })
    vm.onFileChange({
      target: {
        files: [file],
        value: '',
      },
    })

    await nextTick()

    expect(vm.files.length).toBe(1)
    expect(vm.previews.length).toBe(1)
  })

  it('submits with the selected activity id', async () => {
    vi.mocked(createDiary).mockResolvedValue({ id: 1 } as any)
    const wrapper = mountEditor()
    await nextTick()

    const vm = wrapper.vm as any
    vm.selectedActivityId = 1
    vm.form.title = 'Great Day'
    vm.form.content = 'Nice memory'

    await vm.submit()

    expect(createDiary).toHaveBeenCalled()
  })
})
