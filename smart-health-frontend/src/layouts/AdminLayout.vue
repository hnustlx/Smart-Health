<template>
  <el-container class="admin-shell">
    <el-aside class="admin-sidebar" width="248px">
      <div class="admin-brand">
        <div class="admin-brand-mark">SH</div>
        <div>
          <h1>Smart Health Admin</h1>
          <p>后台管理中心</p>
        </div>
      </div>

      <el-menu router :default-active="route.path" class="admin-menu">
        <el-menu-item index="/admin">总览</el-menu-item>
        <el-menu-item index="/admin/users">用户管理</el-menu-item>
        <el-menu-item index="/admin/vip-codes">VIP 激活码</el-menu-item>
        <el-menu-item index="/admin/knowledge">健康知识库</el-menu-item>
      </el-menu>

      <div class="admin-sidebar-note">
        <span>ADMIN</span>
        <strong>{{ user?.username || '管理员' }}</strong>
      </div>
    </el-aside>

    <el-container>
      <el-header class="admin-header">
        <div>
          <strong>{{ pageTitle }}</strong>
          <span>仅管理员可访问</span>
        </div>
        <el-button plain @click="logout">退出后台</el-button>
      </el-header>

      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { clearAuth, getUser } from '../utils/auth'

const route = useRoute()
const router = useRouter()
const user = computed(() => getUser())

const titles = {
  '/admin': '后台总览',
  '/admin/users': '用户管理',
  '/admin/vip-codes': 'VIP 激活码',
  '/admin/knowledge': '健康知识库'
}

const pageTitle = computed(() => titles[route.path] || '管理员后台')

function logout() {
  clearAuth()
  router.push('/admin/login')
}
</script>
