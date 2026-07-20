<template>
  <div class="home-page">
    <header class="site-header">
      <RouterLink class="brand" to="/">
        <div class="brand-mark">B</div>
        <div>
          <div class="brand-title">生物医学文献知识服务与智能综述平台</div>
          <div class="brand-subtitle">Biomedical Literature Knowledge Service</div>
        </div>
      </RouterLink>

      <nav class="top-links">
        <RouterLink to="/literature">文献工作台</RouterLink>
        <RouterLink to="/crawl">采集任务</RouterLink>
        <RouterLink to="/tags">标签管理</RouterLink>
      </nav>
    </header>

    <section class="hero">
      <div class="network-bg"></div>
      <div class="hero-inner">
        <div class="platform-logo">BioMed Literature</div>
        <h1>生物医学文献智能检索平台</h1>
        <p class="hero-intro">
          输入疾病、基因、药物、症状、关键词、PMID 或 DOI，快速定位真实医学文献。
        </p>

        <div class="search-box">
          <el-input
            v-model="query.q"
            size="large"
            clearable
            placeholder="输入关键词，例如 diabetes、TP53、metformin、Alzheimer、PMID 或 DOI"
            @keyup.enter="handleSearch"
          />
          <el-button size="large" type="success" :loading="loading" @click="handleSearch">
            搜索
          </el-button>
        </div>

        <button class="advanced-link" type="button" @click="advancedVisible = !advancedVisible">
          {{ advancedVisible ? '收起高级检索' : '高级检索' }}
        </button>

        <el-collapse-transition>
          <div v-if="advancedVisible" class="advanced-panel">
            <div class="advanced-title">
              <strong>高级检索</strong>
              <span>先输入关键词，再按实体、文献信息或管理信息缩小范围。</span>
            </div>
            <el-form :inline="true" class="advanced-form" @submit.prevent>
              <div class="filter-group">
                <div class="filter-group-title">医学实体</div>
                <el-form-item label="疾病">
                  <el-select v-model="query.diseaseIds" multiple filterable clearable collapse-tags placeholder="全部疾病">
                    <el-option v-for="item in facets.diseases" :key="item.id" :label="item.name" :value="item.id" />
                  </el-select>
                </el-form-item>
                <el-form-item label="基因">
                  <el-select v-model="query.geneIds" multiple filterable clearable collapse-tags placeholder="全部基因">
                    <el-option v-for="item in facets.genes" :key="item.id" :label="item.name" :value="item.id" />
                  </el-select>
                </el-form-item>
                <el-form-item label="药物">
                  <el-select v-model="query.drugIds" multiple filterable clearable collapse-tags placeholder="全部药物">
                    <el-option v-for="item in facets.drugs" :key="item.id" :label="item.name" :value="item.id" />
                  </el-select>
                </el-form-item>
                <el-form-item label="症状">
                  <el-select v-model="query.symptomIds" multiple filterable clearable collapse-tags placeholder="全部症状">
                    <el-option v-for="item in facets.symptoms" :key="item.id" :label="item.name" :value="item.id" />
                  </el-select>
                </el-form-item>
                <el-form-item label="关键词">
                  <el-select v-model="query.keywordIds" multiple filterable clearable collapse-tags placeholder="全部关键词">
                    <el-option v-for="item in facets.keywords" :key="item.id" :label="item.name" :value="item.id" />
                  </el-select>
                </el-form-item>
              </div>

              <div class="filter-group">
                <div class="filter-group-title">文献信息</div>
                <el-form-item label="期刊">
                  <el-select
                    v-model="query.journal"
                    clearable
                    filterable
                    placeholder="全部期刊"
                    popper-class="journal-select-dropdown"
                  >
                    <el-option v-for="item in facets.journals" :key="item" :label="item" :value="item">
                      <span class="journal-option-text" :title="item">{{ item }}</span>
                    </el-option>
                  </el-select>
                </el-form-item>
                <el-form-item label="年份">
                  <el-input-number v-model="query.yearFrom" :min="1500" :max="2100" placeholder="开始" />
                  <span class="range-text">至</span>
                  <el-input-number v-model="query.yearTo" :min="1500" :max="2100" placeholder="结束" />
                </el-form-item>
                <el-form-item>
                  <el-checkbox v-model="query.hasPdf">仅看有 PDF</el-checkbox>
                </el-form-item>
              </div>

              <div class="filter-group">
                <div class="filter-group-title">管理信息</div>
                <el-form-item label="专题">
                  <el-select v-model="query.topicId" clearable filterable placeholder="全部专题">
                    <el-option v-for="item in facets.topics" :key="item.id" :label="item.name" :value="item.id" />
                  </el-select>
                </el-form-item>
                <el-form-item label="标签">
                  <el-select v-model="query.tagId" clearable filterable placeholder="全部标签">
                    <el-option v-for="item in facets.tags" :key="item.id" :label="item.name" :value="item.id" />
                  </el-select>
                </el-form-item>
              </div>

              <div class="filter-actions">
                <el-button type="success" :loading="loading" @click="handleSearch">筛选文献</el-button>
                <el-button :disabled="loading" @click="resetQuery">重置条件</el-button>
              </div>
            </el-form>
          </div>
        </el-collapse-transition>
      </div>
    </section>

    <section class="quick-entry">
      <button class="entry-card" type="button" @click="scrollToGuide">
        <span class="entry-icon icon-guide" aria-hidden="true">?</span>
        <h3>使用说明</h3>
        <p>了解平台的数据来源、知识索引和适用边界。</p>
      </button>
      <button class="entry-card" type="button" @click="advancedVisible = true">
        <span class="entry-icon icon-search" aria-hidden="true"></span>
        <h3>高级检索</h3>
        <p>按疾病、基因、药物、症状和专题缩小结果范围。</p>
      </button>
      <button class="entry-card" type="button" @click="query.hasPdf = true; handleSearch()">
        <span class="entry-icon icon-download" aria-hidden="true"></span>
        <h3>可下载全文</h3>
        <p>优先筛选带 PDF 或全文链接的开放获取文献。</p>
      </button>
      <RouterLink class="entry-card" to="/literature">
        <span class="entry-icon icon-workbench" aria-hidden="true"></span>
        <h3>文献工作台</h3>
        <p>按专题组织文献，管理标签和研究方向。</p>
      </RouterLink>
    </section>

    <main class="content-wrap">
      <el-alert
        v-if="errorMessage"
        class="state-alert"
        type="error"
        :title="errorMessage"
        show-icon
        :closable="false"
      />

      <section ref="resultsSectionRef" class="results-section">
        <div class="section-heading">
          <div>
            <h2>找到的文献</h2>
            <p>共 {{ pageInfo.total }} 篇文献。结果默认精简展示，详情页提供完整信息。</p>
          </div>
          <RouterLink to="/literature">进入文献工作台</RouterLink>
        </div>

        <div v-loading="loading" class="result-list">
          <el-empty
            v-if="!loading && papers.length === 0"
            description="暂无匹配文献，请尝试更换关键词或减少筛选条件"
          />

          <article v-for="paper in papers" :key="paper.id" class="paper-card">
            <h3 @click="goDetail(paper)">{{ paper.title || '未命名文献' }}</h3>
            <p v-if="paper.titleZh" class="title-zh">{{ paper.titleZh }}</p>
            <p class="abstract">{{ paper.abstractText || '暂无英文摘要' }}</p>
            <p v-if="paper.abstractZh" class="abstract abstract-zh">{{ paper.abstractZh }}</p>

            <div class="paper-meta">
              <span>{{ paper.journal || '未知期刊' }}</span>
              <span>{{ paper.publishYear || '-' }}</span>
              <span v-if="paper.pmid">PMID: {{ paper.pmid }}</span>
              <span v-if="paper.doi">DOI: {{ paper.doi }}</span>
            </div>

            <el-space wrap class="tag-line">
              <el-tag v-for="entity in displayEntities(paper)" :key="`entity-${entity.id}`" type="warning">
                {{ entity.entityType }}：{{ entity.entityName }}
              </el-tag>
              <el-tag v-for="topic in paper.topics || []" :key="`topic-${topic.id}`" type="success" effect="plain">
                {{ topic.name }}
              </el-tag>
              <el-tag v-for="tag in paper.tags || []" :key="`tag-${tag.id}`" effect="plain">
                {{ tag.tagName }}
              </el-tag>
            </el-space>

            <div class="paper-actions">
              <el-button link type="primary" @click="goDetail(paper)">查看详情</el-button>
              <el-button v-if="paper.pdfUrl" link type="success" @click="openExternal(paper.pdfUrl)">PDF 下载</el-button>
              <el-button v-if="paper.fullTextUrl" link type="info" @click="openExternal(paper.fullTextUrl)">全文链接</el-button>
            </div>
          </article>
        </div>

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
      </section>

      <section class="home-panels">
        <div class="panel">
          <h2>热门文献</h2>
          <div v-for="paper in hotPapers" :key="`hot-${paper.id}`" class="compact-paper" @click="goDetail(paper)">
            <a>{{ paper.title }}</a>
            <p>{{ paper.journal || '未知期刊' }} · {{ paper.publishYear || '-' }} · PMID: {{ paper.pmid || '-' }}</p>
          </div>
        </div>

        <div class="panel">
          <h2>平台更新</h2>
          <div class="update-item">
            <a>综合搜索页面升级为系统首页</a>
            <p>支持疾病、基因、药物、症状、关键词和专题联合筛选。</p>
          </div>
          <div class="update-item">
            <a>文献工作台合并文献和专题管理</a>
            <p>后续可以在一个页面完成专题组织、文献筛选和关联管理。</p>
          </div>
          <div class="update-item">
            <a>预留智能综述能力</a>
            <p>文献问答、综述生成、证据片段定位可在毕业设计后续阶段扩展。</p>
          </div>
        </div>
      </section>

      <section id="guide" class="guide-section">
        <h2>平台资源</h2>
        <div class="guide-grid">
          <div>
            <h3>数据来源</h3>
            <p>当前主要对接 PubMed，预留 PMC、Europe PMC、Semantic Scholar 等扩展入口。</p>
          </div>
          <div>
            <h3>知识索引</h3>
            <p>通过疾病、基因、药物、症状、关键词等实体建立文献索引。</p>
          </div>
          <div>
            <h3>免责声明</h3>
            <p>本系统用于毕业设计和科研辅助，不替代医学诊断、治疗或正式临床决策。</p>
          </div>
        </div>
      </section>
    </main>

    <footer class="site-footer">
      <span>NCBI 文献资源</span>
      <span>PubMed</span>
      <span>PMC</span>
      <span>Europe PMC</span>
      <span>Semantic Scholar</span>
    </footer>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import { getSearchFacets, searchPapers, showRequestError } from '../../api/paper'

