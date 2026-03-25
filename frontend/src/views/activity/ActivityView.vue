<template>
  <div class="activity-page">
    <!-- Vertical side nav (matches design: journal / partner icons) -->
    <nav class="vnav">
      <div
        class="vnav-item"
        :class="{ active: isJournal }"
        @click="router.push('/activity/journal')"
      >
        <span class="vnav-icon">📒</span>
        <span class="vnav-label">journal</span>
      </div>
      <div
        class="vnav-item"
        :class="{ active: isPartner }"
        @click="router.push('/activity/partner')"
      >
        <span class="vnav-icon">👤+</span>
        <span class="vnav-label">Partner</span>
      </div>
    </nav>

    <!-- Child page fills remaining space -->
    <div class="activity-content">
      <RouterView />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route  = useRoute()

const isJournal = computed(() => route.path.includes('journal'))
const isPartner = computed(() => route.path.includes('partner'))
</script>

<style scoped>
/* .activity-page {
  display: flex;
  height: calc(100vh - var(--nav-height));
  position: relative; z-index: 1;
  overflow: hidden;
} */

.activity-page {
display: flex;
height: calc(100vh - var(--nav-height));
margin-top: var(--nav-height);
position: relative;
z-index: 1;
overflow: hidden;
}

.vnav {
  width: var(--vnav-width);
  flex-shrink: 0;
  background: rgba(0,0,0,.55);
  backdrop-filter: blur(12px);
  border-right: 0.5px solid var(--color-border-dim);
  display: flex; flex-direction: column;
  align-items: center; padding: 24px 0; gap: 8px;
}

.vnav-item {
  width: 80px; padding: 14px 10px;
  display: flex; flex-direction: column;
  align-items: center; gap: 8px;
  border-radius: var(--radius-lg); cursor: pointer;
  transition: background .15s; color: var(--color-text-secondary);
}
.vnav-item:hover { background: var(--color-surface-1); color: var(--color-white); }
.vnav-item.active {
  background: rgba(0,0,0,.8);
  border: 1px solid var(--color-border);
  color: var(--color-white);
}
.vnav-item.active .vnav-label { color: var(--color-green); }

.vnav-icon  { font-size: 22px; }
.vnav-label { font-size: 12px; font-weight: 500; text-align: center; }

.activity-content { flex: 1; overflow: hidden; display: flex; flex-direction: column; }
</style>
