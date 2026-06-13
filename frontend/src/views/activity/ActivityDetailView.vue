<template>
  <div class="detail-view" v-loading="loading">
    <div v-if="detail" class="detail-shell">
      <header class="detail-header">
        <button class="back-link" type="button" @click="router.push('/activity/all')">
          Back
        </button>
        <span class="status-badge" :class="badgeClass(detail.activity.status)">
          {{ statusLabel(detail.activity.status) }}
        </span>
      </header>

      <section class="hero-card">
        <h1>{{ detail.activity.title }}</h1>
        <div class="meta-grid">
          <div>
            <span class="meta-label">Location</span>
            <strong>{{ detail.activity.location }}</strong>
          </div>
          <div>
            <span class="meta-label">Start time</span>
            <strong>{{ formatDate(detail.activity.activityTime) }}</strong>
          </div>
        </div>
      </section>

      <section class="content-grid">
        <article class="info-card">
          <h2>Description</h2>
          <p>{{ detail.activity.description || 'No description provided.' }}</p>
        </article>

        <article v-if="detail.activity.plan" class="info-card">
          <h2>Plan</h2>
          <p>{{ detail.activity.plan }}</p>
        </article>

        <article class="info-card">
          <div class="participants-head">
            <h2>Participants</h2>
            <span>{{ detail.participantCount }}</span>
          </div>
          <div class="participants-list">
            <div
              v-for="participant in detail.participants"
              :key="participant.id"
              class="participant-row"
            >
              <img
                :src="participant.avatar || fallbackAvatar(participant.id)"
                :alt="participant.nickname"
                class="participant-avatar"
              >
              <span>{{ participant.nickname }}</span>
            </div>
          </div>
        </article>
      </section>

      <section
        v-if="showReviewSection"
        ref="reviewSectionEl"
        class="review-shell"
        :class="{ spotlight: reviewSpotlight }"
      >
        <div class="section-header">
          <span class="section-title">Review your partner</span>
          <span class="section-desc">Leave feedback after the activity is completed.</span>
        </div>
        <div class="participants-review-list">
          <div v-for="participant in reviewableParticipants" :key="participant.id" class="review-card">
            <div class="review-card-head">
              <img
                :src="participant.avatar || fallbackAvatar(participant.id)"
                :alt="participant.nickname"
                class="participant-avatar"
              >
              <div class="review-meta">
                <strong>{{ participant.nickname }}</strong>
                <span>{{ reviewLabel(ratingMap[participant.id]) }}</span>
              </div>
            </div>
            <el-rate
              v-model="ratingMap[participant.id]"
              :max="3"
              :disabled="isReviewed(participant.id)"
              @change="(value) => handleRate(participant.id, value)"
            />
            <div class="review-status-row">
              <span class="review-scale">Bad / Okay / Great</span>
              <span v-if="isReviewed(participant.id)" class="reviewed-badge">Reviewed</span>
            </div>
          </div>
        </div>
      </section>

      <footer class="action-bar">
        <template v-if="detail.activity.status === 1">
          <template v-if="isParticipant">
            <el-button type="primary" :loading="completing" @click="completeCurrentActivity">
              Complete Activity
            </el-button>
            <el-button v-if="isCreator" plain @click="showEditModal = true">Edit</el-button>
            <el-button v-if="isCreator" plain type="danger" @click="removeCurrentActivity">
              Delete Activity
            </el-button>
          </template>
        </template>

        <template v-else-if="detail.activity.status === 2">
          <el-button v-if="linkedDiaryId" type="primary" @click="goToJournal">
            View Journal
          </el-button>
          <el-button v-else type="primary" @click="openDiaryEditor(detail.activity.id)">
            Create Journal
          </el-button>
          <el-button v-if="isCreator" plain type="danger" @click="removeCurrentActivity">
            Delete Activity
          </el-button>
        </template>

        <template v-else-if="detail.activity.status === 0">
          <p class="status-copy">
            Waiting for your partner to accept the invitation...
          </p>
          <el-button v-if="isCreator" plain type="danger" @click="removeCurrentActivity">
            Delete Activity
          </el-button>
        </template>
        <template v-else>
          <p class="status-copy">This activity was cancelled.</p>
          <el-button v-if="isCreator" plain type="danger" @click="removeCurrentActivity">
            Delete Activity
          </el-button>
        </template>
      </footer>
    </div>

    <StartActivityModal
      v-model="showEditModal"
      mode="edit"
      :activity="detail?.activity ?? null"
      @saved="refreshDetail"
    />
    <DiaryEditor
      v-model="showEditor"
      :activity-id="selectedActivityId"
      @saved="handleJournalSaved"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import {
  completeActivity,
  deleteActivity,
  getActivityDetail,
  getEvaluationsByEvaluator,
  getMyDiaries,
  reviewParticipant,
  type UserEvaluationRecord,
} from '@/api/activity'
import DiaryEditor from '@/components/activity/DiaryEditor.vue'
import StartActivityModal from '@/components/activity/StartActivityModal.vue'
import { useUserStore } from '@/stores/user'
import type { ActivityDetailResponse, ActivityStatus } from '@/types/activity'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const completing = ref(false)
const showEditModal = ref(false)
const showEditor = ref(false)
const selectedActivityId = ref<number | undefined>()
const detail = ref<ActivityDetailResponse | null>(null)
const linkedDiaryId = ref<number | null>(null)
const reviewSectionEl = ref<HTMLElement | null>(null)
const reviewSpotlight = ref(false)
const ratingMap = ref<Record<number, number>>({})
const existingReviews = ref<Record<number, UserEvaluationRecord>>({})