const router = useRouter()

const loading = ref(false)
const advancedVisible = ref(false)
const errorMessage = ref('')
const papers = ref([])
const resultsSectionRef = ref(null)
const facets = reactive({
  diseases: [],
  genes: [],
  drugs: [],
  symptoms: [],
  keywords: [],
  tags: [],
  topics: [],
  journals: [],
  years: [],
})
const query = reactive({
  q: '',
  diseaseIds: [],
  geneIds: [],
  drugIds: [],
  symptomIds: [],
  keywordIds: [],
  tagId: null,
  topicId: null,
  journal: '',
  yearFrom: null,
  yearTo: null,
  hasPdf: false,
})
const pageInfo = reactive({
  page: 1,
  size: 10,
  total: 0,
  pages: 0,
})

let active = true
let requestSeq = 0

const hotPapers = computed(() => papers.value.slice(0, 5))

function cleanText(value) {
  const text = String(value || '').trim()
  return text || undefined
}

function buildParams() {
  return {
    page: pageInfo.page,
    size: pageInfo.size,
    q: cleanText(query.q),
    diseaseIds: query.diseaseIds,
    geneIds: query.geneIds,
    drugIds: query.drugIds,
    symptomIds: query.symptomIds,
    keywordIds: query.keywordIds,
    tagId: query.tagId || undefined,
    topicId: query.topicId || undefined,
    journal: cleanText(query.journal),
    yearFrom: query.yearFrom || undefined,
    yearTo: query.yearTo || undefined,
    hasPdf: query.hasPdf ? true : undefined,
  }
}

