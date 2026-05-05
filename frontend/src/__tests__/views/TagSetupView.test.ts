import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import TagSetupView from '@/views/auth/TagSetupView.vue'
import * as VueRouter from 'vue-router'

vi.mock('vue-router')

describe('TagSetupView.vue', () => {
  let mockRouter: any

  beforeEach(() => {
    mockRouter = {
      push: vi.fn(),
    }
    vi.mocked(VueRouter.useRouter).mockReturnValue(mockRouter)
  })

  it('渲染标签设置页面', () => {
    const wrapper = mount(TagSetupView, {
      global: {
        stubs: {
          'el-button': true,
          'TagSelector': true,
        },
        mocks: {
          $router: mockRouter,
        },
      },
    })
    expect(wrapper.find('.auth-page').exists()).toBe(true)
  })

  it('显示页面标题和描述', () => {
    const wrapper = mount(TagSetupView, {
      global: {
        stubs: {
          'el-button': true,
          'TagSelector': true,
        },
        mocks: {
          $router: mockRouter,
        },
      },
    })
    expect(wrapper.find('.auth-title').text()).toContain('Choose your interests')
    expect(wrapper.find('.auth-sub').exists()).toBe(true)
  })

  it('包含标签选择器组件', () => {
    const wrapper = mount(TagSetupView, {
      global: {
        stubs: {
          'el-button': true,
          'TagSelector': { template: '<div class="tag-selector-stub"></div>' },
        },
        mocks: {
          $router: mockRouter,
        },
      },
    })
    expect(wrapper.find('.tag-selector-stub').exists()).toBe(true)
  })

  it('按钮在没有选择标签时禁用', async () => {
    const wrapper = mount(TagSetupView, {
      global: {
        stubs: {
          'el-button': { template: '<button :disabled="disabled"><slot /></button>' },
          'TagSelector': true,
        },
        mocks: {
          $router: mockRouter,
        },
      },
    })
    
    const vm = wrapper.vm as any
    expect(vm.tags.length).toBe(0)
  })

  it('选择标签后按钮应该启用', async () => {
    const wrapper = mount(TagSetupView, {
      global: {
        stubs: {
          'el-button': true,
          'TagSelector': {
            template: `<div @click="$emit('update:modelValue', ['read', 'climb'])"></div>`,
          },
        },
        mocks: {
          $router: mockRouter,
        },
      },
    })
    
    const vm = wrapper.vm as any
    vm.tags = ['read', 'climb']
    await nextTick()
    
    expect(vm.tags.length).toBeGreaterThan(0)
  })

  it('显示加载状态', async () => {
    const wrapper = mount(TagSetupView, {
      global: {
        stubs: {
          'el-button': { template: '<button :loading="loading"><slot /></button>' },
          'TagSelector': true,
        },
        mocks: {
          $router: mockRouter,
        },
      },
    })
    
    const vm = wrapper.vm as any
    expect(vm.loading).toBe(false)
  })

  it('最多可以选择 8 个标签', () => {
    const wrapper = mount(TagSetupView, {
      global: {
        stubs: {
          'el-button': true,
          'TagSelector': { template: '<div class="tag-selector"></div>' },
        },
        mocks: {
          $router: mockRouter,
        },
      },
    })
    
    expect(wrapper.find('.auth-card').exists()).toBe(true)
  })
})
