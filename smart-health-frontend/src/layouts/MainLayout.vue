<template>
  <el-container class="app-shell">
    <el-aside class="app-sidebar" width="236px">
      <div class="brand">
        <div class="brand-mark">SH</div>
        <div>
          <h1>Smart Health</h1>
          <p>智能健康助手</p>
        </div>
      </div>

      <div class="sidebar-pulse" aria-hidden="true">
        <svg viewBox="0 0 188 44" preserveAspectRatio="none">
          <path ref="sidebarPulsePath" d="M2 24 H38 L48 24 L56 10 L68 36 L80 20 L92 24 H134 L144 24 L152 14 L164 32 L176 24 H186" />
        </svg>
      </div>

      <el-menu router :default-active="route.path" class="side-menu">
        <el-menu-item index="/home"><span class="nav-dot"></span>首页</el-menu-item>
        <el-menu-item index="/profile"><span class="nav-dot"></span>健康档案</el-menu-item>
        <el-menu-item index="/weight"><span class="nav-dot"></span>体重记录</el-menu-item>
        <el-menu-item index="/plan"><span class="nav-dot"></span>AI 计划</el-menu-item>
        <el-menu-item index="/history"><span class="nav-dot"></span>历史计划</el-menu-item>
        <el-menu-item v-if="user?.role === 'VIP'" index="/chat"><span class="nav-dot"></span>智能问答</el-menu-item>
        <el-menu-item index="/ai-config"><span class="nav-dot"></span>AI 配置</el-menu-item>
        <el-menu-item index="/vip"><span class="nav-dot"></span>升级 VIP</el-menu-item>
      </el-menu>

      <div class="sidebar-status">
        <span>MEMBERSHIP</span>
        <strong>{{ roleLabel }}</strong>
        <small>{{ user?.role === 'VIP' ? '已解锁进阶健康能力' : '升级后解锁更多能力' }}</small>
      </div>
    </el-aside>

    <el-container>
      <el-header class="app-header">
        <div>
          <strong>{{ pageTitle }}</strong>
          <span>{{ roleLabel }}</span>
        </div>
        <el-button type="primary" plain @click="logout">退出登录</el-button>
      </el-header>
      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { gsap } from 'gsap'
import { clearAuth, getUser } from '../utils/auth'

const route = useRoute()
const router = useRouter()
const user = ref(getUser())
const sidebarPulsePath = ref()
let sidebarPulseTween
let sidebarPulseGlowTween

const titles = {
  '/home': '首页',
  '/profile': '健康档案',
  '/weight': '体重记录',
  '/plan': 'AI 计划',
  '/history': '历史计划',
  '/chat': '智能问答',
  '/ai-config': 'AI 配置',
  '/vip': '升级 VIP'
}

const roleNames = {
  USER: '普通用户',
  VIP: 'VIP 用户',
  ADMIN: '管理员'
}

const pageTitle = computed(() => titles[route.path] || 'Smart Health')
const roleLabel = computed(() => roleNames[user.value?.role] || '未识别角色')

function logout() {
  clearAuth()
  router.push('/login')
}

function refreshUser() {
  user.value = getUser()
}

onMounted(() => {
  window.addEventListener('smart-health-user-updated', refreshUser)
  const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  const path = sidebarPulsePath.value
  if (!path || reduceMotion) {
    return
  }

  const length = path.getTotalLength()
  gsap.set(path, { strokeDasharray: length, strokeDashoffset: length })

  gsap
    .timeline()
    .to(path, { strokeDashoffset: 0, duration: 0.9, ease: 'power2.out' })
    .add(() => {
      sidebarPulseTween = gsap.to(path, {
        strokeDashoffset: -length,
        duration: 2.6,
        ease: 'none',
        repeat: -1
      })
      sidebarPulseGlowTween = gsap.to(path, {
        opacity: 0.62,
        duration: 0.9,
        ease: 'sine.inOut',
        repeat: -1,
        yoyo: true
      })
    })
})

onBeforeUnmount(() => {
  window.removeEventListener('smart-health-user-updated', refreshUser)
  sidebarPulseTween?.kill()
  sidebarPulseGlowTween?.kill()
})
</script>
