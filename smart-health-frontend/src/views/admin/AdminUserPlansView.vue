<template>
  <section ref="pageRoot" class="admin-page">
    <div class="admin-section-heading">
      <p>计划记录</p>
      <h2>AI 计划记录管理</h2>
    </div>
    <el-table v-loading="loading" :data="plans" border>
      <el-table-column prop="id" label="ID" width="100" />
      <el-table-column label="类型" width="140">
        <template #default="{ row }">{{ formatPlanType(row.planType) }}</template>
      </el-table-column>
      <el-table-column label="级别" width="140">
        <template #default="{ row }">{{ formatPlanLevel(row.planLevel) }}</template>
      </el-table-column>
      <el-table-column prop="trendSummary" label="体重趋势" />
      <el-table-column prop="createTime" label="创建时间" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button type="primary" link @click="viewDetail(row.id)">查看</el-button>
          <el-button type="danger" link @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="detailVisible" title="AI 计划详情" width="960px" destroy-on-close>
      <div ref="detailRef" v-loading="detailLoading" class="admin-plan-detail">
        <template v-if="detail">
          <el-descriptions border :column="2">
            <el-descriptions-item label="计划 ID">{{ detail.id }}</el-descriptions-item>
            <el-descriptions-item label="级别">{{ formatPlanLevel(detail.planLevel) }}</el-descriptions-item>
            <el-descriptions-item label="类型">{{ formatPlanType(detail.planType) }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatTime(detail.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="体重趋势" :span="2">{{ detail.trendSummary || '-' }}</el-descriptions-item>
          </el-descriptions>

          <div class="admin-plan-rendered">
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

            <el-empty v-if="weeklyPlans.length === 0" description="暂无可渲染的饮食与运动计划" />
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
          </div>
        </template>
      </div>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Bicycle, ForkSpoon, Medal } from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'
import { gsap } from 'gsap'
import { deleteUserPlan, getUserPlans } from '../../api/modules/admin'
import { formatPlanLevel, formatPlanType, getPlanDetail, normalizePlanDetail } from '../../api/modules/plan'

const route = useRoute()
const loading = ref(false)
const detailLoading = ref(false)
const detailVisible = ref(false)
const plans = ref([])
const detail = ref()
const pageRoot = ref()
const detailRef = ref()
const detailContent = computed(() => detail.value?.planContent || {})
const dietPlan = computed(() => detailContent.value.dietPlan || [])
const exercisePlan = computed(() => detailContent.value.exercisePlan || [])
const isVipDetail = computed(() => detail.value?.planLevel === 'VIP')
const vipDetail = computed(() => (isVipDetail.value ? detailContent.value.vipDetail : undefined))
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
let ctx

onMounted(() => {
  loadPlans()
  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
    return
  }
  ctx = gsap.context(() => {
    gsap.from('.admin-section-heading, .el-table', {
      autoAlpha: 0,
      y: 18,
      duration: 0.5,
      stagger: 0.08,
      ease: 'power2.out'
    })
  }, pageRoot.value)
})

async function loadPlans() {
  loading.value = true
  try {
    plans.value = await getUserPlans(route.params.id)
  } finally {
    loading.value = false
  }
}

async function viewDetail(id) {
  detailVisible.value = true
  detailLoading.value = true
  try {
    detail.value = normalizePlanDetail(await getPlanDetail(id))
    await nextTick()
    animateDetail()
  } finally {
    detailLoading.value = false
  }
}

async function remove(id) {
  await ElMessageBox.confirm('确认删除这条 AI 计划？', '删除确认', { type: 'warning' })
  await deleteUserPlan(id)
  ElMessage.success('AI 计划已删除')
  await loadPlans()
}

function formatTime(value) {
  return value ? value.replace('T', ' ') : '-'
}

function formatCalorie(value) {
  if (value === undefined || value === null || value === '') {
    return ''
  }

  const text = String(value).trim()
  return /kcal|千卡|大卡/i.test(text) ? text : `${text} kcal`
}

function animateDetail() {
  if (!detailRef.value || window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
    return
  }
  gsap.fromTo(
    detailRef.value.children,
    { autoAlpha: 0, y: 14 },
    { autoAlpha: 1, y: 0, duration: 0.42, stagger: 0.06, ease: 'power2.out' }
  )
}

onBeforeUnmount(() => {
  ctx?.revert()
})
</script>
