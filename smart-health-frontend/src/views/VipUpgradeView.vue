<template>
  <section class="vip-page">
    <div class="vip-hero">
      <div>
        <p class="vip-kicker">VIP Upgrade</p>
        <h1>解锁更完整的个性化健康规划</h1>
        <p>普通用户每天可生成 2 次 AI 计划，VIP 用户可生成 5 次，并获得详细饮食、训练强度、营养比例和智能健康问答。</p>
      </div>
      <div class="vip-badge">
        <span>当前身份</span>
        <strong>{{ roleLabel }}</strong>
      </div>
    </div>

    <div class="vip-grid">
      <section class="vip-card basic">
        <span>普通用户</span>
        <h2>基础健康规划</h2>
        <strong>免费</strong>
        <ul>
          <li>每日 2 次 AI 计划生成</li>
          <li>基础饮食与运动建议</li>
          <li>体重记录与趋势展示</li>
          <li>历史计划查询</li>
        </ul>
      </section>

      <section class="vip-card featured">
        <span>VIP 用户</span>
        <h2>进阶健康助手</h2>
        <strong>推荐升级</strong>
        <ul>
          <li>每日 5 次 AI 计划生成</li>
          <li>热量估算与营养比例建议</li>
          <li>训练强度分级与每周复盘</li>
          <li>VIP 智能健康问答</li>
        </ul>
        <el-button type="primary" size="large" @click="upgradePreview">立即升级 VIP</el-button>
      </section>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getUser, setUser } from '../utils/auth'

const roleNames = {
  USER: '普通用户',
  VIP: 'VIP 用户',
  ADMIN: '管理员'
}

const user = computed(() => getUser())
const roleLabel = computed(() => roleNames[user.value?.role] || '普通用户')

function upgradePreview() {
  setUser({
    ...user.value,
    role: 'VIP'
  })
  sessionStorage.setItem('vip_preview_enabled', 'true')
  ElMessage.success('已升级为 VIP 用户')
  window.location.reload()
}
</script>
