// src/composables/useAuth.ts
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import type { LoginForm, RegisterForm } from '@/types/user'

export function useAuth() {
const userStore = useUserStore()
const router = useRouter()

const login = async (form: LoginForm) => {
const result = await userStore.loginAction(form)

if (result.needsTagSetup) {
router.push({ name: 'TagSetup' })
return result
}

const redirect = router.currentRoute.value.query.redirect as string | undefined
router.push(redirect || '/')
return result
}

const register = async (form: RegisterForm) => {
await userStore.registerAction(form)
router.push({ name: 'Login' })
}

const logout = () => {
userStore.logout()
router.push({ name: 'Login' })
}

return { login, register, logout, userStore }
}