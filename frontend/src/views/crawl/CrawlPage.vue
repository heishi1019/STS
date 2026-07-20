<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <div>
          <h2>采集任务</h2>
          <p>从 PubMed 采集真实文献并写入 MySQL，采集结果会进入综合搜索和文献工作台。</p>
        </div>
        <el-button @click="$router.push('/literature')">查看文献工作台</el-button>
      </div>
    </template>

    <el-alert
      class="tip-alert"
      type="info"
      title="当前已接入 PubMed 采集器，支持检索关键词和最大采集数量；年份范围和跳过重复开关暂未在现有采集器中支持。"
      show-icon
      :closable="false"
    />

    <el-alert
      v-if="errorMessage"
      class="tip-alert"
      type="error"
      :title="errorMessage"
      show-icon
      :closable="false"
    />

    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="crawl-form" @submit.prevent>
      <el-form-item label="数据源">
        <el-select v-model="form.dataSource" disabled>
          <el-option label="PubMed" value="pubmed" />
        </el-select>
      </el-form-item>

      <el-form-item label="检索关键词" prop="query">
        <el-input
          v-model="form.query"
          clearable
          maxlength="500"
          show-word-limit
          placeholder="例如：diabetes mellitus"
          @keyup.enter="submitForm"
        />
      </el-form-item>

      <el-form-item label="最大采集数量" prop="retmax">
        <el-input-number
          v-model="form.retmax"
          :min="1"
          :max="100"
          :step="1"
          controls-position="right"
        />
        <span class="form-help">PubMed 采集器当前限制为 1-100 条。</span>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :loading="submitting" @click="submitForm">
          开始采集
        </el-button>
        <el-button :disabled="submitting" @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>

    <el-card v-if="result" shadow="never" class="result-card">
      <template #header>采集结果</template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="任务 ID">
          {{ result.taskId || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="任务状态">
          <el-tag :type="result.failedCount > 0 ? 'warning' : 'success'">
            {{ result.status || '-' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="请求数量">
          {{ result.requestedCount ?? 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="实际获取数量">
          {{ result.fetchedCount ?? 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="成功写入数量">
          {{ result.successWriteCount ?? 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="跳过重复数量">
          {{ result.skippedDuplicateCount ?? 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="新增数量">
          {{ result.insertedCount ?? 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="重复更新数量">
          {{ result.updatedCount ?? 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="失败数量">
          {{ result.failedCount ?? 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="PubMed 总匹配数">
          {{ result.totalResults ?? 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="说明" :span="2">
          {{ result.message || '采集完成' }}
        </el-descriptions-item>
        <el-descriptions-item v-if="result.errorMessage" label="错误信息" :span="2">
          {{ result.errorMessage }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </el-card>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { collectPubMed, showRequestError } from '../../api/paper'

const formRef = ref(null)
const submitting = ref(false)
const errorMessage = ref('')
const result = ref(null)

const form = reactive({
  dataSource: 'pubmed',
  query: '',
  retmax: 10,
})

const rules = {
  query: [
    { required: true, message: '检索关键词不能为空', trigger: 'blur' },
    { min: 1, max: 500, message: '检索关键词长度不能超过 500 个字符', trigger: 'blur' },
  ],
  retmax: [
    { required: true, message: '最大采集数量不能为空', trigger: 'change' },
    {
      validator: (_rule, value, callback) => {
        if (!Number.isInteger(value) || value < 1 || value > 100) {
          callback(new Error('最大采集数量必须在 1 到 100 之间'))
          return
        }
        callback()
      },
      trigger: 'change',
    },
  ],
}

async function submitForm() {
  if (submitting.value) return
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  errorMessage.value = ''
  try {
    const response = await collectPubMed({
      query: form.query.trim(),
      retmax: form.retmax,
    })
    result.value = response?.data || null
    ElMessage.success(response?.message || 'PubMed 采集完成')
  } catch (error) {
    errorMessage.value = error?.message || 'PubMed 采集失败'
    showRequestError(error, 'PubMed 采集失败')
  } finally {
    submitting.value = false
  }
}

function resetForm() {
  form.query = ''
  form.retmax = 10
  result.value = null
  errorMessage.value = ''
  formRef.value?.clearValidate()
}
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

.tip-alert {
  margin-bottom: 16px;
}

.crawl-form {
  max-width: 760px;
}

.form-help {
  margin-left: 12px;
  color: #909399;
  font-size: 13px;
}

.result-card {
  margin-top: 20px;
  border-radius: 10px;
}
</style>
