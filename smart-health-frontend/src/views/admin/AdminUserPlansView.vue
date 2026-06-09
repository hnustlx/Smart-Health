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
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button type="danger" link @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute } from 'vue-router'
import { deleteUserPlan, getUserPlans } from '../../api/modules/admin'

const route = useRoute()
const loading = ref(false)
const plans = ref([])

onMounted(loadPlans)

async function loadPlans() {
  loading.value = true
  try {
    plans.value = await getUserPlans(route.params.id)
  } finally {
    loading.value = false
  }
}

async function remove(id) {
  await ElMessageBox.confirm('确认删除这条 AI 计划？', '删除确认', { type: 'warning' })
  await deleteUserPlan(id)
  ElMessage.success('AI 计划已删除')
  await loadPlans()
}
</script>
