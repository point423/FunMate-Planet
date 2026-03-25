<template>
  <div class="tag-selector">
    <div class="tag-grid">
      <button
        v-for="tag in ALL_TAGS"
        :key="tag.value"
        class="tag-chip"
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
import { defineProps, defineEmits } from 'vue'

const props = defineProps<{ modelValue: string[]; max?: number }>()
const emit  = defineEmits<{ (e: 'update:modelValue', v: string[]): void }>()

const max = props.max ?? 8
const selected = props.modelValue

const ALL_TAGS = [
  { value: 'read',    emoji: '📚', label: 'read'    },
  { value: 'climb',   emoji: '⛰',  label: 'climb'   },
  { value: 'cycle',   emoji: '🚲', label: 'cycle'   },
  { value: 'photo',   emoji: '📷', label: 'photo'   },
  { value: 'draw',    emoji: '🎨', label: 'draw'    },
  { value: 'music',   emoji: '🎵', label: 'music'   },
  { value: 'shop',    emoji: '🛍', label: 'shop'    },
  { value: 'cook',    emoji: '🍳', label: 'cook'    },
  { value: 'plant',   emoji: '🌿', label: 'plant'   },
  { value: 'journal', emoji: '📒', label: 'journal' },
  { value: 'travel',  emoji: '✈️', label: 'travel'  },
  { value: 'kungfu',  emoji: '🥋', label: 'Kung Fu' },
]

const toggle = (val: string) => {
  const next = selected.includes(val)
    ? selected.filter(v => v !== val)
    : selected.length < max ? [...selected, val] : selected
  emit('update:modelValue', next)
}
</script>

<style scoped>
.tag-selector { display: flex; flex-direction: column; gap: 16px; }
.tag-grid { display: flex; flex-wrap: wrap; gap: 10px; justify-content: center; }
.tag-hint  { text-align: center; font-size: 12px; color: var(--color-text-secondary); }
</style>
