import { reactive } from 'vue'

const state = reactive({
  token: localStorage.getItem('token') || '',
  userId: localStorage.getItem('userId') || '',
  /** 与后端 employee.position 一致，如「店长」 */
  position: localStorage.getItem('adminPosition') || ''
})

export function setAuth(token, userId, position) {
  state.token = token || ''
  state.userId = userId || ''
  state.position = (position != null && position !== '') ? String(position).trim() : ''
  if (token) localStorage.setItem('token', token)
  if (userId) localStorage.setItem('userId', userId)
  if (state.position) localStorage.setItem('adminPosition', state.position)
  else localStorage.removeItem('adminPosition')
}

export function clearAuth() {
  state.token = ''
  state.userId = ''
  state.position = ''
  localStorage.removeItem('token')
  localStorage.removeItem('userId')
  localStorage.removeItem('adminPosition')
}

export function isStoreManagerAuth() {
  return state.position === '店长'
}

export function useAuth() {
  return state
}
