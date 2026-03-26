// src/api/auth.ts
import request from './index'
import type { LoginForm, RegisterForm } from '@/types/user'

export const login = (data: LoginForm) =>
  request.post<{ token: string; userInfo: unknown }>('/auth/login', data)

export const register = (data: RegisterForm) =>
  request.post('/auth/register', data)

export const getUserInfo = () =>
  request.get('/users/me')

export const reportLocation = (data: { longitude: number; latitude: number }) =>
  request.post('/users/location', data)
