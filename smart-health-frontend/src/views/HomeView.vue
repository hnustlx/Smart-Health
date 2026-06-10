<template>
  <section ref="homeRoot" class="home-page">
    <div ref="homeRevealDoor" class="home-reveal-door" aria-hidden="true">
      <div ref="homeRevealLeftDoor" class="home-reveal-door-panel home-reveal-door-panel--left"></div>
      <div ref="homeRevealRightDoor" class="home-reveal-door-panel home-reveal-door-panel--right"></div>
    </div>
    <div class="home-hero home-animate">
      <img
        class="home-hero-image"
        src="https://images.unsplash.com/photo-1490645935967-10de6ba17061?auto=format&fit=crop&w=1200&q=82"
        alt=""
      />
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
        <span class="summary-card-icon" aria-hidden="true">🥗</span>
        <span>健康档案</span>
        <strong>{{ profileStatus }}</strong>
        <small>{{ profileSummary }}</small>
      </div>
      <div class="summary-card home-animate">
        <span class="summary-card-icon" aria-hidden="true">⚖️</span>
        <span>体重记录</span>
        <strong>{{ weightStatus }}</strong>
        <small>{{ weightSummary }}</small>
      </div>
      <div class="summary-card home-animate">
        <span class="summary-card-icon" aria-hidden="true">💬</span>
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

        <div class="home-trend-metrics">
          <div v-for="item in trendMetrics" :key="item.label">
            <span>{{ item.label }}</span>
            <strong :class="item.tone">{{ item.value }}</strong>
            <small>{{ item.hint }}</small>
          </div>
        </div>

        <div class="home-trend-chart">
          <svg viewBox="0 0 680 300" role="img" aria-label="体重趋势预览图">
            <defs>
              <linearGradient id="homeTrendAreaGradient" x1="0" x2="0" y1="0" y2="1">
                <stop offset="0%" stop-color="#10b981" stop-opacity="0.22" />
                <stop offset="100%" stop-color="#10b981" stop-opacity="0.03" />
              </linearGradient>
              <linearGradient id="homeTrendLineGradient" x1="0" x2="1" y1="0" y2="0">
                <stop offset="0%" stop-color="#0f766e" />
                <stop offset="100%" stop-color="#059669" />
              </linearGradient>
            </defs>

            <g class="home-trend-grid">
              <line v-for="line in trendGridLines" :key="line" x1="58" x2="632" :y1="line" :y2="line" />
            </g>
            <line
              v-if="hasWeightTrend"
              class="home-trend-average-line"
              x1="58"
              x2="632"
              :y1="trendAverageLineY"
              :y2="trendAverageLineY"
            />

            <g v-if="hasWeightTrend" class="home-trend-y-labels">
              <text v-for="label in trendYLabels" :key="label.value" x="46" :y="label.y + 4">{{ label.value }}</text>
            </g>
            <g v-if="hasWeightTrend" class="home-trend-x-labels">
              <text v-for="label in trendXLabels" :key="label.id" :x="label.x" y="258">{{ label.value }}</text>
            </g>

            <path v-if="hasWeightTrend" class="trend-area" :d="trendAreaPath" />
            <path v-if="hasWeightTrend" ref="trendPath" class="trend-path" :d="trendPathD" />
            <g v-if="hasWeightTrend" class="trend-dots">
              <g v-for="point in trendPoints" :key="point.id">
                <circle :cx="point.x" :cy="point.y" r="5" />
                <text :x="point.x" :y="point.y - 14">{{ point.label }}</text>
              </g>
            </g>

            <g v-if="latestTrendPoint" class="home-trend-latest">
              <line :x1="latestTrendPoint.x" :x2="latestTrendPoint.x" y1="42" y2="220" />
              <rect :x="latestTrendPoint.x - 42" :y="latestTrendPoint.y + 14" width="84" height="30" rx="8" />
              <text :x="latestTrendPoint.x" :y="latestTrendPoint.y + 34">{{ latestTrendPoint.label }}</text>
            </g>
            <text v-else class="home-trend-empty" x="340" y="148">暂无体重记录</text>
          </svg>
        </div>

        <div class="home-trend-footer">
          <p>{{ trendInsight }}</p>
          <div class="home-trend-records">
            <span v-for="item in trendRecordItems" :key="item.id">{{ item.label }} · {{ item.weight }}kg</span>
            <span v-if="trendRecordItems.length === 0">等待记录</span>
          </div>
        </div>
      </section>

      <section class="checkin-panel home-animate">
        <div class="panel-heading">
          <div>
            <p>健康打卡</p>
            <h2>近 30 天完成情况</h2>
          </div>
          <span>{{ checkinActiveDays }} 天已打卡</span>
        </div>

        <div class="checkin-stats">
          <div>
            <span>当前连续</span>
            <strong>{{ checkinStreak }} 天</strong>
          </div>
          <div>
            <span>累计打卡</span>
            <strong>{{ checkinTotalDays }} 天</strong>
          </div>
        </div>

        <el-button class="checkin-action" type="primary" :loading="checkingIn" :disabled="checkedToday" @click="submitCheckin">
          {{ checkedToday ? '今日已打卡' : '今日打卡' }}
        </el-button>

        <div class="checkin-heatmap" aria-label="近 30 天健康打卡热力图">
          <span
            v-for="day in checkinDays"
            :key="day.key"
            class="checkin-cell"
            :class="`level-${day.level}`"
            :title="day.title"
          ></span>
        </div>

        <div class="checkin-legend">
          <span>少</span>
          <i class="level-0"></i>
          <i class="level-3"></i>
          <span>多</span>
        </div>

        <div class="checkin-breakdown">
          <div>
            <span>7 天 VIP 奖励</span>
            <strong>{{ rewardProgressText }}</strong>
          </div>
        </div>

        <div v-if="latestRewardCode" class="checkin-reward">
          <span>已生成奖励码</span>
          <strong>{{ latestRewardCode }}</strong>
        </div>

        <p class="checkin-note">{{ checkinNote }}</p>
      </section>
    </div>

  </section>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { gsap } from 'gsap'
