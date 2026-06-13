<template>
  <div class="all-view">
    <header class="page-header">
      <div>
        <p class="eyebrow">All Activities</p>
        <h1>Your activity board</h1>
        <p class="subtitle">Track what is live, waiting, completed, or cancelled in one place.</p>
      </div>
      <el-button type="primary" round @click="showCreateModal = true">
        + New Activity
      </el-button>
    </header>

    <div v-loading="loading" class="group-list">
      <section
        v-for="group in visibleGroups"
        :key="group.key"
        class="group-section"
      >
        <div class="group-header">
          <span class="group-kicker">{{ group.kicker }}</span>
          <h2>{{ group.title }}</h2>
        </div>

        <div class="activity-grid">
          <article
            v-for="item in group.items"
            :key="item.activity.id"
            class="activity-card"
            :class="[`status-${group.key}`, { muted: isMuted(item.activity.status) }]"
            @click="handleCardClick(item)"
          >
            <div class="card-top">
              <span class="status-badge" :class="badgeClass(item.activity.status)">
                {{ statusLabel(item.activity.status) }}
              </span>
              <span class="card-time">{{ formatDate(item.activity.activityTime) }}</span>
            </div>

            <h3>{{ item.activity.title }}</h3>

            <div v-if="item.activity.status !== 3" class="meta-row">
              <span>{{ item.activity.location }}</span>
              <span>{{ item.participantCount }} / {{ item.activity.maxParticipants }} joined</span>
            </div>
            <div v-else class="meta-row">
              <span>{{ formatDate(item.activity.activityTime) }}</span>
            </div>

            <div v-if="item.activity.status !== 3" class="avatar-row">
              <img
                v-for="participant in item.participants.slice(0, 3)"
                :key="participant.id"
                :src="participant.avatar || fallbackAvatar(participant.id)"
                :alt="participant.nickname"
                class="avatar"
              >
            </div>

            <div class="card-footer">
              <template v-if="item.activity.status === 0">
                <span class="hint waiting">Waiting for partner...</span>
                <button
                  v-if="canDeleteActivity(item.activity)"
                  class="inline-action danger"
                  type="button"
                  @click.stop="removeActivity(item.activity.id)"
                >
                  Delete
                </button>
              </template>
              <template v-else-if="item.activity.status === 1">
                <span class="hint progress">In Progress</span>
                <button
                  v-if="canDeleteActivity(item.activity)"
                  class="inline-action danger"
                  type="button"
                  @click.stop="removeActivity(item.activity.id)"
                >
                  Delete
                </button>
              </template>
              <template v-else-if="item.activity.status === 2 && !item.hasJournal">
                <button
                  class="inline-action create"
                  type="button"
                  @click.stop="openDiaryEditor(item.activity.id)"
                >
                  + Create Journal
                </button>
                <button
                  v-if="canDeleteActivity(item.activity)"
                  class="inline-action danger"
                  type="button"
                  @click.stop="removeActivity(item.activity.id)"
                >
                  Delete
                </button>
              </template>
              <template v-else-if="item.activity.status === 2">
                <span class="hint journal">Journal</span>
                <button
                  v-if="canDeleteActivity(item.activity)"
                  class="inline-action danger"
                  type="button"
                  @click.stop="removeActivity(item.activity.id)"
                >
                  Delete
                </button>
              </template>
              <template v-else>
                <span class="hint cancelled">Read only</span>
                <button
                  v-if="canDeleteActivity(item.activity)"
                  class="inline-action danger"
                  type="button"
                  @click.stop="removeActivity(item.activity.id)"
                >
                  Delete
                </button>
              </template>
            </div>
          </article>
        </div>
      </section>

      <div v-if="!loading && visibleGroups.length === 0" class="empty-state">
        No activities yet. Start your first one and it will appear here.
      </div>
    </div>

    <StartActivityModal v-model="showCreateModal" @saved="refreshList" />
    <DiaryEditor
      v-model="showEditor"
      :activity-id="selectedActivityId"
      @saved="handleJournalSaved"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { deleteActivity, getActivityDetail, getMyActivities } from '@/api/activity'
