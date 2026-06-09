import request from '../request'

export function getDashboard() {
  return request.get('/admin/dashboard')
}

export function getUsers(params) {
  return request.get('/admin/users', { params })
}

export function getUserDetail(id) {
  return request.get(`/admin/users/${id}`)
}

export function enableUser(id) {
  return request.put(`/admin/users/${id}/enable`)
}

export function disableUser(id) {
  return request.put(`/admin/users/${id}/disable`)
}

export function getUserWeights(id) {
  return request.get(`/admin/users/${id}/weights`)
}

export function getUserPlans(id) {
  return request.get(`/admin/users/${id}/plans`)
}

export function getUserProfile(id) {
  return request.get(`/admin/users/${id}/profile`)
}

export function deleteUserWeight(recordId) {
  return request.delete(`/admin/users/weights/${recordId}`)
}

export function deleteUserPlan(planId) {
  return request.delete(`/admin/users/plans/${planId}`)
}

export function generateVipCodes(count) {
  return request.post('/admin/vip-codes/generate', undefined, { params: { count } })
}

export function getVipCodes() {
  return request.get('/admin/vip-codes/list')
}

export function createKnowledge(data) {
  return request.post('/admin/knowledge', data)
}

export function updateKnowledge(id, data) {
  return request.put(`/admin/knowledge/${id}`, data)
}

export function deleteKnowledge(id) {
  return request.delete(`/admin/knowledge/${id}`)
}

export function getKnowledgeList(params) {
  return request.get('/admin/knowledge/list', { params })
}

export function enableKnowledge(id) {
  return request.put(`/admin/knowledge/${id}/enable`)
}

export function disableKnowledge(id) {
  return request.put(`/admin/knowledge/${id}/disable`)
}
