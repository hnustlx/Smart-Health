<template>
  <section class="admin-page">
    <div class="admin-section-heading">
      <p>Weight Records</p>
      <h2>体重记录管理</h2>
    </div>
    <el-table v-loading="loading" :data="records" border>
      <el-table-column prop="id" label="ID" width="100" />
      <el-table-column prop="recordDate" label="日期" />
      <el-table-column prop="weight" label="体重 kg" />
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
import { deleteUserWeight, getUserWeights } from '../../api/modules/admin'

const route = useRoute()
const loading = ref(false)
const records = ref([])

onMounted(loadRecords)

async function loadRecords() {
  loading.value = true
  try {
    records.value = await getUserWeights(route.params.id)
  } finally {
    loading.value = false
  }
}

async function remove(id) {
  await ElMessageBox.confirm('确认删除这条体重记录？', '删除确认', { type: 'warning' })
  await deleteUserWeight(id)
  ElMessage.success('体重记录已删除')
  await loadRecords()
}
</script>
