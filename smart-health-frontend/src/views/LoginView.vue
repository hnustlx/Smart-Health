<template>
  <main ref="authPage" class="auth-page" @mousemove="trackPointer" @mouseleave="resetPointer">
    <div class="auth-door" aria-hidden="true">
      <div ref="leftDoor" class="auth-door-panel auth-door-panel--left"></div>
      <div ref="rightDoor" class="auth-door-panel auth-door-panel--right"></div>
    </div>
    <div ref="pulseBackdrop" class="pulse-backdrop" aria-hidden="true">
      <svg class="pulse-line" viewBox="0 0 1440 240" preserveAspectRatio="none">
        <path
          ref="pulseLine"
          d="M0 124 H135 L158 124 L176 82 L204 166 L232 104 L260 124 H410 L438 124 L462 72 L492 176 L522 108 L552 124 H730 L758 124 L782 92 L808 154 L838 112 L868 124 H1030 L1056 124 L1080 78 L1112 170 L1142 108 L1172 124 H1440"
        />
      </svg>
      <div ref="pulseGlow" class="pulse-glow"></div>
    </div>

    <div ref="doorSpark" class="door-spark" aria-hidden="true"></div>
    <HealthAssistant ref="assistantRef" :state="assistantState" :pointer="assistantPointer" />

    <section ref="loginCard" class="auth-card login-card">
      <div ref="cardContent" class="auth-card-content">
        <div class="auth-brand-row">
          <img class="auth-logo" :src="qingyaMark" alt="青芽" />
          <p class="eyebrow">青芽健康助手</p>
        </div>
        <h1>
          <span>进入你的</span>
          <span>健康计划中心</span>
        </h1>
        <p class="auth-subtitle">连接健康档案与体重趋势，开启更清晰的每日计划。</p>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="submit">
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="form.username"
              size="large"
              autocomplete="username"
              @focus="assistantState = 'username'"
              @blur="assistantState = 'idle'"
            />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              size="large"
              type="password"
              autocomplete="current-password"
              show-password
              @focus="assistantState = 'password'"
              @blur="assistantState = 'idle'"
            />
          </el-form-item>
          <el-button class="auth-submit" type="primary" size="large" :loading="loading" @click="submit">
            登录
          </el-button>
          <el-button
            v-if="devLoginEnabled"
            class="auth-dev-submit"
            size="large"
            :loading="loading"
            @click="previewLogin"
          >
            体验登录
          </el-button>
        </el-form>
        <p class="auth-status">{{ statusText }}</p>
        <p class="auth-link">还没有账号？<router-link to="/register">去注册</router-link></p>
      </div>
    </section>
  </main>
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { gsap } from 'gsap'
import HealthAssistant from '../components/HealthAssistant.vue'
import { login } from '../api/modules/auth'
import { setToken, setUser } from '../utils/auth'
import qingyaMark from '../assets/qingya-mark.svg'

const router = useRouter()
const route = useRoute()
const authPage = ref()
const formRef = ref()
const loginCard = ref()
const cardContent = ref()
const assistantRef = ref()
const leftDoor = ref()
const rightDoor = ref()
const doorSpark = ref()
const pulseBackdrop = ref()
const pulseLine = ref()
const pulseGlow = ref()
const loading = ref(false)
const statusText = ref('等待登录')
const assistantState = ref('idle')
const assistantPointer = reactive({
  x: 0,
  y: 0
})
const devLoginEnabled = import.meta.env.VITE_ENABLE_DEV_TOOLS === 'true'
let ctx

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

