<template>
  <div class="chat-page">

    <!-- ── Left sidebar ───────────────────────── -->
    <aside class="chat-sidebar">
      <div class="sidebar-header">
        <h2 class="sidebar-title">💬 Chat with your partner</h2>
        <div class="sidebar-search-row">
          <div class="sidebar-search-wrap">
            <el-icon class="search-icon"><Search /></el-icon>
            <input
              v-model="searchQ"
              class="sidebar-search"
              placeholder="Search partner here"
            >
          </div>
          <button class="add-btn" @click="openAddPartnerDialog">+</button>
        </div>
        <div class="sidebar-tabs">
          <button
            class="stab"
            :class="{ on: tab === 'partners' }"
            @click="tab = 'partners'"
          >
            Partner({{ partners.length }})
          </button>
          <button
            class="stab badge"
            :class="{ on: tab === 'applications' }"
            :data-count="applications.length"
            @click="tab = 'applications'"
          >
            Application({{ applications.length }})
          </button>
        </div>
      </div>

      <!-- Partner list -->
      <div v-if="tab === 'partners'" class="sidebar-list">
        <div
          v-for="p in filteredPartners"
          :key="p.id"
          class="partner-item"
          :class="{ active: activeId === p.id }"
          @click="selectPartner(p)"
        >
          <img :src="p.avatar" class="pi-avatar" :alt="p.nickname">
          <div class="pi-info">
            <div class="pi-top">
              <span class="pi-name">{{ p.nickname }}</span>
              <span class="pi-date">{{ p.lastMsgDate }}</span>
            </div>
            <div class="pi-bottom">
              <div class="pi-msg">{{ p.lastMsg || 'No messages yet' }}</div>
            </div>
          </div>
        </div>
        <div v-if="filteredPartners.length === 0" class="empty-tip">No partners found</div>
      </div>

      <!-- Applications list -->
      <div v-else class="sidebar-list">
        <div
          v-for="a in applications"
          :key="a.id"
          class="partner-item"
          :class="{ active: activeId === a.fromUser.id }"
          @click="selectApplication(a)"
        >
          <img :src="a.fromUser.avatar" class="pi-avatar" :alt="a.fromUser.nickname">
          <div class="pi-info">
            <div class="pi-top">
              <span class="pi-name">{{ a.fromUser.nickname }}</span>
              <span class="pi-dist">📍 {{ formatDistance(a.fromUser.distance) }}</span>
            </div>
            <div class="pi-score">⭐ {{ a.fromUser.score }}</div>
          </div>
        </div>
        <div v-if="applications.length === 0" class="empty-tip">No pending applications</div>
      </div>
    </aside>

    <!-- ── Middle: chat window ─────────────────── -->
    <main class="chat-main" v-show="tab === 'partners'">
      <div class="chat-bg" />

      <template v-if="activePartner">
        <!-- Header -->
        <div class="chat-header">
          <button class="chat-back" @click="activeId = null">‹</button>
          <span class="chat-partner-name">{{ activePartner.nickname }}</span>
        </div>

        <!-- Messages -->
        <div class="chat-msgs" ref="msgsEl">
          <div
            v-for="(msg, i) in messages"
            :key="msg.id ?? i"
            class="msg-item"
            :class="msg.mine ? 'mine' : 'them'"
          >
            <div v-if="msg.timestamp" class="msg-timestamp">{{ msg.timestamp }}</div>
            <div class="msg-row" :class="msg.mine ? 'me' : 'them'">
              <img
                v-if="!msg.mine"
                :src="activePartner.avatar"
                class="msg-avatar"
              >
              <div class="msg-bubble">{{ msg.content }}</div>
              <img
                v-if="msg.mine"
                :src="myAvatar"
                class="msg-avatar"
              >
            </div>
          </div>
        </div>

        <!-- Input -->
        <div class="chat-input-area">
          <input
            v-model="inputText"
            class="chat-input"
            placeholder="type here."
            @keydown.enter="sendMessage"
          >
          <button type="button" class="emoji-btn" @click="emojiPanelVisible = !emojiPanelVisible">☺</button>
          <button type="button" class="more-btn" @click="showMoreMenu = !showMoreMenu">⊕</button>
          <button type="button" class="send-btn" @click="sendMessage">Send</button>
          <div v-if="emojiPanelVisible" class="emoji-panel">
            <button
              v-for="emoji in emojiList"
              :key="emoji"
              type="button"
              class="emoji-item"
              @click="insertEmoji(emoji)"
            >
              {{ emoji }}
            </button>
          </div>
          <!-- More menu -->
          <div v-if="showMoreMenu" class="more-menu">
            <div class="more-item" @click="handleMoreAction('activity')">🎯 Start activity</div>
            <div class="more-item" @click="handleMoreAction('location')">📍 Send location</div>
            <div class="more-item" @click="handleMoreAction('journal')">📖 Shared journal</div>
          </div>
        </div>
      </template>

      <!-- Empty state -->
      <div v-else class="chat-empty">
        <span class="chat-empty-icon">💬</span>
        <p>Select a partner to start chatting</p>
      </div>
    </main>

    <!-- ── Right panel ─────────────────────────── -->
    <aside class="chat-right" :class="{ 'expanded': tab === 'applications' }">

      <!-- Partners view: activity detail -->
      <template v-if="tab === 'partners'">
        <div class="cr-header">
          <span class="cr-title">Activity detail</span>
          <el-icon style="cursor:pointer"><MoreFilled /></el-icon>
        </div>
        <div class="cr-calendar">
          <ActivityCalendar :event-days="calendarEventDays" />
        </div>
        <div class="cr-activity" v-if="activeActivity">
          <div class="act-entry-card" role="button" tabindex="0" @click="openActivityJournal">
            <img :src="activeActivity.coverImg" alt="activity journal">
            <div class="act-thumb-labels">
              <span class="atl-name">{{ activeActivity.title }}</span>
              <span class="atl-date">{{ activeActivity.date }}</span>
            </div>
          </div>
          <div class="cr-activity-intro">{{ activeActivity.intro }}</div>
          <div class="cr-activity-meta">Participants: {{ activeActivity.participants.join(', ') }}</div>
        </div>
        <div v-else class="cr-empty">No active activity</div>
      </template>

      <!-- Applications view: user profile detail -->
      <template v-else-if="activeApplication">
        <div class="app-detail-scroll">
          <div class="app-profile-header glass-card">
            <div class="app-ava-wrap">
              <img :src="activeApplication.fromUser.avatar" class="app-detail-avatar" :alt="activeApplication.fromUser.nickname">
            </div>

            <div class="app-info-wrap">
              <div class="app-name-row">
                <h2 class="app-name">{{ activeApplication.fromUser.nickname }}</h2>
                <span class="app-user-dist">📍 {{ formatDistance(activeApplication.fromUser.distance) }}</span>
              </div>
              <p class="app-bio">{{ activeApplication.fromUser.bio }}</p>
              <div class="app-tags">
                <span v-for="t in activeApplication.fromUser.tags" :key="t" class="tag-chip active">
                  <span class="app-tag-emoji">{{ getTagMeta(t).emoji }}</span>{{ t }}
                </span>
              </div>
            </div>

            <div class="app-stats">
              <div class="app-stat">
                <div class="app-stat-label">activities</div>
                <div class="app-stat-value">{{ activeApplication.fromUser.activities }}</div>
                <div class="app-stat-sub">completed</div>
              </div>
              <div class="app-stat">
                <div class="app-stat-label">positive feedback</div>
                <div class="app-stat-value" style="color:var(--color-text-gold)">⭐ {{ activeApplication.fromUser.score }}</div>
              </div>
              <div class="app-stat">
                <div class="app-stat-label">ranking</div>
                <div class="app-stat-value">{{ activeApplication.fromUser.ranking }}</div>
              </div>
            </div>
          </div>

          <div class="app-profile-grid">
            <div class="app-section glass-card">
              <h3 class="app-section-title">📒 journal</h3>
              <div class="app-journal-wall">
                <div
                  v-for="(img, i) in activeApplication.fromUser.publicJournals"
                  :key="i"
                  class="app-jw-thumb"
                  :class="{ tall: i === 0 }"
                >
                  <img :src="img.coverImage" :alt="img.title">
                </div>
                <div v-if="activeApplication.fromUser.publicJournals.length === 0" class="app-empty-tip">No journals yet</div>
              </div>
            </div>

            <div class="app-section glass-card">
              <h3 class="app-section-title">🔴 activities</h3>
              <div class="app-act-list">
                <div
                  v-for="act in activeApplication.fromUser.recentActivities"
                  :key="act.id"
                  class="app-act-row"
                >
                  <span class="app-act-icon">{{ act.icon }}</span>
                  <div class="app-act-row-info">
                    <div class="app-act-row-name">{{ act.name }}</div>
                    <div class="app-act-row-meta">
                      <div class="app-act-avs">
                        <img
                          v-for="av in act.participants.slice(0, 3)"
                          :key="av"
                          :src="av"
                          class="app-act-av"
                        >
                      </div>
                    </div>
                  </div>
                  <span class="app-act-date">{{ act.date }}</span>
                </div>
                <div v-if="activeApplication.fromUser.recentActivities.length === 0" class="app-empty-tip">No activities yet</div>
              </div>
            </div>
          </div>
        </div>

        <!-- Accept / Decline -->
        <div class="app-action-btns">
          <button class="btn-accept" @click="acceptApplication(activeApplication)">accept ✓</button>
          <button class="btn-decline" @click="declineApplication(activeApplication)">disagree ✕</button>
        </div>
      </template>

      <div v-else class="cr-empty">Select an application to review</div>
    </aside>

    <el-dialog
      v-model="startActivityVisible"
      width="520px"
      :show-close="false"
      class="activity-dialog"
      align-center
    >
      <div class="activity-form-card">
        <button type="button" class="activity-close" @click="startActivityVisible = false">×</button>
        <div class="activity-form-title">Start activity</div>
        <p class="activity-form-subtitle">Fill in the details to create a shared activity record.</p>

        <div class="activity-form-grid">
          <label class="activity-field">
            <span>Name</span>
            <input v-model="activityDraft.name" class="activity-input" placeholder="e.g. Sunset ride" />
          </label>
          <label class="activity-field">
            <span>Location</span>
            <input v-model="activityDraft.location" class="activity-input" placeholder="e.g. West Lake" />
          </label>
          <label class="activity-field activity-field-full">
            <span>Participants</span>
            <input v-model="activityDraft.participants" class="activity-input" placeholder="e.g. Rose, Alex, Mia" />
          </label>
          <label class="activity-field">
            <span>Start Time</span>
            <input v-model="activityDraft.startTime" type="datetime-local" class="activity-input" />
          </label>
          <label class="activity-field">
            <span>Status</span>
            <select v-model="activityDraft.status" class="activity-input">
              <option value="planned">planned</option>
              <option value="ongoing">ongoing</option>
              <option value="completed">completed</option>
            </select>
          </label>
        </div>

        <div class="activity-form-actions">
          <button type="button" class="btn-outline" @click="startActivityVisible = false">Cancel</button>
          <button type="button" class="btn-green" @click="confirmStartActivity">Start activity</button>
        </div>
      </div>
    </el-dialog>

    <el-dialog
      v-model="addPartnerVisible"
      width="460px"
      :show-close="false"
      class="activity-dialog"
      align-center
    >
      <div class="activity-form-card">
        <button type="button" class="activity-close" @click="addPartnerVisible = false">×</button>
        <div class="activity-form-title">Add partner</div>
        <p class="activity-form-subtitle">Enter a username to search and add a new partner.</p>

        <div class="activity-form-grid" style="grid-template-columns: 1fr auto; align-items: end;">
          <label class="activity-field activity-field-full" style="grid-column: 1 / span 1;">
            <span>Username</span>
            <input
              v-model="partnerSearchQ"
              class="activity-input"
              placeholder="e.g. rose"
              @keydown.enter="searchPartnerByUsername"
            />
          </label>
          <button type="button" class="btn-green" style="height: 44px; min-width: 96px;" @click="searchPartnerByUsername">Search</button>
        </div>

        <div v-if="partnerSearchLoading" class="cr-empty" style="min-height: 120px;">Searching...</div>

        <div v-else-if="searchedPartner" class="found-user-card" @click="addSearchedPartner">
          <img :src="searchedPartner.avatar" class="found-user-avatar" :alt="searchedPartner.nickname">
          <div class="found-user-info">
            <div class="found-user-name">{{ searchedPartner.nickname }}</div>
            <div class="found-user-meta">@{{ searchedPartner.username }} · 📍 {{ formatDistance(searchedPartner.distance) }}</div>
            <div class="found-user-bio">{{ searchedPartner.bio }}</div>
          </div>
          <button type="button" class="btn-outline" @click.stop="addSearchedPartner">Add</button>
        </div>

        <div v-else-if="partnerSearchDone" class="cr-empty" style="min-height: 120px;">
          No user found.
        </div>

        <div class="activity-form-actions">
          <button type="button" class="btn-outline" @click="addPartnerVisible = false">Close</button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onMounted, onBeforeUnmount, watch, reactive } from 'vue'
