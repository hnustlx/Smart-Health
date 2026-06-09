<template>
  <section class="page-section weight-page">
    <div class="section-heading">
      <p>体重记录</p>
      <h2>记录体重并展示趋势折线</h2>
    </div>

    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="weight-entry-form">
      <div class="weight-field-card">
        <el-form-item label="体重 kg" prop="weight">
          <el-input-number v-model="form.weight" :min="20" :max="300" :precision="1" controls-position="right" />
        </el-form-item>
      </div>
      <div class="weight-field-card">
        <el-form-item label="记录日期" prop="recordDate">
          <el-date-picker v-model="form.recordDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" />
        </el-form-item>
      </div>
      <div class="weight-submit-card">
        <span>保存到趋势</span>
        <el-button type="primary" :loading="saving" @click="submit">添加记录</el-button>
      </div>
    </el-form>

    <div v-loading="loading" class="data-panel">
      <el-empty v-if="records.length === 0" description="暂无体重记录" />
      <template v-else>
        <div class="weight-trend">
          <div class="trend-summary">
            <div>
              <span>最新体重</span>
              <strong>{{ weightStats.latest }} kg</strong>
            </div>
            <div>
              <span>累计变化</span>
              <strong :class="{ 'is-down': weightStats.change < 0 }">{{ weightStats.changeText }}</strong>
            </div>
            <div>
              <span>记录次数</span>
              <strong>{{ records.length }} 次</strong>
            </div>
          </div>
          <div class="trend-chart">
            <svg viewBox="0 0 760 320" role="img" aria-label="体重趋势图">
              <defs>
                <linearGradient id="weightAreaGradient" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stop-color="#10b981" stop-opacity="0.26" />
                  <stop offset="100%" stop-color="#10b981" stop-opacity="0.02" />
                </linearGradient>
                <linearGradient id="weightLineGradient" x1="0" y1="0" x2="1" y2="0">
                  <stop offset="0%" stop-color="#0f766e" />
                  <stop offset="100%" stop-color="#ff5c8a" />
                </linearGradient>
              </defs>
              <g class="trend-grid">
                <line v-for="tick in yTicks" :key="tick.value" x1="64" x2="716" :y1="tick.y" :y2="tick.y" />
              </g>
              <g class="trend-axis">
                <line x1="64" y1="260" x2="716" y2="260" />
                <line x1="64" y1="38" x2="64" y2="260" />
              </g>
              <g class="trend-y-labels">
                <text v-for="tick in yTicks" :key="`label-${tick.value}`" x="48" :y="tick.y + 5">{{ tick.value }}kg</text>
              </g>
              <path class="trend-area-fill" :d="chartAreaPath" />
              <polyline class="trend-line" :points="trendPoints" />
              <g class="trend-points">
                <g v-for="point in chartSeries" :key="point.id" class="trend-point">
                  <circle :cx="point.x" :cy="point.y" r="6" />
                  <text :x="point.x" :y="point.y - 14">{{ point.weight }}kg</text>
                </g>
              </g>
              <g v-if="latestPoint" class="trend-latest">
                <line :x1="latestPoint.x" :x2="latestPoint.x" y1="44" y2="260" />
                <circle :cx="latestPoint.x" :cy="latestPoint.y" r="10" />
              </g>
              <g class="trend-x-labels">
                <text v-for="label in xLabels" :key="label.id" :x="label.x" y="292">{{ label.text }}</text>
              </g>
            </svg>
          </div>
        </div>
        <div class="table-filter-bar">
          <el-select v-model="filters.date" clearable placeholder="按日期筛选">
            <el-option v-for="date in dateOptions" :key="date" :label="date" :value="date" />
          </el-select>
          <el-select v-model="filters.weight" clearable placeholder="按体重筛选">
            <el-option v-for="weight in weightOptions" :key="weight" :label="`${weight} kg`" :value="weight" />
          </el-select>
          <span>共 {{ filteredRecords.length }} 条</span>
        </div>
        <el-table :data="filteredRecords" border>
          <el-table-column prop="recordDate" label="日期" />
          <el-table-column prop="weight" label="体重 kg" />
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button type="danger" link @click="remove(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addWeight, deleteWeight, getWeightHistory } from '../api/modules/weight'

const formRef = ref()
const loading = ref(false)
const saving = ref(false)
const records = ref([])
const filters = reactive({
  date: '',
  weight: ''
})

