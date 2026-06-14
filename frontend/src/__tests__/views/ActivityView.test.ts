import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import * as VueRouter from 'vue-router'
import ActivityView from '@/views/activity/ActivityView.vue'

vi.mock('vue-router')

describe('ActivityView.vue', () => {
  let mockRouter: { push: ReturnType<typeof vi.fn> }
  let mockRoute: { path: string }

  beforeEach(() => {
    mockRouter = { push: vi.fn() }
    mockRoute = { path: '/activity/all' }
    vi.mocked(VueRouter.useRouter).mockReturnValue(mockRouter as any)
    vi.mocked(VueRouter.useRoute).mockReturnValue(mockRoute as any)
  })

  it('renders the activity shell and side navigation', () => {
    const wrapper = mount(ActivityView, {
      global: {
        stubs: {
          RouterView: true,
          ElIcon: { template: '<i><slot /></i>' },
        },
      },
    })

    expect(wrapper.find('.activity-page').exists()).toBe(true)
    expect(wrapper.find('.vnav').exists()).toBe(true)
    expect(wrapper.findAll('.vnav-item')).toHaveLength(3)
  })

  it('marks the all item as active on the all route', () => {
    const wrapper = mount(ActivityView, {
      global: {
        stubs: {
          RouterView: true,
          ElIcon: { template: '<i><slot /></i>' },
        },
      },
    })

    expect(wrapper.findAll('.vnav-item')[0].classes()).toContain('active')
  })

  it('navigates to partner when the partner item is clicked', async () => {
    const wrapper = mount(ActivityView, {
      global: {
        stubs: {
          RouterView: true,
          ElIcon: { template: '<i><slot /></i>' },
        },
      },
    })

    await wrapper.findAll('.vnav-item')[2].trigger('click')

    expect(mockRouter.push).toHaveBeenCalledWith('/activity/partner')
  })
})