import { Search, MoreFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import ActivityCalendar from '@/components/activity/ActivityCalendar.vue'
import { 
  getFriends, 
  getFriendApplications, 
  handleFriendRequest,  // ✅ 改为 handleFriendRequest
  getUserById,
  getUserByUsername,
  sendFriendRequest,
} from '@/api/user'
import { getChatConversations, getChatMessages, sendChatMessage } from '@/api/chat'
import { formatDistance } from '@/utils/format'
import { getTagMeta } from '@/utils/tags'
import type { NearbyUser } from '@/types/user'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()
const myAvatar  = computed(() => userStore.userInfo?.avatar || '')

// ── State ────────────────────────────────────────────────────────
const tab         = ref<'partners' | 'applications'>('partners')
const searchQ     = ref('')
const activeId    = ref<number | null>(null)
const inputText   = ref('')
const showMoreMenu= ref(false)
const emojiPanelVisible = ref(false)
const msgsEl      = ref<HTMLElement | null>(null)
const startActivityVisible = ref(false)
const addPartnerVisible = ref(false)
const partnerSearchQ = ref('')
const searchedPartner = ref<NearbyUser | null>(null)
const partnerSearchLoading = ref(false)
const partnerSearchDone = ref(false)

// 从 localStorage 获取聊天记录
const getLocalMessages = (partnerId: number): MsgItem[] => {
  try {
    const userId = userStore.userInfo?.id
    if (!userId) return []
    
    const key = `chat_messages_${userId}_${partnerId}`
    const stored = localStorage.getItem(key)
    return stored ? JSON.parse(stored) : []
  } catch {
    return []
  }
}

// 保存聊天记录到 localStorage
const saveLocalMessages = (partnerId: number, messages: MsgItem[]) => {
  try {
    const userId = userStore.userInfo?.id
    if (!userId) return
    
    const key = `chat_messages_${userId}_${partnerId}`
    localStorage.setItem(key, JSON.stringify(messages))
  } catch {
    // 存储失败时静默处理
  }
}



// Partner shape used in sidebar
interface PartnerItem {
  id: number; nickname: string; avatar: string
  lastMsg: string; lastMsgDate: string; distance: number; score: number
  publicJournals: NearbyUser['publicJournals']
  recentActivities: NearbyUser['recentActivities']
}
const partners     = ref<PartnerItem[]>([])
interface ApplicationItem {
  id: number
  fromUser: NearbyUser
  toUserId: number
  status: 'pending' | 'accepted' | 'declined'
  createdAt: string
}
const applications = ref<ApplicationItem[]>([])

const emojiList = [
  '😀', '😄', '😁', '😊', '😍', '😘', '😋', '😎', '🤗', '🥰',
  '😂', '🤣', '😅', '🙂', '🙃', '😉', '😇', '😌', '😴', '😢',
  '😭', '😡', '👍', '👋', '🙏', '💖', '💞', '🎉', '🌟', '🐱',
]

// Messages map: partnerId → message array
interface MsgItem { id?: string; content: string; mine: boolean; timestamp?: string }
const msgMap = ref<Record<number, MsgItem[]>>({})
let messagePollTimer: number | null = null

const activePartner = computed(() =>
  partners.value.find(p => p.id === activeId.value) ?? null
)
const messages = computed(() =>
  activeId.value ? (msgMap.value[activeId.value] ?? []) : []
)

const activeApplication = ref<ApplicationItem | null>(null)

const filteredPartners = computed(() =>
  partners.value.filter(p =>
    p.nickname.toLowerCase().includes(searchQ.value.toLowerCase())
  )
)

// ── Current activity shown in right panel ────────────────────────
interface ActivityEntry {
  journalId?: number
  title: string
  date: string
  coverImg: string
  intro: string
  participants: string[]
}
const partnerActivityOverrides = ref<Record<number, ActivityEntry>>({})

const activityDraft = reactive({
  name: '',
  location: '',
  participants: '',
  startTime: '',
  status: 'planned' as 'planned' | 'ongoing' | 'completed',
})

const resetActivityDraft = () => {
  activityDraft.name = ''
  activityDraft.location = ''
  activityDraft.participants = ''
  activityDraft.startTime = ''
  activityDraft.status = 'planned'
}

const activeActivity = computed<ActivityEntry | null>(() => {
  const partner = activePartner.value
  if (!partner) return null

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
    coverImg: journal.coverImage,
    intro: `A shared activity journal created by ${myName} and ${partner.nickname}. ${activity.name} is the latest shared moment.`,
    participants: [myName, partner.nickname],
  }
})