const form = reactive({
  weight: undefined,
  recordDate: new Date().toISOString().slice(0, 10)
})

const rules = {
  weight: [{ required: true, message: '请输入体重', trigger: 'blur' }],
  recordDate: [{ required: true, message: '请选择记录日期', trigger: 'change' }]
}

const chartSeries = computed(() => {
  const values = records.value.map((item) => Number(item.weight))
  const min = Math.min(...values)
  const max = Math.max(...values)
  const padding = Math.max((max - min) * 0.18, 0.4)
  const chartMin = min - padding
  const chartMax = max + padding
  const range = chartMax - chartMin || 1

  return records.value.map((item, index) => {
    const x = records.value.length === 1 ? 390 : 64 + (index * 652) / (records.value.length - 1)
    const y = 260 - ((Number(item.weight) - chartMin) / range) * 222
    return {
      ...item,
      x,
      y,
      weight: Number(item.weight).toFixed(1)
    }
  })
})

const trendPoints = computed(() => {
  if (records.value.length === 1) {
    const point = chartSeries.value[0]
    return `64,${point.y} 716,${point.y}`
  }
  return chartSeries.value.map((point) => `${point.x},${point.y}`).join(' ')
})

const chartAreaPath = computed(() => {
  const points = chartSeries.value
  if (points.length === 1) {
    return `M64,${points[0].y} L716,${points[0].y} L716,260 L64,260 Z`
  }
  return `M${points.map((point) => `${point.x},${point.y}`).join(' L')} L716,260 L64,260 Z`
})

const yTicks = computed(() => {
  const values = records.value.map((item) => Number(item.weight))
  const min = Math.min(...values)
  const max = Math.max(...values)
  const padding = Math.max((max - min) * 0.18, 0.4)
  const chartMin = min - padding
  const chartMax = max + padding
  return [0, 1, 2, 3, 4].map((step) => {
    const ratio = step / 4
    return {
      value: (chartMax - (chartMax - chartMin) * ratio).toFixed(1),
      y: 38 + ratio * 222
    }
  })
})

const xLabels = computed(() => {
  if (chartSeries.value.length <= 3) {
    return chartSeries.value.map((point) => ({
      id: point.id,
      x: point.x,
      text: point.recordDate.slice(5)
    }))
  }
  const first = chartSeries.value[0]
  const middle = chartSeries.value[Math.floor(chartSeries.value.length / 2)]
  const last = chartSeries.value[chartSeries.value.length - 1]
  return [first, middle, last].map((point) => ({
    id: point.id,
    x: point.x,
    text: point.recordDate.slice(5)
  }))
})

const latestPoint = computed(() => chartSeries.value[chartSeries.value.length - 1])

const weightStats = computed(() => {
  const first = Number(records.value[0].weight)
  const latest = Number(records.value[records.value.length - 1].weight)
  const change = Number((latest - first).toFixed(1))
  return {
    latest: latest.toFixed(1),
    change,
    changeText: `${change > 0 ? '+' : ''}${change.toFixed(1)} kg`
  }
})

const dateOptions = computed(() => [...new Set(records.value.map((item) => item.recordDate))])
const weightOptions = computed(() => [...new Set(records.value.map((item) => Number(item.weight).toFixed(1)))])
const filteredRecords = computed(() =>
  records.value.filter((item) => {
    const dateMatch = !filters.date || item.recordDate === filters.date
    const weightMatch = !filters.weight || Number(item.weight).toFixed(1) === filters.weight
    return dateMatch && weightMatch
  })
)

onMounted(loadRecords)

async function loadRecords() {
  loading.value = true
  try {
    const data = await getWeightHistory()
    records.value = [...data].sort((a, b) => a.recordDate.localeCompare(b.recordDate))
  } finally {
    loading.value = false
  }
}

async function submit() {
  await formRef.value.validate()
  saving.value = true
  try {
    await addWeight(form)
    ElMessage.success('体重记录已添加')
    await loadRecords()
  } finally {
    saving.value = false
  }
}

async function remove(id) {
  await ElMessageBox.confirm('确认删除这条体重记录？', '删除确认', { type: 'warning' })
  await deleteWeight(id)
  ElMessage.success('体重记录已删除')
  await loadRecords()
}
</script>
