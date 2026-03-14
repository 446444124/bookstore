import { reactive } from 'vue'

const state = reactive({
  token: localStorage.getItem('token') || '',
  userId: localStorage.getItem('userId') || ''
})

export function setAuth(token, userId) {
  state.token = token || ''
  state.userId = userId || ''
  if (token) localStorage.setItem('token', token)
  if (userId) localStorage.setItem('userId', userId)
}

export function clearAuth() {
  state.token = ''
  state.userId = ''
  localStorage.removeItem('token')
  localStorage.removeItem('userId')
}

export function useAuth() {
  return state
}
