<template>
  <div class="journal-view">
    <!-- Left: grid of journal cards -->
    <div class="journal-left">
      <div class="journal-grid">
        <!-- Existing journals -->
        <div
          v-for="j in diaries"
          :key="j.id"
          class="journal-card"
          :class="{ active: activeId === j.id }"
          @click="selectJournal(j.id)"
        >
          <div class="jc-img">
            <!-- 兼容 coverImage 或 images 字段，并提供兜底图 -->
            <img :src="j.coverImage || (j.images ? (Array.isArray(j.images) ? j.images[0] : j.images) : '') || 'https://images.unsplash.com/photo-1517486808906-6ca8b3f04846?q=80&w=400'" :alt="j.title">
          </div>
          <div class="jc-body">
            <!-- 兼容后端 content 字段作为标题 -->
            <div class="jc-title">{{ j.title || j.content || 'Untitled Journal' }}</div>
            <div class="jc-footer">
              <div class="jc-avs">
                <div
                  v-for="p in (j.participants || []).slice(0, 3)"
                  :key="p.userId"
                  class="jc-av"
                >
                  <img :src="p.avatar" :alt="p.nickname">
                </div>
                <span v-if="j.participants?.length > 3" class="more-count">
                  +{{ j.participants.length - 3 }}
                </span>
              </div>
              <span class="jc-date" :class="{ green: activeId === j.id }">
                {{ formatDate(j.createdAt || j.createTime) }}
              </span>
            </div>

          </div>
        </div>

        <!-- New journal button -->
        <div class="journal-card new-card" @click="showEditor = true">
          <span class="new-plus">+</span>
          <span class="new-label">new journal</span>
        </div>
      </div>
    </div>

    <!-- Right: detail or empty state -->
    <div class="journal-right">
      <div class="journal-detail" v-if="activeDiary">
        <!-- 顶部封面 -->
        <div class="jd-cover">
          <img v-if="coverImageUrl" :src="coverImageUrl" :alt="activeDiary.title">
          <div v-else class="jd-cover-placeholder">暂无封面图片</div>
          <div class="jd-cover-overlay">
            <h2 class="jd-title">{{ activeDiary.title || activeDiary.content || '活动回顾' }}</h2>
            <div class="jd-meta-tags">
              <span class="m-tag">📍 {{ activeDiary.location || '未知地点' }}</span>
              <span class="m-tag">📅 {{ formatDate(activeDiary.createdAt || activeDiary.createTime) }}</span>
              <span class="m-tag">
                👥 {{ activeDiary.participants?.length || 0 }}人
                <div class="participant-avatars">
                  <el-avatar
                    v-for="(p, idx) in (activeDiary.participants || []).slice(0, 3)"
                    :key="p.userId"
                    :size="24"
                    :src="p.avatar"
                    class="participant-avatar"
                  />
                  <span v-if="activeDiary.participants?.length > 3" class="more-count">
                    +{{ activeDiary.participants.length - 3 }}
                  </span>
                </div>
              </span>
            </div>

          </div>
        </div>

        <div class="jd-content">
          <!-- 1. AI 总结板块 -->
          <div class="ai-summary-card" :class="{ 'is-loading': aiLoading }">
            <div class="ai-header">
              <span class="ai-icon">✨</span>
              <span class="ai-title">AI 星球观察者</span>
              <el-button size="small" type="primary" round @click="generateAiSummary" :loading="aiLoading">
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
                <el-rate v-model="p.userRating" @change="(val) => handleRate(p.userId, val)" :colors="['#99A9BF', '#F7BA2A', '#FF9900']" />
              </div>
            </div>
            <div v-if="otherParticipants.length === 0" class="no-data">本次活动只有你自己哦~</div>
          </div>

          <!-- 3. 活动日记内容 -->
          <div class="section-header">
            <span class="section-title">活动记录</span>
          </div>
          <div class="diary-body">
            <div class="diary-text">{{ activeDiary.content || '暂无文字记录' }}</div>
            <div class="diary-images" v-if="parsedImages.length">
              <el-image 
                v-for="(img, idx) in parsedImages" 
                :key="idx" 
                :src="img" 
                class="diary-img" 
                :preview-src-list="parsedImages" 
                :preview-teleported="true"
                :z-index="9999"
              />

            </div>
          </div>
        </div>
        
                  <!-- 4. 底部操作栏 -->
          <div class="jd-footer">
            <el-popconfirm
              title="确定要删除这篇日记吗？删除后不可恢复哦~"
              confirm-button-text="确定删除"
              cancel-button-text="再想想"
              confirm-button-type="danger"
              @confirm="handleDelete"
            >
              <template #reference>
                <el-button type="danger" plain :icon="Delete">删除日记</el-button>
              </template>
            </el-popconfirm>
          </div><!-- end of journal-detail -->
      </div>

      <div v-else class="jr-empty">
        <span style="font-size:48px">📒</span>
        <p>Select a journal to view</p>
      </div>
    </div>



    <!-- Diary editor dialog -->
    <DiaryEditor
      v-if="showEditor"
      v-model="showEditor"
      :activity-id="0"
      :partners="partners"
      @saved="onSaved"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import DiaryEditor from '@/components/activity/DiaryEditor.vue'
