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

    <div v-if="profile" class="data-panel admin-profile-panel">
      <h3>健康档案</h3>
      <el-descriptions border :column="2">
        <el-descriptions-item label="年龄">{{ profile.age }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ profile.gender }}</el-descriptions-item>
        <el-descriptions-item label="身高">{{ profile.height }} cm</el-descriptions-item>
        <el-descriptions-item label="体重">{{ profile.weight }} kg</el-descriptions-item>
        <el-descriptions-item label="活动水平">{{ profile.activityLevel }}</el-descriptions-item>
        <el-descriptions-item label="健康目标">{{ profile.goal }}</el-descriptions-item>
        <el-descriptions-item label="饮食偏好">{{ profile.dietPreference || '-' }}</el-descriptions-item>
      </el-descriptions>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getUserDetail, getUserProfile } from '../../api/modules/admin'

const route = useRoute()
const loading = ref(false)
const user = ref()
const profile = ref()

onMounted(loadDetail)

async function loadDetail() {
  loading.value = true
  try {
    const [userResult, profileResult] = await Promise.allSettled([
      getUserDetail(route.params.id),
      getUserProfile(route.params.id)
    ])
    if (userResult.status === 'fulfilled') {
      user.value = userResult.value
    }
    if (profileResult.status === 'fulfilled') {
      profile.value = profileResult.value
    }
  } finally {
    loading.value = false
  }
}
</script>
