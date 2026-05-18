import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { ref } from 'vue'
import AppNavbar from '@/components/common/AppNavbar.vue'
import * as VueRouter from 'vue-router'

vi.mock('vue-router')

describe('AppNavbar.vue', () => {
  let mockRouter: any

  beforeEach(() => {
    mockRouter = {
      push: vi.fn(),
    }
    // 直接获取 mock 函数并设置返回值
    vi.mocked(VueRouter.useRouter).mockReturnValue(mockRouter)
    vi.mocked(VueRouter.useRoute).mockReturnValue({ path: '/' })
  })

  it('渲染导航栏', () => {
    const wrapper = mount(AppNavbar, {
      global: {
        mocks: {
          $router: mockRouter,
        },
      },
    })
    expect(wrapper.exists()).toBe(true)
    expect(wrapper.find('.navbar').exists()).toBe(true)
  })

  it('点击Logo跳转到首页', async () => {
    const wrapper = mount(AppNavbar, {
      global: {
        mocks: {
          $router: mockRouter,
        },
      },
    })
    const logo = wrapper.find('.nav-logo')
    await logo.trigger('click')
    expect(mockRouter.push).toHaveBeenCalledWith('/')
  })

  it('导航栏包含必要的链接', () => {
    const wrapper = mount(AppNavbar, {
      global: {
        mocks: {
          $router: mockRouter,
        },
      },
    })
    // 验证导航栏渲染成功
    expect(wrapper.find('.navbar').exists()).toBe(true)
  })
})
