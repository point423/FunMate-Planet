<template>
  <div class="find-partner-view">
    <section class="hero-card">
      <div>
        <p class="eyebrow">Partner Finder</p>
        <h1>Find a buddy who matches your pace.</h1>
        <p class="hero-copy">
          Browse nearby people, filter by nickname or tag, and jump straight into their
          profile when someone feels like a good fit.
        </p>
      </div>
      <div class="hero-stats">
        <div class="stat-card">
          <span class="stat-label">Activities</span>
          <strong>{{ myStats.activities }}</strong>
        </div>
        <div class="stat-card">
          <span class="stat-label">Positive Rate</span>
          <strong>{{ myStats.score }}</strong>
        </div>
        <div class="stat-card">
          <span class="stat-label">Ranking</span>
          <strong>{{ myStats.ranking }}</strong>
        </div>
      </div>
    </section>

    <section class="search-row">
      <div class="tag-filter-strip">
        <button
          class="tag-filter-chip"
          :class="{ active: !activeTag }"
          type="button"
          @click="applyTagFilter('')"
        >
          All
        </button>
        <button
          v-for="tag in allTags"
          :key="tag.value"
          class="tag-filter-chip"
          :class="{ active: activeTag === tag.value }"
          type="button"
          @click="applyTagFilter(tag.value)"
        >
          <span>{{ tag.emoji }}</span>{{ tag.label }}
        </button>
      </div>
      <div class="search-shell">
        <el-icon class="search-icon"><Search /></el-icon>
        <input
          v-model="searchQ"
          class="search-input"
          placeholder="Search by nickname"
        >
      </div>
    </section>

    <section class="content-shell">
      <div class="partners-panel">
        <section class="partner-grid" v-loading="loading">
          <UserCard
            v-for="user in filteredUsers"
            :key="user.id"
            :user="user"
            @select="openUserDetail"
          />
          <div v-if="!loading && filteredUsers.length === 0" class="empty-state">
            No matching partner yet. Try a different keyword or tag.
          </div>
        </section>
      </div>

      <aside class="leaderboard-card" v-loading="loading">
        <div class="leaderboard-head">
          <div>
            <p class="leaderboard-kicker">Leaderboard</p>
            <h2>Top rated partners</h2>
          </div>
          <span class="leaderboard-chip">By positive rate</span>
        </div>

        <div v-if="podiumUsers.length" class="podium-shell">
          <div class="podium">
            <button
              v-for="entry in podiumUsers"
              :key="entry.rank"
              class="podium-step"
              :class="`rank-${entry.rank}`"
              type="button"
              @click="openUserDetail(entry.user)"
            >
              <span class="podium-medal">{{ entry.medal }}</span>
              <img :src="entry.user.avatar" :alt="entry.user.nickname" class="podium-avatar">
              <strong>{{ entry.user.nickname }}</strong>
              <span class="podium-score">{{ formatScore(entry.user.score, entry.user.reviewCount) }}</span>
              <span class="podium-base">No.{{ entry.rank }}</span>
            </button>
          </div>
        </div>

        <div v-else-if="!loading" class="leaderboard-empty">
          The ranking will appear after the first round of reviews.
        </div>

        <div v-if="restLeaderboard.length" class="leaderboard-list">
          <button
            v-for="(user, index) in restLeaderboard"
            :key="user.id"
            class="leaderboard-row"
            type="button"
            @click="openUserDetail(user)"
          >
            <span class="leaderboard-rank">#{{ index + 4 }}</span>
            <img :src="user.avatar" :alt="user.nickname" class="leaderboard-avatar">
            <div class="leaderboard-meta">
              <strong>{{ user.nickname }}</strong>
              <span>{{ user.activities }} activities</span>
            </div>
            <span class="leaderboard-value">{{ formatScore(user.score, user.reviewCount) }}</span>
          </button>
        </div>
      </aside>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import UserCard from '@/components/discover/UserCard.vue'
import { getLeaderboard, getNearbyUsers } from '@/api/dazi'
import { getFriends } from '@/api/user'
import { useUserStore } from '@/stores/user'
import type { NearbyUser } from '@/types/user'
import { TAG_META_LIST } from '@/utils/tags'
import { formatScore } from '@/utils/format'

