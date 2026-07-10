<template>
  <div class="dashboard-page">
    <el-row :gutter="16">
      <el-col :span="6" v-for="item in cards" :key="item.label">
        <el-card shadow="never" class="stat-card">
          <div class="stat-value">{{ item.value }}</div>
          <div class="stat-label">{{ item.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="section-card">
      <template #header>今日开发重点</template>
      <el-timeline>
        <el-timeline-item timestamp="前端目录结构">完成页面模块拆分</el-timeline-item>
        <el-timeline-item timestamp="接口对接">下一步连接后端文献接口</el-timeline-item>
        <el-timeline-item timestamp="统计分析">后续接入 ECharts 图表</el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup>
const cards = [
  { label: '文献数量', value: '待统计' },
  { label: '专题数量', value: '待统计' },
  { label: '标签数量', value: '待统计' },
  { label: '采集任务', value: '待统计' },
]
</script>

<style scoped>
.stat-card {
  border-radius: 12px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #111827;
}

.stat-label {
  margin-top: 8px;
  color: #6b7280;
}

.section-card {
  margin-top: 16px;
  border-radius: 12px;
}
</style>
