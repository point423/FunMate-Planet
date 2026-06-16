// src/utils/format.ts
import { format, formatDistanceToNow } from 'date-fns'

export const formatDate = (date: string | Date, pattern = 'dd/MM/yyyy') =>
  format(new Date(date), pattern)

export const timeAgo = (date: string | Date) =>
  formatDistanceToNow(new Date(date), { addSuffix: true })

export const formatDistance = (km?: number | null) => {
  if (km == null || isNaN(Number(km))) return '--'
  const n = Number(km)
  return n < 1 ? `${Math.round(n * 1000)}m` : `${n.toFixed(1)}km`
}

export const formatScore = (score: number, reviewCount?: number) => {
  if (reviewCount === 0) return 'No reviews yet'
  const value = Number.isFinite(Number(score)) ? Number(score) : 0
  return `${Math.round(value)}% positive`
}
