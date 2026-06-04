<template>
  <div class="user-detail-page">
    <div class="profile-inner" v-if="user">

      <!-- Header card -->
      <div class="profile-header glass-card">
        <img :src="user.avatar" class="ph-ava" alt="avatar">

        <div class="ph-info">
          <h2 class="ph-name">{{ user.nickname }}</h2>
          <p class="ph-bio">{{ user.bio }}</p>
          <div class="ph-tags">
            <span v-for="tag in user.tags" :key="tag" class="tag-chip active">{{ tag }}</span>
          </div>
        </div>

        <div class="ph-stats">
          <div class="ph-stat">
            <div class="ph-stat-label">activities</div>
            <div class="ph-stat-num">{{ user.activities }}</div>
            <div class="ph-stat-sub">completed</div>
          </div>
          <div class="ph-stat">
            <div class="ph-stat-label">positive feedback</div>
            <div class="ph-stat-num" style="color:var(--color-text-gold)">⭐ {{ user.score }}</div>
          </div>
          <div class="ph-stat">
            <div class="ph-stat-label">ranking</div>
            <div class="ph-stat-num">{{ user.ranking }}</div>
          </div>
        </div>
      </div>

      <!-- Grid: journal + activities -->
      <div class="profile-grid">

        <div class="profile-section glass-card">
          <h3 class="ps-title">📒 journal</h3>
          <div class="journal-wall">
            <div
              v-for="j in user.publicJournals"
              :key="j.id"
              class="jw-thumb"
            >
              <img :src="j.coverImage" :alt="j.title">
            </div>
            <div v-if="user.publicJournals.length === 0" class="ps-empty">No public journals</div>
          </div>
        </div>

        <div class="profile-section glass-card">
          <h3 class="ps-title">🔴 activities</h3>
          <div class="act-list">
            <div v-for="act in user.recentActivities" :key="act.id" class="act-row">
              <span class="act-icon">{{ act.icon }}</span>
              <div class="act-row-info">
                <div class="act-row-name">{{ act.name }}</div>
                <div class="act-row-meta">
                  <div class="act-avs">
                    <img
                      v-for="av in act.participants.slice(0,3)"
                      :key="av"
                      :src="av"
                      class="act-av"
                    >
                  </div>
                </div>
              </div>
              <span class="act-date">{{ act.date }}</span>
            </div>
            <div v-if="user.recentActivities.length === 0" class="ps-empty">No activities</div>
          </div>
        </div>

      </div>

      <!-- Apply / Discard actions -->
      <div class="action-row">
        <button class="btn-apply" :disabled="applying" @click="applyFriend">
          <span>Apply</span> 👥+
        </button>
        <button class="btn-discard" @click="discard">
          discard ✕
        </button>
      </div>
    </div>

    <!-- Loading -->
    <div v-else-if="loading" class="page-loading">
      <el-skeleton :rows="8" animated style="max-width:900px;margin:40px auto;padding:0 40px" />
    </div>

    <!-- Error -->
    <div v-else class="page-error">
      <p>User not found.</p>
      <button class="btn-outline" @click="router.back()">← Go back</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUserById, sendFriendRequest } from '@/api/user'
import type { NearbyUser } from '@/types/user'

const route  = useRoute()
const router = useRouter()

const user    = ref<NearbyUser | null>(null)
const loading = ref(false)
const applying= ref(false)

const loadUser = async (id: number) => {
  loading.value = true
  try { user.value = await getUserById(id) as NearbyUser }
  catch { user.value = null }
  finally { loading.value = false }
}

const applyFriend = async () => {
  if (!user.value) return
  applying.value = true
  try {
    await sendFriendRequest(user.value.id)
    ElMessage.success(`Friend request sent to ${user.value.nickname}!`)
    router.back()
  } finally { applying.value = false }
}

const discard = () => router.back()

onMounted(() => {
  const id = Number(route.params.id)
  if (id) loadUser(id)
})

watch(() => route.params.id, (id) => { if (id) loadUser(Number(id)) })
</script>