const calendarEventDays = computed(() => {
  const value = activeActivity.value?.date
  if (!value) return []
  const parsed = new Date(value)
  return Number.isNaN(parsed.getTime()) ? [] : [parsed.getDate()]
})

const openActivityJournal = () => {
  if (!activeActivity.value?.journalId) return
  router.push({ name: 'JournalDetail', params: { id: String(activeActivity.value.journalId) } })
}

const ensureCurrentUser = async () => {
  if (!userStore.userInfo && userStore.isLoggedIn) {
    try {
      await userStore.fetchUserInfo()
    } catch {
      // keep existing fallback behavior when the profile request fails
    }
  }
}

// ── Load data ────────────────────────────────────────────────────
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
      const conversation = conversationMap.get(id)

      let detail: Record<string, unknown> = friend
      try {
        detail = await getUserById(id) as unknown as Record<string, unknown>
      } catch {
        detail = friend
      }

      return {
        id,
        nickname: String(detail.nickname ?? friend.nickname ?? ''),
        avatar: String(detail.avatar ?? friend.avatar ?? '/default-avatar.png'),
        lastMsg: conversation?.lastMessage || 'No messages yet',
        lastMsgDate: formatMsgTime(conversation?.lastMessageTime),
        distance: Number(detail.distance ?? friend.distance ?? 0),
        score: Number(detail.score ?? friend.score ?? 0),
        publicJournals: Array.isArray(detail.publicJournals) ? (detail.publicJournals as NearbyUser['publicJournals']) : [],
        recentActivities: Array.isArray(detail.recentActivities) ? (detail.recentActivities as NearbyUser['recentActivities']) : [],
      }
    }))
  } catch { /* use empty */ }
}

