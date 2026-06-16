import { beforeEach, describe, expect, it, vi } from 'vitest'
import * as chatApi from '@/api/chat'
import request from '@/api/index'

vi.mock('@/api/index', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
  },
}))

describe('API - chat.ts', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('loads chat messages with default pagination', async () => {
    vi.mocked(request.get).mockResolvedValue({
      total: 1,
      list: [],
      targetUserId: 5,
      pageNum: 1,
      pageSize: 50,
    })

    await chatApi.getChatMessages(5)

    expect(request.get).toHaveBeenCalledWith('/chat/messages', {
      params: { targetUserId: 5, pageNum: 1, pageSize: 50 },
    })
  })

  it('loads chat messages with custom pagination and conversation list', async () => {
    vi.mocked(request.get).mockResolvedValue([])

    await chatApi.getChatMessages(6, 2, 20)
    await chatApi.getChatConversations()

    expect(request.get).toHaveBeenNthCalledWith(1, '/chat/messages', {
      params: { targetUserId: 6, pageNum: 2, pageSize: 20 },
    })
    expect(request.get).toHaveBeenNthCalledWith(2, '/chat/conversations')
  })

  it('sends chat messages to the expected endpoint', async () => {
    vi.mocked(request.post).mockResolvedValue('ok')

    await chatApi.sendChatMessage(9, 'hello')

    expect(request.post).toHaveBeenCalledWith('/chat/messages', {
      receiverId: 9,
      content: 'hello',
    })
  })
})
