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
const token = ref<string>(localStorage.getItem('token') || '')
const userInfo = ref<UserInfo | null>(null)

const isLoggedIn = computed(() => !!token.value)

const loginAction = async (form: LoginForm): Promise<LoginActionResult> => {
const data = await login(form) as unknown as { token: string; isNewUser: boolean }

token.value = data.token
localStorage.setItem('token', data.token)

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
localStorage.removeItem('token')
}

return { token, userInfo, isLoggedIn, loginAction, registerAction, fetchUserInfo, logout }
})