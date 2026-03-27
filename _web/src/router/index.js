import { createRouter, createWebHashHistory } from 'vue-router'
import Login from '../pages/Login.vue'
import AdminLayout from '../layouts/AdminLayout.vue'
import Dashboard from '../pages/Dashboard.vue'
import Books from '../pages/Books.vue'
import BookEdit from '../pages/BookEdit.vue'
import Categories from '../pages/Categories.vue'
import Employees from '../pages/Employees.vue'
import Orders from '../pages/Orders.vue'
import Profile from '../pages/Profile.vue'
import SecondHand from '../pages/SecondHand.vue'
import SecondHandConfig from '../pages/SecondHandConfig.vue'
import SystemNotice from '../pages/SystemNotice.vue'
import CarouselConfig from '../pages/CarouselConfig.vue'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: '/', redirect: '/login' },
    { path: '/login', component: Login },
    {
      path: '/admin',
      component: AdminLayout,
      children: [
        { path: '', redirect: '/admin/dashboard' },
        { path: 'dashboard', component: Dashboard },
        { path: 'books', component: Books },
        { path: 'books/create', component: BookEdit },
        { path: 'books/:id/edit', component: BookEdit },
        { path: 'categories', component: Categories },
        { path: 'orders', component: Orders },
        { path: 'orders/pending-confirm', component: Orders },
        { path: 'orders/pending-delivery', component: Orders },
        { path: 'orders/pending-complete', component: Orders },
        { path: 'orders/return-review', component: Orders },
        { path: 'employees', component: Employees },
        { path: 'second-hand', component: SecondHand },
        { path: 'second-hand-config', component: SecondHandConfig },
        { path: 'carousel', component: CarouselConfig },
        { path: 'system-notice', component: SystemNotice },
        { path: 'profile', component: Profile }
      ]
    }
  ]
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path.startsWith('/admin') && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
