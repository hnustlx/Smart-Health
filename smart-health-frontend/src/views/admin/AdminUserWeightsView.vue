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
    </el-table>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getUserWeights } from '../../api/modules/admin'

const route = useRoute()
const loading = ref(false)
const records = ref([])

onMounted(async () => {
  loading.value = true
  try {
    records.value = await getUserWeights(route.params.id)
  } finally {
    loading.value = false
  }
})
</script>
