<template>
  <div class="auth-page">
    <div class="auth-card glass-card">
      <div class="auth-logo">
        <span class="auth-logo-icon">✦</span>
        <span class="auth-logo-text">Fun Mate Planet</span>
      </div>
      <h2 class="auth-title">Create account</h2>
      <el-form :model="form" @submit.prevent="onSubmit">
        <el-form-item>
          <el-input v-model="form.nickname" placeholder="Nickname" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.username" placeholder="Username" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="Password" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.confirmPassword" type="password" placeholder="Confirm password" size="large" show-password />
        </el-form-item>
        <el-button
          type="primary" size="large"
          style="width:100%;background:var(--color-green);border-color:var(--color-green);color:#000;font-weight:700;"
          :loading="loading" native-type="submit"
        >
          Sign up
        </el-button>
      </el-form>
      <p class="auth-switch">
        Already have an account?
        <router-link to="/login" class="auth-link">Log in</router-link>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useAuth } from '@/composables/useAuth'
import { ElMessage } from 'element-plus'

const { register } = useAuth()
const loading = ref(false)
const form    = reactive({ nickname: '', username: '', password: '', confirmPassword: '' })

const onSubmit = async () => {
  if (form.password !== form.confirmPassword) { ElMessage.error('Passwords do not match'); return }
  loading.value = true
  try { await register(form) } catch { /* */ } finally { loading.value = false }
}
</script>

<style scoped>
.auth-page { min-height:100vh; display:flex; align-items:center; justify-content:center; position:relative; z-index:1; }
.auth-card { width:420px; padding:40px; display:flex; flex-direction:column; gap:20px; }
.auth-logo { display:flex; align-items:center; justify-content:center; gap:10px; font-family:var(--font-display); font-size:20px; }
.auth-logo-icon { width:36px; height:36px; border-radius:50%; background:linear-gradient(135deg,#E8A87C,#95FF8D); display:flex; align-items:center; justify-content:center; font-size:16px; }
.auth-title  { font-family:var(--font-display); font-size:26px; text-align:center; }
.auth-switch { text-align:center; font-size:13px; color:var(--color-text-secondary); }
.auth-link   { color:var(--color-green); text-decoration:none; }
</style>
