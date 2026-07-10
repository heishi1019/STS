<template>
  <el-container class="main-layout">
    <el-aside class="sidebar" width="220px">
      <div class="brand">
        <div class="brand-logo">B</div>
        <div>
          <div class="brand-title">Biomed Lit</div>
          <div class="brand-subtitle">文献知识服务平台</div>
        </div>
      </div>

      <el-menu
        class="side-menu"
        :default-active="activePath"
        router
        background-color="#111827"
        text-color="#cbd5e1"
        active-text-color="#ffffff"
      >
        <el-menu-item index="/dashboard">仪表盘</el-menu-item>
        <el-menu-item index="/papers">文献管理</el-menu-item>
        <el-menu-item index="/tags">标签管理</el-menu-item>
        <el-menu-item index="/topics">专题管理</el-menu-item>
        <el-menu-item index="/crawl">采集任务</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="topbar">
        <div>
          <h1>{{ pageTitle }}</h1>
          <p>生物医学文献采集、管理、检索与分析</p>
        </div>
      </el-header>

      <el-main class="page-main">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const activePath = computed(() => route.path)
const pageTitle = computed(() => route.meta.title || '生物医学文献知识服务平台')
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  background: #f5f7fb;
}

.sidebar {
  background: #111827;
  color: #ffffff;
}

.brand {
  height: 72px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 18px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.brand-logo {
  width: 38px;
  height: 38px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  background: #409eff;
  font-weight: 700;
}

.brand-title {
  font-size: 16px;
  font-weight: 700;
}

.brand-subtitle {
  margin-top: 2px;
  font-size: 12px;
  color: #94a3b8;
}

.side-menu {
  border-right: none;
}

.topbar {
  height: 72px;
  display: flex;
  align-items: center;
  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
}

.topbar h1 {
  margin: 0;
  font-size: 20px;
  color: #111827;
}

.topbar p {
  margin: 6px 0 0;
  font-size: 13px;
  color: #6b7280;
}

.page-main {
  padding: 24px;
}
</style>
