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
  timeout: 15000,
  adapter: useMockData ? mockAdapter : undefined
})

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
    ElMessage.error(body.message || '请求失败')
    return Promise.reject(body)
  },
  (error) => {
    const status = error.response?.status
    const message = error.response?.data?.message || error.message || '网络异常'
    if (status === 401) {
      clearAuth()
      ElMessage.warning('登录已失效，请重新登录')
      router.push('/login')
    } else {
      ElMessage.error(message)
    }
    return Promise.reject(error)
  }
)

export default request
