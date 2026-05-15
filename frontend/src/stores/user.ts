// src/stores/user.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, register, getUserInfo } from '@/api/auth'
import type { UserInfo, LoginForm, RegisterForm } from '@/types/user'

interface LoginActionResult {
token: string
needsTagSetup: boolean
}

const normalizeTags = (tags: unknown): string[] => {
if (Array.isArray(tags)) {
return tags.map(tag => String(tag).trim()).filter(Boolean)
}

if (typeof tags === 'string') {
const value = tags.trim()
if (!value) return []

if (value.startsWith('[') && value.endsWith(']')) {
try {
const parsed = JSON.parse(value)
if (Array.isArray(parsed)) {
return parsed.map(tag => String(tag).trim()).filter(Boolean)
}
} catch {
// Ignore invalid JSON and fallback to comma split.
}
}

return value.split(/[，,]/).map(tag => tag.trim()).filter(Boolean)
}

return []
}

const normalizeUserInfo = (raw: unknown): UserInfo => {
const data = raw as UserInfo & { tags?: unknown }
return {
...data,
tags: normalizeTags(data.tags),
}
}

export const useUserStore = defineStore('user', () => {
// Keep token in memory and mirror it to localStorage so router guards
// and API interceptors share the same login state during local development.
const token = ref<string>(localStorage.getItem('token') ?? '')
const userInfo = ref<UserInfo | null>(null)

const isLoggedIn = computed(() => !!token.value)

const persistToken = (value: string) => {
	token.value = value
	if (value) {
		localStorage.setItem('token', value)
	} else {
		localStorage.removeItem('token')
	}
}

const loginAction = async (form: LoginForm): Promise<LoginActionResult> => {
const data = await login(form) as unknown as { token: string; isNewUser: boolean }

	persistToken(data.token)

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
const data = normalizeUserInfo(await getUserInfo())
userInfo.value = data
return data
}

const logout = () => {
	persistToken('')
	userInfo.value = null
}

return { token, userInfo, isLoggedIn, loginAction, registerAction, fetchUserInfo, logout }
})