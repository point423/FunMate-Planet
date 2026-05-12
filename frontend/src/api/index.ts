// src/api/index.ts
import axios from 'axios'
import type { AxiosInstance, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'

const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 600000,
  headers: { 'Content-Type': 'application/json' },
})

service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) config.headers.Authorization = `Bearer ${token}`
    // If sending FormData, let the browser set the Content-Type (including boundary)
    // to avoid adding charset which some servers (Spring) may reject.
    if (config.data instanceof FormData) {
      try {
        // remove common header keys that axios may set in defaults
        if (config.headers) {
          const h = config.headers as unknown as Record<string, unknown>
          Reflect.deleteProperty(h, 'Content-Type')
          Reflect.deleteProperty(h, 'content-type')
          if (h.common && typeof h.common === 'object') {
            Reflect.deleteProperty(h.common as Record<string, unknown>, 'Content-Type')
            Reflect.deleteProperty(h.common as Record<string, unknown>, 'content-type')
          }
          if (h.post && typeof h.post === 'object') {
            Reflect.deleteProperty(h.post as Record<string, unknown>, 'Content-Type')
            Reflect.deleteProperty(h.post as Record<string, unknown>, 'content-type')
          }
        }
      } catch (e) {
        // ignore
      }
    }
    return config
  },
  (error) => Promise.reject(error),
)

service.interceptors.response.use(
  (response: AxiosResponse) => {
    const { code, message, data } = response.data
    if (code === 200 || code === 201 || code === 0) return data
    ElMessage.error(message || '请求失败')
    return Promise.reject(new Error(message || '请求失败'))
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  },
)

export default service