async function loadFacets() {
  try {
    const result = await getSearchFacets()
    if (!active) return
    const data = result?.data || {}
    facets.diseases = Array.isArray(data.diseases) ? data.diseases : []
    facets.genes = Array.isArray(data.genes) ? data.genes : []
    facets.drugs = Array.isArray(data.drugs) ? data.drugs : []
    facets.symptoms = Array.isArray(data.symptoms) ? data.symptoms : []
    facets.keywords = Array.isArray(data.keywords) ? data.keywords : []
    facets.tags = Array.isArray(data.tags) ? data.tags : []
    facets.topics = Array.isArray(data.topics) ? data.topics : []
    facets.journals = Array.isArray(data.journals) ? data.journals : []
    facets.years = Array.isArray(data.years) ? data.years : []
  } catch (error) {
    if (!active) return
    showRequestError(error, '筛选项加载失败')
  }
}

async function loadPapers(shouldScroll = false) {
  if (loading.value) return
  const seq = ++requestSeq
  loading.value = true
  errorMessage.value = ''
  try {
    const result = await searchPapers(buildParams())
    if (!active || seq !== requestSeq) return
    const pageData = result?.data || {}
    papers.value = Array.isArray(pageData.records) ? pageData.records : []
    pageInfo.total = Number(pageData.total || 0)
    pageInfo.pages = Number(pageData.pages || 0)
    pageInfo.page = Number(pageData.current || pageInfo.page)
    pageInfo.size = Number(pageData.size || pageInfo.size)
    if (shouldScroll) {
      await nextTick()
      scrollToResults()
    }
  } catch (error) {
    if (!active || seq !== requestSeq) return
    errorMessage.value = error?.message || '综合搜索失败'
    showRequestError(error, '综合搜索失败')
    if (shouldScroll) {
      await nextTick()
      scrollToResults()
    }
  } finally {
    if (active && seq === requestSeq) {
      loading.value = false
    }
  }
}

