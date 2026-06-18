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
          <span>共 {{ total }} 条</span>
        </div>
        <el-empty v-if="filteredPlans.length === 0" description="没有匹配的历史计划" />
        <template v-if="filteredPlans.length > 0">
          <el-table :data="filteredPlans" border @row-click="selectPlan">
            <el-table-column prop="createTime" label="创建时间">
            <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="类型" width="140">
            <template #default="{ row }">{{ formatPlanType(row.planType) }}</template>
          </el-table-column>
          <el-table-column label="级别" width="140">
            <template #default="{ row }">{{ formatPlanLevel(row.planLevel) }}</template>
          </el-table-column>
          <el-table-column prop="trendSummary" label="体重趋势" />
        </el-table>
          <el-pagination
            v-if="total > size"
            v-model:current-page="page"
            v-model:page-size="size"
            class="table-pagination"
            background
            layout="total, prev, pager, next"
            :total="total"
            @current-change="loadHistory"
          />
        </template>

        <el-empty v-else description="没有匹配的历史计划" />

        <section v-if="detail" class="history-detail">
          <div class="history-detail-head">
            <div>
              <p>{{ detailLevelText }} / {{ detailTypeText }}</p>
              <h3>计划详情</h3>
            </div>
            <span>{{ formatTime(detail.createTime) }}</span>
          </div>
          <el-alert
            v-if="detail.trendSummary"
            :title="detail.trendSummary"
            type="success"
            :closable="false"
            show-icon
          />

          <div class="plan-pair-head history-plan-head">
            <div class="track-heading">
              <span class="track-icon"><ForkSpoon /></span>
              <div>
                <p>饮食</p>
                <h4>饮食计划</h4>
              </div>
            </div>
            <div class="track-heading exercise-track">
              <span class="track-icon"><Bicycle /></span>
              <div>
                <p>运动</p>
                <h4>运动计划</h4>
              </div>
            </div>
          </div>

          <el-empty v-if="weeklyPlans.length === 0" description="暂无饮食与运动计划" />
          <div v-else class="plan-week-list">
            <section v-for="item in weeklyPlans" :key="item.day" class="plan-day-pair">
              <div class="day-ribbon">{{ item.day }}</div>
              <div class="plan-day-card diet-card">
                <div class="plan-day-head">
                  <strong>今日餐单</strong>
                  <span>饮食</span>
                </div>
                <div class="meal-list">
                  <p><span>早餐</span>{{ item.diet?.breakfast || '待生成' }}</p>
                  <p><span>午餐</span>{{ item.diet?.lunch || '待生成' }}</p>
                  <p><span>晚餐</span>{{ item.diet?.dinner || '待生成' }}</p>
                  <p><span>加餐</span>{{ item.diet?.snack || '待生成' }}</p>
                </div>
                <div v-if="item.diet?.calorie" class="calorie-insight">
                  <template v-if="isVipDetail">
                    <div v-if="item.diet.calorie.total">
                      <span>总热量</span>
                      <strong>{{ item.diet.calorie.total }}</strong>
                    </div>
                    <div v-if="item.diet.calorie.target">
                      <span>目标区间</span>
                      <strong>{{ item.diet.calorie.target }}</strong>
                    </div>
                    <ul v-if="item.diet.calorie.foods?.length" class="calorie-food-list">
                      <li v-for="food in item.diet.calorie.foods" :key="`${item.day}-${food.name}`">
                        <span>{{ food.name }}</span>
                        <strong>{{ formatCalorie(food.kcal) }}</strong>
                      </li>
                    </ul>
                    <p v-if="item.diet.calorie.advice">{{ item.diet.calorie.advice }}</p>
                  </template>
                  <template v-else>
                    <div v-if="item.diet.calorie.target" class="basic-calorie-range">
                      <span>建议摄入区间</span>
                      <strong>{{ item.diet.calorie.target }}</strong>
                    </div>
                    <p v-if="item.diet.calorie.advice">{{ item.diet.calorie.advice }}</p>
                  </template>
                </div>
              </div>
              <div class="plan-day-card exercise-card">
                <div class="plan-day-head">
                  <strong>今日训练</strong>
                  <span>运动</span>
                </div>
                <div class="exercise-grid">
                  <p><span>类型</span><strong>{{ item.exercise?.type || '待生成' }}</strong></p>
                  <p><span>时长</span><strong>{{ item.exercise?.duration || '待生成' }}</strong></p>
                  <p><span>强度</span><strong>{{ item.exercise?.intensity || '待生成' }}</strong></p>
                </div>
                <div v-if="item.exercise?.items?.length" class="exercise-detail-list">
                  <h5>具体项目建议</h5>
                  <ul>
                    <li v-for="project in item.exercise.items" :key="`${item.day}-${project.name}`">
                      <span>{{ project.name }}</span>
                      <strong>{{ project.detail }}</strong>
                    </li>
                  </ul>
                  <p v-if="item.exercise.note">{{ item.exercise.note }}</p>
                </div>
              </div>
            </section>
          </div>

          <section v-if="vipItems.length" class="vip-detail-panel">
            <div class="vip-detail-title">
              <span><Medal /></span>
              <h4>VIP 详细建议</h4>
            </div>
            <div class="vip-detail-grid">
              <p v-for="item in vipItems" :key="item.label">
                <span>{{ item.label }}</span>{{ item.value }}
              </p>
            </div>
          </section>
        </section>
      </template>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { Bicycle, ForkSpoon, Medal } from '@element-plus/icons-vue'
