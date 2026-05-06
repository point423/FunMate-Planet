import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import TagSelector from '@/components/profile/TagSelector.vue'

describe('TagSelector.vue', () => {
  it('渲染标签选择器', () => {
    const wrapper = mount(TagSelector, {
      props: {
        modelValue: [],
      },
    })
    expect(wrapper.exists()).toBe(true)
    expect(wrapper.find('.tag-selector').exists()).toBe(true)
  })

  it('显示所有标签按钮', () => {
    const wrapper = mount(TagSelector, {
      props: {
        modelValue: [],
      },
    })
    const tags = wrapper.findAll('.tag-chip')
    // 应该有 12 个标签
    expect(tags.length).toBe(12)
  })

  it('点击标签时选中该标签', async () => {
    const wrapper = mount(TagSelector, {
      props: {
        modelValue: [],
      },
    })
    
    const firstTag = wrapper.find('.tag-chip')
    await firstTag.trigger('click')
    
    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    const emitted = wrapper.emitted('update:modelValue')
    expect(emitted![0]).toEqual([['read']])
  })

  it('显示已选择的标签数量', () => {
    const wrapper = mount(TagSelector, {
      props: {
        modelValue: ['read', 'climb'],
      },
    })
    expect(wrapper.find('.tag-hint').text()).toContain('Selected: 2 / 8')
  })

  it('达到最大选择数量后不能继续选择', async () => {
    const wrapper = mount(TagSelector, {
      props: {
        modelValue: ['read', 'climb', 'cycle', 'photo', 'draw', 'music', 'shop', 'cook'],
        max: 8,
      },
    })
    
    const unselectedTag = wrapper.findAll('.tag-chip').find(tag => !tag.classes('active'))
    
    if (unselectedTag) {
      await unselectedTag.trigger('click')
      // 由于已满，不应发出更新
      const events = wrapper.emitted()
      // 在达到最大值时，点击不选中的标签不应该发出事件
      if (events['update:modelValue']) {
        const lastEmit = events['update:modelValue'][events['update:modelValue'].length - 1]
        expect((lastEmit as any)[0].length).toBeLessThanOrEqual(8)
      }
    }
  })

  it('取消选择已选中的标签', async () => {
    const wrapper = mount(TagSelector, {
      props: {
        modelValue: ['read'],
      },
    })
    
    // 第一个标签是 'read'，应该已选中
    const tags = wrapper.findAll('.tag-chip')
    const readTag = tags[0]
    
    // 验证它是活跃的
    expect(readTag.classes('active')).toBe(true)
    
    // 点击取消选择
    await readTag.trigger('click')
    
    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    const emitted = wrapper.emitted('update:modelValue')
    expect(emitted![emitted!.length - 1]).toEqual([[]])
  })

  it('自定义最大选择数量', () => {
    const wrapper = mount(TagSelector, {
      props: {
        modelValue: [],
        max: 5,
      },
    })
    expect(wrapper.find('.tag-hint').text()).toContain('Selected: 0 / 5')
  })
})
