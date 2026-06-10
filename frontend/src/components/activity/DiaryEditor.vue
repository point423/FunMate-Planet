<template>
  <el-dialog v-model="visible" title="New Journal" width="520px" class="diary-dialog">
    <el-form :model="form" label-position="top">
      <el-form-item label="Title">
        <el-input v-model="form.title" placeholder="Give your journal a title" />
      </el-form-item>

      <!-- Partner 选择区域 -->
      <div class="form-field" v-if="partners && partners.length > 0">
        <label class="field-label">Partners</label>
        <div class="partner-selector">
          <div
            v-for="p in partners"
            :key="p.id"
            class="partner-chip"
            :class="{ selected: selectedPartnerIds.includes(p.id) }"
            @click="togglePartner(p.id)"
          >
            <img :src="p.avatar" class="partner-avatar" />
            <span class="partner-name">{{ p.nickname }}</span>
          </div>
        </div>
      </div>

      <el-form-item label="Photos">
        <div class="photo-grid">
          <div
            v-for="(src, i) in previews"
            :key="i"
            class="photo-thumb"
          >
            <img :src="src" alt="preview">
            <button class="remove-btn" @click="removePhoto(i)">×</button>
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
import { ref, reactive } from 'vue'
import { createDiary } from '@/api/activity'
import { ElMessage } from 'element-plus'

const visible = defineModel<boolean>()
// const emit    = defineEmits<{ (e: 'saved'): void }>()
const emit = defineEmits(['update:modelValue', 'saved'])

// const props   = defineProps<{ activityId: number }>()
const props = defineProps<{
  modelValue: boolean
  activityId: number
  partners?: Array<{ id: number; nickname: string; avatar: string }>
}>()
const selectedPartnerIds = ref<number[]>([])
const loading = ref(false)
const previews = ref<string[]>([])
const files    = ref<File[]>([])

// Limits: per-photo 5MB, total request 10MB (must match backend application.yml)
const MAX_PHOTO_BYTES = 5 * 1024 * 1024
const MAX_TOTAL_BYTES = 10 * 1024 * 1024

const form = reactive({ title: '', content: '' })

// 切换选中 Partner 的方法
const togglePartner = (id: number) => {
  const index = selectedPartnerIds.value.indexOf(id)
  if (index > -1) {
    selectedPartnerIds.value.splice(index, 1)
  } else {
    selectedPartnerIds.value.push(id)
  }
}


const onFileChange = (e: Event) => {
  const input = e.target as HTMLInputElement
  const incoming = Array.from(input.files ?? []) as File[]
  if (incoming.length === 0) return

  // calculate current total size
  const currentTotal = files.value.reduce((s, f) => s + (f.size || 0), 0)
  let newTotal = currentTotal

  for (const f of incoming) {
    if (f.size > MAX_PHOTO_BYTES) {
      ElMessage.error(`Single photo too large: ${(f.size/1024/1024).toFixed(2)}MB (max ${(MAX_PHOTO_BYTES/1024/1024)}MB)`)
      continue
    }
    newTotal += f.size
    if (newTotal > MAX_TOTAL_BYTES) {
      ElMessage.error(`Total upload too large. Limit ${(MAX_TOTAL_BYTES/1024/1024)}MB`)
      break
    }
    files.value.push(f)
    previews.value.push(URL.createObjectURL(f))
  }
    // reset input so same file can be re-selected later
// reset input so same file can be re-selected later
  if (input && input.value) {
    input.value = ''
  }

}

const removePhoto = (i: number) => {
  files.value.splice(i, 1)
  previews.value.splice(i, 1)
}

// const submit = async () => {
//   if (!form.title) { ElMessage.warning('Please enter a title'); return }
//   loading.value = true
//   // validate total size before sending
//   const totalSize = files.value.reduce((s, f) => s + (f.size || 0), 0)
//   if (totalSize > MAX_TOTAL_BYTES) {
//     ElMessage.error(`Total upload too large. Limit ${(MAX_TOTAL_BYTES/1024/1024)}MB`)
//     loading.value = false
//     return
//   }
//   const fd = new FormData()
//   fd.append('activityId', String(props.activityId))
//   fd.append('title', form.title)
//   fd.append('content', form.content)
//   files.value.forEach(f => fd.append('files', f))
//   try {
//     await createDiary(fd)
//     ElMessage.success('Journal saved!')
//     visible.value = false
//     emit('saved')
//   } finally {
//     loading.value = false
//   }
// }

