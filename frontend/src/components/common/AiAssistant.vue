<template>
  <div class="ai-assistant">
    <div class="chat-container">
      <div class="messages">
        <div v-for="(msg, index) in messages" :key="index"
             :class="['message', msg.role === 'user' ? 'user' : 'ai']">
          {{ msg.content }}
        </div>
      </div>

      <div class="input-area">
        <input
          v-model="userInput"
          @keyup.enter="sendMessage"
          placeholder="输入你的需求，如：想找附近的运动伙伴"
        />
        <button @click="sendMessage" :disabled="loading">
          {{ loading ? '思考中...' : '发送' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { getSuggestion } from '@/api/ai'
import { useAuth } from '@/composables/useAuth'

const { user } = useAuth()
const userInput = ref('')
const loading = ref(false)
const messages = ref<Array<{role: string, content: string}>>([])

const sendMessage = async () => {
  if (!userInput.value.trim() || !user.value) return

  messages.value.push({ role: 'user', content: userInput.value })
  loading.value = true

  try {
    const res = await getSuggestion({
      tags: user.value.tags || '',
      location: user.value.location || '',
      query: userInput.value
    })

    messages.value.push({ role: 'ai', content: res.data.suggestion })
    userInput.value = ''
  } catch (error) {
    messages.value.push({ role: 'ai', content: '抱歉，服务暂时不可用' })
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.ai-assistant {
  padding: 20px;
}
.chat-container {
  border: 1px solid #ddd;
  border-radius: 10px;
  height: 500px;
  display: flex;
  flex-direction: column;
}
.messages {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
}
.message {
  margin: 10px 0;
  padding: 10px;
  border-radius: 8px;
  max-width: 80%;
}
.message.user {
  background: #007bff;
  color: white;
  margin-left: auto;
}
.message.ai {
  background: #f0f0f0;
}
.input-area {
  display: flex;
  padding: 10px;
  border-top: 1px solid #ddd;
}
.input-area input {
  flex: 1;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 5px;
}
.input-area button {
  margin-left: 10px;
  padding: 10px 20px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}
</style>
