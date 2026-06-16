import type { SharedDiaryEntryPayload } from '@/types/diary'

const DETAIL_FALLBACK_COVER = 'https://images.unsplash.com/photo-1517486808906-6ca8b3f04846?q=80&w=800'
const GRID_FALLBACK_COVER = 'https://images.unsplash.com/photo-1517486808906-6ca8b3f04846?q=80&w=400'

export const parseImages = (images: unknown): string[] => {
  if (!images) return []
  if (Array.isArray(images)) return images.filter(Boolean).map(String)
  if (typeof images === 'string') {
    try {
      const parsed = JSON.parse(images)
      return Array.isArray(parsed) ? parsed.filter(Boolean).map(String) : [images]
    } catch {
      return images.split(',').filter(Boolean)
    }
  }
  return []
}

export const normalizeDiary = (raw: any) => {
  if (!raw) return null
  const images = parseImages(raw.images)
  return {
    ...raw,
    title: raw.title || (raw.content ? `${raw.content.slice(0, 20)}${raw.content.length > 20 ? '...' : ''}` : 'Untitled Journal'),
    images,
    coverImage: images[0] || '',
    participants: raw.participants || [],
    sharedEntries: raw.sharedEntries || [],
  }
}

export const normalizeDiaryDetailPayload = (raw: any) => {
  if (!raw) return null
  const diary = raw.diary || raw
  const participants = raw.participants || diary.participants || []
  const sharedEntries = raw.sharedEntries || diary.sharedEntries || []

  return normalizeDiary({
    ...diary,
    participants,
    sharedEntries,
  })
}

export const mergeDiaryList = (
  diariesList: unknown,
  diaryDetailCache: Record<number, any>,
  injectedDiary: any | null,
) => {
  const normalized = (!diariesList || !Array.isArray(diariesList) ? [] : diariesList)
    .map((item: any) => {
      const diary = item.diary || item
      const cachedDiary = diaryDetailCache[Number(diary.id)]
      const participants = item.participants || diary.participants || []
      return cachedDiary || normalizeDiary({
        ...diary,
        participants,
        sharedEntries: diary.sharedEntries || [],
      })
    })
    .filter(Boolean)

  if (!injectedDiary) {
    return normalized
  }

  if (!normalized.some((item: any) => item.id === injectedDiary.id)) {
    return [injectedDiary, ...normalized]
  }

  return normalized.map((item: any) => (item.id === injectedDiary.id ? injectedDiary : item))
}

export const detailCoverImageFromDiary = (diary: any) => {
  const images = parseImages(diary?.images)
  return images[0] || DETAIL_FALLBACK_COVER
}

export const coverFromImages = (images: unknown) =>
  parseImages(images)[0] || GRID_FALLBACK_COVER

export const findMySharedEntry = (diary: any, currentUserId: number) => {
  if (!currentUserId) return null

  const entries = Array.isArray(diary?.sharedEntries) ? diary.sharedEntries : []
  return entries.find((item: SharedDiaryEntryPayload) => Number(item.user?.id ?? item.entry?.userId ?? 0) === currentUserId) ?? null
}

export const getMyUploadedCoverFromDiary = (
  diary: any,
  currentUserId: number,
  entryDrafts: Record<number, { content: string; images: string[] | string }>,
  preferDraft = false,
) => {
  const myEntry = findMySharedEntry(diary, currentUserId)
  if (!myEntry) return ''

  if (preferDraft) {
    const draftImages = parseImages(entryDrafts[myEntry.user.id]?.images)
    if (draftImages.length > 0) return draftImages[0]
  }

  const uploadedImages = parseImages(myEntry.entry?.images)
  return uploadedImages[0] || ''
}

export const resolveDiaryCover = (
  diary: any,
  currentUserId: number,
  entryDrafts: Record<number, { content: string; images: string[] | string }>,
) => getMyUploadedCoverFromDiary(diary, currentUserId, entryDrafts) || diary.coverImage || coverFromImages(diary.images)

export const fallbackAvatar = (seed: number) => `https://api.dicebear.com/7.x/avataaars/svg?seed=${seed}`

export const isDiaryOwner = (activeDiaryUserId: unknown, currentUserId: unknown) =>
  Number(activeDiaryUserId) === Number(currentUserId)
