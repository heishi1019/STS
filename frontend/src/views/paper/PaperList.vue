<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <span>文献列表</span>
        <span class="card-tip">共 {{ pageInfo.total }} 篇文献</span>
      </div>
    </template>

    <el-form :inline="true" class="search-form" @submit.prevent>
      <el-form-item label="标题">
        <el-input
          v-model="query.title"
          placeholder="文献标题"
          clearable
          @input="handleSearchConditionChange"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
      <el-form-item label="作者">
        <el-input
          v-model="query.author"
          placeholder="作者姓名"
          clearable
          @input="handleSearchConditionChange"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
      <el-form-item label="关键词">
        <el-input
          v-model="query.keyword"
          placeholder="关键词"
          clearable
          @input="handleSearchConditionChange"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
      <el-form-item label="期刊">
        <el-input
          v-model="query.journal"
          placeholder="期刊名称"
          clearable
          @input="handleSearchConditionChange"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
      <el-form-item label="年份">
        <el-input
          v-model="query.year"
          placeholder="例如：2024"
          clearable
          @input="handleSearchConditionChange"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
      <el-form-item label="DOI">
        <el-input
          v-model="query.doi"
          placeholder="DOI"
          clearable
          @input="handleSearchConditionChange"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
      <el-form-item label="PMID">
        <el-input
          v-model="query.pmid"
          placeholder="PMID"
          clearable
          @input="handleSearchConditionChange"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
      <el-form-item label="标签">
        <el-select
          v-model="query.tagId"
          placeholder="全部标签"
          clearable
          filterable
          @change="handleSearchConditionChange"
        >
          <el-option
            v-for="tag in tags"
            :key="tag.id"
            :label="tag.name"
            :value="tag.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="专题">
        <el-select
          v-model="query.topicId"
          placeholder="全部专题"
          clearable
          filterable
          @change="handleSearchConditionChange"
        >
          <el-option
            v-for="topic in topics"
            :key="topic.id"
            :label="topic.name"
            :value="topic.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="handleSearch">查询</el-button>
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
      <el-table-column prop="title" label="标题" min-width="280" show-overflow-tooltip />
      <el-table-column prop="journal" label="期刊" width="200" show-overflow-tooltip />
      <el-table-column prop="publicationYear" label="年份" width="90" />
      <el-table-column prop="doi" label="DOI" width="180" show-overflow-tooltip />
      <el-table-column prop="pmid" label="PMID" width="140" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click.stop="goDetail(row)">详情</el-button>
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
</template>

<script setup>
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import { getPapers, getTags, getTopics, showRequestError } from '../../api/paper'

const router = useRouter()

const papers = ref([])
const tags = ref([])
const topics = ref([])
const loading = ref(false)
const errorMessage = ref('')
const query = reactive({
  title: '',
  author: '',
  keyword: '',
  journal: '',
  doi: '',
  pmid: '',
  year: '',
  tagId: null,
  topicId: null,
})
const pageInfo = reactive({
  page: 1,
  size: 20,
  total: 0,
  pages: 0,
})

let active = true
let requestSeq = 0

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
    topicId: query.topicId || undefined,
  }
}

async function loadPapers() {
  if (loading.value) {
    return
  }
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

function handleSearch() {
  pageInfo.page = 1
  loadPapers()
}

function handleSearchConditionChange() {
  pageInfo.page = 1
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
  query.topicId = null
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

onMounted(() => {
  loadFilters()
  loadPapers()
})

onBeforeUnmount(() => {
  active = false
  requestSeq += 1
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

.card-tip {
  font-size: 13px;
  color: #6b7280;
}

.search-form {
  margin-bottom: 8px;
}

.state-alert {
  margin-bottom: 12px;
}

.pagination-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