import { ElMessage } from 'element-plus'
import { getCurrentUser } from '../api/modules/auth'
import { checkinToday, getCheckinStatus } from '../api/modules/checkin'
import { getGenerateCount, normalizeGenerateCount } from '../api/modules/plan'
import { getProfile } from '../api/modules/profile'
import { getWeightHistory } from '../api/modules/weight'
import { getUser, setUser } from '../utils/auth'

const currentUser = ref(getUser())
const profile = ref()
const weights = ref([])
const planCount = ref()
const checkinStatus = ref()
const checkingIn = ref(false)
const latestRewardCode = ref('')
const homeRoot = ref()
const trendPath = ref()
const homeRevealDoor = ref()
const homeRevealLeftDoor = ref()
const homeRevealRightDoor = ref()
let ctx
let reduceMotion = false

const isVip = computed(() => currentUser.value?.role === 'VIP')
const displayName = computed(() => {
  const username = currentUser.value?.username
  return username === 'frontend_dev' ? '用户' : username || '用户'
})
const planCountText = computed(() => {
  if (!planCount.value) {
    return isVip.value ? '5 次' : '3 次'
  }
  return `${planCount.value.remaining} 次`
})
const planLimitText = computed(() => {
  if (!planCount.value) {
    return `${isVip.value ? 'VIP 用户' : '普通用户'}每日可生成 ${isVip.value ? 5 : 3} 次`
  }
  return `${isVip.value ? 'VIP 用户' : '普通用户'}今日已用 ${planCount.value.used}/${planCount.value.limit}`
})
const profileStatus = computed(() => (profile.value ? '已同步' : '待同步'))
const profileSummary = computed(() => {
  if (!profile.value) {
    return '年龄、身高、目标、饮食偏好'
  }
  return `${profile.value.age} 岁 / ${profile.value.height}cm / ${profile.value.goal || '目标待完善'}`
})
const recentWeights = computed(() => weights.value.slice(-7))
const hasWeightTrend = computed(() => recentWeights.value.length > 0)
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
const trendValues = computed(() => recentWeights.value.map((item) => Number(item.weight)).filter((item) => !Number.isNaN(item)))
const trendChange = computed(() => {
  if (trendValues.value.length < 2) {
    return 0
  }
  return Number((trendValues.value.at(-1) - trendValues.value[0]).toFixed(1))
})
const trendAverage = computed(() => {
  if (!trendValues.value.length) {
    return undefined
  }
  const total = trendValues.value.reduce((sum, item) => sum + item, 0)
  return Number((total / trendValues.value.length).toFixed(1))
})
const trendChartBounds = computed(() => {
  const values = trendValues.value.length ? trendValues.value : [0]
  const min = Math.min(...values)
  const max = Math.max(...values)
  const padding = Math.max((max - min) * 0.22, 0.5)
  return {
    min: Number((min - padding).toFixed(1)),
    max: Number((max + padding).toFixed(1))
  }
})
const trendGridLines = computed(() => [42, 86.5, 131, 175.5, 220])
const trendYLabels = computed(() => {
  const { min, max } = trendChartBounds.value
  return trendGridLines.value.map((y, index) => ({
    y,
    value: `${(max - ((max - min) * index) / (trendGridLines.value.length - 1)).toFixed(1)}`
  }))
})
const trendPoints = computed(() => {
  if (!hasWeightTrend.value) {
    return []
  }

  const { min, max } = trendChartBounds.value
  const range = max - min || 1

  return recentWeights.value.map((item, index) => ({
    id: item.id,
    x: recentWeights.value.length === 1 ? 58 : 58 + (index * 574) / (recentWeights.value.length - 1),
    y: 220 - ((Number(item.weight) - min) / range) * 178,
    label: Number(item.weight).toFixed(1),
    date: formatShortDate(item.recordDate)
  }))
})
const trendPathD = computed(() => `M${trendPoints.value.map((point) => `${point.x} ${point.y}`).join(' L')}`)
const trendAreaPath = computed(() => `${trendPathD.value} V220 H58 Z`)
const trendAverageLineY = computed(() => {
  const value = trendAverage.value ?? 0
  const { min, max } = trendChartBounds.value
  return 220 - ((value - min) / (max - min || 1)) * 178
})
const latestTrendPoint = computed(() => trendPoints.value.at(-1))
const trendXLabels = computed(() => {
  const points = trendPoints.value
  if (!hasWeightTrend.value) {
    return []
  }
  const first = points[0]
  const latest = points.at(-1)
  return [
    { id: 'first', x: first.x, value: first.date },
    { id: 'latest', x: latest.x, value: latest.date }
  ]
})
const trendMetrics = computed(() => {
  const latest = trendValues.value.at(-1)
  const changeTone = trendChange.value < 0 ? 'is-down' : trendChange.value > 0 ? 'is-up' : ''
  return [
    {
      label: '当前体重',
      value: latest ? `${latest.toFixed(1)} kg` : '待记录',
      hint: latest ? '最近一次记录' : '记录后生成趋势'
    },
    {
      label: '区间变化',
      value: trendValues.value.length > 1 ? `${trendChange.value > 0 ? '+' : ''}${trendChange.value.toFixed(1)} kg` : '--',
      hint: `近 ${recentWeights.value.length || 0} 次记录`,
      tone: changeTone
    },
    {
      label: '平均体重',
      value: trendAverage.value ? `${trendAverage.value.toFixed(1)} kg` : '--',
      hint: '用于观察整体水平'
    }
  ]
})
const trendInsight = computed(() => {
  if (recentWeights.value.length === 0) {
    return '暂无体重记录，添加记录后会展示真实趋势、均值和最近记录。'
  }
  if (recentWeights.value.length === 1) {
    return '已有 1 次体重记录，继续记录后可以判断变化方向。'
  }
  if (trendChange.value < 0) {
    return `近 ${recentWeights.value.length} 次累计下降 ${Math.abs(trendChange.value).toFixed(1)} kg，当前节奏比较稳定。`
  }
  if (trendChange.value > 0) {
    return `近 ${recentWeights.value.length} 次累计上升 ${trendChange.value.toFixed(1)} kg，建议结合饮食和运动计划复盘。`
  }
  return '近期体重基本持平，可以继续保持当前记录和计划节奏。'
})
const trendRecordItems = computed(() =>
  recentWeights.value
    .slice(-3)
    .reverse()
    .map((item) => ({
      id: item.id,
      label: formatShortDate(item.recordDate),
      weight: Number(item.weight).toFixed(1)
    }))
)
const checkinDateKeys = computed(() => new Set((checkinStatus.value?.dates || []).map((item) => toDateKey(item)).filter(Boolean)))
const last30Days = computed(() => {
  const today = new Date()
  return Array.from({ length: 30 }, (_, index) => {
    const date = new Date(today)
    date.setDate(today.getDate() - 29 + index)
    const key = formatDateKey(date)
    return {
      key,
      label: `${date.getMonth() + 1}/${date.getDate()}`
    }
  })
})
const checkinDays = computed(() =>
  last30Days.value.map((day) => {
    const checked = checkinDateKeys.value.has(day.key)
    return {
      ...day,
      level: checked ? 3 : 0,
      title: `${day.label}：${checked ? '已打卡' : '未打卡'}`
    }
  })
)
const checkinActiveDays = computed(() => last30Days.value.filter((day) => checkinDateKeys.value.has(day.key)).length)
const checkinStreak = computed(() => checkinStatus.value?.currentStreak || 0)
const checkinTotalDays = computed(() => checkinStatus.value?.totalDays || 0)
const checkedToday = computed(() => Boolean(checkinStatus.value?.checkedToday))
const rewardRemainingDays = computed(() => checkinStatus.value?.nextRewardRemainingDays || 30)
const rewardProgressText = computed(() => (rewardRemainingDays.value === 30 ? '还差 30 天' : `还差 ${rewardRemainingDays.value} 天`))
const checkinNote = computed(() => {
  if (latestRewardCode.value) {
    return '奖励码已生成，可在升级 VIP 页面使用，管理员也能在激活码列表中查看。'
  }
  if (!checkinTotalDays.value) {
    return '点击今日打卡开始累计，满 30 天自动生成 7 天 VIP 激活码。'
  }
  if (checkinStreak.value >= 7) {
    return '连续打卡已超过一周，继续保持，满 30 天会自动生成奖励码。'
  }
  return '每天手动打卡一次，累计满 30 天可获得 7 天 VIP 激活码。'
})

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
  const [userData, profileData, weightData, countData, checkinData] = await Promise.allSettled([
    getCurrentUser(),
    getProfile(),
    getWeightHistory(),
    getGenerateCount(),
    getCheckinStatus()
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
    planCount.value = normalizeGenerateCount(countData.value)
  }
  if (checkinData.status === 'fulfilled') {
    checkinStatus.value = checkinData.value
  }

  await nextTick()
  animateTrend()
}

