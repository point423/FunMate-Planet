<template>
  <div class="journal-view">
    <div class="journal-left">
      <div class="journal-grid">
        <div
          v-for="journal in diaries"
          :key="journal.id"
          class="journal-card"
          :class="{ active: activeId === journal.id }"
          @click="selectJournal(journal.id)"
        >
          <div class="jc-img">
            <img :src="journal.coverImage || coverFromImages(journal.images)" :alt="journal.title">
          </div>
          <div class="jc-body">
            <div class="jc-title">{{ journal.title || journal.content || 'Untitled Journal' }}</div>
            <div class="jc-footer">
              <div class="jc-avs">
                <div v-for="participant in (journal.participants || []).slice(0, 3)" :key="participant.userId" class="jc-av">
                  <img :src="participant.avatar" :alt="participant.nickname">
                </div>
                <span v-if="journal.participants?.length > 3" class="more-count">
                  +{{ journal.participants.length - 3 }}
                </span>
              </div>
              <span class="jc-date" :class="{ green: activeId === journal.id }">
                {{ formatDate(journal.createdAt || journal.createTime) }}
              </span>
            </div>
          </div>
        </div>

        <div class="journal-card new-card" @click="showEditor = true">
          <span class="new-plus">+</span>
          <span class="new-label">new journal</span>
        </div>
      </div>
    </div>

    <div class="journal-right">
      <div v-if="activeDiary" class="journal-detail">
        <div class="jd-cover">
          <img :src="detailCoverImage" :alt="activeDiary.title || 'Shared Journal Cover'">
          <div class="jd-cover-overlay">
            <h2 class="jd-title">{{ activeDiary.title || activeDiary.content || 'Shared Journal' }}</h2>
            <div class="jd-meta-tags">
              <span class="m-tag">{{ activeDiary.location || 'Unknown location' }}</span>
              <span class="m-tag">{{ formatDate(activeDiary.createdAt || activeDiary.createTime) }}</span>
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
                  <el-image
                    v-for="(image, index) in parseImages(entryDrafts[item.user.id].images)"
                    :key="index"
                    :src="image"
                    class="shared-image"
                    :preview-src-list="parseImages(entryDrafts[item.user.id].images)"
                  />
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
                  <el-image
                    v-for="(image, index) in parseImages(item.entry.images)"
                    :key="index"
                    :src="image"
                    class="shared-image"
                    :preview-src-list="parseImages(item.entry.images)"
                  />
                </div>
              </template>
            </article>
          </div>
        </div>

        <div class="jd-footer">
          <el-popconfirm
            v-if="canDeleteDiary"
            title="Delete this journal? This cannot be undone."
            confirm-button-text="Delete"
            cancel-button-text="Cancel"
            confirm-button-type="danger"
            @confirm="handleDelete"
          >
            <template #reference>
              <el-button type="danger" plain :icon="Delete">Delete Journal</el-button>
            </template>
          </el-popconfirm>
        </div>
      </div>

      <div v-else class="jr-empty">
        <span class="empty-icon">Journal</span>
        <p>Select a journal to view</p>
      </div>
    </div>

    <DiaryEditor v-if="showEditor" v-model="showEditor" @saved="onSaved" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import DiaryEditor from '@/components/activity/DiaryEditor.vue'
import { getActivityAiSummary, updateMySharedDiaryEntry, uploadImage } from '@/api/activity'
import { useActivityStore } from '@/stores/activity'
import { useUserStore } from '@/stores/user'
import { formatDate } from '@/utils/format'
import type { SharedDiaryEntryPayload } from '@/types/diary'

const route = useRoute()
const router = useRouter()
const actStore = useActivityStore()
const userStore = useUserStore()

const activeId = ref<number | null>(null)
const showEditor = ref(false)
const aiLoading = ref(false)
const aiSummary = ref('')
const displayText = ref('')
const sharedEntries = ref<SharedDiaryEntryPayload[]>([])
const entryDrafts = ref<Record<number, { content: string; images: string[] | string }>>({})
const savingUserId = ref<number | null>(null)
const injectedDiary = ref<any | null>(null)
const deletingDiaryId = ref<number | null>(null)

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

const normalizeDiary = (raw: any) => {
  if (!raw) return null
  const images = parseImages(raw.images)
  return {
    ...raw,
    title: raw.title || (raw.content ? `${raw.content.slice(0, 20)}${raw.content.length > 20 ? '...' : ''}` : 'Untitled Journal'),
    images,
    coverImage: images[0] || '',
    participants: raw.participants || [],
    sharedEntries: raw.sharedEntries || [],
  }
}

