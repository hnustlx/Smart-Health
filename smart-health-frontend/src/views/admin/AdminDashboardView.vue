<template>
  <section v-loading="loading" class="admin-page admin-dashboard-page">
    <div class="admin-section-heading">
      <p>Dashboard</p>
      <h2>后台总览</h2>
    </div>

    <el-alert
      v-if="error"
      class="admin-dashboard-alert"
      type="error"
      title="总览数据加载失败"
      :description="error"
      show-icon
      :closable="false"
    />

    <div class="admin-overview-grid admin-metric-grid">
      <div class="admin-overview-card">
        <span>使用用户</span>
        <strong>{{ formatNumber(dashboard.users.total) }}</strong>
        <small>今日新增 {{ formatNumber(dashboard.users.newToday) }} · VIP {{ formatNumber(dashboard.users.vipTotal) }}</small>
      </div>
      <div class="admin-overview-card">
        <span>在线情况</span>
        <strong>{{ formatNumber(dashboard.users.online) }}</strong>
        <small>今日活跃 {{ formatNumber(dashboard.users.activeToday) }} · 7 日 {{ formatNumber(dashboard.users.active7Days) }}</small>
      </div>
      <div class="admin-overview-card">
        <span>AI 计划</span>
        <strong>{{ formatNumber(dashboard.plans.generatedToday) }}</strong>
        <small>7 日生成 {{ formatNumber(dashboard.plans.generated7Days) }} · VIP {{ formatNumber(dashboard.plans.vipGeneratedToday) }}</small>
      </div>
      <div class="admin-overview-card">
        <span>知识引用</span>
        <strong>{{ formatNumber(dashboard.knowledge.referencedToday) }}</strong>
        <small>7 日引用 {{ formatNumber(dashboard.knowledge.referenced7Days) }} · 启用 {{ formatNumber(dashboard.knowledge.enabled) }}</small>
      </div>
    </div>

    <div class="admin-dashboard-panels">
      <section class="admin-dashboard-panel">
        <div class="admin-dashboard-panel-title">
          <div>
            <span>Knowledge</span>
            <h3>热门知识引用</h3>
          </div>
          <el-button link type="primary" @click="$router.push('/admin/knowledge')">查看知识库</el-button>
        </div>
        <el-empty v-if="!dashboard.topKnowledge.length" description="暂无引用记录" />
        <el-table v-else :data="dashboard.topKnowledge" size="large">
          <el-table-column prop="title" label="知识标题" min-width="180" />
          <el-table-column prop="category" label="分类" width="120" />
          <el-table-column prop="referenceCount" label="7 日引用" width="110" />
          <el-table-column label="最近引用" width="180">
            <template #default="{ row }">{{ formatTime(row.lastReferencedTime) }}</template>
          </el-table-column>
        </el-table>
      </section>

      <section class="admin-dashboard-panel">
        <div class="admin-dashboard-panel-title">
          <div>
            <span>Activity</span>
            <h3>最近活跃用户</h3>
          </div>
          <el-button link type="primary" @click="$router.push('/admin/users')">查看用户</el-button>
        </div>
        <el-empty v-if="!dashboard.recentActiveUsers.length" description="暂无活跃记录" />
        <el-table v-else :data="dashboard.recentActiveUsers" size="large">
          <el-table-column prop="username" label="用户名" min-width="140" />
          <el-table-column prop="role" label="角色" width="90" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.online ? 'success' : 'info'" effect="plain">
                {{ row.online ? '在线' : '离线' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="最近活跃" width="180">
            <template #default="{ row }">{{ formatTime(row.lastActiveTime) }}</template>
          </el-table-column>
        </el-table>
      </section>
    </div>

    <div class="admin-dashboard-strip">
      <div>
        <span>禁用用户</span>
        <strong>{{ formatNumber(dashboard.users.disabledTotal) }}</strong>
      </div>
      <div>
        <span>基础计划今日生成</span>
        <strong>{{ formatNumber(dashboard.plans.basicGeneratedToday) }}</strong>
      </div>
      <div>
        <span>知识总数</span>
        <strong>{{ formatNumber(dashboard.knowledge.total) }}</strong>
      </div>
      <div>
        <span>禁用知识</span>
        <strong>{{ formatNumber(dashboard.knowledge.disabled) }}</strong>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { getDashboard } from '../../api/modules/admin'

const loading = ref(false)
const error = ref('')

const dashboard = reactive({
  users: {
    total: 0,
    online: 0,
    activeToday: 0,
    active7Days: 0,
    newToday: 0,
    vipTotal: 0,
    disabledTotal: 0
  },
  plans: {
    generatedToday: 0,
    generated7Days: 0,
    basicGeneratedToday: 0,
    vipGeneratedToday: 0
  },
  knowledge: {
    total: 0,
    enabled: 0,
    disabled: 0,
    referencedToday: 0,
    referenced7Days: 0
  },
  topKnowledge: [],
  recentActiveUsers: []
})

function applyDashboard(data) {
  Object.assign(dashboard.users, data?.users || {})
  Object.assign(dashboard.plans, data?.plans || {})
  Object.assign(dashboard.knowledge, data?.knowledge || {})
  dashboard.topKnowledge = data?.topKnowledge || []
  dashboard.recentActiveUsers = data?.recentActiveUsers || []
}

function formatNumber(value) {
  return Number(value || 0).toLocaleString('zh-CN')
}

function formatTime(value) {
  if (!value) {
    return '-'
  }
  return String(value).replace('T', ' ').slice(0, 16)
}

async function loadDashboard() {
  loading.value = true
  error.value = ''
  try {
    applyDashboard(await getDashboard())
  } catch (err) {
    error.value = err?.message || err?.data?.message || '请稍后重试'
  } finally {
    loading.value = false
  }
}

onMounted(loadDashboard)
</script>
