<template>
  <el-card shadow="never" class="page-card">
    <template #header>采集任务</template>

    <el-form label-width="100px" class="crawl-form">
      <el-form-item label="检索关键词">
        <el-input placeholder="例如：diabetes mellitus" />
      </el-form-item>
      <el-form-item label="数据源">
        <el-select placeholder="请选择数据源" model-value="PubMed">
          <el-option label="PubMed" value="PubMed" />
          <el-option label="Europe PMC" value="Europe PMC" disabled />
          <el-option label="Semantic Scholar" value="Semantic Scholar" disabled />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary">开始采集</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<style scoped>
.page-card {
  border-radius: 12px;
}

.crawl-form {
  max-width: 640px;
}
</style>
