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
          <div class="ot-tags">{{ (user.tags || []).slice(0, 2).join(" · ") }}</div>
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
        <p class="match-bio">{{ matchedUser.bio || 'This wanderer is a bit mysterious...' }}</p>
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
          ⭐ {{ formatScore(matchedUser.score) }}
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
    // 修复：给定位增加 5 秒超时，防止浏览器无响应导致匹配按钮没反应
    const pos = await new Promise<GeolocationPosition>((resolve, reject) => {
      navigator.geolocation.getCurrentPosition(resolve, reject, { timeout: 5000 });
    });
    return {
      longitude: pos.coords.longitude,
      latitude: pos.coords.latitude,
    };
  } catch (e) {
    console.warn(">>> [AutoMatch] 定位获取失败或超时，使用默认位置");
    return DEFAULT_LOCATION;
  }
};

const normalizeTags = (raw: unknown): string[] => {
  if (Array.isArray(raw)) return raw.filter(Boolean).map(String);
  if (typeof raw === "string") {
    return raw.split(",").map((s) => s.trim()).filter(Boolean);
  }
  return [];
};

const normalizeNearbyUser = (raw: any): NearbyUser => ({
  ...raw,
  bio: raw?.bio ?? "",
  avatar: raw?.avatar || "https://api.dicebear.com/7.x/avataaars/svg?seed=" + raw?.id,
  tags: normalizeTags(raw?.tags),
  activities: Number(raw?.activities ?? 0),
  // 修复：兼容后端分数变成对象的情况
  score: typeof raw?.averageScore === 'object' ? Number(raw.averageScore.parsedValue) : Number(raw?.averageScore ?? 0),
  distance: Number(raw?.distance ?? 0),
});

const loadOrbitUsers = async () => {
  try {
    const position = await getCurrentPosition();
    const data = (await getNearbyUsers({
      longitude: position.longitude,
      latitude: position.latitude,
      radius: 10,
      pageSize: 6,
    })) as any;

    const list = Array.isArray(data) ? data : (data.content || []);
    orbitUsers.value = list
      .map(normalizeNearbyUser)
      .filter((user) => user.id !== currentUserId.value)
      .slice(0, 6);
  } catch (e) {
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
  console.log(">>> [AutoMatch] 正在寻找搭子...");
  if (matchPool.length === 0) {
    try {
      const position = await getCurrentPosition();
      const nearbyRes = await getNearbyUsers({
        longitude: position.longitude,
        latitude: position.latitude,
        radius: 100, // 扩大范围确保能搜到人
        pageSize: 20,
      }) as any;

      const nearbyList = Array.isArray(nearbyRes) ? nearbyRes : (nearbyRes.content || []);

      matchPool = nearbyList
        .map(normalizeNearbyUser)
        .filter((u) => u.id !== currentUserId.value);

      if (matchPool.length === 0) {
        console.log(">>> [AutoMatch] 附近没人，尝试随机用户兜底...");
        const random = await getRandomUser() as any;
        if (random && random.id !== currentUserId.value) {
          matchPool = [normalizeNearbyUser(random)];
        }
      }

      if (matchPool.length === 0) {
        ElMessage.warning("暂无可匹配用户");
        return;
      }

      openMatchCard(matchPool, 0);
      return;
    } catch (err) {
      console.error(">>> [AutoMatch] 匹配过程出错:", err);
      ElMessage.error("加载匹配用户失败");
      return;
    }
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
  } catch (e) {}
};

const openOrbitUser = (user: NearbyUser) => {
  matchedUser.value = user;
  matchVisible.value = true;
};

onMounted(() => {
  updateLocation();
  loadOrbitUsers();
});
</script>

<style scoped>
.home-page { position: relative; z-index: 1; min-height: calc(100vh - var(--nav-height)); display: flex; flex-direction: column; align-items: center; justify-content: center; padding-bottom: 40px; }
.globe-scene { position: relative; width: 700px; height: 560px; display: flex; align-items: center; justify-content: center; }
.orbit-ring { position: absolute; border-radius: 50%; border: 0.5px solid rgba(255, 255, 255, 0.12); top: 50%; left: 50%; transform: translate(-50%, -50%); pointer-events: none; }
.ring-1 { width: 400px; height: 200px; transform: translate(-50%, -50%) rotateX(75deg); }
.ring-2 { width: 520px; height: 260px; transform: translate(-50%, -50%) rotateX(75deg); }
.ring-3 { width: 640px; height: 320px; transform: translate(-50%, -50%) rotateX(75deg); }
.globe { width: 280px; height: 280px; border-radius: 50%; background: linear-gradient(145deg, #2a5ba8 0%, #06144a 100%); box-shadow: 0 0 50px rgba(60, 100, 200, 0.35); position: relative; z-index: 3; overflow: hidden; }
.orbit-user { position: absolute; width: 44px; height: 44px; border-radius: 50%; cursor: pointer; z-index: 5; }
.orbit-user img { width: 44px; height: 44px; border-radius: 50%; border: 2.5px solid rgba(255, 255, 255, 0.45); object-fit: cover; }
.home-btns { display: flex; gap: 16px; margin-top: 8px; z-index: 10; }
.match-card { padding: 32px; display: flex; flex-direction: column; align-items: center; gap: 14px; position: relative; text-align: center; color: #f5f5f5; background-color: #0a0a0a !important; }
.match-avatar { width: 80px; height: 80px; border-radius: 50%; border: 2.5px solid var(--color-green-border); }
.match-name { font-family: var(--font-display); font-size: 22px; }
.match-tags { display: flex; gap: 7px; flex-wrap: wrap; justify-content: center; }
.match-btns { display: flex; gap: 10px; width: 100%; }
.match-close { position: absolute; top: 12px; right: 16px; background: none; border: none; color: var(--color-text-secondary); font-size: 22px; cursor: pointer; }
</style>
