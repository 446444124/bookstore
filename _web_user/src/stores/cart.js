import { ref } from 'vue'

export const cartCount = ref(0)

export const refreshCartCount = async () => {
  try {
    const token = localStorage.getItem('token') || ''
    const resp = await fetch('/user/cart/list', {
      method: 'GET',
      headers: token ? { authentication: token } : {}
    })
    const ct = resp.headers.get('content-type') || ''
    let data = {}
    if (ct.includes('application/json')) {
      try { data = await resp.json() } catch (_) {}
    }
    if (resp.ok) {
      const d = data?.data ?? data
      const rows = Array.isArray(d) ? d : d?.records || d?.list || d?.items || d?.rows || d?.data || []
      const count = (rows || []).reduce((sum, it) => {
        const n = Number(it?.num ?? it?.quantity ?? 1) || 0
        return sum + n
      }, 0)
      cartCount.value = count
    } else {
      cartCount.value = 0
    }
  } catch (_) {
    cartCount.value = 0
  }
}

export const resetCartCount = () => {
  cartCount.value = 0
}
