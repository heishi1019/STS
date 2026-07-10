import { createRouter, createWebHistory } from 'vue-router'

import MainLayout from '../layout/MainLayout.vue'
import Dashboard from '../views/dashboard/Dashboard.vue'
import PaperList from '../views/paper/PaperList.vue'
import PaperDetail from '../views/paper/PaperDetail.vue'
import TagList from '../views/tag/TagList.vue'
import TopicList from '../views/topic/TopicList.vue'
import CrawlPage from '../views/crawl/CrawlPage.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: MainLayout,
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: Dashboard,
          meta: { title: '仪表盘' },
        },
        {
          path: 'papers',
          name: 'paper-list',
          component: PaperList,
          meta: { title: '文献管理' },
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
          path: 'topics',
          name: 'topic-list',
          component: TopicList,
          meta: { title: '专题管理' },
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
