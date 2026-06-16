import type { ChatMessageDto } from '@/api/chat'
import type { Activity, ActivityDetailResponse, ActivityStatus } from '@/types/activity'
import type { NearbyUser } from '@/types/user'
import { getTagMeta } from '@/utils/tags'

export interface MsgItem {
  id?: string
  content: string
  mine: boolean
  timestamp?: string
}

export interface ActivityEntry {
  activityId?: number
  journalId?: number
  title: string
  date: string
  scheduledAt: string
  coverImg: string
  intro: string
  participants: string[]
  participantIds: number[]
  location: string
  statusLabel: string
  participantCount: number
  maxParticipants: number
  description: string
}

export const EMPTY_LAST_MESSAGE = 'No messages yet'

export const normalizeTagValues = (tags: unknown): string[] => {
  if (Array.isArray(tags)) {
    return tags.map((tag) => String(tag).trim()).filter(Boolean)
  }
  if (typeof tags === 'string') {
    return tags.split(/(?:[;,]+|锛寍銆?)/).map((tag) => tag.trim()).filter(Boolean)
  }
  return []
}

export const getApplicationDisplayTags = (tags: unknown) =>
  normalizeTagValues(tags).map(getTagMeta)

export const normalizeUserShape = (raw: Record<string, unknown>): NearbyUser => ({
  id: Number(raw?.id ?? 0),
  username: String(raw?.username ?? ''),
  nickname: String(raw?.nickname ?? (raw?.id ? `User ${raw.id}` : 'Unknown user')),
  avatar: String(raw?.avatar ?? '/default-avatar.png'),
  bio: String(raw?.bio ?? ''),
  tags: normalizeTagValues(raw?.tags),
  activities: Number(raw?.activities ?? 0),
  score: Number(raw?.score ?? raw?.averageScore ?? 0),
  ranking: Number(raw?.ranking ?? 0),
  longitude: raw?.longitude as number | undefined,
  latitude: raw?.latitude as number | undefined,
  createdAt: String(raw?.createdAt ?? raw?.createTime ?? ''),
  distance: Number(raw?.distance ?? 0),
  publicJournals: Array.isArray(raw?.publicJournals) ? raw.publicJournals as NearbyUser['publicJournals'] : [],
  recentActivities: Array.isArray(raw?.recentActivities) ? raw.recentActivities as NearbyUser['recentActivities'] : [],
})

export const formatMsgTime = (value: unknown) => {
  if (!value) return ''
  const date = new Date(String(value))
  if (Number.isNaN(date.getTime())) return ''
  return date.toLocaleString()
}

export const activityStatusLabel = (status: ActivityStatus | number | undefined) => {
  const map: Record<number, string> = {
    0: 'Pending',
    1: 'Active',
    2: 'Completed',
    3: 'Cancelled',
  }
  return map[Number(status ?? 1)] ?? 'Active'
}

export const buildSharedActivityEntry = (params: {
  activity: Activity
  detail: ActivityDetailResponse
  currentUserId: number
  currentUserNickname: string
  partners: Array<{ id: number; nickname: string; avatar: string; publicJournals?: NearbyUser['publicJournals'] }>
}): ActivityEntry => {
  const { activity, detail, currentUserId, currentUserNickname, partners } = params
  const participantIds = (detail.participants ?? []).map((participant) => Number(participant.id)).filter(Boolean)
  const participantNames = (detail.participants ?? []).map((participant) => participant.nickname).filter(Boolean)
  const currentPartnerId = participantIds.find((id) => id !== currentUserId)
  const currentPartner = partners.find((partner) => partner.id === currentPartnerId)
  const partnerName = currentPartner?.nickname || participantNames.find((name) => name !== currentUserNickname) || 'your partner'

  return {
    activityId: activity.id,
    title: activity.title || `${partnerName}'s activity`,
    date: formatMsgTime(activity.activityTime) || 'Time TBD',
    scheduledAt: activity.activityTime,
    coverImg: currentPartner?.publicJournals?.[0]?.coverImage || currentPartner?.avatar || '/default-avatar.png',
    intro: `You and ${partnerName} are currently doing this activity together.`,
    participants: participantNames,
    participantIds,
    location: activity.location || 'Location TBD',
    statusLabel: activityStatusLabel(activity.status),
    participantCount: detail.participantCount ?? participantIds.length,
    maxParticipants: activity.maxParticipants || detail.participantCount || participantIds.length || 2,
    description: activity.description?.trim() || 'No detailed introduction has been added for this activity yet.',
  }
}

