<template>
  <div class="profile-page">
    <div class="profile-inner">

      <!-- Header card -->
      <div class="profile-header glass-card">
        <div class="ph-ava-wrap">
          <img :src="user.avatar || placeholder" class="ph-ava" alt="avatar">
          <label class="ph-ava-edit" title="Change photo">
            ✏️
            <input type="file" accept="image/*" hidden @change="onAvatarChange">
          </label>
        </div>

        <div class="ph-info">
          <h2 class="ph-name">{{ user.nickname }}</h2>
          <p class="ph-bio">{{ user.bio }}</p>
          <div class="ph-tags">
            <span
                v-for="tag in displayTags"
                :key="tag.value"
              class="tag-chip active"
              ><span class="ph-tag-emoji">{{ tag.emoji }}</span>{{ tag.label }}</span>
          </div>
        </div>

        <div class="ph-stats">
          <div class="ph-stat">
            <div class="ph-stat-label">activities</div>
            <div class="ph-stat-num">{{ user.activities }}</div>
            <div class="ph-stat-sub">completed</div>
          </div>
          <div class="ph-stat">
            <div class="ph-stat-label">positive feedback</div>
            <div class="ph-stat-num" style="color:var(--color-text-gold)">⭐ {{ user.score }}</div>
          </div>
          <div class="ph-stat">
            <div class="ph-stat-label">ranking</div>
            <div class="ph-stat-num">{{ user.ranking }}</div>
          </div>
        </div>

        <button class="btn-edit" @click="openEditDialog">Edit</button>
      </div>

      <!-- Grid: journal + activities -->
      <div class="profile-grid">

        <!-- Journal wall -->
        <div class="profile-section glass-card">
          <h3 class="ps-title">📒 journal</h3>
          <div class="journal-wall">
            <div
              v-for="(j, i) in diaries"
              :key="j.id"
              class="jw-thumb"
              :class="{ tall: i === 0 }"
              @click="router.push({ path: '/activity/journal', query: { diaryId: String(j.id) } })"
            >
              <img :src="j.coverImage" :alt="j.title">
            </div>
            <div v-if="diaries.length === 0" class="ps-empty">No journals yet</div>
          </div>
        </div>

        <!-- Recent activities -->
        <div class="profile-section glass-card">
          <h3 class="ps-title">🔴 activities</h3>
          <div class="act-list">
            <div v-for="act in activities" :key="act.id" class="act-row">
              <span class="act-icon">{{ act.icon }}</span>
              <div class="act-row-info">
                <div class="act-row-name">{{ act.name }}</div>
                <div class="act-row-meta">
                  <div class="act-avs">
                    <img
                      v-for="av in act.participants.slice(0,3)"
                      :key="av"
                      :src="av"
                      class="act-av"
                    >
                  </div>
                </div>
              </div>
              <span class="act-date">{{ act.date }}</span>
            </div>
            <div v-if="activities.length === 0" class="ps-empty">No activities yet</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Edit profile dialog -->
    <el-dialog v-model="editVisible" title="Edit Profile" width="460px" align-center>
      <el-form :model="editForm" label-position="top">
        <el-form-item label="Nickname">
          <el-input v-model="editForm.nickname" />
        </el-form-item>
        <el-form-item label="Bio">
          <el-input v-model="editForm.bio" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="Interests">
          <TagSelector v-model="editForm.tags" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="saving" @click="saveProfile">Save</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import TagSelector from '@/components/profile/TagSelector.vue'
import { useUserStore } from '@/stores/user'
import { useActivityStore } from '@/stores/activity'
import { uploadImage } from '@/api/activity'
import { updateProfile } from '@/api/user'  // ✅ 改为直接用 updateProfile
import { formatDate } from '@/utils/format'
import { getTagMeta } from '@/utils/tags'
import type { Diary } from '@/types/diary'

const router    = useRouter()
const userStore = useUserStore()
const actStore  = useActivityStore()

const placeholder = 'https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?w=200&q=60'
const editVisible = ref(false)
const saving      = ref(false)
const MAX_AVATAR_BYTES = 5 * 1024 * 1024 // 5MB