async function submitCheckin() {
  checkingIn.value = true
  try {
    const data = await checkinToday()
    checkinStatus.value = data
    latestRewardCode.value = data.rewardCode || ''
    ElMessage.success(latestRewardCode.value ? `打卡成功，已生成 7 天 VIP 码：${latestRewardCode.value}` : '打卡成功')
  } finally {
    checkingIn.value = false
  }
}

function toDateKey(value) {
  if (!value) {
    return ''
  }
  if (typeof value === 'string' && /^\d{4}-\d{2}-\d{2}/.test(value)) {
    return value.slice(0, 10)
  }
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? '' : formatDateKey(date)
}

function formatDateKey(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function formatShortDate(value) {
  const key = toDateKey(value)
  if (!key) {
    return '--'
  }
  const [, month, day] = key.split('-')
  return `${Number(month)}/${Number(day)}`
}

function animateHome() {
  ctx = gsap.context(() => {
    const fromLogin = sessionStorage.getItem('login_home_transition') === '1'
    sessionStorage.removeItem('login_home_transition')
    fromLogin ? animateLoginHomeEntry() : animateDefaultHomeEntry()

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

function animateDefaultHomeEntry() {
  return gsap
    .timeline({ defaults: { ease: 'power3.out' } })
    .from('.home-animate', { autoAlpha: 0, y: 24, duration: 0.62, stagger: 0.07 })
    .from('.trend-dots circle', { autoAlpha: 0, scale: 0.4, transformOrigin: 'center', stagger: 0.08, duration: 0.32 }, '-=0.38')
}

function animateLoginHomeEntry() {
  const doorOpenVars = getDoorOpenVars()
  const timeline = gsap.timeline({ defaults: { ease: 'power3.out' } })
  gsap.set(homeRevealDoor.value, { autoAlpha: 1 })
  gsap.set([homeRevealLeftDoor.value, homeRevealRightDoor.value], { xPercent: 0, yPercent: 0 })
  gsap.set('.home-animate', { autoAlpha: 1, y: 0, scale: 1 })

  timeline
    .from('.home-hero-image', { scale: 1.04, duration: 0.82, ease: 'power2.out' }, 0)
    .to(homeRevealLeftDoor.value, { ...doorOpenVars.left, duration: 0.82, ease: 'power3.inOut' }, 0)
    .to(homeRevealRightDoor.value, { ...doorOpenVars.right, duration: 0.82, ease: 'power3.inOut' }, 0)
    .from('.home-hero-copy > *, .home-hero-panel', { autoAlpha: 0.72, y: 4, duration: 0.24, stagger: 0.025 }, 0.1)
    .from('.trend-dots circle', { autoAlpha: 0, scale: 0.4, transformOrigin: 'center', stagger: 0.08, duration: 0.3 }, 0.58)
    .set(homeRevealDoor.value, { autoAlpha: 0 })

  return timeline
}

function getDoorOpenVars() {
  if (window.matchMedia('(max-width: 860px)').matches) {
    return {
      left: { yPercent: -112, xPercent: 0 },
      right: { yPercent: 112, xPercent: 0 }
    }
  }
  return {
    left: { xPercent: -112, yPercent: 0 },
    right: { xPercent: 112, yPercent: 0 }
  }
}

function animateTrend() {
  if (reduceMotion || !trendPath.value || !hasWeightTrend.value) {
    return
  }
  const length = trendPath.value.getTotalLength()
  gsap.set(trendPath.value, { strokeDasharray: length, strokeDashoffset: length })
  gsap.to(trendPath.value, { strokeDashoffset: 0, duration: 1.1, ease: 'power2.inOut', overwrite: true })
}
</script>
