<template>
  <div class="chat-page">
    <aside class="chat-sidebar">
      <div class="sidebar-header">
        <h2 class="sidebar-title">Chat with your partner</h2>
        <div class="sidebar-search-row">
          <div class="sidebar-search-wrap">
            <el-icon class="search-icon"><Search /></el-icon>
            <input v-model="searchQ" class="sidebar-search" placeholder="Search partner here">
          </div>
          <button class="add-btn" type="button" @click="openAddPartnerDialog">+</button>
        </div>
        <div class="sidebar-tabs">
          <button class="stab" :class="{ on: tab === 'partners' }" type="button" @click="tab = 'partners'">
            Partner({{ partners.length }})
          </button>
          <button
            class="stab badge"
            :class="{ on: tab === 'applications' }"
            :data-count="applications.length"
            type="button"
            @click="tab = 'applications'"
          >
            Application({{ applications.length }})
          </button>
        </div>
      </div>

      <div v-if="tab === 'partners'" class="sidebar-list">
        <div
          v-for="partner in filteredPartners"
          :key="partner.id"
          class="partner-item"
          :class="{ active: activeId === partner.id }"
          @click="selectPartner(partner)"
        >
          <img :src="partner.avatar" class="pi-avatar" :alt="partner.nickname">
          <div class="pi-info">
            <div class="pi-top">
              <span class="pi-name">{{ partner.nickname }}</span>
              <span class="pi-date">{{ partner.lastMsgDate }}</span>
            </div>
            <div class="pi-msg">{{ partner.lastMsg || EMPTY_LAST_MESSAGE }}</div>
          </div>
        </div>
        <div v-if="filteredPartners.length === 0" class="empty-tip">No partners found</div>
      </div>

      <div v-else class="sidebar-list">
        <div
          v-for="item in applications"
          :key="`${item.type}-${item.id}`"
          class="partner-item"
          :class="{ active: activeApplication?.id === item.id && activeApplication?.type === item.type }"
          @click="selectApplication(item)"
        >
          <img :src="item.fromUser.avatar" class="pi-avatar" :alt="item.fromUser.nickname">
          <div class="pi-info">
            <div class="pi-top">
              <span class="pi-name">{{ item.fromUser.nickname }}</span>
              <span class="app-kind">{{ item.type === 'activity' ? 'Activity' : 'Friend' }}</span>
            </div>
            <div class="pi-msg">{{ item.preview }}</div>
          </div>
        </div>
        <div v-if="applications.length === 0" class="empty-tip">No pending applications</div>
      </div>
    </aside>

    <main class="chat-main" v-show="tab === 'partners'">
      <div class="chat-bg" />

      <template v-if="activePartner">
        <div class="chat-header">
          <button class="chat-back" type="button" @click="activeId = null">‹</button>
          <span class="chat-partner-name">{{ activePartner.nickname }}</span>
        </div>

        <div class="chat-msgs" ref="msgsEl">
          <div
            v-for="(msg, index) in messages"
            :key="msg.id ?? index"
            class="msg-item"
            :class="msg.mine ? 'mine' : 'them'"
          >
            <div v-if="msg.timestamp" class="msg-timestamp">{{ msg.timestamp }}</div>
            <div class="msg-row" :class="msg.mine ? 'me' : 'them'">
              <img v-if="!msg.mine" :src="activePartner.avatar" class="msg-avatar">
              <div class="msg-bubble">{{ msg.content }}</div>
              <img v-if="msg.mine" :src="myAvatar" class="msg-avatar">
            </div>
          </div>
        </div>

        <div class="chat-input-area">
          <input
            v-model="inputText"
            class="chat-input"
            placeholder="type here."
            @keydown.enter="sendMessage"
          >
          <button
            type="button"
            class="more-btn emoji-trigger"
            :class="{ active: showEmojiPanel }"
            aria-label="Open emoji collection"
            @click="showEmojiPanel = !showEmojiPanel"
          >
            😊
          </button>
          <button type="button" class="send-btn" @click="sendMessage">Send</button>
          <div v-if="showEmojiPanel" class="emoji-panel">
            <button
              v-for="emoji in emojiCollection"
              :key="emoji"
              type="button"
              class="emoji-item"
              @click="insertEmoji(emoji)"
            >
              {{ emoji }}
            </button>
          </div>
        </div>
      </template>

      <div v-else class="chat-empty">
        <span class="chat-empty-icon">Chat</span>
        <p>Select a partner to start chatting</p>
      </div>
    </main>

    <aside class="chat-right" :class="{ expanded: tab === 'applications' }">
      <template v-if="tab === 'partners'">
        <div class="cr-header">
          <span class="cr-title">Activity detail</span>
          <el-icon><MoreFilled /></el-icon>
        </div>
        <div class="cr-calendar">
          <ActivityCalendar :event-days="calendarEventDays" />
        </div>
        <div v-if="activeActivity" class="cr-activity">
          <div class="act-entry-card" role="button" tabindex="0" @click="openActivityDetailCard">
            <img :src="activeActivity.coverImg" alt="activity journal">
            <div class="act-thumb-labels">
              <span class="atl-name">{{ activeActivity.title }}</span>
              <span class="atl-date">{{ activeActivity.date }}</span>
            </div>
          </div>
          <div class="cr-activity-intro">{{ activeActivity.intro }}</div>
          <div class="cr-activity-summary glass-card">
            <div class="cr-activity-summary-head">
              <span class="cr-activity-summary-kicker">Current activity</span>
              <span class="cr-activity-status">{{ activeActivity.statusLabel }}</span>
            </div>
            <p class="cr-activity-description">{{ activeActivity.description }}</p>
            <div class="cr-activity-grid">
              <div class="cr-activity-item">
                <span class="cr-activity-label">Location</span>
                <strong>{{ activeActivity.location }}</strong>
              </div>
              <div class="cr-activity-item">
                <span class="cr-activity-label">Schedule</span>
                <strong>{{ activeActivity.date }}</strong>
              </div>
              <div class="cr-activity-item">
                <span class="cr-activity-label">People</span>
                <strong>{{ activeActivity.participantCount }} / {{ activeActivity.maxParticipants }}</strong>
              </div>
              <div class="cr-activity-item">
                <span class="cr-activity-label">Partner</span>
                <strong>{{ activePartner?.nickname || 'Teammate' }}</strong>
              </div>
            </div>
            <button
              v-if="activeActivity.activityId || activeActivity.journalId"
              type="button"
              class="btn-outline cr-activity-action"
              @click="openActivityDetailCard"
            >
              {{ activeActivity.activityId ? 'View activity detail' : 'Open shared journal' }}
            </button>
          </div>
          <div class="cr-activity-meta">Participants: {{ activeActivity.participants.join(', ') }}</div>
        </div>
        <div v-else class="cr-empty">No active activity with this partner</div>
      </template>

      <template v-else-if="activeApplication">
        <div class="app-detail-scroll">
          <div class="app-profile-header glass-card">
            <img :src="activeApplication.fromUser.avatar" class="app-detail-avatar" :alt="activeApplication.fromUser.nickname">
            <div class="app-info-wrap">
              <div class="app-name-row">
                <h2 class="app-name">{{ activeApplication.fromUser.nickname }}</h2>
                <span class="app-kind large">{{ activeApplication.type === 'activity' ? 'Activity invitation' : 'Friend application' }}</span>
              </div>
              <p class="app-bio">{{ activeApplication.fromUser.bio || 'This partner has not added a bio yet.' }}</p>
              <div v-if="activeApplication.type === 'friend'" class="app-tags">
                <span v-for="tag in applicationDisplayTags" :key="tag.value" class="tag-chip active">
                  <span>{{ tag.emoji }}</span>{{ tag.label }}
                </span>
              </div>
            </div>
          </div>

          <div v-if="activeApplication.type === 'activity'" class="activity-invite-card glass-card">
            <div class="invite-label">You have been invited to</div>
            <h3>{{ activeApplication.activity?.title || 'Untitled activity' }}</h3>
            <div class="invite-meta">
              <span>{{ activeApplication.activity?.location || 'Location TBD' }}</span>
              <span>{{ formatMsgTime(activeApplication.activity?.activityTime) || 'Time TBD' }}</span>
            </div>
            <p>{{ activeApplication.activity?.description || 'No description yet.' }}</p>
            <button
              v-if="activeApplication.activity?.id"
              class="btn-outline"
              type="button"
              @click="router.push(`/activity/${activeApplication.activity?.id}`)"
            >
              View activity detail
            </button>
          </div>

          <div v-else class="app-profile-grid">
            <div class="app-section glass-card">
              <h3 class="app-section-title">Journal</h3>
              <div class="app-journal-wall">
                <div
                  v-for="journal in applicationPublicJournals"
                  :key="journal.id"
                  class="app-jw-thumb"
                  role="button"
                  tabindex="0"
                  @click="openApplicationJournal(journal)"
                  @keydown.enter.prevent="openApplicationJournal(journal)"
                  @keydown.space.prevent="openApplicationJournal(journal)"
                >
                  <img :src="journal.coverImage" :alt="journal.title">
                </div>
                <div v-if="applicationPublicJournals.length === 0" class="app-empty-tip">No journals yet</div>
              </div>
            </div>

            <div class="app-section glass-card">
              <h3 class="app-section-title">Activities</h3>
              <div class="app-act-list">
                <div v-for="activity in applicationRecentActivities" :key="activity.id" class="app-act-row">
                  <span class="app-act-icon">{{ activity.icon }}</span>
                  <div class="app-act-row-info">
                    <div class="app-act-row-name">{{ activity.name }}</div>
                  </div>
                  <span class="app-act-date">{{ activity.date }}</span>
                </div>
                <div v-if="applicationRecentActivities.length === 0" class="app-empty-tip">No activities yet</div>
              </div>
            </div>
          </div>
        </div>

        <div class="app-action-btns">
          <button class="btn-accept" type="button" @click="acceptApplication(activeApplication)">accept</button>
          <button class="btn-decline" type="button" @click="declineApplication(activeApplication)">decline</button>
        </div>
      </template>

      <div v-else class="cr-empty">Select an application to review</div>
    </aside>

    <el-dialog v-model="startActivityVisible" width="520px" :show-close="false" class="activity-dialog" align-center>
      <div class="activity-form-card">
        <button type="button" class="activity-close" @click="startActivityVisible = false">x</button>
        <div class="activity-form-title">Start activity</div>
        <p class="activity-form-subtitle">This will invite {{ activePartner?.nickname || 'your partner' }} and send a chat notification.</p>

        <div class="activity-form-grid">
          <label class="activity-field">
            <span>Name</span>
            <input v-model="activityDraft.name" class="activity-input" placeholder="e.g. Sunset ride">
          </label>
          <label class="activity-field">
            <span>Location</span>
            <input v-model="activityDraft.location" class="activity-input" placeholder="e.g. West Lake">
          </label>
          <label class="activity-field activity-field-full">
            <span>Description</span>
            <input v-model="activityDraft.description" class="activity-input" placeholder="What will you do together?">
          </label>
          <label class="activity-field">
            <span>Start Time</span>
            <input v-model="activityDraft.startTime" type="datetime-local" class="activity-input">
          </label>
          <label class="activity-field">
            <span>Max participants</span>
            <input v-model.number="activityDraft.maxParticipants" type="number" min="2" max="20" class="activity-input">
          </label>
        </div>

        <div class="activity-form-actions">
          <button type="button" class="btn-outline" @click="startActivityVisible = false">Cancel</button>
          <button type="button" class="btn-green" @click="confirmStartActivity">Send invitation</button>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="addPartnerVisible" width="460px" :show-close="false" class="activity-dialog" align-center>
      <div class="activity-form-card">
        <button type="button" class="activity-close" @click="addPartnerVisible = false">x</button>
        <div class="activity-form-title">Add partner</div>
        <p class="activity-form-subtitle">Enter a username to search and add a new partner.</p>

        <div class="activity-form-grid search-grid">
          <label class="activity-field">
            <span>Username</span>
            <input v-model="partnerSearchQ" class="activity-input" placeholder="e.g. rose" @keydown.enter="searchPartnerByUsername">
          </label>
          <button type="button" class="btn-green search-submit" @click="searchPartnerByUsername">Search</button>
        </div>

        <div v-if="partnerSearchLoading" class="cr-empty small">Searching...</div>
        <div v-else-if="searchedPartner" class="found-user-card" @click="addSearchedPartner">
          <img :src="searchedPartner.avatar" class="found-user-avatar" :alt="searchedPartner.nickname">
          <div class="found-user-info">
            <div class="found-user-name">{{ searchedPartner.nickname }}</div>
            <div class="found-user-meta">@{{ searchedPartner.username }}</div>
            <div class="found-user-bio">{{ searchedPartner.bio }}</div>
          </div>
          <button type="button" class="btn-outline" @click.stop="addSearchedPartner">Add</button>
        </div>
        <div v-else-if="partnerSearchDone" class="cr-empty small">No user found.</div>

        <div class="activity-form-actions">
          <button type="button" class="btn-outline" @click="addPartnerVisible = false">Close</button>
        </div>
      </div>
    </el-dialog>

    <el-dialog
      v-model="applicationJournalVisible"
      class="journal-card-dialog"
      width="520px"
      align-center
      :show-close="true"
      destroy-on-close
    >
      <article v-if="selectedApplicationJournal && selectedApplicationJournalOwner" class="shared-card mine app-journal-card">
        <div class="shared-card-head">
          <div class="shared-user">
            <img :src="selectedApplicationJournalOwner.avatar || '/default-avatar.png'" :alt="selectedApplicationJournalOwner.nickname" class="shared-avatar">
            <div class="shared-meta">
              <strong>{{ selectedApplicationJournalOwner.nickname }}</strong>
              <span>Shared card</span>
            </div>
          </div>
          <span v-if="selectedApplicationJournal.sharedEntryId" class="shared-updated">#{{ selectedApplicationJournal.sharedEntryId }}</span>
        </div>

        <div class="app-journal-title">{{ selectedApplicationJournal.title || 'Untitled Journal' }}</div>
        <p class="shared-text">{{ selectedApplicationJournal.excerpt || 'No journal entry yet.' }}</p>
        <div class="shared-images" v-if="selectedApplicationJournal.coverImage">
          <el-image
            :src="selectedApplicationJournal.coverImage"
            class="shared-image"
            :preview-src-list="[selectedApplicationJournal.coverImage]"
            fit="cover"
          />
        </div>
      </article>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { MoreFilled, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import ActivityCalendar from '@/components/activity/ActivityCalendar.vue'
import { createActivity, getActivityDetail, getActivityInvitations, getMyActivities, handleActivityInvitation } from '@/api/activity'
import { getChatConversations, getChatMessages, sendChatMessage } from '@/api/chat'
import type { ChatMessageDto } from '@/api/chat'
import {
  getFriendApplications,
  getFriends,
  getUserById,
  getUserByUsername,
  handleFriendRequest,
  sendFriendRequest,
} from '@/api/user'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import type { Activity, ActivityDetailResponse, ActivityInvitationPayload, ActivityStatus } from '@/types/activity'
import type { JournalThumb, NearbyUser } from '@/types/user'
import {
  activityStatusLabel,
  buildConversationMap,
  buildPartnerSummary,
  buildSharedActivityEntry,
  createPendingActivityEntry,
  EMPTY_LAST_MESSAGE,
  formatMsgTime,
  getApplicationDisplayTags,
  getLastCachedMessage,
  getLocalMessageStorageKey,
  mergeMessages,
  normalizeBackendMessages,
  normalizeUserShape,
  removeInvalidMessageKeys,
  type ActivityEntry,
  type MsgItem,
} from './chat-helpers'

const userStore = useUserStore()
const router = useRouter()
const myAvatar = computed(() => userStore.userInfo?.avatar || '/default-avatar.png')

const tab = ref<'partners' | 'applications'>('partners')
const searchQ = ref('')
const activeId = ref<number | null>(null)
const inputText = ref('')
const showEmojiPanel = ref(false)
const msgsEl = ref<HTMLElement | null>(null)
const startActivityVisible = ref(false)
const addPartnerVisible = ref(false)
const applicationJournalVisible = ref(false)
const partnerSearchQ = ref('')
const searchedPartner = ref<NearbyUser | null>(null)
const partnerSearchLoading = ref(false)
const partnerSearchDone = ref(false)
const selectedApplicationJournal = ref<JournalThumb | null>(null)
const selectedApplicationJournalOwner = ref<Pick<NearbyUser, 'nickname' | 'avatar'> | null>(null)

interface PartnerItem {
  id: number
  nickname: string
  avatar: string
  lastMsg: string
  lastMsgDate: string
  distance: number
  score: number
  publicJournals: NearbyUser['publicJournals']
  recentActivities: NearbyUser['recentActivities']
}

interface ApplicationItem {
  id: number
  type: 'friend' | 'activity'
  fromUser: NearbyUser
  toUserId: number
  status: 'pending' | 'accepted' | 'declined' | 'cancelled' | 'expired'
  createdAt: string
  preview: string
  activity?: Activity | null
}

const partners = ref<PartnerItem[]>([])
const applications = ref<ApplicationItem[]>([])
const activeApplication = ref<ApplicationItem | null>(null)
const msgMap = ref<Record<number, MsgItem[]>>({})
const partnerActivityOverrides = ref<Record<number, ActivityEntry>>({})
const sharedPartnerActivities = ref<Record<number, ActivityEntry>>({})
let messagePollTimer: number | null = null

const activityDraft = reactive({
  name: '',
  location: '',
  description: '',
  startTime: '',
  maxParticipants: 2,
})

const emojiCollection = [
  '😀', '😂', '🥰', '😎', '😭', '😴', '🥳', '🤔',
  '👍', '👏', '🙏', '💪', '🔥', '✨', '🌟', '💚',
  '🍻', '☕', '🎵', '🎮', '🏀', '🚲', '📚', '🌿',
]

const activePartner = computed(() => partners.value.find((partner) => partner.id === activeId.value) ?? null)
const messages = computed(() => (activeId.value ? msgMap.value[activeId.value] ?? [] : []))
const filteredPartners = computed(() =>
  partners.value.filter((partner) => partner.nickname.toLowerCase().includes(searchQ.value.toLowerCase())),
)
const applicationPublicJournals = computed(() => activeApplication.value?.fromUser.publicJournals ?? [])
const applicationRecentActivities = computed(() => activeApplication.value?.fromUser.recentActivities ?? [])
const applicationDisplayTags = computed(() => {
  const tags = activeApplication.value?.fromUser.tags as unknown
  const normalizedTags = Array.isArray(tags)
    ? tags
    : typeof tags === 'string'
      ? tags.split(/(?:[;,]+|，|、)/)
      : []

  return normalizedTags
    .map(tag => String(tag).trim())
    .filter(Boolean)
    .map(getTagMeta)
})

const activeActivity = computed<ActivityEntry | null>(() => {
  const partner = activePartner.value
  if (!partner) return null

  const sharedActivity = sharedPartnerActivities.value[partner.id]
  if (sharedActivity) return sharedActivity

  const override = partnerActivityOverrides.value[partner.id]
  if (override) return override

  const journal = partner.publicJournals?.[0]
  const activity = partner.recentActivities?.[0]
  if (!journal || !activity) return null

  const myName = userStore.userInfo?.nickname || 'Me'
  return {
    journalId: journal.id,
    title: journal.title || activity.name || `${partner.nickname}'s shared journal`,
    date: activity.date,
    scheduledAt: activity.date,
    coverImg: journal.coverImage,
    intro: `A shared activity journal created by ${myName} and ${partner.nickname}.`,
    participants: [myName, partner.nickname],
    participantIds: [Number(userStore.userInfo?.id ?? 0), partner.id].filter(Boolean),
    location: 'Shared journal',
    statusLabel: 'Shared',
    participantCount: 2,
    maxParticipants: 2,
    description: 'Open the shared journal to revisit what you and your partner have done together.',
  }
})

const calendarEventDays = computed(() => {
  const value = activeActivity.value?.scheduledAt
  if (!value) return []
  const parsed = new Date(value)
  return Number.isNaN(parsed.getTime()) ? [] : [parsed.getDate()]
})

const ensureCurrentUser = async () => {
  if (!userStore.userInfo && userStore.isLoggedIn) {
    try {
      await userStore.fetchUserInfo()
    } catch {
      // Keep the page usable when profile loading fails.
    }
  }
}

const normalizeUserShape = (raw: Record<string, unknown>): NearbyUser => {
  const tagsRaw = raw?.tags
  const tags = Array.isArray(tagsRaw)
    ? tagsRaw.map((value) => String(value)).filter(Boolean)
    : typeof tagsRaw === 'string'
      ? tagsRaw.split(',').map((value) => value.trim()).filter(Boolean)
      : []

  return {
    id: Number(raw?.id ?? 0),
    username: String(raw?.username ?? ''),
    nickname: String(raw?.nickname ?? (raw?.id ? `User ${raw.id}` : 'Unknown user')),
    avatar: String(raw?.avatar ?? '/default-avatar.png'),
    bio: String(raw?.bio ?? ''),
    tags,
    activities: Number(raw?.activities ?? 0),
    score: Number(raw?.score ?? raw?.averageScore ?? 0),
    ranking: Number(raw?.ranking ?? 0),
    longitude: raw?.longitude as number | undefined,
    latitude: raw?.latitude as number | undefined,
    createdAt: String(raw?.createdAt ?? raw?.createTime ?? ''),
    distance: Number(raw?.distance ?? 0),
    publicJournals: Array.isArray(raw?.publicJournals) ? raw.publicJournals as NearbyUser['publicJournals'] : [],
    recentActivities: Array.isArray(raw?.recentActivities) ? raw.recentActivities as NearbyUser['recentActivities'] : [],
  }
}

const toNearbyUser = async (userId: number, fallback?: Record<string, unknown> | null) => {
  let rawUser: Record<string, unknown> = fallback ?? {}
  try {
    rawUser = await getUserById(userId) as unknown as Record<string, unknown>
  } catch {
    rawUser = fallback ?? { id: userId }
  }
  return normalizeUserShape({ ...rawUser, id: rawUser.id ?? userId })
}

const formatMsgTime = (value: unknown) => {
  if (!value) return ''
  const date = new Date(String(value))
  if (Number.isNaN(date.getTime())) return ''
  return date.toLocaleString()
}

const activityStatusLabel = (status: ActivityStatus | number | undefined) => {
  const map: Record<number, string> = {
    0: 'Pending',
    1: 'Active',
    2: 'Completed',
    3: 'Cancelled',
  }
  return map[Number(status ?? 1)] ?? 'Active'
}

const buildSharedActivityEntry = (activity: Activity, detail: ActivityDetailResponse): ActivityEntry => {
  const participantIds = (detail.participants ?? []).map((participant) => Number(participant.id)).filter(Boolean)
  const participantNames = (detail.participants ?? []).map((participant) => participant.nickname).filter(Boolean)
  const currentPartnerId = participantIds.find((id) => id !== Number(userStore.userInfo?.id ?? 0))
  const currentPartner = partners.value.find((partner) => partner.id === currentPartnerId)
  const partnerName = currentPartner?.nickname || participantNames.find((name) => name !== userStore.userInfo?.nickname) || 'your partner'

  return {
    activityId: activity.id,
    title: activity.title || `${partnerName}'s activity`,
    date: formatMsgTime(activity.activityTime) || 'Time TBD',
    scheduledAt: activity.activityTime,
    coverImg: currentPartner?.publicJournals?.[0]?.coverImage || currentPartner?.avatar || '/default-avatar.png',
    intro: `You and ${partnerName} are currently doing this activity together.`,
    participants: participantNames,
    participantIds,
    location: activity.location || 'Location TBD',
    statusLabel: activityStatusLabel(activity.status),
    participantCount: detail.participantCount ?? participantIds.length,
    maxParticipants: activity.maxParticipants || detail.participantCount || participantIds.length || 2,
    description: activity.description?.trim() || 'No detailed introduction has been added for this activity yet.',
  }
}

const refreshSharedActivities = async () => {
  try {
    const grouped = await getMyActivities()
    const activeActivities = [...(grouped.active ?? [])].sort(
      (left, right) => new Date(right.activityTime).getTime() - new Date(left.activityTime).getTime(),
    )

    const details = await Promise.allSettled(
      activeActivities.map(async (activity) => ({
        activity,
        detail: await getActivityDetail(activity.id),
      })),
    )

    const nextMap: Record<number, ActivityEntry> = {}
    details.forEach((entry) => {
      if (entry.status !== 'fulfilled') return
      const activityEntry = buildSharedActivityEntry(entry.value.activity, entry.value.detail)
      const partnerIds = activityEntry.participantIds.filter((id) => id !== Number(userStore.userInfo?.id ?? 0))
      partnerIds.forEach((partnerId) => {
        if (!nextMap[partnerId]) nextMap[partnerId] = activityEntry
      })
    })
    sharedPartnerActivities.value = nextMap
  } catch {
    sharedPartnerActivities.value = {}
  }
}

const getLocalMessages = (partnerId: number): MsgItem[] => {
  try {
    const userId = userStore.userInfo?.id
    if (!userId) return []
    const stored = localStorage.getItem(`chat_messages_${userId}_${partnerId}`)
    return stored ? JSON.parse(stored) : []
  } catch {
    return []
  }
}

const saveLocalMessages = (partnerId: number, messagesToSave: MsgItem[]) => {
  try {
    const userId = userStore.userInfo?.id
    if (!userId) return
    localStorage.setItem(`chat_messages_${userId}_${partnerId}`, JSON.stringify(messagesToSave))
  } catch {
    // Ignore local storage failures.
  }
}

const normalizeBackendMessages = (list: ChatMessageDto[]) =>
  list
    .slice()
    .sort((a, b) => new Date(a.createTime).getTime() - new Date(b.createTime).getTime())
    .map((item) => ({
      id: item.id,
      content: String(item.content ?? ''),
      mine: Number(item.senderId) === Number(userStore.userInfo?.id),
      timestamp: formatMsgTime(item.createTime),
    }))

const getLastCachedMessage = (partnerId: number) => {
  const localMessages = getLocalMessages(partnerId)
  return localMessages[localMessages.length - 1]
}

const preloadPartnerLastMessages = async () => {
  const partnersWithoutSummary = partners.value.filter((partner) => partner.lastMsg === EMPTY_LAST_MESSAGE)

  await Promise.allSettled(partnersWithoutSummary.map(async (partner) => {
    const data = await getChatMessages(partner.id, 1, 1000)
    const list = Array.isArray(data?.list) ? data.list : []
    const backendMessages = normalizeBackendMessages(list)
    if (backendMessages.length === 0) return

    const localMessages = getLocalMessages(partner.id)
    const merged = [...localMessages]
    backendMessages.forEach((message) => {
      if (!merged.some((existing) => existing.id === message.id)) merged.push(message)
    })

    msgMap.value[partner.id] = merged
    saveLocalMessages(partner.id, merged)
    updatePartnerLastMessage(partner.id, merged)
  }))
}

const loadPartners = async () => {
  try {
    const [friendData, conversationData] = await Promise.all([
      getFriends() as unknown as Array<Record<string, unknown>>,
      getChatConversations().catch(() => []),
    ])

    const conversationMap = new Map<number, { lastMessage: string; lastMessageTime: string }>()
    for (const item of conversationData as Array<Record<string, unknown>>) {
      const userId = Number(item.userId ?? 0)
      if (!userId) continue
      conversationMap.set(userId, {
        lastMessage: String(item.lastMessage ?? ''),
        lastMessageTime: String(item.lastMessageTime ?? ''),
      })
    }

    partners.value = await Promise.all(friendData.map(async (friend) => {
      const id = Number(friend.id ?? 0)
      const detail = await toNearbyUser(id, friend)
      const conversation = conversationMap.get(id)
      const cachedMessage = getLastCachedMessage(id)
      return {
        id,
        nickname: detail.nickname,
        avatar: detail.avatar || '/default-avatar.png',
        lastMsg: conversation?.lastMessage || cachedMessage?.content || EMPTY_LAST_MESSAGE,
        lastMsgDate: conversation?.lastMessageTime ? formatMsgTime(conversation.lastMessageTime) : cachedMessage?.timestamp || '',
        distance: detail.distance,
        score: detail.score,
        publicJournals: detail.publicJournals,
        recentActivities: detail.recentActivities,
      }
    }))
    await preloadPartnerLastMessages()
    await refreshSharedActivities()
  } catch {
    partners.value = []
    sharedPartnerActivities.value = {}
  }
}

const mapFriendApplications = async () => {
  const data = await getFriendApplications() as {
    incoming?: Array<{ id: number; senderId: number; receiverId: number; status?: string; createTime?: string }>
  } | ApplicationItem[]

  if (Array.isArray(data)) {
    return data
      .filter((item) => item?.fromUser && item.status === 'pending')
      .map((item) => ({ ...item, type: 'friend' as const, preview: 'Friend request' }))
  }

  const incoming = (data?.incoming ?? []).filter((request) => (request.status ?? 'pending') === 'pending')
  return Promise.all(incoming.map(async (request) => ({
    id: request.id,
    type: 'friend' as const,
    fromUser: await toNearbyUser(request.senderId),
    toUserId: request.receiverId,
    status: 'pending' as const,
    createdAt: request.createTime ?? '',
    preview: 'Wants to become your partner',
  })))
}

const mapActivityInvitations = async () => {
  const data = await getActivityInvitations()
  const incoming = (data.incoming ?? []).filter((item) => item.invitation?.status === 'pending')
  return Promise.all(incoming.map(async (payload: ActivityInvitationPayload) => {
    const senderId = Number(payload.sender?.id ?? payload.invitation.senderId)
    const sender = await toNearbyUser(senderId, payload.sender as unknown as Record<string, unknown>)
    return {
      id: payload.invitation.id,
      type: 'activity' as const,
      fromUser: sender,
      toUserId: payload.invitation.receiverId,
      status: payload.invitation.status,
      createdAt: payload.invitation.createTime,
      preview: `Invited you to ${payload.activity?.title || 'an activity'}`,
      activity: payload.activity,
    }
  }))
}

const loadApplications = async () => {
  try {
    const [friendApps, activityApps] = await Promise.all([
      mapFriendApplications().catch(() => []),
      mapActivityInvitations().catch(() => []),
    ])
    applications.value = [...activityApps, ...friendApps]
  } catch {
    applications.value = []
  }
}

const loadMessages = async (partnerId: number) => {
  try {
    await ensureCurrentUser()
    const localMessages = getLocalMessages(partnerId)
    const data = await getChatMessages(partnerId, 1, 100)
    const list = Array.isArray(data?.list) ? data.list : []
    const backendMessages = normalizeBackendMessages(list)

    const merged = [...localMessages]
    backendMessages.forEach((message) => {
      if (!merged.some((existing) => existing.id === message.id)) merged.push(message)
    })

    msgMap.value[partnerId] = merged
    saveLocalMessages(partnerId, merged)
    updatePartnerLastMessage(partnerId, merged)

    await nextTick()
    if (msgsEl.value) msgsEl.value.scrollTop = msgsEl.value.scrollHeight
  } catch {
    msgMap.value[partnerId] = getLocalMessages(partnerId)
    updatePartnerLastMessage(partnerId, msgMap.value[partnerId])
  }
}

const updatePartnerLastMessage = (partnerId: number, partnerMessages: MsgItem[]) => {
  const lastMessage = partnerMessages[partnerMessages.length - 1]
  if (!lastMessage) return
  const partner = partners.value.find((item) => item.id === partnerId)
  if (!partner) return
  partner.lastMsg = lastMessage.content
  partner.lastMsgDate = lastMessage.timestamp || ''
}

const stopMessagePolling = () => {
  if (messagePollTimer !== null) {
    window.clearInterval(messagePollTimer)
    messagePollTimer = null
  }
}

const startMessagePolling = (partnerId: number) => {
  stopMessagePolling()
  messagePollTimer = window.setInterval(() => {
    if (tab.value === 'partners' && activeId.value === partnerId) loadMessages(partnerId)
  }, 3000)
}

const selectPartner = (partner: PartnerItem) => {
  activeId.value = partner.id
  activeApplication.value = null
  loadMessages(partner.id)
  startMessagePolling(partner.id)
}

const selectApplication = (application: ApplicationItem) => {
  activeId.value = application.fromUser.id
  activeApplication.value = application
  stopMessagePolling()
}

const sendMessage = async () => {
  const content = inputText.value.trim()
  if (!content || !activeId.value) return

  const targetId = activeId.value
  try {
    await sendChatMessage(targetId, content)
    inputText.value = ''
    showEmojiPanel.value = false
    await loadMessages(targetId)
    await loadPartners()
  } catch {
    const tempMessage: MsgItem = {
      id: `temp_${Date.now()}`,
      content,
      mine: true,
      timestamp: formatMsgTime(new Date().toISOString()),
    }
    msgMap.value[targetId] = [...(msgMap.value[targetId] ?? []), tempMessage]
    saveLocalMessages(targetId, msgMap.value[targetId])
    updatePartnerLastMessage(targetId, msgMap.value[targetId])
    inputText.value = ''
    showEmojiPanel.value = false
  }
}

const acceptApplication = async (application: ApplicationItem) => {
  try {
    if (application.type === 'activity') {
      await handleActivityInvitation(application.id, true)
      ElMessage.success('Activity invitation accepted.')
      await refreshSharedActivities()
    } else {
      await handleFriendRequest(application.id, true)
      ElMessage.success(`${application.fromUser.nickname} is now your partner!`)
      await loadPartners()
    }

    applications.value = applications.value.filter((item) => !(item.id === application.id && item.type === application.type))
    activeApplication.value = null
    await loadApplications()
  } catch {
    // API interceptor already shows the error.
  }
}

const declineApplication = async (application: ApplicationItem) => {
  try {
    if (application.type === 'activity') {
      await handleActivityInvitation(application.id, false)
      ElMessage.info('Activity invitation declined.')
    } else {
      await handleFriendRequest(application.id, false)
      ElMessage.info('Application declined.')
    }

    applications.value = applications.value.filter((item) => !(item.id === application.id && item.type === application.type))
    activeApplication.value = null
    await loadApplications()
  } catch {
    // API interceptor already shows the error.
  }
}

const insertEmoji = (emoji: string) => {
  inputText.value = `${inputText.value}${emoji}`
  showEmojiPanel.value = false
}

const resetActivityDraft = () => {
  activityDraft.name = ''
  activityDraft.location = ''
  activityDraft.description = ''
  activityDraft.startTime = ''
  activityDraft.maxParticipants = 2
}

const confirmStartActivity = async () => {
  if (!activePartner.value) return

  const title = activityDraft.name.trim()
  const location = activityDraft.location.trim()
  const description = activityDraft.description.trim()
  if (!title || !location || !activityDraft.startTime) {
    ElMessage.warning('Please fill in Name, Location, and Start Time')
    return
  }

  const startedAt = new Date(activityDraft.startTime)
  if (Number.isNaN(startedAt.getTime())) {
    ElMessage.warning('Please choose a valid Start Time')
    return
  }

  try {
    const activity = await createActivity({
      title,
      location,
      description,
      plan: '',
      activityTime: activityDraft.startTime,
      maxParticipants: Number(activityDraft.maxParticipants || 2),
      inviteeId: activePartner.value.id,
    })

    partnerActivityOverrides.value[activePartner.value.id] = {
      activityId: activity.id,
      title,
      date: formatMsgTime(activity.activityTime || activityDraft.startTime),
      scheduledAt: activity.activityTime || activityDraft.startTime,
      coverImg: activePartner.value.publicJournals?.[0]?.coverImage || activePartner.value.avatar,
      intro: `${title} is waiting for ${activePartner.value.nickname} to accept.`,
      participants: [userStore.userInfo?.nickname || 'Me', activePartner.value.nickname],
      participantIds: [Number(userStore.userInfo?.id ?? 0), activePartner.value.id].filter(Boolean),
      location,
      statusLabel: 'Pending',
      participantCount: 1,
      maxParticipants: Number(activityDraft.maxParticipants || 2),
      description: description || 'Waiting for your partner to accept this activity invitation.',
    }

    startActivityVisible.value = false
    resetActivityDraft()
    ElMessage.success('Activity invitation sent.')
    await loadMessages(activePartner.value.id)
  } catch {
    // API interceptor already shows the error.
  }
}

const openActivityDetailCard = () => {
  if (activeActivity.value?.activityId) {
    router.push(`/activity/${activeActivity.value.activityId}`)
    return
  }
  if (!activeActivity.value?.journalId) return
  router.push({ path: '/activity/journal', query: { diaryId: String(activeActivity.value.journalId) } })
}

const openApplicationJournal = (journal: JournalThumb) => {
  if (!activeApplication.value) return
  selectedApplicationJournal.value = journal
  selectedApplicationJournalOwner.value = {
    nickname: activeApplication.value.fromUser.nickname,
    avatar: activeApplication.value.fromUser.avatar,
  }
  applicationJournalVisible.value = true
}

const openAddPartnerDialog = () => {
  partnerSearchQ.value = ''
  searchedPartner.value = null
  partnerSearchDone.value = false
  addPartnerVisible.value = true
}

const searchPartnerByUsername = async () => {
  const username = partnerSearchQ.value.trim()
  if (!username) {
    ElMessage.warning('Please enter a username')
    return
  }

  partnerSearchLoading.value = true
  partnerSearchDone.value = false
  searchedPartner.value = null
  try {
    const raw = await getUserByUsername(username) as unknown as Record<string, unknown>
    const candidate = normalizeUserShape(raw)
    if (!candidate.id) {
      partnerSearchDone.value = true
      return
    }
    if (candidate.id === userStore.userInfo?.id) {
      ElMessage.warning('You cannot add yourself')
      return
    }
    searchedPartner.value = candidate
    partnerSearchDone.value = true
  } catch {
    partnerSearchDone.value = true
  } finally {
    partnerSearchLoading.value = false
  }
}

const addSearchedPartner = async () => {
  if (!searchedPartner.value) return
  if (partners.value.some((partner) => partner.id === searchedPartner.value?.id)) {
    ElMessage.info('This user is already in your partner list')
    return
  }

  try {
    await sendFriendRequest(searchedPartner.value.id)
    ElMessage.success(`Friend request sent to ${searchedPartner.value.nickname}`)
    addPartnerVisible.value = false
    await loadApplications()
  } catch {
    // API interceptor already shows the error.
  }
}

const cleanInvalidLocalMessages = () => {
  try {
    const userId = userStore.userInfo?.id
    if (!userId) return
    const currentPartnerIds = new Set(partners.value.map((partner) => partner.id))
    for (let index = 0; index < localStorage.length; index++) {
      const key = localStorage.key(index)
      if (key?.startsWith(`chat_messages_${userId}_`)) {
        const partnerId = Number(key.replace(`chat_messages_${userId}_`, ''))
        if (!currentPartnerIds.has(partnerId)) localStorage.removeItem(key)
      }
    }
  } catch {
    // Ignore local storage failures.
  }
}

onMounted(async () => {
  await ensureCurrentUser()
  await Promise.all([loadPartners(), loadApplications()])
  cleanInvalidLocalMessages()
})

watch(tab, (nextTab) => {
  if (nextTab !== 'partners') {
    stopMessagePolling()
    return
  }
  if (activeId.value) {
    loadMessages(activeId.value)
    startMessagePolling(activeId.value)
  }
})

onBeforeUnmount(stopMessagePolling)
</script>

<style scoped>
.chat-page {
  display: flex;
  height: 100vh;
  padding-top: var(--nav-height);
  box-sizing: border-box;
  position: relative;
  z-index: 1;
  overflow: hidden;
}

.chat-sidebar {
  width: var(--sidebar-width);
  flex-shrink: 0;
  background: rgba(0, 0, 0, 0.65);
  border-right: 0.5px solid var(--color-border-dim);
  display: flex;
  flex-direction: column;
  backdrop-filter: blur(14px);
}

.sidebar-header {
  padding: 20px 20px 0;
}

.sidebar-title {
  font-family: var(--font-display);
  font-size: 17px;
  margin-bottom: 14px;
}

.sidebar-search-row {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 12px;
}

.sidebar-search-wrap {
  position: relative;
  flex: 1;
}

.search-icon {
  position: absolute;
  left: 11px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--color-text-secondary);
  font-size: 13px;
}

