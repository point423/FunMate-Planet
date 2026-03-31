<template>
  <div class="home-page">
    <!-- Globe scene -->
    <div class="globe-scene">
      <!-- Orbit rings -->
      <div class="orbit-ring ring-1" />
      <div class="orbit-ring ring-2" />
      <div class="orbit-ring ring-3" />

      <!-- Globe -->
      <div class="globe" ref="globeEl">
        <div class="globe-land" />
        <div class="globe-clouds" />
      </div>

      <!-- Orbit users (canvas positions) -->
      <div
        v-for="(user, i) in orbitUsers"
        :key="user.id"
        class="orbit-user"
        :style="orbitPositions[i]"
        :title="user.nickname"
        @click="openUserDetail(user)"
      >
        <img :src="user.avatar" :alt="user.nickname">
        <div class="orbit-tooltip">
          <div class="ot-name">{{ user.nickname }}</div>
          <div class="ot-tags">{{ user.tags.slice(0, 2).join(' · ') }}</div>
        </div>
      </div>
    </div>

    <!-- Buttons -->
    <div class="home-btns">
      <button class="btn-green" @click="openAutoMatch">Auto Match</button>
      <button class="btn-outline" @click="router.push('/activity/partner')">Scan List</button>
    </div>

    <!-- Auto match modal -->
    <el-dialog
      v-model="matchVisible"
      width="420px"
      :show-close="false"
      class="match-dialog"
      align-center
    >
      <div v-if="matchedUser" class="match-card">
        <button class="match-close" @click="matchVisible = false">×</button>
        <img :src="matchedUser.avatar" class="match-avatar" :alt="matchedUser.nickname">
        <h3 class="match-name">{{ matchedUser.nickname }}</h3>
        <p class="match-bio">{{ matchedUser.bio }}</p>
        <div class="match-tags">
          <span v-for="tag in matchedUser.tags" :key="tag" class="tag-chip active">{{ tag }}</span>
        </div>
        <p class="match-meta">
          📍 {{ formatDistance(matchedUser.distance) }} &nbsp;·&nbsp;
          {{ formatScore(matchedUser.score) }} &nbsp;·&nbsp;
          {{ matchedUser.activities }} activities
        </p>
        <div class="match-btns">
          <button class="btn-green" style="flex:1" @click="applyFriend(matchedUser)">Apply 👥+</button>
          <button class="btn-outline" style="flex:1" @click="nextMatch">Next →</button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getNearbyUsers, getRandomUser, getLeaderboard } from '@/api/dazi'  // ✅ 改导入
import { sendFriendRequest } from '@/api/user'
import { useLocation } from '@/composables/useLocation'
import { formatDistance, formatScore } from '@/utils/format'
import type { NearbyUser } from '@/types/user'

const router = useRouter()
const { updateLocation } = useLocation()

// ── Orbit users ─────────────────────────────────────────────────
const orbitUsers = ref<NearbyUser[]>([])

/** Fixed CSS positions for up to 6 orbit avatars, mirroring the Figma layout */
const orbitPositions = [
  { top: '18%', left: '43%' },
  { top: '22%', left: '63%' },
  { top: '47%', left: '72%' },
  { top: '66%', left: '57%' },
  { top: '66%', left: '38%' },
  { top: '42%', left: '24%' },
]

const loadOrbitUsers = async () => {
  try {
    const data = await getNearbyUsers({ radius: 10, pageSize: 6 }) as NearbyUser[]  // ✅ 改函数名
    orbitUsers.value = data.slice(0, 6)
  } catch {
    // keep empty — UI gracefully shows no avatars
  }
}

// ── Auto match ──────────────────────────────────────────────────
const matchVisible = ref(false)
const matchedUser  = ref<NearbyUser | null>(null)
let   matchPool    = [] as NearbyUser[]
let   matchIdx     = 0

const openAutoMatch = async () => {
  if (matchPool.length === 0) {
    try {
      matchPool = await getNearbyUsers({ radius: 10, pageSize: 20 }) as NearbyUser[]  // ✅ 改函数名
    } catch {
      ElMessage.error('Failed to load nearby users')
      return
    }
  }
  matchedUser.value  = matchPool[matchIdx % matchPool.length]
  matchVisible.value = true
}

const nextMatch = () => {
  matchIdx++
  matchedUser.value = matchPool[matchIdx % matchPool.length]
}

const applyFriend = async (user: NearbyUser) => {
  try {
    await sendFriendRequest(user.id)
    ElMessage.success(`Friend request sent to ${user.nickname}!`)
    matchVisible.value = false
  } catch { /* handled by interceptor */ }
}

const openUserDetail = (user: NearbyUser) => {
  router.push(`/user/${user.id}`)
}

// ── Lifecycle ───────────────────────────────────────────────────
onMounted(() => {
  updateLocation()
  loadOrbitUsers()
})
</script>

<style scoped>
.home-page {
  position: relative;
  z-index: 1;
  min-height: calc(100vh - var(--nav-height));
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-bottom: 40px;
}

