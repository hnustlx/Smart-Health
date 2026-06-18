import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'
import { clearAuth, getToken } from '../utils/auth'

const useMockData = import.meta.env.DEV && import.meta.env.VITE_USE_MOCK_DATA === 'true'
const mockAdapter = async (config) => {
  const { createMockResponse } = await import('../dev/mockData')
  return createMockResponse(config)
}

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  timeout: 30000,
  adapter: useMockData ? mockAdapter : undefined
})

function getLoginPath() {
  return router.currentRoute.value.path.startsWith('/admin') ? '/admin/login' : '/login'
}

request.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const body = response.data
    if (!body || typeof body.code === 'undefined') {
      return body
    }
    if (body.code === 200) {
      return body.data
    }
    if (body.code === 401) {
      clearAuth()
      ElMessage.warning(body.message || '登录已失效，请重新登录')
      router.push(getLoginPath())
      return Promise.reject(body)
    }
    ElMessage.error(body.message || '请求失败')
    return Promise.reject(body)
  },
  (error) => {
    const status = error.response?.status
    const data = error.response?.data
    const message = data?.message || error.message || '网络异常'
    if (status === 401) {
      if (data?.code === 401) {
        ElMessage.error(message)
      } else {
        clearAuth()
        ElMessage.warning('登录已失效，请重新登录')
        router.push(getLoginPath())
      }
    } else {
      ElMessage.error(message)
    }
    return Promise.reject(error)
  }
)

export default request