const diaries = computed(() => {
  const diariesList = actStore.diaries
  const normalized = (!diariesList || !Array.isArray(diariesList) ? [] : diariesList)
    .map((item: any) => {
      const diary = item.diary || item
      const participants = item.participants || diary.participants || []
      return normalizeDiary({
        ...diary,
        participants,
        sharedEntries: diary.sharedEntries || [],
      })
    })
    .filter(Boolean)

  if (injectedDiary.value && !normalized.some((item: any) => item.id === injectedDiary.value.id)) {
    return [injectedDiary.value, ...normalized]
  }

  return normalized
})

const activeDiary = computed(() => actStore.activeDiary || diaries.value.find((diary: any) => diary.id === activeId.value) || null)
const canDeleteDiary = computed(() => Number(activeDiary.value?.userId) === Number(userStore.userInfo?.id))

const detailCoverImage = computed(() => {
  const images = parseImages(activeDiary.value?.images)
  return images[0] || 'https://images.unsplash.com/photo-1517486808906-6ca8b3f04846?q=80&w=800'
})

const coverFromImages = (images: any) =>
  parseImages(images)[0] || 'https://images.unsplash.com/photo-1517486808906-6ca8b3f04846?q=80&w=400'

const fallbackAvatar = (seed: number) => `https://api.dicebear.com/7.x/avataaars/svg?seed=${seed}`

const isMine = (userId: number) => userStore.userInfo?.id === userId

const hydrateSharedEntries = (diary: any) => {
  const entries = Array.isArray(diary?.sharedEntries) ? diary.sharedEntries : []
  sharedEntries.value = entries

  const drafts: Record<number, { content: string; images: string[] | string }> = {}
  entries.forEach((item: SharedDiaryEntryPayload) => {
    drafts[item.user.id] = {
      content: item.entry?.content || '',
      images: parseImages(item.entry?.images),
    }
  })
  entryDrafts.value = drafts
}

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
  if (!activeDiary.value) return
  const id = activeDiary.value.activityId || activeId.value
  aiLoading.value = true
  try {
    const response = await getActivityAiSummary(id) as any
    aiSummary.value = typeof response === 'string' ? response : (response.data || 'Failed to generate summary')
    typewriter(aiSummary.value)
  } catch {
    ElMessage.error('AI summary failed.')
  } finally {
    aiLoading.value = false
  }
}

const selectJournal = async (id: number) => {
  if (deletingDiaryId.value === id) return

  activeId.value = id
  try {
    const detail = await actStore.fetchDiaryDetail(id)
    const normalized = normalizeDiary(detail)
    if (normalized) {
      injectedDiary.value = normalized
      hydrateSharedEntries(normalized)
    }
    router.replace({ name: 'Journal', query: { diaryId: String(id) } })
  } catch {
    if (deletingDiaryId.value === id) return
    activeId.value = null
    injectedDiary.value = null
    sharedEntries.value = []
    entryDrafts.value = {}
    router.replace({ name: 'Journal', query: {} })
  }
}

const ensureInitialSelection = async () => {
  if (deletingDiaryId.value) return

  const requestedId = Number(route.query.diaryId || route.params.id)
  if (requestedId) {
    if (!diaries.value.some((diary: any) => diary.id === requestedId) && injectedDiary.value?.id !== requestedId) {
      router.replace({ name: 'Journal', query: {} })
    } else {
      await selectJournal(requestedId)
      return
    }
  }

  if (diaries.value.length > 0) {
    await selectJournal(diaries.value[0].id)
    return
  }

  activeId.value = null
  injectedDiary.value = null
  sharedEntries.value = []
  entryDrafts.value = {}
}

const onSaved = async () => {
  await actStore.fetchDiaries()
  const newestDiary = diaries.value[0]
  if (newestDiary?.id) {
    await selectJournal(newestDiary.id)
    return
  }
  await ensureInitialSelection()
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
  if (!activeId.value || !entryDrafts.value[userId]) return

  savingUserId.value = userId
  try {
    await updateMySharedDiaryEntry(activeId.value, {
      content: entryDrafts.value[userId].content,
      images: parseImages(entryDrafts.value[userId].images),
    })
    const detail = await actStore.fetchDiaryDetail(activeId.value)
    const normalized = normalizeDiary(detail)
    if (normalized) {
      injectedDiary.value = normalized
      hydrateSharedEntries(normalized)
    }
    await actStore.fetchDiaries()
    ElMessage.success('Your shared journal card was updated.')
  } catch {
    ElMessage.error('Failed to update your shared card.')
  } finally {
    savingUserId.value = null
  }
}

