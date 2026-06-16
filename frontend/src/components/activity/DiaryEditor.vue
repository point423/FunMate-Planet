<template>
  <el-dialog v-model="visible" title="New Journal" width="520px" class="diary-dialog">
    <el-form :model="form" label-position="top">
      <el-form-item label="Activity" required>
        <el-select
          v-model="selectedActivityId"
          class="full-width"
          :disabled="activityLocked"
          :loading="activityLoading"
          placeholder="Choose a completed activity"
          @change="handleActivityChange"
        >
          <el-option
            v-for="activity in activityOptions"
            :key="activity.id"
            :label="activity.title"
            :value="activity.id"
          >
            <div class="activity-option">
              <span>{{ activity.title }}</span>
              <small>{{ formatTime(activity.activityTime) }}</small>
            </div>
          </el-option>
        </el-select>
      </el-form-item>

      <div v-if="participants.length > 0" class="form-field">
        <label class="field-label">Participants</label>
        <div class="partner-selector readonly">
          <div v-for="participant in participants" :key="participant.id" class="partner-chip">
            <img
              :src="participant.avatar || fallbackAvatar(participant.id)"
              class="partner-avatar"
              :alt="participant.nickname"
            >
            <span class="partner-name">{{ participant.nickname }}</span>
          </div>
        </div>
      </div>

      <el-form-item label="Title">
        <el-input v-model="form.title" placeholder="Give your journal a title" />
      </el-form-item>

      <el-form-item label="Photos">
        <div class="photo-grid">
          <div
            v-for="(src, i) in previews"
            :key="i"
            class="photo-thumb"
          >
            <img :src="src" alt="preview">
            <button class="remove-btn" type="button" @click="removePhoto(i)">x</button>
          </div>
          <label v-if="previews.length < 9" class="photo-add">
            <span>+</span>
            <input type="file" accept="image/*" multiple hidden @change="onFileChange">
          </label>
        </div>
      </el-form-item>

      <el-form-item label="Write your memory">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="4"
          placeholder="Describe this experience..."
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">Cancel</el-button>
      <el-button type="primary" :loading="loading" @click="submit">Save Journal</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createDiary, getActivityDetail, getCompletableActivities } from '@/api/activity'
import type { Activity, ActivityUser } from '@/types/activity'

const visible = defineModel<boolean>({ required: true })
const emit = defineEmits(['saved'])

const props = defineProps<{
  activityId?: number
}>()

const loading = ref(false)
const activityLoading = ref(false)
const previews = ref<string[]>([])
const files = ref<File[]>([])
const activityOptions = ref<Activity[]>([])
const participants = ref<ActivityUser[]>([])
const selectedActivityId = ref<number | null>(null)

const MAX_PHOTO_BYTES = 5 * 1024 * 1024
const MAX_TOTAL_BYTES = 10 * 1024 * 1024

const form = reactive({
  title: '',
  content: '',
})

const activityLocked = ref(false)

const resetState = () => {
  form.title = ''
  form.content = ''
  previews.value = []
  files.value = []
  participants.value = []
  activityOptions.value = []
  selectedActivityId.value = null
  activityLocked.value = false
}

const initialize = async () => {
  resetState()
  activityLoading.value = true
  try {
    if (props.activityId) {
      activityLocked.value = true
      selectedActivityId.value = props.activityId
      const detail = await getActivityDetail(props.activityId)
      activityOptions.value = [detail.activity]
      participants.value = detail.participants ?? []
      return
    }

    const activities = await getCompletableActivities()
    activityOptions.value = Array.isArray(activities) ? activities : []

    if (activityOptions.value.length === 1) {
      selectedActivityId.value = activityOptions.value[0].id
      await loadParticipants(activityOptions.value[0].id)
    }
  } finally {
    activityLoading.value = false
  }
}

watch(
  () => visible.value,
  async (open) => {
    if (open) {
      await initialize()
    } else {
      resetState()
    }
  },
  { immediate: true },
)

const loadParticipants = async (activityId: number) => {
  const detail = await getActivityDetail(activityId)
  participants.value = detail.participants ?? []
}

const handleActivityChange = async (activityId: number) => {
  await loadParticipants(activityId)
}

