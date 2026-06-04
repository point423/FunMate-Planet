<template>
  <div class="find-partner-view">
    <!-- ── 广场主区域 ─────────────────────────── -->
    <div class="fp-main">
      <!-- 顶部统计 -->
      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-card-head">活动参与 <span>🔥</span></div>
          <div class="stat-card-val">{{ myStats.activities }} 次已完成</div>
        </div>
        <div class="stat-card">
          <div class="stat-card-head">社交评分 <span>🌟</span></div>
          <div class="stat-card-val">⭐ {{ myStats.score }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-card-head">全站排名 <span>🔖</span></div>
          <div class="stat-card-val">{{ myStats.ranking }}</div>
        </div>
      </div>

      <!-- 模式切换与搜索 -->
      <div class="mode-header">
        <div class="mode-tabs">
          <button :class="{ active: mode === 'partners' }" @click="mode = 'partners'">找搭子</button>
          <button :class="{ active: mode === 'activities' }" @click="mode = 'activities'">找活动</button>
        </div>

        <div class="action-bar">
          <el-button v-if="mode === 'activities'" type="primary" round @click="openCreateModal">＋ 发起活动</el-button>
          <div class="fp-search-wrap">
            <el-icon class="fp-search-icon"><Search /></el-icon>
            <input v-model="searchQ" class="fp-search" :placeholder="mode === 'partners' ? '搜索昵称...' : '搜索活动标题...'">
          </div>
        </div>
      </div>

      <!-- 活动/搭子列表 -->
      <div class="content-scroll" v-loading="loading">
        <div v-if="mode === 'partners'" class="partners-grid">
          <UserCard v-for="user in filteredUsers" :key="user.id" :user="user" @select="openUserDetail" />
        </div>

        <div v-else class="activities-grid">
          <!-- 这里使用后端返回的包装对象 { activity, joinedCount } -->
          <ActivityCard
            v-for="item in filteredActivities"
            :key="item.activity.id"
            :activity="item.activity"
            :joined-count="item.joinedCount"
            @click="showDetail"
          />
        </div>

        <div v-if="isEmpty" class="no-results">暂时没有符合条件的活动，要不你来发起一个？</div>
      </div>
    </div>

    <!-- ── 右侧社交达人榜 ── -->
    <aside class="leaderboard-panel">
       <h3 class="lb-title">社交达人榜 🏆</h3>
       <div class="lb-list">
        <div v-for="(u, i) in leaderboard" :key="u.id || i" class="lb-item">
          <span class="lb-rank">#{{ i + 1 }}</span>
          <img :src="u.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" class="lb-ava">
          <div class="lb-info">
            <div class="lb-name">{{ u.nickname || '神秘搭子' }}</div>
            <div class="lb-meta">⭐ {{ u.score || 0 }} · {{ u.reviewCount || 0 }}评</div>
          </div>
        </div>
      </div>
    </aside>

    <!-- ── 活动放大详情页 (这就是您要的放大详情) ── -->
    <el-dialog v-model="detailVisible" width="750px" append-to-body class="dark-dialog magnified-activity-dialog" destroy-on-close>
      <template #header>
        <div class="magnified-header-info">
          <el-tag v-if="currentDetail" :type="statusTagType(currentDetail.activity.status)">{{ statusText(currentDetail.activity.status) }}</el-tag>
          <span class="header-subtitle">活动详情档案馆</span>
        </div>
      </template>

      <div v-if="currentDetail" class="magnified-body-container">
        <h1 class="act-magnified-title">{{ currentDetail.activity.title }}</h1>

        <div class="act-specs-row">
          <div class="spec-unit">
            <el-icon><Calendar /></el-icon>
            <div class="unit-text">
              <span class="unit-label">活动日期时间</span>
              <span class="unit-val">{{ formatDate(currentDetail.activity.activityTime) }}</span>
            </div>
          </div>
          <div class="spec-unit">
            <el-icon><Location /></el-icon>
            <div class="unit-text">
              <span class="unit-label">集合具体地点</span>
              <span class="unit-val">{{ currentDetail.activity.location }}</span>
            </div>
          </div>
        </div>

        <div class="magnified-section">
          <h4 class="section-label-text">活动详情与要求</h4>
          <div class="magnified-desc-box">{{ currentDetail.activity.description || '主人很懒，什么都没写哦~' }}</div>
        </div>

        <div class="magnified-section">
          <div class="members-title-row">
            <h4 class="section-label-text">当前参与成员</h4>
            <span class="members-fraction">{{ currentDetail.participantCount }} / {{ currentDetail.activity.maxParticipants }} 人</span>
          </div>
          <el-progress
            :percentage="Math.min(100, (currentDetail.participantCount / currentDetail.activity.maxParticipants) * 100)"
            :show-text="false"
            color="#00ff95"
            stroke-width="12"
            class="members-progress"
          />
          <div class="members-avatar-wall">
            <div v-for="p in currentDetail.participants" :key="p.id" class="member-ava-wrapper">
              <el-tooltip :content="p.nickname" placement="top">
                <img :src="p.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" class="wall-circle-ava">
              </el-tooltip>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="magnified-footer-actions">
          <!-- 权限控制：只有发起人可见编辑 -->
          <el-button v-if="isCreator" type="warning" plain size="large" round @click="handleEdit">
            <el-icon style="margin-right: 5px;"><Edit /></el-icon> 编辑活动资料
          </el-button>

          <div class="footer-right-group">
            <!-- 一键进入聊天室：加入后显示 -->
            <el-button v-if="hasJoined" type="success" size="large" round @click="goToChat">
              <el-icon style="margin-right: 5px;"><ChatDotRound /></el-icon> 进入活动聊天室
            </el-button>

            <el-button
              v-if="!hasJoined && currentDetail?.activity.status === 0"
              type="primary"
              size="large"
              round
              :loading="joining"
              @click="handleJoin"
            >
              申请入队
            </el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- 发起/修改活动表单 -->
    <el-dialog v-model="showCreateModal" :title="isEditing ? '更新活动档案' : '发起新活动'" width="500px" append-to-body class="dark-dialog">
      <el-form :model="createForm" label-position="top">
        <el-form-item label="活动标题" required><el-input v-model="createForm.title" placeholder="例如：周末下午茶" /></el-form-item>
        <el-form-item label="详细介绍"><el-input v-model="createForm.description" type="textarea" rows="4" placeholder="活动安排、AA说明等..." /></el-form-item>
        <div style="display: flex; gap: 16px;">
          <el-form-item label="开始时间" required style="flex: 1;"><el-date-picker v-model="createForm.activityTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%;" /></el-form-item>
          <el-form-item label="人数上限" required style="width: 120px;"><el-input-number v-model="createForm.maxParticipants" :min="2" :max="100" /></el-form-item>
        </div>
        <el-form-item label="集合地点" required><el-input v-model="createForm.location" placeholder="具体地点" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateModal = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="submitActivity">{{ isEditing ? '确认修改' : '确认发布' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Calendar, Location, Edit, ChatDotRound } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import UserCard from '@/components/discover/UserCard.vue'
import ActivityCard from '@/components/activity/ActivityCard.vue'
import { getNearbyUsers } from '@/api/dazi'
import { joinActivity, createActivity, updateActivity, getActivityDetail, getLeaderboard as getActivityLeaderboard, getActivities } from '@/api/activity'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// --- 基础状态 ---
const mode = ref<'partners' | 'activities'>('activities')
const users = ref([])
const activities = ref([]) // 格式: [{activity: Object, joinedCount: number}, ...]
const leaderboard = ref([])
const searchQ = ref('')
const loading = ref(false)

// --- 放大详情页控制 ---
const detailVisible = ref(false)
const currentDetail = ref<any>(null)
const joining = ref(false)

// --- 发起与编辑 ---
const showCreateModal = ref(false)
const creating = ref(false)
const isEditing = ref(false)
const editingId = ref<number | null>(null)
const createForm = reactive({ title: '', description: '', activityTime: '', location: '', maxParticipants: 5 })

// --- 计算属性 ---
const myStats = computed(() => ({
  activities: userStore.userInfo?.activities ?? 0,
  score: userStore.userInfo?.score ?? 5.0,
  ranking: userStore.userInfo?.ranking ?? '99+',
}))

// 判断当前活动是否由我创建
const isCreator = computed(() => {
  if (!currentDetail.value || !userStore.userInfo) return false
  return currentDetail.value.activity.creatorId === userStore.userInfo.id
})

// 判断我是否已在该活动成员列表中
const hasJoined = computed(() => {
  if (!currentDetail.value || !userStore.userInfo) return false
  return currentDetail.value.participants.some((p: any) => p.id === userStore.userInfo.id)
})

const filteredActivities = computed(() => {
  const list = activities.value || []
  if (!searchQ.value) return list
  return list.filter((item: any) => item.activity.title?.toLowerCase().includes(searchQ.value.toLowerCase()))
})

const filteredUsers = computed(() => {
  if (!searchQ.value) return users.value
  return (users.value || []).filter((u: any) => u.nickname?.toLowerCase().includes(searchQ.value.toLowerCase()))
})

const isEmpty = computed(() => {
  return (mode.value === 'partners' && filteredUsers.value.length === 0) ||
         (mode.value === 'activities' && filteredActivities.value.length === 0)
})

// --- 业务方法 ---

const loadData = async () => {
  loading.value = true
  try {
    const [usersRes, lbRes, actRes] = await Promise.all([
      getNearbyUsers({ radius: 10 }),
      getActivityLeaderboard(),
      getActivities({ pageNum: 0, pageSize: 20 })
    ])
    users.value = usersRes || []
    leaderboard.value = lbRes?.data || lbRes || []
    // 重要：后端已更新接口，现在返回包装对象数组
    activities.value = actRes?.content || []
  } catch (e) {
    ElMessage.error('加载列表失败')
  } finally {
    loading.value = false
  }
}

// 放大详情页显示
const showDetail = async (activity: any) => {
  try {
    const res = await getActivityDetail(activity.id)
    currentDetail.value = res // res 是 {activity, participants, participantCount}
    detailVisible.value = true
  } catch (e) {
    ElMessage.error('获取档案失败')
  }
}

const handleJoin = async () => {
  if (!currentDetail.value) return
  joining.value = true
  try {
    await joinActivity(currentDetail.value.activity.id)
    ElMessage.success('成功入伙！')
    // 立即刷新详情
    const res = await getActivityDetail(currentDetail.value.activity.id)
    currentDetail.value = res
    loadData() // 同步刷新广场列表的人数统计
  } catch (e) {} finally { joining.value = false }
}

const goToChat = () => {
  // 一键直通您已有的聊天板块，并传入 activityId 参数
  router.push({ path: '/chat', query: { activityId: currentDetail.value.activity.id } })
}

const openCreateModal = () => {
  isEditing.value = false
  Object.assign(createForm, { title: '', description: '', activityTime: '', location: '', maxParticipants: 5 })
  showCreateModal.value = true
}

const handleEdit = () => {
  isEditing.value = true
  editingId.value = currentDetail.value.activity.id
  const act = currentDetail.value.activity
  Object.assign(createForm, {
    title: act.title,
    description: act.description,
    activityTime: act.activityTime,
    location: act.location,
    maxParticipants: act.maxParticipants
  })
  showCreateModal.value = true
}

const submitActivity = async () => {
  if (!createForm.title || !createForm.activityTime || !createForm.location) {
    return ElMessage.warning('请将活动必填项填写完整')
  }
  creating.value = true
  try {
    if (isEditing.value && editingId.value) {
      await updateActivity(editingId.value, { ...createForm })
      ElMessage.success('活动信息已更新')
    } else {
      await createActivity({ ...createForm })
      ElMessage.success('活动已发布')
    }
    showCreateModal.value = false
    detailVisible.value = false
    loadData()
  } catch (e) {} finally { creating.value = false }
}

const statusText = (s: number) => ({ 0: '招募中', 1: '进行中', 2: '已结束' }[s] || '未知')
const statusTagType = (s: number) => (['success', 'warning', 'info'][s] as any)
const formatDate = (d: string) => new Date(d).toLocaleString('zh-CN', { year:'numeric', month:'2-digit', day:'2-digit', hour:'2-digit', minute:'2-digit' })
const openUserDetail = (user: any) => router.push(`/user/${user.id}`)

onMounted(loadData)
</script>

<style scoped>
.find-partner-view { display: flex; flex: 1; height: 100%; overflow: hidden; }
.fp-main { flex: 1; padding: 24px; display: flex; flex-direction: column; gap: 24px; overflow: hidden; }
.stats-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
.stat-card { background: var(--color-surface-2); border: 1px solid var(--color-border); border-radius: var(--radius-lg); padding: 16px; }
.stat-card-head { font-size: 12px; color: var(--color-text-secondary); margin-bottom: 4px; }
.stat-card-val { font-size: 18px; font-weight: bold; }
.mode-header { display: flex; justify-content: space-between; align-items: center; }
.mode-tabs { display: flex; background: var(--color-surface-1); padding: 4px; border-radius: var(--radius-md); }
.mode-tabs button { padding: 6px 16px; border: none; background: transparent; color: var(--color-text-secondary); cursor: pointer; border-radius: 4px; font-size: 14px; transition: 0.2s; }
.mode-tabs button.active { background: var(--color-surface-2); color: var(--color-white); box-shadow: 0 2px 4px rgba(0,0,0,0.2); }
.action-bar { display: flex; align-items: center; gap: 12px; }
.fp-search-wrap { position: relative; }
.fp-search { background: var(--color-surface-2); border: 1px solid var(--color-border); padding: 8px 12px 8px 32px; border-radius: 20px; color: white; width: 220px; transition: width 0.3s; }
.fp-search:focus { width: 300px; border-color: var(--color-green); }
.fp-search-icon { position: absolute; left: 10px; top: 50%; transform: translateY(-50%); color: var(--color-text-hint); }
.content-scroll { flex: 1; overflow-y: auto; padding-bottom: 20px; }
.partners-grid, .activities-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 20px; }
.leaderboard-panel { width: 300px; border-left: 1px solid var(--color-border); padding: 24px; background: rgba(255,255,255,0.01); }
.lb-title { margin-top: 0; margin-bottom: 20px; font-size: 18px; color: var(--color-green); }
.lb-list { display: flex; flex-direction: column; gap: 12px; }
.lb-item { display: flex; align-items: center; gap: 12px; padding: 12px; background: var(--color-surface-1); border-radius: var(--radius-md); }
.lb-rank { font-weight: bold; color: var(--color-green); width: 24px; }
.lb-ava { width: 36px; height: 36px; border-radius: 50%; }

/* 放大的详情巨幕样式 (核心更新) */
.magnified-activity-dialog :deep(.el-dialog__body) { padding-top: 10px; }
.magnified-header-info { display: flex; align-items: center; gap: 12px; }
.header-archive-no { color: #666; font-size: 11px; text-transform: uppercase; letter-spacing: 1px; }
.act-magnified-title { font-size: 38px; color: #fff; margin: 10px 0 25px 0; font-weight: 800; line-height: 1.1; letter-spacing: -1px; }
.act-specs-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 35px; }
.spec-unit { background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.05); padding: 20px; border-radius: 16px; display: flex; align-items: center; gap: 16px; }
.spec-unit .el-icon { font-size: 32px; color: var(--color-green); }
.unit-text { display: flex; flex-direction: column; }
.unit-label { font-size: 11px; color: #666; text-transform: uppercase; margin-bottom: 4px; }
.unit-val { font-size: 16px; color: #efefef; font-weight: 600; }
.magnified-section { margin-bottom: 35px; }
.section-label-text { font-size: 15px; color: #888; margin-bottom: 15px; font-weight: 600; text-transform: uppercase; }
.magnified-desc-box { background: rgba(0,255,149,0.02); border: 1px dashed rgba(0,255,149,0.2); padding: 25px; border-radius: 16px; color: #bbb; line-height: 1.8; font-size: 16px; border-left: 5px solid var(--color-green); }
.members-title-row { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 12px; }
.members-fraction { font-size: 20px; color: var(--color-green); font-weight: 900; font-family: 'Courier New', Courier, monospace; }
.members-avatar-wall { display: flex; flex-wrap: wrap; gap: 12px; margin-top: 20px; }
.wall-circle-ava { width: 50px; height: 50px; border-radius: 50%; border: 3px solid #1a1a1a; box-shadow: 0 4px 15px rgba(0,0,0,0.4); transition: 0.3s; cursor: pointer; }
.wall-circle-ava:hover { transform: translateY(-8px) scale(1.1); border-color: var(--color-green); z-index: 10; }
.magnified-footer-actions { display: flex; justify-content: space-between; width: 100%; padding: 15px 0 5px; }
.footer-right-group { display: flex; gap: 15px; }
.no-results { text-align: center; color: var(--color-text-hint); padding: 40px; }
</style>