import { useActivityStore } from '@/stores/activity'
import { useUserStore } from '@/stores/user'
import { getActivityAiSummary, reviewParticipant } from '@/api/activity'
import { formatDate } from '@/utils/format'
import { getFriends } from '@/api/user'
import { Delete } from '@element-plus/icons-vue'

const actStore     = useActivityStore()
const userStore    = useUserStore()

// 使用 computed 保证 Store 数据变化时 UI 自动更新
// const diaries      = computed(() => actStore.diaries)
const diaries = computed(() => {
  const diariesList = actStore.diaries
  if (!diariesList || !Array.isArray(diariesList)) return []
  
  return diariesList.map(item => {
    // 处理两种可能的数据结构
    const diary = item.diary || item
    const participants = item.participants || []
    
    // 1. 处理标题
    const title = diary.title || (diary.content ? diary.content.substring(0, 20) + (diary.content.length > 20 ? '...' : '') : 'Untitled Journal')
    
    // 2. 处理图片：使用 parseImages 函数解析图片
    const images = parseImages(diary.images)
    
    // 3. 处理封面：取解析后的图片数组第一张作为封面
    const coverImage = images.length > 0 ? images[0] : ''

    return { 
      ...diary, 
      title, 
      images, 
      coverImage,
      participants 
    }
  })
})


const activeId     = ref<number | null>(null)
const activeDiary  = computed(() => diaries.value.find(d => d.id === activeId.value))
const showEditor   = ref(false)
const partners     = ref<any[]>([])

// 统一解析后端返回的 images 字段，兼容 JSON字符串、逗号分隔字符串、数组
const parseImages = (images: any): string[] => {
  if (!images) return []
  if (Array.isArray(images)) return images.filter(Boolean)
  if (typeof images === 'string') {
    try {
      const parsed = JSON.parse(images)
      return Array.isArray(parsed) ? parsed.filter(Boolean) : [images]
    } catch {
      return images.split(',').filter(Boolean)
    }
  }
  return []
}


const selectJournal = (id: number) => {
  activeId.value = id
  // router.push(`/activity/journal/${id}`)
}

const onSaved = () => { actStore.fetchDiaries() }

// AI 总结相关
const aiLoading = ref(false)
const aiSummary = ref('')
const displayText = ref('')

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
  if (!activeDiary.value) return
  const id = activeDiary.value.activityId || activeId.value
  aiLoading.value = true
  try {
    const res = await getActivityAiSummary(id) as any
    aiSummary.value = typeof res === 'string' ? res : (res.data || 'AI 总结生成失败')
    typewriter(aiSummary.value)
  } catch (e) {
    ElMessage.error('AI 正在开小差，请稍后再试')
  } finally {
    aiLoading.value = false
  }
}

// 评价搭子相关
const otherParticipants = computed(() => {
  if (!activeDiary.value || !activeDiary.value.participants) return []
  return activeDiary.value.participants
    .filter((p: any) => p.userId !== userStore.userInfo?.id)
    .map((p: any) => ({ ...p, userRating: 0 }))
})

const handleRate = async (targetId: number, rating: number) => {
  if (!activeDiary.value) return
  const activityId = activeDiary.value.activityId
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

// 图片解析
const parsedImages = computed(() => parseImages(activeDiary.value?.images))


// 封面图优先级：coverImage -> images数组第一张 -> images字符串 -> 无图
// const coverImageUrl = computed(() => {
//   const d = activeDiary.value
//   if (!d) return ''
//   if (d.coverImage) return d.coverImage
//   if (d.images) {
//     if (Array.isArray(d.images)) return d.images[0] || ''
//     if (typeof d.images === 'string') {
//       try {
//         const parsed = JSON.parse(d.images)
//         return Array.isArray(parsed) ? parsed[0] || '' : d.images
//       } catch {
//         return d.images
//       }
//     }
//   }
//   return ''
// })
const coverImageUrl = computed(() => activeDiary.value?.coverImage || '')



// 删除日记相关
const handleDelete = async () => {
  if (!activeDiary.value) return
  try {
    // 假设 actStore 中有删除日记的方法，或者直接调用 API
    // 此处以 actStore 为例，若没有请在 store 中补充或直接调用 api
    await actStore.deleteDiary(activeDiary.value.id) 
    
    ElMessage.success('日记已成功删除')
    activeId.value = null // 清空选中状态，右侧回到空状态界面
    
    // 删除后刷新列表
    await actStore.fetchDiaries() 
  } catch (error) {
    ElMessage.error('删除失败，请稍后重试')
  }
}

onMounted(async () => { 
  actStore.fetchDiaries()
  try {
    const data = await getFriends()
    partners.value = Array.isArray(data) ? data : []
  } catch { /* ignore */ }
})
</script>

<style scoped>
.journal-view { display: flex; flex: 1; overflow: hidden; height: 100%; }

/* Left panel */
.journal-left {
  width: 420px; flex-shrink: 0;
  background: rgba(255,255,255,.04);
  border-right: 0.5px solid var(--color-border-dim);
  overflow-y: auto; padding: 22px 18px;
}

.journal-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }

