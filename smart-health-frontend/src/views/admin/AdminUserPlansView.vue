<template>
  <section class="admin-page">
    <div class="admin-section-heading">
      <p>Plan Records</p>
      <h2>AI 计划记录管理</h2>
    </div>
    <el-table v-loading="loading" :data="plans" border>
      <el-table-column prop="id" label="ID" width="100" />
      <el-table-column prop="planType" label="类型" width="140" />
      <el-table-column prop="planLevel" label="级别" width="140" />
      <el-table-column prop="trendSummary" label="体重趋势" />
      <el-table-column prop="createTime" label="创建时间" />
    </el-table>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getUserPlans } from '../../api/modules/admin'

const route = useRoute()
const loading = ref(false)
const plans = ref([])

onMounted(async () => {
  loading.value = true
  try {
    plans.value = await getUserPlans(route.params.id)
  } finally {
    loading.value = false
  }
})
</script>