/* ── Globe scene ── */
.globe-scene {
  position: relative;
  width: 700px;
  height: 560px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.orbit-ring {
  position: absolute;
  border-radius: 50%;
  border: 0.5px solid rgba(255,255,255,0.12);
  top: 50%; left: 50%;
  transform: translate(-50%, -50%);
  pointer-events: none;
}
.ring-1 { width: 400px; height: 200px; transform: translate(-50%,-50%) rotateX(75deg); }
.ring-2 { width: 520px; height: 260px; transform: translate(-50%,-50%) rotateX(75deg); }
.ring-3 { width: 640px; height: 320px; transform: translate(-50%,-50%) rotateX(75deg); }

.globe {
  width: 280px; height: 280px;
  border-radius: 50%;
  background:
    radial-gradient(circle at 32% 28%, rgba(120,180,255,.28) 0%, transparent 40%),
    linear-gradient(145deg, #2A5BA8 0%, #1A3E8A 35%, #0E2266 65%, #06144A 100%);
  box-shadow:
    0 0 0 1px rgba(100,160,255,.18),
    0 0 50px rgba(60,100,200,.35),
    0 0 100px rgba(40,80,180,.18),
    inset 0 0 60px rgba(0,0,60,.5);
  position: relative; z-index: 3;
  overflow: hidden;
  animation: globeGlow 4s ease-in-out infinite;
}
@keyframes globeGlow {
  0%,100% { box-shadow: 0 0 50px rgba(60,100,200,.35), inset 0 0 60px rgba(0,0,60,.5); }
  50%      { box-shadow: 0 0 80px rgba(60,100,220,.55), inset 0 0 60px rgba(0,0,60,.5); }
}
.globe::before {
  content: '';
  position: absolute; width: 42%; height: 35%; top: 12%; left: 15%;
  background: radial-gradient(ellipse, rgba(200,230,255,.2) 0%, transparent 70%);
  border-radius: 50%;
}
.globe-land {
  position: absolute; inset: 0; border-radius: 50%;
  background:
    radial-gradient(ellipse 22% 14% at 38% 46%, rgba(60,160,100,.5)  0%, transparent 100%),
    radial-gradient(ellipse 28% 10% at 60% 65%, rgba(50,130,80,.42)  0%, transparent 100%),
    radial-gradient(ellipse 16% 10% at 22% 58%, rgba(55,140,85,.38)  0%, transparent 100%);
  animation: landDrift 20s linear infinite;
}
@keyframes landDrift { from { transform: translateX(0); } to { transform: translateX(-100%); } }

.globe-clouds {
  position: absolute; inset: 0; border-radius: 50%;
  background:
    radial-gradient(ellipse 30% 6% at 40% 28%, rgba(255,255,255,.14) 0%, transparent 100%),
    radial-gradient(ellipse 22% 5% at 65% 48%, rgba(255,255,255,.10) 0%, transparent 100%);
  animation: cloudDrift 45s linear infinite;
}
@keyframes cloudDrift { from { transform: translateX(0); } to { transform: translateX(-50px); } }

/* ── Orbit user avatars ── */
.orbit-user {
  position: absolute;
  width: 44px; height: 44px;
  border-radius: 50%; overflow: visible;
  cursor: pointer; z-index: 5;
}
.orbit-user img {
  width: 44px; height: 44px;
  border-radius: 50%;
  border: 2.5px solid rgba(255,255,255,.45);
  object-fit: cover;
  box-shadow: 0 2px 12px rgba(0,0,0,.5);
  transition: transform .2s, border-color .2s;
}
.orbit-user:hover img { transform: scale(1.2); border-color: var(--color-green); }
.orbit-user:hover .orbit-tooltip { opacity: 1; transform: translateX(-50%) translateY(0); }

.orbit-tooltip {
  position: absolute;
  top: calc(100% + 8px); left: 50%;
  transform: translateX(-50%) translateY(6px);
  background: rgba(18,16,42,.9);
  border: 0.5px solid var(--color-border);
  border-radius: 10px; padding: 6px 10px;
  white-space: nowrap; opacity: 0;
  transition: opacity .2s, transform .2s;
  pointer-events: none; backdrop-filter: blur(10px);
  z-index: 200;
}
.ot-name { font-size: 12px; font-weight: 600; margin-bottom: 2px; }
.ot-tags { font-size: 11px; color: var(--color-text-secondary); }

/* ── Buttons ── */
.home-btns {
  display: flex; gap: 16px;
  margin-top: 8px;
  position: relative; z-index: 10;
}

/* ── Match dialog ── */
:deep(.match-dialog .el-dialog) {
  background: linear-gradient(145deg, #111, #1c1c1c);
  border: 0.5px solid var(--color-border);
  border-radius: var(--radius-xl);
}
:deep(.match-dialog .el-dialog__header) { display: none; }
:deep(.match-dialog .el-dialog__body)   { padding: 0; }

.match-card {
  padding: 32px; display: flex; flex-direction: column;
  align-items: center; gap: 14px; position: relative; text-align: center;
}
.match-close {
  position: absolute; top: 12px; right: 16px;
  background: none; border: none; color: var(--color-text-secondary);
  font-size: 22px; cursor: pointer; line-height: 1;
}
.match-avatar {
  width: 80px; height: 80px; border-radius: 50%;
  object-fit: cover;
  border: 2.5px solid var(--color-green-border);
  box-shadow: 0 0 28px var(--color-green-dim);
}
.match-name   { font-family: var(--font-display); font-size: 22px; }
.match-bio    { font-size: 13px; color: var(--color-text-secondary); line-height: 1.6; }
.match-tags   { display: flex; gap: 7px; flex-wrap: wrap; justify-content: center; }
.match-meta   { font-size: 12px; color: var(--color-text-secondary); }
.match-btns   { display: flex; gap: 10px; width: 100%; }
</style>
