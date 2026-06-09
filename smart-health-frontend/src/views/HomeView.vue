<template>
  <section ref="homeRoot" class="home-page">
    <div class="home-hero home-animate">
      <div class="home-hero-copy">
        <p class="home-kicker">今日健康概览</p>
        <h1>欢迎回来，{{ displayName }}</h1>
        <p>完善健康档案并持续记录体重，系统会为你生成更贴合目标的饮食与运动计划。</p>
        <div class="home-actions">
          <router-link class="home-primary-action" to="/plan">生成 AI 计划</router-link>
          <router-link class="home-secondary-action" to="/vip">{{ isVip ? '查看 VIP' : '升级 VIP' }}</router-link>
        </div>
      </div>

      <div class="home-hero-panel">
        <div>
          <span>今日 AI 计划</span>
          <strong>{{ planCountText }}</strong>
        </div>
        <small>{{ planLimitText }}</small>
      </div>
    </div>

    <div class="home-summary">
      <div class="summary-card home-animate">
        <span>健康档案</span>
        <strong>{{ profileStatus }}</strong>
        <small>{{ profileSummary }}</small>
      </div>
      <div class="summary-card home-animate">
        <span>体重记录</span>
        <strong>{{ weightStatus }}</strong>
        <small>{{ weightSummary }}</small>
      </div>
      <div class="summary-card home-animate">
        <span>VIP 问答</span>
        <strong>{{ vipStatus }}</strong>
        <small>{{ vipSummary }}</small>
      </div>
    </div>

    <div class="home-grid">
      <section class="trend-panel home-animate">
        <div class="panel-heading">
          <div>
            <p>体重趋势</p>
            <h2>{{ trendTitle }}</h2>
          </div>
          <span>近 {{ recentWeights.length }} 次</span>
        </div>
        <svg viewBox="0 0 640 240" role="img" aria-label="体重趋势预览图">
          <path class="trend-area" :d="trendAreaPath" />
          <path ref="trendPath" class="trend-path" :d="trendPathD" />
          <g class="trend-dots">
            <circle v-for="point in trendPoints" :key="point.id" :cx="point.x" :cy="point.y" r="5" />
          </g>
        </svg>
      </section>

      <section class="today-panel home-animate">
        <div class="panel-heading">
          <div>
            <p>今日动作</p>
            <h2>建议优先完成</h2>
          </div>
        </div>
        <ul class="task-list">
          <li v-for="task in tasks" :key="task.title">
            <span></span>
            <div>
              <strong>{{ task.title }}</strong>
              <small>{{ task.detail }}</small>
            </div>
          </li>
        </ul>
      </section>
    </div>

  </section>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { gsap } from 'gsap'
import { getCurrentUser } from '../api/modules/auth'
import { getGenerateCount, getPlanHistory } from '../api/modules/plan'
import { getProfile } from '../api/modules/profile'
import { getWeightHistory } from '../api/modules/weight'
import { getUser, setUser } from '../utils/auth'

const currentUser = ref(getUser())
const profile = ref()
const weights = ref([])
const planCount = ref()
const plans = ref([])
const homeRoot = ref()
const trendPath = ref()
let ctx
let reduceMotion = false

