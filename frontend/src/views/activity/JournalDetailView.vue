<template>
  <div class="journal-detail" v-if="diary">
    <div class="jd-cover">
      <img :src="coverImage" :alt="diary.title || 'Shared Journal Cover'">
      <div class="jd-cover-overlay">
        <h2 class="jd-title">{{ diary.title || 'Shared Journal' }}</h2>
        <div class="jd-meta-tags">
          <span class="m-tag">{{ diary.location || 'Unknown location' }}</span>
          <span class="m-tag">{{ formatDate(diary.createTime) }}</span>
        </div>
      </div>
    </div>

    <div class="jd-content">
      <div class="ai-summary-card" :class="{ 'is-loading': aiLoading }">
        <div class="ai-header">
          <span class="ai-title">AI Summary</span>
          <el-button size="small" type="primary" round :loading="aiLoading" @click="generateAiSummary">
            {{ aiSummary ? 'Regenerate' : 'Generate Summary' }}
          </el-button>
        </div>
        <div class="ai-body">
          <p v-if="displayText" class="ai-text typewriter">{{ displayText }}</p>
          <p v-else-if="aiLoading" class="ai-placeholder">Summarizing this shared moment...</p>
          <p v-else class="ai-placeholder">Generate an AI summary for this shared journal.</p>
        </div>
      </div>

      <div class="section-header">
        <span class="section-title">Shared Journal</span>
        <span class="section-desc">Each participant keeps one card. You can only edit your own.</span>
      </div>

      <div class="shared-grid">
        <article
          v-for="item in sharedEntries"
          :key="item.user.id"
          class="shared-card"
          :class="{ mine: isMine(item.user.id) }"
        >
          <div class="shared-card-head">
            <div class="shared-user">
              <img
                :src="item.user.avatar || fallbackAvatar(item.user.id)"
                :alt="item.user.nickname"
                class="shared-avatar"
              >
              <div class="shared-meta">
                <strong>{{ item.user.nickname }}</strong>
                <span>{{ isMine(item.user.id) ? 'Your card' : 'Read only' }}</span>
              </div>
            </div>
            <span class="shared-updated">{{ formatDate(item.entry.updateTime || item.entry.createTime) }}</span>
          </div>

          <template v-if="isMine(item.user.id)">
            <textarea
              v-model="entryDrafts[item.user.id].content"
              class="shared-editor"
              placeholder="Write what this activity felt like..."
            />
            <div class="shared-images" v-if="parseImages(entryDrafts[item.user.id].images).length > 0">
              <img
                v-for="(image, index) in parseImages(entryDrafts[item.user.id].images)"
                :key="index"
                :src="image"
                class="shared-image"
              >
            </div>
            <label class="shared-upload">
              <span>Add images</span>
              <input type="file" accept="image/*" multiple hidden @change="(event) => onEntryFileChange(event, item.user.id)">
            </label>
            <button class="save-btn" type="button" :disabled="savingUserId === item.user.id" @click="saveMyEntry(item.user.id)">
              {{ savingUserId === item.user.id ? 'Saving...' : 'Save My Card' }}
            </button>
          </template>

          <template v-else>
            <p class="shared-text">{{ item.entry.content || 'No journal entry yet.' }}</p>
            <div class="shared-images" v-if="parseImages(item.entry.images).length > 0">
              <img
                v-for="(image, index) in parseImages(item.entry.images)"
                :key="index"
                :src="image"
                class="shared-image"
              >
            </div>
          </template>
        </article>
      </div>
    </div>
  </div>

  <div v-else class="jd-loading">
    <el-skeleton :rows="10" animated />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getActivityAiSummary, getDiaryDetail, updateMySharedDiaryEntry, uploadImage } from '@/api/activity'
import { useUserStore } from '@/stores/user'
import { formatDate } from '@/utils/format'
import type { SharedDiaryEntryPayload } from '@/types/diary'

const route = useRoute()
const userStore = useUserStore()

const diary = ref<any>(null)
const sharedEntries = ref<SharedDiaryEntryPayload[]>([])
const entryDrafts = ref<Record<number, { content: string; images: string[] | string }>>({})
const savingUserId = ref<number | null>(null)

const aiLoading = ref(false)
const aiSummary = ref('')
const displayText = ref('')

const coverImage = computed(() => {
  const images = parseImages(diary.value?.images)
  return images[0] || 'https://images.unsplash.com/photo-1517486808906-6ca8b3f04846?q=80&w=800'
})

const parseImages = (images: any): string[] => {
  if (!images) return []
  if (Array.isArray(images)) return images.filter(Boolean)
  if (typeof images === 'string') {
    try {
      const parsed = JSON.parse(images)
      return Array.isArray(parsed) ? parsed.filter(Boolean) : [images]
    } catch {
      return images.split(',').filter(Boolean)
    }
  }
  return []
}

const fallbackAvatar = (seed: number) => `https://api.dicebear.com/7.x/avataaars/svg?seed=${seed}`

const isMine = (userId: number) => userStore.userInfo?.id === userId

const typewriter = (text: string) => {
  displayText.value = ''
  let index = 0
  const timer = setInterval(() => {
    if (index < text.length) {
      displayText.value += text.charAt(index)
      index += 1
    } else {
      clearInterval(timer)
    }
  }, 30)
}

const generateAiSummary = async () => {
  const activityId = diary.value?.activityId || Number(route.params.id)
  aiLoading.value = true
  try {
    const response = await getActivityAiSummary(activityId) as any
    aiSummary.value = typeof response === 'string' ? response : (response.data || 'Failed to generate summary')
    typewriter(aiSummary.value)
  } catch {
    ElMessage.error('AI summary failed.')
  } finally {
    aiLoading.value = false
  }
}

