// src/composables/useApi.ts
import { ref } from 'vue'

/** Generic async data-fetching composable */
export function useApi<T>(fn: (...args: unknown[]) => Promise<T>) {
  const data = ref<T | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  const execute = async (...args: unknown[]) => {
    loading.value = true
    error.value = null
    try {
      data.value = await fn(...args)
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Unknown error'
    } finally {
      loading.value = false
    }
  }

  return { data, loading, error, execute }
}