.sidebar-search {
  width: 100%;
  padding: 9px 14px 9px 32px;
  background: var(--color-surface-2);
  border: 0.5px solid var(--color-border);
  border-radius: var(--radius-full);
  color: var(--color-white);
  font-size: 13px;
}

.add-btn {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: transparent;
  border: 2px solid var(--color-green);
  color: var(--color-green);
  font-size: 18px;
  cursor: pointer;
  flex-shrink: 0;
}

.sidebar-tabs {
  display: flex;
  gap: 8px;
  padding-bottom: 12px;
  border-bottom: 0.5px solid var(--color-border-dim);
}

.stab {
  padding: 7px 16px;
  border-radius: var(--radius-full);
  font-size: 12px;
  background: transparent;
  border: 1px solid var(--color-border);
  color: var(--color-text-secondary);
  cursor: pointer;
  position: relative;
}

.stab.on {
  background: var(--color-black);
  border-color: var(--color-white);
  color: var(--color-white);
}

.stab.badge::after {
  content: attr(data-count);
  position: absolute;
  top: -5px;
  right: -5px;
  background: var(--color-green);
  color: var(--color-black);
  font-size: 9px;
  font-weight: 700;
  padding: 1px 5px;
  border-radius: 8px;
  min-width: 16px;
  text-align: center;
}

