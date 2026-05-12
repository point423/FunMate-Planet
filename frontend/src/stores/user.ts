// src/stores/user.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, register, getUserInfo } from '@/api/auth'
import type { UserInfo, LoginForm, RegisterForm } from '@/types/user'

interface LoginActionResult {
token: string
needsTagSetup: boolean
}

export const useUserStore = defineStore('user', () => {
// Keep token in memory to reduce persistent exposure to XSS.
// Prefer HttpOnly cookies set by the backend for production.
const token = ref<string>('')
const userInfo = ref<UserInfo | null>(null)

const isLoggedIn = computed(() => !!token.value)

const loginAction = async (form: LoginForm): Promise<LoginActionResult> => {
const data = await login(form) as unknown as { token: string; isNewUser: boolean }

	token.value = data.token

if (data.isNewUser) {
userInfo.value = null
return { token: data.token, needsTagSetup: true }
}

await fetchUserInfo()
return { token: data.token, needsTagSetup: false }
}

const registerAction = async (form: RegisterForm) => {
await register(form)
}

const fetchUserInfo = async () => {
const data = await getUserInfo() as unknown as UserInfo
userInfo.value = data
return data
}

const logout = () => {
	token.value = ''
	userInfo.value = null
}

return { token, userInfo, isLoggedIn, loginAction, registerAction, fetchUserInfo, logout }
})