function handleSearch() {
  pageInfo.page = 1
  loadPapers(true)
}

function resetQuery() {
  query.q = ''
  query.diseaseIds = []
  query.geneIds = []
  query.drugIds = []
  query.symptomIds = []
  query.keywordIds = []
  query.tagId = null
  query.topicId = null
  query.journal = ''
  query.yearFrom = null
  query.yearTo = null
  query.hasPdf = false
  pageInfo.page = 1
  loadPapers()
}

function handlePageChange(page) {
  pageInfo.page = page
  loadPapers(true)
}

function handleSizeChange(size) {
  pageInfo.size = size
  pageInfo.page = 1
  loadPapers(true)
}

function goDetail(paper) {
  if (!paper?.id) return
  router.push({ name: 'paper-detail', params: { id: paper.id } })
}

function openExternal(url) {
  if (!url) return
  window.open(url, '_blank', 'noopener,noreferrer')
}

function displayEntities(paper) {
  if (!Array.isArray(paper?.entities)) return []
  return paper.entities.filter((entity) => entity.entityType !== 'KEYWORD')
}

function scrollToGuide() {
  document.getElementById('guide')?.scrollIntoView({ behavior: 'smooth' })
}

function scrollToResults() {
  resultsSectionRef.value?.scrollIntoView({
    behavior: 'smooth',
    block: 'start',
  })
}

onMounted(() => {
  loadFacets()
  loadPapers()
})

onBeforeUnmount(() => {
  active = false
  requestSeq += 1
})
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: #ffffff;
  color: #111827;
}

.site-header {
  height: 72px;
  max-width: 1250px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  color: inherit;
  text-decoration: none;
}

.brand-mark {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  background: #0f766e;
  color: #ffffff;
  font-weight: 800;
  font-size: 20px;
}

.brand-title {
  font-weight: 800;
  color: #0f172a;
}

.brand-subtitle {
  margin-top: 3px;
  font-size: 12px;
  color: #64748b;
}

.top-links {
  display: flex;
  gap: 24px;
}

.top-links a {
  color: #334155;
  font-weight: 700;
  text-decoration: none;
}

.top-links a:hover {
  color: #047857;
}

