import request from '../request'

export function getCheckinStatus() {
  return request.get('/checkin/status')
}

export function checkinToday() {
  return request.post('/checkin/today')
}
