<template>
  <div class="app-root">
    <!-- Animated star background, shown on all pages -->
    <canvas ref="starsCanvas" class="stars-canvas" />

    <!-- Main navbar (hidden on auth/blank layout pages) -->
    <AppNavbar v-if="showNavbar" />

    <!-- Page content -->
    <RouterView />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import AppNavbar from '@/components/common/AppNavbar.vue'

const route  = useRoute()
const starsCanvas = ref<HTMLCanvasElement | null>(null)

const showNavbar = computed(() => route.meta.layout !== 'blank')

// ── Star animation ──────────────────────────────────────────────
let raf = 0
interface Star { x: number; y: number; r: number; o: number; sp: number; ph: number }
let stars: Star[] = []

function initStars(canvas: HTMLCanvasElement) {
  canvas.width  = window.innerWidth
  canvas.height = window.innerHeight
  stars = Array.from({ length: 280 }, () => ({
    x:  Math.random() * canvas.width,
    y:  Math.random() * canvas.height,
    r:  Math.random() * 1.2 + 0.2,
    o:  Math.random() * 0.65 + 0.15,
    sp: Math.random() * 0.003 + 0.001,
    ph: Math.random() * Math.PI * 2,
  }))
}

function drawStars(t: number) {
  const canvas = starsCanvas.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')!
  ctx.clearRect(0, 0, canvas.width, canvas.height)
  stars.forEach(s => {
    const o = s.o * (0.55 + 0.45 * Math.sin(t * s.sp + s.ph))
    ctx.beginPath()
    ctx.arc(s.x, s.y, s.r, 0, Math.PI * 2)
    ctx.fillStyle = `rgba(255,255,255,${o})`
    ctx.fill()
  })
  raf = requestAnimationFrame(drawStars)
}

function onResize() {
  if (starsCanvas.value) initStars(starsCanvas.value)
}

onMounted(() => {
  if (starsCanvas.value) {
    initStars(starsCanvas.value)
    raf = requestAnimationFrame(drawStars)
  }
  window.addEventListener('resize', onResize)
})

onUnmounted(() => {
  cancelAnimationFrame(raf)
  window.removeEventListener('resize', onResize)
})
</script>

<style scoped>
.app-root {
  position: relative;
  min-height: 100vh;
  background: radial-gradient(ellipse at 50% 0%, #0a1628 0%, #000 60%);
}
.stars-canvas {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
}
</style>
