import { createRouter, createWebHistory } from 'vue-router'

import MainLayout from '../layout/MainLayout.vue'
import SearchPage from '../views/search/SearchPage.vue'
import LiteratureWorkbench from '../views/literature/LiteratureWorkbench.vue'
import PaperDetail from '../views/paper/PaperDetail.vue'
import TagList from '../views/tag/TagList.vue'
import CrawlPage from '../views/crawl/CrawlPage.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: SearchPage,
      meta: { title: '综合搜索' },
    },
    {
      path: '/search',
      redirect: '/',
    },
    {
      path: '/dashboard',
      redirect: '/',
    },
    {
      path: '/papers',
      redirect: '/literature',
    },
    {
      path: '/topics',
      redirect: '/literature',
    },
    {
      path: '/',
      component: MainLayout,
      children: [
        {
          path: 'literature',
          name: 'literature-workbench',
          component: LiteratureWorkbench,
          meta: { title: '文献工作台' },
        },
        {
          path: 'papers/:id',
          name: 'paper-detail',
          component: PaperDetail,
          meta: { title: '文献详情' },
        },
        {
          path: 'tags',
          name: 'tag-list',
          component: TagList,
          meta: { title: '标签管理' },
        },
        {
          path: 'crawl',
          name: 'crawl-page',
          component: CrawlPage,
          meta: { title: '采集任务' },
        },
      ],
    },
  ],
})

export default router
