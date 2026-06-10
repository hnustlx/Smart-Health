import request from '../request'

export function getProfile() {
  return request.get('/profile')
}

export function createProfile(data) {
  return request.post('/profile', data)
}

export function updateProfile(data) {
  return request.put('/profile', data)
}
