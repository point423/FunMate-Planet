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
        @click="openOrbitUser(user)"
      >
        <img :src="user.avatar" :alt="user.nickname" />
        <div class="orbit-tooltip">
          <div class="ot-name">{{ user.nickname }}</div>
          <div class="ot-tags">{{ user.tags.slice(0, 2).join(" · ") }}</div>
        </div>
      </div>
    </div>

    <!-- Buttons -->
    <div class="home-btns">
      <button class="btn-green" @click="openAutoMatch">Auto Match</button>
      <button class="btn-outline" @click="router.push('/activity/partner')">
        Scan List
      </button>
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
        <img
          :src="matchedUser.avatar"
          class="match-avatar"
          :alt="matchedUser.nickname"
        />
        <h3 class="match-name">{{ matchedUser.nickname }}</h3>
        <p class="match-bio">{{ matchedUser.bio }}</p>
        <div class="match-tags">
          <span
            v-for="tag in matchedUser.tags"
            :key="tag"
            class="tag-chip active"
            >{{ tag }}</span
          >
        </div>
        <p class="match-meta">
          📍 {{ formatDistance(matchedUser.distance) }} &nbsp;·&nbsp;
          {{ formatScore(matchedUser.score) }} &nbsp;·&nbsp;
          {{ matchedUser.activities }} activities
        </p>
        <div class="match-btns">
          <button
            class="btn-green"
            style="flex: 1"
            @click="applyFriend(matchedUser)"
          >
            Apply 👥+
          </button>
          <button class="btn-outline" style="flex: 1" @click="nextMatch">
            Next →
          </button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { getNearbyUsers, getRandomUser } from "@/api/dazi";
import { sendFriendRequest } from "@/api/user";
import { useLocation } from "@/composables/useLocation";
import { formatDistance, formatScore } from "@/utils/format";
import type { NearbyUser } from "@/types/user";
import { useUserStore } from "@/stores/user";

const router = useRouter();
const { updateLocation } = useLocation();
const userStore = useUserStore();
const currentUserId = computed(() => userStore.userInfo?.id ?? null);

const DEFAULT_LOCATION = {
  longitude: 120.1551,
  latitude: 30.2741,
};

// ── Orbit users ────────────────────────────────────────────────
const orbitUsers = ref<NearbyUser[]>([]);

const orbitPositions = [
  { top: "18%", left: "43%" },
  { top: "22%", left: "63%" },
  { top: "47%", left: "72%" },
  { top: "66%", left: "57%" },
  { top: "66%", left: "38%" },
  { top: "42%", left: "24%" },
];

const getCurrentPosition = async () => {
  if (!navigator.geolocation) return DEFAULT_LOCATION;
  try {
    const pos = await new Promise<GeolocationPosition>((resolve, reject) => {
      navigator.geolocation.getCurrentPosition(resolve, reject);
    });
    return {
      longitude: pos.coords.longitude,
      latitude: pos.coords.latitude,
    };
  } catch {
    return DEFAULT_LOCATION;
  }
};

const normalizeTags = (raw: unknown): string[] => {
  if (Array.isArray(raw)) return raw.filter(Boolean).map(String);
  if (typeof raw === "string") {
    return raw
      .split(",")
      .map((s) => s.trim())
      .filter(Boolean);
  }
  return [];
};

const normalizeNearbyUser = (raw: Record<string, unknown>): NearbyUser => ({
  ...raw,
  bio: raw?.bio ?? "",
  avatar: raw?.avatar || "/default-avatar.png",
  tags: normalizeTags(raw?.tags),
  activities: Number(raw?.activities ?? 0),
  score: Number(raw?.score ?? raw?.averageScore ?? 0),
  distance: Number(raw?.distance ?? 0),
  ranking: Number(raw?.ranking ?? 0),
  publicJournals: Array.isArray(raw?.publicJournals) ? raw.publicJournals : [],
  recentActivities: Array.isArray(raw?.recentActivities)
    ? raw.recentActivities
    : [],
});

const loadOrbitUsers = async () => {
  try {
    const position = await getCurrentPosition();
    const data = (await getNearbyUsers({
      longitude: position.longitude,
      latitude: position.latitude,
      radius: 10,
      pageSize: 6,
    })) as NearbyUser[];

    orbitUsers.value = data
      .map(normalizeNearbyUser)
      .filter((user) => user.id !== currentUserId.value)
      .slice(0, 6);
  } catch {
    orbitUsers.value = [];
  }
};

