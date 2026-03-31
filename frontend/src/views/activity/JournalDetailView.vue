<template>
  <div class="journal-detail" v-if="diary">
    <!-- Cover photo -->
    <div class="jd-cover">
      <img :src="diary.coverImage" :alt="diary.title">
    </div>

    <!-- Content area -->
    <div class="jd-content">
      <!-- Title + info buttons -->
      <h2 class="jd-title">{{ diary.title }}</h2>
      <div class="jd-info-btns">
        <button class="jr-btn" @click="openModal('participants')">Participants</button>
        <button class="jr-btn" @click="openModal('location')">Location</button>
        <button class="jr-btn" @click="openModal('lasttime')">Last time</button>
      </div>

      <!-- Diary entries (two-column grid) -->
      <div class="jd-entries">
        <div
          v-for="entry in diary.entries"
          :key="entry.id"
          class="jd-entry"
        >
          <p class="entry-text">{{ entry.content }}</p>
          <div class="entry-author">
            <img :src="entry.authorAvatar" class="entry-av">
            --{{ entry.authorNickname }}
          </div>
        </div>
      </div>

      <!-- Rate partner -->
      <div class="rate-row">
        <span class="rate-label">If it's convenient, please rate this partner ❤️</span>
        <div class="rate-emojis">
          <span class="rate-emoji" @click="rate('good')">😊</span>
          <span class="rate-emoji" @click="rate('neutral')">😐</span>
          <span class="rate-emoji" @click="rate('bad')">😢</span>
        </div>
      </div>
    </div>

    <!-- Info modal -->
    <el-dialog v-model="modalVisible" :title="modalTitle" width="380px" align-center>
      <div class="modal-rows">
        <div v-for="row in modalRows" :key="row.label" class="modal-row">
          <span class="mr-label">{{ row.label }}</span>
          <span class="mr-value">{{ row.value }}</span>
        </div>
      </div>
    </el-dialog>
  </div>

  <div v-else-if="loading" class="jd-loading">
    <el-skeleton :rows="6" animated />
  </div>

  <div v-else class="jr-empty">
    <span style="font-size:40px">📒</span>
    <p>Journal not found</p>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useActivityStore } from '@/stores/activity'
import { ratePartner } from '@/api/user'
import { formatDate } from '@/utils/format'
import type { Diary } from '@/types/diary'

const route    = useRoute()
const actStore = useActivityStore()

const diary   = ref<Diary | null>(null)
const loading = ref(false)

const modalVisible = ref(false)
const modalTitle   = ref('')
const modalRows    = ref<{ label: string; value: string }[]>([])

const loadDiary = async (id: number) => {
  loading.value = true
  try { diary.value = await actStore.fetchDiaryDetail(id) }
  finally { loading.value = false }
}

const openModal = (type: 'participants' | 'location' | 'lasttime') => {
  if (!diary.value) return
  if (type === 'participants') {
    modalTitle.value = 'Participants'
    modalRows.value  = diary.value.participants.map(p => ({ label: p.nickname, value: p.avatar ? '' : '' }))
  } else if (type === 'location') {
    modalTitle.value = 'Location'
    modalRows.value  = [{ label: 'Location', value: diary.value.location }]
  } else {
    modalTitle.value = 'Last time'
    modalRows.value  = [
      { label: 'End Time', value: formatDate(diary.value.endTime) },
    ]
  }
  modalVisible.value = true
}

const rate = async (r: 'good' | 'neutral' | 'bad') => {
  if (!diary.value) return
  try {
    // ✅ 改为 scoreLevel (1-3)
    const scoreMap = { good: 3, neutral: 2, bad: 1 }
    const score = scoreMap[r] as 1 | 2 | 3
    
    for (const p of diary.value.participants) {
      await ratePartner(p.userId, score)
    }
    ElMessage.success('Rating submitted!')
  } catch { /* handled */ }
}

onMounted(() => {
  const id = Number(route.params.id)
  if (id) loadDiary(id)
})

watch(() => route.params.id, (id) => {
  if (id) loadDiary(Number(id))
})
</script>

<style scoped>
.journal-detail { display: flex; flex-direction: column; height: 100%; overflow: hidden; }

.jd-cover { width: 100%; height: 300px; overflow: hidden; flex-shrink: 0; background: #222; }
.jd-cover img { width: 100%; height: 100%; object-fit: cover; }

.jd-content {
  flex: 1; overflow-y: auto; padding: 22px 26px;
  display: flex; flex-direction: column; gap: 18px;
}

.jd-title { font-family: var(--font-display); font-size: 22px; }

.jd-info-btns { display: flex; gap: 10px; flex-wrap: wrap; }
.jr-btn {
  padding: 7px 16px; border-radius: var(--radius-full);
  background: var(--color-surface-2); border: 1px solid var(--color-border);
  color: var(--color-white); font-size: 13px; cursor: pointer;
  transition: border-color .15s, color .15s;
}
.jr-btn:hover { border-color: var(--color-green-border); color: var(--color-green); }

.jd-entries { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.jd-entry {
  background: var(--color-surface-1); border-radius: var(--radius-md);
  padding: 14px; font-size: 13px; line-height: 1.65;
  color: rgba(255,255,255,.85); border: 0.5px solid var(--color-border-dim);
}
.entry-author {
  display: flex; align-items: center; gap: 7px;
  font-size: 12px; color: var(--color-text-secondary);
  margin-top: 10px; justify-content: flex-end;
}
.entry-av { width: 20px; height: 20px; border-radius: 50%; object-fit: cover; }

.rate-row  { display: flex; align-items: center; gap: 14px; flex-wrap: wrap; }
.rate-label{ font-size: 13px; color: rgba(255,255,255,.7); }
.rate-emojis { display: flex; gap: 10px; }
.rate-emoji {
  font-size: 26px; cursor: pointer;
  transition: transform .15s;
}
.rate-emoji:hover { transform: scale(1.35); }

/* modal */
.modal-rows { display: flex; flex-direction: column; gap: 10px; }
.modal-row  { display: flex; gap: 12px; padding: 8px 0; border-bottom: 0.5px solid var(--color-border-dim); }
.mr-label   { color: var(--color-text-secondary); min-width: 90px; font-size: 13px; }
.mr-value   { font-size: 13px; }

.jd-loading { padding: 24px; flex: 1; }
.jr-empty {
  flex: 1; display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  gap: 12px; opacity: .4; font-size: 14px;
}
</style>
