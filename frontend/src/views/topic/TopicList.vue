<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <span>专题管理</span>
        <el-button type="primary" @click="openCreateDialog">新增专题</el-button>
      </div>
    </template>

    <el-alert
      v-if="errorMessage"
      class="error-alert"
      type="error"
      :title="errorMessage"
      show-icon
      :closable="false"
    />

    <el-table v-loading="loading" :data="topics" border row-key="id">
      <el-table-column prop="name" label="专题名称" min-width="180" />
      <el-table-column prop="slug" label="Slug" width="180" show-overflow-tooltip />
      <el-table-column prop="description" label="专题说明" min-width="240" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.description || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="searchQuery" label="检索式" min-width="220" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.searchQuery || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
          <el-button link type="danger" @click="confirmDelete(row)">删除</el-button>
        </template>
      </el-table-column>

      <template #empty>
        <el-empty description="暂无专题数据" />
      </template>
    </el-table>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新增专题' : '编辑专题'"
      width="520px"
      destroy-on-close
      @closed="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="90px"
        @submit.prevent
      >
        <el-form-item label="专题名称" prop="name">
          <el-input
            v-model="form.name"
            maxlength="255"
            show-word-limit
            placeholder="例如：糖尿病相关研究"
            @blur="fillSlugIfEmpty"
            @keyup.enter="submitForm"
          />
        </el-form-item>
        <el-form-item label="Slug" prop="slug">
          <el-input
            v-model="form.slug"
            maxlength="255"
            show-word-limit
            placeholder="例如：diabetes-research"
            @keyup.enter="submitForm"
          />
        </el-form-item>
        <el-form-item label="专题说明" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            maxlength="1000"
            show-word-limit
            placeholder="可填写专题说明"
          />
        </el-form-item>
        <el-form-item label="检索式" prop="searchQuery">
          <el-input
            v-model="form.searchQuery"
            type="textarea"
            :rows="3"
            maxlength="1000"
            show-word-limit
            placeholder="可填写 PubMed 检索式，如 diabetes mellitus[Title/Abstract]"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button :disabled="submitting" @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">
          保存
        </el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import {
  createTopic,
  deleteTopic,
  getTopics,
  showRequestError,
  updateTopic,
} from '../../api/paper'

const loading = ref(false)
const submitting = ref(false)
const topics = ref([])
const errorMessage = ref('')
const dialogVisible = ref(false)
const dialogMode = ref('create')
const editingId = ref(null)
const formRef = ref(null)
const form = reactive({
  name: '',
  slug: '',
  description: '',
  searchQuery: '',
})

const rules = {
  name: [
    { required: true, message: '专题名称不能为空', trigger: 'blur' },
    { min: 1, max: 255, message: '专题名称长度不能超过 255 个字符', trigger: 'blur' },
  ],
  slug: [
    { required: true, message: '专题 slug 不能为空', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z0-9][a-zA-Z0-9_-]*$/,
      message: 'Slug 只能包含字母、数字、下划线和中划线',
      trigger: 'blur',
    },
  ],
}

let active = true

async function loadTopics() {
  loading.value = true
  errorMessage.value = ''
  try {
    const result = await getTopics()
    if (!active) return
    topics.value = Array.isArray(result?.data) ? result.data : []
  } catch (error) {
    if (!active) return
    errorMessage.value = error?.message || '专题列表加载失败'
    topics.value = []
    showRequestError(error, '专题列表加载失败')
  } finally {
    if (active) {
      loading.value = false
    }
  }
}

function openCreateDialog() {
  dialogMode.value = 'create'
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

function openEditDialog(row) {
  dialogMode.value = 'edit'
  editingId.value = row.id
  form.name = row.name || ''
  form.slug = row.slug || ''
  form.description = row.description || ''
  form.searchQuery = row.searchQuery || ''
  dialogVisible.value = true
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

function fillSlugIfEmpty() {
  if (form.slug.trim()) return
  const generated = form.name
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '')
  form.slug = generated || `topic-${Date.now()}`
}

async function submitForm() {
  if (submitting.value) return
  fillSlugIfEmpty()
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const payload = {
      name: form.name.trim(),
      slug: form.slug.trim(),
      description: form.description?.trim() || null,
      searchQuery: form.searchQuery?.trim() || null,
    }
    const result = dialogMode.value === 'create'
      ? await createTopic(payload)
      : await updateTopic(editingId.value, payload)
    ElMessage.success(result?.message || '保存成功')
    dialogVisible.value = false
    await loadTopics()
  } catch (error) {
    showRequestError(error, '专题保存失败')
  } finally {
    submitting.value = false
  }
}

async function confirmDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定删除专题“${row.name}”吗？删除后该专题与文献的关联也会被清理。`,
      '删除确认',
      {
        type: 'warning',
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger',
      },
    )
  } catch {
    return
  }

  loading.value = true
  errorMessage.value = ''
  try {
    const result = await deleteTopic(row.id)
    ElMessage.success(result?.message || '删除成功')
    await loadTopics()
  } catch (error) {
    errorMessage.value = error?.message || '专题删除失败'
    showRequestError(error, '专题删除失败')
  } finally {
    if (active) {
      loading.value = false
    }
  }
}

function resetForm() {
  form.name = ''
  form.slug = ''
  form.description = ''
  form.searchQuery = ''
  editingId.value = null
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

onMounted(loadTopics)

onBeforeUnmount(() => {
  active = false
})
</script>

<style scoped>
.page-card {
  border-radius: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.error-alert {
  margin-bottom: 12px;
}
</style>
