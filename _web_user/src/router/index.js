import { createRouter, createWebHashHistory } from 'vue-router'
import Home from '../pages/Home.vue'
import Login from '../pages/Login.vue'
import Browse from '../pages/Browse.vue'
import BookDetail from '../pages/BookDetail.vue'
import Cart from '../pages/Cart.vue'
import Profile from '../pages/Profile.vue'

const routes = [
  { path: '/', component: Home },
  { path: '/login', component: Login },
  { path: '/browse', component: Browse },
  { path: '/book/:id', component: BookDetail },
  { path: '/cart', component: Cart },
  { path: '/profile', component: Profile }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  const token = localStorage.getItem('token') || ''
  const userId = localStorage.getItem('userId') || ''
  const isLogin = to.path === '/login'
  const isHome = to.path === '/'
  if (!token || !userId) {
    if (!isLogin && !isHome) return next({ path: '/login', query: { redirect: to.fullPath, msg: '请先登录' } })
    return next()
  }
  try {
    const resp = await fetch(`/user/user/${encodeURIComponent(userId)}`, {
      method: 'GET',
      headers: { authentication: token }
    })
    if (!resp.ok) {
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      localStorage.removeItem('userRealName')
      localStorage.removeItem('userUsername')
      localStorage.removeItem('userAvatar')
      if (!isLogin && !isHome) return next({ path: '/login', query: { redirect: to.fullPath, msg: '请先登录' } })
      return next()
    }
  } catch (_) {
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('userRealName')
    localStorage.removeItem('userUsername')
    localStorage.removeItem('userAvatar')
    if (!isLogin && !isHome) return next({ path: '/login', query: { redirect: to.fullPath, msg: '请先登录' } })
    return next()
  }
  return next()
})

export default router
