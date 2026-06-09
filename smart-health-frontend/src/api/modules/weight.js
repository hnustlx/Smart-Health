import request from '../request'

export function addWeight(data) {
  return request.post('/weight/add', data)
}

export function getWeightHistory() {
  return request.get('/weight/history')
}

export function deleteWeight(id) {
  return request.delete(`/weight/${id}`)
}