export const normalizeBackendMessages = (list: ChatMessageDto[], currentUserId: number) =>
  list
    .slice()
    .sort((a, b) => new Date(a.createTime).getTime() - new Date(b.createTime).getTime())
    .map((item) => ({
      id: item.id,
      content: String(item.content ?? ''),
      mine: Number(item.senderId) === currentUserId,
      timestamp: formatMsgTime(item.createTime),
    }))

export const mergeMessages = (localMessages: MsgItem[], backendMessages: MsgItem[]) => {
  const merged = [...localMessages]
  backendMessages.forEach((message) => {
    if (!merged.some((existing) => existing.id === message.id)) merged.push(message)
  })
  return merged
}

export const getLastCachedMessage = (messages: MsgItem[]) => messages[messages.length - 1]

export const buildConversationMap = (conversationData: Array<Record<string, unknown>>) => {
  const conversationMap = new Map<number, { lastMessage: string; lastMessageTime: string }>()
  for (const item of conversationData) {
    const userId = Number(item.userId ?? 0)
    if (!userId) continue
    conversationMap.set(userId, {
      lastMessage: String(item.lastMessage ?? ''),
      lastMessageTime: String(item.lastMessageTime ?? ''),
    })
  }
  return conversationMap
}

export const buildPartnerSummary = (params: {
  id: number
  detail: NearbyUser
  conversation?: { lastMessage: string; lastMessageTime: string }
  cachedMessage?: MsgItem
  emptyLastMessage?: string
}) => {
  const { id, detail, conversation, cachedMessage, emptyLastMessage = EMPTY_LAST_MESSAGE } = params
  return {
    id,
    nickname: detail.nickname,
    avatar: detail.avatar || '/default-avatar.png',
    lastMsg: conversation?.lastMessage || cachedMessage?.content || emptyLastMessage,
    lastMsgDate: conversation?.lastMessageTime ? formatMsgTime(conversation.lastMessageTime) : cachedMessage?.timestamp || '',
    distance: detail.distance,
    score: detail.score,
    publicJournals: detail.publicJournals,
    recentActivities: detail.recentActivities,
  }
}

export const getLocalMessageStorageKey = (userId: number, partnerId: number) =>
  `chat_messages_${userId}_${partnerId}`

export const removeInvalidMessageKeys = (
  storage: Pick<Storage, 'length' | 'key' | 'removeItem'>,
  userId: number,
  partnerIds: number[],
) => {
  const currentPartnerIds = new Set(partnerIds)
  for (let index = 0; index < storage.length; index++) {
    const key = storage.key(index)
    if (!key?.startsWith(`chat_messages_${userId}_`)) continue
    const partnerId = Number(key.replace(`chat_messages_${userId}_`, ''))
    if (!currentPartnerIds.has(partnerId)) storage.removeItem(key)
  }
}

export const createPendingActivityEntry = (params: {
  activity: Partial<Activity> & { id: number; activityTime?: string }
  partner: { id: number; nickname: string; avatar: string; publicJournals?: NearbyUser['publicJournals'] }
  draft: { title: string; location: string; description: string; startTime: string; maxParticipants: number }
  currentUserId: number
  currentUserNickname: string
}): ActivityEntry => {
  const { activity, partner, draft, currentUserId, currentUserNickname } = params
  return {
    activityId: activity.id,
    title: draft.title,
    date: formatMsgTime(activity.activityTime || draft.startTime),
    scheduledAt: activity.activityTime || draft.startTime,
    coverImg: partner.publicJournals?.[0]?.coverImage || partner.avatar,
    intro: `${draft.title} is waiting for ${partner.nickname} to accept.`,
    participants: [currentUserNickname || 'Me', partner.nickname],
    participantIds: [currentUserId, partner.id].filter(Boolean),
    location: draft.location,
    statusLabel: 'Pending',
    participantCount: 1,
    maxParticipants: Number(draft.maxParticipants || 2),
    description: draft.description || 'Waiting for your partner to accept this activity invitation.',
  }
}
