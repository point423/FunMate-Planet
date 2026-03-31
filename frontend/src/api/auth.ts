// src/api/auth.ts
import request from './index'
import type { LoginForm, RegisterForm, UserInfo } from '@/types/user'

export interface LoginResponse {
token: string
isNewUser: boolean
}

export const login = (data: LoginForm) =>
request.post<LoginResponse>('/auth/login', data)

export const register = (data: RegisterForm) =>
request.post('/auth/register', data)

export const getUserInfo = () =>
request.get<UserInfo>('/users/me')

export const reportLocation = (data: { longitude: number; latitude: number }) =>
request.post('/users/location', data)