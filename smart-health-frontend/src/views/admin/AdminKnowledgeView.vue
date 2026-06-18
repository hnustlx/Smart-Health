<template>
  <section class="admin-page">
    <div class="admin-section-heading">
      <p>Knowledge</p>
      <h2>健康知识库管理</h2>
    </div>

    <div class="table-toolbar">
      <el-input v-model="filters.keyword" clearable placeholder="关键词" @keyup.enter="loadList" />
      <el-input v-model="filters.category" clearable placeholder="分类" @keyup.enter="loadList" />
      <el-select v-model="filters.status" clearable placeholder="状态">
        <el-option label="启用" value="enabled" />
        <el-option label="禁用" value="disabled" />
      </el-select>
      <el-select v-model="filters.level" clearable placeholder="级别">
        <el-option label="basic" value="basic" />
        <el-option label="vip" value="vip" />
        <el-option label="all" value="all" />
      </el-select>
      <el-button type="primary" @click="loadList">查询</el-button>
      <el-button @click="openCreate">新增知识</el-button>
    </div>

    <el-table v-loading="loading" :data="list" border>
      <el-table-column prop="id" label="知识 ID" min-width="180" />
      <el-table-column label="标题" min-width="180">
        <template #default="{ row }">{{ row.metadata?.title }}</template>
      </el-table-column>
      <el-table-column label="分类" width="130">
        <template #default="{ row }">{{ row.metadata?.category }}</template>
      </el-table-column>
      <el-table-column label="级别" width="100">
        <template #default="{ row }">{{ row.metadata?.level }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.metadata?.status === 'enabled' ? 'success' : 'danger'">
            {{ row.metadata?.status === 'enabled' ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="document" label="正文" min-width="240" show-overflow-tooltip />
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button v-if="row.metadata?.status === 'enabled'" link type="warning" @click="disable(row.id)">禁用</el-button>
          <el-button v-else link type="success" @click="enable(row.id)">启用</el-button>
          <el-button link type="danger" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-if="total > size"
      v-model:current-page="page"
      v-model:page-size="size"
      class="table-pagination"
      background
      layout="total, prev, pager, next"
      :total="total"
      @current-change="loadList"
    />

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑健康知识' : '新增健康知识'" width="680px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="知识标题" prop="metadata.title">
          <el-input v-model="form.metadata.title" />
        </el-form-item>
        <el-form-item label="知识分类" prop="metadata.category">
          <el-input v-model="form.metadata.category" />
        </el-form-item>
        <el-form-item label="关键词" prop="metadata.keywords">
          <el-input v-model="form.metadata.keywords" />
        </el-form-item>
        <div class="form-grid two">
          <el-form-item label="状态" prop="metadata.status">
            <el-select v-model="form.metadata.status">
              <el-option label="启用" value="enabled" />
              <el-option label="禁用" value="disabled" />
            </el-select>
          </el-form-item>
          <el-form-item label="适用级别" prop="metadata.level">
            <el-select v-model="form.metadata.level">
              <el-option label="basic" value="basic" />
              <el-option label="vip" value="vip" />
              <el-option label="all" value="all" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="健康知识正文" prop="document">
          <el-input v-model="form.document" type="textarea" :rows="5" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createKnowledge,
  deleteKnowledge,
  disableKnowledge,
  enableKnowledge,
  getKnowledgeList,
  updateKnowledge
} from '../../api/modules/admin'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingId = ref('')
const formRef = ref()
const list = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

const filters = reactive({
  keyword: '',
  category: '',
  status: '',
  level: ''
})

const form = reactive(createEmptyForm())

const rules = {
  document: [{ required: true, message: '请输入健康知识正文', trigger: 'blur' }],
  'metadata.title': [{ required: true, message: '请输入知识标题', trigger: 'blur' }],
  'metadata.category': [{ required: true, message: '请输入知识分类', trigger: 'blur' }],
  'metadata.status': [{ required: true, message: '请选择状态', trigger: 'change' }],
  'metadata.level': [{ required: true, message: '请选择适用级别', trigger: 'change' }]
}

onMounted(loadList)

function createEmptyForm() {
  return {
    document: '',
    metadata: {
      category: '',
      title: '',
      keywords: '',
      status: 'enabled',
      level: 'all'
    }
  }
}

function resetForm(data = createEmptyForm()) {
  Object.assign(form, {
    document: data.document,
    metadata: { ...data.metadata }
  })
}

async function loadList() {
  loading.value = true
  try {
    const data = await getKnowledgeList({
      keyword: filters.keyword || undefined,
      category: filters.category || undefined,
      status: filters.status || undefined,
      level: filters.level || undefined,
      page: page.value,
      size: size.value
    })
    list.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = ''
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  resetForm(row)
  dialogVisible.value = true
}

async function save() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (editingId.value) {
      await updateKnowledge(editingId.value, form)
      ElMessage.success('健康知识已修改')
    } else {
      await createKnowledge(form)
      ElMessage.success('健康知识已新增')
    }
    dialogVisible.value = false
    await loadList()
  } finally {
    saving.value = false
  }
}

async function enable(id) {
  await enableKnowledge(id)
  ElMessage.success('健康知识已启用')
  await loadList()
}

async function disable(id) {
  await disableKnowledge(id)
  ElMessage.success('健康知识已禁用')
  await loadList()
}

async function remove(id) {
  await ElMessageBox.confirm('确认删除这条健康知识？', '删除确认', { type: 'warning' })
  await deleteKnowledge(id)
  ElMessage.success('健康知识已删除')
  await loadList()
}
</script>
