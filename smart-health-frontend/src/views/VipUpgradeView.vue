<template>
  <section class="vip-page">
    <div class="vip-hero">
      <div>
        <p class="vip-kicker">VIP Upgrade</p>
        <h1>解锁更完整的个性化健康规划</h1>
        <p>普通用户每天可生成 3 次 AI 计划，VIP 用户可生成 5 次，并获得详细饮食、训练强度、营养比例和智能健康问答。</p>
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
          <li>每日 3 次 AI 计划生成</li>
          <li>基础饮食与运动建议</li>
          <li>体重记录与趋势展示</li>
          <li>历史计划查询</li>
        </ul>
      </section>

      <section class="vip-card featured">
        <span>VIP 用户</span>
        <h2>进阶健康助手</h2>
        <strong>激活码升级</strong>
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
            立即激活 VIP
          </el-button>
        </el-form>
      </section>
    </div>
  </section>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { activateVip } from '../api/modules/auth'
import { getUser, setUser } from '../utils/auth'

const roleNames = {
  USER: '普通用户',
  VIP: 'VIP 用户',
  ADMIN: '管理员'
}

const user = computed(() => getUser())
const roleLabel = computed(() => roleNames[user.value?.role] || '普通用户')
const loading = ref(false)
const form = reactive({
  code: ''
})

async function activate() {
  if (!form.code) {
    ElMessage.warning('请输入 VIP 激活码')
    return
  }
  loading.value = true
  try {
    const nextUser = await activateVip({ code: form.code })
    setUser(nextUser)
    window.dispatchEvent(new Event('smart-health-user-updated'))
    ElMessage.success('VIP 激活成功')
    form.code = ''
  } finally {
    loading.value = false
  }
}
</script>