.journal-card {
  border-radius: var(--radius-lg); overflow: hidden;
  background: var(--color-card-solid);
  border: 0.5px solid var(--color-border-dim);
  cursor: pointer; transition: transform .15s, border-color .15s;
}
.journal-card:hover  { transform: translateY(-3px); border-color: rgba(255,255,255,.22); }
.journal-card.active { border-color: var(--color-green); }

.jc-img { width: 100%; height: 130px; overflow: hidden; background: #333; }
.jc-img img { width: 100%; height: 100%; object-fit: cover; }

.jc-body   { padding: 10px 12px; }
.jc-title  { font-size: 14px; font-weight: 500; margin-bottom: 8px; color: var(--color-text); }
.jc-footer { display: flex; align-items: center; justify-content: space-between; }
.jc-avs    { display: flex; }
.jc-av     { width: 22px; height: 22px; border-radius: 50%; overflow: hidden; background: #555; border: 1.5px solid #000; margin-left: -5px; }
.jc-av:first-child { margin-left: 0; }
.jc-av img { width: 100%; height: 100%; object-fit: cover; }
.jc-date      { font-size: 11px; color: var(--color-text-secondary); }
.jc-date.green{ color: var(--color-green); }

/* New card */
.new-card {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  min-height: 160px; border: 1.5px dashed var(--color-border) !important;
  background: transparent !important; color: var(--color-text-secondary);
  gap: 8px; transition: border-color .15s, color .15s !important;
}
.new-card:hover { border-color: var(--color-green-border) !important; color: var(--color-green) !important; }
.new-plus  { font-size: 28px; }
.new-label { font-size: 12px; font-family: monospace; }

/* Right panel */
.journal-right { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.jr-empty {
  flex: 1; display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  gap: 12px; opacity: .4; font-size: 14px;
}

/* Detail panel */
/* .jr-detail { flex: 1; overflow-y: auto; padding: 24px; }
.jr-cover  { width: 100%; max-height: 320px; border-radius: var(--radius-lg); overflow: hidden; margin-bottom: 20px; background: #333; }
.jr-cover img { width: 100%; height: 100%; object-fit: cover; }
.jr-info   { padding: 0 4px; }
.jr-title  { font-size: 22px; font-weight: 600; margin-bottom: 12px; color: var(--color-text); }
.jr-meta   { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.jr-date   { font-size: 13px; color: var(--color-text-secondary); }
.jr-avs    { display: flex; }
.jr-content { font-size: 15px; line-height: 1.7; color: var(--color-text-secondary); white-space: pre-wrap; } */

/* Detail panel from JournalDetailView */
.journal-detail { 
  height: 100%; 
  overflow-y: auto; 
  background: var(--color-bg); 
  color: #eee; 
  display: flex;          /* 新增 */
  flex-direction: column; /* 新增 */
}
.jd-cover { height: 260px; position: relative; overflow: hidden; flex-shrink: 0; } /* 新增 flex-shrink */
.jd-cover img { width: 100%; height: 100%; object-fit: cover; }
.jd-cover-placeholder {
  width: 100%; height: 100%; 
  display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #1a1a1a 0%, #2c2c2c 100%);
  color: #555; font-size: 14px;
}

.jd-cover-overlay {
  position: absolute; bottom: 0; left: 0; right: 0; padding: 40px 24px 20px;
  background: linear-gradient(transparent, rgba(0,0,0,0.9));
}
.jd-title { font-size: 28px; font-weight: bold; margin: 0; color: #fff; }
.jd-meta-tags { display: flex; gap: 15px; margin-top: 10px; opacity: 0.8; }

.participant-avatars {
  display: flex;
  align-items: center;
  gap: -8px;
  margin-left: 8px;
}

.participant-avatar {
  border: 2px solid var(--color-bg);
  margin-left: -8px;
}

.more-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: var(--color-surface-1);
  border: 2px solid var(--color-bg);
  margin-left: -8px;
  font-size: 12px;
  color: var(--color-text-secondary);
}


.m-tag { font-size: 13px; color: rgba(255,255,255,0.8); }

.jd-content { 
  padding: 24px; 
  display: flex; 
  flex-direction: column; 
  gap: 24px; 
  flex: 1;               /* 新增：占据剩余空间 */
}
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

/* 强制修改 Element Plus 图片预览的关闭按钮颜色为黑色，解决白色导航栏不可见问题 */
:deep(.el-image-viewer__close) {
  color: #000 !important;
}
:deep(.el-image-viewer__close .el-icon) {
  color: #000 !important;
  background-color: rgba(255, 255, 255, 0.7) !important;
  border-radius: 50%;
}




.no-data { text-align: center; color: #666; padding: 20px; font-size: 13px; }

.typewriter {
  border-right: 2px solid var(--color-green);
  animation: blink 0.7s infinite;
}

@keyframes blink { 50% { border-color: transparent; } }


</style>
