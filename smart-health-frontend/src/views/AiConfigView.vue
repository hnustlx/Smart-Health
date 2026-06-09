<template>
  <section class="page-section">
    <div class="section-heading">
      <p>AI Config</p>
      <h2>AI 配置</h2>
    </div>

    <div v-loading="loading" class="data-panel ai-config-panel">
      <el-form :model="form" label-position="top" class="form-grid two">
        <el-form-item label="模式">
          <el-select v-model="form.provider">
            <el-option label="默认 DeepSeek" value="DEFAULT" />
            <el-option label="自定义 API Key" value="CUSTOM" />
            <el-option label="本地 Ollama" value="LOCAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="模型">
          <el-input v-model.trim="form.model" placeholder="deepseek-chat" />
        </el-form-item>
        <el-form-item v-if="form.provider === 'CUSTOM'" label="API Key">
          <el-input v-model.trim="form.apiKey" type="password" show-password placeholder="sk-..." />
        </el-form-item>
        <el-form-item v-if="form.provider === 'CUSTOM'" label="API 地址">
          <el-input v-model.trim="form.apiUrl" placeholder="https://api.deepseek.com/v1/chat/completions" />
        </el-form-item>
      </el-form>

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
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getAiConfig, resetAiConfig, saveAiConfig } from '../api/modules/aiConfig'

const loading = ref(false)
const saving = ref(false)
const resetting = ref(false)
const current = reactive({
  apiKey: ''
})
const form = reactive({
  provider: 'DEFAULT',
  apiKey: '',
  apiUrl: '',
  model: 'deepseek-chat'
})

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
  saving.value = true
  try {
    const payload = {
      provider: form.provider,
      apiKey: form.provider === 'CUSTOM' ? form.apiKey || undefined : undefined,
      apiUrl: form.provider === 'CUSTOM' ? form.apiUrl || undefined : undefined,
      model: form.model || undefined
    }
    const data = await saveAiConfig(payload)
    current.apiKey = data.apiKey || ''
    form.apiKey = ''
    ElMessage.success('AI 配置已保存')
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
  } finally {
    resetting.value = false
  }
}
</script>