const currentUserId = computed(() => userStore.userInfo?.id ?? null)
const isParticipant = computed(() =>
  detail.value?.participants.some((participant) => participant.id === currentUserId.value) ?? false,
)
const isCreator = computed(() => detail.value?.activity.creatorId === currentUserId.value)
const reviewableParticipants = computed(() =>
  detail.value?.participants.filter((participant) => participant.id !== currentUserId.value) ?? [],
)
const showReviewSection = computed(() =>
  detail.value?.activity.status === 2 && isParticipant.value && reviewableParticipants.value.length > 0,
)

const refreshDetail = async () => {
  const activityId = Number(route.params.activityId)
  if (!activityId) return

  loading.value = true
  try {
    detail.value = await getActivityDetail(activityId)
    await loadLinkedDiaryId(activityId)
    await loadExistingReviews(activityId)
  } finally {
    loading.value = false
  }
}

const loadLinkedDiaryId = async (activityId: number) => {
  try {
    const diaryResult = await getMyDiaries({ pageNum: 1, pageSize: 100 })
    const list = Array.isArray((diaryResult as any)?.content)
      ? (diaryResult as any).content
      : Array.isArray(diaryResult)
        ? diaryResult
        : []

    const matched = list.find((entry: any) => {
      const diary = entry?.diary ?? entry
      return diary?.activityId === activityId
    })

    linkedDiaryId.value = matched ? Number((matched.diary ?? matched).id) : null
  } catch {
    linkedDiaryId.value = null
  }
}

const completeCurrentActivity = async () => {
  if (!detail.value) return

  completing.value = true
  try {
    await completeActivity(detail.value.activity.id)
    await refreshDetail()
    await revealReviewSection()
    if (!detail.value?.hasJournal) {
      openDiaryEditor(detail.value.activity.id)
    }
  } finally {
    completing.value = false
  }
}

const goToJournal = () => {
  if (!linkedDiaryId.value) return
  router.push({ path: '/activity/journal', query: { diaryId: String(linkedDiaryId.value) } })
}

const openDiaryEditor = (activityId: number) => {
  selectedActivityId.value = activityId
  showEditor.value = true
}

const handleJournalSaved = async () => {
  showEditor.value = false
  await refreshDetail()
}

const loadExistingReviews = async (activityId: number) => {
  const evaluatorId = currentUserId.value
  ratingMap.value = {}
  if (!evaluatorId) {
    existingReviews.value = {}
    return
  }

  try {
    const evaluations = await getEvaluationsByEvaluator(evaluatorId)
    const reviewMap: Record<number, UserEvaluationRecord> = {}
    ;(evaluations ?? [])
      .filter((evaluation) => Number(evaluation.activityId) === activityId)
      .forEach((evaluation) => {
        reviewMap[evaluation.targetId] = evaluation
        ratingMap.value[evaluation.targetId] = evaluation.scoreLevel
      })
    existingReviews.value = reviewMap
  } catch {
    existingReviews.value = {}
  }
}

const handleRate = async (targetId: number, value: number) => {
  if (!detail.value || !value || isReviewed(targetId)) return

  try {
    await reviewParticipant(detail.value.activity.id, {
      revieweeId: targetId,
      rating: value,
      comment: 'Submitted from activity detail view',
    })
    existingReviews.value[targetId] = {
      id: Date.now(),
      evaluatorId: currentUserId.value ?? 0,
      targetId,
      activityId: detail.value.activity.id,
      scoreLevel: value,
      createTime: new Date().toISOString(),
    }
    ElMessage.success('Review submitted.')
  } catch {
    ratingMap.value[targetId] = 0
    ElMessage.error('Review submission failed.')
  }
}