import DiaryEditor from '@/components/activity/DiaryEditor.vue'
import StartActivityModal from '@/components/activity/StartActivityModal.vue'
import { useUserStore } from '@/stores/user'
import type { Activity, ActivityDetailResponse, ActivityStatus, GroupedActivitiesResponse } from '@/types/activity'

type ActivityCardItem = ActivityDetailResponse & {
  activity: Activity
}

type GroupKey = keyof GroupedActivitiesResponse

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const showCreateModal = ref(false)
const showEditor = ref(false)
const selectedActivityId = ref<number | undefined>()
const groups = ref<Record<GroupKey, ActivityCardItem[]>>({
  pending: [],
  active: [],
  completed: [],
  cancelled: [],
})

const groupMeta: Array<{ key: GroupKey; title: string; kicker: string }> = [
  { key: 'active', title: 'Active', kicker: 'Live now' },
  { key: 'pending', title: 'Pending', kicker: 'Waiting' },
  { key: 'completed', title: 'Completed', kicker: 'Wrapped' },
  { key: 'cancelled', title: 'Cancelled', kicker: 'Stopped' },
]

const visibleGroups = computed(() =>
  groupMeta
    .map((meta) => ({ ...meta, items: groups.value[meta.key] }))
    .filter((group) => group.items.length > 0),
)

const refreshList = async () => {
  loading.value = true
  try {
    const grouped = await getMyActivities()
    groups.value = await hydrateGroups(grouped)
  } catch {
    groups.value = {
      pending: [],
      active: [],
      completed: [],
      cancelled: [],
    }
  } finally {
    loading.value = false
  }
}

const hydrateGroups = async (grouped: GroupedActivitiesResponse) => {
  const detailEntries = await Promise.allSettled(
    Object.values(grouped)
      .flat()
      .map(async (activity) => ({
        id: activity.id,
        detail: await getActivityDetail(activity.id),
      })),
  )

  const detailMap = new Map<number, ActivityDetailResponse>()
  detailEntries.forEach((entry) => {
    if (entry.status === 'fulfilled') {
      detailMap.set(entry.value.id, entry.value.detail)
    }
  })

  const buildItems = (activities: Activity[]) =>
    activities.map((activity) => {
      const detail = detailMap.get(activity.id)
      return {
        activity,
        participants: detail?.participants ?? [],
        participantCount: detail?.participantCount ?? 0,
        hasJournal: detail?.hasJournal ?? false,
      }
    })

  return {
    pending: buildItems(grouped.pending ?? []),
    active: buildItems(grouped.active ?? []),
    completed: buildItems(grouped.completed ?? []),
    cancelled: buildItems(grouped.cancelled ?? []),
  }
}

const handleCardClick = (item: ActivityCardItem) => {
  if (item.activity.status === 0 || item.activity.status === 3) {
    return
  }
  router.push(`/activity/${item.activity.id}`)
}

const openDiaryEditor = (activityId: number) => {
  selectedActivityId.value = activityId
  showEditor.value = true
}

const removeActivity = async (activityId: number) => {
  try {
    await ElMessageBox.confirm(
      'Delete this pending activity? This cannot be undone.',
      'Delete Activity',
      {
        confirmButtonText: 'Delete',
        cancelButtonText: 'Cancel',
        type: 'warning',
      },
    )
    await deleteActivity(activityId)
    await refreshList()
  } catch {
    // Ignore cancel and let API interceptor handle actual errors.
  }
}

const handleJournalSaved = async () => {
  showEditor.value = false
  await refreshList()
}

const statusLabel = (status: ActivityStatus) => {
  const map: Record<ActivityStatus, string> = {
    0: 'Pending',
    1: 'Active',
    2: 'Completed',
    3: 'Cancelled',
  }
  return map[status]
}