.sidebar-list {
  flex: 1;
  overflow-y: auto;
}

.partner-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  cursor: pointer;
  transition: background 0.15s;
  border-bottom: 0.5px solid var(--color-border-dim);
}

.partner-item:hover,
.partner-item.active {
  background: var(--color-surface-1);
}

.partner-item.active {
  border-left: 3px solid var(--color-green);
}

.pi-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
  border: 1.5px solid var(--color-border);
}

.pi-info {
  flex: 1;
  min-width: 0;
}

.pi-top {
  display: flex;
  justify-content: space-between;
  gap: 10px;
}

.pi-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-green);
}

.pi-date,
.app-kind {
  font-size: 11px;
  color: var(--color-text-secondary);
}

.app-kind.large {
  padding: 4px 9px;
  border: 1px solid var(--color-border);
  border-radius: 999px;
}

.pi-msg {
  font-size: 12px;
  color: var(--color-text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-top: 5px;
}

.empty-tip {
  padding: 24px;
  text-align: center;
  font-size: 13px;
  color: var(--color-text-hint);
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
}

.chat-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
  background: url('https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?w=1200&q=50') center/cover;
  opacity: 0.3;
}

.chat-header,
.chat-input-area {
  position: relative;
  z-index: 2;
  background: rgba(0, 0, 0, 0.65);
  backdrop-filter: blur(8px);
  border-bottom: 0.5px solid var(--color-border-dim);
}

