<template>
  <div class="activity-page">
    <nav class="vnav">
      <button
        class="vnav-item"
        :class="{ active: isAll }"
        type="button"
        @click="router.push('/activity/all')"
      >
        <el-icon class="vnav-icon"><List /></el-icon>
        <span class="vnav-label">All</span>
      </button>
      <button
        class="vnav-item"
        :class="{ active: isJournal }"
        type="button"
        @click="router.push('/activity/journal')"
      >
        <el-icon class="vnav-icon"><Notebook /></el-icon>
        <span class="vnav-label">Journal</span>
      </button>
      <button
        class="vnav-item"
        :class="{ active: isPartner }"
        type="button"
        @click="router.push('/activity/partner')"
      >
        <el-icon class="vnav-icon"><User /></el-icon>
        <span class="vnav-label">Partner</span>
      </button>
    </nav>

    <div class="activity-content">
      <RouterView />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { List, Notebook, User } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'

const router = useRouter()
const route = useRoute()

const isAll = computed(() => route.path === '/activity' || route.path.includes('/activity/all'))
const isJournal = computed(() => route.path.includes('/activity/journal'))
const isPartner = computed(() => route.path.includes('/activity/partner'))
</script>

<style scoped>
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
  background: rgba(0, 0, 0, 0.55);
  backdrop-filter: blur(12px);
  border-right: 0.5px solid var(--color-border-dim);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 0;
  gap: 8px;
}

.vnav-item {
  width: 80px;
  padding: 14px 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  background: transparent;
  border: 0;
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: background 0.15s;
  color: var(--color-text-secondary);
}

.vnav-item:hover {
  background: var(--color-surface-1);
  color: var(--color-white);
}

.vnav-item.active {
  background: rgba(0, 0, 0, 0.8);
  border: 1px solid var(--color-border);
  color: var(--color-white);
}

.vnav-item.active .vnav-label {
  color: var(--color-green);
}

.vnav-icon {
  font-size: 22px;
}

.vnav-label {
  font-size: 12px;
  font-weight: 500;
  text-align: center;
}

.activity-content {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
</style>
