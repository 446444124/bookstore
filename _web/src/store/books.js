import { reactive } from 'vue'

const stored = localStorage.getItem('bookIds')
const state = reactive({
  ids: stored ? JSON.parse(stored) : []
})

export function setBookIds(ids) {
  const list = Array.isArray(ids) ? ids.filter(Boolean) : []
  state.ids = list
  localStorage.setItem('bookIds', JSON.stringify(list))
}

export function useBooksStore() {
  return state
}