.chat-header {
  padding: 14px 20px;
  display: flex;
  align-items: center;
  gap: 14px;
}

.chat-back {
  background: none;
  border: none;
  color: var(--color-white);
  font-size: 22px;
  cursor: pointer;
}

.chat-partner-name {
  font-family: var(--font-display);
  font-size: 20px;
  flex: 1;
  text-align: center;
}

.chat-msgs {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: relative;
  z-index: 2;
}

.msg-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.msg-item.mine {
  align-items: flex-end;
}

.msg-item.them {
  align-items: flex-start;
}

.msg-timestamp {
  text-align: center;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.45);
  background: rgba(0, 0, 0, 0.4);
  border-radius: 8px;
  padding: 3px 10px;
  align-self: center;
}

.msg-row {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  max-width: 72%;
}

.msg-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
}

.msg-bubble {
  padding: 11px 15px;
  border-radius: 18px;
  font-size: 14px;
  line-height: 1.55;
}

.msg-row.them .msg-bubble {
  background: rgba(255, 255, 255, 0.92);
  color: #000;
  border-bottom-left-radius: 4px;
}

.msg-row.me .msg-bubble {
  background: rgba(0, 0, 0, 0.75);
  color: var(--color-white);
  border: 1px solid var(--color-green-border);
  border-bottom-right-radius: 4px;
}

