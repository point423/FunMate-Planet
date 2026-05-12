<template>
  <div class="tag-selector">
    <div class="tag-grid">
      <button
        v-for="tag in ALL_TAGS"
        :key="tag.value"
        class="tag-chip"
        type="button"
        :class="{ active: selected.includes(tag.value) }"
        @click="toggle(tag.value)"
      >
        <span>{{ tag.emoji }}</span>{{ tag.label }}
      </button>
    </div>
    <p class="tag-hint">Selected: {{ selected.length }} / {{ max }}</p>
  </div>
</template>

<script setup lang="ts">
import { defineProps, defineEmits, computed } from 'vue'
import { TAG_META_LIST } from '@/utils/tags'

const props = defineProps<{ modelValue?: string[]; max?: number }>()
const emit  = defineEmits<{ (e: 'update:modelValue', v: string[]): void }>()

const max = props.max ?? 8

// computed getter/setter so we always operate on current value and emit updates
const selected = computed<string[]>({
  get: () => props.modelValue ?? [],
  set: (v: string[]) => emit('update:modelValue', v),
})

const ALL_TAGS = TAG_META_LIST

const toggle = (val: string) => {
  const curr = selected.value
  const next = curr.includes(val)
    ? curr.filter(v => v !== val)
    : curr.length < max ? [...curr, val] : curr
  selected.value = next
}
</script>

<style scoped>
.tag-selector { display: flex; flex-direction: column; gap: 16px; }
.tag-grid { display: flex; flex-wrap: wrap; gap: 10px; justify-content: center; }
.tag-hint  { text-align: center; font-size: 12px; color: var(--color-text-secondary); }

.tag-chip {
  display: inline-flex; align-items: center; gap: 8px;
  padding: 6px 10px; border-radius: 9999px;
  border: 1px solid var(--color-border-dim);
  background: transparent; color: var(--color-text);
  font-size: 13px; cursor: pointer; transition: all 0.12s;
}
.tag-chip span { line-height: 1; }
.tag-chip:hover { transform: translateY(-1px); opacity: 0.95; }

.tag-chip.active {
  background: var(--color-green);
  color: #111;
  border-color: var(--color-green);
}
</style>
