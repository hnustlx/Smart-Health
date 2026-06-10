<template>
  <section class="admin-page">
    <div class="admin-section-heading">
      <p>Users</p>
      <h2>用户管理</h2>
    </div>

    <div class="table-toolbar">
      <el-input v-model="keyword" clearable placeholder="搜索用户名" @keyup.enter="loadUsers" />
      <el-button type="primary" @click="loadUsers">查询</el-button>
    </div>

    <el-table v-loading="loading" :data="users" border>
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="role" label="角色" width="120" />
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="vipExpireTime" label="VIP 到期时间" />
      <el-table-column label="操作" width="280">
        <template #default="{ row }">
          <el-button link type="primary" @click="$router.push(`/admin/users/${row.id}`)">详情</el-button>
          <el-button link @click="$router.push(`/admin/users/${row.id}/weights`)">体重</el-button>
          <el-button link @click="$router.push(`/admin/users/${row.id}/plans`)">计划</el-button>
          <el-button v-if="row.status === 1" link type="danger" @click="disable(row.id)">禁用</el-button>
          <el-button v-else link type="success" @click="enable(row.id)">启用</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="page"
      v-model:page-size="size"
      class="table-pagination"
      background
      layout="total, prev, pager, next"
      :total="total"
      @current-change="loadUsers"
    />
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { disableUser, enableUser, getUsers } from '../../api/modules/admin'

const loading = ref(false)
const users = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')

onMounted(loadUsers)

async function loadUsers() {
  loading.value = true
  try {
    const data = await getUsers({ page: page.value, size: size.value, keyword: keyword.value || undefined })
    users.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

async function enable(id) {
  await enableUser(id)
  ElMessage.success('用户已启用')
  await loadUsers()
}

async function disable(id) {
  await disableUser(id)
  ElMessage.success('用户已禁用')
  await loadUsers()
}
</script>
