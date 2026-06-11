<template>
  <section class="page-section">
    <div class="section-heading chat-heading">
      <div>
        <p>智能问答</p>
        <h2>AI 智能问答</h2>
      </div>
    </div>
    <el-alert
      v-if="!['VIP', 'ADMIN'].includes(user?.role)"
      title="智能健康问答为 VIP 和管理员专属功能。"
      type="warning"
      show-icon
      :closable="false"
    />
    <div v-else class="chat-panel chat-workspace">
      <div class="chat-main">
        <div ref="chatRoot" class="chat-messages">
          <div v-if="messages.length === 0" class="chat-assistant-empty">
            <div ref="chatAssistantWrap" class="chat-assistant-wrap">
              <HealthAssistant state="idle" />
            </div>
            <h3>青芽已就绪</h3>
            <p>输入饮食、运动或健康管理问题，我会结合知识库给出建议。</p>
          </div>
          <div v-for="(message, index) in messages" :key="index" :class="['chat-turn', message.role]">
            <div v-if="message.role === 'assistant'" class="chat-avatar">
              <HealthAssistant state="idle" />
            </div>
            <div class="chat-message">
              <strong>{{ message.role === 'user' ? '我' : '青芽' }}</strong>
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
              <span>Ask 青芽</span>
              <strong>描述你的饮食、运动或健康管理问题</strong>
            </div>
            <div class="chat-quick-actions">
              <button class="veggie-chip veggie-chip--carrot" type="button" @click="fillQuestion('减脂期间晚餐怎么吃？')">
                <span class="veggie-icon veggie-icon--carrot" aria-hidden="true"></span>
                减脂晚餐
              </button>
              <button class="veggie-chip veggie-chip--sprout" type="button" @click="fillQuestion('一周运动计划怎么安排？')">
                <span class="veggie-icon veggie-icon--sprout" aria-hidden="true"></span>
                运动安排
              </button>
            </div>
          </div>
          <el-form-item prop="question">
            <el-input v-model="form.question" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="例如：减脂期间晚餐怎么吃？" @keydown.enter.exact.prevent="submit" />
          </el-form-item>
          <div class="chat-composer-footer">
            <span>回答仅供健康管理参考</span>
            <div class="chat-submit-actions">
              <el-button class="chat-new-button veggie-button" plain @click="startNewConversation">
                <span class="veggie-icon veggie-icon--broccoli" aria-hidden="true"></span>
                新开对话
              </el-button>
              <el-button class="veggie-button" type="primary" :loading="loading" @click="submit">
                <span class="veggie-icon veggie-icon--leaf" aria-hidden="true"></span>
                发送问题
              </el-button>
            </div>
          </div>
        </el-form>
      </div>

      <aside class="chat-history-panel">
        <div class="chat-history-head">
          <div>
            <span>历史会话</span>
            <strong>{{ conversations.length }} 条记录</strong>
          </div>
        </div>
        <div class="chat-history-list">
          <button
            v-for="conversation in conversations"
            :key="conversation.id"
            type="button"
            :class="['chat-history-item', { active: conversation.id === activeConversationId }]"
            @click="selectConversation(conversation.id)"
          >
            <strong>{{ conversation.title }}</strong>
            <span>{{ formatConversationTime(conversation.updatedAt) }}</span>
            <small>{{ conversation.messages.length ? `${conversation.messages.length} 条消息` : '新对话' }}</small>
          </button>
        </div>
      </aside>
    </div>
  </section>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, reactive, ref } from 'vue'
import { gsap } from 'gsap'
import { getUser } from '../utils/auth'
import { askHealthQuestion } from '../api/modules/chat'
import HealthAssistant from '../components/HealthAssistant.vue'

const user = getUser()
const storageKey = `smart-health-chat-conversations-${user?.userId || user?.username || 'guest'}`
const formRef = ref()
const chatRoot = ref()
const chatAssistantWrap = ref()
const loading = ref(false)
const conversations = ref([])
const activeConversationId = ref('')
let ctx
const form = reactive({
  question: ''
})

const activeConversation = computed(() => conversations.value.find((item) => item.id === activeConversationId.value))
const messages = computed(() => activeConversation.value?.messages || [])

const rules = {
  question: [{ required: true, message: '请输入问题', trigger: 'blur' }]
}

onMounted(() => {
  loadConversations()
  startNewConversation()

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
  if (!activeConversation.value) {
    startNewConversation()
  }
  const question = form.question
  appendMessage({ role: 'user', content: question })
  form.question = ''
  loading.value = true

  try {
    const data = await askHealthQuestion({ question })
    appendMessage({
      role: 'assistant',
      content: data.answer,
      references: data.references || []
    })
  } finally {
    loading.value = false
    scrollToLatest()
  }
}

function fillQuestion(question) {
  form.question = question
}

function startNewConversation() {
  const blankConversations = conversations.value.filter((item) => item.messages.length === 0)
  conversations.value = conversations.value.filter((item) => item.messages.length > 0)

  const now = new Date().toISOString()
  const conversation = {
    id: `${Date.now()}-${Math.random().toString(16).slice(2)}`,
    title: '新对话',
    createdAt: now,
    updatedAt: now,
    messages: []
  }
  conversations.value.unshift(conversation)
  activeConversationId.value = conversation.id
  if (blankConversations.length > 0) {
    saveConversations()
  }
}

function selectConversation(id) {
  activeConversationId.value = id
  scrollToLatest()
}

function appendMessage(message) {
  const conversation = activeConversation.value
  if (!conversation) {
    return
  }

  conversation.messages.push(message)
  conversation.updatedAt = new Date().toISOString()
  if (conversation.title === '新对话' && message.role === 'user') {
    conversation.title = message.content.slice(0, 18)
  }
  conversations.value = [
    conversation,
    ...conversations.value.filter((item) => item.id !== conversation.id)
  ]
  activeConversationId.value = conversation.id
  saveConversations()
  scrollToLatest()
}

function loadConversations() {
  try {
    const saved = JSON.parse(localStorage.getItem(storageKey) || '[]')
    conversations.value = Array.isArray(saved) ? saved : []
  } catch {
    conversations.value = []
  }
}

function saveConversations() {
  localStorage.setItem(storageKey, JSON.stringify(conversations.value.slice(0, 20)))
}

function scrollToLatest() {
  nextTick(() => {
    if (chatRoot.value) {
      chatRoot.value.scrollTop = chatRoot.value.scrollHeight
    }
  })
}

function formatConversationTime(value) {
  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date(value))
}
</script>