// Shallow reactive copy of the user for display
const user = computed(() => ({
  nickname:   userStore.userInfo?.nickname   ?? '',
  avatar:     userStore.userInfo?.avatar     ?? '',
  bio:        userStore.userInfo?.bio        ?? '',
  tags:       userStore.userInfo?.tags       ?? [],
  activities: userStore.userInfo?.activities ?? 0,
  score:      userStore.userInfo?.score      ?? 0,
  ranking:    userStore.userInfo?.ranking    ?? '--',
}))

const displayTags = computed(() =>
  user.value.tags.map((value) => ({
    value,
    label: getTagMeta(value).label,
    emoji: getTagMeta(value).emoji,
  }))
)

const editForm = reactive({
  nickname: '',
  bio:      '',
  tags:     [] as string[],
})

const openEditDialog = () => {
  editForm.nickname = user.value.nickname
  editForm.bio = user.value.bio
  editForm.tags = [...user.value.tags]
  editVisible.value = true
}

// Derived data from store
const diaries = computed<Diary[]>(() => (actStore.diaries ?? []).slice(0, 6))
const activities = computed(() => {
  const list = (actStore.diaries ?? []) as Diary[]
  return list
    .slice(0, 5)
    .map((d: Diary) => ({
      id:           d.id,
      icon:         '🌅',
      name:         d.title,
      date:         formatDate(d.createdAt),
      participants: d.participants.map((p: Diary['participants'][number]) => p.avatar),
    }))
})

// ── Actions ──────────────────────────────────────────────────────
const saveProfile = async () => {
  saving.value = true
  try {
    await updateProfile(editForm as Parameters<typeof updateProfile>[0])
    await userStore.fetchUserInfo()
    editVisible.value = false
    ElMessage.success('Profile updated!')
  } finally { saving.value = false }
}

const onAvatarChange = async (e: Event) => {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  if (file.size > MAX_AVATAR_BYTES) {
    ElMessage.error('Image too large. Max size is 5MB.')
    return
  }
  try {
    const uploadResult = await uploadImage(file)
    const url =
      typeof uploadResult === 'string'
        ? uploadResult
        : (uploadResult as { url?: string; data?: { url?: string } }).url
          ?? (uploadResult as { data?: { url?: string } }).data?.url
    if (!url) {
      ElMessage.error('Upload failed: invalid image URL')
      return
    }
    await updateProfile({ avatar: url })  // ✅ 改为通用 updateProfile
    await userStore.fetchUserInfo()
    ElMessage.success('Avatar updated!')
  } catch { /* handled */ }
}

onMounted(() => {
  userStore.fetchUserInfo()
  actStore.fetchDiaries()
})
</script>

<style scoped>
/* .profile-page  { position: relative; z-index: 1; overflow-y: auto; min-height: calc(100vh - var(--nav-height)); } */

.profile-page {
  position: relative;
  z-index: 1;
  overflow-y: auto;
  min-height: 100vh;
  padding-top: var(--nav-height);
  box-sizing: border-box;
}

.profile-inner { max-width: 1060px; margin: 0 auto; padding: 36px 40px 60px; display: flex; flex-direction: column; gap: 22px; }

/* Header card */
.profile-header { display: flex; align-items: center; gap: 24px; padding: 28px 32px; }

.ph-ava-wrap { position: relative; flex-shrink: 0; }
.ph-ava {
  width: 84px; height: 84px; border-radius: 50%;
  object-fit: cover; border: 2.5px solid var(--color-border);
  box-shadow: 0 0 20px rgba(149,255,141,.2);
}
.ph-ava-edit {
  position: absolute; bottom: 2px; right: 2px;
  width: 22px; height: 22px; border-radius: 50%;
  background: var(--color-card-solid); border: 1px solid var(--color-border);
  display: flex; align-items: center; justify-content: center;
  font-size: 10px; cursor: pointer;
}

