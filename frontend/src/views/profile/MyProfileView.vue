<template>
  <div class="profile-page">
    <div class="profile-inner">
      <div class="profile-header glass-card">
        <div class="ph-ava-wrap">
          <img :src="user.avatar || placeholder" class="ph-ava" alt="avatar">
          <label class="ph-ava-edit" title="Change photo">
            Edit
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
            >
              <span class="ph-tag-emoji">{{ tag.emoji }}</span>{{ tag.label }}
            </span>
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
            <div class="ph-stat-num" style="color:var(--color-text-gold)">{{ formatScore(user.score, user.reviewCount) }}</div>
            <div class="ph-stat-sub">{{ user.reviewCount }} reviews</div>
          </div>
          <div class="ph-stat">
            <div class="ph-stat-label">ranking</div>
            <div class="ph-stat-num">{{ user.ranking }}</div>
          </div>
        </div>

        <button class="btn-edit" @click="openEditDialog">Edit</button>
      </div>

      <div class="profile-grid">
        <div class="profile-section glass-card">
          <h3 class="ps-title">Journal</h3>
          <div class="journal-wall">
            <div
              v-for="(journal, index) in diaries"
              :key="journal.id"
              class="jw-thumb"
              :class="{ tall: index === 0 }"
              role="button"
              tabindex="0"
              @click="openJournalDialog(journal)"
              @keydown.enter.prevent="openJournalDialog(journal)"
              @keydown.space.prevent="openJournalDialog(journal)"
            >
              <img :src="journal.coverImage" :alt="journal.title">
            </div>
            <div v-if="diaries.length === 0" class="ps-empty">No shared cards yet</div>
          </div>
        </div>

        <div class="profile-section glass-card">
          <h3 class="ps-title">Activities</h3>
          <div class="act-list">
            <div v-for="activity in activities" :key="activity.id" class="act-row">
              <span class="act-icon">{{ activity.icon || '•' }}</span>
              <div class="act-row-info">
                <div class="act-row-name">{{ activity.name }}</div>
                <div class="act-row-meta">
                  <div class="act-avs">
                    <img
                      v-for="avatar in activity.participants.slice(0, 3)"
                      :key="avatar"
                      :src="avatar"
                      class="act-av"
                    >
                  </div>
                </div>
              </div>
              <span class="act-date">{{ formatActivityDate(activity.date) }}</span>
            </div>
            <div v-if="activities.length === 0" class="ps-empty">No activities yet</div>
          </div>
        </div>
      </div>
    </div>

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

    <el-dialog
      v-model="journalDialogVisible"
      class="journal-card-dialog"
      width="520px"
      align-center
      :show-close="true"
      destroy-on-close
    >
      <article v-if="selectedJournal" class="shared-card mine profile-journal-card">
        <div class="shared-card-head">
          <div class="shared-user">
            <img :src="user.avatar || placeholder" :alt="user.nickname" class="shared-avatar">
            <div class="shared-meta">
              <strong>{{ user.nickname }}</strong>
              <span>Your card</span>
            </div>
          </div>
          <span v-if="selectedJournal.sharedEntryId" class="shared-updated">#{{ selectedJournal.sharedEntryId }}</span>
        </div>

        <div class="profile-journal-title">{{ selectedJournal.title || 'Untitled Journal' }}</div>
        <p class="shared-text">{{ selectedJournal.excerpt || 'No journal entry yet.' }}</p>
        <div class="shared-images" v-if="selectedJournal.coverImage">
          <el-image
            :src="selectedJournal.coverImage"
            class="shared-image"
            :preview-src-list="[selectedJournal.coverImage]"
            fit="cover"
          />
        </div>
      </article>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import TagSelector from '@/components/profile/TagSelector.vue'
import { uploadImage } from '@/api/activity'
import { updateProfile } from '@/api/user'
import { useUserStore } from '@/stores/user'
import { formatScore } from '@/utils/format'
import type { ActivityItem, JournalThumb } from '@/types/user'
import {
  buildProfileUser,
  formatActivityDate,
  getDisplayTags,
  getProfileActivities,
  getProfileDiaries,
  MAX_AVATAR_BYTES,
  resolveUploadUrl,
  syncEditForm,
} from './profile-helpers'

const userStore = useUserStore()

const placeholder = 'https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?w=200&q=60'
const editVisible = ref(false)
const journalDialogVisible = ref(false)
const selectedJournal = ref<JournalThumb | null>(null)
const saving = ref(false)

const user = computed(() => buildProfileUser(userStore.userInfo))

const diaries = computed<JournalThumb[]>(() => getProfileDiaries(user.value))
const activities = computed<ActivityItem[]>(() => getProfileActivities(user.value))

const displayTags = computed(() => getDisplayTags(user.value.tags))

const editForm = reactive({
  nickname: '',
  bio: '',
  tags: [] as string[],
})

const openEditDialog = () => {
  syncEditForm(editForm, user.value)
  editVisible.value = true
}

const openJournalDialog = (journal: JournalThumb) => {
  selectedJournal.value = journal
  journalDialogVisible.value = true
}

const saveProfile = async () => {
  saving.value = true
  try {
    await updateProfile(editForm as Parameters<typeof updateProfile>[0])
    await userStore.fetchUserInfo()
    editVisible.value = false
    ElMessage.success('Profile updated!')
  } finally {
    saving.value = false
  }
}

