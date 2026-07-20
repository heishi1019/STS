<template>
  <div class="literature-page">
    <div class="page-heading">
      <div>
        <h1>文献工作台</h1>
        <p>在一个页面完成文献检索、专题筛选、专题维护和文献归类。</p>
      </div>
      <el-button type="primary" @click="openCreateTopicDialog">新增专题</el-button>
    </div>

    <div class="workspace">
      <aside class="topic-panel">
        <div class="topic-panel-header">
          <h2>专题</h2>
          <span>{{ topics.length }} 个专题</span>
        </div>

        <button
          class="topic-item"
          :class="{ active: selectedTopicId === null }"
          type="button"
          @click="selectTopic(null)"
        >
          <span>全部文献</span>
          <small>查看数据库中的所有文献</small>
        </button>

        <button
          v-for="topic in topics"
          :key="topic.id"
          class="topic-item"
          :class="{ active: selectedTopicId === topic.id }"
          type="button"
          @click="selectTopic(topic.id)"
        >
          <span>{{ topic.name }}</span>
          <small>{{ topic.description || topic.searchQuery || '暂无说明' }}</small>
        </button>
      </aside>

      <main class="paper-panel">
        <el-card shadow="never" class="paper-card">
          <template #header>
            <div class="card-header">
              <div>
                <h2>{{ currentTopicName }}</h2>
                <p>共 {{ pageInfo.total }} 篇文献</p>
              </div>
              <div v-if="currentTopic" class="topic-actions">
                <el-button @click="openEditTopicDialog(currentTopic)">编辑专题</el-button>
                <el-button type="danger" plain @click="confirmDeleteTopic(currentTopic)">删除专题</el-button>
              </div>
            </div>
          </template>

          <el-form :inline="true" class="search-form" @submit.prevent>
            <el-form-item label="标题">
              <el-input v-model="query.title" clearable placeholder="文献标题" @keyup.enter="handleSearch" />
            </el-form-item>
            <el-form-item label="作者">
              <el-input v-model="query.author" clearable placeholder="作者姓名" @keyup.enter="handleSearch" />
            </el-form-item>
            <el-form-item label="关键词">
              <el-input v-model="query.keyword" clearable placeholder="关键词" @keyup.enter="handleSearch" />
            </el-form-item>
            <el-form-item label="期刊">
              <el-input v-model="query.journal" clearable placeholder="期刊名称" @keyup.enter="handleSearch" />
            </el-form-item>
            <el-form-item label="年份">
              <el-input v-model="query.year" clearable placeholder="例如 2026" @keyup.enter="handleSearch" />
            </el-form-item>
            <el-form-item label="DOI">
              <el-input v-model="query.doi" clearable placeholder="DOI" @keyup.enter="handleSearch" />
            </el-form-item>
            <el-form-item label="PMID">
              <el-input v-model="query.pmid" clearable placeholder="PMID" @keyup.enter="handleSearch" />
            </el-form-item>
            <el-form-item label="标签">
              <el-select v-model="query.tagId" clearable filterable placeholder="全部标签">
                <el-option v-for="tag in tags" :key="tag.id" :label="tag.name" :value="tag.id" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="loading" @click="handleSearch">搜索</el-button>
              <el-button :disabled="loading" @click="resetQuery">重置</el-button>
            </el-form-item>
          </el-form>

          <el-alert
            v-if="errorMessage"
            class="state-alert"
            type="error"
            :title="errorMessage"
            show-icon
            :closable="false"
          />

          <el-table
            v-loading="loading"
            :data="papers"
            border
            row-key="id"
            empty-text="暂无文献数据"
            @row-click="goDetail"
          >
            <el-table-column prop="title" label="标题" min-width="320" show-overflow-tooltip />
            <el-table-column prop="journal" label="期刊" width="220" show-overflow-tooltip />
            <el-table-column prop="publicationYear" label="年份" width="90" />
            <el-table-column prop="pmid" label="PMID" width="130" />
            <el-table-column prop="doi" label="DOI" width="190" show-overflow-tooltip />
            <el-table-column label="操作" width="230" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click.stop="goDetail(row)">详情</el-button>

                <el-button
                  v-if="selectedTopicId"
                  link
                  type="danger"
                  @click.stop="removeFromCurrentTopic(row)"
                >
                  移出专题
                </el-button>

                <el-dropdown v-else trigger="click" @command="(topicId) => addToTopic(row, topicId)">
                  <el-button link type="success" @click.stop>
                    加入专题
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item
                        v-for="topic in topics"
                        :key="topic.id"
                        :command="topic.id"
                      >
                        {{ topic.name }}
                      </el-dropdown-item>
                      <el-dropdown-item v-if="topics.length === 0" disabled>
                        暂无专题
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>

                <el-button link type="info" @click.stop="goDetail(row)">标签</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-row">
            <el-pagination
              v-model:current-page="pageInfo.page"
              v-model:page-size="pageInfo.size"
              :page-sizes="[10, 20, 50, 100]"
              :total="pageInfo.total"
              :disabled="loading"
              layout="total, sizes, prev, pager, next, jumper"
              background
              @current-change="handlePageChange"
              @size-change="handleSizeChange"
            />
          </div>
        </el-card>
      </main>
    </div>

    <el-dialog
      v-model="topicDialogVisible"
      :title="topicDialogMode === 'create' ? '新增专题' : '编辑专题'"
      width="540px"
      destroy-on-close
      @closed="resetTopicForm"
    >
      <el-form ref="topicFormRef" :model="topicForm" :rules="topicRules" label-width="90px" @submit.prevent>
        <el-form-item label="专题名称" prop="name">
          <el-input
            v-model="topicForm.name"
            maxlength="255"
            show-word-limit
            placeholder="例如：糖尿病相关研究"
            @blur="fillSlugIfEmpty"
            @keyup.enter="submitTopicForm"
          />
        </el-form-item>
        <el-form-item label="Slug" prop="slug">
          <el-input
            v-model="topicForm.slug"
            maxlength="255"
            show-word-limit
            placeholder="例如：diabetes-research"
            @keyup.enter="submitTopicForm"
          />
        </el-form-item>
        <el-form-item label="专题说明">
          <el-input
            v-model="topicForm.description"
            type="textarea"
            :rows="3"
            maxlength="1000"
            show-word-limit
            placeholder="可填写专题说明"
          />
        </el-form-item>
        <el-form-item label="检索式">
          <el-input
            v-model="topicForm.searchQuery"
            type="textarea"
            :rows="3"
            maxlength="1000"
            show-word-limit
            placeholder="可填写 PubMed 检索式，如 diabetes mellitus[Title/Abstract]"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button :disabled="topicSubmitting" @click="topicDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="topicSubmitting" @click="submitTopicForm">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'