const router = useRouter()
const userStore = useUserStore()

type RawData = Record<string, unknown>

const users = ref<NearbyUser[]>([])
const leaderboard = ref<NearbyUser[]>([])
const loading = ref(false)
const searchQ = ref('')
const activeTag = ref('')
const allTags = TAG_META_LIST

const myStats = computed(() => ({
  activities: userStore.userInfo?.activities ?? 0,
  score: formatScore(userStore.userInfo?.score ?? 0, userStore.userInfo?.reviewCount ?? 0),
  ranking: userStore.userInfo?.ranking ?? '99+',
}))

const filteredUsers = computed(() => {
  const keyword = searchQ.value.trim().toLowerCase()
  const selectedTag = activeTag.value.trim().toLowerCase()

  return users.value.filter((user) => {
    const matchesKeyword = !keyword || user.nickname?.toLowerCase().includes(keyword)
    const matchesTag =
      !selectedTag || user.tags.some((tag) => tag.trim().toLowerCase() === selectedTag)

    return matchesKeyword && matchesTag
  })
})

const toRecord = (value: unknown): RawData =>
  value && typeof value === 'object' ? value as RawData : {}

const toNumber = (value: unknown, fallback = 0) => {
  const nested = toRecord(value)
  const resolved = 'parsedValue' in nested ? nested.parsedValue : value
  const result = Number(resolved)
  return Number.isFinite(result) ? result : fallback
}

const toStringArray = (value: unknown) => {
  if (Array.isArray(value)) return value.map((item) => String(item).trim()).filter(Boolean)
  if (typeof value === 'string') {
    return value.split(/(?:[;,]+|\uFF0C|\u3001)/).map((item) => item.trim()).filter(Boolean)
  }
  return []
}

const resolveAvatar = (value: unknown) => {
  if (typeof value === 'string' && value.trim()) return value
  const nested = toRecord(value)
  return typeof nested.url === 'string' && nested.url.trim() ? nested.url : '/default-avatar.png'
}

const extractList = (payload: unknown) => {
  if (Array.isArray(payload)) return payload
  const nested = toRecord(payload)
  return Array.isArray(nested.content) ? nested.content : []
}

const normalizePartner = (raw: RawData): NearbyUser => ({
  id: toNumber(raw.id),
  username: String(raw.username ?? raw.nickname ?? ''),
  nickname: String(raw.nickname ?? raw.username ?? 'Anonymous'),
  avatar: resolveAvatar(raw.avatar),
  bio: String(raw.bio ?? 'No introduction yet.'),
  tags: toStringArray(raw.tags),
  activities: toNumber(raw.activities),
  score: toNumber(raw.score ?? raw.averageScore),
  reviewCount: raw.reviewCount == null ? undefined : toNumber(raw.reviewCount),
  ranking: toNumber(raw.ranking),
  longitude: raw.longitude == null ? undefined : toNumber(raw.longitude),
  latitude: raw.latitude == null ? undefined : toNumber(raw.latitude),
  createdAt: String(raw.createdAt ?? ''),
  distance: toNumber(raw.distance),
})

const getFriendId = (friend: unknown) => {
  const record = toRecord(friend)
  return toNumber(record.id ?? record.friendId ?? record.userId)
}

const podiumUsers = computed(() => {
  const [first, second, third] = leaderboard.value
  return [
    second ? { rank: 2, medal: '2', user: second } : null,
    first ? { rank: 1, medal: '1', user: first } : null,
    third ? { rank: 3, medal: '3', user: third } : null,
  ].filter((entry): entry is { rank: number; medal: string; user: NearbyUser } => Boolean(entry))
})

const restLeaderboard = computed(() => leaderboard.value.slice(3))

