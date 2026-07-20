<template>
  <el-container class="main-layout">
    <el-header class="top-nav">
      <RouterLink class="brand" to="/">
        <div class="brand-logo">B</div>
        <div>
          <div class="brand-title">生物医学文献平台</div>
          <div class="brand-subtitle">Literature Knowledge Service</div>
        </div>
      </RouterLink>

      <nav class="nav-links">
        <RouterLink to="/">首页</RouterLink>
        <RouterLink to="/literature">文献工作台</RouterLink>
        <RouterLink to="/tags">标签管理</RouterLink>
        <RouterLink to="/crawl">采集任务</RouterLink>
      </nav>
    </el-header>

    <el-main class="page-main">
      <RouterView />
    </el-main>
  </el-container>
</template>

<style scoped>
.main-layout {
  min-height: 100vh;
  background: #f5f7fb;
}

.top-nav {
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 40px;
  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  color: inherit;
  text-decoration: none;
}

.brand-logo {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  background: #0f766e;
  color: #ffffff;
  font-weight: 800;
}

.brand-title {
  font-size: 17px;
  font-weight: 700;
  color: #0f172a;
}

.brand-subtitle {
  margin-top: 2px;
  font-size: 12px;
  color: #64748b;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 24px;
}

.nav-links a {
  color: #334155;
  text-decoration: none;
  font-size: 14px;
  font-weight: 600;
}

.nav-links a.router-link-active {
  color: #047857;
}

.page-main {
  padding: 24px 40px;
}
</style>
