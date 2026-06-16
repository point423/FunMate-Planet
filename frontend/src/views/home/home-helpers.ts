import type { NearbyUser } from '@/types/user'

export const DEFAULT_LOCATION = {
  longitude: 120.1551,
  latitude: 30.2741,
}

export const normalizeTags = (raw: unknown): string[] => {
  if (Array.isArray(raw)) return raw.filter(Boolean).map(String)
  if (typeof raw === 'string') {
    return raw.split(',').map((value) => value.trim()).filter(Boolean)
  }
  return []
}

export const normalizeNearbyUser = (raw: any): NearbyUser => ({
  ...raw,
  bio: raw?.bio ?? '',
  avatar: raw?.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${raw?.id}`,
  tags: normalizeTags(raw?.tags),
  activities: Number(raw?.activities ?? 0),
  score: typeof raw?.averageScore === 'object' ? Number(raw.averageScore.parsedValue) : Number(raw?.averageScore ?? 0),
  reviewCount: raw?.reviewCount == null ? undefined : Number(raw.reviewCount),
  distance: Number(raw?.distance ?? 0),
})

export const buildOrbitUsers = (raw: unknown, currentUserId: number | null) => {
  const list = Array.isArray(raw) ? raw : ((raw as { content?: unknown[] } | null)?.content || [])
  return list
    .map(normalizeNearbyUser)
    .filter((user) => user.id !== currentUserId)
    .slice(0, 6)
}

export const getCurrentPosition = async (
  geolocation: Pick<Geolocation, 'getCurrentPosition'> | undefined,
) => {
  if (!geolocation) return DEFAULT_LOCATION
  try {
    const pos = await new Promise<GeolocationPosition>((resolve, reject) => {
      geolocation.getCurrentPosition(resolve, reject, { timeout: 5000 })
    })
    return {
      longitude: pos.coords.longitude,
      latitude: pos.coords.latitude,
    }
  } catch {
    return DEFAULT_LOCATION
  }
}