const loadUsers = async (refreshLeaderboard = true) => {
  loading.value = true
  try {
    const requests: Promise<unknown>[] = [
      getNearbyUsers({
        radius: 10,
        tags: activeTag.value || undefined,
      }) as Promise<unknown>,
      getFriends() as Promise<unknown>,
    ]

    if (refreshLeaderboard) {
      requests.push(getLeaderboard() as Promise<unknown>)
    }

    const [nearbyResult, friendsResult, leaderboardResult] = await Promise.allSettled(requests)

    const friendsPayload = friendsResult.status === 'fulfilled' ? friendsResult.value : []
    const rankingPayload =
      refreshLeaderboard && leaderboardResult?.status === 'fulfilled' ? leaderboardResult.value : []
    const friends = extractList(friendsPayload)
    const rankingList = extractList(rankingPayload)
    const currentUserId = Number(userStore.userInfo?.id ?? 0)
    const friendIds = new Set(friends.map(getFriendId).filter(Boolean))

    if (nearbyResult.status === 'fulfilled') {
      const list = extractList(nearbyResult.value)
      users.value = list
        .map((item) => normalizePartner(toRecord(item)))
        .filter((user) => {
          const userId = Number(user.id ?? 0)
          return userId && userId !== currentUserId && !friendIds.has(userId)
        })
    } else {
      users.value = []
    }

    if (refreshLeaderboard) {
      leaderboard.value = rankingList
        .map((item) => normalizePartner(toRecord(item)))
        .filter((user) => Number(user.id ?? 0))
    }

    if (nearbyResult.status === 'rejected' && refreshLeaderboard && leaderboardResult?.status === 'rejected') {
      ElMessage.error('Failed to load nearby users and leaderboard.')
    } else if (nearbyResult.status === 'rejected') {
      ElMessage.error('Failed to load nearby users.')
    } else if (refreshLeaderboard && leaderboardResult?.status === 'rejected') {
      ElMessage.warning('Leaderboard is temporarily unavailable.')
    }
  } finally {
    loading.value = false
  }
}

const applyTagFilter = async (tag: string) => {
  activeTag.value = activeTag.value === tag ? '' : tag
  await loadUsers(false)
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
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(280px, 360px);
  align-items: start;
  gap: 16px;
}

.tag-filter-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  min-width: 0;
}

.tag-filter-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 9px 14px;
  border: 1px solid var(--color-border);
  border-radius: 999px;
  background: var(--color-surface-2);
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease, color 0.2s ease, background 0.2s ease;
}

.tag-filter-chip span {
  line-height: 1;
}

.tag-filter-chip:hover {
  transform: translateY(-1px);
  border-color: rgba(0, 255, 149, 0.4);
  color: var(--color-text);
}

.tag-filter-chip.active {
  background: rgba(0, 255, 149, 0.14);
  border-color: rgba(0, 255, 149, 0.55);
  color: var(--color-green);
}

.search-shell {
  width: min(360px, 100%);
  position: relative;
  justify-self: end;
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

.content-shell {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(300px, 340px);
  gap: 20px;
  align-items: start;
}

.partners-panel {
  min-width: 0;
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

.leaderboard-card {
  position: sticky;
  top: 24px;
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding: 22px;
  border: 1px solid var(--color-border);
  border-radius: 24px;
  background:
    radial-gradient(circle at top center, rgba(255, 215, 0, 0.16), transparent 42%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.08), rgba(255, 255, 255, 0.03));
  backdrop-filter: blur(12px);
}

.leaderboard-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.leaderboard-head h2 {
  margin: 6px 0 0;
  font-size: 24px;
}

.leaderboard-kicker {
  margin: 0;
  color: #ffdd67;
  text-transform: uppercase;
  letter-spacing: 0.14em;
  font-size: 11px;
}

.leaderboard-chip {
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.1);
  color: var(--color-text-secondary);
  font-size: 11px;
  white-space: nowrap;
}

.podium-shell {
  padding: 8px 0 4px;
}

.podium {
  display: flex;
  align-items: end;
  justify-content: center;
  gap: 12px;
  min-height: 250px;
}

