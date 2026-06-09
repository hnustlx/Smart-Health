import request from '../request'

export function generatePlan() {
  return request.post('/plan/generate')
}

export function getPlanHistory() {
  return request.get('/plan/history')
}

export function getPlanDetail(id) {
  return request.get(`/plan/${id}`)
}

export function getGenerateCount() {
  return request.get('/plan/generate-count')
}
