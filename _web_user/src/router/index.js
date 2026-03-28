import { createRouter, createWebHashHistory } from 'vue-router'
import Home from '../pages/Home.vue'
import Login from '../pages/Login.vue'
import ForgotPassword from '../pages/ForgotPassword.vue'
import Browse from '../pages/Browse.vue'
import BookDetail from '../pages/BookDetail.vue'
import Cart from '../pages/Cart.vue'
import Profile from '../pages/Profile.vue'
import PaySuccess from '../pages/PaySuccess.vue'
import WalletRechargeSuccess from '../pages/WalletRechargeSuccess.vue'
import Orders from '../pages/Orders.vue'
import SecondHandBrowse from '../pages/SecondHandBrowse.vue'
import SecondHandDetail from '../pages/SecondHandDetail.vue'
import SecondHandSell from '../pages/SecondHandSell.vue'
import SpecialOffer from '../pages/SpecialOffer.vue'

const routes = [
  { path: '/', component: Home },
  { path: '/login', component: Login },
  { path: '/forgot-password', component: ForgotPassword },
  { path: '/browse', component: Browse },
  { path: '/book/:id', component: BookDetail },
  { path: '/paysuccess', component: PaySuccess },
  { path: '/wallet-rechargesuccess', component: WalletRechargeSuccess },
  { path: '/profile', component: Profile },
  { path: '/cart', component: Cart },
  { path: '/orders', component: Orders },
  { path: '/second-hand', component: SecondHandBrowse },
  { path: '/second-hand/sell', component: SecondHandSell },
  { path: '/second-hand/:id', component: SecondHandDetail },
  { path: '/special-offer', component: SpecialOffer }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  const token = localStorage.getItem('token') || ''
  const userId = localStorage.getItem('userId') || ''
  const isLogin = to.path === '/login'
  const isForgotPassword = to.path === '/forgot-password'
  const isHome = to.path === '/'
  const isSecondHandPublic =
    to.path === '/second-hand' ||
    (to.path.startsWith('/second-hand/') && to.path !== '/second-hand/sell' && !to.path.endsWith('/sell'))
  // 图书详情可匿名浏览（加入购物车仍须登录）
  const isBookDetail = /^\/book\/[^/]+$/.test(to.path)
  if (!token || !userId) {
    if (!isLogin && !isForgotPassword && !isHome && !isSecondHandPublic && !isBookDetail) {
      return next({ path: '/login', query: { redirect: to.fullPath, msg: '请先登录' } })
    }
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
      if (!isLogin && !isForgotPassword && !isHome) return next({ path: '/login', query: { redirect: to.fullPath, msg: '请先登录' } })
      return next()
    }
  } catch (_) {
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('userRealName')
    localStorage.removeItem('userUsername')
    localStorage.removeItem('userAvatar')
    if (!isLogin && !isForgotPassword && !isHome) return next({ path: '/login', query: { redirect: to.fullPath, msg: '请先登录' } })
    return next()
  }
  return next()
})

export default router
