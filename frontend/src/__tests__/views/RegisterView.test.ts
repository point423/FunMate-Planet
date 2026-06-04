import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import RegisterView from '@/views/auth/RegisterView.vue'
import { ElMessage } from 'element-plus'

describe('RegisterView.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('渲染注册表单', () => {
    const wrapper = mount(RegisterView, {
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
    expect(wrapper.find('.auth-page').exists()).toBe(true)
    expect(wrapper.find('.auth-card').exists()).toBe(true)
  })

  it('表单包含所有必需字段', () => {
    const wrapper = mount(RegisterView, {
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
    expect(vm.form).toHaveProperty('nickname')
    expect(vm.form).toHaveProperty('username')
    expect(vm.form).toHaveProperty('password')
    expect(vm.form).toHaveProperty('confirmPassword')
  })

  it('密码不匹配时提交验证失败', async () => {
    const wrapper = mount(RegisterView, {
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
    vm.form.nickname = 'Test User'
    vm.form.username = 'testuser'
    vm.form.password = 'password123'
    vm.form.confirmPassword = 'password456'
    
    await vm.onSubmit()
    
    expect(ElMessage.error).toHaveBeenCalledWith('Passwords do not match')
  })

  it('密码匹配时允许提交', async () => {
    const wrapper = mount(RegisterView, {
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
    vm.form.nickname = 'Test User'
    vm.form.username = 'testuser'
    vm.form.password = 'password123'
    vm.form.confirmPassword = 'password123'
    
    // 不应该显示错误
    await vm.onSubmit()
    expect(ElMessage.error).not.toHaveBeenCalledWith('Passwords do not match')
  })

  it('包含登入链接', () => {
    const wrapper = mount(RegisterView, {
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
