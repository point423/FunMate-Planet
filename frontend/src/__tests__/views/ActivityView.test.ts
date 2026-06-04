import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import ActivityView from '@/views/activity/ActivityView.vue'
import * as VueRouter from 'vue-router'

vi.mock('vue-router')

describe('ActivityView.vue', () => {
  let mockRouter: any
  let mockRoute: any

  beforeEach(() => {
    mockRouter = {
      push: vi.fn(),
    }
    mockRoute = {
      path: '/activity/journal',
    }
    vi.mocked(VueRouter.useRouter).mockReturnValue(mockRouter)
    vi.mocked(VueRouter.useRoute).mockReturnValue(mockRoute)
  })

  it('渲染活动页面布局', () => {
    const wrapper = mount(ActivityView, {
      global: {
        stubs: {
          'RouterView': true,
        },
        mocks: {
          $router: mockRouter,
          $route: mockRoute,
        },
      },
    })
    expect(wrapper.find('.activity-page').exists()).toBe(true)
  })

  it('显示垂直导航栏', () => {
    const wrapper = mount(ActivityView, {
      global: {
        stubs: {
          'RouterView': true,
        },
        mocks: {
          $router: mockRouter,
          $route: mockRoute,
        },
      },
    })
    expect(wrapper.find('.vnav').exists()).toBe(true)
  })

  it('导航栏包含日记和伙伴选项', () => {
    const wrapper = mount(ActivityView, {
      global: {
        stubs: {
          'RouterView': true,
        },
        mocks: {
          $router: mockRouter,
          $route: mockRoute,
        },
      },
    })
    const navItems = wrapper.findAll('.vnav-item')
    expect(navItems.length).toBe(2)
  })

  it('当前路由为日记时日记导航项应为活跃', () => {
    const wrapper = mount(ActivityView, {
      global: {
        stubs: {
          'RouterView': true,
        },
        mocks: {
          $router: mockRouter,
          $route: { path: '/activity/journal' },
        },
      },
    })
    
    const vm = wrapper.vm as any
    expect(vm.isJournal).toBe(true)
    expect(vm.isPartner).toBe(false)
  })

  it('当前路由为伙伴时伙伴导航项应为活跃', () => {
    // 重新 mock 路由为伙伴路由
    const mockPartnerRoute = { path: '/activity/partner' }
    vi.mocked(VueRouter.useRoute).mockReturnValue(mockPartnerRoute)
    
    const wrapper = mount(ActivityView, {
      global: {
        stubs: {
          'RouterView': true,
        },
        mocks: {
          $router: mockRouter,
          $route: mockPartnerRoute,
        },
      },
    })
    
    const vm = wrapper.vm as any
    expect(vm.isJournal).toBe(false)
    expect(vm.isPartner).toBe(true)
  })

  it('点击日记导航跳转到日记页面', async () => {
    const wrapper = mount(ActivityView, {
      global: {
        stubs: {
          'RouterView': true,
        },
        mocks: {
          $router: mockRouter,
          $route: mockRoute,
        },
      },
    })
    
    const journalNav = wrapper.findAll('.vnav-item')[0]
    await journalNav.trigger('click')
    
    expect(mockRouter.push).toHaveBeenCalledWith('/activity/journal')
  })

  it('点击伙伴导航跳转到伙伴页面', async () => {
    const wrapper = mount(ActivityView, {
      global: {
        stubs: {
          'RouterView': true,
        },
        mocks: {
          $router: mockRouter,
          $route: mockRoute,
        },
      },
    })
    
    const partnerNav = wrapper.findAll('.vnav-item')[1]
    await partnerNav.trigger('click')
    
    expect(mockRouter.push).toHaveBeenCalledWith('/activity/partner')
  })
})
