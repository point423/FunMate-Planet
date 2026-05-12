// src/stores/activity.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getMyDiaries, getDiaryDetail } from '@/api/activity'
import type { Diary } from '@/types/diary'
import type { Activity } from '@/types/activity'

export const useActivityStore = defineStore('activity', () => {
  const diaries = ref<Diary[]>([])
  const activeDiary = ref<Diary | null>(null)
  const currentActivity = ref<Activity | null>(null)

  const fetchDiaries = async () => {
    const result = await getMyDiaries()
    diaries.value = Array.isArray(result) ? result : []
  }

  const fetchDiaryDetail = async (id: number) => {
    activeDiary.value = await getDiaryDetail(id) as Diary
    return activeDiary.value
  }

  return { diaries, activeDiary, currentActivity, fetchDiaries, fetchDiaryDetail }
})
