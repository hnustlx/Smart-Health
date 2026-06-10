<template>
  <section ref="configRoot" class="page-section">
    <div class="section-heading">
      <p>AI Config</p>
      <h2>AI 配置</h2>
    </div>

    <div v-loading="loading" class="data-panel ai-config-panel">
      <div ref="statusRef" class="ai-config-status-strip" :class="`provider-${form.provider.toLowerCase()}`">
        <span>{{ providerLabel }}</span>
        <strong>{{ providerSummary }}</strong>
      </div>

      <el-form :model="form" label-position="top" class="form-grid two">
        <el-form-item label="模式">
          <el-select v-model="form.provider">
            <el-option label="默认 DeepSeek" value="DEFAULT" />
            <el-option label="自定义 API Key" value="CUSTOM" />
            <el-option label="服务器本地 Ollama" value="LOCAL" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.provider === 'CUSTOM'" label="模型">
          <el-input v-model.trim="form.model" placeholder="deepseek-chat" />
        </el-form-item>
        <el-form-item v-if="form.provider === 'CUSTOM'" label="API Key">
          <el-input v-model.trim="form.apiKey" type="password" show-password placeholder="sk-..." />
        </el-form-item>
        <el-form-item v-if="form.provider === 'CUSTOM'" label="API 地址">
          <el-input v-model.trim="form.apiUrl" placeholder="https://api.deepseek.com/v1/chat/completions" />
        </el-form-item>
      </el-form>

      <el-alert
        v-if="form.provider === 'LOCAL' && !isVip"
        title="服务器本地 Ollama 模式仅 VIP 用户可用"
        type="warning"
        :closable="false"
        show-icon
      />
      <el-alert
        v-else-if="form.provider === 'LOCAL'"
        title="保存后将使用后端服务器的 Ollama 模型，模型由服务器配置"
        type="info"
        :closable="false"
        show-icon
      />

      <div v-if="current.apiKey" class="config-status">
        <span>当前 Key</span>
        <strong>{{ current.apiKey }}</strong>
      </div>

      <div class="form-actions">
        <el-button type="primary" :loading="saving" @click="save">保存配置</el-button>
        <el-button :loading="resetting" @click="reset">恢复默认</el-button>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { gsap } from 'gsap'
import { getAiConfig, resetAiConfig, saveAiConfig } from '../api/modules/aiConfig'
import { getUser } from '../utils/auth'

const loading = ref(false)
const saving = ref(false)
const resetting = ref(false)
const configRoot = ref()
const statusRef = ref()
const user = ref(getUser())
const current = reactive({
  apiKey: ''
})
const form = reactive({
  provider: 'DEFAULT',
  apiKey: '',
  apiUrl: '',
  model: 'deepseek-chat'
})
const isVip = computed(() => user.value?.role === 'VIP' || user.value?.role === 'ADMIN')
const providerLabel = computed(() => {
  const labels = {
    DEFAULT: '默认 DeepSeek',
    CUSTOM: '自定义 API Key',
    LOCAL: '服务器本地 Ollama'
  }
  return labels[form.provider] || '默认 DeepSeek'
})
const providerSummary = computed(() => {
  if (form.provider === 'CUSTOM') {
    return current.apiKey ? '已保存脱敏 Key，可继续沿用或重新填写' : '保存前需要填写 API Key'
  }
  if (form.provider === 'LOCAL') {
    return isVip.value ? 'VIP 可用，由后端服务器调用已配置的 Ollama 模型' : '当前身份不可用'
  }
  return '使用服务端默认 DeepSeek 配置'
})
let ctx

onMounted(loadConfig)

async function loadConfig() {
  loading.value = true
  try {
    const data = await getAiConfig()
    Object.assign(form, {
      provider: data.provider || 'DEFAULT',
      apiKey: '',
      apiUrl: data.apiUrl || '',
      model: data.model || 'deepseek-chat'
    })
    current.apiKey = data.apiKey || ''
  } finally {
    loading.value = false
  }
}

async function save() {
  if (form.provider === 'CUSTOM' && !form.apiKey && !current.apiKey) {
    ElMessage.warning('请输入 API Key')
    return
  }
  if (form.provider === 'LOCAL' && !isVip.value) {
    ElMessage.warning('服务器本地 Ollama 模式仅 VIP 用户可用')
    return
  }
  saving.value = true
  try {
    const payload = {
      provider: form.provider,
      apiKey: form.provider === 'CUSTOM' ? form.apiKey || undefined : undefined,
      apiUrl: form.provider === 'CUSTOM' ? form.apiUrl || undefined : undefined,
      model: form.provider === 'CUSTOM' ? form.model || undefined : undefined
    }
    const data = await saveAiConfig(payload)
    current.apiKey = data.apiKey || ''
    form.apiKey = ''
    ElMessage.success('AI 配置已保存')
    animateStatus()
  } finally {
    saving.value = false
  }
}

async function reset() {
  resetting.value = true
  try {
    await resetAiConfig()
    ElMessage.success('已恢复默认 AI 配置')
    await loadConfig()
    animateStatus()
  } finally {
    resetting.value = false
  }
}

function animateStatus() {
  if (!statusRef.value || window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
    return
  }
  gsap.fromTo(
    statusRef.value,
    { y: -8, scale: 0.98 },
    { y: 0, scale: 1, duration: 0.42, ease: 'back.out(1.6)', overwrite: 'auto' }
  )
}

watch(() => form.provider, animateStatus)

onMounted(() => {
  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
    return
  }
  ctx = gsap.context(() => {
    gsap.from('.ai-config-panel > *', {
      autoAlpha: 0,
      y: 14,
      duration: 0.48,
      stagger: 0.06,
      ease: 'power2.out'
    })
  }, configRoot.value)
})

onBeforeUnmount(() => {
  ctx?.revert()
})
</script>
