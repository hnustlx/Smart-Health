<template>
  <section class="admin-page">
    <div class="admin-section-heading">
      <p>AI Config</p>
      <h2>AI 配置管理</h2>
    </div>

    <div class="table-toolbar">
      <el-input v-model="keyword" clearable placeholder="用户名搜索" @keyup.enter="page=1; loadList()" />
      <el-button type="primary" @click="page=1; loadList()">查询</el-button>
    </div>

    <el-table v-loading="loading" :data="list" border>
      <el-table-column prop="userId" label="用户 ID" width="100" />
      <el-table-column prop="username" label="用户名" width="140" />
      <el-table-column label="提供方" width="120">
        <template #default="{ row }">
          <el-tag :type="row.provider === 'CUSTOM' ? 'success' : 'info'">
            {{ row.provider }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="customProvider" label="自定义厂商" width="130" />
      <el-table-column prop="model" label="模型" min-width="160" />
      <el-table-column prop="apiUrl" label="API 地址" min-width="200" show-overflow-tooltip />
      <el-table-column prop="apiKey" label="API Key" width="160">
        <template #default>
          <span>****</span>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column prop="updatedAt" label="更新时间" width="180" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="showDetail(row)">详情</el-button>
          <el-button link type="danger" @click="resetConfig(row)">重置</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="table-pagination">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[5, 10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="loadList"
        @size-change="handleSizeChange"
      />
    </div>

    <el-dialog v-model="detailVisible" :title="'AI 配置详情 - 用户 #' + detailUserId" width="600px">
      <el-form v-if="detailData" label-position="left" label-width="120px">
        <el-form-item label="用户 ID"><span>{{ detailData.userId }}</span></el-form-item>
        <el-form-item label="用户名"><span>{{ detailData.username }}</span></el-form-item>
        <el-form-item label="提供方"><span>{{ detailData.provider }}</span></el-form-item>
        <el-form-item label="自定义厂商"><span>{{ detailData.customProvider }}</span></el-form-item>
        <el-form-item label="API Key"><span>****</span></el-form-item>
        <el-form-item label="API 地址"><span>{{ detailData.apiUrl }}</span></el-form-item>
        <el-form-item label="模型"><span>{{ detailData.model }}</span></el-form-item>
        <el-form-item label="创建时间"><span>{{ detailData.createdAt }}</span></el-form-item>
        <el-form-item label="更新时间"><span>{{ detailData.updatedAt }}</span></el-form-item>
      </el-form>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getAiConfigs,
  getAiConfigDetail,
  resetAiConfigAdmin
} from '../../api/modules/admin'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')
const detailVisible = ref(false)
const detailUserId = ref(null)
const detailData = ref(null)

onMounted(loadList)

async function loadList() {
  loading.value = true
  try {
    const res = await getAiConfigs({
      keyword: keyword.value || undefined,
      page: page.value,
      size: size.value
    })
    list.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function handleSizeChange(val) {
  size.value = val
  page.value = 1
  loadList()
}

async function showDetail(row) {
  detailUserId.value = row.userId
  const res = await getAiConfigDetail(row.userId)
  detailData.value = res
  detailVisible.value = true
}

async function resetConfig(row) {
  await ElMessageBox.confirm('确认重置该用户的 AI 配置？', '重置确认', { type: 'warning' })
  await resetAiConfigAdmin(row.userId)
  ElMessage.success('AI 配置已重置为默认')
  await loadList()
}
</script>

<style scoped>
.table-pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
