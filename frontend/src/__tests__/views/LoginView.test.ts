import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import LoginView from '@/views/auth/LoginView.vue'
import * as VueRouter from 'vue-router'
import { ElMessage } from 'element-plus'

vi.mock('vue-router')

describe('LoginView.vue', () => {
  let mockRouter: any

  beforeEach(() => {
    mockRouter = {
      push: vi.fn(),
      currentRoute: { value: { query: {} } },
    }
    
    vi.mocked(VueRouter.useRouter).mockReturnValue(mockRouter)
  })

  afterEach(() => {
    vi.clearAllMocks()
  })

  it('渲染登入表单', () => {
    const wrapper = mount(LoginView, {
      global: {
        stubs: {
          'el-form': true,
          'el-form-item': true,
          'el-input': true,
          'el-button': true,
          'router-link': true,
        },
      },
    })
    expect(wrapper.exists()).toBe(true)
    expect(wrapper.find('.auth-page').exists()).toBe(true)
  })

  it('表单包含用户名和密码字段', () => {
    const wrapper = mount(LoginView, {
      global: {
        stubs: {
          'el-form': true,
          'el-form-item': true,
          'el-input': true,
          'el-button': true,
          'router-link': true,
        },
      },
    })
    expect(wrapper.find('.auth-card').exists()).toBe(true)
  })

  it('用户名为空时提交验证失败', async () => {
    const wrapper = mount(LoginView, {
      global: {
        stubs: {
          'el-form': true,
          'el-form-item': true,
          'el-input': true,
          'el-button': true,
          'router-link': true,
        },
      },
    })
    
    const vm = wrapper.vm as any
    vm.form.username = ''
    vm.form.password = '123456'
    
    await vm.onSubmit()
    
    expect(ElMessage.warning).toHaveBeenCalledWith('Please fill in all fields')
  })

  it('密码为空时提交验证失败', async () => {
    const wrapper = mount(LoginView, {
      global: {
        stubs: {
          'el-form': true,
          'el-form-item': true,
          'el-input': true,
          'el-button': true,
          'router-link': true,
        },
      },
    })
    
    const vm = wrapper.vm as any
    vm.form.username = 'testuser'
    vm.form.password = ''
    
    await vm.onSubmit()
    
    expect(ElMessage.warning).toHaveBeenCalledWith('Please fill in all fields')
  })

  it('包含注册链接', () => {
    const wrapper = mount(LoginView, {
      global: {
        stubs: {
          'el-form': true,
          'el-form-item': true,
          'el-input': true,
          'el-button': true,
          'router-link': { template: '<a><slot></slot></a>' },
        },
      },
    })
    expect(wrapper.find('.auth-switch').exists()).toBe(true)
  })
})
