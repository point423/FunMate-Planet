import { getTagMeta } from '@/utils/tags'
import type { ActivityItem, JournalThumb, UserInfo } from '@/types/user'

export const MAX_AVATAR_BYTES = 5 * 1024 * 1024

export const buildProfileUser = (userInfo: UserInfo | null | undefined) => ({
  nickname: userInfo?.nickname ?? '',
  avatar: userInfo?.avatar ?? '',
  bio: userInfo?.bio ?? '',
  tags: userInfo?.tags ?? [],
  activities: userInfo?.activities ?? 0,
  score: userInfo?.score ?? 0,
  reviewCount: userInfo?.reviewCount ?? 0,
  ranking: userInfo?.ranking ?? '--',
  publicJournals: userInfo?.publicJournals ?? [],
  recentActivities: userInfo?.recentActivities ?? [],
})

export const getProfileDiaries = (user: ReturnType<typeof buildProfileUser>): JournalThumb[] =>
  (user.publicJournals ?? []).slice(0, 6)

export const getProfileActivities = (user: ReturnType<typeof buildProfileUser>): ActivityItem[] =>
  (user.recentActivities ?? []).slice(0, 5)

export const getDisplayTags = (tags: string[]) =>
  tags.map((value) => ({
    value,
    label: getTagMeta(value).label,
    emoji: getTagMeta(value).emoji,
  }))

export const syncEditForm = (
  form: { nickname: string; bio: string; tags: string[] },
  user: ReturnType<typeof buildProfileUser>,
) => {
  form.nickname = user.nickname
  form.bio = user.bio
  form.tags = [...user.tags]
}

export const resolveUploadUrl = (uploadResult: unknown) => {
  if (typeof uploadResult === 'string') return uploadResult
  const result = uploadResult as { url?: string; data?: { url?: string } }
  return result.url ?? result.data?.url ?? ''
}

export const formatActivityDate = (value: string) => {
  if (!value) return ''
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleDateString()
}
