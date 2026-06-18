import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import AiAssistantView from '@/views/ai/AiAssistantView.vue'

const getSuggestionMock = vi.hoisted(() => vi.fn())

vi.mock('@/api/ai', () => ({
  getSuggestion: getSuggestionMock,
}))

vi.mock('@/stores/user', () => ({
  useUserStore: () => ({
    userInfo: {
      tags: ['read', 'travel'],
      latitude: 30.27,
      longitude: 120.15,
    },
  }),
}))

describe('AiAssistantView.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders welcome tips before any message is sent', () => {
    const wrapper = mount(AiAssistantView)
    expect(wrapper.text()).toContain('AI')
    expect(wrapper.text()).toContain('你可以这样问我')
  })

  it('sends a message and renders the ai response', async () => {
    getSuggestionMock.mockResolvedValue({ suggestion: '试试去西湖徒步。' })

    const wrapper = mount(AiAssistantView)
    const input = wrapper.find('input')

    await input.setValue('周末推荐什么活动')
    await wrapper.find('button').trigger('click')
    await flushPromises()

    expect(getSuggestionMock).toHaveBeenCalledWith({
      tags: 'read,travel',
      location: '30.27,120.15',
      query: '周末推荐什么活动',
    })
    expect(wrapper.text()).toContain('周末推荐什么活动')
    expect(wrapper.text()).toContain('试试去西湖徒步。')
  })

  it('shows fallback error text when the ai service fails', async () => {
    getSuggestionMock.mockRejectedValue(new Error('boom'))

    const wrapper = mount(AiAssistantView)
    const input = wrapper.find('input')

    await input.setValue('帮我找搭子')
    await wrapper.find('button').trigger('click')
    await flushPromises()

    expect(wrapper.text()).toContain('服务暂时不可用')
  })
})
