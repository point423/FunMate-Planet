// src/composables/useLocation.ts
import { ref } from 'vue'
import { reportLocation } from '@/api/auth'

/** Gets the browser geolocation and reports it to the backend */
export function useLocation() {
  const lat = ref<number | null>(null)
  const lng = ref<number | null>(null)
  const error = ref<string | null>(null)

  const updateLocation = () => new Promise<void>((resolve) => {
    if (!navigator.geolocation) {
      error.value = 'Geolocation not supported'
      resolve()
      return
    }
    navigator.geolocation.getCurrentPosition(
      async (pos) => {
        lat.value = pos.coords.latitude
        lng.value = pos.coords.longitude

        // 保存到 localStorage，供其他 API 使用
        localStorage.setItem('user_latitude', lat.value.toString())
        localStorage.setItem('user_longitude', lng.value.toString())

        await reportLocation({ latitude: lat.value, longitude: lng.value })
        resolve()
      },
      (err) => {
        error.value = err.message
        resolve()
      },
    )
  })

  return { lat, lng, error, updateLocation }
}