.hero {
  position: relative;
  overflow: hidden;
  background: linear-gradient(145deg, #12365c, #075985 55%, #0f766e);
  color: #ffffff;
}

.network-bg {
  position: absolute;
  inset: 0;
  opacity: 0.24;
  background-image:
    radial-gradient(circle at 12% 18%, rgba(255,255,255,0.55) 0 14px, transparent 15px),
    radial-gradient(circle at 46% 8%, rgba(255,255,255,0.4) 0 18px, transparent 19px),
    radial-gradient(circle at 70% 38%, rgba(255,255,255,0.35) 0 21px, transparent 22px),
    radial-gradient(circle at 84% 72%, rgba(255,255,255,0.28) 0 28px, transparent 29px),
    linear-gradient(35deg, transparent 0 48%, rgba(255,255,255,0.18) 49% 50%, transparent 51%),
    linear-gradient(145deg, transparent 0 58%, rgba(255,255,255,0.16) 59% 60%, transparent 61%);
}

.hero-inner {
  position: relative;
  max-width: 1250px;
  margin: 0 auto;
  padding: 76px 28px 88px;
}

.platform-logo {
  font-size: 42px;
  font-weight: 900;
  letter-spacing: -0.04em;
}

.hero h1 {
  margin: 18px 0 10px;
  font-size: 32px;
}

.hero-intro {
  max-width: 780px;
  margin: 0 0 26px;
  line-height: 1.8;
  color: #dbeafe;
}

.search-box {
  display: flex;
  max-width: 960px;
  height: 74px;
  gap: 0;
  overflow: hidden;
  border: 2px solid #dbeafe;
  border-radius: 4px;
  background: #ffffff;
  box-shadow: 0 20px 45px rgba(15, 23, 42, 0.22);
}

.search-box :deep(.el-input) {
  height: 100%;
}

.search-box :deep(.el-input__wrapper) {
  height: 100%;
  border-radius: 0;
  padding: 0 26px;
  box-shadow: none;
}

.search-box :deep(.el-input__inner) {
  height: 100%;
  font-size: 18px;
}

.search-box .el-button {
  width: 145px;
  height: 100%;
  border: none;
  border-radius: 0;
  font-size: 18px;
  font-weight: 800;
}

.advanced-link {
  margin-top: 12px;
  border: none;
  background: transparent;
  color: #ffffff;
  font-weight: 700;
  cursor: pointer;
  padding: 0;
}

.advanced-panel {
  max-width: 1050px;
  margin-top: 18px;
  padding: 18px 18px 14px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.96);
  color: #111827;
}

.advanced-title {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 14px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e5e7eb;
}

.advanced-title strong {
  color: #0f172a;
  font-size: 16px;
}

.advanced-title span {
  color: #64748b;
  font-size: 13px;
}

.advanced-form :deep(.el-select) {
  width: 210px;
}

:global(.journal-select-dropdown) {
  max-width: 420px;
}

:global(.journal-select-dropdown .el-select-dropdown__item) {
  max-width: 420px;
}

:global(.journal-option-text) {
  display: block;
  max-width: 360px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.filter-group {
  margin-bottom: 12px;
  padding: 12px 12px 0;
  border-radius: 10px;
  background: #f8fafc;
}

.filter-group-title {
  margin-bottom: 10px;
  color: #075985;
  font-size: 13px;
  font-weight: 800;
}

.filter-actions {
  padding: 4px 12px 0;
}

.range-text {
  margin: 0 8px;
  color: #64748b;
}

.quick-entry {
  max-width: 1120px;
  margin: 34px auto;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  padding: 0 28px;
}

.entry-card {
  border: 1px solid #e5e7eb;
  border-radius: 18px;
  padding: 28px;
  background: #ffffff;
  text-align: center;
  text-decoration: none;
  color: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
}

.entry-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.12);
}

.entry-icon {
  width: 74px;
  height: 74px;
  margin: 0 auto 18px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #eff6ff;
  color: #075985;
  border: 1px solid #dbeafe;
  position: relative;
}

.entry-icon::before,
.entry-icon::after {
  content: "";
  position: absolute;
  box-sizing: border-box;
}

.icon-guide {
  font-size: 38px;
  font-weight: 600;
  line-height: 1;
}

.icon-search::before {
  width: 27px;
  height: 27px;
  border: 3px solid #075985;
  border-radius: 50%;
  left: 50%;
  top: 50%;
  transform: translate(-58%, -58%);
}