.chat-input-area {
  padding: 13px 18px;
  display: flex;
  align-items: center;
  gap: 10px;
  border-top: 0.5px solid var(--color-border-dim);
  border-bottom: 0;
}

.chat-input {
  flex: 1;
  padding: 11px 16px;
  border-radius: var(--radius-full);
  background: var(--color-surface-2);
  border: 0.5px solid var(--color-border);
  color: var(--color-white);
  font-size: 14px;
}

.more-btn,
.send-btn {
  height: 34px;
  border: none;
  cursor: pointer;
}

.more-btn {
  width: 34px;
  border-radius: 50%;
  background: transparent;
  border: 2px solid rgba(255, 255, 255, 0.45);
  color: var(--color-white);
  font-size: 18px;
}

.more-btn.active,
.more-btn:hover {
  border-color: var(--color-green-border);
  background: rgba(0, 255, 149, 0.12);
}

.send-btn,
.btn-green,
.btn-accept {
  padding: 0 16px;
  border-radius: 999px;
  background: var(--color-green);
  color: var(--color-black);
  font-size: 13px;
  font-weight: 700;
  border: none;
  cursor: pointer;
}

.emoji-panel {
  position: absolute;
  bottom: calc(100% + 8px);
  right: 86px;
  width: 240px;
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 6px;
  padding: 10px;
  background: rgba(12, 14, 16, 0.96);
  border: 0.5px solid var(--color-border);
  border-radius: 18px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(14px);
}