const onFileChange = (e: Event) => {
  const input = e.target as HTMLInputElement
  const incoming = Array.from(input.files ?? []) as File[]
  if (incoming.length === 0) return

  const currentTotal = files.value.reduce((sum, file) => sum + (file.size || 0), 0)
  let newTotal = currentTotal

  for (const file of incoming) {
    if (file.size > MAX_PHOTO_BYTES) {
      ElMessage.error(`Single photo too large: ${(file.size / 1024 / 1024).toFixed(2)}MB`)
      continue
    }

    newTotal += file.size
    if (newTotal > MAX_TOTAL_BYTES) {
      ElMessage.error(`Total upload too large. Limit ${MAX_TOTAL_BYTES / 1024 / 1024}MB`)
      break
    }

    files.value.push(file)
    previews.value.push(URL.createObjectURL(file))
  }

  if (input.value) {
    input.value = ''
  }
}

const removePhoto = (index: number) => {
  files.value.splice(index, 1)
  previews.value.splice(index, 1)
}

const submit = async () => {
  if (!selectedActivityId.value) {
    ElMessage.warning('Please choose an activity')
    return
  }

  if (!form.title) {
    ElMessage.warning('Please enter a title')
    return
  }

  const totalSize = files.value.reduce((sum, file) => sum + (file.size || 0), 0)
  if (totalSize > MAX_TOTAL_BYTES) {
    ElMessage.error(`Total upload too large. Limit ${MAX_TOTAL_BYTES / 1024 / 1024}MB`)
    return
  }

  loading.value = true
  try {
    const fd = new FormData()
    fd.append('activityId', String(selectedActivityId.value))
    fd.append('title', form.title)
    fd.append('content', form.content)
    files.value.forEach((file) => fd.append('files', file))

    await createDiary(fd)
    ElMessage.success('Journal saved!')
    visible.value = false
    emit('saved')
  } finally {
    loading.value = false
  }
}

const formatTime = (value: string) =>
  new Date(value).toLocaleString('en-US', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })

const fallbackAvatar = (seed: number) => `https://api.dicebear.com/7.x/avataaars/svg?seed=${seed}`
</script>

<style scoped>
.full-width {
  width: 100%;
}

.activity-option {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.activity-option small {
  color: var(--color-text-secondary);
}

.field-label {
  display: block;
  margin-bottom: 10px;
  color: var(--color-text-secondary);
  font-size: 13px;
}

.photo-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.photo-thumb {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
}

.photo-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-btn {
  position: absolute;
  top: 3px;
  right: 3px;
  background: rgba(0, 0, 0, 0.65);
  border: none;
  color: white;
  border-radius: 50%;
  width: 18px;
  height: 18px;
  font-size: 12px;
  line-height: 1;
  cursor: pointer;
}

.photo-add {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  border: 1.5px dashed var(--color-border);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: border-color 0.15s;
}

.photo-add:hover {
  border-color: var(--color-green-border);
  color: var(--color-green);
}

.partner-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.partner-chip {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 20px;
  border: 1px solid var(--color-border);
}

.partner-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
}

.partner-name {
  font-size: 13px;
  color: var(--color-text);
}
</style>

<style>
.diary-dialog {
  --el-dialog-bg-color: #1a1a1a;
  --el-dialog-border-color: var(--color-border-dim);
}

.diary-dialog .el-dialog {
  background-color: #1a1a1a !important;
}

.diary-dialog .el-overlay-dialog {
  background-color: #1a1a1a !important;
}

.diary-dialog .el-dialog__header {
  background-color: #1a1a1a !important;
  border-bottom: 1px solid var(--color-border-dim) !important;
}

.diary-dialog .el-dialog__title {
  color: #fff !important;
}

.diary-dialog .el-dialog__close {
  color: var(--color-text-secondary) !important;
}

.diary-dialog .el-dialog__body {
  background-color: #1a1a1a !important;
  color: #fff !important;
}

.diary-dialog .el-form {
  background-color: #1a1a1a !important;
}

.diary-dialog .el-form-item {
  background-color: transparent !important;
}

.diary-dialog .el-form-item__label {
  color: #fff !important;
  background-color: transparent !important;
}

.diary-dialog .el-form-item__content {
  background-color: transparent !important;
}

.diary-dialog .el-input__wrapper,
.diary-dialog .el-select__wrapper,
.diary-dialog .el-textarea__inner {
  background-color: #2a2a2a !important;
  border-color: var(--color-border-dim) !important;
}

.diary-dialog .el-input__inner {
  background-color: #2a2a2a !important;
  color: #fff !important;
}

.diary-dialog .el-dialog__footer {
  background-color: #1a1a1a !important;
  border-top: 1px solid var(--color-border-dim) !important;
}
</style>
