<template>
  <div class="find-partner-view">

    <!-- ── Main area ─────────────────────────── -->
    <div class="fp-main">

      <!-- Stats row (my stats) -->
      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-card-head">activities <span>🔥</span></div>
          <div class="stat-card-val">{{ myStats.activities }} completed</div>
        </div>
        <div class="stat-card">
          <div class="stat-card-head">Positive feedback <span>🌟</span></div>
          <div class="stat-card-val">⭐ {{ myStats.score }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-card-head">Ranking <span>🔖</span></div>
          <div class="stat-card-val">{{ myStats.ranking }}</div>
        </div>
      </div>

      <!-- Section heading + search -->
      <div class="fp-header">
        <p class="fp-heading">
          Find people nearby who are on the same frequency,<br>find your partner.
        </p>
        <div class="fp-search-wrap">
          <el-icon class="fp-search-icon"><Search /></el-icon>
          <input
            v-model="searchQ"
            class="fp-search"
            placeholder="Search nickname or tag ..."
            @input="applyFilters"
          >
        </div>
      </div>

      <!-- Tag filters -->
      <div class="tag-row">
        <button
          v-for="tag in TAG_LIST"
          :key="tag.value"
          class="tag-chip"
          :class="{ active: activeTag === tag.value }"
          @click="selectTag(tag.value)"
        >
          <span>{{ tag.emoji }}</span>{{ tag.label }}
        </button>
        <button class="tag-more">•••</button>
      </div>

      <!-- Partner grid -->
      <div class="partners-grid" v-if="!loading">
        <UserCard
          v-for="user in filteredUsers"
          :key="user.id"
          :user="user"
          @click="openDetail"
          @detail="openDetail"
        />
        <div v-if="filteredUsers.length === 0" class="no-results">
          No partners found nearby. Try adjusting your filters.
        </div>
      </div>
      <div v-else class="grid-loading">
        <el-skeleton v-for="i in 6" :key="i" :rows="3" animated style="margin-bottom:14px" />
      </div>
    </div>

    <!-- ── Leaderboard panel ───────────────────── -->
    <aside class="leaderboard-panel">
      <h3 class="lb-title">leaderboard 📊</h3>

      <!-- Top 3 podium -->
      <div class="lb-podium">
        <!-- Silver (2nd) -->
        <div class="lb-pod silver" v-if="leaderboard[1]">
          <span class="lb-crown">🥈</span>
          <img :src="leaderboard[1].avatar" class="lb-pod-ava silver-ring">
          <div class="lb-pod-name">{{ leaderboard[1].nickname }}</div>
          <div class="lb-pod-score">⭐ {{ leaderboard[1].score }}</div>
          <div class="lb-pod-bar" style="height:40px" />
        </div>
        <!-- Gold (1st) -->
        <div class="lb-pod gold" v-if="leaderboard[0]">
          <span class="lb-crown">🥇</span>
          <img :src="leaderboard[0].avatar" class="lb-pod-ava gold-ring">
          <div class="lb-pod-name">{{ leaderboard[0].nickname }}</div>
          <div class="lb-pod-score">⭐ {{ leaderboard[0].score }}</div>
          <div class="lb-pod-bar" style="height:60px" />
        </div>
        <!-- Bronze (3rd) -->
        <div class="lb-pod bronze" v-if="leaderboard[2]">
          <span class="lb-crown">🥉</span>
          <img :src="leaderboard[2].avatar" class="lb-pod-ava bronze-ring">
          <div class="lb-pod-name">{{ leaderboard[2].nickname }}</div>
          <div class="lb-pod-score">⭐ {{ leaderboard[2].score }}</div>
          <div class="lb-pod-bar" style="height:30px" />
        </div>
      </div>

      <!-- Rank 4+ list -->
      <div class="lb-list">
        <div
          v-for="(u, i) in leaderboard.slice(3)"
          :key="u.id"
          class="lb-item"
          @click="openDetail(u as unknown as NearbyUser)"
        >
          <span class="lb-rank">#{{ i + 4 }}</span>
          <img :src="u.avatar" class="lb-ava">
          <div class="lb-info">
            <div class="lb-name">{{ u.nickname }}</div>
            <div class="lb-dist">📍 {{ formatDistance(u.distance) }}</div>
          </div>
          <span class="lb-score">⭐ {{ u.score }}</span>
        </div>
      </div>
    </aside>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import UserCard from '@/components/discover/UserCard.vue'
import { getNearbyUsers, getLeaderboard } from '@/api/dazi'  // ✅ 改导入
import { useUserStore } from '@/stores/user'
import { formatDistance } from '@/utils/format'
import type { NearbyUser } from '@/types/user'

const router    = useRouter()
const userStore = useUserStore()

// ── State ────────────────────────────────────────────────────────
const users      = ref<NearbyUser[]>([])
const leaderboard= ref<(NearbyUser & { score: number })[]>([])
const loading    = ref(false)
const searchQ    = ref('')
const activeTag  = ref('All')

const myStats = computed(() => ({
  activities: userStore.userInfo?.activities ?? 0,
  score:      userStore.userInfo?.score      ?? 0,
  ranking:    userStore.userInfo?.ranking    ?? '--',
}))

// ── Tag list ──────────────────────────────────────────────────────
const TAG_LIST = [
  { value: 'All',    emoji: '',   label: 'All'     },
  { value: 'read',   emoji: '📚', label: 'read'    },
  { value: 'climb',  emoji: '⛰',  label: 'climb'   },
  { value: 'cycle',  emoji: '🚲', label: 'cycle'   },
  { value: 'photo',  emoji: '📷', label: 'photo'   },
  { value: 'draw',   emoji: '🎨', label: 'draw'    },
  { value: 'music',  emoji: '🎵', label: 'music'   },
  { value: 'shop',   emoji: '🛍', label: 'shop'    },
]

// ── Filtered list ────────────────────────────────────────────────
const filteredUsers = computed(() => {
  let list = [...users.value]
  if (activeTag.value !== 'All')
    list = list.filter(u => u.tags.some(t => t.toLowerCase().includes(activeTag.value.toLowerCase())))
  if (searchQ.value)
    list = list.filter(u =>
      u.nickname.toLowerCase().includes(searchQ.value.toLowerCase()) ||
      u.tags.join(' ').toLowerCase().includes(searchQ.value.toLowerCase())
    )
  return list
})

// ── Actions ──────────────────────────────────────────────────────
const selectTag   = (tag: string) => { activeTag.value = tag }
const applyFilters= () => { /* reactive via computed */ }
const openDetail  = (user: NearbyUser) => router.push(`/user/${user.id}`)

// ── Load data ────────────────────────────────────────────────────
const loadData = async () => {
  loading.value = true
  try {
    const [usersRes, lbRes] = await Promise.all([
      getNearbyUsers({ radius: 10, pageSize: 30 }),  // ✅ 改函数名
      getLeaderboard(),  // ✅ 改为无参数
    ])
    users.value = usersRes as NearbyUser[]
    leaderboard.value = lbRes as (NearbyUser & { score: number })[]
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.find-partner-view { display: flex; flex: 1; overflow: hidden; height: 100%; }

/* ── Main ── */
.fp-main {
  flex: 1; padding: 24px 24px 20px;
  overflow-y: auto; display: flex; flex-direction: column; gap: 20px;
}

.stats-row { display: grid; grid-template-columns: repeat(3,1fr); gap: 14px; }
.stat-card {
  background: var(--color-surface-2); border: 0.5px solid var(--color-border);
  border-radius: var(--radius-lg); padding: 18px 20px;
}
.stat-card-head { font-size: 13px; color: rgba(255,255,255,.8); margin-bottom: 6px; display: flex; align-items: center; justify-content: space-between; }
.stat-card-val  { font-family: var(--font-display); font-size: 20px; }

.fp-header {
  display: flex; align-items: flex-end; justify-content: space-between; gap: 16px;
}
.fp-heading { font-family: var(--font-display); font-size: 17px; line-height: 1.4; max-width: 380px; }
.fp-search-wrap { position: relative; }
.fp-search-icon { position: absolute; left: 11px; top: 50%; transform: translateY(-50%); color: var(--color-text-secondary); }
.fp-search {
  padding: 9px 14px 9px 32px; border-radius: var(--radius-full);
  background: var(--color-surface-2); border: 0.5px solid var(--color-border);
  color: var(--color-white); font-size: 13px; width: 220px;
}
.fp-search::placeholder { color: var(--color-text-hint); }

.tag-row  { display: flex; gap: 8px; flex-wrap: wrap; align-items: center; }
.tag-more {
  padding: 6px 14px; border-radius: var(--radius-full); font-size: 12px;
  border: 1px solid var(--color-border); color: var(--color-text-secondary);
  background: var(--color-surface-1); cursor: pointer;
}

.partners-grid {
  display: grid; grid-template-columns: repeat(3,1fr); gap: 14px;
  align-content: start;
}
.no-results {
  grid-column: 1 / -1; text-align: center;
  padding: 40px; font-size: 14px; color: var(--color-text-secondary);
}
.grid-loading { display: flex; flex-direction: column; }

/* ── Leaderboard ── */
.leaderboard-panel {
  width: var(--leaderboard-width); flex-shrink: 0;
  background: rgba(255,255,200,.06); backdrop-filter: blur(12px);
  border-left: 0.5px solid var(--color-border-dim);
  padding: 22px 18px; overflow-y: auto;
  display: flex; flex-direction: column; gap: 16px;
}

.lb-title { font-family: var(--font-display); font-size: 18px; }

.lb-podium {
  display: grid; grid-template-columns: 1fr 1.1fr 1fr;
  gap: 6px; align-items: end;
}
.lb-pod { display: flex; flex-direction: column; align-items: center; gap: 4px; }
.lb-crown { font-size: 18px; }
.lb-pod-ava {
  border-radius: 50%; object-fit: cover;
  width: 46px; height: 46px;
  border: 2px solid var(--color-border);
}
.gold-ring   { border-color: #FFD700; width: 54px; height: 54px; }
.silver-ring { border-color: #C0C0C0; }
.bronze-ring { border-color: #CD7F32; }
.lb-pod-name  { font-size: 11px; font-weight: 600; text-align: center; }
.lb-pod-score { font-size: 11px; color: var(--color-text-gold); }
.lb-pod-bar   { width: 100%; border-radius: 4px 4px 0 0; background: rgba(255,255,255,.06); }

.lb-list { display: flex; flex-direction: column; gap: 8px; }
.lb-item {
  display: flex; align-items: center; gap: 10px;
  padding: 8px 10px; border-radius: var(--radius-md);
  background: var(--color-surface-1); border: 0.5px solid var(--color-border-dim);
  cursor: pointer; transition: background .15s;
}
.lb-item:hover { background: var(--color-surface-2); }
.lb-rank  { font-family: var(--font-display); font-size: 13px; color: var(--color-green); min-width: 28px; }
.lb-ava   { width: 32px; height: 32px; border-radius: 50%; object-fit: cover; flex-shrink: 0; }
.lb-info  { flex: 1; }
.lb-name  { font-size: 12px; font-weight: 500; }
.lb-dist  { font-size: 11px; color: var(--color-text-danger); }
.lb-score { font-size: 12px; color: var(--color-text-gold); }
</style>
