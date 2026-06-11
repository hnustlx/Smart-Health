<template>
  <main class="auth-page compact">
    <section class="auth-card">
      <p class="eyebrow">创建账号</p>
      <h1>开始记录健康变化</h1>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" size="large" autocomplete="username" :ref="setRef(0)" @keyup.enter="onEnter(0)" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" size="large" type="password" autocomplete="new-password" show-password :ref="setRef(1)" @keyup.enter="onEnter(1)" />
        </el-form-item>
        <el-button class="auth-submit" type="primary" size="large" :loading="loading" @click="submit">
          注册
        </el-button>
      </el-form>
      <p class="auth-link">已有账号？<router-link to="/login">去登录</router-link></p>
    </section>
  </main>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '../api/modules/auth'
import { useEnterToNext } from '../composables/useEnterToNext'

const router = useRouter()
const formRef = ref()
const { setRef, onEnter } = useEnterToNext(2)
const loading = ref(false)
const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, message: '密码至少 8 位', trigger: 'blur' },
    {
      pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/,
      message: '密码需包含大小写字母和数字',
      trigger: 'blur'
    }
  ]
}

async function submit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await register(form)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>