.emoji-item {
  width: 100%;
  aspect-ratio: 1;
  border: none;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.06);
  font-size: 18px;
  cursor: pointer;
  transition: transform 0.15s ease, background 0.15s ease;
}

.emoji-item:hover {
  transform: translateY(-1px) scale(1.08);
  background: rgba(0, 255, 149, 0.18);
}

.chat-empty,
.cr-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--color-text-hint);
  padding: 24px;
  text-align: center;
}

.chat-empty {
  position: relative;
  z-index: 2;
}

.chat-empty-icon {
  font-family: var(--font-display);
  font-size: 32px;
}

.chat-right {
  width: 300px;
  flex-shrink: 0;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(14px);
  border-left: 0.5px solid var(--color-border-dim);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-right.expanded {
  width: 100%;
  flex: 1;
}

.cr-header {
  padding: 15px 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 0.5px solid var(--color-border-dim);
}

.cr-title {
  font-family: var(--font-display);
  font-size: 16px;
}

.cr-calendar {
  border-bottom: 0.5px solid var(--color-border-dim);
}

.cr-activity,
.app-detail-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.act-entry-card {
  width: 100%;
  border-radius: var(--radius-md);
  overflow: hidden;
  position: relative;
  background: #333;
  cursor: pointer;
}

.act-entry-card img {
  width: 100%;
  height: 110px;
  object-fit: cover;
  display: block;
}

.act-thumb-labels {
  position: absolute;
  bottom: 8px;
  left: 10px;
  right: 10px;
  display: flex;
  justify-content: space-between;
}

.atl-name,
.atl-date {
  font-size: 11px;
  background: rgba(0, 0, 0, 0.6);
  padding: 2px 7px;
  border-radius: 6px;
}

.cr-activity-intro,
.cr-activity-meta {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.86);
  line-height: 1.6;
}