// ── Auto match ─────────────────────────────────────────────────
const matchVisible = ref(false);
const matchedUser = ref<NearbyUser | null>(null);
let matchPool: NearbyUser[] = [];
let matchIdx = 0;

const openMatchCard = (pool: NearbyUser[], index = 0) => {
  if (pool.length === 0) return;
  matchPool = pool;
  matchIdx = Math.max(0, Math.min(index, pool.length - 1));
  matchedUser.value = matchPool[matchIdx];
  matchVisible.value = true;
};

const openAutoMatch = async () => {
  if (matchPool.length === 0) {
    try {
      const position = await getCurrentPosition();
      const nearby = (await getNearbyUsers({
        longitude: position.longitude,
        latitude: position.latitude,
        radius: 10,
        pageSize: 20,
      })) as NearbyUser[];

      matchPool = nearby
        .map(normalizeNearbyUser)
        .filter((u) => u.id !== currentUserId.value);

      // 附近没人时，尝试拿随机用户
      if (matchPool.length === 0) {
        const random = await getRandomUser();
        if (random) {
          const candidate = normalizeNearbyUser(random);
          if (candidate.id !== currentUserId.value) {
            matchPool = [candidate];
          }
        }
      }

      if (matchPool.length === 0) {
        ElMessage.warning("暂无可匹配用户");
        return;
      }

      openMatchCard(matchPool, 0);
      return;
    } catch {
      ElMessage.error("加载匹配用户失败");
      return;
    }
  }

  if (matchPool.length === 0) {
    ElMessage.warning("暂无可匹配用户");
    return;
  }

  openMatchCard(matchPool, matchIdx % matchPool.length);
};

const nextMatch = () => {
  if (matchPool.length === 0) return;
  matchIdx++;
  matchedUser.value = matchPool[matchIdx % matchPool.length];
};

const applyFriend = async (user: NearbyUser) => {
  try {
    await sendFriendRequest(user.id);
    ElMessage.success(`Friend request sent to ${user.nickname}!`);
    matchVisible.value = false;
  } catch {
    // handled by interceptor
  }
};

const openOrbitUser = (user: NearbyUser) => {
  const index = orbitUsers.value.findIndex((item) => item.id === user.id);
  openMatchCard(orbitUsers.value, index >= 0 ? index : 0);
};

// ── Lifecycle ──────────────────────────────────────────────────
onMounted(() => {
  updateLocation();
  loadOrbitUsers();
});
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
  border: 0.5px solid rgba(255, 255, 255, 0.12);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  pointer-events: none;
}
.ring-1 {
  width: 400px;
  height: 200px;
  transform: translate(-50%, -50%) rotateX(75deg);
}
.ring-2 {
  width: 520px;
  height: 260px;
  transform: translate(-50%, -50%) rotateX(75deg);
}
.ring-3 {
  width: 640px;
  height: 320px;
  transform: translate(-50%, -50%) rotateX(75deg);
}

.globe {
  width: 280px;
  height: 280px;
  border-radius: 50%;
  background: radial-gradient(
      circle at 32% 28%,
      rgba(120, 180, 255, 0.28) 0%,
      transparent 40%
    ),
    linear-gradient(145deg, #2a5ba8 0%, #1a3e8a 35%, #0e2266 65%, #06144a 100%);
  box-shadow: 0 0 0 1px rgba(100, 160, 255, 0.18),
    0 0 50px rgba(60, 100, 200, 0.35), 0 0 100px rgba(40, 80, 180, 0.18),
    inset 0 0 60px rgba(0, 0, 60, 0.5);
  position: relative;
  z-index: 3;
  overflow: hidden;
  animation: globeGlow 4s ease-in-out infinite;
}
@keyframes globeGlow {
  0%,
  100% {
    box-shadow: 0 0 50px rgba(60, 100, 200, 0.35),
      inset 0 0 60px rgba(0, 0, 60, 0.5);
  }
  50% {
    box-shadow: 0 0 80px rgba(60, 100, 220, 0.55),
      inset 0 0 60px rgba(0, 0, 60, 0.5);
  }
}
.globe::before {
  content: "";
  position: absolute;
  width: 42%;
  height: 35%;
  top: 12%;
  left: 15%;
  background: radial-gradient(
    ellipse,
    rgba(200, 230, 255, 0.2) 0%,
    transparent 70%
  );
  border-radius: 50%;
}
.globe-land {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: radial-gradient(
      ellipse 22% 14% at 38% 46%,
      rgba(60, 160, 100, 0.5) 0%,
      transparent 100%
    ),
    radial-gradient(
      ellipse 28% 10% at 60% 65%,
      rgba(50, 130, 80, 0.42) 0%,
      transparent 100%
    ),
    radial-gradient(
      ellipse 16% 10% at 22% 58%,
      rgba(55, 140, 85, 0.38) 0%,
      transparent 100%
    );
  animation: landDrift 20s linear infinite;
}
@keyframes landDrift {
  from {
    transform: translateX(0);
  }
  to {
    transform: translateX(-100%);
  }
}

.globe-clouds {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: radial-gradient(
      ellipse 30% 6% at 40% 28%,
      rgba(255, 255, 255, 0.14) 0%,
      transparent 100%
    ),
    radial-gradient(
      ellipse 22% 5% at 65% 48%,
      rgba(255, 255, 255, 0.1) 0%,
      transparent 100%
    );
  animation: cloudDrift 45s linear infinite;
}
@keyframes cloudDrift {
  from {
    transform: translateX(0);
  }
  to {
    transform: translateX(-50px);
  }
}

/* ── Orbit user avatars ── */
.orbit-user {
  position: absolute;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  overflow: visible;
  cursor: pointer;
  z-index: 5;
}
.orbit-user img {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: 2.5px solid rgba(255, 255, 255, 0.45);
  object-fit: cover;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.5);
  transition: transform 0.2s, border-color 0.2s;
}
.orbit-user:hover img {
  transform: scale(1.2);
  border-color: var(--color-green);
}
.orbit-user:hover .orbit-tooltip {
  opacity: 1;
  transform: translateX(-50%) translateY(0);
}

