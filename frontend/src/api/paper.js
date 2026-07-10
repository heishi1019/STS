import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({
  baseURL: '/api',
  timeout: 15000,
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

export function showRequestError(error, fallback = '数据加载失败') {
  ElMessage.error(error?.message || fallback)
}

export default {
  getPapers,
  getPaperById,
  getTags,
  createTag,
  updateTag,
  deleteTag,
  getTopics,
  createTopic,
  updateTopic,
  deleteTopic,
  showRequestError,
}
