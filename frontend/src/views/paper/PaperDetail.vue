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
          <h3>标签</h3>
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
      </template>
    </div>
  </el-card>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'

import { getPaperById, showRequestError } from '../../api/paper'

const route = useRoute()

const paper = ref(null)
const loading = ref(false)
const errorMessage = ref('')

let active = true

const authors = computed(() => (Array.isArray(paper.value?.authors) ? paper.value.authors : []))
const keywords = computed(() => (Array.isArray(paper.value?.keywords) ? paper.value.keywords : []))
const tags = computed(() => (Array.isArray(paper.value?.tags) ? paper.value.tags : []))

async function loadPaperDetail() {
  const id = route.params.id
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
    }
  } catch (error) {
    if (!active) return
    paper.value = null
    errorMessage.value = error?.message || '文献详情加载失败'
    showRequestError(error, '文献详情加载失败')
  } finally {
    if (active) {
      loading.value = false
    }
  }
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

.empty-text {
  color: #909399;
  font-size: 14px;
}
</style>