const submit = async () => {
  if (!form.title) { ElMessage.warning('Please enter a title'); return }
  loading.value = true
  const totalSize = files.value.reduce((s, f) => s + (f.size || 0), 0)
  if (totalSize > MAX_TOTAL_BYTES) {
    ElMessage.error(`Total upload too large. Limit ${(MAX_TOTAL_BYTES/1024/1024)}MB`)
    loading.value = false
    return
  }

  const fd = new FormData()
  fd.append('activityId', String(props.activityId))
  fd.append('title', form.title)
  fd.append('content', form.content)
  
  // 添加参与者信息
  if (selectedPartnerIds.value && selectedPartnerIds.value.length > 0) {
    fd.append('participantIds', JSON.stringify(selectedPartnerIds.value))
    console.log('发送的参与者ID:', selectedPartnerIds.value)
  }
  
  files.value.forEach(f => fd.append('files', f))
  try {
    const result = await createDiary(fd)
    console.log('保存结果:', result)
    ElMessage.success('Journal saved!')
    visible.value = false
    emit('saved')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败，请重试')
  } finally {
    loading.value = false
  }
}


</script>

<style scoped>
.photo-grid { display: flex; flex-wrap: wrap; gap: 8px; }
.photo-thumb {
  width: 80px; height: 80px; border-radius: 8px; overflow: hidden;
  position: relative;
}
.photo-thumb img { width: 100%; height: 100%; object-fit: cover; }
.remove-btn {
  position: absolute; top: 3px; right: 3px;
  background: rgba(0,0,0,.65); border: none;
  color: white; border-radius: 50%; width: 18px; height: 18px;
  font-size: 12px; line-height: 1; cursor: pointer;
}
.photo-add {
  width: 80px; height: 80px; border-radius: 8px;
  border: 1.5px dashed var(--color-border);
  display: flex; align-items: center; justify-content: center;
  font-size: 24px; color: var(--color-text-secondary);
  cursor: pointer; transition: border-color .15s;
}
.photo-add:hover { border-color: var(--color-green-border); color: var(--color-green); }
</style>

<!-- Global styles for teleported dialog -->
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

.diary-dialog .el-input__wrapper {
  background-color: #2a2a2a !important;
  border-color: var(--color-border-dim) !important;
}

.diary-dialog .el-input__inner {
  background-color: #2a2a2a !important;
  color: #fff !important;
}

.diary-dialog .el-input.is-focus .el-input__wrapper {
  border-color: var(--color-green-border) !important;
}

.diary-dialog .el-textarea__wrapper {
  background-color: transparent !important;
}

.diary-dialog .el-textarea__inner {
  background-color: #2a2a2a !important;
  color: #fff !important;
  border-color: var(--color-border-dim) !important;
}

.diary-dialog .el-textarea.is-focus .el-textarea__inner {
  border-color: var(--color-green-border) !important;
}

.diary-dialog .el-dialog__footer {
  background-color: #1a1a1a !important;
  border-top: 1px solid var(--color-border-dim) !important;
}

.diary-dialog .el-dialog__footer .el-button {
  background-color: #fff !important;
  border-color: #fff !important;
  color: #1a1a1a !important;
}

.diary-dialog .el-dialog__footer .el-button--primary {
  background-color: var(--color-green) !important;
  border-color: var(--color-green) !important;
  color: #111 !important;
  font-weight: 700 !important;
}

.diary-dialog .el-dialog__footer .el-button--primary:hover,
.diary-dialog .el-dialog__footer .el-button--primary:focus {
  background-color: #111 !important;
  border-color: #111 !important;
  color: var(--color-green) !important;
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
  cursor: pointer;
  transition: all 0.2s;
}

.partner-chip:hover {
  border-color: var(--color-green-border);
}

.partner-chip.selected {
  background: rgba(144, 255, 140, 0.15);
  border-color: var(--color-green);
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