const isVip = computed(() => currentUser.value?.role === 'VIP')
const displayName = computed(() => {
  const username = currentUser.value?.username
  return username === 'frontend_dev' ? '用户' : username || '用户'
})
const planCountText = computed(() => {
  if (!planCount.value) {
    return isVip.value ? '5 次' : '2 次'
  }
  return `${planCount.value.remainingCount} 次`
})
const planLimitText = computed(() => {
  if (!planCount.value) {
    return `${isVip.value ? 'VIP 用户' : '普通用户'}每日可生成 ${isVip.value ? 5 : 2} 次`
  }
  return `${isVip.value ? 'VIP 用户' : '普通用户'}今日已用 ${planCount.value.usedCount}/${planCount.value.limitCount}`
})
const profileStatus = computed(() => (profile.value ? '已同步' : '待同步'))
const profileSummary = computed(() => {
  if (!profile.value) {
    return '年龄、身高、目标、饮食偏好'
  }
  return `${profile.value.age} 岁 / ${profile.value.height}cm / ${profile.value.goal || '目标待完善'}`
})
const recentWeights = computed(() => weights.value.slice(-7))
const weightStatus = computed(() => (recentWeights.value.length ? `${recentWeights.value.at(-1).weight} kg` : '待记录'))
const weightSummary = computed(() => {
  if (recentWeights.value.length < 2) {
    return recentWeights.value.length ? '继续记录后可观察变化趋势' : '记录体重后展示变化趋势'
  }
  const first = Number(recentWeights.value[0].weight)
  const latest = Number(recentWeights.value.at(-1).weight)
  const change = Number((latest - first).toFixed(1))
  return `近 ${recentWeights.value.length} 次变化 ${change > 0 ? '+' : ''}${change} kg`
})
const vipStatus = computed(() => (isVip.value ? '已开通' : '未开通'))
const vipSummary = computed(() => (isVip.value ? '已解锁智能问答与 VIP 详细计划' : '升级后解锁自由健康问答'))
const trendTitle = computed(() => {
  if (recentWeights.value.length < 2) {
    return '最近变化趋势'
  }
  return Number(recentWeights.value.at(-1).weight) <= Number(recentWeights.value[0].weight) ? '体重稳步下降' : '体重小幅波动'
})
const trendPoints = computed(() => {
  if (recentWeights.value.length === 0) {
    return [
      { id: 'empty-1', x: 24, y: 182 },
      { id: 'empty-2', x: 174, y: 134 },
      { id: 'empty-3', x: 326, y: 92 },
      { id: 'empty-4', x: 478, y: 72 },
      { id: 'empty-5', x: 616, y: 48 }
    ]
  }

  const values = recentWeights.value.map((item) => Number(item.weight))
  const min = Math.min(...values)
  const max = Math.max(...values)
  const padding = Math.max((max - min) * 0.18, 0.4)
  const chartMin = min - padding
  const chartMax = max + padding
  const range = chartMax - chartMin || 1

  return recentWeights.value.map((item, index) => ({
    id: item.id,
    x: recentWeights.value.length === 1 ? 320 : 24 + (index * 592) / (recentWeights.value.length - 1),
    y: 198 - ((Number(item.weight) - chartMin) / range) * 150
  }))
})
const trendPathD = computed(() => `M${trendPoints.value.map((point) => `${point.x} ${point.y}`).join(' L')}`)
const trendAreaPath = computed(() => `${trendPathD.value} V220 H24 Z`)
const latestPlan = computed(() => plans.value[0])
const tasks = computed(() => [
  {
    title: profile.value ? '健康档案已完善' : '完善健康档案',
    detail: profile.value ? `${profile.value.dietPreference || '饮食偏好已记录'}，目标为${profile.value.goal || '健康管理'}` : '年龄、身高、目标会影响计划质量'
  },
  {
    title: recentWeights.value.length ? '查看体重趋势' : '记录今日体重',
    detail: recentWeights.value.length ? weightSummary.value : '连续记录后可观察变化方向'
  },
  {
    title: latestPlan.value ? '查看最新 AI 计划' : '生成 AI 健康计划',
    detail: latestPlan.value ? `${latestPlan.value.planLevel} / ${latestPlan.value.planType}，${latestPlan.value.trendSummary}` : '生成后可查看饮食与运动建议'
  }
])

onMounted(() => {
  reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  loadHomeData()

  if (reduceMotion) {
    return
  }

  animateHome()
})

onBeforeUnmount(() => {
  ctx?.revert()
})

async function loadHomeData() {
  const [userData, profileData, weightData, countData, historyData] = await Promise.allSettled([
    getCurrentUser(),
    getProfile(),
    getWeightHistory(),
    getGenerateCount(),
    getPlanHistory()
  ])

  if (userData.status === 'fulfilled') {
    currentUser.value = userData.value
    setUser(userData.value)
    window.dispatchEvent(new Event('smart-health-user-updated'))
  }
  if (profileData.status === 'fulfilled') {
    profile.value = profileData.value
  }
  if (weightData.status === 'fulfilled') {
    weights.value = [...weightData.value].sort((a, b) => a.recordDate.localeCompare(b.recordDate))
  }
  if (countData.status === 'fulfilled') {
    planCount.value = countData.value
  }
  if (historyData.status === 'fulfilled') {
    plans.value = [...historyData.value].sort((a, b) => b.createTime.localeCompare(a.createTime))
  }

  await nextTick()
  animateTrend()
}

function animateHome() {
  ctx = gsap.context(() => {
    gsap
      .timeline({ defaults: { ease: 'power3.out' } })
      .from('.home-animate', { autoAlpha: 0, y: 24, duration: 0.62, stagger: 0.07 })
      .from('.trend-dots circle', { autoAlpha: 0, scale: 0.4, transformOrigin: 'center', stagger: 0.08, duration: 0.32 }, '-=0.38')

    gsap.to('.home-primary-action', {
      y: -3,
      duration: 1.8,
      ease: 'sine.inOut',
      repeat: -1,
      yoyo: true
    })
  }, homeRoot.value)
  animateTrend()
}

function animateTrend() {
  if (reduceMotion || !trendPath.value) {
    return
  }
  const length = trendPath.value.getTotalLength()
  gsap.set(trendPath.value, { strokeDasharray: length, strokeDashoffset: length })
  gsap.to(trendPath.value, { strokeDashoffset: 0, duration: 1.1, ease: 'power2.inOut', overwrite: true })
}
</script>
