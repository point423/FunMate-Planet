// src/stores/user.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, register, getUserInfo } from '@/api/auth'
import type { UserInfo, LoginForm, RegisterForm } from '@/types/user'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)

  const isLoggedIn = computed(() => !!token.value)

  const loginAction = async (form: LoginForm) => {
    const data = await login(form) as { token: string; userInfo: UserInfo }
    token.value = data.token
    userInfo.value = data.userInfo
    localStorage.setItem('token', data.token)
    return data
  }

  const registerAction = async (form: RegisterForm) => {
    await register(form)
  }

  const fetchUserInfo = async () => {
    const data = await getUserInfo() as UserInfo
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