onMounted(() => {
  const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  if (reduceMotion) {
    return
  }

  ctx = gsap.context(() => {
    const pulsePath = pulseLine.value
    const pulseLength = pulsePath.getTotalLength()
    gsap.set(pulsePath, { strokeDasharray: pulseLength, strokeDashoffset: pulseLength })
    gsap.set(loginCard.value, { autoAlpha: 0, y: 24, scale: 0.98 })
    gsap.set(pulseGlow.value, { xPercent: -120, autoAlpha: 0 })

    gsap
      .timeline({ defaults: { ease: 'power3.out' } })
      .to(pulsePath, { strokeDashoffset: 0, duration: 1.45, ease: 'power2.inOut' })
      .to(pulseGlow.value, { autoAlpha: 1, xPercent: 120, duration: 1.35, ease: 'power2.inOut' }, '<0.05')
      .to(loginCard.value, { autoAlpha: 1, y: 0, scale: 1, duration: 0.68 }, '-=0.38')

    gsap.to(pulsePath, {
      strokeDashoffset: -pulseLength,
      duration: 3.8,
      ease: 'none',
      repeat: -1,
      delay: 1.45
    })

    gsap.to(pulseGlow.value, {
      xPercent: 135,
      autoAlpha: 0.95,
      duration: 3.8,
      ease: 'none',
      repeat: -1,
      delay: 1.45
    })
  }, authPage.value)
})

onUnmounted(() => {
  ctx?.revert()
})

function trackPointer(event) {
  const bounds = authPage.value.getBoundingClientRect()
  assistantPointer.x = ((event.clientX - bounds.left) / bounds.width - 0.5) * 2
  assistantPointer.y = ((event.clientY - bounds.top) / bounds.height - 0.5) * 2
}

function resetPointer() {
  assistantPointer.x = 0
  assistantPointer.y = 0
}

async function playSuccessTransition() {
  assistantState.value = 'success'
  await assistantRef.value?.playSuccess()
  const assistantEl = assistantRef.value?.getRootEl()
  await gsap
    .timeline({ defaults: { ease: 'power4.inOut' } })
    .to(pulseBackdrop.value, { autoAlpha: 0, filter: 'blur(10px)', duration: 0.18, ease: 'power2.out' })
    .to(loginCard.value, { y: -16, autoAlpha: 0, scale: 0.96, duration: 0.34, ease: 'power2.in' }, '<0.02')
    .to(assistantEl, { top: '44%', left: '43%', width: '190px', duration: 0.58, ease: 'power3.inOut' }, '-=0.08')
  await assistantRef.value?.playKnock()
  await gsap
    .timeline({ defaults: { ease: 'power4.inOut' } })
    .fromTo(doorSpark.value, { autoAlpha: 0, scale: 0.56 }, { autoAlpha: 1, scale: 1.18, duration: 0.18, ease: 'power2.out' })
    .to(doorSpark.value, { autoAlpha: 0, scale: 1.7, duration: 0.24, ease: 'power2.out' })
    .to(assistantEl, { x: -34, autoAlpha: 0, scale: 0.7, duration: 0.26, ease: 'power2.in' }, '<0.02')
    .to(leftDoor.value, { xPercent: -112, duration: 0.78 }, '<0.04')
    .to(rightDoor.value, { xPercent: 112, duration: 0.78 }, '<')
}

async function submit() {
  await formRef.value.validate()
  loading.value = true
  assistantState.value = 'loading'
  statusText.value = '正在读取健康档案'

  try {
    const user = await login(form)
    setToken(user.token)
    setUser({
      userId: user.userId,
      username: user.username,
      role: user.role,
      status: user.status,
      vipExpireTime: user.vipExpireTime
    })
    statusText.value = '正在准备个性化计划'
    await playSuccessTransition()
    ElMessage.success('登录成功')
    router.push(route.query.redirect || '/home')
  } finally {
    loading.value = false
    assistantState.value = 'idle'
  }
}

async function previewLogin() {
  loading.value = true
  assistantState.value = 'loading'
  statusText.value = '正在进入体验环境'

  try {
    setToken('frontend-dev-token')
    setUser({
      userId: 0,
      username: '体验用户',
      role: 'USER',
      status: 1,
      vipExpireTime: ''
    })
    sessionStorage.removeItem('vip_preview_enabled')
    statusText.value = '正在准备健康工作台'
    await playSuccessTransition()
    router.push(route.query.redirect || '/home')
  } finally {
    loading.value = false
    assistantState.value = 'idle'
  }
}
</script>
