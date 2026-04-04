// src/utils/validators.ts

export const required = (msg = 'This field is required') =>
  (v: string) => !!v || msg

export const minLen = (n: number) =>
  (v: string) => v.length >= n || `At least ${n} characters`

export const maxLen = (n: number) =>
  (v: string) => v.length <= n || `Max ${n} characters`

export const passwordMatch = (other: string) =>
  (v: string) => v === other || 'Passwords do not match'

export const isEmail = (v: string) =>
  /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v) || 'Invalid email address'
