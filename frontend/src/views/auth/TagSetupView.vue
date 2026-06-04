<template>
  <div class="auth-page">
    <div class="auth-card glass-card">
      <h2 class="auth-title">Choose your interests</h2>
      <p class="auth-sub">
        Pick tags that describe what you love doing. We'll use these to find
        your perfect partner.
      </p>
      <TagSelector v-model="tags" :max="8" />
      <el-button
        type="primary"
        size="large"
        style="
          width: 100%;
          background: var(--color-green);
          border-color: var(--color-green);
          color: #000;
          font-weight: 700;
        "
        :disabled="tags.length === 0"
        :loading="loading"
        @click="onSave"
      >
        Let's go →
      </el-button>
    </div>
  </div>
</template> 
<script setup lang="ts"> 
import { ref } from 'vue' 
import { useRouter } from 'vue-router' 
import { ElMessage } from 'element-plus' 
import TagSelector from '@/components/profile/TagSelector.vue' 
import { updateTags } from '@/api/user' 
import { useUserStore } from '@/stores/user' 
const router = useRouter() 
const userStore = useUserStore() 
const tags = ref<string[]>([]) 
const loading = ref(false) 
const onSave = async () => { loading.value = true 
  try { await updateTags(tags.value) 
    await userStore.fetchUserInfo() 
    ElMessage.success('Tags saved') 
    router.push('/') } 
    finally { loading.value = false } } 
    </script> 
<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 1;
}
.auth-card {
  width: 560px;
  padding: 40px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.auth-title {
  font-family: var(--font-display);
  font-size: 26px;
  text-align: center;
}
.auth-sub {
  font-size: 14px;
  color: var(--color-text-secondary);
  text-align: center;
}
</style>
