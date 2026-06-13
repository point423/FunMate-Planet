<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="540px"
    class="activity-modal"
    destroy-on-close
  >
    <el-form :model="form" label-position="top">
      <el-form-item v-if="showInviteeSelect" label="Invite a friend">
        <el-select
          v-model="form.inviteeId"
          class="full-width"
          filterable
          clearable
          :loading="friendsLoading"
          placeholder="Choose a friend"
        >
          <el-option
            v-for="friend in friends"
            :key="friend.id"
            :label="friend.nickname"
            :value="friend.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="Title" required>
        <el-input v-model="form.title" placeholder="Weekend coffee, museum walk, city hike..." />
      </el-form-item>

      <el-form-item label="Description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="What are you planning to do together?"
        />
      </el-form-item>

      <el-form-item label="Plan">
        <el-input
          v-model="form.plan"
          type="textarea"
          :rows="3"
          placeholder="Optional timeline, checklist, or game plan"
        />
      </el-form-item>

      <div class="row">
        <el-form-item class="grow" label="Start time" required>
          <el-date-picker
            v-model="form.activityTime"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            class="full-width"
          />
        </el-form-item>
        <el-form-item label="Max people" required>
          <el-input-number v-model="form.maxParticipants" :min="2" :max="20" />
        </el-form-item>
      </div>

      <el-form-item label="Location" required>
        <el-input v-model="form.location" placeholder="Where should everyone meet?" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">Cancel</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">
        {{ props.mode === 'edit' ? 'Save Changes' : 'Create Activity' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createActivity, updateActivity } from '@/api/activity'
import { getFriends } from '@/api/user'
import type { Activity, ActivityForm } from '@/types/activity'

const props = withDefaults(
  defineProps<{
    mode?: 'create' | 'edit'
    activity?: Partial<Activity> | null
  }>(),
  {
    mode: 'create',
    activity: null,
  },
)

const emit = defineEmits<{
  (e: 'saved'): void
}>()

const visible = defineModel<boolean>({ required: true })

type FriendOption = {
  id: number
  nickname: string
  avatar?: string
}

const friends = ref<FriendOption[]>([])
const friendsLoading = ref(false)
const submitting = ref(false)

const form = reactive<ActivityForm>({
  title: '',
  description: '',
  plan: '',
  activityTime: '',
  location: '',
  maxParticipants: 2,
  inviteeId: undefined,
})

const dialogTitle = computed(() =>
  props.mode === 'edit' ? 'Edit Activity' : 'New Activity',
)

const showInviteeSelect = computed(() => props.mode === 'create')

const syncFormFromProps = () => {
  form.title = props.activity?.title ?? ''
  form.description = props.activity?.description ?? ''
  form.plan = props.activity?.plan ?? ''
  form.activityTime = normalizeActivityTime(props.activity?.activityTime)
  form.location = props.activity?.location ?? ''
  form.maxParticipants = props.activity?.maxParticipants ?? 2
  form.inviteeId = undefined
}

const resetForm = () => {
  form.title = ''
  form.description = ''
  form.plan = ''
  form.activityTime = ''
  form.location = ''
  form.maxParticipants = 2
  form.inviteeId = undefined
}

const normalizeActivityTime = (value?: string | null) => {
  if (!value) return ''
  return value.length >= 19 ? value.slice(0, 19) : value
}

const loadFriends = async () => {
  friendsLoading.value = true
  try {
    const data = await getFriends()
    friends.value = Array.isArray(data) ? data : []
  } catch {
    friends.value = []
  } finally {
    friendsLoading.value = false
  }
}

watch(
  () => visible.value,
  async (open) => {
    if (!open) {
      resetForm()
      return
    }

    if (props.mode === 'edit') {
      syncFormFromProps()
      return
    }

    resetForm()
    await loadFriends()
  },
  { immediate: true },
)

watch(
  () => props.activity,
  () => {
    if (visible.value && props.mode === 'edit') {
      syncFormFromProps()
    }
  },
  { deep: true },
)

const submit = async () => {
  if (!form.title || !form.activityTime || !form.location) {
    ElMessage.warning('Please complete the required fields.')
    return
  }

  if (showInviteeSelect.value && friends.value.length > 0 && !form.inviteeId) {
    ElMessage.warning('Please choose a friend to invite.')
    return
  }

  submitting.value = true
  try {
    if (props.mode === 'edit' && props.activity?.id) {
      await updateActivity(props.activity.id, { ...form })
      ElMessage.success('Activity updated.')
    } else {
      await createActivity({ ...form })
      ElMessage.success('Activity created.')
    }
    visible.value = false
    emit('saved')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.full-width {
  width: 100%;
}

.row {
  display: flex;
  gap: 16px;
}

.grow {
  flex: 1;
}
</style>
