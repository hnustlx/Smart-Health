<template>
  <section class="page-section">
    <div class="section-heading">
      <p>AI 计划</p>
      <h2>生成饮食与运动建议</h2>
    </div>

    <div class="plan-toolbar">
      <div class="count-card">
        <span>今日生成次数</span>
        <strong>{{ countText }}</strong>
      </div>
      <el-button type="primary" size="large" :loading="generating" @click="generate">
        生成 AI 计划
      </el-button>
    </div>

    <el-alert
      v-if="!isVip"
      title="当前为普通用户，仅展示基础饮食与运动建议；VIP 详细建议和智能问答需升级后使用。"
      type="info"
      :closable="false"
      show-icon
    />

    <el-empty v-if="!plan" description="生成后将在这里展示饮食与运动建议" />
    <article v-else class="plan-result">
      <div class="result-heading">
        <div>
          <p>{{ plan.planLevel }} / {{ plan.planType }}</p>
          <h3>本次个性化计划</h3>
        </div>
        <span>{{ formatTime(plan.createTime) }}</span>
      </div>

      <el-alert v-if="plan.trendSummary" :title="plan.trendSummary" type="success" :closable="false" show-icon />

      <div class="plan-pair-head">
        <div class="track-heading">
          <span class="track-icon"><ForkSpoon /></span>
          <div>
            <p>Nutrition</p>
            <h4>饮食计划</h4>
          </div>
        </div>
        <div class="track-heading exercise-track">
          <span class="track-icon"><Bicycle /></span>
          <div>
            <p>Training</p>
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
              <strong><span class="day-emoji">{{ item.dietEmoji }}</span>今日餐单</strong>
              <span>饮食</span>
            </div>
            <div class="meal-list">
              <p><span>🌤 早餐</span>{{ item.diet?.breakfast || '待生成' }}</p>
              <p><span>☀️ 午餐</span>{{ item.diet?.lunch || '待生成' }}</p>
              <p><span>🌙 晚餐</span>{{ item.diet?.dinner || '待生成' }}</p>
              <p><span>🍵 加餐</span>{{ item.diet?.snack || '待生成' }}</p>
            </div>
            <div v-if="item.diet?.calorie" class="calorie-insight">
              <template v-if="isVip">
                <div>
                  <span>🔥 总热量</span>
                  <strong>{{ item.diet.calorie.total }}</strong>
                </div>
                <div>
                  <span>🎯 目标区间</span>
                  <strong>{{ item.diet.calorie.target }}</strong>
                </div>
                <ul v-if="item.diet.calorie.foods?.length" class="calorie-food-list">
                  <li v-for="food in item.diet.calorie.foods" :key="`${item.day}-${food.name}`">
                    <span>{{ food.name }}</span>
                    <strong>{{ food.kcal }}</strong>
                  </li>
                </ul>
                <p>{{ item.diet.calorie.advice }}</p>
              </template>
              <template v-else>
                <div class="basic-calorie-range">
                  <span>🎯 建议摄入区间</span>
                  <strong>{{ item.diet.calorie.target }}</strong>
                </div>
                <p>普通用户先按建议区间控制总摄入；升级 VIP 后可查看每种食物热量和总热量明细。</p>
              </template>
            </div>
          </div>
          <div class="plan-day-card exercise-card">
            <div class="plan-day-head">
              <strong><span class="day-emoji">{{ item.exerciseEmoji }}</span>今日训练</strong>
              <span>运动</span>
            </div>
            <div class="exercise-grid">
              <p><span>🎯 类型</span><strong>{{ item.exercise?.type || '待生成' }}</strong></p>
              <p><span>⏱ 时长</span><strong>{{ item.exercise?.duration || '待生成' }}</strong></p>
              <p><span>🔥 强度</span><strong>{{ item.exercise?.intensity || '待生成' }}</strong></p>
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

      <section v-if="vipDetail" class="vip-detail-panel">
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

      <section v-if="references.length" class="reference-list">
        <div class="reference-heading">
          <span><Collection /></span>
          <h4>参考知识</h4>
        </div>
        <div class="reference-tags">
          <el-tag v-for="item in references" :key="`${item.category}-${item.title}`">
            {{ item.category }} / {{ item.title }}
          </el-tag>
        </div>
      </section>
    </article>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Bicycle, Collection, ForkSpoon, Medal } from '@element-plus/icons-vue'
import { generatePlan, getGenerateCount, normalizeGenerateCount } from '../api/modules/plan'
import { getUser } from '../utils/auth'

const generating = ref(false)
const count = ref()
const plan = ref()
const user = getUser()
const isVip = computed(() => user?.role === 'VIP')

const countText = computed(() => {
  if (!count.value) {
    return '待加载'
  }
  return `${count.value.used}/${count.value.limit}，剩余 ${count.value.remaining}`
})
const dietPlan = computed(() => plan.value?.planContent?.dietPlan || [])
const exercisePlan = computed(() => plan.value?.planContent?.exercisePlan || [])
const vipDetail = computed(() => (isVip.value ? plan.value?.planContent?.vipDetail : undefined))
const references = computed(() => plan.value?.references || [])
const weeklyPlans = computed(() => {
  const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  const dietByDay = new Map(dietPlan.value.map((item) => [item.day, item]))
  const exerciseByDay = new Map(exercisePlan.value.map((item) => [item.day, item]))
  const dietEmojis = ['🥣', '🥑', '🍳', '🫐', '🍤', '🍗', '🥗']
  const exerciseEmojis = ['🚶', '🏋️', '💪', '🧘', '🚴', '🏃', '🌿']

  return days
    .filter((day) => dietByDay.has(day) || exerciseByDay.has(day))
    .map((day, index) => ({
      day,
      diet: dietByDay.get(day),
      exercise: exerciseByDay.get(day),
      dietEmoji: dietEmojis[index],
      exerciseEmoji: exerciseEmojis[index]
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

onMounted(loadCount)

async function loadCount() {
  count.value = normalizeGenerateCount(await getGenerateCount())
}

async function generate() {
  generating.value = true
  try {
    plan.value = await generatePlan()
    ElMessage.success('AI 计划已生成')
    await loadCount()
  } finally {
    generating.value = false
  }
}

function formatTime(value) {
  return value ? value.replace('T', ' ') : ''
}
</script>
