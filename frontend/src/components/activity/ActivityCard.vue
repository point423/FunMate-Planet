<template>
  <div class="activity-card" @click="$emit('click', activity)">
    <div class="ac-header">
      <span class="ac-tag" :class="statusClass">{{ statusText }}</span>
      <span class="ac-time">⏰ {{ formatTime(activity.activityTime) }}</span>
    </div>

    <h3 class="ac-title">{{ activity.title }}</h3>
    <p class="ac-desc">{{ activity.description }}</p>

    <div class="ac-meta">
      <div class="ac-loc">📍 {{ activity.location }}</div>
      <div class="ac-p">
        <!-- 核心需求：显示当前已加入人数 -->
        <span class="p-count-highlight">已加入 {{ joinedCount || 0 }} / {{ activity.maxParticipants }} 人</span>
      </div>
    </div>

    <div class="ac-footer">
      <span class="ac-action-hint">点击查看档案详情</span>
      <el-icon><ArrowRight /></el-icon>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'

const props = defineProps<{
  activity: any,
  joinedCount: number
}>()

defineEmits(['click'])

const statusText = computed(() => {
  const map: Record<number, string> = { 0: '招募中', 1: '进行中', 2: '已结束' }
  return map[props.activity.status] || '未知'
})

const statusClass = computed(() => {
  const map: Record<number, string> = { 0: 'tag-green', 1: 'tag-blue', 2: 'tag-gray' }
  return map[props.activity.status] || ''
})

const formatTime = (time: string) => {
  if (!time) return '待定'
  return new Date(time).toLocaleString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}
</script>

<style scoped>
.activity-card {
  background: var(--color-surface-2);
  border: 1px solid var(--color-border);
  border-radius: 16px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  position: relative;
  overflow: hidden;
}
.activity-card:hover {
  border-color: var(--color-green);
  transform: translateY(-5px);
  background: rgba(255, 255, 255, 0.05);
  box-shadow: 0 10px 30px rgba(0, 255, 149, 0.15);
}
.ac-header { display: flex; justify-content: space-between; align-items: center; }
.ac-tag { font-size: 10px; padding: 2px 8px; border-radius: 6px; font-weight: 800; text-transform: uppercase; }
.tag-green { background: rgba(0, 255, 149, 0.2); color: var(--color-green); }
.tag-blue { background: rgba(0, 149, 255, 0.2); color: #0095ff; }
.tag-gray { background: rgba(255, 255, 255, 0.1); color: #888; }
.ac-time { font-size: 12px; color: var(--color-text-secondary); }
.ac-title { font-size: 18px; font-weight: 700; color: #fff; margin: 0; }
.ac-desc { font-size: 13px; color: var(--color-text-secondary); height: 40px; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; line-height: 1.5; }
.ac-meta { display: flex; justify-content: space-between; font-size: 12px; color: var(--color-text-hint); margin-top: auto; }
.p-count-highlight { color: var(--color-green); font-weight: bold; background: rgba(0, 255, 149, 0.1); padding: 2px 6px; border-radius: 4px; }
.ac-footer { display: flex; justify-content: flex-end; align-items: center; gap: 4px; color: var(--color-green); font-size: 12px; margin-top: 8px; font-weight: 600; }
</style>
