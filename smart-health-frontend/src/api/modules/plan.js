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

export function normalizeGenerateCount(count) {
  if (!count) {
    return undefined
  }
  return {
    used: count.used ?? count.usedCount ?? 0,
    limit: count.limit ?? count.limitCount ?? 0,
    remaining: count.remaining ?? count.remainingCount ?? 0
  }
}