import {
  formatPlanLevel,
  formatPlanType,
  getPlanDetail,
  getPlanHistory,
  normalizePlanDetail
} from '../api/modules/plan'

const loading = ref(false)
const plans = ref([])
const detail = ref()
const page = ref(1)
const size = ref(10)
const total = ref(0)
const filters = reactive({
  date: '',
  type: '',
  level: ''
})

const dateOptions = computed(() => [...new Set(plans.value.map((item) => item.createTime.slice(0, 10)))])
const typeOptions = computed(() => [...new Set(plans.value.map((item) => item.planType).filter(Boolean))])
const levelOptions = computed(() => [...new Set(plans.value.map((item) => item.planLevel).filter(Boolean))])
const detailContent = computed(() => detail.value?.planContent || {})
const detailLevelText = computed(() => formatPlanLevel(detail.value?.planLevel))
const detailTypeText = computed(() => formatPlanType(detail.value?.planType))
const dietPlan = computed(() => detailContent.value.dietPlan || [])
const exercisePlan = computed(() => detailContent.value.exercisePlan || [])
const isVipDetail = computed(() => detail.value?.planLevel === 'VIP')
const vipDetail = computed(() => (isVipDetail.value ? detailContent.value.vipDetail : undefined))
const filteredPlans = computed(() =>
  plans.value.filter((item) => {
    const dateMatch = !filters.date || item.createTime.startsWith(filters.date)
    const typeMatch = !filters.type || item.planType === filters.type
    const levelMatch = !filters.level || item.planLevel === filters.level
    return dateMatch && typeMatch && levelMatch
  })
)
const weeklyPlans = computed(() => {
  const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  const dietByDay = new Map(dietPlan.value.map((item) => [item.day, item]))
  const exerciseByDay = new Map(exercisePlan.value.map((item) => [item.day, item]))

  return days
    .filter((day) => dietByDay.has(day) || exerciseByDay.has(day))
    .map((day) => ({
      day,
      diet: dietByDay.get(day),
      exercise: exerciseByDay.get(day)
    }))
})
const vipItems = computed(() => {
  if (!vipDetail.value) {
    return []
  }

  const labels = {
    calorieEstimate: '热量估算',
    nutritionRatio: '营养比例',
    mealStrategy: '餐盘策略',
    trainingFocus: '训练重点',
    recoveryPlan: '恢复安排',
    hydrationTarget: '饮水目标',
    riskReminder: '风险提醒',
    weeklyReview: '每周复盘'
  }

  return Object.entries(labels)
    .filter(([key]) => vipDetail.value[key])
    .map(([key, label]) => ({
      label,
      value: vipDetail.value[key]
    }))
})

onMounted(loadHistory)

async function loadHistory() {
  loading.value = true
  try {
    const data = await getPlanHistory({ page: page.value, size: size.value })
    plans.value = [...data.records].sort((a, b) => b.createTime.localeCompare(a.createTime))
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

async function selectPlan(row) {
  detail.value = normalizePlanDetail(await getPlanDetail(row.id))
}

function formatTime(value) {
  return value ? value.replace('T', ' ') : ''
}

function formatCalorie(value) {
  if (value === undefined || value === null || value === '') {
    return ''
  }

  const text = String(value).trim()
  return /kcal|千卡|大卡/i.test(text) ? text : `${text} kcal`
}
</script>