const badgeClass = (status: ActivityStatus) => {
  const map: Record<ActivityStatus, string> = {
    0: 'badge-pending',
    1: 'badge-active',
    2: 'badge-completed',
    3: 'badge-cancelled',
  }
  return map[status]
}

const formatDate = (value: string) =>
  new Date(value).toLocaleString('en-US', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })

const isMuted = (status: ActivityStatus) => status === 0 || status === 3

const canDeleteActivity = (activity: Activity) =>
  activity.creatorId === userStore.userInfo?.id

const fallbackAvatar = (seed: number) => `https://api.dicebear.com/7.x/avataaars/svg?seed=${seed}`

onMounted(refreshList)
</script>

<style scoped>
.all-view {
  flex: 1;
  overflow-y: auto;
  padding: 28px;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 24px;
}

.eyebrow {
  margin: 0 0 6px;
  color: var(--color-green);
  text-transform: uppercase;
  letter-spacing: 0.16em;
  font-size: 12px;
}

.page-header h1 {
  margin: 0;
  font-size: 34px;
}

.subtitle {
  margin: 10px 0 0;
  color: var(--color-text-secondary);
}

.group-list {
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.group-section {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.group-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.group-header h2 {
  margin: 0;
  font-size: 24px;
}

.group-kicker {
  color: var(--color-text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.12em;
  font-size: 11px;
}

.activity-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 18px;
}

.activity-card {
  padding: 20px;
  border-radius: 22px;
  border: 1px solid var(--color-border);
  background:
    linear-gradient(160deg, rgba(255, 255, 255, 0.06), rgba(255, 255, 255, 0.02)),
    rgba(0, 0, 0, 0.28);
  cursor: pointer;
  transition: transform 0.18s ease, border-color 0.18s ease;
}

.activity-card:hover {
  transform: translateY(-4px);
  border-color: rgba(255, 255, 255, 0.24);
}

.activity-card.muted {
  opacity: 0.6;
  cursor: default;
}

.activity-card.muted:hover {
  transform: none;
}

.card-top,
.meta-row,
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.activity-card h3 {
  margin: 16px 0 14px;
  font-size: 22px;
  line-height: 1.15;
}

.status-badge,
.hint,
.inline-action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.badge-pending {
  background: rgba(255, 255, 255, 0.12);
  color: #d4d4d4;
}

.badge-active {
  background: rgba(0, 255, 149, 0.18);
  color: var(--color-green);
}

.badge-completed {
  background: rgba(80, 196, 93, 0.18);
  color: #8aff9b;
}

.badge-cancelled {
  background: rgba(255, 129, 129, 0.16);
  color: #ff9b9b;
}

.card-time,
.meta-row {
  color: var(--color-text-secondary);
  font-size: 13px;
}

.avatar-row {
  display: flex;
  margin: 18px 0;
}

.avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  border: 2px solid rgba(0, 0, 0, 0.8);
  margin-left: -8px;
  object-fit: cover;
}

.avatar:first-child {
  margin-left: 0;
}

.hint.waiting {
  background: rgba(255, 255, 255, 0.1);
  color: #d8d8d8;
}

.hint.progress {
  background: rgba(0, 255, 149, 0.16);
  color: var(--color-green);
}

.hint.journal {
  background: rgba(91, 204, 96, 0.16);
  color: #8aff9b;
}

.hint.cancelled {
  background: rgba(255, 255, 255, 0.08);
  color: var(--color-text-secondary);
}

.inline-action {
  border: 0;
  cursor: pointer;
  background: rgba(255, 168, 70, 0.2);
  color: #ffcb82;
}

.inline-action.danger {
  background: rgba(255, 92, 92, 0.18);
  color: #ff9b9b;
}

.empty-state {
  padding: 56px 20px;
  border-radius: 24px;
  border: 1px dashed var(--color-border);
  text-align: center;
  color: var(--color-text-secondary);
}

@media (max-width: 900px) {
  .page-header {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
