<template>
  <div class="journal-view">
    <!-- Left: grid of journal cards -->
    <div class="journal-left">
      <div class="journal-grid">
        <!-- Existing journals -->
        <div
          v-for="j in diaries"
          :key="j.id"
          class="journal-card"
          :class="{ active: activeId === j.id }"
          @click="selectJournal(j.id)"
        >
          <div class="jc-img">
            <!-- 兼容 coverImage 或 images 字段，并提供兜底图 -->
            <img :src="j.coverImage || (j.images ? (Array.isArray(j.images) ? j.images[0] : j.images) : '') || 'https://images.unsplash.com/photo-1517486808906-6ca8b3f04846?q=80&w=400'" :alt="j.title">
          </div>
          <div class="jc-body">
            <!-- 兼容后端 content 字段作为标题 -->
            <div class="jc-title">{{ j.title || j.content || 'Untitled Journal' }}</div>
            <div class="jc-footer">
              <div class="jc-avs">
                <!-- 增加空值保护 -->
                <div
                  v-for="p in (j.participants || []).slice(0, 3)"
                  :key="p.userId"
                  class="jc-av"
                >
                  <img :src="p.avatar" :alt="p.nickname">
                </div>
              </div>
              <!-- 兼容后端 createTime 字段 -->
              <span class="jc-date" :class="{ green: activeId === j.id }">
                {{ formatDate(j.createdAt || j.createTime) }}
              </span>
            </div>
          </div>
        </div>

        <!-- New journal button -->
        <div class="journal-card new-card" @click="showEditor = true">
          <span class="new-plus">+</span>
          <span class="new-label">new journal</span>
        </div>
      </div>
    </div>

    <!-- Right: detail or empty state -->
    <div class="journal-right">
      <RouterView v-if="activeId" />
      <div v-else class="jr-empty">
        <span style="font-size:48px">📒</span>
        <p>Select a journal to view</p>
      </div>
    </div>

    <!-- Diary editor dialog -->
    <DiaryEditor
      v-if="showEditor"
      v-model="showEditor"
      :activity-id="0"
      @saved="onSaved"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import DiaryEditor from '@/components/activity/DiaryEditor.vue'
import { useActivityStore } from '@/stores/activity'
import { formatDate } from '@/utils/format'

const router       = useRouter()
const actStore     = useActivityStore()

// 使用 computed 保证 Store 数据变化时 UI 自动更新
const diaries      = computed(() => actStore.diaries)
const activeId     = ref<number | null>(null)
const showEditor   = ref(false)

const selectJournal = (id: number) => {
  activeId.value = id
  router.push(`/activity/journal/${id}`)
}

const onSaved = () => { actStore.fetchDiaries() }

onMounted(() => { actStore.fetchDiaries() })
</script>

<style scoped>
.journal-view { display: flex; flex: 1; overflow: hidden; height: 100%; }

/* Left panel */
.journal-left {
  width: 420px; flex-shrink: 0;
  background: rgba(255,255,255,.04);
  border-right: 0.5px solid var(--color-border-dim);
  overflow-y: auto; padding: 22px 18px;
}

.journal-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }

.journal-card {
  border-radius: var(--radius-lg); overflow: hidden;
  background: var(--color-card-solid);
  border: 0.5px solid var(--color-border-dim);
  cursor: pointer; transition: transform .15s, border-color .15s;
}
.journal-card:hover  { transform: translateY(-3px); border-color: rgba(255,255,255,.22); }
.journal-card.active { border-color: var(--color-green); }

.jc-img { width: 100%; height: 130px; overflow: hidden; background: #333; }
.jc-img img { width: 100%; height: 100%; object-fit: cover; }

.jc-body   { padding: 10px 12px; }
.jc-title  { font-size: 14px; font-weight: 500; margin-bottom: 8px; color: var(--color-text); }
.jc-footer { display: flex; align-items: center; justify-content: space-between; }
.jc-avs    { display: flex; }
.jc-av     { width: 22px; height: 22px; border-radius: 50%; overflow: hidden; background: #555; border: 1.5px solid #000; margin-left: -5px; }
.jc-av:first-child { margin-left: 0; }
.jc-av img { width: 100%; height: 100%; object-fit: cover; }
.jc-date      { font-size: 11px; color: var(--color-text-secondary); }
.jc-date.green{ color: var(--color-green); }

/* New card */
.new-card {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  min-height: 160px; border: 1.5px dashed var(--color-border) !important;
  background: transparent !important; color: var(--color-text-secondary);
  gap: 8px; transition: border-color .15s, color .15s !important;
}
.new-card:hover { border-color: var(--color-green-border) !important; color: var(--color-green) !important; }
.new-plus  { font-size: 28px; }
.new-label { font-size: 12px; font-family: monospace; }

/* Right panel */
.journal-right { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.jr-empty {
  flex: 1; display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  gap: 12px; opacity: .4; font-size: 14px;
}
</style>