const onAvatarChange = async (event: Event) => {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file) return
  if (file.size > MAX_AVATAR_BYTES) {
    ElMessage.error('Image too large. Max size is 5MB.')
    return
  }

  try {
    const uploadResult = await uploadImage(file)
    const url = resolveUploadUrl(uploadResult)

    if (!url) {
      ElMessage.error('Upload failed: invalid image URL')
      return
    }

    await updateProfile({ avatar: url })
    await userStore.fetchUserInfo()
    ElMessage.success('Avatar updated!')
  } catch {
    // handled by interceptor
  }
}

onMounted(() => {
  userStore.fetchUserInfo()
})
</script>

<style scoped>
.profile-page {
  position: relative;
  z-index: 1;
  overflow-y: auto;
  min-height: 100vh;
  padding-top: var(--nav-height);
  box-sizing: border-box;
}

.profile-inner {
  max-width: 1060px;
  margin: 0 auto;
  padding: 36px 40px 60px;
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.profile-header { display: flex; align-items: center; gap: 24px; padding: 28px 32px; }
.ph-ava-wrap { position: relative; flex-shrink: 0; }
.ph-ava {
  width: 84px; height: 84px; border-radius: 50%;
  object-fit: cover; border: 2.5px solid var(--color-border);
  box-shadow: 0 0 20px rgba(149,255,141,.2);
}
.ph-ava-edit {
  position: absolute; bottom: 2px; right: 2px;
  width: 32px; height: 22px; border-radius: 999px;
  background: var(--color-card-solid); border: 1px solid var(--color-border);
  display: flex; align-items: center; justify-content: center;
  font-size: 10px; cursor: pointer;
}

.ph-info { flex: 1; }
.ph-name { font-family: var(--font-display); font-size: 26px; margin-bottom: 6px; }
.ph-bio { font-size: 13px; color: var(--color-text-secondary); margin-bottom: 12px; line-height: 1.55; }
.ph-tags { display: flex; gap: 7px; flex-wrap: wrap; }
.ph-tag-emoji { margin-right: 6px; }

.ph-stats { display: flex; gap: 0; flex-shrink: 0; }
.ph-stat { text-align: center; padding: 0 24px; border-left: 1px solid var(--color-border-dim); }
.ph-stat:first-child { border-left: none; }
.ph-stat-label { font-size: 12px; color: var(--color-text-secondary); margin-bottom: 5px; }
.ph-stat-num { font-family: var(--font-display); font-size: 24px; }
.ph-stat-sub { font-size: 12px; color: var(--color-text-secondary); }

.btn-edit {
  padding: 10px 22px; border-radius: var(--radius-md);
  background: var(--color-white); color: var(--color-black);
  font-size: 14px; font-weight: 700; border: none; cursor: pointer;
  flex-shrink: 0;
}

.profile-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.profile-section { padding: 22px; }
.ps-title { font-family: var(--font-display); font-size: 17px; margin-bottom: 16px; }
.ps-empty { font-size: 13px; color: var(--color-text-hint); text-align: center; padding: 20px 0; }

.journal-wall { display: grid; grid-template-columns: repeat(3, 1fr); gap: 7px; }
.jw-thumb { border-radius: 8px; overflow: hidden; background: #333; cursor: pointer; aspect-ratio: 1; border: 1px solid transparent; transition: border-color .2s, transform .2s; }
.jw-thumb.tall { grid-row: span 2; aspect-ratio: auto; }
.jw-thumb img { width: 100%; height: 100%; object-fit: cover; transition: transform .2s; }
.jw-thumb:hover,
.jw-thumb:focus-visible { border-color: rgba(0, 255, 149, 0.45); transform: translateY(-1px); outline: none; }
.jw-thumb:hover img,
.jw-thumb:focus-visible img { transform: scale(1.06); }

:deep(.journal-card-dialog) {
  --el-dialog-bg-color: rgba(18, 20, 22, 0.96);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 22px;
  box-shadow: 0 28px 80px rgba(0, 0, 0, 0.48);
}

:deep(.journal-card-dialog .el-dialog__header) {
  padding: 0;
}

:deep(.journal-card-dialog .el-dialog__body) {
  padding: 18px;
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

.profile-journal-card {
  background: linear-gradient(145deg, rgba(26, 30, 29, 0.98), rgba(14, 16, 18, 0.98));
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
.shared-meta strong { font-size: 15px; color: var(--color-text); }
.shared-meta span,
.shared-updated { font-size: 12px; color: var(--color-text-secondary); }

.profile-journal-title {
  font-size: 17px;
  font-weight: 800;
  color: var(--color-text);
  line-height: 1.35;
}

.shared-text {
  margin: 0;
  min-height: 120px;
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
  height: 180px;
  object-fit: cover;
  border-radius: 12px;
}

.act-list { display: flex; flex-direction: column; gap: 14px; }
.act-row { display: flex; align-items: center; gap: 12px; }
.act-icon { font-size: 18px; flex-shrink: 0; }
.act-row-info { flex: 1; }
.act-row-name { font-size: 14px; font-weight: 500; margin-bottom: 4px; }
.act-row-meta { display: flex; align-items: center; gap: 8px; }
.act-avs { display: flex; }
.act-av { width: 20px; height: 20px; border-radius: 50%; object-fit: cover; border: 1.5px solid #000; margin-left: -5px; }
.act-av:first-child { margin-left: 0; }
.act-date { font-size: 12px; color: var(--color-text-secondary); flex-shrink: 0; }
</style>
