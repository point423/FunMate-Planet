// src/stores/activity.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getMyDiaries, getDiaryDetail } from '@/api/activity'
import { deleteDiary as deleteDiaryApi } from '@/api/activity'
import type { Diary } from '@/types/diary'
import type { Activity } from '@/types/activity'

export const useActivityStore = defineStore('activity', () => {
  const diaries = ref<Diary[]>([])
  const activeDiary = ref<Diary | null>(null)
  const currentActivity = ref<Activity | null>(null)

  const fetchDiaries = async () => {
    const result = await getMyDiaries() as any
    // 后端返回的是分页对象，数据在 content 字段中
    if (result && Array.isArray(result.content)) {
      diaries.value = result.content
    } else if (Array.isArray(result)) {
      diaries.value = result
    } else {
      diaries.value = []
    }
  }

  const fetchDiaryDetail = async (id: number) => {
    activeDiary.value = await getDiaryDetail(id) as Diary
    return activeDiary.value
  }
  
  const deleteDiary = async (id: number) => {
    await deleteDiaryApi(id)
  }

  return { diaries, activeDiary, currentActivity, fetchDiaries, fetchDiaryDetail, deleteDiary }
})
