<template>
  <div class="activity-card" :class="{ 'status-ended': activity.status === 2 }">
    <div class="ac-header">
      <span class="ac-tag" :class="statusClass">{{ statusText }}</span>
      <span class="ac-time">⏰ {{ formatTime(activity.activityTime) }}</span>
    </div>

    <h3 class="ac-title">{{ activity.title }}</h3>
    <p class="ac-desc">{{ activity.description }}</p>

    <div class="ac-meta">
      <div class="ac-loc">📍 {{ activity.location }}</div>
      <div class="ac-p">👥 {{ activity.maxParticipants }}人 max</div>
    </div>

    <div class="ac-footer">
      <button
        v-if="activity.status === 0"
        class="join-btn"
        @click.stop="$emit('join', activity.id)"
      >
        Join Now
      </button>
      <button
        v-if="activity.status === 2"
        class="review-btn"
        @click.stop="$emit('review', activity.id)"
      >
        View Review
      </button>
      <span v-if="activity.status === 1" class="ongoing-label">Ongoing...</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { formatDate } from '@/utils/format'

const props = defineProps<{
  activity: any
}>()

const statusText = computed(() => {
  const map: Record<number, string> = { 0: 'Recruiting', 1: 'Ongoing', 2: 'Ended' }
  return map[props.activity.status] || 'Unknown'
})

const statusClass = computed(() => {
  const map: Record<number, string> = { 0: 'tag-green', 1: 'tag-blue', 2: 'tag-gray' }
  return map[props.activity.status] || ''
})

const formatTime = (time: string) => {
  if (!time) return 'TBD'
  return new Date(time).toLocaleString([], { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}
</script>

<style scoped>
.activity-card {
  background: var(--color-surface-2);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  transition: all 0.2s;
  cursor: default;
}

.activity-card:hover {
  border-color: var(--color-green);
  transform: translateY(-2px);
}

.ac-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.ac-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: bold;
  text-transform: uppercase;
}

.tag-green { background: rgba(0, 255, 149, 0.2); color: var(--color-green); }
.tag-blue { background: rgba(0, 149, 255, 0.2); color: #0095ff; }
.tag-gray { background: rgba(255, 255, 255, 0.1); color: #888; }

.ac-time { font-size: 12px; color: var(--color-text-secondary); }

.ac-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  color: var(--color-white);
}

.ac-desc {
  font-size: 13px;
  color: var(--color-text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  height: 36px;
}

.ac-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--color-text-hint);
}

.ac-footer {
  margin-top: 4px;
  display: flex;
  justify-content: flex-end;
}

.join-btn {
  background: var(--color-green);
  color: black;
  border: none;
  padding: 6px 16px;
  border-radius: var(--radius-md);
  font-weight: 600;
  cursor: pointer;
}

.review-btn {
  background: transparent;
  border: 1px solid var(--color-border);
  color: var(--color-white);
  padding: 5px 14px;
  border-radius: var(--radius-md);
  cursor: pointer;
}

.ongoing-label {
  font-size: 12px;
  color: var(--color-green);
  font-style: italic;
}

.status-ended {
  opacity: 0.8;
  filter: grayscale(0.5);
}
</style>