.cr-activity-summary {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.cr-activity-summary-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.cr-activity-summary-kicker,
.cr-activity-label {
  font-size: 11px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--color-text-secondary);
}

.cr-activity-status {
  padding: 5px 10px;
  border-radius: 999px;
  background: rgba(0, 255, 149, 0.14);
  color: var(--color-green);
  font-size: 11px;
  font-weight: 700;
}

.cr-activity-description {
  margin: 0;
  font-size: 13px;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.9);
}

.cr-activity-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.cr-activity-item {
  padding: 12px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.04);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.cr-activity-item strong {
  font-size: 13px;
  line-height: 1.5;
  color: var(--color-text);
}

.cr-activity-action {
  width: 100%;
}

.cr-activity-meta {
  font-size: 11px;
  color: var(--color-text-secondary);
}

.glass-card {
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid var(--color-border);
  border-radius: 18px;
}

.app-profile-header {
  display: flex;
  gap: 14px;
  align-items: flex-start;
  padding: 22px 24px;
}

.app-detail-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid var(--color-border);
}

.app-info-wrap {
  flex: 1;
  min-width: 0;
}

.app-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 4px;
}

.app-name {
  font-family: var(--font-display);
  font-size: 22px;
  margin: 0;
}

.app-bio,
.invite-meta,
.activity-invite-card p {
  font-size: 13px;
  color: var(--color-text-secondary);
  line-height: 1.5;
}