const loadApplications = async () => {
  try {
    const data = await getFriendApplications() as {
      incoming?: Array<{ id: number; senderId: number; receiverId: number; status?: string; createTime?: string }>
      outgoing?: Array<{ id: number; senderId: number; receiverId: number; status?: string; createTime?: string }>
    } | ApplicationItem[]

    if (Array.isArray(data)) {
      applications.value = data.filter(item => !!item?.fromUser && item.status === 'pending')
      return
    }

    const incoming = (data?.incoming ?? []).filter(req => (req.status ?? 'pending') === 'pending')

    const mapped = await Promise.all(incoming.map(async (req) => {
      let rawUser: Record<string, unknown> = {}
      try {
        rawUser = await getUserById(req.senderId) as unknown as Record<string, unknown>
      } catch {
        rawUser = {}
      }

      const tagsRaw = rawUser?.tags
      const tags = Array.isArray(tagsRaw)
        ? tagsRaw.map(v => String(v)).filter(Boolean)
        : typeof tagsRaw === 'string'
          ? tagsRaw.split(',').map(v => v.trim()).filter(Boolean)
          : []

      const fromUser: NearbyUser = {
        id: Number(rawUser?.id ?? req.senderId),
        username: String(rawUser?.username ?? ''),
        nickname: String(rawUser?.nickname ?? `User ${req.senderId}`),
        avatar: String(rawUser?.avatar ?? '/default-avatar.png'),
        bio: String(rawUser?.bio ?? ''),
        tags,
        activities: Number(rawUser?.activities ?? 0),
        score: Number(rawUser?.score ?? rawUser?.averageScore ?? 0),
        ranking: Number(rawUser?.ranking ?? 0),
        longitude: rawUser?.longitude as number | undefined,
        latitude: rawUser?.latitude as number | undefined,
        createdAt: String(rawUser?.createdAt ?? ''),
        distance: Number(rawUser?.distance ?? 0),
        publicJournals: Array.isArray(rawUser?.publicJournals) ? (rawUser.publicJournals as NearbyUser['publicJournals']) : [],
        recentActivities: Array.isArray(rawUser?.recentActivities) ? (rawUser.recentActivities as NearbyUser['recentActivities']) : [],
      }

      return {
        id: req.id,
        fromUser,
        toUserId: req.receiverId,
        status: (req.status ?? 'pending') as 'pending' | 'accepted' | 'declined',
        createdAt: req.createTime ?? '',
      } as ApplicationItem
    }))

    applications.value = mapped
  } catch { /* use empty */ }
}

const formatMsgTime = (value: unknown) => {
  if (!value) return ''
  const d = new Date(String(value))
  if (Number.isNaN(d.getTime())) return ''
  return d.toLocaleString()
}

// const loadMessages = async (partnerId: number) => {
//   try {
//     await ensureCurrentUser()
//     const data = await getChatMessages(partnerId, 1, 100)
//     const list = Array.isArray(data?.list) ? data.list : []

//     msgMap.value[partnerId] = list
//       .slice()
//       .sort((a, b) => new Date(a.createTime).getTime() - new Date(b.createTime).getTime())
//       .map(item => ({
//         id: item.id,
//         content: String(item.content ?? ''),
//         mine: Number(item.senderId) === Number(userStore.userInfo?.id),
//         timestamp: formatMsgTime(item.createTime),
//       }))

//     await nextTick()
//     if (msgsEl.value) msgsEl.value.scrollTop = msgsEl.value.scrollHeight
//   } catch {
//     if (!msgMap.value[partnerId]) msgMap.value[partnerId] = []
//   }
// }

const loadMessages = async (partnerId: number) => {
  try {
    await ensureCurrentUser()
    
    // 先从本地存储加载
    const localMsgs = getLocalMessages(partnerId)
    if (localMsgs.length > 0) {
      msgMap.value[partnerId] = localMsgs
      
      // 更新partner列表中的最后一条消息
      const lastMsg = localMsgs[localMsgs.length - 1]
      const partnerIndex = partners.value.findIndex(p => p.id === partnerId)
      if (partnerIndex !== -1) {
        partners.value[partnerIndex].lastMsg = lastMsg.content
        partners.value[partnerIndex].lastMsgDate = lastMsg.timestamp || ''
      }
      
      await nextTick()
      if (msgsEl.value) msgsEl.value.scrollTop = msgsEl.value.scrollHeight
    }
    
    // 然后从后端加载最新消息
    const data = await getChatMessages(partnerId, 1, 100)
    const list = Array.isArray(data?.list) ? data.list : []

    const newMessages = list
      .slice()
      .sort((a, b) => new Date(a.createTime).getTime() - new Date(b.createTime).getTime())
      .map(item => ({
        id: item.id,
        content: String(item.content ?? ''),
        mine: Number(item.senderId) === Number(userStore.userInfo?.id),
        timestamp: formatMsgTime(item.createTime),
      }))

    // 合并本地和后端消息，去重
    const mergedMessages = [...localMsgs]
    newMessages.forEach(msg => {
      if (!mergedMessages.some(m => m.id === msg.id)) {
        mergedMessages.push(msg)
      }
    })

    msgMap.value[partnerId] = mergedMessages
    saveLocalMessages(partnerId, mergedMessages)
    
    // 更新partner列表中的最后一条消息
    if (mergedMessages.length > 0) {
      const lastMsg = mergedMessages[mergedMessages.length - 1]
      const partnerIndex = partners.value.findIndex(p => p.id === partnerId)
      if (partnerIndex !== -1) {
        partners.value[partnerIndex].lastMsg = lastMsg.content
        partners.value[partnerIndex].lastMsgDate = lastMsg.timestamp || ''
      }
    }

    await nextTick()
    if (msgsEl.value) msgsEl.value.scrollTop = msgsEl.value.scrollHeight
  } catch {
    if (!msgMap.value[partnerId]) {
      msgMap.value[partnerId] = getLocalMessages(partnerId)
      
      // 更新partner列表中的最后一条消息
      const localMsgs = msgMap.value[partnerId]
      if (localMsgs.length > 0) {
        const lastMsg = localMsgs[localMsgs.length - 1]
        const partnerIndex = partners.value.findIndex(p => p.id === partnerId)
        if (partnerIndex !== -1) {
          partners.value[partnerIndex].lastMsg = lastMsg.content
          partners.value[partnerIndex].lastMsgDate = lastMsg.timestamp || ''
        }
      }
    }
  }
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
    if (tab.value === 'partners' && activeId.value === partnerId) {
      loadMessages(partnerId)
    }
  }, 3000)
}

