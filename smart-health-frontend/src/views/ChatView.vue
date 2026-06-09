<template>
  <section class="page-section">
    <div class="section-heading">
      <p>智能问答</p>
      <h2>VIP 专属健康问答</h2>
    </div>
    <el-alert
      v-if="user?.role !== 'VIP'"
      title="智能健康问答为 VIP 专属功能，开通 VIP 后可使用。"
      type="warning"
      show-icon
      :closable="false"
    />
    <div v-else class="chat-panel">
      <div ref="chatRoot" class="chat-messages">
        <div v-if="messages.length === 0" class="chat-assistant-empty">
          <div ref="chatAssistantWrap" class="chat-assistant-wrap">
            <HealthAssistant state="idle" />
          </div>
          <h3>你的健康小助手已就绪</h3>
          <p>输入饮食、运动或健康管理问题，我会结合知识库给出建议。</p>
        </div>
        <div v-for="(message, index) in messages" :key="index" :class="['chat-turn', message.role]">
          <div v-if="message.role === 'assistant'" class="chat-avatar">
            <HealthAssistant state="idle" />
          </div>
          <div class="chat-message">
            <strong>{{ message.role === 'user' ? '我' : 'AI 健康助手' }}</strong>
            <p>{{ message.content }}</p>
            <div v-if="message.references?.length" class="reference-list">
              <el-tag v-for="item in message.references" :key="`${index}-${item.category}-${item.title}`">
                {{ item.category }} / {{ item.title }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" class="chat-form chat-composer" @submit.prevent="submit">
        <div class="chat-composer-head">
          <div>
            <span>Ask Smart Health</span>
            <strong>描述你的饮食、运动或健康管理问题</strong>
          </div>
          <div class="chat-quick-actions">
            <button type="button" @click="fillQuestion('减脂期间晚餐怎么吃？')">减脂晚餐</button>
            <button type="button" @click="fillQuestion('一周运动计划怎么安排？')">运动安排</button>
          </div>
        </div>
        <el-form-item prop="question">
          <el-input v-model="form.question" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="例如：减脂期间晚餐怎么吃？" />
        </el-form-item>
        <div class="chat-composer-footer">
          <span>回答仅供健康管理参考</span>
          <el-button type="primary" :loading="loading" @click="submit">发送问题</el-button>
        </div>
      </el-form>
    </div>
  </section>
</template>

<script setup>
import { onMounted, onUnmounted, reactive, ref } from 'vue'
import { gsap } from 'gsap'
import { getUser } from '../utils/auth'
import { askHealthQuestion } from '../api/modules/chat'
import HealthAssistant from '../components/HealthAssistant.vue'

const user = getUser()
const formRef = ref()
const chatRoot = ref()
const chatAssistantWrap = ref()
const loading = ref(false)
const messages = ref([])
let ctx
const form = reactive({
  question: ''
})

const rules = {
  question: [{ required: true, message: '请输入问题', trigger: 'blur' }]
}

onMounted(() => {
  const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  if (reduceMotion || !chatRoot.value) {
    return
  }

  ctx = gsap.context(() => {
    gsap.from('.chat-assistant-empty', { autoAlpha: 0, y: 18, duration: 0.62, ease: 'power3.out' })
    gsap.to('.chat-assistant-wrap', {
      y: -8,
      duration: 2.4,
      ease: 'sine.inOut',
      repeat: -1,
      yoyo: true
    })
  }, chatRoot.value)
})

onUnmounted(() => {
  ctx?.revert()
})

async function submit() {
  await formRef.value.validate()
  const question = form.question
  messages.value.push({ role: 'user', content: question })
  form.question = ''
  loading.value = true

  try {
    const data = await askHealthQuestion({ question })
    messages.value.push({
      role: 'assistant',
      content: data.answer,
      references: data.references || []
    })
  } finally {
    loading.value = false
  }
}

function fillQuestion(question) {
  form.question = question
}
</script>
