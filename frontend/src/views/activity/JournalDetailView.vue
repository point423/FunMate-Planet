<template>
  <div class="journal-detail" v-if="diary">
    <!-- 顶部封面 -->
    <div class="jd-cover">
      <img :src="diary.coverImage || 'https://images.unsplash.com/photo-1517486808906-6ca8b3f04846?q=80&w=800'" :alt="diary.title">
      <div class="jd-cover-overlay">
        <h2 class="jd-title">{{ diary.title || '活动回顾' }}</h2>
        <div class="jd-meta-tags">
          <span class="m-tag">📍 {{ diary.location || '未知地点' }}</span>
          <span class="m-tag">📅 {{ formatDate(diary.createTime) }}</span>
        </div>
      </div>
    </div>

    <div class="jd-content">
      <!-- 1. AI 总结板块 (带打字机效果) -->
      <div class="ai-summary-card" :class="{ 'is-loading': aiLoading }">
        <div class="ai-header">
          <span class="ai-icon">✨</span>
          <span class="ai-title">AI 星球观察者</span>
          <el-button
            size="small"
            type="primary"
            round
            @click="generateAiSummary"
            :loading="aiLoading"
          >
            {{ aiSummary ? '重新生成' : '开启 AI 回忆录' }}
          </el-button>
        </div>
        <div class="ai-body">
          <p v-if="displayText" class="ai-text typewriter">{{ displayText }}</p>
          <p v-else-if="aiLoading" class="ai-placeholder">正在深度解析本次社交能量...</p>
          <p v-else class="ai-placeholder">点击上方按钮，生成本次活动的 AI 专属总结。</p>
        </div>
      </div>

      <!-- 2. 评价搭子 -->
      <div class="section-header">
        <span class="section-title">评价你的搭子</span>
        <span class="section-desc">好评会让对方在排行榜上更亮眼哦</span>
      </div>

      <div class="participants-list">
        <div v-for="p in otherParticipants" :key="p.userId" class="p-card">
          <el-avatar :size="50" :src="p.avatar" />
          <div class="p-info">
            <div class="p-name">{{ p.nickname }}</div>
            <el-rate
              v-model="p.userRating"
              @change="(val) => handleRate(p.userId, val)"
              :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
            />
          </div>
        </div>
        <div v-if="otherParticipants.length === 0" class="no-data">本次活动只有你自己哦~</div>
      </div>

      <!-- 3. 活动日记内容 -->
      <div class="section-header">
        <span class="section-title">活动记录</span>
      </div>
      <div class="diary-body">
        <div class="diary-text">{{ diary.content || '暂无文字记录' }}</div>
        <div class="diary-images" v-if="parsedImages.length">
          <el-image
            v-for="(img, idx) in parsedImages"
            :key="idx"
            :src="img"
            class="diary-img"
            :preview-src-list="parsedImages"
          />
        </div>
      </div>
    </div>
  </div>

  <div v-else class="jd-loading">
    <el-skeleton :rows="10" animated />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useActivityStore } from '@/stores/activity'
import { useUserStore } from '@/stores/user'
import { getActivityAiSummary, reviewParticipant } from '@/api/activity'
import { formatDate } from '@/utils/format'

const route = useRoute()
const actStore = useActivityStore()
const userStore = useUserStore()

const diary = ref<any>(null)
const aiLoading = ref(false)
const aiSummary = ref('')
const displayText = ref('')

// 过滤掉自己
const otherParticipants = computed(() => {
  if (!diary.value || !diary.value.participants) return []
  return diary.value.participants
    .filter(p => p.userId !== userStore.userInfo?.id)
    .map(p => ({ ...p, userRating: 0 }))
})

const parsedImages = computed(() => {
  if (!diary.value?.images) return []
  try {
    const imgs = typeof diary.value.images === 'string' ? JSON.parse(diary.value.images) : diary.value.images
    return Array.isArray(imgs) ? imgs : [imgs]
  } catch {
    return [diary.value.images]
  }
})

