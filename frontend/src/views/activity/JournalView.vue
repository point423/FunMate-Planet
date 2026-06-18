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
          <img :src="resolveDiaryCoverForCurrentUser(journal)" :alt="journal.title">
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
          <div class="jd-content-hero">
            <div v-if="myUploadedCoverImage" class="jd-content-cover">
              <img :src="myUploadedCoverImage" :alt="activeDiary.title || 'My Journal Cover'">
            </div>
            <h3 class="jd-content-title">
              {{ activeDiary.title || activeDiary.content || 'Shared Journal' }}
            </h3>
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
                  <div
                    v-for="(image, index) in parseImages(entryDrafts[item.user.id].images)"
                    :key="`${item.user.id}-${index}`"
                    class="shared-image-item"
                  >
                    <el-image
                      :src="image"
                      class="shared-image"
                      :preview-src-list="parseImages(entryDrafts[item.user.id].images)"
                      :preview-teleported="true"
                    />
                    <button
                      type="button"
                      class="shared-image-remove"
                      aria-label="Remove image"
                      @click.stop="removeDraftImage(item.user.id, index)"
                    >
                      ×
                    </button>
                  </div>
                </div>
                <label class="shared-upload">
                  <span>Add images</span>
                  <input type="file" accept="image/*" multiple hidden @change="(event) => onEntryFileChange(event, item.user.id)">
                </label>
                <div class="mine-card-actions">
                  <button class="mine-card-btn primary" type="button" :disabled="savingUserId === item.user.id" @click="saveMyEntry(item.user.id)">
                    {{ savingUserId === item.user.id ? 'Saving...' : 'Save My Card' }}
                  </button>
                  <button class="mine-card-btn secondary" type="button" :disabled="sharingUserId === item.user.id" @click="shareMyEntry(item.user.id)">
                    {{ sharingUserId === item.user.id ? 'Sharing...' : 'Share to Profile' }}
                  </button>
                </div>
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
                    :preview-teleported="true"
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
import { getDiaryDetail, shareMySharedDiaryEntry, updateMySharedDiaryEntry, uploadImage } from '@/api/activity'
import { useActivityStore } from '@/stores/activity'
import { useUserStore } from '@/stores/user'
import { formatDate } from '@/utils/format'
import type { SharedDiaryEntryPayload } from '@/types/diary'
import {
  detailCoverImageFromDiary,
  fallbackAvatar,
  getMyUploadedCoverFromDiary,
  isDiaryOwner,
  mergeDiaryList,
  normalizeDiaryDetailPayload,
  parseImages,
  resolveDiaryCover,
} from './journal-helpers'

const route = useRoute()
const router = useRouter()
const actStore = useActivityStore()
const userStore = useUserStore()

const activeId = ref<number | null>(null)
const showEditor = ref(false)
const sharedEntries = ref<SharedDiaryEntryPayload[]>([])
const entryDrafts = ref<Record<number, { content: string; images: string[] | string }>>({})
const savingUserId = ref<number | null>(null)
const sharingUserId = ref<number | null>(null)
const injectedDiary = ref<any | null>(null)
const deletingDiaryId = ref<number | null>(null)
const diaryDetailCache = ref<Record<number, any>>({})

const cacheDiaryDetail = (raw: any) => {
  const normalized = normalizeDiaryDetailPayload(raw)
  if (normalized?.id) {
    diaryDetailCache.value = {
      ...diaryDetailCache.value,
      [normalized.id]: normalized,
    }
  }
  return normalized
}

const diaries = computed(() => mergeDiaryList(actStore.diaries, diaryDetailCache.value, injectedDiary.value))

const activeDiary = computed(() => actStore.activeDiary || diaries.value.find((diary: any) => diary.id === activeId.value) || null)
const canDeleteDiary = computed(() => isDiaryOwner(activeDiary.value?.userId, userStore.userInfo?.id))

const detailCoverImage = computed(() => detailCoverImageFromDiary(activeDiary.value))

