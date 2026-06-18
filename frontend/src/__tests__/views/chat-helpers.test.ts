import { describe, expect, it } from 'vitest'
import {
  activityStatusLabel,
  buildConversationMap,
  buildPartnerSummary,
  buildSharedActivityEntry,
  createPendingActivityEntry,
  EMPTY_LAST_MESSAGE,
  formatMsgTime,
  getApplicationDisplayTags,
  getLastCachedMessage,
  getLocalMessageStorageKey,
  mergeMessages,
  normalizeBackendMessages,
  normalizeTagValues,
  normalizeUserShape,
  removeInvalidMessageKeys,
} from '@/views/chat/chat-helpers'

describe('chat helpers', () => {
  it('normalizes tag values and user shapes', () => {
    expect(normalizeTagValues(['read', '', 'travel'])).toEqual(['read', 'travel'])
    expect(normalizeTagValues('read,travel;music')).toEqual(['read', 'travel', 'music'])
    expect(getApplicationDisplayTags('read,travel').map((tag) => tag.value)).toEqual(['read', 'travel'])

    expect(
      normalizeUserShape({
        id: '5',
        nickname: 'Amy',
        averageScore: 88,
        tags: 'read,travel',
      }),
    ).toMatchObject({
      id: 5,
      nickname: 'Amy',
      avatar: '/default-avatar.png',
      tags: ['read', 'travel'],
      score: 88,
    })
  })

  it('formats message time and activity status labels', () => {
    expect(formatMsgTime(undefined)).toBe('')
    expect(formatMsgTime('invalid')).toBe('')
    expect(formatMsgTime('2026-06-16T00:00:00Z')).toBeTruthy()
    expect(activityStatusLabel(0)).toBe('Pending')
    expect(activityStatusLabel(1)).toBe('Active')
    expect(activityStatusLabel(2)).toBe('Completed')
    expect(activityStatusLabel(3)).toBe('Cancelled')
    expect(activityStatusLabel(99)).toBe('Active')
  })

  it('builds shared activity entries and pending activity overrides', () => {
    const shared = buildSharedActivityEntry({
      activity: {
        id: 1,
        creatorId: 1,
        title: '',
        description: '  shared activity ',
        activityTime: '2026-06-16T10:00:00Z',
        location: 'Lake',
        maxParticipants: 4,
        status: 1,
        createTime: '',
      },
      detail: {
        activity: {} as any,
        participants: [
          { id: 1, nickname: 'Me', avatar: '' },
          { id: 2, nickname: 'Amy', avatar: '' },
        ],
        participantCount: 2,
        hasJournal: true,
      },
      currentUserId: 1,
      currentUserNickname: 'Me',
      partners: [{ id: 2, nickname: 'Amy', avatar: '/amy.png', publicJournals: [{ id: 1, coverImage: '/cover.png', title: 'Trip' }] }],
    })

    expect(shared).toMatchObject({
      activityId: 1,
      title: "Amy's activity",
      coverImg: '/cover.png',
      location: 'Lake',
      statusLabel: 'Active',
      participantCount: 2,
      maxParticipants: 4,
      description: 'shared activity',
    })

    const pending = createPendingActivityEntry({
      activity: { id: 2, activityTime: '2026-06-17T10:00:00Z' },
      partner: { id: 2, nickname: 'Amy', avatar: '/amy.png', publicJournals: [] },
      draft: {
        title: 'Sunset ride',
        location: 'West Lake',
        description: '',
        startTime: '2026-06-17T10:00:00Z',
        maxParticipants: 3,
      },
      currentUserId: 1,
      currentUserNickname: 'Me',
    })

    expect(pending).toMatchObject({
      activityId: 2,
      title: 'Sunset ride',
      location: 'West Lake',
      statusLabel: 'Pending',
      participantCount: 1,
      maxParticipants: 3,
    })
  })

  it('normalizes backend messages and merges them with cached ones', () => {
    const normalized = normalizeBackendMessages([
      {
        id: '2',
        senderId: 2,
        receiverId: 1,
        content: 'later',
        createTime: '2026-06-16T11:00:00Z',
        isRead: true,
      },
      {
        id: '1',
        senderId: 1,
        receiverId: 2,
        content: 'first',
        createTime: '2026-06-16T10:00:00Z',
        isRead: true,
      },
    ], 1)

    expect(normalized[0]).toMatchObject({ id: '1', mine: true, content: 'first' })
    expect(normalized[1]).toMatchObject({ id: '2', mine: false, content: 'later' })

    const merged = mergeMessages([{ id: '1', content: 'first', mine: true }], normalized)
    expect(merged).toHaveLength(2)
    expect(getLastCachedMessage(merged)?.id).toBe('2')
  })

  it('builds conversation maps and partner summaries', () => {
    const map = buildConversationMap([
      { userId: 2, lastMessage: 'hello', lastMessageTime: '2026-06-16T10:00:00Z' },
      { userId: 0, lastMessage: 'skip', lastMessageTime: '' },
    ])

    expect(map.get(2)).toEqual({
      lastMessage: 'hello',
      lastMessageTime: '2026-06-16T10:00:00Z',
    })

    const summary = buildPartnerSummary({
      id: 2,
      detail: {
        id: 2,
        username: 'amy',
        nickname: 'Amy',
        avatar: '',
        bio: '',
        tags: [],
        activities: 0,
        score: 91,
        ranking: 1,
        createdAt: '',
        distance: 1.2,
        publicJournals: [],
        recentActivities: [],
      } as any,
      cachedMessage: { id: 'm1', content: 'cached', mine: false, timestamp: 'yesterday' },
    })

    expect(summary.lastMsg).toBe('cached')
    expect(summary.lastMsgDate).toBe('yesterday')
    expect(
      buildPartnerSummary({
        id: 3,
        detail: summary as any,
      }).lastMsg,
    ).toBe(EMPTY_LAST_MESSAGE)
  })

  it('uses stable local-storage keys and removes stale chat caches', () => {
    expect(getLocalMessageStorageKey(1, 2)).toBe('chat_messages_1_2')

    const removed: string[] = []
    const storage = {
      length: 3,
      key: (index: number) => ['chat_messages_1_2', 'chat_messages_1_3', 'other'][index] ?? null,
      removeItem: (key: string) => {
        removed.push(key)
      },
    }

    removeInvalidMessageKeys(storage, 1, [2])
    expect(removed).toEqual(['chat_messages_1_3'])
  })
})
