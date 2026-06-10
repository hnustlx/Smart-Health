import request from '../request'

export function askHealthQuestion(data) {
  return request.post('/chat/ask', data)
}
