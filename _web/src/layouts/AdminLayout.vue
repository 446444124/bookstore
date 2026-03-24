<template>
  <el-container class="admin">
    <el-aside width="200px" class="aside">
      <el-menu :default-active="active" router>
        <el-menu-item index="/admin/dashboard">仪表盘</el-menu-item>
        <el-menu-item index="/admin/books">图书管理</el-menu-item>
        <el-menu-item index="/admin/categories">分类管理</el-menu-item>
        <el-menu-item index="/admin/orders">订单管理</el-menu-item>
        <el-menu-item index="/admin/orders/pending-confirm">
          <span class="menu-with-dot">待接单<span v-if="showDot(2)" class="menu-badge">{{ badgeText(2) }}</span></span>
        </el-menu-item>
        <el-menu-item index="/admin/orders/pending-delivery">
          <span class="menu-with-dot">待配送<span v-if="showDot(3)" class="menu-badge">{{ badgeText(3) }}</span></span>
        </el-menu-item>
        <el-menu-item index="/admin/orders/pending-complete">
          <span class="menu-with-dot">待完成<span v-if="showDot(4)" class="menu-badge">{{ badgeText(4) }}</span></span>
        </el-menu-item>
        <el-menu-item index="/admin/orders/return-review">
          <span class="menu-with-dot">退货申请<span v-if="showDot(7)" class="menu-badge">{{ badgeText(7) }}</span></span>
        </el-menu-item>
        <el-menu-item index="/admin/employees">员工管理</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div>图书系统 · 管理后台</div>
        <div class="header-right">
          <div
            class="profile-link"
            @click="goProfile"
            title="个人主页"
            tabindex="0"
            @keydown.enter="goProfile"
            @keydown.space.prevent="goProfile"
          >
            <img class="avatar" :src="avatarSrc" @error="onAvatarError" alt="管理员头像" />
            <div class="user-info">
              <div class="line">姓名：{{ displayName }}</div>
              <div class="line">工号：{{ displayEmpNo }}</div>
            </div>
            <span class="home-label">个人主页</span>
            <span class="chevron">›</span>
          </div>
          <el-button size="small" type="danger" @click="onLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { http } from '../api/http'
import { useAuth, clearAuth } from '../store/auth'
import { useRouter } from 'vue-router'
const route = useRoute()
const active = computed(() => route.path)
const router = useRouter()
const auth = useAuth()
const user = ref(null)
const statusCount = ref({})
let timer = null
const displayName = computed(() => {
  if (!user.value) return '—'
  return user.value.realName || user.value.name || '—'
})
const displayEmpNo = computed(() => (user.value && user.value.empNo) ? user.value.empNo : (auth.userId || '—'))
const avatarSrc = ref('data:image/svg+xml;utf8,<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"36\" height=\"36\" viewBox=\"0 0 36 36\"><defs><style>.c{fill:%23e5e7eb}.d{fill:%239ca3af}</style></defs><circle cx=\"18\" cy=\"18\" r=\"18\" class=\"c\"/><circle cx=\"18\" cy=\"13\" r=\"6\" class=\"d\"/><path d=\"M6 30c2.8-6 9.2-7 12-7s9.2 1 12 7\" class=\"d\"/></svg>')
const onAvatarError = () => {
  avatarSrc.value = 'data:image/svg+xml;utf8,<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"36\" height=\"36\" viewBox=\"0 0 36 36\"><defs><style>.c{fill:%23e5e7eb}.d{fill:%239ca3af}</style></defs><circle cx=\"18\" cy=\"18\" r=\"18\" class=\"c\"/><circle cx=\"18\" cy=\"13\" r=\"6\" class=\"d\"/><path d=\"M6 30c2.8-6 9.2-7 12-7s9.2 1 12 7\" class=\"d\"/></svg>'
}
onMounted(async () => {
  if (auth.userId) {
    const resp = await http(`/admin/admin/${auth.userId}`)
    if (resp && resp.code === 1) {
      user.value = resp.data
      const u = user.value
      if (u?.avatar) avatarSrc.value = u.avatar
      else if (u?.avatarUrl) avatarSrc.value = u.avatarUrl
    }
  }
  loadStatusCount()
  timer = window.setInterval(loadStatusCount, 15000)
})
const onAvatarUpdated = (e) => {
  const d = e?.detail
  const url = (d && d.url) || d || ''
  if (url) avatarSrc.value = url
}
onMounted(() => {
  window.addEventListener('avatar-updated', onAvatarUpdated)
})
onUnmounted(() => {
  window.removeEventListener('avatar-updated', onAvatarUpdated)
  if (timer) window.clearInterval(timer)
})
const showDot = (s) => Number(statusCount.value?.[s] || 0) > 0
const badgeText = (s) => {
  const n = Number(statusCount.value?.[s] || 0)
  if (n <= 0) return ''
  return n > 99 ? '99+' : String(n)
}
const loadStatusCount = async () => {
  try {
    const resp = await http('/admin/order/statusCount', { method: 'GET', json: false })
    if (resp && resp.code === 1) {
      statusCount.value = resp.data || {}
    }
  } catch (_) {}
}
const onLogout = async () => {
  await http('/admin/admin/logout', { method: 'POST' })
  clearAuth()
  router.push('/login')
}
const goProfile = () => {
  router.push('/admin/profile')
}
</script>

<style>
.admin {
  height: 100vh;
  background: var(--admin-bg);
}
.aside {
  border-right: 1px solid var(--admin-border);
  background: var(--admin-surface);
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--admin-border);
  background: color-mix(in srgb, var(--admin-surface) 92%, transparent);
  backdrop-filter: blur(8px);
  position: sticky;
  top: 0;
  z-index: 20;
}
.main {
  background: var(--admin-bg);
  padding: var(--admin-space-2);
}
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.profile-link {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 2px 6px;
  border-radius: var(--admin-radius);
  transition: background-color .2s ease, box-shadow .2s ease;
}
.profile-link:hover {
  background: var(--admin-primary-soft);
  box-shadow: inset 0 0 0 1px color-mix(in srgb, var(--admin-primary) 35%, #d7deeb);
}
.profile-link:active {
  background: #e0e7ff;
}
.profile-link:focus-visible {
  outline: 2px solid #3b82f6;
  outline-offset: 2px;
}
.profile-link:hover .user-info .line {
  text-decoration: underline;
}
.home-label {
  color: var(--admin-primary);
  text-decoration: underline;
  font-size: 11px;
}
.chevron {
  color: var(--admin-primary);
  font-size: 12px;
  line-height: 1;
}
.avatar {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  object-fit: cover;
  background: #e5e7eb;
  overflow: hidden;
  display: block;
}
.user-info {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}
.user-info .line {
  color: var(--admin-sub);
  font-size: 12px;
}
.menu-with-dot {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.menu-badge {
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 999px;
  background: #ef4444;
  color: #fff;
  font-size: 11px;
  line-height: 18px;
  text-align: center;
  font-weight: 600;
}

@media (max-width: 920px) {
  .admin {
    height: auto;
    min-height: 100vh;
  }
  .main {
    padding: 12px;
  }
  .header {
    height: auto;
    padding: 10px 12px;
    flex-wrap: wrap;
    gap: 8px;
  }
}
</style>
