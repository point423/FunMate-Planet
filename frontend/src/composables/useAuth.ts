// src/composables/useAuth.ts
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import type { LoginForm, RegisterForm } from '@/types/user'

/** Wraps login / logout with router navigation */
export function useAuth() {
  const userStore = useUserStore()
  const router = useRouter()

  const login = async (form: LoginForm) => {
    await userStore.loginAction(form)
    const redirect = router.currentRoute.value.query.redirect as string
    router.push(redirect || '/')
  }

  const register = async (form: RegisterForm) => {
    await userStore.registerAction(form)
    router.push({ name: 'TagSetup' })
  }

  const logout = () => {
    userStore.logout()
    router.push({ name: 'Login' })
  }

  return { login, register, logout, userStore }
}
