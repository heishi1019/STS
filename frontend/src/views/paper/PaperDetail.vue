<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <span>文献详情</span>
        <el-button @click="$router.back()">返回</el-button>
      </div>
    </template>

    <div v-loading="loading">
      <el-alert
        v-if="errorMessage"
        class="state-alert"
        type="error"
        :title="errorMessage"
        show-icon
        :closable="false"
      />

      <el-empty v-if="!loading && !paper" description="未找到这篇文献" />

      <template v-if="paper">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="标题" :span="2">
            {{ paper.title || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="期刊">{{ paper.journal || '-' }}</el-descriptions-item>
          <el-descriptions-item label="发表年份">{{ paper.publishYear || '-' }}</el-descriptions-item>
          <el-descriptions-item label="DOI">{{ paper.doi || '-' }}</el-descriptions-item>
          <el-descriptions-item label="PMID">{{ paper.pmid || '-' }}</el-descriptions-item>
          <el-descriptions-item label="摘要" :span="2">
            {{ paper.abstractText || '暂无摘要' }}
          </el-descriptions-item>
        </el-descriptions>

        <section class="detail-section">
          <h3>作者</h3>
          <el-space wrap>
            <el-tag v-for="author in authors" :key="author.id || author.name" type="info">
              {{ author.name || '未知作者' }}
            </el-tag>
            <span v-if="authors.length === 0" class="empty-text">暂无作者信息</span>
          </el-space>
        </section>

        <section class="detail-section">
          <h3>关键词</h3>
          <el-space wrap>
            <el-tag v-for="keyword in keywords" :key="keyword.id || keyword.keyword">
              {{ keyword.keyword || '-' }}
            </el-tag>
            <span v-if="keywords.length === 0" class="empty-text">暂无关键词</span>
          </el-space>
        </section>

        <section class="detail-section">
          <div class="section-header">
            <h3>标签</h3>
            <el-button size="small" type="primary" @click="openTagDialog">管理标签</el-button>
          </div>
          <el-space wrap>
            <el-tag
              v-for="tag in tags"
              :key="tag.id || tag.tagName"
              :color="tag.color || undefined"
              effect="dark"
            >
              {{ tag.tagName || '-' }}
            </el-tag>
            <span v-if="tags.length === 0" class="empty-text">暂无标签</span>
          </el-space>
        </section>

        <section class="detail-section">
          <div class="section-header">
            <h3>专题</h3>
            <el-button
              size="small"
              type="primary"
              :loading="topicLoading"
              @click="openTopicDialog"
            >
              管理专题
            </el-button>
          </div>
          <el-space wrap>
            <el-tag
              v-for="topic in paperTopics"
              :key="topic.id || topic.name"
              type="success"
            >
              {{ topic.name || '-' }}
            </el-tag>
            <span v-if="paperTopics.length === 0" class="empty-text">暂无专题</span>
          </el-space>
        </section>
      </template>
    </div>

    <el-dialog
      v-model="tagDialogVisible"
      title="管理文献标签"
      width="520px"
      destroy-on-close
      @closed="resetTagDialog"
    >
      <el-alert
        v-if="allTags.length === 0"
        class="state-alert"
        type="info"
        title="当前还没有可选标签，请先到标签管理页面新增标签。"
        show-icon
        :closable="false"
      />

      <el-form label-width="90px" @submit.prevent>
        <el-form-item label="当前标签">
          <el-select
            v-model="selectedTagIds"
            multiple
            filterable
            clearable
            collapse-tags
            collapse-tags-tooltip
            placeholder="请选择一个或多个标签"
            style="width: 100%"
          >
            <el-option
              v-for="tag in allTags"
              :key="tag.id"
              :label="tag.name"
              :value="tag.id"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button :disabled="tagSaving" @click="tagDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="tagSaving" @click="savePaperTags">
          保存
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="topicDialogVisible"
      title="管理文献专题"
      width="520px"
      destroy-on-close
      @closed="resetTopicDialog"
    >
      <el-alert
        v-if="allTopics.length === 0"
        class="state-alert"
        type="info"
        title="当前还没有可选专题，请先到专题管理页面新增专题。"
        show-icon
        :closable="false"
      />

      <el-form label-width="90px" @submit.prevent>
        <el-form-item label="当前专题">
          <el-select
            v-model="selectedTopicIds"
            multiple
            filterable
            clearable
            collapse-tags
            collapse-tags-tooltip
            placeholder="请选择一个或多个专题"
            style="width: 100%"
          >
            <el-option
              v-for="topic in allTopics"
              :key="topic.id"
              :label="topic.name"
              :value="topic.id"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button :disabled="topicSaving" @click="topicDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="topicSaving" @click="savePaperTopics">
          保存
        </el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'

import {
  addPaperTopic,
  addPaperTag,
  getPaperById,
  getPaperTopics,
  getPaperTags,
  getTags,
  getTopics,
  removePaperTopic,
  removePaperTag,
  showRequestError,
} from '../../api/paper'

const route = useRoute()

const paper = ref(null)
const loading = ref(false)
const errorMessage = ref('')
const tagDialogVisible = ref(false)
const tagSaving = ref(false)
const allTags = ref([])
const selectedTagIds = ref([])
const originalTagIds = ref([])
const paperTopics = ref([])
const topicLoading = ref(false)
const topicDialogVisible = ref(false)
const topicSaving = ref(false)
const allTopics = ref([])
const selectedTopicIds = ref([])
const originalTopicIds = ref([])

let active = true

const authors = computed(() => (Array.isArray(paper.value?.authors) ? paper.value.authors : []))
const keywords = computed(() => (Array.isArray(paper.value?.keywords) ? paper.value.keywords : []))
const tags = computed(() => (Array.isArray(paper.value?.tags) ? paper.value.tags : []))

function currentPaperId() {
  return route.params.id
}

async function loadPaperDetail() {
  const id = currentPaperId()
  if (!id) {
    paper.value = null
    errorMessage.value = '缺少文献 id'
    return
  }

  loading.value = true
  errorMessage.value = ''
  try {
    const result = await getPaperById(id)
    if (!active) return
    paper.value = result?.data || null
    if (!paper.value) {
      errorMessage.value = '文献不存在'
      paperTopics.value = []
    } else {
      await loadPaperTopics(id)
    }
  } catch (error) {
    if (!active) return
    paper.value = null
    paperTopics.value = []
    errorMessage.value = error?.message || '文献详情加载失败'
    showRequestError(error, '文献详情加载失败')
  } finally {
    if (active) {
      loading.value = false
    }
  }
}

async function loadPaperTopics(paperId = currentPaperId()) {
  if (!paperId) {
    paperTopics.value = []
    return
  }

  topicLoading.value = true
  try {
    const result = await getPaperTopics(paperId)
    if (!active) return
    paperTopics.value = Array.isArray(result?.data) ? result.data : []
  } catch (error) {
    if (!active) return
    paperTopics.value = []
    showRequestError(error, '文献专题加载失败')
  } finally {
    if (active) {
      topicLoading.value = false
    }
  }
}

async function openTagDialog() {
  const paperId = currentPaperId()
  if (!paperId) {
    errorMessage.value = '缺少文献 id'
    return
  }

  tagSaving.value = true
  try {
    const [tagResult, paperTagResult] = await Promise.all([
      getTags(),
      getPaperTags(paperId),
    ])
    if (!active) return

    allTags.value = Array.isArray(tagResult?.data) ? tagResult.data : []
    const currentTags = Array.isArray(paperTagResult?.data) ? paperTagResult.data : []
    originalTagIds.value = currentTags.map((tag) => tag.id)
    selectedTagIds.value = [...originalTagIds.value]
    tagDialogVisible.value = true
  } catch (error) {
    showRequestError(error, '文献标签加载失败')
  } finally {
    if (active) {
      tagSaving.value = false
    }
  }
}

async function savePaperTags() {
  if (tagSaving.value) return
  const paperId = currentPaperId()
  if (!paperId) {
    errorMessage.value = '缺少文献 id'
    return
  }

  const selectedSet = new Set(selectedTagIds.value)
  const originalSet = new Set(originalTagIds.value)
  const toAdd = [...selectedSet].filter((tagId) => !originalSet.has(tagId))
  const toRemove = [...originalSet].filter((tagId) => !selectedSet.has(tagId))

  if (toAdd.length === 0 && toRemove.length === 0) {
    ElMessage.info('标签没有变化')
    tagDialogVisible.value = false
    return
  }

  tagSaving.value = true
  try {
    await Promise.all([
      ...toAdd.map((tagId) => addPaperTag(paperId, tagId)),
      ...toRemove.map((tagId) => removePaperTag(paperId, tagId)),
    ])
    if (!active) return
    ElMessage.success('文献标签保存成功')
    tagDialogVisible.value = false
    await loadPaperDetail()
  } catch (error) {
    showRequestError(error, '文献标签保存失败')
  } finally {
    if (active) {
      tagSaving.value = false
    }
  }
}

function resetTagDialog() {
  selectedTagIds.value = []
  originalTagIds.value = []
  allTags.value = []
}

async function openTopicDialog() {
  const paperId = currentPaperId()
  if (!paperId) {
    errorMessage.value = '缺少文献 id'
    return
  }

  topicSaving.value = true
  try {
    const [topicResult, paperTopicResult] = await Promise.all([
      getTopics(),
      getPaperTopics(paperId),
    ])
    if (!active) return

    allTopics.value = Array.isArray(topicResult?.data) ? topicResult.data : []
    const currentTopics = Array.isArray(paperTopicResult?.data) ? paperTopicResult.data : []
    originalTopicIds.value = currentTopics.map((topic) => topic.id)
    selectedTopicIds.value = [...originalTopicIds.value]
    topicDialogVisible.value = true
  } catch (error) {
    showRequestError(error, '文献专题加载失败')
  } finally {
    if (active) {
      topicSaving.value = false
    }
  }
}

async function savePaperTopics() {
  if (topicSaving.value) return
  const paperId = currentPaperId()
  if (!paperId) {
    errorMessage.value = '缺少文献 id'
    return
  }

  const selectedSet = new Set(selectedTopicIds.value)
  const originalSet = new Set(originalTopicIds.value)
  const toAdd = [...selectedSet].filter((topicId) => !originalSet.has(topicId))
  const toRemove = [...originalSet].filter((topicId) => !selectedSet.has(topicId))

  if (toAdd.length === 0 && toRemove.length === 0) {
    ElMessage.info('专题没有变化')
    topicDialogVisible.value = false
    return
  }

  topicSaving.value = true
  try {
    await Promise.all([
      ...toAdd.map((topicId) => addPaperTopic(paperId, topicId)),
      ...toRemove.map((topicId) => removePaperTopic(paperId, topicId)),
    ])
    if (!active) return
    ElMessage.success('文献专题保存成功')
    topicDialogVisible.value = false
    await loadPaperDetail()
  } catch (error) {
    showRequestError(error, '文献专题保存失败')
  } finally {
    if (active) {
      topicSaving.value = false
    }
  }
}

function resetTopicDialog() {
  selectedTopicIds.value = []
  originalTopicIds.value = []
  allTopics.value = []
}

onMounted(loadPaperDetail)

watch(
  () => route.params.id,
  () => {
    loadPaperDetail()
  },
)

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

.state-alert {
  margin-bottom: 12px;
}

.detail-section {
  margin-top: 20px;
}

.detail-section h3 {
  margin: 0 0 12px;
  font-size: 16px;
  color: #111827;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.section-header h3 {
  margin: 0;
}

.empty-text {
  color: #909399;
  font-size: 14px;
}
</style>