// ── Actions ──────────────────────────────────────────────────────
const selectPartner = (p: PartnerItem) => {
  activeId.value = p.id
  activeApplication.value = null
  emojiPanelVisible.value = false
  loadMessages(p.id)
  startMessagePolling(p.id)
}

const selectApplication = (a: ApplicationItem) => {
  activeId.value = a.fromUser.id
  activeApplication.value = a
  emojiPanelVisible.value = false
}

// const sendMessage = async () => {
//   const txt = inputText.value.trim()
//   if (!txt || !activeId.value) return

//   const targetId = activeId.value
//   try {
//     await sendChatMessage(targetId, txt)
//     inputText.value = ''
//     showMoreMenu.value = false
//     emojiPanelVisible.value = false
//     await loadMessages(targetId)
//     await loadPartners()
//   } catch {
//     ElMessage.error('Message send failed')
//   }
// }

const sendMessage = async () => {
  const txt = inputText.value.trim()
  if (!txt || !activeId.value) return

  const targetId = activeId.value
  try {
    await sendChatMessage(targetId, txt)
    inputText.value = ''
    showMoreMenu.value = false
    emojiPanelVisible.value = false
    
    // 更新partner列表中的最后一条消息
    const partnerIndex = partners.value.findIndex(p => p.id === targetId)
    if (partnerIndex !== -1) {
      partners.value[partnerIndex].lastMsg = txt
      partners.value[partnerIndex].lastMsgDate = formatMsgTime(new Date().toISOString())
    }
    
    await loadMessages(targetId)
    await loadPartners()
  } catch {
    // 即使发送失败，也保存到本地
    const tempMsg: MsgItem = {
      id: `temp_${Date.now()}`,
      content: txt,
      mine: true,
      timestamp: formatMsgTime(new Date().toISOString())
    }
    
    if (!msgMap.value[targetId]) {
      msgMap.value[targetId] = []
    }
    msgMap.value[targetId].push(tempMsg)
    saveLocalMessages(targetId, msgMap.value[targetId])
    
    // 更新partner列表中的最后一条消息
    const partnerIndex = partners.value.findIndex(p => p.id === targetId)
    if (partnerIndex !== -1) {
      partners.value[partnerIndex].lastMsg = txt
      partners.value[partnerIndex].lastMsgDate = formatMsgTime(new Date().toISOString())
    }
    
    inputText.value = ''
    showMoreMenu.value = false
    emojiPanelVisible.value = false
    
    await nextTick()
    if (msgsEl.value) msgsEl.value.scrollTop = msgsEl.value.scrollHeight
  }
}

// 清理无效的本地存储消息
const cleanInvalidLocalMessages = () => {
  try {
    const userId = userStore.userInfo?.id
    if (!userId) return
    
    const currentPartnerIds = new Set(partners.value.map(p => p.id))
    
    // 遍历localStorage中所有的聊天记录
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i)
      if (key && key.startsWith(`chat_messages_${userId}_`)) {
        const partnerId = Number(key.replace(`chat_messages_${userId}_`, ''))
        // 如果该partner不在当前partner列表中，删除其聊天记录
        if (!currentPartnerIds.has(partnerId)) {
          localStorage.removeItem(key)
        }
      }
    }
  } catch {
    // 静默处理
  }
}




const insertEmoji = (emoji: string) => {
  inputText.value += emoji
  emojiPanelVisible.value = false
}

const acceptApplication = async (app: ApplicationItem) => {
  try {
    await handleFriendRequest(app.id, true)  // ✅ 改为统一接口
    ElMessage.success(`${app.fromUser.nickname} is now your partner!`)
    applications.value = applications.value.filter(a => a.id !== app.id)
    activeApplication.value = null
    await loadPartners()
  } catch { /* */ }
}

const declineApplication = async (app: ApplicationItem) => {
  try {
    await handleFriendRequest(app.id, false)  // ✅ 改为统一接口
    ElMessage.info('Application declined')
    applications.value = applications.value.filter(a => a.id !== app.id)
    activeApplication.value = null
  } catch { /* */ }
}

const handleMoreAction = (action: string) => {
  showMoreMenu.value = false
  if (action === 'activity') {
    if (activePartner.value) {
      activityDraft.name = `${activePartner.value.nickname}'s activity`
      activityDraft.location = 'TBD'
      activityDraft.participants = activePartner.value.nickname
      activityDraft.status = 'planned'
    }
    startActivityVisible.value = true
  }
  else if (action === 'location') ElMessage.info('Location sharing coming soon')
  else ElMessage.info('Shared journal coming soon')
}

const confirmStartActivity = () => {
  const name = activityDraft.name.trim()
  const location = activityDraft.location.trim()
  const participants = activityDraft.participants
    .split(',')
    .map(item => item.trim())
    .filter(Boolean)

  if (!name || !location || participants.length === 0 || !activityDraft.startTime) {
    ElMessage.warning('Please fill in Name, Location, Participants, and Start Time')
    return
  }

  const startedAt = new Date(activityDraft.startTime)
  if (Number.isNaN(startedAt.getTime())) {
    ElMessage.warning('Please choose a valid Start Time')
    return
  }

  if (activePartner.value) {
    partnerActivityOverrides.value[activePartner.value.id] = {
      journalId: activePartner.value.publicJournals?.[0]?.id,
      title: name,
      date: `${startedAt.getMonth() + 1}/${startedAt.getDate()}/${startedAt.getFullYear()}`,
      coverImg: activePartner.value?.publicJournals?.[0]?.coverImage || activePartner.value?.avatar || 'https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=400&q=70',
      intro: `${name} is a shared activity with ${activePartner.value.nickname} at ${location}.`,
      participants,
    }
  }

  startActivityVisible.value = false
  resetActivityDraft()
  ElMessage.success('Activity started')
}

