<template>
  <div ref="rootRef" class="health-assistant" aria-hidden="true">
    <svg class="health-assistant__svg" viewBox="0 0 620 560" role="img">
      <defs>
        <filter id="assistantShadow" x="-35%" y="-35%" width="170%" height="170%">
          <feDropShadow dx="0" dy="24" stdDeviation="24" flood-color="#064e3b" flood-opacity="0.18" />
          <feDropShadow dx="0" dy="7" stdDeviation="8" flood-color="#10b981" flood-opacity="0.1" />
        </filter>
        <linearGradient id="assistantLeaf" x1="0" y1="0" x2="1" y2="1">
          <stop stop-color="#bbf7d0" />
          <stop offset="0.42" stop-color="#4ade80" />
          <stop offset="1" stop-color="#059669" />
        </linearGradient>
        <linearGradient id="assistantRing" x1="0" y1="0" x2="1" y2="1">
          <stop stop-color="#5ee4a4" />
          <stop offset="0.5" stop-color="#13b981" />
          <stop offset="1" stop-color="#047857" />
        </linearGradient>
        <radialGradient id="assistantFace" cx="42%" cy="30%" r="70%">
          <stop stop-color="#ffffff" />
          <stop offset="0.72" stop-color="#fffdf7" />
          <stop offset="1" stop-color="#f8f0df" />
        </radialGradient>
        <radialGradient id="assistantEye" cx="38%" cy="32%" r="68%">
          <stop stop-color="#50665f" />
          <stop offset="0.36" stop-color="#20352f" />
          <stop offset="1" stop-color="#101c19" />
        </radialGradient>
        <radialGradient id="assistantBlush" cx="50%" cy="50%" r="50%">
          <stop stop-color="#fb7185" stop-opacity="0.72" />
          <stop offset="1" stop-color="#fb7185" stop-opacity="0" />
        </radialGradient>
        <linearGradient id="assistantHand" x1="0" y1="0" x2="1" y2="1">
          <stop stop-color="#e6fff0" />
          <stop offset="0.7" stop-color="#bbf7d0" />
          <stop offset="1" stop-color="#86efac" />
        </linearGradient>
      </defs>

      <g ref="bodyRef" filter="url(#assistantShadow)">
        <ellipse cx="310" cy="494" rx="156" ry="48" fill="#064e3b" opacity="0.1" />
        <g ref="leafRef">
          <path
            d="M310 54 C340 0 417 -8 466 32 C423 80 372 98 318 145 C260 98 210 82 164 34 C212 -8 282 0 310 54Z"
            fill="url(#assistantLeaf)"
          />
          <path d="M318 145 C318 114 316 82 310 54" fill="none" stroke="#ecfdf5" stroke-width="8" stroke-linecap="round" opacity="0.55" />
          <path d="M284 94 C252 70 220 52 184 38" fill="none" stroke="#ecfdf5" stroke-width="8" stroke-linecap="round" opacity="0.45" />
          <path d="M348 94 C384 68 414 50 444 36" fill="none" stroke="#ecfdf5" stroke-width="8" stroke-linecap="round" opacity="0.45" />
        </g>

        <path
          d="M136 230 C166 132 244 86 310 96 C382 86 456 136 482 230 C430 194 366 180 310 186 C248 180 188 196 136 230Z"
          fill="#0f9f74"
          opacity="0.34"
        />
        <circle cx="310" cy="310" r="206" fill="url(#assistantRing)" />
        <circle cx="310" cy="324" r="156" fill="url(#assistantFace)" />
        <path
          d="M190 252 C218 192 274 172 310 176 C352 172 410 196 432 252"
          fill="none"
          stroke="#e7fff1"
          stroke-width="16"
          stroke-linecap="round"
          opacity="0.34"
        />

        <g ref="eyesRef">
          <g :style="eyeStyle">
            <ellipse cx="250" cy="324" rx="27" ry="31" fill="url(#assistantEye)" />
            <ellipse cx="370" cy="324" rx="27" ry="31" fill="url(#assistantEye)" />
            <circle cx="241" cy="310" r="8" fill="#ffffff" />
            <circle cx="255" cy="330" r="5" fill="#9ca3af" opacity="0.7" />
            <circle cx="361" cy="310" r="8" fill="#ffffff" />
            <circle cx="375" cy="330" r="5" fill="#9ca3af" opacity="0.7" />
          </g>
          <path d="M238 278 C260 266 280 268 296 280" fill="none" stroke="#047857" stroke-width="9" stroke-linecap="round" opacity="0.24" />
          <path d="M324 280 C340 268 360 266 382 278" fill="none" stroke="#047857" stroke-width="9" stroke-linecap="round" opacity="0.24" />
        </g>

        <ellipse cx="212" cy="382" rx="42" ry="31" fill="url(#assistantBlush)" />
        <ellipse cx="408" cy="382" rx="42" ry="31" fill="url(#assistantBlush)" />
        <path ref="mouthRef" d="M274 408 C292 426 328 426 346 408" fill="none" stroke="#17352d" stroke-width="11" stroke-linecap="round" />

        <g ref="leftHandRef">
          <circle cx="144" cy="362" r="46" fill="url(#assistantHand)" stroke="#10b981" stroke-width="12" />
          <path d="M128 344 C142 336 158 336 172 344" fill="none" stroke="#047857" stroke-width="6" stroke-linecap="round" opacity="0.22" />
        </g>
        <g ref="rightHandRef" class="assistant-knock-hand">
          <circle cx="476" cy="362" r="46" fill="url(#assistantHand)" stroke="#10b981" stroke-width="12" />
          <path d="M460 344 C474 336 490 336 504 344" fill="none" stroke="#047857" stroke-width="6" stroke-linecap="round" opacity="0.22" />
        </g>

        <circle cx="430" cy="198" r="9" fill="#ffffff" opacity="0.72" />
        <circle cx="456" cy="224" r="5" fill="#ffffff" opacity="0.72" />
        <circle cx="196" cy="210" r="6" fill="#ffffff" opacity="0.66" />
      </g>
    </svg>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { gsap } from 'gsap'