const hydrateEntryDrafts = () => {
  const drafts: Record<number, { content: string; images: string[] | string }> = {}
  sharedEntries.value.forEach((item) => {
    drafts[item.user.id] = {
      content: item.entry?.content || '',
      images: parseImages(item.entry?.images),
    }
  })
  entryDrafts.value = drafts
}

const loadDiary = async () => {
  const id = Number(route.params.id)
  if (!id) return
  const result = await getDiaryDetail(id) as any
  diary.value = result.diary
  sharedEntries.value = Array.isArray(result.sharedEntries) ? result.sharedEntries : []
  hydrateEntryDrafts()
}

const onEntryFileChange = async (event: Event, userId: number) => {
  const input = event.target as HTMLInputElement
  const files = Array.from(input.files ?? [])
  if (files.length === 0) return

  try {
    const uploaded = await Promise.all(files.map((file) => uploadImage(file)))
    const current = parseImages(entryDrafts.value[userId]?.images)
    entryDrafts.value[userId].images = [...current, ...uploaded.map((item) => item.url)]
  } catch {
    ElMessage.error('Image upload failed.')
  } finally {
    input.value = ''
  }
}

const saveMyEntry = async (userId: number) => {
  const diaryId = Number(route.params.id)
  if (!diaryId || !entryDrafts.value[userId]) return

  savingUserId.value = userId
  try {
    await updateMySharedDiaryEntry(diaryId, {
      content: entryDrafts.value[userId].content,
      images: parseImages(entryDrafts.value[userId].images),
    })
    await loadDiary()
    ElMessage.success('Your shared journal card was updated.')
  } catch {
    ElMessage.error('Failed to update your shared card.')
  } finally {
    savingUserId.value = null
  }
}

onMounted(loadDiary)
</script>

<style scoped>
.journal-detail { height: 100%; overflow-y: auto; background: var(--color-bg); color: #eee; }
.jd-cover { height: 280px; position: relative; overflow: hidden; }
.jd-cover img { width: 100%; height: 100%; object-fit: cover; }
.jd-cover-overlay {
  position: absolute; bottom: 0; left: 0; right: 0; padding: 40px 24px 20px;
  background: linear-gradient(transparent, rgba(0,0,0,0.92));
}
.jd-title { font-size: 30px; font-weight: 700; margin: 0; color: #fff; }
.jd-meta-tags { display: flex; gap: 15px; margin-top: 10px; opacity: 0.85; flex-wrap: wrap; }
.m-tag { font-size: 13px; color: rgba(255,255,255,0.82); }

.jd-content { padding: 24px; display: flex; flex-direction: column; gap: 24px; }

.ai-summary-card {
  background: rgba(0, 255, 149, 0.05);
  border: 1px solid rgba(0, 255, 149, 0.2);
  border-radius: 16px;
  padding: 20px;
}
.ai-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; gap: 12px; }
.ai-title { font-weight: 700; color: var(--color-green); font-size: 15px; }
.ai-text { font-size: 14px; line-height: 1.8; color: #ddd; white-space: pre-wrap; }
.ai-placeholder { color: #666; font-size: 13px; font-style: italic; }

.section-header { border-left: 4px solid var(--color-green); padding-left: 12px; }
.section-title { font-size: 16px; font-weight: 700; display: block; }
.section-desc { font-size: 11px; color: #888; margin-top: 4px; display: block; }

.shared-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.shared-card {
  background: var(--color-surface-1);
  border: 1px solid var(--color-border);
  border-radius: 18px;
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.shared-card.mine {
  border-color: rgba(0, 255, 149, 0.26);
  box-shadow: 0 10px 24px rgba(0, 255, 149, 0.08);
}

.shared-card-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.shared-user {
  display: flex;
  align-items: center;
  gap: 12px;
}

.shared-avatar {
  width: 46px;
  height: 46px;
  border-radius: 50%;
  object-fit: cover;
}

.shared-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.shared-meta strong {
  font-size: 15px;
}

.shared-meta span,
.shared-updated {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.shared-editor {
  min-height: 140px;
  padding: 14px;
  border-radius: 14px;
  border: 1px solid var(--color-border);
  background: rgba(0,0,0,0.18);
  color: #e8e8e8;
  resize: vertical;
  line-height: 1.6;
}

.shared-text {
  margin: 0;
  min-height: 140px;
  padding: 14px;
  border-radius: 14px;
  background: rgba(0,0,0,0.18);
  color: #d0d0d0;
  line-height: 1.7;
  white-space: pre-wrap;
}

.shared-images {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(110px, 1fr));
  gap: 10px;
}

.shared-image {
  width: 100%;
  height: 110px;
  object-fit: cover;
  border-radius: 10px;
}

.shared-upload {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 40px;
  padding: 0 14px;
  border-radius: 12px;
  border: 1px dashed var(--color-border);
  color: var(--color-text-secondary);
  cursor: pointer;
}

.save-btn {
  height: 40px;
  border: none;
  border-radius: 12px;
  background: var(--color-green);
  color: #111;
  font-weight: 700;
  cursor: pointer;
}

.save-btn:disabled {
  opacity: 0.6;
  cursor: default;
}

.jd-loading { padding: 40px; }

.typewriter {
  border-right: 2px solid var(--color-green);
  animation: blink 0.7s infinite;
}

@keyframes blink { 50% { border-color: transparent; } }
</style>
