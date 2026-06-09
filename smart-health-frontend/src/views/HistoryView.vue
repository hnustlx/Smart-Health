<template>
  <section class="page-section">
    <div class="section-heading">
      <p>历史计划</p>
      <h2>按创建时间倒序查看历史记录</h2>
    </div>

    <div v-loading="loading" class="history-layout">
      <el-empty v-if="plans.length === 0" description="暂无历史计划" />
      <template v-else>
        <div class="table-filter-bar">
          <el-select v-model="filters.date" clearable placeholder="按创建日期筛选">
            <el-option v-for="date in dateOptions" :key="date" :label="date" :value="date" />
          </el-select>
          <el-select v-model="filters.type" clearable placeholder="按类型筛选">
            <el-option v-for="type in typeOptions" :key="type" :label="type" :value="type" />
          </el-select>
          <el-select v-model="filters.level" clearable placeholder="按级别筛选">
            <el-option v-for="level in levelOptions" :key="level" :label="level" :value="level" />
          </el-select>
          <span>共 {{ filteredPlans.length }} 条</span>
        </div>
        <el-empty v-if="filteredPlans.length === 0" description="没有匹配的历史计划" />
        <el-table v-else :data="filteredPlans" border @row-click="selectPlan">
          <el-table-column prop="createTime" label="创建时间">
            <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column prop="planType" label="类型" width="140" />
          <el-table-column prop="planLevel" label="级别" width="140" />
          <el-table-column prop="trendSummary" label="体重趋势" />
        </el-table>

        <section v-if="detail" class="history-detail">
          <h3>计划详情</h3>
          <p class="muted-text">{{ detail.trendSummary }}</p>
          <pre>{{ prettyContent }}</pre>
        </section>
      </template>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { getPlanDetail, getPlanHistory } from '../api/modules/plan'

const loading = ref(false)
const plans = ref([])
const detail = ref()
const filters = reactive({
  date: '',
  type: '',
  level: ''
})

const prettyContent = computed(() => JSON.stringify(detail.value?.planContent || {}, null, 2))
const dateOptions = computed(() => [...new Set(plans.value.map((item) => item.createTime.slice(0, 10)))])
const typeOptions = computed(() => [...new Set(plans.value.map((item) => item.planType).filter(Boolean))])
const levelOptions = computed(() => [...new Set(plans.value.map((item) => item.planLevel).filter(Boolean))])
const filteredPlans = computed(() =>
  plans.value.filter((item) => {
    const dateMatch = !filters.date || item.createTime.startsWith(filters.date)
    const typeMatch = !filters.type || item.planType === filters.type
    const levelMatch = !filters.level || item.planLevel === filters.level
    return dateMatch && typeMatch && levelMatch
  })
)

onMounted(loadHistory)

async function loadHistory() {
  loading.value = true
  try {
    const data = await getPlanHistory()
    plans.value = [...data].sort((a, b) => b.createTime.localeCompare(a.createTime))
  } finally {
    loading.value = false
  }
}

async function selectPlan(row) {
  detail.value = await getPlanDetail(row.id)
}

function formatTime(value) {
  return value ? value.replace('T', ' ') : ''
}
</script>
