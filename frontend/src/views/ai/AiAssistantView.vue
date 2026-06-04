<template>
  <div class="ai-assistant-page">
    <div class="page-header">
      <h1>🤖 AI 活动建议助手</h1>
      <p class="subtitle">告诉我你的需求，为你推荐个性化活动和搭子</p>
    </div>

    <div class="chat-container">
      <!-- 消息列表 -->
      <div class="messages" ref="messagesRef">
        <div v-if="messages.length === 0" class="welcome-tips">
          <div class="tip-card">
            <h3>💡 你可以这样问我：</h3>
            <ul>
              <li>"我想找附近的运动伙伴"</li>
              <li>"周末有什么有趣的活动推荐？"</li>
              <li>"帮我找一个喜欢摄影的搭子"</li>
              <li>"推荐一些户外活动"</li>
            </ul>
          </div>
        </div>

        <div
          v-for="(msg, index) in messages"
          :key="index"
          :class="['message', msg.role === 'user' ? 'user' : 'ai']"
        >
          <div class="message-avatar">
            {{ msg.role === 'user' ? '👤' : '🤖' }}
          </div>
          <div class="message-content">
            <div class="message-text">{{ msg.content }}</div>
            <div class="message-time">{{ formatTime(msg.time) }}</div>
          </div>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="message ai">
          <div class="message-avatar">🤖</div>
          <div class="message-content">
            <div class="typing-indicator">
              <span></span><span></span><span></span>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="input-area">
        <div class="user-info">
          <span class="info-label">兴趣标签：</span>
          <span class="tags">{{ user?.tags?.join(',') || '未设置' }}</span>
        </div>
        <div class="input-wrapper">
          <input
            v-model="userInput"
            @keyup.enter="sendMessage"
            placeholder="输入你的需求，如：想找附近的运动伙伴"
            :disabled="loading"
          />
          <button @click="sendMessage" :disabled="loading || !userInput.trim()">
            {{ loading ? '思考中...' : '发送' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { getSuggestion } from '@/api/ai'

const userStore = useUserStore()
const user = computed(() => userStore.userInfo)

const userInput = ref('')
const loading = ref(false)
const messagesRef = ref<HTMLElement>()

interface Message {
  role: 'user' | 'ai'
  content: string
  time: Date
}

const messages = ref<Message[]>([])

const sendMessage = async () => {
  if (!userInput.value.trim() || !user.value) return

  const query = userInput.value.trim()
  messages.value.push({
    role: 'user',
    content: query,
    time: new Date()
  })

  userInput.value = ''
  loading.value = true

  scrollToBottom()

  try {
    const res = await getSuggestion({
      tags: user.value.tags?.join(',') || '',
      location: (user.value.latitude && user.value.longitude) ? `${user.value.latitude},${user.value.longitude}` : '未知位置',
      query: query
    })

    messages.value.push({
      role: 'ai',
      content: res.suggestion,
      time: new Date()
    })

    scrollToBottom()
  } catch (error) {
    messages.value.push({
      role: 'ai',
      content: '抱歉，服务暂时不可用，请稍后再试',
      time: new Date()
    })
    scrollToBottom()
  } finally {
    loading.value = false
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

const formatTime = (date: Date) => {
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}
</script>

<style scoped>
.ai-assistant-page {
  min-height: calc(100vh - var(--nav-height));
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 28px;
  margin-bottom: 8px;
  color: var(--color-text);
}

.subtitle {
  color: var(--color-text-secondary);
  font-size: 14px;
}

.chat-container {
  border: 1px solid var(--color-border);
  border-radius: 16px;
  height: 600px;
  display: flex;
  flex-direction: column;
  background: rgba(18, 16, 42, 0.5);
  backdrop-filter: blur(10px);
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.welcome-tips {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.tip-card {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 24px;
  max-width: 400px;
}

.tip-card h3 {
  font-size: 16px;
  margin-bottom: 12px;
  color: var(--color-green);
}

.tip-card ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.tip-card li {
  padding: 8px 0;
  color: var(--color-text-secondary);
  font-size: 14px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.tip-card li:last-child {
  border-bottom: none;
}

.message {
  display: flex;
  gap: 12px;
  max-width: 80%;
  animation: messageSlideIn 0.3s ease-out;
}

@keyframes messageSlideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message.ai {
  align-self: flex-start;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.message-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.message-text {
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.6;
  font-size: 14px;
  word-wrap: break-word;
}

.message.user .message-text {
  background: var(--color-green);
  color: #000;
  border-bottom-right-radius: 4px;
}

.message.ai .message-text {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid var(--color-border);
  border-bottom-left-radius: 4px;
}

.message-time {
  font-size: 11px;
  color: var(--color-text-secondary);
  padding: 0 4px;
}

.message.user .message-time {
  text-align: right;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 12px 16px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-text-secondary);
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  30% {
    transform: translateY(-8px);
    opacity: 1;
  }
}

.input-area {
  border-top: 1px solid var(--color-border);
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.user-info {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.info-label {
  margin-right: 8px;
}

.tags {
  color: var(--color-green);
}

.input-wrapper {
  display: flex;
  gap: 12px;
}

.input-wrapper input {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.05);
  color: var(--color-text);
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}

.input-wrapper input:focus {
  border-color: var(--color-green);
}

.input-wrapper input:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.input-wrapper button {
  padding: 12px 24px;
  background: var(--color-green);
  color: #000;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.input-wrapper button:hover:not(:disabled) {
  background: var(--color-green-hover);
  transform: translateY(-1px);
}

.input-wrapper button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