.icon-search::after {
  width: 15px;
  height: 3px;
  border-radius: 999px;
  background: #075985;
  transform: rotate(45deg);
  left: 44px;
  top: 46px;
  transform-origin: left center;
}

.icon-download::before {
  width: 3px;
  height: 30px;
  border-radius: 999px;
  background: #075985;
  top: 18px;
}

.icon-download::after {
  width: 20px;
  height: 20px;
  border-right: 3px solid #075985;
  border-bottom: 3px solid #075985;
  transform: rotate(45deg);
  top: 31px;
}

.icon-workbench::before {
  width: 32px;
  height: 38px;
  border: 3px solid #075985;
  border-radius: 4px;
  top: 18px;
}

.icon-workbench::after {
  width: 18px;
  height: 3px;
  border-radius: 999px;
  background: #075985;
  top: 29px;
  box-shadow: 0 9px 0 #075985, 0 18px 0 #075985;
}

.entry-card h3 {
  margin: 0 0 10px;
  font-size: 20px;
}

.entry-card p {
  margin: 0;
  color: #64748b;
  line-height: 1.6;
}

.content-wrap {
  max-width: 1250px;
  margin: 0 auto;
  padding: 0 28px 48px;
}

.state-alert {
  margin-bottom: 16px;
}

.section-heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  border-top: 1px solid #cbd5e1;
  padding-top: 28px;
  margin-bottom: 18px;
}

.section-heading h2 {
  margin: 0 0 8px;
  font-size: 26px;
}

.section-heading p {
  margin: 0;
  color: #64748b;
}

.section-heading a {
  color: #0369a1;
  font-weight: 700;
  text-decoration: none;
}

.result-list {
  min-height: 160px;
}

.paper-card {
  padding: 18px 0;
  border-bottom: 1px solid #e5e7eb;
}

.paper-card h3 {
  margin: 0 0 6px;
  color: #0065a8;
  cursor: pointer;
  font-size: 19px;
}

.paper-card h3:hover {
  text-decoration: underline;
}

.title-zh {
  margin: 0 0 8px;
  color: #374151;
  font-weight: 700;
}

.abstract {
  margin: 6px 0;
  color: #334155;
  line-height: 1.7;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.abstract-zh {
  color: #64748b;
}

.paper-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 10px;
  color: #37613b;
  font-size: 14px;
}

.tag-line {
  margin-top: 12px;
}

.paper-actions {
  display: flex;
  gap: 8px;
  margin-top: 10px;
}

.pagination-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
}

.home-panels {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 36px;
  margin-top: 56px;
  padding-top: 34px;
  border-top: 1px solid #cbd5e1;
}

.panel {
  padding: 0 28px;
}

.panel + .panel {
  border-left: 1px solid #94a3b8;
}

.panel h2 {
  margin: 0 0 20px;
}

.compact-paper,
.update-item {
  margin-bottom: 18px;
  cursor: pointer;
}

.compact-paper a,
.update-item a {
  color: #0065a8;
  font-size: 16px;
  font-weight: 700;
}

.compact-paper p,
.update-item p {
  margin: 5px 0 0;
  color: #475569;
  line-height: 1.6;
}

.guide-section {
  margin-top: 48px;
  padding: 28px;
  border-radius: 18px;
  background: #f8fafc;
}

.guide-section h2 {
  margin: 0 0 18px;
}

.guide-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.guide-grid h3 {
  margin: 0 0 8px;
}

.guide-grid p {
  margin: 0;
  color: #64748b;
  line-height: 1.7;
}

.site-footer {
  min-height: 76px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 28px;
  background: #e5e7eb;
  color: #0f172a;
}

@media (max-width: 900px) {
  .site-header,
  .top-links,
  .search-box,
  .section-heading {
    flex-direction: column;
    align-items: flex-start;
  }

  .site-header {
    height: auto;
    gap: 12px;
    padding: 18px 28px;
  }

  .quick-entry,
  .home-panels,
  .guide-grid {
    grid-template-columns: 1fr;
  }

  .panel + .panel {
    border-left: none;
    border-top: 1px solid #cbd5e1;
    padding-top: 24px;
  }
}
</style>
