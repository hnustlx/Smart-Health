<template>
  <section class="admin-page">
    <div class="admin-section-heading">
      <p>User Detail</p>
      <h2>用户详情</h2>
    </div>
    <el-descriptions v-loading="loading" v-if="user" border :column="2">
      <el-descriptions-item label="用户 ID">{{ user.id }}</el-descriptions-item>
      <el-descriptions-item label="用户名">{{ user.username }}</el-descriptions-item>
      <el-descriptions-item label="角色">{{ user.role }}</el-descriptions-item>
      <el-descriptions-item label="状态">{{ user.status === 1 ? '正常' : '禁用' }}</el-descriptions-item>
      <el-descriptions-item label="VIP 到期时间">{{ user.vipExpireTime || '-' }}</el-descriptions-item>
    </el-descriptions>
    <el-empty v-else-if="!loading" description="未找到用户详情" />
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getUserDetail } from '../../api/modules/admin'

const route = useRoute()
const loading = ref(false)
const user = ref()

onMounted(async () => {
  loading.value = true
  try {
    user.value = await getUserDetail(route.params.id)
  } finally {
    loading.value = false
  }
})
</script>
