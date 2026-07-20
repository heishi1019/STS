<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <div>
          <h2>标签管理</h2>
          <p>维护文献标签，用于快速标记重点文献、综述素材和待阅读内容。</p>
        </div>
        <el-button type="primary" @click="openCreateDialog">新增标签</el-button>
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

    <el-table v-loading="loading" :data="tags" border row-key="id">
      <el-table-column prop="name" label="标签名称" min-width="180" />
      <el-table-column label="颜色" width="180">
        <template #default="{ row }">
          <el-tag :color="row.color || defaultTagColor" effect="dark">
            {{ row.color || '默认颜色' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="说明" min-width="260" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.description || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
          <el-button link type="danger" @click="confirmDelete(row)">删除</el-button>
        </template>
      </el-table-column>

      <template #empty>
        <el-empty description="暂无标签数据" />
      </template>
    </el-table>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新增标签' : '编辑标签'"
      width="480px"
      destroy-on-close
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" @submit.prevent>
        <el-form-item label="标签名称" prop="name">
          <el-input
            v-model="form.name"
            maxlength="100"
            show-word-limit
            placeholder="请输入标签名称"
            @keyup.enter="submitForm"
          />
        </el-form-item>
        <el-form-item label="标签颜色" prop="color">
          <div class="color-row">
            <el-color-picker v-model="form.color" />
            <el-input
              v-model="form.color"
              clearable
              placeholder="例如：#409EFF，可留空"
            />
          </div>
        </el-form-item>
        <el-form-item label="说明">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="可填写标签说明"
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
  createTag,
  deleteTag,
  getTags,
  showRequestError,
  updateTag,
} from '../../api/paper'

const defaultTagColor = '#409EFF'
const loading = ref(false)
const submitting = ref(false)
const tags = ref([])
const errorMessage = ref('')
const dialogVisible = ref(false)
const dialogMode = ref('create')
const editingId = ref(null)
const formRef = ref(null)
const form = reactive({
  name: '',
  color: '',
  description: '',
})

const rules = {
  name: [
    { required: true, message: '标签名称不能为空', trigger: 'blur' },
    { min: 1, max: 100, message: '标签名称长度不能超过 100 个字符', trigger: 'blur' },
  ],
  color: [
    {
      pattern: /^$|^#[0-9A-Fa-f]{6}$/,
      message: '标签颜色必须是 #RRGGBB 格式',
      trigger: 'blur',
    },
  ],
}

let active = true

async function loadTags() {
  loading.value = true
  errorMessage.value = ''
  try {
    const result = await getTags()
    if (!active) return
    tags.value = Array.isArray(result?.data) ? result.data : []
  } catch (error) {
    if (!active) return
    errorMessage.value = error?.message || '标签列表加载失败'
    tags.value = []
    showRequestError(error, '标签列表加载失败')
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
  form.color = row.color || ''
  form.description = row.description || ''
  dialogVisible.value = true
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

async function submitForm() {
  if (submitting.value) return
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const payload = {
      name: form.name.trim(),
      color: form.color?.trim() || null,
      description: form.description?.trim() || null,
    }
    const result = dialogMode.value === 'create'
      ? await createTag(payload)
      : await updateTag(editingId.value, payload)
    ElMessage.success(result?.message || '保存成功')
    dialogVisible.value = false
    await loadTags()
  } catch (error) {
    showRequestError(error, '标签保存失败')
  } finally {
    submitting.value = false
  }
}

async function confirmDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定删除标签“${row.name}”吗？删除后该标签与文献的关联也会被清理。`,
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
    const result = await deleteTag(row.id)
    ElMessage.success(result?.message || '删除成功')
    await loadTags()
  } catch (error) {
    errorMessage.value = error?.message || '标签删除失败'
    showRequestError(error, '标签删除失败')
  } finally {
    if (active) {
      loading.value = false
    }
  }
}

function resetForm() {
  form.name = ''
  form.color = ''
  form.description = ''
  editingId.value = null
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

onMounted(loadTags)

onBeforeUnmount(() => {
  active = false
})
</script>

<style scoped>
.page-card {
  border-radius: 14px;
}

.card-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
}

.card-header h2 {
  margin: 0 0 6px;
}

.card-header p {
  margin: 0;
  color: #64748b;
}

.error-alert {
  margin-bottom: 12px;
}

.color-row {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 12px;
  width: 100%;
}
</style>
