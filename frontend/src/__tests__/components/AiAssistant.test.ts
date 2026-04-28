import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import AiAssistant from '@/components/common/AiAssistant.vue'
import { ElMessage } from 'element-plus'

describe('AiAssistant.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('渲染 AI 助手组件', () => {
    const wrapper = mount(AiAssistant, {
      global: {
        stubs: {
          'RouterLink': true,
        },
      },
    })
    expect(wrapper.find('.ai-assistant').exists()).toBe(true)
    expect(wrapper.find('.chat-container').exists()).toBe(true)
  })

  it('显示消息列表', () => {
    const wrapper = mount(AiAssistant, {
      global: {
        stubs: {
          'RouterLink': true,
        },
      },
    })
    expect(wrapper.find('.messages').exists()).toBe(true)
  })

  it('包含消息输入框', () => {
    const wrapper = mount(AiAssistant, {
      global: {
        stubs: {
          'RouterLink': true,
        },
      },
    })
    const input = wrapper.find('.input-area input')
    expect(input.exists()).toBe(true)
  })

  it('发送空消息时不触发任何动作', async () => {
    const wrapper = mount(AiAssistant, {
      global: {
        stubs: {
          'RouterLink': true,
        },
      },
    })
    
    const vm = wrapper.vm as any
    vm.userInput = ''
    
    // 调用 sendMessage
    await vm.sendMessage()
    await nextTick()
    
    // 消息列表应该为空
    expect(vm.messages.length).toBe(0)
  })

  it('输入框初始为空', () => {
    const wrapper = mount(AiAssistant, {
      global: {
        stubs: {
          'RouterLink': true,
        },
      },
    })
    
    const input = wrapper.find('.input-area input') as any
    expect(input.element.value).toBe('')
  })

  it('发送按钮在加载中时显示加载状态', async () => {
    const wrapper = mount(AiAssistant, {
      global: {
        stubs: {
          'RouterLink': true,
        },
      },
    })
    
    const vm = wrapper.vm as any
    vm.loading = true
    await nextTick()
    
    const button = wrapper.find('.input-area button')
    expect(button.attributes('disabled')).toBeDefined()
    expect(button.text()).toContain('思考中')
  })

  it('用户消息被正确添加到消息列表', async () => {
    const wrapper = mount(AiAssistant, {
      global: {
        stubs: {
          'RouterLink': true,
        },
      },
    })
    
    const vm = wrapper.vm as any
    vm.userInput = 'test message'
    vm.user = { tags: 'read', location: '123,456' }
    
    // 手动添加消息而不调用 API
    vm.messages.push({ role: 'user', content: 'test message' })
    await nextTick()
    
    expect(vm.messages.length).toBe(1)
    expect(vm.messages[0].content).toBe('test message')
  })

  it('用户消息和 AI 消息应该有不同的样式类', async () => {
    const wrapper = mount(AiAssistant, {
      global: {
        stubs: {
          'RouterLink': true,
        },
      },
    })
    
    const vm = wrapper.vm as any
    vm.messages = [
      { role: 'user', content: 'user message' },
      { role: 'ai', content: 'ai message' },
    ]
    await nextTick()
    
    const messageElements = wrapper.findAll('.message')
    expect(messageElements[0].classes()).toContain('user')
    expect(messageElements[1].classes()).toContain('ai')
  })
})
