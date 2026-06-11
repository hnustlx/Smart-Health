<template>
  <main class="admin-login-page">
    <section class="admin-login-panel">
      <div class="admin-login-copy">
        <span>青芽 Admin</span>
        <h1>后台管理入口</h1>
        <p>用于管理用户、计划记录与健康知识库。</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="submit">
        <el-form-item label="管理员账号" prop="username">
          <el-input v-model="form.username" size="large" autocomplete="username" :ref="setRef(0)" @keyup.enter="onEnter(0)" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" size="large" type="password" autocomplete="current-password" show-password :ref="setRef(1)" @keyup.enter="onEnter(1)" />
        </el-form-item>
        <el-button class="admin-login-submit" type="primary" size="large" :loading="loading" @click="submit">
          登录后台
        </el-button>
        <el-button
          v-if="devLoginEnabled"
          class="admin-login-preview"
          size="large"
          :loading="loading"
          @click="previewLogin"
        >
          体验管理员登录
        </el-button>
      </el-form>

      <router-link class="admin-login-back" to="/login">返回用户登录</router-link>
    </section>
  </main>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../../api/modules/auth'
import { clearAuth, setToken, setUser } from '../../utils/auth'
import { useEnterToNext } from '../../composables/useEnterToNext'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const { setRef, onEnter } = useEnterToNext(2)
const loading = ref(false)
const devLoginEnabled = import.meta.env.VITE_ENABLE_DEV_TOOLS === 'true'

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function submit() {
  await formRef.value.validate()
  loading.value = true

  try {
    const user = await login(form)
    if (user.role !== 'ADMIN') {
      clearAuth()
      ElMessage.warning('该账号无管理员权限')
      return
    }

    setToken(user.token)
    setUser({
      userId: user.userId,
      username: user.username,
      role: user.role,
      status: user.status,
      vipExpireTime: user.vipExpireTime
    })
    ElMessage.success('管理员登录成功')
    router.push(route.query.redirect || '/admin')
  } finally {
    loading.value = false
  }
}

function previewLogin() {
  setToken('frontend-admin-dev-token')
  setUser({
    userId: 1,
    username: 'admin_preview',
    role: 'ADMIN',
    status: 1,
    vipExpireTime: '2027-01-01 00:00:00'
  })
  ElMessage.success('已进入管理员体验账号')
  router.push(route.query.redirect || '/admin')
}
</script>