const openAddPartnerDialog = () => {
  partnerSearchQ.value = ''
  searchedPartner.value = null
  partnerSearchDone.value = false
  addPartnerVisible.value = true
}

const normalizeUserShape = (raw: Record<string, unknown>): NearbyUser => {
  const tagsRaw = raw?.tags
  const tags = Array.isArray(tagsRaw)
    ? tagsRaw.map(v => String(v)).filter(Boolean)
    : typeof tagsRaw === 'string'
      ? tagsRaw.split(',').map(v => v.trim()).filter(Boolean)
      : []

  return {
    id: Number(raw?.id ?? 0),
    username: String(raw?.username ?? ''),
    nickname: String(raw?.nickname ?? ''),
    avatar: String(raw?.avatar ?? '/default-avatar.png'),
    bio: String(raw?.bio ?? ''),
    tags,
    activities: Number(raw?.activities ?? 0),
    score: Number(raw?.score ?? raw?.averageScore ?? 0),
    ranking: Number(raw?.ranking ?? 0),
    longitude: raw?.longitude as number | undefined,
    latitude: raw?.latitude as number | undefined,
    createdAt: String(raw?.createdAt ?? ''),
    distance: Number(raw?.distance ?? 0),
    publicJournals: Array.isArray(raw?.publicJournals) ? (raw.publicJournals as NearbyUser['publicJournals']) : [],
    recentActivities: Array.isArray(raw?.recentActivities) ? (raw.recentActivities as NearbyUser['recentActivities']) : [],
  }
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
    searchedPartner.value = null
  } finally {
    partnerSearchLoading.value = false
  }
}

const addSearchedPartner = async () => {
  if (!searchedPartner.value) return
  if (partners.value.some(p => p.id === searchedPartner.value?.id)) {
    ElMessage.info('This user is already in your partner list')
    return
  }

  try {
    await sendFriendRequest(searchedPartner.value.id)
    ElMessage.success(`Friend request sent to ${searchedPartner.value.nickname}`)
    addPartnerVisible.value = false
  } catch {
    // handled by interceptor
  }
}

// onMounted(async () => {
//   await ensureCurrentUser()
//   await Promise.all([loadPartners(), loadApplications()])
// })

