<template>
  <section ref="vipRoot" class="vip-page">
    <div class="vip-hero vip-animate">
      <div>
        <p class="vip-kicker">VIP 升级</p>
        <h1>解锁更完整的个性化健康规划</h1>
        <p>普通用户每天可生成 3 次 AI 计划，VIP 用户可生成 5 次，并获得详细饮食、训练强度、营养比例和智能健康问答。</p>
      </div>
      <div ref="badgeRef" class="vip-badge">
        <span>当前身份</span>
        <strong>{{ roleLabel }}</strong>
        <small>{{ vipExpireText }}</small>
      </div>
    </div>

    <div class="vip-grid">
      <section class="vip-card vip-animate basic">
        <span>普通用户</span>
        <h2>基础健康规划</h2>
        <strong>免费</strong>
        <ul>
          <li>每日 3 次 AI 计划生成</li>
          <li>基础饮食与运动建议</li>
          <li>体重记录与趋势展示</li>
          <li>历史计划查询</li>
        </ul>
      </section>

      <section class="vip-card vip-animate featured">
        <span>VIP 用户</span>
        <h2>进阶健康助手</h2>
        <strong>{{ vipActionTitle }}</strong>
        <ul>
          <li>每日 5 次 AI 计划生成</li>
          <li>热量估算与营养比例建议</li>
          <li>训练强度分级与每周复盘</li>
          <li>VIP 智能健康问答</li>
        </ul>
        <el-form :model="form" class="vip-code-form" @submit.prevent>
          <el-form-item>
            <el-input v-model.trim="form.code" placeholder="输入 VIP 激活码" />
          </el-form-item>
          <el-button type="primary" size="large" :loading="loading" @click="activate">
            {{ vipActionText }}
          </el-button>
        </el-form>
      </section>
    </div>
  </section>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { gsap } from 'gsap'
import { activateVip } from '../api/modules/auth'
import { getUser, setUser } from '../utils/auth'

const roleNames = {
  USER: '普通用户',
  VIP: 'VIP 用户',
  ADMIN: '管理员'
}

const vipRoot = ref()
const badgeRef = ref()
const user = ref(getUser())
const roleLabel = computed(() => roleNames[user.value?.role] || '普通用户')
const isVip = computed(() => user.value?.role === 'VIP')
const vipExpireText = computed(() => {
  if (!isVip.value) {
    return '升级后显示 VIP 到期时间'
  }
  return user.value?.vipExpireTime ? `到期时间 ${formatTime(user.value.vipExpireTime)}` : 'VIP 已开通'
})
const vipActionTitle = computed(() => (isVip.value ? '激活码续充' : '激活码升级'))
const vipActionText = computed(() => (isVip.value ? '立即续充 VIP' : '立即激活 VIP'))
const loading = ref(false)
const form = reactive({
  code: ''
})
let ctx

async function activate() {
  if (!form.code) {
    ElMessage.warning('请输入 VIP 激活码')
    return
  }
  loading.value = true
  try {
    const wasVip = isVip.value
    const nextUser = await activateVip({ code: form.code })
    user.value = nextUser
    setUser(nextUser)
    window.dispatchEvent(new Event('smart-health-user-updated'))
    ElMessage.success(wasVip ? 'VIP 续充成功' : 'VIP 激活成功')
    form.code = ''
    animateBadge()
  } finally {
    loading.value = false
  }
}

function formatTime(value) {
  return value ? value.replace('T', ' ') : '-'
}

function animateBadge() {
  if (!badgeRef.value || window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
    return
  }
  gsap.fromTo(
    badgeRef.value,
    { scale: 0.96, y: 8 },
    { scale: 1, y: 0, duration: 0.5, ease: 'back.out(1.8)', overwrite: 'auto' }
  )
}

onMounted(() => {
  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
    return
  }
  ctx = gsap.context(() => {
    gsap.from('.vip-animate', {
      autoAlpha: 0,
      y: 24,
      duration: 0.62,
      stagger: 0.08,
      ease: 'power3.out'
    })
  }, vipRoot.value)
})

onBeforeUnmount(() => {
  ctx?.revert()
})
</script>