// 打字机效果
const typewriter = (text: string) => {
  displayText.value = ''
  let i = 0
  const timer = setInterval(() => {
    if (i < text.length) {
      displayText.value += text.charAt(i)
      i++
    } else {
      clearInterval(timer)
    }
  }, 30)
}

const generateAiSummary = async () => {
  const id = diary.value.activityId || Number(route.params.id)
  aiLoading.value = true
  try {
    const res = await getActivityAiSummary(id) as any
    // 假设后端 Result.data 是字符串
    aiSummary.value = typeof res === 'string' ? res : (res.data || 'AI 总结生成失败')
    typewriter(aiSummary.value)
  } catch (e) {
    ElMessage.error('AI 正在开小差，请稍后再试')
  } finally {
    aiLoading.value = false
  }
}

const handleRate = async (targetId: number, rating: number) => {
  const activityId = diary.value.activityId
  try {
    await reviewParticipant(activityId, {
      revieweeId: targetId,
      rating: rating,
      comment: '来自日记回顾页的评价'
    })
    ElMessage.success('评价成功，感谢参与！')
  } catch (e) {
    ElMessage.error('评价提交失败')
  }
}

onMounted(async () => {
  const id = Number(route.params.id)
  if (id) {
    diary.value = await actStore.fetchDiaryDetail(id)
  }
})
</script>

<style scoped>
.journal-detail { height: 100%; overflow-y: auto; background: var(--color-bg); color: #eee; }
.jd-cover { height: 260px; position: relative; overflow: hidden; }
.jd-cover img { width: 100%; height: 100%; object-fit: cover; }
.jd-cover-overlay {
  position: absolute; bottom: 0; left: 0; right: 0; padding: 40px 24px 20px;
  background: linear-gradient(transparent, rgba(0,0,0,0.9));
}
.jd-title { font-size: 28px; font-weight: bold; margin: 0; color: #fff; }
.jd-meta-tags { display: flex; gap: 15px; margin-top: 10px; opacity: 0.8; }
.m-tag { font-size: 13px; color: rgba(255,255,255,0.8); }

.jd-content { padding: 24px; display: flex; flex-direction: column; gap: 24px; }

/* AI Card */
.ai-summary-card {
  background: rgba(0, 255, 149, 0.05);
  border: 1px solid rgba(0, 255, 149, 0.2);
  border-radius: 16px; padding: 20px;
}
.ai-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.ai-title { font-weight: bold; color: var(--color-green); font-size: 15px; }
.ai-text { font-size: 14px; line-height: 1.8; color: #ddd; white-space: pre-wrap; }
.ai-placeholder { color: #666; font-size: 13px; font-style: italic; }

.section-header { border-left: 4px solid var(--color-green); padding-left: 12px; margin-top: 10px; }
.section-title { font-size: 16px; font-weight: bold; display: block; }
.section-desc { font-size: 11px; color: #888; margin-top: 2px; }

.participants-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 12px; }
.p-card {
  background: var(--color-surface-1); padding: 12px; border-radius: 12px;
  display: flex; align-items: center; gap: 12px; border: 1px solid var(--color-border);
}
.p-info { flex: 1; }
.p-name { font-size: 13px; font-weight: 500; margin-bottom: 4px; }

.diary-body { background: var(--color-surface-1); padding: 20px; border-radius: 12px; }
.diary-text { font-size: 15px; line-height: 1.6; color: #ccc; margin-bottom: 15px; }
.diary-images { display: grid; grid-template-columns: repeat(auto-fill, minmax(120px, 1fr)); gap: 10px; }
.diary-img { border-radius: 8px; height: 120px; width: 100%; }

.no-data { text-align: center; color: #666; padding: 20px; font-size: 13px; }
.jd-loading { padding: 40px; }

.typewriter {
  border-right: 2px solid var(--color-green);
  animation: blink 0.7s infinite;
}

@keyframes blink { 50% { border-color: transparent; } }
</style>