const myUploadedCoverImage = computed(() =>
  getMyUploadedCoverFromDiary(activeDiary.value, Number(userStore.userInfo?.id ?? 0), entryDrafts.value, true) || '',
)

const isMine = (userId: number) => userStore.userInfo?.id === userId

const resolveDiaryCoverForCurrentUser = (diary: any) =>
  resolveDiaryCover(diary, Number(userStore.userInfo?.id ?? 0), entryDrafts.value)

const preloadDiaryCovers = async () => {
  const diaryIds = diaries.value
    .map((diary: any) => Number(diary.id))
    .filter(Boolean)

  const missingIds = diaryIds.filter((id) => !diaryDetailCache.value[id])
  if (missingIds.length === 0) return

  const detailResults = await Promise.allSettled(missingIds.map((id) => getDiaryDetail(id)))
  detailResults.forEach((result) => {
    if (result.status === 'fulfilled') {
      cacheDiaryDetail(result.value)
    }
  })
}

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

const selectJournal = async (id: number) => {
  if (deletingDiaryId.value === id) return

  activeId.value = id
  try {
    const detail = await actStore.fetchDiaryDetail(id)
    const normalized = cacheDiaryDetail(detail)
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
  await preloadDiaryCovers()
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

const removeDraftImage = (userId: number, imageIndex: number) => {
  const currentImages = parseImages(entryDrafts.value[userId]?.images)
  if (currentImages.length === 0) return

  entryDrafts.value[userId].images = currentImages.filter((_, index) => index !== imageIndex)
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
    const normalized = cacheDiaryDetail(detail)
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

const shareMyEntry = async (userId: number) => {
  if (!activeId.value) return

  sharingUserId.value = userId
  try {
    await shareMySharedDiaryEntry(activeId.value)
    ElMessage.success('Shared to your profile journal wall.')
  } catch {
    ElMessage.error('Failed to share your card.')
  } finally {
    sharingUserId.value = null
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
    diaryDetailCache.value = Object.fromEntries(
      Object.entries(diaryDetailCache.value).filter(([key]) => Number(key) !== deletedId),
    )
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
  await preloadDiaryCovers()
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

.jd-content-hero {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.jd-content-cover {
  border-radius: 20px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.03);
}

.jd-content-cover img {
  width: 100%;
  max-height: 320px;
  display: block;
  object-fit: cover;
}

.jd-content-title {
  margin: 0;
  font-size: 28px;
  line-height: 1.15;
  font-weight: 700;
  color: #f6f6f6;
}

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

.shared-image-item {
  position: relative;
}

.shared-image {
  width: 100%;
  height: 110px;
  object-fit: cover;
  border-radius: 10px;
}

.shared-image-remove {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.72);
  color: #fff;
  font-size: 18px;
  line-height: 1;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  z-index: 2;
  transition: transform 0.15s ease, background 0.15s ease;
}

.shared-image-remove:hover {
  transform: scale(1.05);
  background: rgba(255, 92, 92, 0.92);
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

.mine-card-actions {
  display: flex;
  gap: 10px;
}

.mine-card-btn {
  flex: 1;
  height: 40px;
  border-radius: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.15s, border-color 0.15s, background 0.15s;
}

.mine-card-btn:hover:not(:disabled) {
  transform: translateY(-1px);
}

.mine-card-btn.primary {
  border: none;
  background: var(--color-green);
  color: #111;
}

.mine-card-btn.secondary {
  border: 1px solid rgba(255, 255, 255, 0.16);
  background: rgba(255, 255, 255, 0.05);
  color: var(--color-text);
}

.mine-card-btn.secondary:hover:not(:disabled) {
  border-color: rgba(0, 255, 149, 0.34);
  background: rgba(0, 255, 149, 0.08);
}

.mine-card-btn:disabled { opacity: 0.6; cursor: default; }

.jd-footer {
  padding: 0 24px 24px;
}

:deep(.el-image-viewer__wrapper) {
  z-index: 4000 !important;
}

:deep(.el-image-viewer__btn) {
  z-index: 4001 !important;
}
</style>
