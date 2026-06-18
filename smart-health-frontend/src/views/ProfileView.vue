<template>
  <section class="profile-wizard-page">
    <div class="profile-wizard-hero">
      <div>
        <p>健康档案</p>
        <h2>建立你的健康画像</h2>
        <span>完成三步建档后，AI 计划会基于这些信息生成更贴合目标的建议。</span>
      </div>
      <div class="profile-progress-badge">
        <strong>{{ profileCompleted ? '3/3' : `${activeStep + 1}/3` }}</strong>
        <small>{{ profileCompleted ? '已完成' : steps[activeStep].title }}</small>
      </div>
    </div>

    <el-form ref="formRef" v-loading="loading" :model="form" :rules="rules" label-position="top" class="profile-wizard-card">
      <el-steps :active="stepActive" finish-status="success" align-center>
        <el-step v-for="step in steps" :key="step.title" :title="step.title" :description="step.description" />
      </el-steps>

      <div class="profile-step-panel">
        <section ref="portraitPanelRef" v-if="showProfile" class="profile-portrait-panel">
          <div class="profile-step-copy">
            <span>Health Profile</span>
            <h3>你的健康画像</h3>
            <p>这些信息会作为 AI 计划、体重趋势和健康问答的基础参考。</p>
          </div>
          <div class="profile-portrait-grid">
            <div>
              <span>基础信息</span>
              <strong>{{ form.age }} 岁 / {{ form.gender }} / {{ form.height }}cm</strong>
            </div>
            <div>
              <span>当前体重</span>
              <strong>{{ form.weight }} kg</strong>
            </div>
            <div>
              <span>生活方式</span>
              <strong>{{ form.activityLevel }}活动</strong>
            </div>
            <div>
              <span>健康目标</span>
              <strong>{{ form.goal }}</strong>
            </div>
            <div class="wide">
              <span>饮食偏好</span>
              <strong>{{ form.dietPreference || '暂无特别偏好' }}</strong>
            </div>
          </div>
        </section>

        <section v-else-if="activeStep === 0" class="profile-step-content">
          <div class="profile-step-copy">
            <span>Step 01</span>
            <h3>先确认基础身体信息</h3>
            <p>这些数据会用于计算计划强度和饮食建议的基础范围。</p>
          </div>
          <div class="form-grid">
            <el-form-item label="年龄" prop="age">
              <el-input-number v-model="form.age" :min="1" :max="120" controls-position="right" />
            </el-form-item>
            <el-form-item label="性别" prop="gender">
              <el-select v-model="form.gender" placeholder="请选择">
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
              </el-select>
            </el-form-item>
            <el-form-item label="身高 cm" prop="height">
              <el-input-number v-model="form.height" :min="80" :max="240" :precision="1" controls-position="right" />
            </el-form-item>
            <el-form-item label="当前体重 kg" prop="weight">
              <el-input-number v-model="form.weight" :min="20" :max="300" :precision="1" controls-position="right" />
            </el-form-item>
          </div>
        </section>

        <section v-else-if="activeStep === 1" class="profile-step-content">
          <div class="profile-step-copy">
            <span>Step 02</span>
            <h3>记录你的生活方式</h3>
            <p>活动水平和饮食偏好会影响运动频率、餐食搭配和执行难度。</p>
          </div>
          <el-form-item label="活动水平" prop="activityLevel">
            <el-radio-group v-model="form.activityLevel" class="choice-grid">
              <el-radio-button value="较低">较低</el-radio-button>
              <el-radio-button value="中等">中等</el-radio-button>
              <el-radio-button value="较高">较高</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="饮食偏好" prop="dietPreference">
            <el-input v-model="form.dietPreference" placeholder="例如：少油少糖、低盐、偏素食" />
          </el-form-item>
        </section>

        <section v-else class="profile-step-content">
          <div class="profile-step-copy">
            <span>Step 03</span>
            <h3>选择本阶段健康目标</h3>
            <p>目标会决定 AI 计划更偏向减脂、增肌、控糖或稳定管理。</p>
          </div>
          <el-form-item label="健康目标" prop="goal">
            <el-radio-group v-model="form.goal" class="choice-grid">
              <el-radio-button value="减脂">减脂</el-radio-button>
              <el-radio-button value="增肌">增肌</el-radio-button>
              <el-radio-button value="控糖">控糖</el-radio-button>
              <el-radio-button value="保持健康">保持健康</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <div class="profile-review-card">
            <span>即将保存的健康画像</span>
            <strong>{{ reviewText }}</strong>
          </div>
        </section>
      </div>

      <div v-if="profileCompleted" class="profile-wizard-actions">
        <el-button @click="editProfile">继续编辑</el-button>
        <el-button type="primary" @click="viewProfile">查看健康画像</el-button>
      </div>

      <div v-else class="profile-wizard-actions">
        <el-button :disabled="activeStep === 0 || saving" @click="prevStep">上一步</el-button>
        <el-button v-if="activeStep < steps.length - 1" type="primary" @click="nextStep">下一步</el-button>
        <el-button v-else type="primary" :loading="saving" @click="submit">
          {{ profileExists ? '保存健康画像' : '完成建档' }}
        </el-button>
      </div>
    </el-form>
  </section>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createProfile, getProfile, updateProfile } from '../api/modules/profile'