onMounted(async () => {
  await ensureCurrentUser()
  await Promise.all([loadPartners(), loadApplications()])
  
  // 清理无效的本地存储消息
  cleanInvalidLocalMessages()
  
  // 初始化本地存储
  try {
    const stored = localStorage.getItem('chat_partners')
    if (stored) {
      const localPartners = JSON.parse(stored)
      localPartners.forEach((p: PartnerItem) => {
        if (!msgMap.value[p.id]) {
          msgMap.value[p.id] = getLocalMessages(p.id)
        }
      })
    }
  } catch {
    // 静默处理
  }
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

onBeforeUnmount(() => {
  stopMessagePolling()
})
</script>

<style scoped>
/* ── Page layout ── */
/* .chat-page {
  display: flex;
  height: calc(100vh - var(--nav-height));
  position: relative; z-index: 1;
  overflow: hidden;
} */

.chat-page {
  display: flex;
  height: 100vh;
  padding-top: var(--nav-height);
  box-sizing: border-box;
  position: relative;
  z-index: 1;
  overflow: hidden;
}

/* ── Left sidebar ── */
.chat-sidebar {
  width: var(--sidebar-width);
  flex-shrink: 0;
  background: rgba(0,0,0,0.65);
  border-right: 0.5px solid var(--color-border-dim);
  display: flex; flex-direction: column;
  backdrop-filter: blur(14px);
}

.sidebar-header { padding: 20px 20px 0; }
.sidebar-title  { font-family: var(--font-display); font-size: 17px; margin-bottom: 14px; }

.sidebar-search-row { display: flex; gap: 8px; align-items: center; margin-bottom: 12px; }
.sidebar-search-wrap { position: relative; flex: 1; }
.search-icon { position: absolute; left: 11px; top: 50%; transform: translateY(-50%); color: var(--color-text-secondary); font-size: 13px; }
.sidebar-search {
  width: 100%; padding: 9px 14px 9px 32px;
  background: var(--color-surface-2); border: 0.5px solid var(--color-border);
  border-radius: var(--radius-full); color: var(--color-white); font-size: 13px;
}
.sidebar-search::placeholder { color: var(--color-text-hint); }
.add-btn {
  width: 28px; height: 28px; border-radius: 50%;
  background: transparent; border: 2px solid var(--color-green);
  color: var(--color-green); font-size: 18px; line-height: 1;
  display: flex; align-items: center; justify-content: center; cursor: pointer;
  flex-shrink: 0;
}

.sidebar-tabs { display: flex; gap: 8px; padding-bottom: 12px; border-bottom: 0.5px solid var(--color-border-dim); }
.stab {
  padding: 7px 16px; border-radius: var(--radius-full); font-size: 12px;
  background: transparent; border: 1px solid var(--color-border);
  color: var(--color-text-secondary); cursor: pointer; transition: all .15s;
  position: relative;
}
.stab.on    { background: var(--color-black); border-color: var(--color-white); color: var(--color-white); }
.stab.badge::after {
  content: attr(data-count);
  position: absolute; top: -5px; right: -5px;
  background: var(--color-green); color: var(--color-black);
  font-size: 9px; font-weight: 700; padding: 1px 5px;
  border-radius: 8px; min-width: 16px; text-align: center;
}

.sidebar-list { flex: 1; overflow-y: auto; }
.partner-item {
  display: flex; align-items: center; gap: 12px;
  padding: 12px 20px; cursor: pointer; transition: background .15s;
  border-bottom: 0.5px solid var(--color-border-dim);
}
.partner-item:hover   { background: var(--color-surface-1); }
.partner-item.active  { background: var(--color-green-dim); border-left: 3px solid var(--color-green); }
.pi-avatar { width: 44px; height: 44px; border-radius: 50%; object-fit: cover; flex-shrink: 0; border: 1.5px solid var(--color-border); }
.pi-info   { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 5px; }
.pi-top    { display: flex; justify-content: space-between; align-items: center; gap: 10px; }
.pi-bottom { display: flex; align-items: center; justify-content: space-between; gap: 10px; }
.pi-name   { font-size: 13px; font-weight: 600; color: var(--color-green); }
.pi-date, .pi-dist { font-size: 11px; color: var(--color-text-secondary); }
.pi-msg {
  font-size: 12px;
  color: var(--color-text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
  min-width: 0;
}
.pi-score { font-size: 12px; color: var(--color-text-secondary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.empty-tip { padding: 24px; text-align: center; font-size: 13px; color: var(--color-text-hint); }

/* ── Middle chat ── */
.chat-main {
  flex: 1; display: flex; flex-direction: column;
  position: relative; overflow: hidden;
}
.chat-bg {
  position: absolute; inset: 0; z-index: 0;
  background: url('https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?w=1200&q=50') center/cover;
  opacity: 0.3;
}
.chat-header {
  position: relative; z-index: 2;
  padding: 14px 20px; display: flex; align-items: center; gap: 14px;
  background: rgba(0,0,0,.55); backdrop-filter: blur(8px);
  border-bottom: 0.5px solid var(--color-border-dim);
}
.chat-back { background: none; border: none; color: var(--color-white); font-size: 22px; cursor: pointer; }
.chat-partner-name { font-family: var(--font-display); font-size: 20px; flex: 1; text-align: center; }

.chat-msgs {
  flex: 1; overflow-y: auto; padding: 20px 24px;
  display: flex; flex-direction: column; gap: 16px;
  position: relative; z-index: 2;
}
.msg-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.msg-item.mine { align-items: flex-end; }
.msg-item.them { align-items: flex-start; }
.msg-timestamp {
  text-align: center; font-size: 11px;
  color: rgba(255,255,255,.45); background: rgba(0,0,0,.4);
  border-radius: 8px; padding: 3px 10px; align-self: center;
}
.msg-row { display: flex; align-items: flex-end; gap: 10px; max-width: 72%; }
.msg-row.them { align-self: flex-start; }
.msg-row.me   { 
  align-self: flex-end; 
  /* flex-direction: row-reverse; */
}
.msg-row.me .msg-avatar {
  border: 1px solid var(--color-green-border);
}
.msg-avatar { width: 36px; height: 36px; border-radius: 50%; object-fit: cover; flex-shrink: 0; }
.msg-bubble {
  padding: 11px 15px; border-radius: 18px; font-size: 14px; line-height: 1.55;
}
.msg-row.them .msg-bubble { background: rgba(255,255,255,.92); color: #000; border-bottom-left-radius: 4px; }
.msg-row.me   .msg-bubble { background: rgba(0,0,0,.75); color: var(--color-white); border: 1px solid var(--color-green-border); border-bottom-right-radius: 4px; }

.chat-input-area {
  position: relative; z-index: 2;
  padding: 13px 18px; display: flex; align-items: center; gap: 10px;
  background: rgba(0,0,0,.65); backdrop-filter: blur(8px);
  border-top: 0.5px solid var(--color-border-dim);
}
.chat-input {
  flex: 1; padding: 11px 16px; border-radius: var(--radius-full);
  background: var(--color-surface-2); border: 0.5px solid var(--color-border);
  color: var(--color-white); font-size: 14px;
}
.chat-input::placeholder { color: var(--color-text-hint); }
.emoji-btn {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  border: 1px solid rgba(255,255,255,.2);
  background: rgba(255,255,255,.06);
  color: var(--color-white);
  font-size: 18px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex-shrink: 0;
  transition: background .15s, border-color .15s, transform .15s;
}
.emoji-btn:hover {
  background: rgba(255,255,255,.12);
  border-color: var(--color-green-border);
  transform: translateY(-1px);
}
.emoji-panel {
  position: absolute;
  right: 56px;
  bottom: calc(100% + 10px);
  width: 280px;
  padding: 12px;
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
  background: rgba(12, 12, 12, .98);
  border: 1px solid var(--color-border);
  border-radius: 18px;
  box-shadow: 0 18px 40px rgba(0,0,0,.55);
  z-index: 5;
}
.emoji-panel::before {
  content: '';
  position: absolute;
  right: 86px;
  bottom: -7px;
  width: 12px;
  height: 12px;
  background: rgba(12, 12, 12, .98);
  border-right: 1px solid var(--color-border);
  border-bottom: 1px solid var(--color-border);
  transform: rotate(45deg);
}
.emoji-item {
  width: 100%;
  aspect-ratio: 1;
  border: none;
  border-radius: 12px;
  background: rgba(255,255,255,.04);
  color: var(--color-white);
  font-size: 20px;
  cursor: pointer;
  transition: transform .12s, background .12s;
}
.emoji-item:hover {
  background: rgba(144, 255, 140, .12);
  transform: scale(1.08);
}
.more-btn {
  width: 32px; height: 32px; border-radius: 50%;
  background: transparent; border: 2px solid rgba(255,255,255,.45);
  color: rgba(255,255,255,.7); font-size: 20px;
  display: flex; align-items: center; justify-content: center; cursor: pointer;
  transition: border-color .15s; flex-shrink: 0;
}
.more-btn:hover { border-color: var(--color-green); color: var(--color-green); }
.send-btn {
  height: 34px;
  padding: 0 16px;
  border: none;
  border-radius: 999px;
  background: var(--color-green);
  color: var(--color-black);
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  flex-shrink: 0;
  transition: transform .15s, filter .15s, opacity .15s;
}
.send-btn:hover {
  transform: translateY(-1px);
  filter: brightness(1.05);
}
.send-btn:disabled {
  opacity: 0.6;
  cursor: default;
  transform: none;
}
.more-menu {
  position: absolute; bottom: calc(100% + 8px); right: 16px;
  background: var(--color-card-solid); border: 0.5px solid var(--color-border);
  border-radius: var(--radius-md); overflow: hidden;
  box-shadow: 0 8px 24px rgba(0,0,0,.5);
}
.more-item {
  padding: 11px 18px; font-size: 13px; cursor: pointer;
  transition: background .15s; white-space: nowrap;
}
.more-item:hover { background: var(--color-surface-2); color: var(--color-green); }

.chat-empty {
  flex: 1; display: flex; flex-direction: column;
  align-items: center; justify-content: center; gap: 12px;
  opacity: .4; position: relative; z-index: 2;
}
.chat-empty-icon { font-size: 48px; }

/* ── Right panel ── */
.chat-right {
  width: 300px; flex-shrink: 0;
  background: rgba(0,0,0,.7); backdrop-filter: blur(14px);
  border-left: 0.5px solid var(--color-border-dim);
  display: flex; flex-direction: column; overflow: hidden;
}
.cr-header {
  padding: 15px 18px; display: flex; align-items: center; justify-content: space-between;
  border-bottom: 0.5px solid var(--color-border-dim); flex-shrink: 0;
}
.cr-title { font-family: var(--font-display); font-size: 16px; }
.cr-calendar { border-bottom: 0.5px solid var(--color-border-dim); flex-shrink: 0; }

.cr-activity {
  flex: 1; overflow-y: auto; padding: 14px;
  display: flex; flex-direction: column; gap: 12px;
}
.act-thumb { width: 100%; height: 110px; border-radius: var(--radius-md); overflow: hidden; position: relative; background: #333; }
.act-thumb img { width: 100%; height: 100%; object-fit: cover; }
.act-entry-card { width: 100%; border-radius: var(--radius-md); overflow: hidden; position: relative; background: #333; cursor: pointer; }
.act-entry-card img { width: 100%; height: 110px; object-fit: cover; display: block; }
.act-thumb-labels {
  position: absolute; bottom: 8px; left: 10px; right: 10px;
  display: flex; justify-content: space-between;
}
.atl-name, .atl-date {
  font-size: 11px; background: rgba(0,0,0,.6); padding: 2px 7px; border-radius: 6px;
}
.cr-activity-intro {
  font-size: 13px;
  color: rgba(255,255,255,.86);
  line-height: 1.6;
}
.cr-activity-meta {
  font-size: 11px;
  color: var(--color-text-secondary);
}

.cr-empty {
  flex: 1; display: flex; align-items: center; justify-content: center;
  font-size: 13px; color: var(--color-text-hint); padding: 24px; text-align: center;
}

/* Application detail */
.app-detail-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.app-profile-header {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  padding: 22px 24px;
}
.app-ava-wrap {
  flex-shrink: 0;
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
.app-user-dist {
  font-size: 11px;
  color: var(--color-text-danger);
  font-family: var(--font-body);
  font-weight: 400;
}
.app-bio {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-bottom: 8px;
  line-height: 1.5;
}
.app-tags {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
}
.app-tag-emoji { margin-right: 6px; }

.app-stats {
  display: flex;
  gap: 0;
  flex-shrink: 0;
}
.app-stat {
  text-align: center;
  padding: 0 20px;
  border-left: 1px solid var(--color-border-dim);
}
.app-stat:first-child { border-left: none; }
.app-stat-label { font-size: 12px; color: var(--color-text-secondary); margin-bottom: 5px; }
.app-stat-value { font-family: var(--font-display); font-size: 22px; }
.app-stat-sub   { font-size: 12px; color: var(--color-text-secondary); }

.app-profile-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}
.app-section {
  padding: 22px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.app-section-title { font-family: var(--font-display); font-size: 17px; margin-bottom: 0; }
.app-empty-tip { font-size: 13px; color: var(--color-text-hint); text-align: center; padding: 20px 0; }

.app-journal-wall {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 7px;
}
.app-jw-thumb {
  border-radius: 8px;
  overflow: hidden;
  background: #333;
  cursor: pointer;
  aspect-ratio: 1;
}
.app-jw-thumb.tall {
  grid-row: span 2;
  aspect-ratio: auto;
}
.app-jw-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform .2s;
}
.app-jw-thumb:hover img { transform: scale(1.06); }

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
.app-act-row:last-child { border-bottom: none; padding-bottom: 0; }
.app-act-icon { font-size: 18px; flex-shrink: 0; }
.app-act-row-info { flex: 1; min-width: 0; }
.app-act-row-name { font-size: 14px; font-weight: 500; margin-bottom: 4px; }
.app-act-row-meta { display: flex; align-items: center; gap: 8px; }
.app-act-avs { display: flex; }
.app-act-av {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  object-fit: cover;
  border: 1.5px solid #000;
  margin-left: -5px;
}
.app-act-av:first-child { margin-left: 0; }
.app-act-date { font-size: 12px; color: var(--color-text-secondary); flex-shrink: 0; }

.app-action-btns { display: flex; gap: 10px; padding: 14px 16px; border-top: 0.5px solid var(--color-border-dim); flex-shrink: 0; }
.btn-accept  { flex: 1; padding: 10px; border-radius: var(--radius-md); background: var(--color-green); color: var(--color-black); font-size: 13px; font-weight: 700; border: none; cursor: pointer; }
.btn-decline { flex: 1; padding: 10px; border-radius: var(--radius-md); background: var(--color-white); color: var(--color-black); font-size: 13px; font-weight: 600; border: none; cursor: pointer; }

:deep(.activity-dialog .el-dialog) {
  background: #0a0a0a !important;
  border: 1px solid #2a2a2a !important;
  border-radius: var(--radius-xl);
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.65);
}
:deep(.activity-dialog .el-dialog__header) { display: none; }
:deep(.activity-dialog .el-dialog__body) {
  padding: 0;
  background-color: #0a0a0a !important;
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
  line-height: 1;
}
.activity-form-title {
  font-family: var(--font-display);
  font-size: 22px;
}
.activity-form-subtitle {
  font-size: 13px;
  color: var(--color-text-secondary);
  line-height: 1.5;
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
  font-size: 12px;
  color: var(--color-text-secondary);
}
.activity-field-full { grid-column: 1 / -1; }
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
  box-shadow: 0 0 0 2px rgba(144, 255, 140, 0.12);
}
.activity-form-actions {
  display: flex;
  gap: 10px;
  margin-top: 4px;
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
  transition: background .15s, border-color .15s, transform .15s;
}
.found-user-card:hover {
  background: var(--color-surface-2);
  border-color: var(--color-green-border);
  transform: translateY(-1px);
}
.found-user-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
  border: 1.5px solid var(--color-border);
}
.found-user-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.found-user-name {
  font-size: 14px;
  font-weight: 700;
  color: var(--color-white);
}
.found-user-meta {
  font-size: 11px;
  color: var(--color-text-secondary);
}
.found-user-bio {
  font-size: 12px;
  color: var(--color-text-secondary);
  line-height: 1.4;
  line-clamp: 2;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
/* Application mode: expand chat-right to fill view */
.chat-right.expanded {
  width: 100%;
  flex: 1;
}
</style>