.orbit-tooltip {
  position: absolute;
  top: calc(100% + 8px);
  left: 50%;
  transform: translateX(-50%) translateY(6px);
  background: rgba(18, 16, 42, 0.9);
  border: 0.5px solid var(--color-border);
  border-radius: 10px;
  padding: 6px 10px;
  white-space: nowrap;
  opacity: 0;
  transition: opacity 0.2s, transform 0.2s;
  pointer-events: none;
  backdrop-filter: blur(10px);
  z-index: 200;
}
.ot-name {
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 2px;
}
.ot-tags {
  font-size: 11px;
  color: var(--color-text-secondary);
}

/* ── Buttons ── */
.home-btns {
  display: flex;
  gap: 16px;
  margin-top: 8px;
  position: relative;
  z-index: 10;
}

/* ── Match dialog ── */
:deep(.match-dialog .el-dialog) {
  background: #0a0a0a !important;
  border: 1px solid #2a2a2a !important;
  border-radius: var(--radius-xl);
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.65);
}
:deep(.match-dialog .el-dialog__header) {
  display: none;
}
:deep(.match-dialog .el-dialog__body) {
  padding: 0;
  background-color: #0a0a0a !important;
}

.match-card {
  padding: 32px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
  position: relative;
  text-align: center;
  color: #f5f5f5;
  background-color: #0a0a0a !important;  /* ← 加这一行 */
}
.match-close {
  position: absolute;
  top: 12px;
  right: 16px;
  background: none;
  border: none;
  color: var(--color-text-secondary);
  font-size: 22px;
  cursor: pointer;
  line-height: 1;
}
.match-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  border: 2.5px solid var(--color-green-border);
  box-shadow: 0 0 28px var(--color-green-dim);
}
.match-name {
  font-family: var(--font-display);
  font-size: 22px;
}
.match-bio {
  font-size: 13px;
  color: var(--color-text-secondary);
  line-height: 1.6;
}
.match-tags {
  display: flex;
  gap: 7px;
  flex-wrap: wrap;
  justify-content: center;
}
.match-meta {
  font-size: 12px;
  color: var(--color-text-secondary);
}
.match-btns {
  display: flex;
  gap: 10px;
  width: 100%;
}
</style>

<style>
/* 强制覆盖 el-dialog__body 所有白色相关样式 */
.match-dialog .el-dialog__body {
  background-color: #0a0a0a !important;
  padding: 0 !important;
  margin: 0 !important;
  border: none !important;
  box-shadow: none !important;
}

.match-dialog .el-dialog {
  background-color: #0a0a0a !important;
  border: 1px solid #2a2a2a !important;
  overflow: hidden !important;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.65) !important;
}

.match-dialog .el-dialog__header {
  display: none !important;
}

.match-dialog .el-overlay-dialog {
  background-color: transparent !important;
}

/* 最终兜底：直接强制所有可能有白色的容器 */
div[class*="el-dialog__body"] {
  background-color: #0a0a0a !important;
  padding: 0 !important;
}

div[class*="el-dialog"][class*="is-align-center"] {
  background-color: #0a0a0a !important;
}
</style>