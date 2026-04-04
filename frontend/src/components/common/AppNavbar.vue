<template>
  <nav class="navbar">
    <!-- Logo -->
    <div class="nav-logo" @click="router.push('/')">
      <!-- <svg width="40" height="40" viewBox="0 0 44 44" fill="none">
        <path d="M22 6C22 6 14 10 14 22C14 34 22 38 22 38C22 38 30 34 30 22C30 10 22 6 22 6Z"
              stroke="currentColor" stroke-width="1.5" fill="none"/>
        <path d="M10 22C10 22 14 14 22 14C30 14 34 22 34 22"
              stroke="currentColor" stroke-width="1.5" fill="none"/>
        <circle cx="22" cy="14" r="2.5" fill="currentColor"/>
        <path d="M18 8Q22 4 26 8" stroke="currentColor" stroke-width="1.5" fill="none" stroke-linecap="round"/>
        <path d="M20 4L22 2L24 4" stroke="currentColor" stroke-width="1.5" fill="none" stroke-linecap="round"/>
      </svg> -->

      <svg
        width="40"
        height="40"
        viewBox="0 0 100 100"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        aria-label="logo"
      >
        <!-- 冲击线 -->
        <path
          d="M45 25L45 15"
          stroke="currentColor"
          stroke-width="4"
          stroke-linecap="round"
        />
        <path
          d="M55 25L55 15"
          stroke="currentColor"
          stroke-width="4"
          stroke-linecap="round"
        />
        <path
          d="M35 30L28 23"
          stroke="currentColor"
          stroke-width="4"
          stroke-linecap="round"
        />
        <path
          d="M65 30L72 23"
          stroke="currentColor"
          stroke-width="4"
          stroke-linecap="round"
        />

        <!-- 左手 -->
        <path
          d="M25 55C25 55 30 40 40 40C45 40 48 43 50 45C52 43 55 40 60 40C70 40 75 55 75 55C75 55 80 60 80 70C80 80 70 85 70 85"
          stroke="currentColor"
          stroke-width="4"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M25 55C20 60 20 65 22 70C24 75 30 80 30 80"
          stroke="currentColor"
          stroke-width="4"
          stroke-linecap="round"
        />

        <!-- 右手 -->
        <path
          d="M75 55C75 55 70 40 60 40C55 40 52 43 50 45"
          stroke="currentColor"
          stroke-width="4"
          stroke-linecap="round"
        />

        <!-- 手指细节 -->
        <path
          d="M40 45L40 55"
          stroke="currentColor"
          stroke-width="3"
          stroke-linecap="round"
        />
        <path
          d="M60 45L60 55"
          stroke="currentColor"
          stroke-width="3"
          stroke-linecap="round"
        />
        <path
          d="M50 48L50 58"
          stroke="currentColor"
          stroke-width="3"
          stroke-linecap="round"
        />
      </svg>

      <span>Fun Mate Planet</span>
    </div>

    <!-- Centre pill nav -->
    <div class="nav-pill">
      <button
        v-for="item in navItems"
        :key="item.name"
        class="nav-btn"
        :class="{
          'active-green': route.name === item.name,
          active: isChildActive(item),
        }"
        @click="router.push(item.path)"
      >
        {{ item.label }}
      </button>
    </div>

    <!-- Right: auth or user -->
    <div class="nav-right">
      <template v-if="!userStore.isLoggedIn">
        <span class="nav-login" @click="router.push('/login')">Log in</span>
        <div class="nav-divider" />
        <button class="nav-signup" @click="router.push('/register')">
          Sign up
        </button>
      </template>
      <!-- <template v-else>
        <div class="nav-user" @click="router.push('/profile')">
          <img
            :src="userStore.userInfo?.avatar || ''"
            class="nav-avatar"
            alt="avatar"
          />
          <span class="nav-username">{{ userStore.userInfo?.nickname }}</span>
        </div>
      </template> -->

      <template v-else>
        <div class="nav-user-area">
          <div class="nav-user" @click="router.push('/profile')">
            <img
              :src="userStore.userInfo?.avatar || ''"
              class="nav-avatar"
              alt="avatar"
            />
            <span class="nav-username">{{ userStore.userInfo?.nickname }}</span>
          </div>
          <button class="nav-logout" @click="handleLogout">Log out</button>
        </div>
      </template>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { useRouter, useRoute } from "vue-router";
import { useUserStore } from "@/stores/user";

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const handleLogout = () => {
  userStore.logout();
  router.push("/login");
};

const navItems = [
  { name: "Home", label: "Home", path: "/" },
  { name: "Chat", label: "Chat", path: "/chat" },
  { name: "Activity", label: "Activity", path: "/activity" },
];

const isChildActive = (item: { name: string; path: string }) =>
  route.path.startsWith(item.path) && item.path !== "/";
</script>

