<template>
  <el-container class="app-shell">
    <el-aside class="app-sidebar" width="236px">
      <div class="brand">
        <img class="brand-mark" :src="qingyaMark" alt="青芽" />
        <div>
          <h1>青芽</h1>
          <p>青芽健康助手</p>
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
        <span>会员状态</span>
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
        <el-button class="logout-button" plain @click="logout">退出登录</el-button>
      </el-header>
      <el-main class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="page-fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
    <!-- 移动端底部导航栏 -->
    <nav class="mobile-bottom-nav">
      <router-link to="/home" class="mobile-nav-item" :class="{ active: route.path === '/home' }">
        <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
        <span>首页</span>
      </router-link>
      <router-link to="/profile" class="mobile-nav-item" :class="{ active: route.path === '/profile' }">
        <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>
        <span>档案</span>
      </router-link>
      <router-link to="/plan" class="mobile-nav-item" :class="{ active: route.path === '/plan' }">
        <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
        <span>计划</span>
      </router-link>
      <router-link to="/weight" class="mobile-nav-item" :class="{ active: route.path === '/weight' }">
        <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 2v20l6-4 6 4V2z"/></svg>
        <span>体重</span>
      </router-link>
      <button class="mobile-nav-item mobile-logout" @click="logout">
        <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
        <span>退出</span>
      </button>
    </nav>
  </el-container>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { gsap } from 'gsap'
import { clearAuth, getUser } from '../utils/auth'
import qingyaMark from '../assets/qingya-mark.svg'

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

const pageTitle = computed(() => titles[route.path] || '青芽')
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

<style scoped>
.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.page-fade-enter-from {
  opacity: 0;
  transform: translateY(6px);
}
.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}
</style>
