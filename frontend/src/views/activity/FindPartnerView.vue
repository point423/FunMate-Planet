<template>
  <div class="find-partner-view">
    <!-- ── Main area ─────────────────────────── -->
    <div class="fp-main">
      <!-- Stats row -->
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

      <!-- Mode Switcher -->
      <div class="mode-header">
        <div class="mode-tabs">
          <button
            :class="{ active: mode === 'partners' }"
            @click="mode = 'partners'"
          >
            Find Partners
          </button>
          <button
            :class="{ active: mode === 'activities' }"
            @click="mode = 'activities'"
          >
            Find Activities
          </button>
        </div>

        <div class="fp-search-wrap">
          <el-icon class="fp-search-icon"><Search /></el-icon>
          <input
            v-model="searchQ"
            class="fp-search"
            :placeholder="mode === 'partners' ? 'Search nickname...' : 'Search activity...'"
          >
        </div>
      </div>

      <!-- Content Area -->
      <div class="content-scroll">
        <!-- Partners Grid -->
        <div v-if="mode === 'partners'" class="partners-grid">
          <UserCard
            v-for="user in filteredUsers"
            :key="user.id"
            :user="user"
            @select="openUserDetail"
          />
        </div>

        <!-- Activities Grid -->
        <div v-else class="activities-grid">
          <ActivityCard
            v-for="act in filteredActivities"
            :key="act.id"
            :activity="act"
            @join="handleJoin"
          />
        </div>

        <div v-if="isEmpty" class="no-results">
          No items found. Try adjusting your filters.
        </div>
      </div>
    </div>

    <!-- ── Leaderboard panel (Right) ───────────────────── -->
    <aside class="leaderboard-panel">
       <h3 class="lb-title">Top Players 📊</h3>
       <!-- Leaderboard items... (keep existing logic) -->
       <div class="lb-list">
        <div v-for="(u, i) in leaderboard" :key="u.id" class="lb-item">
          <span class="lb-rank">#{{ i + 1 }}</span>
          <img :src="u.avatar" class="lb-ava">
          <div class="lb-info">
            <div class="lb-name">{{ u.nickname }}</div>
            <div class="lb-dist">⭐ {{ u.score }}</div>
          </div>
        </div>
      </div>
    </aside>

    <!-- Join Dialog -->
    <el-dialog v-model="joinDialogVisible" title="Join Activity" width="400px">
      <p>Do you want to join this activity? You can chat with partners after joining.</p>
      <template #footer>
        <el-button @click="joinDialogVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="joining" @click="confirmJoin">Join</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import UserCard from '@/components/discover/UserCard.vue'
import ActivityCard from '@/components/activity/ActivityCard.vue'
import { getNearbyUsers, getLeaderboard } from '@/api/dazi'
import { joinActivity } from '@/api/activity'
import request from '@/api/index'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// State
const mode = ref<'partners' | 'activities'>('partners')
const users = ref([])
const activities = ref([])
const leaderboard = ref([])
const searchQ = ref('')
const loading = ref(false)

const joinDialogVisible = ref(false)
const selectedActId = ref<number | null>(null)
const joining = ref(false)

// Computed
const myStats = computed(() => ({
  activities: userStore.userInfo?.activities ?? 0,
  score: userStore.userInfo?.score ?? 5.0,
  ranking: userStore.userInfo?.ranking ?? '99+',
}))

const filteredUsers = computed(() => {
  if (!searchQ.value) return users.value
  return users.value.filter(u => u.nickname.toLowerCase().includes(searchQ.value.toLowerCase()))
})

const filteredActivities = computed(() => {
  if (!searchQ.value) return activities.value
  return activities.value.filter(a => a.title.toLowerCase().includes(searchQ.value.toLowerCase()))
})

const isEmpty = computed(() => {
  return (mode.value === 'partners' && filteredUsers.value.length === 0) ||
         (mode.value === 'activities' && filteredActivities.value.length === 0)
})

// Actions
const loadData = async () => {
  loading.value = true
  try {
    const [usersRes, lbRes, actRes] = await Promise.all([
      getNearbyUsers({ radius: 10 }),
      getLeaderboard(),
      request.get('/activities', { params: { pageNum: 0, pageSize: 20 } })
    ])
    users.value = usersRes
    leaderboard.value = lbRes
    activities.value = actRes.content // Spring Data Page result
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const openUserDetail = (user) => router.push(`/user/${user.id}`)

const handleJoin = (id: number) => {
  selectedActId.value = id
  joinDialogVisible.value = true
}

const confirmJoin = async () => {
  if (!selectedActId.value) return
  joining.value = true
  try {
    await joinActivity(selectedActId.value)
    ElMessage.success('Joined successfully! Go to chat to find your partners.')
    joinDialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('Failed to join')
  } finally {
    joining.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.find-partner-view { display: flex; flex: 1; height: 100%; overflow: hidden; }
.fp-main { flex: 1; padding: 24px; display: flex; flex-direction: column; gap: 24px; overflow: hidden; }

.stats-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
.stat-card {
  background: var(--color-surface-2); border: 1px solid var(--color-border);
  border-radius: var(--radius-lg); padding: 16px;
}
.stat-card-head { font-size: 12px; color: var(--color-text-secondary); margin-bottom: 4px; }
.stat-card-val { font-size: 18px; font-weight: bold; }

.mode-header { display: flex; justify-content: space-between; align-items: center; }
.mode-tabs { display: flex; background: var(--color-surface-1); padding: 4px; border-radius: var(--radius-md); }
.mode-tabs button {
  padding: 6px 16px; border: none; background: transparent; color: var(--color-text-secondary);
  cursor: pointer; border-radius: 4px; font-size: 14px; transition: all 0.2s;
}
.mode-tabs button.active { background: var(--color-surface-2); color: var(--color-white); box-shadow: 0 2px 4px rgba(0,0,0,0.2); }

.fp-search-wrap { position: relative; }
.fp-search {
  background: var(--color-surface-2); border: 1px solid var(--color-border);
  padding: 8px 12px 8px 32px; border-radius: 20px; color: white; width: 200px;
}
.fp-search-icon { position: absolute; left: 10px; top: 50%; transform: translateY(-50%); color: var(--color-text-hint); }

.content-scroll { flex: 1; overflow-y: auto; padding-bottom: 20px; }
.partners-grid, .activities-grid {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px;
}

.leaderboard-panel { width: 300px; border-left: 1px solid var(--color-border); padding: 24px; background: rgba(255,255,255,0.02); }
.lb-title { margin-top: 0; margin-bottom: 20px; font-size: 18px; }
.lb-list { display: flex; flex-direction: column; gap: 12px; }
.lb-item { display: flex; align-items: center; gap: 12px; padding: 10px; background: var(--color-surface-1); border-radius: var(--radius-md); }
.lb-rank { font-weight: bold; color: var(--color-green); width: 24px; }
.lb-ava { width: 32px; height: 32px; border-radius: 50%; }
.lb-info { flex: 1; }
.lb-name { font-size: 13px; font-weight: 500; }
</style>