<style scoped>
/* .navbar {
  position: fixed; top: 0; left: 0; right: 0;
  z-index: 100;
  height: var(--nav-height);
  background: rgba(0,0,0,0.70);
  backdrop-filter: blur(20px);
  border-bottom: 0.5px solid var(--color-border-dim);
  display: flex; align-items: center;
  padding: 0 40px;
} */

.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  height: var(--nav-height);
  background: #fff; /* 纯白 */
  backdrop-filter: none; /* 去掉磨砂叠色 */
  -webkit-backdrop-filter: none;
  border-bottom: 1px solid #e5e7eb; /* 浅灰分割线 */
  display: flex;
  align-items: center;
  padding: 0 40px;
  color: #111;
}

/* .nav-logo {
  display: flex; align-items: center; gap: 12px;
  font-family: var(--font-display); font-size: 22px;
  cursor: pointer; margin-right: 40px; flex-shrink: 0;
  color: var(--color-white);
  user-select: none;
} */

.nav-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  font-family: var(--font-display);
  font-size: 22px;
  cursor: pointer;
  margin-right: 40px;
  flex-shrink: 0;
  color: #111;
  user-select: none;
}

/* .nav-pill {
  display: flex; align-items: center; gap: 4px;
  background: rgba(255,255,255,0.10);
  border: 0.5px solid var(--color-border);
  border-radius: var(--radius-full);
  padding: 6px 8px;
  margin: 0 auto;
} */

.nav-pill {
  display: flex;
  align-items: center;
  gap: 4px;
  background: rgba(0, 0, 0, 0.06);
  border: 0.5px solid rgba(0, 0, 0, 0.12);
  border-radius: var(--radius-full);
  padding: 6px 8px;
  margin: 0 auto;
}

/* .nav-btn {
  padding: 8px 22px; border-radius: var(--radius-full);
  font-size: 15px; font-family: var(--font-body);
  color: var(--color-text-secondary);
  background: transparent; border: none; cursor: pointer;
  transition: all 0.18s;
} */

/* .nav-btn {
  padding: 8px 22px; border-radius: var(--radius-full);
  font-size: 15px; font-family: var(--font-body);
  color: rgba(0,0,0,0.62);
  background: transparent; border: none; cursor: pointer;
  transition: all 0.18s;
} */

.nav-btn {
  padding: 8px 22px;
  border-radius: var(--radius-full);
  font-size: 15px;
  font-family: var(--font-body);
  color: rgba(0, 0, 0, 0.62);
  background: transparent;
  border: none;
  cursor: pointer;
  transition: all 0.18s;
}

/* .nav-btn:hover        { color: var(--color-white); }
.nav-btn.active       { background: var(--color-black); color: var(--color-white); }
.nav-btn.active-green { background: var(--color-green); color: var(--color-black); font-weight: 700; } */

.nav-btn:hover {
  color: #111;
}
/* .nav-btn.active {
  background: #111;
  color: #fff;
}
.nav-btn.active-green {
  background: var(--color-green);
  color: #111;
  font-weight: 700;
} */

.nav-btn.active,
.nav-btn.active-green {
  background: #111;
  color: var(--color-green);
  font-weight: 700;
  border: 1px solid rgba(149, 255, 141, 0.35);
}

.nav-right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 20px;
}
/* .nav-login  { font-size: 14px; cursor: pointer; }
.nav-divider { width: 1px; height: 20px; background: var(--color-border); } */

.nav-login {
  font-size: 14px;
  cursor: pointer;
  color: #111;
}
.nav-divider {
  width: 1px;
  height: 20px;
  background: rgba(0, 0, 0, 0.18);
}

.nav-signup {
  padding: 9px 22px;
  border-radius: var(--radius-full);
  background: var(--color-green);
  color: var(--color-black);
  font-size: 14px;
  font-weight: 700;
  border: none;
  cursor: pointer;
  transition: opacity 0.15s;
}
.nav-signup:hover {
  opacity: 0.88;
}

/* .nav-user {
  display: flex; align-items: center; gap: 10px; cursor: pointer;
} */

.nav-user {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  color: #111;
}

/* .nav-avatar {
  width: 38px; height: 38px; border-radius: 50%;
  border: 2px solid var(--color-border);
  object-fit: cover; background: #333;
} */

.nav-avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  border: 2px solid rgba(0, 0, 0, 0.12);
  object-fit: cover;
  background: #e5e5e5;
}

.nav-username {
  font-size: 15px;
  font-weight: 500;
}

.nav-user-area {
  display: flex;
  align-items: center;
  gap: 10px;
}

.nav-logout {
  padding: 7px 14px;
  border-radius: 9999px;
  border: 1px solid rgba(0, 0, 0, 0.2);
  background: #fff;
  color: #111;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.15s;
}

.nav-logout:hover {
  background: #111;
  color: var(--color-green);
  border-color: #111;
}
</style>