.podium-step {
  width: min(100px, 31%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  padding: 14px 10px 0;
  border: 1px solid transparent;
  border-radius: 22px 22px 14px 14px;
  color: var(--color-text);
  cursor: pointer;
  background: transparent;
  transition: transform 0.2s ease, border-color 0.2s ease, filter 0.2s ease;
}

.podium-step:hover {
  transform: translateY(-4px);
  filter: brightness(1.05);
}

.podium-step strong {
  font-size: 13px;
  text-align: center;
  line-height: 1.2;
}

.podium-medal {
  position: relative;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 15px;
  box-shadow: inset 0 2px 4px rgba(255, 255, 255, 0.28);
}

.podium-medal::before,
.podium-medal::after {
  content: '';
  position: absolute;
  top: -10px;
  width: 9px;
  height: 16px;
  border-radius: 4px 4px 1px 1px;
  background: linear-gradient(180deg, #7dc6ff, #315bff);
}

.podium-medal::before {
  left: 7px;
  transform: rotate(-12deg);
}

.podium-medal::after {
  right: 7px;
  transform: rotate(12deg);
  background: linear-gradient(180deg, #ff7b7b, #d94343);
}

.podium-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid rgba(255, 255, 255, 0.18);
}

.podium-score {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.78);
  text-align: center;
}

.podium-base {
  width: 100%;
  margin-top: auto;
  padding: 16px 8px;
  border-radius: 18px 18px 12px 12px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.rank-1 {
  height: 238px;
}

.rank-1 .podium-medal {
  background: linear-gradient(180deg, #ffe892, #f3b221);
  color: #5f4100;
}

.rank-1 .podium-base {
  background: linear-gradient(180deg, rgba(255, 221, 103, 0.96), rgba(217, 145, 12, 0.92));
  color: #442900;
}

.rank-2 {
  height: 206px;
}

.rank-2 .podium-medal {
  background: linear-gradient(180deg, #f2f5fb, #b5bfd0);
  color: #354052;
}

.rank-2 .podium-base {
  background: linear-gradient(180deg, rgba(219, 227, 239, 0.92), rgba(138, 149, 170, 0.92));
  color: #243041;
}

.rank-3 {
  height: 182px;
}

.rank-3 .podium-medal {
  background: linear-gradient(180deg, #e7b28f, #b76b34);
  color: #4f2608;
}

.rank-3 .podium-base {
  background: linear-gradient(180deg, rgba(214, 147, 101, 0.94), rgba(137, 76, 42, 0.94));
  color: #fff2e7;
}

.leaderboard-empty {
  padding: 24px 18px;
  border: 1px dashed rgba(255, 255, 255, 0.14);
  border-radius: 18px;
  color: var(--color-text-secondary);
  text-align: center;
  line-height: 1.6;
}

.leaderboard-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.leaderboard-row {
  width: 100%;
  display: grid;
  grid-template-columns: auto auto minmax(0, 1fr) auto;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 18px;
  background: rgba(0, 0, 0, 0.22);
  color: var(--color-text);
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease, background 0.2s ease;
}

.leaderboard-row:hover {
  transform: translateX(3px);
  border-color: rgba(255, 255, 255, 0.18);
  background: rgba(255, 255, 255, 0.08);
}

.leaderboard-rank {
  font-size: 12px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.72);
}

.leaderboard-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid rgba(255, 255, 255, 0.14);
}

.leaderboard-meta {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.leaderboard-meta strong,
.leaderboard-value {
  font-size: 13px;
}

.leaderboard-meta strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.leaderboard-meta span {
  font-size: 11px;
  color: var(--color-text-secondary);
}

.leaderboard-value {
  color: #ffd56a;
  text-align: right;
}

@media (max-width: 960px) {
  .hero-card {
    grid-template-columns: 1fr;
  }

  .hero-stats {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .content-shell {
    grid-template-columns: 1fr;
  }

  .leaderboard-card {
    position: static;
  }

  .search-row {
    grid-template-columns: 1fr;
  }

  .search-shell {
    width: 100%;
    justify-self: stretch;
  }
}

@media (max-width: 720px) {
  .podium {
    gap: 8px;
    min-height: 220px;
  }

  .podium-step {
    width: min(110px, 32%);
    padding-inline: 8px;
  }

  .podium-avatar {
    width: 48px;
    height: 48px;
  }

  .leaderboard-row {
    grid-template-columns: auto auto minmax(0, 1fr);
  }

  .leaderboard-value {
    grid-column: 2 / 4;
    justify-self: end;
  }
}
</style>
