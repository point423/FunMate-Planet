<template>
  <div class="calendar">
    <div class="cal-nav">
      <button class="cal-arrow" @click="prevMonth">‹</button>
      <div class="cal-label">
        <select v-model="month" class="cal-select">
          <option v-for="(m, i) in MONTHS" :key="i" :value="i">{{ m }}</option>
        </select>
        <select v-model="year" class="cal-select">
          <option v-for="y in years" :key="y" :value="y">{{ y }}</option>
        </select>
      </div>
      <button class="cal-arrow" @click="nextMonth">›</button>
    </div>
    <div class="cal-grid">
      <div v-for="d in DAY_HDRS" :key="d" class="cal-hdr">{{ d }}</div>
      <div
        v-for="(cell, i) in cells"
        :key="i"
        class="cal-cell"
        :class="{
          'other-month': !cell.current,
          today: cell.isToday,
          'has-event': cell.hasEvent,
        }"
      >{{ cell.day }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const props = defineProps<{ eventDays?: number[] }>()

const now   = new Date()
const year  = ref(now.getFullYear())
const month = ref(now.getMonth())

const MONTHS   = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec']
const DAY_HDRS = ['Su','Mo','Tu','We','Th','Fr','Sa']
const years    = Array.from({ length: 5 }, (_, i) => now.getFullYear() - 2 + i)

const prevMonth = () => { if (month.value === 0) { month.value = 11; year.value-- } else month.value-- }
const nextMonth = () => { if (month.value === 11) { month.value = 0; year.value++ } else month.value++ }

const cells = computed(() => {
  const first    = new Date(year.value, month.value, 1).getDay()
  const daysInM  = new Date(year.value, month.value + 1, 0).getDate()
  const daysInPM = new Date(year.value, month.value, 0).getDate()
  const result = []
  for (let i = 0; i < first; i++)
    result.push({ day: daysInPM - first + i + 1, current: false, isToday: false, hasEvent: false })
  for (let d = 1; d <= daysInM; d++)
    result.push({
      day: d, current: true,
      isToday: d === now.getDate() && month.value === now.getMonth() && year.value === now.getFullYear(),
      hasEvent: props.eventDays?.includes(d) ?? false,
    })
  return result
})
</script>

<style scoped>
.calendar { padding: 14px; }
.cal-nav  { display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px; }
.cal-arrow { background: none; border: none; color: var(--color-white); font-size: 18px; cursor: pointer; padding: 2px 6px; }
.cal-label { display: flex; gap: 6px; }
.cal-select {
  background: #f5f5f5; border: 0.5px solid var(--color-border);
  color: #000; border-radius: 6px; padding: 4px 8px; font-size: 12px;
}
.cal-select option {
  color: #000;
}
.cal-grid  { display: grid; grid-template-columns: repeat(7,1fr); gap: 2px; }
.cal-hdr   { text-align: center; font-size: 10px; color: var(--color-text-secondary); padding: 4px 0; }
.cal-cell  {
  text-align: center; font-size: 12px; padding: 5px 2px;
  border-radius: 6px; cursor: pointer; color: var(--color-text-secondary);
  transition: background .15s;
}
.cal-cell:hover   { background: var(--color-surface-2); color: var(--color-white); }
.cal-cell.today   { background: var(--color-green-dim); color: var(--color-green); font-weight: 700; }
.cal-cell.has-event { background: var(--color-surface-3); color: var(--color-white); }
.cal-cell.other-month { opacity: .3; }
</style>
