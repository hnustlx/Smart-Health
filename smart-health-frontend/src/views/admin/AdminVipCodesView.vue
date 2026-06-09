<template>
  <section class="admin-page">
    <div class="admin-section-heading">
      <p>VIP Codes</p>
      <h2>VIP 激活码管理</h2>
    </div>

    <div class="table-toolbar">
      <el-input-number v-model="count" :min="1" :max="100" />
      <el-button type="primary" :loading="generating" @click="generate">生成激活码</el-button>
    </div>

    <div v-if="latestCodes.length" class="data-panel latest-codes">
      <span>本次生成</span>
      <el-tag v-for="code in latestCodes" :key="code" type="success">{{ code }}</el-tag>
    </div>

    <el-table v-loading="loading" :data="codes" border>
      <el-table-column prop="code" label="激活码" min-width="180" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="usedBy" label="使用用户" width="120" />
      <el-table-column prop="createdBy" label="创建管理员" width="120" />
      <el-table-column prop="createdAt" label="创建时间" min-width="180" />
      <el-table-column prop="expiresAt" label="过期时间" min-width="180" />
      <el-table-column prop="usedAt" label="使用时间" min-width="180" />
    </el-table>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { generateVipCodes, getVipCodes } from '../../api/modules/admin'

const loading = ref(false)
const generating = ref(false)
const count = ref(1)
const codes = ref([])
const latestCodes = ref([])

onMounted(loadCodes)

async function loadCodes() {
  loading.value = true
  try {
    codes.value = await getVipCodes()
  } finally {
    loading.value = false
  }
}

async function generate() {
  generating.value = true
  try {
    const data = await generateVipCodes(count.value)
    latestCodes.value = data.codes || []
    ElMessage.success(`已生成 ${data.count || latestCodes.value.length} 个激活码`)
    await loadCodes()
  } finally {
    generating.value = false
  }
}

function statusText(status) {
  return status === 1 ? '已使用' : status === 2 ? '已过期' : '未使用'
}

function statusType(status) {
  return status === 1 ? 'info' : status === 2 ? 'danger' : 'success'
}
</script>
