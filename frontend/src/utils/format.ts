// src/utils/format.ts
import { format, formatDistanceToNow } from 'date-fns'

export const formatDate = (date: string | Date, pattern = 'dd/MM/yyyy') =>
  format(new Date(date), pattern)

export const timeAgo = (date: string | Date) =>
  formatDistanceToNow(new Date(date), { addSuffix: true })

export const formatDistance = (km: number) =>
  km < 1 ? `${Math.round(km * 1000)}m` : `${km.toFixed(1)}km`

export const formatScore = (score: number) =>
  `⭐ ${score.toFixed(2)}`