const revealReviewSection = async () => {
  if (!showReviewSection.value) return

  await nextTick()
  reviewSpotlight.value = true
  reviewSectionEl.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  window.setTimeout(() => {
    reviewSpotlight.value = false
  }, 2200)
}

const removeCurrentActivity = async () => {
  if (!detail.value) return

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
    await deleteActivity(detail.value.activity.id)
    router.push('/activity/all')
  } catch {
    // Ignore cancel and let API interceptor handle real failures.
  }
}

const isReviewed = (targetId: number) => Boolean(existingReviews.value[targetId])

const reviewLabel = (value?: number) => {
  if (value === 1) return 'Bad'
  if (value === 2) return 'Okay'
  if (value === 3) return 'Great'
  return 'Tap Bad / Okay / Great'
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
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })

const fallbackAvatar = (seed: number) => `https://api.dicebear.com/7.x/avataaars/svg?seed=${seed}`

onMounted(refreshDetail)
</script>

<style scoped>
.detail-view {
  flex: 1;
  overflow-y: auto;
  padding: 28px;
}

.detail-shell {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.back-link {
  border: 0;
  background: transparent;
  color: var(--color-text-secondary);
  cursor: pointer;
}

.hero-card,
.info-card {
  border-radius: 24px;
  border: 1px solid var(--color-border);
  background:
    radial-gradient(circle at top left, rgba(0, 255, 149, 0.12), transparent 28%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.06), rgba(255, 255, 255, 0.02));
}

.hero-card {
  padding: 28px;
}

.hero-card h1 {
  margin: 0;
  font-size: 40px;
  line-height: 1.05;
  font-family: Righteous, var(--font-display), sans-serif;
}

.meta-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-top: 24px;
}

.meta-grid strong,
.meta-label {
  display: block;
}

.meta-label {
  margin-bottom: 8px;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  color: var(--color-text-secondary);
}

.content-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.info-card {
  padding: 22px;
}

.info-card h2 {
  margin: 0 0 12px;
  font-size: 20px;
}

.info-card p {
  margin: 0;
  color: var(--color-text-secondary);
  line-height: 1.7;
  white-space: pre-wrap;
}

.participants-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.participants-head h2 {
  margin-bottom: 0;
}

.participants-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.participant-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.participant-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.action-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0 24px;
}

.review-shell {
  padding: 22px 24px;
  border-radius: 24px;
  border: 1px solid var(--color-border);
  background:
    radial-gradient(circle at top left, rgba(0, 255, 149, 0.1), transparent 26%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.06), rgba(255, 255, 255, 0.02));
  transition: transform 0.35s ease, box-shadow 0.35s ease, border-color 0.35s ease;
}

.review-shell.spotlight {
  transform: translateY(-4px);
  border-color: rgba(0, 255, 149, 0.45);
  box-shadow: 0 20px 40px rgba(0, 255, 149, 0.12);
}

.section-header {
  border-left: 4px solid var(--color-green);
  padding-left: 12px;
  margin-bottom: 18px;
}

.section-title {
  display: block;
  font-size: 16px;
  font-weight: 700;
}

.section-desc {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: var(--color-text-secondary);
}

.participants-review-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 14px;
}

.review-card {
  padding: 16px;
  border-radius: 16px;
  background: var(--color-surface-1);
  border: 1px solid var(--color-border);
}

.review-card-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.review-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.review-meta strong {
  font-size: 14px;
}

.review-meta span {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.review-status-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 12px;
}

.review-scale {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.reviewed-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 24px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(0, 255, 149, 0.16);
  color: var(--color-green);
  font-size: 12px;
  font-weight: 700;
}

.status-copy {
  margin: 0;
  color: var(--color-text-secondary);
}

.status-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 30px;
  padding: 0 12px;
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
  background: rgba(91, 204, 96, 0.16);
  color: #8aff9b;
}

.badge-cancelled {
  background: rgba(255, 129, 129, 0.16);
  color: #ff9b9b;
}

@media (max-width: 1000px) {
  .content-grid {
    grid-template-columns: 1fr;
  }

  .meta-grid {
    grid-template-columns: 1fr;
  }
}
</style>