const formRef = ref()
const loading = ref(false)
const saving = ref(false)
const profileExists = ref(false)
const profileCompleted = ref(false)
const showProfile = ref(false)
const activeStep = ref(0)
const portraitPanelRef = ref()

const steps = [
  { title: '身体信息', description: '年龄、性别、身高、体重' },
  { title: '生活方式', description: '活动水平与饮食偏好' },
  { title: '健康目标', description: '确认计划方向' }
]

const form = reactive({
  age: undefined,
  gender: '',
  height: undefined,
  weight: undefined,
  activityLevel: '',
  dietPreference: '',
  goal: ''
})

const rules = {
  age: [{ required: true, message: '请输入年龄', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  height: [{ required: true, message: '请输入身高', trigger: 'blur' }],
  weight: [{ required: true, message: '请输入体重', trigger: 'blur' }],
  activityLevel: [{ required: true, message: '请选择活动水平', trigger: 'change' }],
  goal: [{ required: true, message: '请选择健康目标', trigger: 'change' }]
}

const stepFields = [
  ['age', 'gender', 'height', 'weight'],
  ['activityLevel'],
  ['goal']
]

const stepActive = computed(() => (profileCompleted.value ? steps.length : activeStep.value))

const reviewText = computed(() => {
  const body = form.age && form.height && form.weight ? `${form.age} 岁 / ${form.height}cm / ${form.weight}kg` : '身体信息待完善'
  const lifestyle = form.activityLevel ? `${form.activityLevel}活动` : '活动水平待选择'
  const goal = form.goal || '目标待选择'
  return `${body}，${lifestyle}，当前目标：${goal}`
})

onMounted(loadProfile)

async function loadProfile() {
  loading.value = true
  try {
    const profile = await getProfile()
    Object.assign(form, {
      age: profile.age,
      gender: profile.gender,
      height: profile.height,
      weight: profile.weight,
      activityLevel: profile.activityLevel,
      dietPreference: profile.dietPreference || '',
      goal: profile.goal
    })
    profileExists.value = true
    profileCompleted.value = true
    showProfile.value = true
    activeStep.value = steps.length - 1
  } catch (error) {
    if (error.response?.status !== 404 && error.code !== 404) {
      console.warn(error)
    }
  } finally {
    loading.value = false
  }
}

async function submit() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (profileExists.value) {
      await updateProfile(form)
      ElMessage.success('健康档案已更新')
    } else {
      await createProfile(form)
      profileExists.value = true
      ElMessage.success('健康档案已创建')
    }
    profileCompleted.value = true
    showProfile.value = true
  } finally {
    saving.value = false
  }
}

async function nextStep() {
  await formRef.value.validateField(stepFields[activeStep.value])
  activeStep.value += 1
}

function prevStep() {
  activeStep.value -= 1
}

function editProfile() {
  showProfile.value = false
  profileCompleted.value = false
  activeStep.value = steps.length - 1
}

function viewProfile() {
  showProfile.value = true
  nextTick(() => {
    portraitPanelRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  })
}
</script>
