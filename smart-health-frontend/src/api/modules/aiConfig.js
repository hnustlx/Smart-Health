import request from '../request'

export function getAiConfig() {
  return request.get('/ai-config')
}

export function saveAiConfig(data) {
  return request.post('/ai-config', data)
}

export function resetAiConfig() {
  return request.delete('/ai-config')
}
