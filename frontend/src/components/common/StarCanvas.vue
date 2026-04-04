<template>
  <canvas ref="canvas" class="star-canvas" />
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

const canvas = ref<HTMLCanvasElement | null>(null)
let raf = 0

interface Star { x:number; y:number; r:number; o:number; sp:number; ph:number }
let stars: Star[] = []

function init(c: HTMLCanvasElement) {
  c.width  = window.innerWidth
  c.height = window.innerHeight
  stars = Array.from({ length: 260 }, () => ({
    x: Math.random() * c.width,  y: Math.random() * c.height,
    r: Math.random() * 1.2 + .2, o: Math.random() * .6 + .15,
    sp: Math.random() * .003 + .001, ph: Math.random() * Math.PI * 2,
  }))
}

function draw(t: number) {
  const c = canvas.value; if (!c) return
  const ctx = c.getContext('2d')!
  ctx.clearRect(0, 0, c.width, c.height)
  stars.forEach(s => {
    const o = s.o * (.55 + .45 * Math.sin(t * s.sp + s.ph))
    ctx.beginPath(); ctx.arc(s.x, s.y, s.r, 0, Math.PI*2)
    ctx.fillStyle = `rgba(255,255,255,${o})`; ctx.fill()
  })
  raf = requestAnimationFrame(draw)
}

onMounted(() => { if (canvas.value) { init(canvas.value); raf = requestAnimationFrame(draw) } })
onUnmounted(() => cancelAnimationFrame(raf))
window.addEventListener('resize', () => { if (canvas.value) init(canvas.value) })
</script>

<style scoped>
.star-canvas { position: fixed; inset: 0; z-index: 0; pointer-events: none; }
</style>