<style scoped>
.user-detail-page {
  position: relative; z-index: 1;
  overflow-y: auto; min-height: calc(100vh - var(--nav-height));
}
.profile-inner {
  max-width: 1060px; margin: 0 auto;
  padding: 36px 40px 60px;
  display: flex; flex-direction: column; gap: 22px;
}

/* Header */
.profile-header { display: flex; align-items: center; gap: 24px; padding: 28px 32px; }
.ph-ava {
  width: 84px; height: 84px; border-radius: 50%;
  object-fit: cover; border: 2.5px solid var(--color-border);
  flex-shrink: 0;
}
.ph-info { flex: 1; }
.ph-name { font-family: var(--font-display); font-size: 26px; margin-bottom: 6px; }
.ph-bio  { font-size: 13px; color: var(--color-text-secondary); margin-bottom: 12px; line-height: 1.55; }
.ph-tags { display: flex; gap: 7px; flex-wrap: wrap; }

.ph-stats { display: flex; gap: 0; flex-shrink: 0; }
.ph-stat  { text-align: center; padding: 0 24px; border-left: 1px solid var(--color-border-dim); }
.ph-stat:first-child { border-left: none; }
.ph-stat-label { font-size: 12px; color: var(--color-text-secondary); margin-bottom: 5px; }
.ph-stat-num   { font-family: var(--font-display); font-size: 24px; }
.ph-stat-sub   { font-size: 12px; color: var(--color-text-secondary); }

/* Grid */
.profile-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.profile-section { padding: 22px; }
.ps-title { font-family: var(--font-display); font-size: 17px; margin-bottom: 16px; }
.ps-empty { font-size: 13px; color: var(--color-text-hint); text-align: center; padding: 20px 0; }

.journal-wall { display: grid; grid-template-columns: repeat(3,1fr); gap: 7px; }
.jw-thumb { border-radius: 8px; overflow: hidden; background: #333; aspect-ratio: 1; cursor: pointer; }
.jw-thumb img { width: 100%; height: 100%; object-fit: cover; transition: transform .2s; }
.jw-thumb:hover img { transform: scale(1.06); }

.act-list { display: flex; flex-direction: column; gap: 14px; }
.act-row  { display: flex; align-items: center; gap: 12px; }
.act-icon { font-size: 18px; flex-shrink: 0; }
.act-row-info { flex: 1; }
.act-row-name { font-size: 14px; font-weight: 500; margin-bottom: 4px; }
.act-row-meta { display: flex; align-items: center; gap: 8px; }
.act-avs { display: flex; }
.act-av  { width: 20px; height: 20px; border-radius: 50%; object-fit: cover; border: 1.5px solid #000; margin-left: -5px; }
.act-av:first-child { margin-left: 0; }
.act-date { font-size: 12px; color: var(--color-text-secondary); flex-shrink: 0; }

/* Action row */
.action-row {
  display: flex; gap: 16px; justify-content: center;
  padding-bottom: 8px;
}
.btn-apply {
  padding: 13px 40px; border-radius: var(--radius-full);
  background: var(--color-black); border: 2px solid var(--color-green);
  color: var(--color-green); font-size: 15px; font-weight: 600;
  display: flex; align-items: center; gap: 8px;
  cursor: pointer; transition: background .15s;
}
.btn-apply:hover { background: var(--color-green-dim); }
.btn-apply:disabled { opacity: .5; cursor: not-allowed; }

.btn-discard {
  padding: 13px 36px; border-radius: var(--radius-full);
  background: var(--color-white); border: none;
  color: var(--color-black); font-size: 15px; font-weight: 600;
  display: flex; align-items: center; gap: 8px;
  cursor: pointer; transition: opacity .15s;
}
.btn-discard:hover { opacity: .85; }

/* States */
.page-loading { padding: 40px; }
.page-error {
  display: flex; flex-direction: column; align-items: center;
  justify-content: center; gap: 16px; min-height: 50vh;
  font-size: 16px; color: var(--color-text-secondary);
}
</style>