const props = defineProps({
  state: {
    type: String,
    default: 'idle'
  },
  pointer: {
    type: Object,
    default: () => ({ x: 0, y: 0 })
  }
})

const rootRef = ref()
const bodyRef = ref()
const leafRef = ref()
const eyesRef = ref()
const mouthRef = ref()
const leftHandRef = ref()
const rightHandRef = ref()

let ctx
let reduceMotion = false

const eyeStyle = computed(() => {
  if (props.state === 'password') {
    return { transform: 'translate(0px, 8px) scaleY(0.16)', transformOrigin: '310px 324px' }
  }

  const focusOffset = props.state === 'username' ? 7 : 0
  const loadingOffset = props.state === 'loading' ? 3 : 0
  const x = Math.max(-1, Math.min(1, props.pointer.x || 0)) * 8
  const y = Math.max(-1, Math.min(1, props.pointer.y || 0)) * 6 + focusOffset + loadingOffset

  return {
    transform: `translate(${x}px, ${y}px)`,
    transformOrigin: '310px 324px'
  }
})

watch(
  () => props.state,
  (state) => {
    if (reduceMotion) {
      return
    }

    if (state === 'password') {
      gsap.to(leftHandRef.value, { x: 100, y: -42, rotation: -12, transformOrigin: '50% 50%', duration: 0.34, ease: 'back.out(1.8)', overwrite: 'auto' })
      gsap.to(rightHandRef.value, { x: -100, y: -42, rotation: 12, transformOrigin: '50% 50%', duration: 0.34, ease: 'back.out(1.8)', overwrite: 'auto' })
      gsap.to(mouthRef.value, { scaleX: 0.72, y: 6, transformOrigin: '50% 50%', duration: 0.24, overwrite: 'auto' })
      return
    }

    gsap.to([leftHandRef.value, rightHandRef.value], { x: 0, y: 0, rotation: 0, duration: 0.38, ease: 'power3.out', overwrite: 'auto' })
    gsap.to(mouthRef.value, { scaleX: 1, y: 0, duration: 0.24, overwrite: 'auto' })
  }
)

onMounted(() => {
  reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  if (reduceMotion) {
    return
  }

  ctx = gsap.context(() => {
    gsap.set(rootRef.value, { xPercent: -50 })
    gsap.from(rootRef.value, { autoAlpha: 0, y: 18, scale: 0.92, duration: 0.72, ease: 'back.out(1.7)' })
    gsap.to(bodyRef.value, { y: -8, duration: 2.4, ease: 'sine.inOut', repeat: -1, yoyo: true })
    gsap.to(leafRef.value, { rotation: 3, svgOrigin: '310 310', duration: 2.2, ease: 'sine.inOut', repeat: -1, yoyo: true })
  }, rootRef.value)
})

onUnmounted(() => {
  ctx?.revert()
})

async function playSuccess() {
  if (reduceMotion) {
    return
  }

  await gsap
    .timeline()
    .to(bodyRef.value, { y: -28, scale: 1.04, duration: 0.2, ease: 'power2.out', overwrite: 'auto' })
    .to(leafRef.value, { rotation: -8, duration: 0.16, ease: 'power2.out' }, '<')
    .to(bodyRef.value, { y: 0, scale: 1, duration: 0.36, ease: 'bounce.out' })
    .to(leafRef.value, { rotation: 0, duration: 0.24, ease: 'power2.out' }, '<0.06')
}

async function playKnock() {
  if (reduceMotion) {
    return
  }

  await gsap
    .timeline()
    .to(rootRef.value, { rotationY: 180, scale: 0.76, transformPerspective: 700, duration: 0.3, ease: 'power2.inOut', overwrite: 'auto' })
    .to(bodyRef.value, { x: 10, rotation: -4, svgOrigin: '310 310', duration: 0.18, ease: 'power2.out' })
    .to(rightHandRef.value, { x: -74, y: -34, rotation: 22, transformOrigin: '50% 50%', duration: 0.16, ease: 'power2.out' }, '<')
    .to(rightHandRef.value, { x: -48, y: -18, rotation: 4, duration: 0.13, ease: 'power1.in' })
    .to(rightHandRef.value, { x: -78, y: -36, rotation: 24, duration: 0.14, ease: 'power2.out' })
    .to(rightHandRef.value, { x: -48, y: -18, rotation: 4, duration: 0.13, ease: 'power1.in' })
    .to(bodyRef.value, { x: 0, rotation: 0, duration: 0.2, ease: 'power2.out' }, '<0.02')
}

function getRootEl() {
  return rootRef.value
}

defineExpose({ playSuccess, playKnock, getRootEl })
</script>
