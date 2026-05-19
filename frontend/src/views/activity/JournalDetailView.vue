<template>
  <div class="journal-detail" v-if="diary">
    <!-- 顶部封面 -->
    <div class="jd-cover">
      <img :src="diary.coverImage || 'https://images.unsplash.com/photo-1517486808906-6ca8b3f04846?q=80&w=800'" :alt="diary.title">
      <div class="jd-cover-overlay">
        <h2 class="jd-title">{{ diary.title || 'Activity Review' }}</h2>
        <div class="jd-meta-tags">
          <span class="m-tag">📍 {{ diary.location }}</span>
          <span class="m-tag">📅 {{ formatDate(diary.createTime) }}</span>
        </div>
      </div>
    </div>

    <div class="jd-content">
      <!-- AI 建议板块 (核心闭环) -->
      <div class="ai-summary-card" :class="{ 'is-loading': aiLoading }">
        <div class="ai-header">
          <span class="ai-icon">✨</span>
          <span class="ai-title">AI Planet Observer</span>
          <button class="ai-gen-btn" @click="generateAiSummary" :disabled="aiLoading">
            {{ aiSummary ? 'Regenerate' : 'Generate Summary' }}
          </button>
        </div>
        <div class="ai-body">
          <p v-if="aiSummary" class="ai-text">{{ aiSummary }}</p>
          <p v-else-if="aiLoading" class="ai-placeholder">AI is analyzing your social frequency...</p>
          <p v-else class="ai-placeholder">Click to get AI social suggestions for this activity.</p>
        </div>
      </div>

      <!-- 参与者评价板块 -->
      <div class="section-title">Rate your partners</div>
      <div class="participants-list">
        <div v-for="p in diary.participants" :key="p.userId" class="p-item">
          <img :src="p.avatar" class="p-ava">
          <div class="p-info">
            <div class="p-name">{{ p.nickname }}</div>
            <div class="p-rate">
               <span v-for="s in 3" :key="s" class="s-emoji" @click="submitRate(p.userId, s)">
                 {{ s === 3 ? '😊' : s === 2 ? '😐' : '😢' }}
               </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 活动日记内容 -->
      <div class="section-title">Activity Moments</div>
      <div class="jd-entries">
        <div v-for="entry in diary.entries" :key="entry.id" class="jd-entry">
          <p>{{ entry.content }}</p>
          <div class="entry-meta">-- {{ entry.authorNickname }}</div>
        </div>
        <div v-if="!diary.entries?.length" class="no-entries">No diary entries yet.</div>
      </div>
    </div>
  </div>

  <div v-else-if="loading" class="jd-loading">
    <el-skeleton :rows="10" animated />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useActivityStore } from '@/stores/activity'
import { getActivitySummary } from '@/api/ai'
import { ratePartner } from '@/api/user'
import { formatDate } from '@/utils/format'

const route = useRoute()
const actStore = useActivityStore()

const diary = ref<any>(null)
const loading = ref(false)
const aiLoading = ref(false)
const aiSummary = ref('')

const loadDiary = async (id: number) => {
  loading.value = true
  try {
    const res = await actStore.fetchDiaryDetail(id)
    diary.value = res
  } finally {
    loading.value = false
  }
}

const generateAiSummary = async () => {
  if (!diary.value) return
  aiLoading.value = true
  try {
    const participantsNames = diary.value.participants.map(p => p.nickname).join(', ')
    const res = await getActivitySummary({
      title: diary.value.title || 'Coding activity',
      participants: participantsNames,
      reviews: "Participants were very active and helpful."
    })
    aiSummary.value = res.summary
  } catch (e) {
    ElMessage.error('AI is resting, please try again later.')
  } finally {
    aiLoading.value = false
  }
}

const submitRate = async (targetUserId: number, score: number) => {
  try {
    await ratePartner(targetUserId, score)
    ElMessage.success('Rating recorded! Their ranking will be updated.')
  } catch (e) {
    ElMessage.error('Failed to submit rating.')
  }
}

onMounted(() => {
  const id = Number(route.params.id)
  if (id) loadDiary(id)
})
</script>

<style scoped>
.journal-detail { height: 100%; overflow-y: auto; background: var(--color-bg); }

.jd-cover { height: 260px; position: relative; overflow: hidden; }
.jd-cover img { width: 100%; height: 100%; object-fit: cover; }
.jd-cover-overlay {
  position: absolute; bottom: 0; left: 0; right: 0;
  padding: 40px 24px 20px;
  background: linear-gradient(transparent, rgba(0,0,0,0.8));
}
.jd-title { font-size: 28px; font-weight: bold; color: white; margin: 0; }
.jd-meta-tags { display: flex; gap: 12px; margin-top: 10px; }
.m-tag { font-size: 13px; color: rgba(255,255,255,0.8); }

.jd-content { padding: 24px; display: flex; flex-direction: column; gap: 24px; }

/* AI Card */
.ai-summary-card {
  background: linear-gradient(135deg, rgba(0, 255, 149, 0.1), rgba(0, 149, 255, 0.1));
  border: 1px solid rgba(0, 255, 149, 0.3);
  border-radius: var(--radius-lg); padding: 20px;
}
.ai-header { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.ai-icon { font-size: 20px; }
.ai-title { font-weight: bold; flex: 1; color: var(--color-green); }
.ai-gen-btn {
  background: var(--color-green); color: black; border: none;
  padding: 4px 12px; border-radius: 4px; font-size: 12px; font-weight: bold; cursor: pointer;
}
.ai-text { font-size: 14px; line-height: 1.6; color: var(--color-white); }
.ai-placeholder { color: var(--color-text-hint); font-size: 13px; font-style: italic; }

.section-title { font-size: 16px; font-weight: bold; border-left: 4px solid var(--color-green); padding-left: 10px; }

.participants-list { display: flex; gap: 16px; overflow-x: auto; padding-bottom: 10px; }
.p-item {
  background: var(--color-surface-1); padding: 12px; border-radius: var(--radius-md);
  min-width: 140px; display: flex; flex-direction: column; align-items: center; gap: 8px;
}
.p-ava { width: 48px; height: 48px; border-radius: 50%; }
.p-name { font-size: 13px; font-weight: 500; }
.s-emoji { font-size: 20px; cursor: pointer; transition: transform 0.2s; margin: 0 4px; }
.s-emoji:hover { transform: scale(1.3); }

.jd-entries { display: flex; flex-direction: column; gap: 12px; }
.jd-entry {
  background: var(--color-surface-2); padding: 16px; border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
}
.entry-meta { text-align: right; font-size: 12px; color: var(--color-text-hint); margin-top: 8px; }

.no-entries { text-align: center; color: var(--color-text-hint); padding: 20px; }
</style>