.app-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.app-profile-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.app-section,
.activity-invite-card {
  padding: 22px;
}

.app-section-title,
.activity-invite-card h3 {
  font-family: var(--font-display);
  font-size: 18px;
  margin: 0 0 14px;
}

.invite-label {
  color: var(--color-green);
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  margin-bottom: 8px;
}

.invite-meta {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.app-journal-wall {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 7px;
}

.app-jw-thumb {
  border-radius: 8px;
  overflow: hidden;
  background: #333;
  aspect-ratio: 1;
  cursor: pointer;
  border: 1px solid transparent;
  transition: border-color 0.2s, transform 0.2s;
}

.app-jw-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.2s;
}

.app-jw-thumb:hover,
.app-jw-thumb:focus-visible {
  border-color: rgba(0, 255, 149, 0.45);
  transform: translateY(-1px);
  outline: none;
}

.app-jw-thumb:hover img,
.app-jw-thumb:focus-visible img {
  transform: scale(1.06);
}

.app-act-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.app-act-row {
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 0.5px solid var(--color-border-dim);
  padding-bottom: 12px;
}

.app-act-row-info {
  flex: 1;
}

.app-act-date,
.app-empty-tip {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.app-action-btns {
  display: flex;
  gap: 10px;
  padding: 14px 16px;
  border-top: 0.5px solid var(--color-border-dim);
}

.btn-accept,
.btn-decline {
  flex: 1;
  padding: 10px;
  border-radius: var(--radius-md);
  font-size: 13px;
}

.btn-decline,
.btn-outline {
  background: var(--color-white);
  color: var(--color-black);
  border: none;
  cursor: pointer;
  font-weight: 600;
  border-radius: var(--radius-md);
  padding: 10px 14px;
}

:deep(.activity-dialog .el-dialog) {
  background: #0a0a0a !important;
  border: 1px solid #2a2a2a !important;
  border-radius: var(--radius-xl);
}

:deep(.activity-dialog .el-dialog__header) {
  display: none;
}

:deep(.activity-dialog .el-dialog__body) {
  padding: 0;
  background-color: #0a0a0a !important;
}

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

.app-journal-card {
  background: linear-gradient(145deg, rgba(26, 30, 29, 0.98), rgba(14, 16, 18, 0.98));
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
  color: var(--color-text);
}

.shared-meta span,
.shared-updated {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.app-journal-title {
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
  background: rgba(0, 0, 0, 0.18);
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

.activity-form-card {
  position: relative;
  padding: 28px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  color: #f5f5f5;
  background: #0a0a0a;
}

.activity-close {
  position: absolute;
  top: 12px;
  right: 14px;
  background: none;
  border: none;
  color: var(--color-text-secondary);
  font-size: 22px;
  cursor: pointer;
}

.activity-form-title {
  font-family: var(--font-display);
  font-size: 22px;
}

.activity-form-subtitle,
.activity-field {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.activity-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.activity-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.activity-field-full {
  grid-column: 1 / -1;
}

.activity-input {
  width: 100%;
  padding: 11px 12px;
  border-radius: 12px;
  background: var(--color-surface-2);
  border: 1px solid var(--color-border);
  color: var(--color-white);
  font-size: 13px;
  outline: none;
}

.activity-input:focus {
  border-color: var(--color-green-border);
}

.activity-form-actions {
  display: flex;
  gap: 10px;
  margin-top: 4px;
}

.search-grid {
  grid-template-columns: 1fr auto;
  align-items: end;
}

.search-submit {
  height: 44px;
  min-width: 96px;
}

.found-user-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  border-radius: 14px;
  border: 1px solid var(--color-border);
  background: var(--color-surface-1);
  cursor: pointer;
}

.found-user-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
}

.found-user-info {
  flex: 1;
  min-width: 0;
}

.found-user-name {
  font-size: 14px;
  font-weight: 700;
}

.found-user-meta,
.found-user-bio {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.cr-empty.small {
  min-height: 120px;
}

@media (max-width: 720px) {
  .cr-activity-grid {
    grid-template-columns: 1fr;
  }
}
</style>