import {
  addPaperTopic,
  createTopic,
  deleteTopic,
  getPapers,
  getTags,
  getTopics,
  removePaperTopic,
  showRequestError,
  updateTopic,
} from '../../api/paper'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const errorMessage = ref('')
const papers = ref([])
const tags = ref([])
const topics = ref([])
const selectedTopicId = ref(null)
const topicDialogVisible = ref(false)
const topicDialogMode = ref('create')
const topicSubmitting = ref(false)
const editingTopicId = ref(null)
const topicFormRef = ref(null)

const query = reactive({
  title: '',
  author: '',
  keyword: '',
  journal: '',
  doi: '',
  pmid: '',
  year: '',
  tagId: null,
})
const pageInfo = reactive({
  page: 1,
  size: 20,
  total: 0,
  pages: 0,
})
const topicForm = reactive({
  name: '',
  slug: '',
  description: '',
  searchQuery: '',
})

const topicRules = {
  name: [
    { required: true, message: '专题名称不能为空', trigger: 'blur' },
    { max: 255, message: '专题名称不能超过 255 个字符', trigger: 'blur' },
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
let requestSeq = 0

const currentTopic = computed(() => topics.value.find((topic) => topic.id === selectedTopicId.value) || null)
const currentTopicName = computed(() => currentTopic.value?.name || '全部文献')

function cleanText(value) {
  const text = String(value || '').trim()
  return text || undefined
}

function buildParams() {
  const yearText = cleanText(query.year)
  return {
    page: pageInfo.page,
    size: pageInfo.size,
    title: cleanText(query.title),
    author: cleanText(query.author),
    keyword: cleanText(query.keyword),
    journal: cleanText(query.journal),
    doi: cleanText(query.doi),
    pmid: cleanText(query.pmid),
    year: yearText ? Number(yearText) : undefined,
    tagId: query.tagId || undefined,
    topicId: selectedTopicId.value || undefined,
  }
}

async function loadPapers() {
  if (loading.value) return
  const seq = ++requestSeq
  loading.value = true
  errorMessage.value = ''
  try {
    const result = await getPapers(buildParams())
    if (!active || seq !== requestSeq) return
    const pageData = result?.data || {}
    papers.value = Array.isArray(pageData.records) ? pageData.records : []
    pageInfo.total = Number(pageData.total || 0)
    pageInfo.pages = Number(pageData.pages || 0)
    pageInfo.page = Number(pageData.current || pageInfo.page)
    pageInfo.size = Number(pageData.size || pageInfo.size)
  } catch (error) {
    if (!active || seq !== requestSeq) return
    errorMessage.value = error?.message || '文献列表加载失败'
    papers.value = []
    showRequestError(error, '文献列表加载失败')
  } finally {
    if (active && seq === requestSeq) {
      loading.value = false
    }
  }
}

async function loadFilters() {
  try {
    const [tagResult, topicResult] = await Promise.all([getTags(), getTopics()])
    if (!active) return
    tags.value = Array.isArray(tagResult?.data) ? tagResult.data : []
    topics.value = Array.isArray(topicResult?.data) ? topicResult.data : []
  } catch (error) {
    if (!active) return
    showRequestError(error, '筛选项加载失败')
  }
}

function selectTopic(topicId) {
  selectedTopicId.value = topicId
  pageInfo.page = 1
  loadPapers()
}

function handleSearch() {
  pageInfo.page = 1
  loadPapers()
}

function resetQuery() {
  query.title = ''
  query.author = ''
  query.keyword = ''
  query.journal = ''
  query.doi = ''
  query.pmid = ''
  query.year = ''
  query.tagId = null
  pageInfo.page = 1
  loadPapers()
}

function handlePageChange(page) {
  pageInfo.page = page
  loadPapers()
}

function handleSizeChange(size) {
  pageInfo.size = size
  pageInfo.page = 1
  loadPapers()
}

function goDetail(row) {
  if (!row?.id) return
  router.push({ name: 'paper-detail', params: { id: row.id } })
}

function openCreateTopicDialog() {
  topicDialogMode.value = 'create'
  editingTopicId.value = null
  resetTopicForm()
  topicDialogVisible.value = true
}

function openEditTopicDialog(topic) {
  topicDialogMode.value = 'edit'
  editingTopicId.value = topic.id
  topicForm.name = topic.name || ''
  topicForm.slug = topic.slug || ''
  topicForm.description = topic.description || ''
  topicForm.searchQuery = topic.searchQuery || ''
  topicDialogVisible.value = true
  nextTick(() => topicFormRef.value?.clearValidate())
}

function fillSlugIfEmpty() {
  if (topicForm.slug.trim()) return
  const generated = topicForm.name
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '')
  topicForm.slug = generated || `topic-${Date.now()}`
}

async function submitTopicForm() {
  if (topicSubmitting.value) return
  fillSlugIfEmpty()
  const valid = await topicFormRef.value?.validate().catch(() => false)
  if (!valid) return

  topicSubmitting.value = true
  try {
    const payload = {
      name: topicForm.name.trim(),
      slug: topicForm.slug.trim(),
      description: topicForm.description?.trim() || null,
      searchQuery: topicForm.searchQuery?.trim() || null,
    }
    const result = topicDialogMode.value === 'create'
      ? await createTopic(payload)
      : await updateTopic(editingTopicId.value, payload)
    ElMessage.success(result?.message || '专题保存成功')
    topicDialogVisible.value = false
    await loadFilters()
  } catch (error) {
    showRequestError(error, '专题保存失败')
  } finally {
    topicSubmitting.value = false
  }
}

async function confirmDeleteTopic(topic) {
  try {
    await ElMessageBox.confirm(
      `确定删除专题“${topic.name}”吗？删除后该专题与文献的关联也会被清理。`,
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
  try {
    const result = await deleteTopic(topic.id)
    ElMessage.success(result?.message || '专题删除成功')
    selectedTopicId.value = null
    await loadFilters()
    await loadPapers()
  } catch (error) {
    showRequestError(error, '专题删除失败')
  } finally {
    loading.value = false
  }
}

async function addToTopic(row, topicId) {
  if (!row?.id || !topicId) return
  try {
    const result = await addPaperTopic(row.id, topicId)
    ElMessage.success(result?.message || '已加入专题')
  } catch (error) {
    showRequestError(error, '加入专题失败')
  }
}

async function removeFromCurrentTopic(row) {
  if (!row?.id || !selectedTopicId.value) return
  try {
    const result = await removePaperTopic(row.id, selectedTopicId.value)
    ElMessage.success(result?.message || '已移出专题')
    await loadPapers()
  } catch (error) {
    showRequestError(error, '移出专题失败')
  }
}

function resetTopicForm() {
  topicForm.name = ''
  topicForm.slug = ''
  topicForm.description = ''
  topicForm.searchQuery = ''
  editingTopicId.value = null
  nextTick(() => topicFormRef.value?.clearValidate())
}

onMounted(() => {
  const initialTopicId = Number(route.query.topicId)
  selectedTopicId.value = Number.isFinite(initialTopicId) && initialTopicId > 0 ? initialTopicId : null
  loadFilters()
  loadPapers()
})

onBeforeUnmount(() => {
  active = false
  requestSeq += 1
})
</script>

<style scoped>
.literature-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.page-heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
}

.page-heading h1 {
  margin: 0 0 6px;
  color: #0f172a;
}

.page-heading p {
  margin: 0;
  color: #64748b;
}

.workspace {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 18px;
}

.topic-panel {
  align-self: start;
  padding: 16px;
  border-radius: 14px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
}

.topic-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.topic-panel-header h2 {
  margin: 0;
  font-size: 18px;
}

.topic-panel-header span {
  color: #64748b;
  font-size: 13px;
}

.topic-item {
  width: 100%;
  border: 1px solid transparent;
  border-radius: 12px;
  background: transparent;
  padding: 12px;
  margin-bottom: 8px;
  text-align: left;
  cursor: pointer;
  transition: all 0.2s ease;
}

.topic-item span {
  display: block;
  color: #0f172a;
  font-weight: 700;
}

.topic-item small {
  display: block;
  margin-top: 4px;
  color: #64748b;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.topic-item:hover,
.topic-item.active {
  border-color: #99f6e4;
  background: #ecfdf5;
}

.paper-panel {
  min-width: 0;
}

.paper-card {
  border-radius: 14px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.card-header h2 {
  margin: 0 0 4px;
}

.card-header p {
  margin: 0;
  color: #64748b;
}

.topic-actions {
  display: flex;
  gap: 8px;
}

.search-form {
  margin-bottom: 8px;
}

.search-form :deep(.el-input) {
  width: 180px;
}

.search-form :deep(.el-select) {
  width: 180px;
}

.state-alert {
  margin-bottom: 12px;
}

.pagination-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

@media (max-width: 1000px) {
  .workspace {
    grid-template-columns: 1fr;
  }
}
</style>
