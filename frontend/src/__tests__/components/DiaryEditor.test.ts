import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import DiaryEditor from '@/components/activity/DiaryEditor.vue'
import { ElMessage } from 'element-plus'

describe('DiaryEditor.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('渲染日记编辑器对话框', () => {
    const wrapper = mount(DiaryEditor, {
      props: {
        modelValue: true,
        activityId: 1,
      },
      global: {
        stubs: {
          'el-dialog': { template: '<div class="dialog"><slot /></div>' },
          'el-form': true,
          'el-form-item': true,
          'el-input': true,
          'el-button': true,
        },
      },
    })
    expect(wrapper.find('.diary-dialog').exists()).toBe(true)
  })

  it('标题为空时提交验证失败', async () => {
    const wrapper = mount(DiaryEditor, {
      props: {
        modelValue: true,
        activityId: 1,
      },
      global: {
        stubs: {
          'el-dialog': { template: '<div class="dialog"><slot /></div>' },
          'el-form': true,
          'el-form-item': true,
          'el-input': true,
          'el-button': true,
        },
      },
    })
    
    const vm = wrapper.vm as any
    vm.form.title = ''
    vm.form.content = 'Some content'
    
    await vm.submit()
    
    expect(ElMessage.warning).toHaveBeenCalledWith('Please enter a title')
  })

  it('上传文件时更新预览', async () => {
    const wrapper = mount(DiaryEditor, {
      props: {
        modelValue: true,
        activityId: 1,
      },
      global: {
        stubs: {
          'el-dialog': { template: '<div class="dialog"><slot /></div>' },
          'el-form': true,
          'el-form-item': true,
          'el-input': true,
          'el-button': true,
        },
      },
    })
    
    const vm = wrapper.vm as any
    
    // 创建模拟文件
    const file = new File(['test'], 'test.jpg', { type: 'image/jpeg' })
    const event = {
      target: {
        files: [file],
      },
    }
    
    vm.onFileChange(event)
    await nextTick()
    
    expect(vm.files.length).toBe(1)
    expect(vm.previews.length).toBe(1)
  })

  it('可以移除已上传的照片', async () => {
    const wrapper = mount(DiaryEditor, {
      props: {
        modelValue: true,
        activityId: 1,
      },
      global: {
        stubs: {
          'el-dialog': { template: '<div class="dialog"><slot /></div>' },
          'el-form': true,
          'el-form-item': true,
          'el-input': true,
          'el-button': true,
        },
      },
    })
    
    const vm = wrapper.vm as any
    
    // 添加文件
    const file = new File(['test'], 'test.jpg', { type: 'image/jpeg' })
    vm.files.push(file)
    vm.previews.push('url')
    
    expect(vm.files.length).toBe(1)
    
    // 移除文件
    vm.removePhoto(0)
    await nextTick()
    
    expect(vm.files.length).toBe(0)
    expect(vm.previews.length).toBe(0)
  })

  it('最多允许 9 张照片', () => {
    const wrapper = mount(DiaryEditor, {
      props: {
        modelValue: true,
        activityId: 1,
      },
      global: {
        stubs: {
          'el-dialog': { template: '<div class="dialog"><slot /></div>' },
          'el-form': true,
          'el-form-item': true,
          'el-input': true,
          'el-button': true,
        },
      },
    })
    
    const vm = wrapper.vm as any
    
    // 添加 9 张照片
    for (let i = 0; i < 9; i++) {
      vm.previews.push(`url${i}`)
    }
    
    expect(vm.previews.length).toBe(9)
  })
})