.ph-info  { flex: 1; }
.ph-name  { font-family: var(--font-display); font-size: 26px; margin-bottom: 6px; }
.ph-bio   { font-size: 13px; color: var(--color-text-secondary); margin-bottom: 12px; line-height: 1.55; }
.ph-tags  { display: flex; gap: 7px; flex-wrap: wrap; }
.ph-tag-emoji { margin-right: 6px; }

.ph-stats { display: flex; gap: 0; flex-shrink: 0; }
.ph-stat  { text-align: center; padding: 0 24px; border-left: 1px solid var(--color-border-dim); }
.ph-stat:first-child { border-left: none; }
.ph-stat-label { font-size: 12px; color: var(--color-text-secondary); margin-bottom: 5px; }
.ph-stat-num   { font-family: var(--font-display); font-size: 24px; }
.ph-stat-sub   { font-size: 12px; color: var(--color-text-secondary); }

.btn-edit {
  padding: 10px 22px; border-radius: var(--radius-md);
  background: var(--color-white); color: var(--color-black);
  font-size: 14px; font-weight: 700; border: none; cursor: pointer;
  flex-shrink: 0; transition: opacity .15s;
}
.btn-edit:hover { opacity: .88; }

/* Grid */
.profile-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.profile-section { padding: 22px; }
.ps-title  { font-family: var(--font-display); font-size: 17px; margin-bottom: 16px; }
.ps-empty  { font-size: 13px; color: var(--color-text-hint); text-align: center; padding: 20px 0; }

/* Journal wall */
.journal-wall { display: grid; grid-template-columns: repeat(3,1fr); gap: 7px; }
.jw-thumb { border-radius: 8px; overflow: hidden; background: #333; cursor: pointer; aspect-ratio: 1; }
.jw-thumb.tall { grid-row: span 2; aspect-ratio: auto; }
.jw-thumb img { width: 100%; height: 100%; object-fit: cover; transition: transform .2s; }
.jw-thumb:hover img { transform: scale(1.06); }

/* Activities list */
.act-list { display: flex; flex-direction: column; gap: 14px; }
.act-row  { display: flex; align-items: center; gap: 12px; }
.act-icon { font-size: 18px; flex-shrink: 0; }
.act-row-info { flex: 1; }
.act-row-name { font-size: 14px; font-weight: 500; margin-bottom: 4px; }
.act-row-meta { display: flex; align-items: center; gap: 8px; }
.act-avs { display: flex; }
.act-av  { width: 20px; height: 20px; border-radius: 50%; object-fit: cover; border: 1.5px solid #000; margin-left: -5px; }
.act-av:first-child { margin-left: 0; }
.act-date { font-size: 12px; color: var(--color-text-secondary); flex-shrink: 0; }

/* Edit dialog dark theme */
:deep(.el-dialog) {
  background-color: #1a1a1a !important;
}
:deep(.el-dialog__header) {
  background-color: #1a1a1a;
  border-bottom: 1px solid var(--color-border-dim);
}
:deep(.el-dialog__title) {
  color: #fff;
}
:deep(.el-dialog__close) {
  color: var(--color-text-secondary);
}
:deep(.el-dialog__body) {
  background-color: #1a1a1a;
  color: #fff;
}
:deep(.el-dialog__footer) {
  background-color: #1a1a1a;
  border-top: 1px solid var(--color-border-dim);
}
:deep(.el-form-item__label) {
  color: #fff;
}
:deep(.el-input__wrapper) {
  background-color: #2a2a2a !important;
  border-color: var(--color-border-dim);
}
:deep(.el-input__inner) {
  background-color: #2a2a2a;
  color: #fff;
}
:deep(.el-textarea__inner) {
  background-color: #2a2a2a !important;
  color: #fff;
  border-color: var(--color-border-dim);
}
:deep(.el-dialog__footer .el-button--primary) {
  background-color: var(--color-green);
  border-color: var(--color-green);
  color: #111;
  font-weight: 700;
  transition: all 0.15s;
}
:deep(.el-dialog__footer .el-button--primary:hover),
:deep(.el-dialog__footer .el-button--primary:focus) {
  background-color: #111;
  border-color: #111;
  color: var(--color-green);
}
</style>
