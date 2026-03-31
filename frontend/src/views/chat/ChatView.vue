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
          <button class="add-btn" @click="showToast('Add partner coming soon')">+</button>
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
            <div class="pi-msg">{{ p.lastMsg }}</div>
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
    <main class="chat-main">
      <div class="chat-bg" />

      <template v-if="activePartner">
        <!-- Header -->
        <div class="chat-header">
          <button class="chat-back" @click="activeId = null">‹</button>
          <span class="chat-partner-name">{{ activePartner.nickname }}</span>
        </div>

        <!-- Messages -->
        <div class="chat-msgs" ref="msgsEl">
          <template v-for="(msg, i) in messages" :key="i">
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
          </template>
        </div>

        <!-- Input -->
        <div class="chat-input-area">
          <input
            v-model="inputText"
            class="chat-input"
            placeholder="type here."
            @keydown.enter="sendMessage"
          >
          <span class="emoji-btn">🐱</span>
          <button class="more-btn" @click="showMoreMenu = !showMoreMenu">⊕</button>
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
    <aside class="chat-right">

      <!-- Partners view: activity detail -->
      <template v-if="tab === 'partners'">
        <div class="cr-header">
          <span class="cr-title">Activity detail</span>
          <el-icon style="cursor:pointer"><MoreFilled /></el-icon>
        </div>
        <div class="cr-calendar">
          <ActivityCalendar :event-days="[9, 13, 21]" />
        </div>
        <div class="cr-activity" v-if="currentActivity">
          <div class="act-thumb">
            <img :src="currentActivity.coverImg" alt="activity">
            <div class="act-thumb-labels">
              <span class="atl-name">{{ currentActivity.name }}</span>
              <span class="atl-date">{{ currentActivity.date }}</span>
            </div>
          </div>
          <h4 class="cr-section-title">Status</h4>
          <div class="cr-status-rows">
            <div class="cr-row">Name：{{ currentActivity.name }}</div>
            <div class="cr-row">Location: {{ currentActivity.location }}</div>
            <div class="cr-row">Participants: {{ currentActivity.participants }}</div>
            <div class="cr-row">Start Time: {{ currentActivity.startTime }}</div>
          </div>
          <button class="btn-completed" @click="markCompleted">Completed! ✓</button>
        </div>
        <div v-else class="cr-empty">No active activity</div>
      </template>

      <!-- Applications view: user profile detail -->
      <template v-else-if="activeApplication">
        <div class="app-detail-scroll">
          <!-- User info strip -->
          <div class="app-user-strip">
            <img :src="activeApplication.fromUser.avatar" class="app-detail-avatar">
            <div class="app-user-info">
              <div class="app-user-name">
                {{ activeApplication.fromUser.nickname }}
                <span class="app-user-dist">📍 {{ formatDistance(activeApplication.fromUser.distance) }}</span>
              </div>
              <p class="app-user-bio">{{ activeApplication.fromUser.bio }}</p>
              <div class="app-user-tags">
                <span v-for="t in activeApplication.fromUser.tags" :key="t" class="tag-chip active" style="font-size:11px">{{ t }}</span>
              </div>
            </div>
          </div>

          <!-- Stats -->
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

          <!-- Journal thumbnails -->
          <div class="app-section">
            <h4 class="app-section-title">📒 journal</h4>
            <div class="app-journal-thumbs">
              <div
                v-for="(img, i) in activeApplication.fromUser.publicJournals"
                :key="i"
                class="app-j-thumb"
              >
                <img :src="img.coverImage" :alt="img.title">
              </div>
            </div>
          </div>

          <!-- Recent activities -->
          <div class="app-section">
            <h4 class="app-section-title">🔴 activities</h4>
            <div
              v-for="act in activeApplication.fromUser.recentActivities"
              :key="act.id"
              class="app-act-row"
            >
              <span>{{ act.name }}</span>
              <span style="color:var(--color-text-secondary);font-size:11px">{{ act.date }}</span>
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
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onMounted } from 'vue'
import { Search, MoreFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import ActivityCalendar from '@/components/activity/ActivityCalendar.vue'
import { 
  getFriends, 
  getFriendApplications, 
  handleFriendRequest  // ✅ 改为 handleFriendRequest
} from '@/api/user'
import { formatDistance } from '@/utils/format'
import type { FriendApplication } from '@/types/user'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const myAvatar  = computed(() => userStore.userInfo?.avatar || '')

// ── State ────────────────────────────────────────────────────────
const tab         = ref<'partners' | 'applications'>('partners')
const searchQ     = ref('')
const activeId    = ref<number | null>(null)
const inputText   = ref('')
const showMoreMenu= ref(false)
const msgsEl      = ref<HTMLElement | null>(null)

// Partner shape used in sidebar
interface PartnerItem {
  id: number; nickname: string; avatar: string
  lastMsg: string; lastMsgDate: string; distance: number; score: number
}
const partners     = ref<PartnerItem[]>([])
const applications = ref<FriendApplication[]>([])

// Messages map: partnerId → message array
interface MsgItem { content: string; mine: boolean; timestamp?: string }
const msgMap = ref<Record<number, MsgItem[]>>({})

const activePartner = computed(() =>
  partners.value.find(p => p.id === activeId.value) ?? null
)
const messages = computed(() =>
  activeId.value ? (msgMap.value[activeId.value] ?? []) : []
)

const activeApplication = ref<FriendApplication | null>(null)

const filteredPartners = computed(() =>
  partners.value.filter(p =>
    p.nickname.toLowerCase().includes(searchQ.value.toLowerCase())
  )
)

// ── Current activity shown in right panel ────────────────────────
interface ActivitySummary {
  name: string; location: string; participants: string
  startTime: string; date: string; coverImg: string
}
const currentActivity = ref<ActivitySummary | null>({
  name: 'the Green Park walk',
  location: 'Hang Zhou',
  participants: 'Julio, Jose',
  startTime: 'Tuesday 9, 10:00am',
  date: '09/09/2026',
  coverImg: 'https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=400&q=70',
})

// ── Load data ────────────────────────────────────────────────────
const loadPartners = async () => {
  try {
    const data = await getFriends() as PartnerItem[]
    partners.value = data
  } catch { /* use empty */ }
}

const loadApplications = async () => {
  try {
    const data = await getFriendApplications() as FriendApplication[]
    applications.value = data
  } catch { /* use empty */ }
}

// ── Actions ──────────────────────────────────────────────────────
const selectPartner = (p: PartnerItem) => {
  activeId.value = p.id
  activeApplication.value = null
  if (!msgMap.value[p.id]) msgMap.value[p.id] = []
  nextTick(() => { if (msgsEl.value) msgsEl.value.scrollTop = msgsEl.value.scrollHeight })
}

const selectApplication = (a: FriendApplication) => {
  activeId.value = a.fromUser.id
  activeApplication.value = a
}

const sendMessage = () => {
  const txt = inputText.value.trim()
  if (!txt || !activeId.value) return
  if (!msgMap.value[activeId.value]) msgMap.value[activeId.value] = []
  msgMap.value[activeId.value].push({ content: txt, mine: true })
  inputText.value = ''
  showMoreMenu.value = false
  nextTick(() => { if (msgsEl.value) msgsEl.value.scrollTop = msgsEl.value.scrollHeight })
}

const acceptApplication = async (app: FriendApplication) => {
  try {
    await handleFriendRequest(app.id, true)  // ✅ 改为统一接口
    ElMessage.success(`${app.fromUser.nickname} is now your partner!`)
    applications.value = applications.value.filter(a => a.id !== app.id)
    activeApplication.value = null
    loadPartners()
  } catch { /* */ }
}

const declineApplication = async (app: FriendApplication) => {
  try {
    await handleFriendRequest(app.id, false)  // ✅ 改为统一接口
    ElMessage.info('Application declined')
    applications.value = applications.value.filter(a => a.id !== app.id)
    activeApplication.value = null
  } catch { /* */ }
}

const markCompleted = () => {
  ElMessage.success('Activity marked as completed!')
  currentActivity.value = null
}

const handleMoreAction = (action: string) => {
  showMoreMenu.value = false
  if (action === 'activity') ElMessage.info('Start activity feature coming soon')
  else if (action === 'location') ElMessage.info('Location sharing coming soon')
  else ElMessage.info('Shared journal coming soon')
}

const showToast = (msg: string) => ElMessage.info(msg)

onMounted(() => { loadPartners(); loadApplications() })
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
.pi-info   { flex: 1; min-width: 0; }
.pi-top    { display: flex; justify-content: space-between; align-items: center; margin-bottom: 3px; }
.pi-name   { font-size: 13px; font-weight: 600; color: var(--color-green); }
.pi-date, .pi-dist { font-size: 11px; color: var(--color-text-secondary); }
.pi-msg, .pi-score { font-size: 12px; color: var(--color-text-secondary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
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
.msg-timestamp {
  text-align: center; font-size: 11px;
  color: rgba(255,255,255,.45); background: rgba(0,0,0,.4);
  border-radius: 8px; padding: 3px 10px; align-self: center;
}
.msg-row { display: flex; align-items: flex-end; gap: 10px; max-width: 72%; }
.msg-row.them { align-self: flex-start; }
.msg-row.me   { align-self: flex-end; flex-direction: row-reverse; }
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
.emoji-btn { font-size: 22px; cursor: pointer; }
.more-btn {
  width: 32px; height: 32px; border-radius: 50%;
  background: transparent; border: 2px solid rgba(255,255,255,.45);
  color: rgba(255,255,255,.7); font-size: 20px;
  display: flex; align-items: center; justify-content: center; cursor: pointer;
  transition: border-color .15s; flex-shrink: 0;
}
.more-btn:hover { border-color: var(--color-green); color: var(--color-green); }
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
.act-thumb-labels {
  position: absolute; bottom: 8px; left: 10px; right: 10px;
  display: flex; justify-content: space-between;
}
.atl-name, .atl-date {
  font-size: 11px; background: rgba(0,0,0,.6); padding: 2px 7px; border-radius: 6px;
}
.cr-section-title { font-family: var(--font-display); font-size: 15px; }
.cr-status-rows { display: flex; flex-direction: column; gap: 6px; }
.cr-row { font-size: 12px; color: rgba(255,255,255,.8); font-family: monospace; }
.btn-completed {
  width: 100%; padding: 11px; border-radius: var(--radius-md);
  background: var(--color-surface-2); border: 1px solid var(--color-border);
  color: var(--color-white); font-size: 13px; cursor: pointer;
  transition: all .15s;
}
.btn-completed:hover { border-color: var(--color-green-border); color: var(--color-green); }

.cr-empty {
  flex: 1; display: flex; align-items: center; justify-content: center;
  font-size: 13px; color: var(--color-text-hint); padding: 24px; text-align: center;
}

/* Application detail */
.app-detail-scroll { flex: 1; overflow-y: auto; padding: 16px; display: flex; flex-direction: column; gap: 16px; }
.app-user-strip { display: flex; gap: 12px; align-items: flex-start; }
.app-detail-avatar { width: 52px; height: 52px; border-radius: 50%; object-fit: cover; border: 2px solid var(--color-border); flex-shrink: 0; }
.app-user-info { flex: 1; }
.app-user-name { font-family: var(--font-display); font-size: 16px; margin-bottom: 4px; display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.app-user-dist { font-size: 11px; color: var(--color-text-danger); font-family: var(--font-body); font-weight: 400; }
.app-user-bio  { font-size: 12px; color: var(--color-text-secondary); margin-bottom: 8px; line-height: 1.5; }
.app-user-tags { display: flex; gap: 5px; flex-wrap: wrap; }

.app-stats { display: grid; grid-template-columns: repeat(3,1fr); gap: 8px; }
.app-stat  { background: var(--color-surface-1); border-radius: var(--radius-md); padding: 10px 8px; text-align: center; }
.app-stat-label { font-size: 10px; color: var(--color-text-secondary); margin-bottom: 4px; }
.app-stat-value { font-family: var(--font-display); font-size: 14px; }
.app-stat-sub   { font-size: 10px; color: var(--color-text-secondary); }

.app-section       { display: flex; flex-direction: column; gap: 8px; }
.app-section-title { font-family: var(--font-display); font-size: 14px; }
.app-journal-thumbs { display: flex; gap: 6px; }
.app-j-thumb { flex: 1; aspect-ratio: 1; border-radius: 8px; overflow: hidden; background: #333; }
.app-j-thumb img { width: 100%; height: 100%; object-fit: cover; }
.app-act-row { display: flex; justify-content: space-between; font-size: 12px; padding: 5px 0; border-bottom: 0.5px solid var(--color-border-dim); }

.app-action-btns { display: flex; gap: 10px; padding: 14px 16px; border-top: 0.5px solid var(--color-border-dim); flex-shrink: 0; }
.btn-accept  { flex: 1; padding: 10px; border-radius: var(--radius-md); background: var(--color-green); color: var(--color-black); font-size: 13px; font-weight: 700; border: none; cursor: pointer; }
.btn-decline { flex: 1; padding: 10px; border-radius: var(--radius-md); background: var(--color-white); color: var(--color-black); font-size: 13px; font-weight: 600; border: none; cursor: pointer; }
</style>