const handleDelete = async () => {
  if (!activeDiary.value) return
  const deletedId = activeDiary.value.id

  try {
    deletingDiaryId.value = deletedId
    await actStore.deleteDiary(deletedId)
    ElMessage.success('Journal deleted.')

    activeId.value = null
    injectedDiary.value = null
    sharedEntries.value = []
    entryDrafts.value = {}
    router.replace({ name: 'Journal', query: {} })

    await actStore.fetchDiaries()
    await ensureInitialSelection()
  } catch {
    ElMessage.error('Delete failed, please try again later.')
  } finally {
    deletingDiaryId.value = null
  }
}

watch(
  () => diaries.value,
  async () => {
    if (deletingDiaryId.value) return

    if (diaries.value.length === 0) {
      activeId.value = null
      return
    }

    if (activeId.value && diaries.value.some((diary: any) => diary.id === activeId.value)) {
      return
    }

    await ensureInitialSelection()
  },
  { deep: true },
)

onMounted(async () => {
  await actStore.fetchDiaries()
  await ensureInitialSelection()
})
</script>

<style scoped>
.journal-view { display: flex; flex: 1; overflow: hidden; height: 100%; }

.journal-left {
  width: 420px;
  flex-shrink: 0;
  background: rgba(255,255,255,.04);
  border-right: 0.5px solid var(--color-border-dim);
  overflow-y: auto;
  padding: 22px 18px;
}

.journal-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }

.journal-card {
  border-radius: var(--radius-lg);
  overflow: hidden;
  background: var(--color-card-solid);
  border: 0.5px solid var(--color-border-dim);
  cursor: pointer;
  transition: transform .15s, border-color .15s;
}

.journal-card:hover { transform: translateY(-3px); border-color: rgba(255,255,255,.22); }
.journal-card.active { border-color: var(--color-green); }

.jc-img { width: 100%; height: 130px; overflow: hidden; background: #333; }
.jc-img img { width: 100%; height: 100%; object-fit: cover; }

.jc-body { padding: 10px 12px; }
.jc-title { font-size: 14px; font-weight: 500; margin-bottom: 8px; color: var(--color-text); }
.jc-footer { display: flex; align-items: center; justify-content: space-between; }
.jc-avs { display: flex; }
.jc-av {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  overflow: hidden;
  background: #555;
  border: 1.5px solid #000;
  margin-left: -5px;
}
.jc-av:first-child { margin-left: 0; }
.jc-av img { width: 100%; height: 100%; object-fit: cover; }
.jc-date { font-size: 11px; color: var(--color-text-secondary); }
.jc-date.green { color: var(--color-green); }

.new-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 160px;
  border: 1.5px dashed var(--color-border) !important;
  background: transparent !important;
  color: var(--color-text-secondary);
  gap: 8px;
}

.new-plus { font-size: 28px; }
.new-label { font-size: 12px; font-family: monospace; }

.journal-right { flex: 1; display: flex; flex-direction: column; overflow: hidden; }

.jr-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  opacity: .4;
  font-size: 14px;
}

.empty-icon { font-size: 42px; font-family: var(--font-display); }

.journal-detail {
  height: 100%;
  overflow-y: auto;
  background: var(--color-bg);
  color: #eee;
  display: flex;
  flex-direction: column;
}

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

.shared-user { display: flex; align-items: center; gap: 12px; }
.shared-avatar { width: 46px; height: 46px; border-radius: 50%; object-fit: cover; }
.shared-meta { display: flex; flex-direction: column; gap: 4px; }
.shared-meta strong { font-size: 15px; }
.shared-meta span,
.shared-updated { font-size: 12px; color: var(--color-text-secondary); }

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

.save-btn:disabled { opacity: 0.6; cursor: default; }

.jd-footer {
  padding: 0 24px 24px;
}

.typewriter {
  border-right: 2px solid var(--color-green);
  animation: blink 0.7s infinite;
}

@keyframes blink { 50% { border-color: transparent; } }
</style>
