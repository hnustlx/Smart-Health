const TOKEN_KEY = 'smart_health_token'
const USER_KEY = 'smart_health_user'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function getUser() {
  const value = localStorage.getItem(USER_KEY)
  return value ? JSON.parse(value) : null
}

export function setUser(user) {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function clearAuth(clearUser = true) {
  localStorage.removeItem(TOKEN_KEY)
  if (clearUser) {
    localStorage.removeItem(USER_KEY)
  }
}
