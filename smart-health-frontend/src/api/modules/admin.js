import request from '../request'

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
