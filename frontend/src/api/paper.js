import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({
  baseURL: '/api',
  timeout: 15000,
  paramsSerializer: {
    serialize(params) {
      const searchParams = new URLSearchParams()
      Object.entries(params || {}).forEach(([key, value]) => {
        if (value === undefined || value === null || value === '') return
        if (Array.isArray(value)) {
          value.forEach((item) => {
            if (item !== undefined && item !== null && item !== '') {
              searchParams.append(key, item)
            }
          })
          return
        }
        searchParams.append(key, value)
      })
      return searchParams.toString()
    },
  },
})

http.interceptors.response.use(
  (response) => {
    const result = response.data
    if (typeof result?.code === 'number' && result.code !== 200) {
      return Promise.reject(new Error(result.message || '请求失败'))
    }
    return result
  },
  (error) => {
    const message = error.response?.data?.message || error.message || '网络请求失败'
    return Promise.reject(new Error(message))
  },
)

export function getPapers(params) {
  return http.get('/papers', { params })
}

export function getPaperById(id) {
  return http.get(`/papers/${id}`)
}

export function searchPapers(params) {
  return http.get('/search/papers', { params })
}

export function getSearchFacets() {
  return http.get('/search/facets')
}

export function getPaperTags(paperId) {
  return http.get(`/papers/${paperId}/tags`)
}

export function addPaperTag(paperId, tagId) {
  return http.post(`/papers/${paperId}/tags/${tagId}`)
}

export function removePaperTag(paperId, tagId) {
  return http.delete(`/papers/${paperId}/tags/${tagId}`)
}

export function getPaperTopics(paperId) {
  return http.get(`/papers/${paperId}/topics`)
}

export function addPaperTopic(paperId, topicId) {
  return http.post(`/papers/${paperId}/topics/${topicId}`)
}

export function removePaperTopic(paperId, topicId) {
  return http.delete(`/papers/${paperId}/topics/${topicId}`)
}

export function getTags() {
  return http.get('/tags')
}

export function createTag(data) {
  return http.post('/tags', data)
}

export function updateTag(id, data) {
  return http.put(`/tags/${id}`, data)
}

export function deleteTag(id) {
  return http.delete(`/tags/${id}`)
}

export function getTopics() {
  return http.get('/topics')
}

export function createTopic(data) {
  return http.post('/topics', data)
}

export function updateTopic(id, data) {
  return http.put(`/topics/${id}`, data)
}

export function deleteTopic(id) {
  return http.delete(`/topics/${id}`)
}

export function collectPubMed(data) {
  return http.post('/crawl/pubmed', data)
}

export function showRequestError(error, fallback = '数据加载失败') {
  ElMessage.error(error?.message || fallback)
}

export default {
  getPapers,
  getPaperById,
  searchPapers,
  getSearchFacets,
  getPaperTags,
  addPaperTag,
  removePaperTag,
  getPaperTopics,
  addPaperTopic,
  removePaperTopic,
  getTags,
  createTag,
  updateTag,
  deleteTag,
  getTopics,
  createTopic,
  updateTopic,
  deleteTopic,
  collectPubMed,
  showRequestError,
}
