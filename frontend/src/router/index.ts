// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    // ── Auth ──────────────────────────────────
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/auth/LoginView.vue'),
      meta: { requiresAuth: false, layout: 'blank' },
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/auth/RegisterView.vue'),
      meta: { requiresAuth: false, layout: 'blank' },
    },
    {
      path: '/tags',
      name: 'TagSetup',
      component: () => import('@/views/auth/TagSetupView.vue'),
      meta: { requiresAuth: true, layout: 'blank' },
    },

    // ── Main layout ───────────────────────────
    {
      path: '/',
      name: 'Home',
      component: () => import('@/views/home/HomeView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/chat',
      name: 'Chat',
      component: () => import('@/views/chat/ChatView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/activity',
      name: 'Activity',
      component: () => import('@/views/activity/ActivityView.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          redirect: { name: 'Journal' },
        },
        {
          path: 'journal',
          name: 'Journal',
          component: () => import('@/views/activity/JournalView.vue'),
        },
        {
          path: 'journal/:id',
          name: 'JournalDetail',
          component: () => import('@/views/activity/JournalDetailView.vue'),
        },
        {
          path: 'partner',
          name: 'FindPartner',
          component: () => import('@/views/activity/FindPartnerView.vue'),
        },
      ],
    },

    // ── Profile ───────────────────────────────
    {
      path: '/profile',
      name: 'MyProfile',
      component: () => import('@/views/profile/MyProfileView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/user/:id',
      name: 'UserDetail',
      component: () => import('@/views/user/UserDetailView.vue'),
      meta: { requiresAuth: true },
    },

    // ── Fallback ──────────────────────────────
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
})

router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if (!to.meta.requiresAuth && userStore.isLoggedIn && (to.name === 'Login' || to.name === 'Register')) {
    next({ name: 'Home' })
  } else {
    next()
  }
})

export default router
