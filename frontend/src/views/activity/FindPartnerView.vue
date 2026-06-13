<template>
  <div class="find-partner-view">
    <section class="hero-card">
      <div>
        <p class="eyebrow">Partner Finder</p>
        <h1>Find a buddy who matches your pace.</h1>
        <p class="hero-copy">
          Browse nearby people, filter by name, and jump straight into their profile when
          someone feels like a good fit.
        </p>
      </div>
      <div class="hero-stats">
        <div class="stat-card">
          <span class="stat-label">Activities</span>
          <strong>{{ myStats.activities }}</strong>
        </div>
        <div class="stat-card">
          <span class="stat-label">Score</span>
          <strong>{{ myStats.score }}</strong>
        </div>
        <div class="stat-card">
          <span class="stat-label">Ranking</span>
          <strong>{{ myStats.ranking }}</strong>
        </div>
      </div>
    </section>

    <section class="search-row">
      <div class="search-shell">
        <el-icon class="search-icon"><Search /></el-icon>
        <input
          v-model="searchQ"
          class="search-input"
          placeholder="Search by nickname"
        >
      </div>
    </section>

    <section class="partner-grid" v-loading="loading">
      <UserCard
        v-for="user in filteredUsers"
        :key="user.id"
        :user="user"
        @select="openUserDetail"
      />
      <div v-if="!loading && filteredUsers.length === 0" class="empty-state">
        No matching partner yet. Try a different keyword.
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import UserCard from '@/components/discover/UserCard.vue'
import { getNearbyUsers } from '@/api/dazi'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const users = ref<any[]>([])
const loading = ref(false)
const searchQ = ref('')

const myStats = computed(() => ({
  activities: userStore.userInfo?.activities ?? 0,
  score: userStore.userInfo?.score ?? 0,
  ranking: userStore.userInfo?.ranking ?? '99+',
}))

const filteredUsers = computed(() => {
  if (!searchQ.value) return users.value
  const keyword = searchQ.value.toLowerCase()
  return users.value.filter((user) => user.nickname?.toLowerCase().includes(keyword))
})

const loadUsers = async () => {
  loading.value = true
  try {
    const result = await getNearbyUsers({ radius: 10 }) as any
    users.value = Array.isArray(result) ? result : result?.content ?? []
  } catch {
    ElMessage.error('Failed to load nearby users.')
  } finally {
    loading.value = false
  }
}

const openUserDetail = (user: { id: number }) => {
  router.push(`/user/${user.id}`)
}

onMounted(loadUsers)
</script>

<style scoped>
.find-partner-view {
  flex: 1;
  overflow-y: auto;
  padding: 28px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.hero-card {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(280px, 1fr);
  gap: 20px;
  padding: 28px;
  border: 1px solid var(--color-border);
  border-radius: 24px;
  background:
    radial-gradient(circle at top left, rgba(0, 255, 149, 0.14), transparent 36%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.06), rgba(255, 255, 255, 0.02));
}

.eyebrow {
  margin: 0 0 8px;
  color: var(--color-green);
  text-transform: uppercase;
  letter-spacing: 0.16em;
  font-size: 12px;
}

.hero-card h1 {
  margin: 0;
  font-size: 34px;
  line-height: 1.1;
}

.hero-copy {
  max-width: 620px;
  margin: 14px 0 0;
  color: var(--color-text-secondary);
  line-height: 1.7;
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  align-self: end;
}

.stat-card {
  padding: 16px;
  border-radius: 18px;
  background: rgba(0, 0, 0, 0.32);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.stat-card strong {
  display: block;
  margin-top: 10px;
  font-size: 22px;
}

.stat-label {
  font-size: 12px;
  color: var(--color-text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.search-row {
  display: flex;
  justify-content: flex-end;
}

.search-shell {
  width: min(360px, 100%);
  position: relative;
}

.search-icon {
  position: absolute;
  top: 50%;
  left: 14px;
  transform: translateY(-50%);
  color: var(--color-text-hint);
}

.search-input {
  width: 100%;
  padding: 11px 16px 11px 40px;
  border-radius: 999px;
  border: 1px solid var(--color-border);
  background: var(--color-surface-2);
  color: var(--color-text);
}

.partner-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.empty-state {
  grid-column: 1 / -1;
  padding: 40px;
  text-align: center;
  border: 1px dashed var(--color-border);
  border-radius: 20px;
  color: var(--color-text-secondary);
}

@media (max-width: 960px) {
  .hero-card {
    grid-template-columns: 1fr;
  }

  .hero-stats {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}
</style>
