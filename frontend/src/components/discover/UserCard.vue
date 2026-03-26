<template>
  <div class="user-card" @click="emit('click', user)">
    <div class="uc-top">
      <img :src="user.avatar" class="uc-avatar" :alt="user.nickname">
      <div class="uc-meta">
        <div class="uc-name">
          {{ user.nickname }}
          <span class="uc-dist">📍 {{ formatDistance(user.distance) }}</span>
        </div>
      </div>
    </div>
    <p class="uc-bio">{{ user.bio }}</p>
    <div class="uc-tags">
      <span v-for="tag in user.tags.slice(0,3)" :key="tag" class="tag-chip active">{{ tag }}</span>
    </div>
    <div class="uc-footer">
      <span class="uc-score">{{ user.activities }} activities · {{ formatScore(user.score) }}</span>
      <button class="btn-more" @click.stop="emit('detail', user)">more info.</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { NearbyUser } from '@/types/user'
import { formatDistance, formatScore } from '@/utils/format'

defineProps<{ user: NearbyUser }>()
const emit = defineEmits<{
  (e: 'click', u: NearbyUser): void
  (e: 'detail', u: NearbyUser): void
}>()
</script>

<style scoped>
.user-card {
  background: var(--color-surface-1);
  border: 0.5px solid var(--color-border-dim);
  border-radius: var(--radius-lg); padding: 16px;
  cursor: pointer; transition: background .15s, border-color .15s, transform .15s;
  display: flex; flex-direction: column; gap: 10px;
}
.user-card:hover {
  background: var(--color-surface-3);
  border-color: rgba(255,255,255,.2);
  transform: translateY(-2px);
}
.uc-top   { display: flex; align-items: center; gap: 12px; }
.uc-avatar{ width: 44px; height: 44px; border-radius: 50%; object-fit: cover;
             border: 1.5px solid var(--color-border); flex-shrink: 0; }
.uc-name  { font-size: 14px; font-weight: 600; display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }
.uc-dist  { font-size: 12px; color: var(--color-text-danger); font-weight: 400; }
.uc-bio   { font-size: 12px; color: var(--color-text-secondary); line-height: 1.5;
             display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.uc-tags  { display: flex; gap: 5px; flex-wrap: wrap; }
.uc-footer{ display: flex; align-items: center; justify-content: space-between; }
.uc-score { font-size: 11px; color: var(--color-text-secondary); }
.btn-more {
  padding: 5px 12px; border-radius: var(--radius-full);
  background: var(--color-surface-2); border: 1px solid var(--color-border);
  color: var(--color-white); font-size: 11px; cursor: pointer;
  transition: border-color .15s, color .15s;
}
.btn-more:hover { border-color: var(--color-green-border); color: var(--color-green); }
</style>
