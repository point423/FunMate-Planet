// src/composables/useLocation.ts
import { ref } from 'vue'
import { reportLocation } from '@/api/auth'

/** Gets the browser geolocation and reports it to the backend */
export function useLocation() {
  const lat = ref<number | null>(null)
  const lng = ref<number | null>(null)
  const error = ref<string | null>(null)

  const updateLocation = () => {
    if (!navigator.geolocation) {
      error.value = 'Geolocation not supported'
      return
    }
    navigator.geolocation.getCurrentPosition(
      async (pos) => {
        lat.value = pos.coords.latitude
        lng.value = pos.coords.longitude
        await reportLocation({ latitude: lat.value, longitude: lng.value })
      },
      (err) => { error.value = err.message },
    )
  }

  return { lat, lng, error, updateLocation }
}
