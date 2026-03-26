<template>
  <el-container class="admin">
    <el-aside width="220px" class="aside">
      <div class="aside-head" aria-hidden="true">
        <span class="aside-kicker">PTU</span>
        <span class="aside-title">管理后台</span>
      </div>
      <el-menu :default-active="active" router class="aside-menu">
        <el-menu-item index="/admin/dashboard">仪表盘</el-menu-item>
        <el-menu-item index="/admin/books">图书管理</el-menu-item>
        <el-menu-item index="/admin/categories">分类管理</el-menu-item>
        <el-menu-item index="/admin/second-hand">
          <span class="menu-with-dot">二手书回收<span v-if="secondHandPending > 0" class="menu-badge">{{ secondHandBadgeText }}</span></span>
        </el-menu-item>
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
        <div class="header-title">图书系统 · 管理后台</div>
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
const secondHandPending = ref(0)
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
const showDot = (s) => Number(statusCount.value?.[s] || 0) > 0
const badgeText = (s) => {
  const n = Number(statusCount.value?.[s] || 0)
  if (n <= 0) return ''
  return n > 99 ? '99+' : String(n)
}
const secondHandBadgeText = computed(() => {
  const n = Number(secondHandPending.value || 0)
  if (n <= 0) return ''
  return n > 99 ? '99+' : String(n)
})
const loadStatusCount = async () => {
  try {
    const resp = await http('/admin/order/statusCount', { method: 'GET', json: false })
    if (resp && Number(resp.code) === 1) {
      statusCount.value = resp.data || {}
    }
  } catch (_) {}
}
/** 待审核条数：用分页 total，避免单独接口未部署或 code 类型不一致导致徽标始终为 0 */
const loadSecondHandPending = async () => {
  try {
    const qs = new URLSearchParams({ page: '1', pageSize: '1', status: '0' })
    const resp = await http(`/admin/secondHand/page?${qs}`, { method: 'GET', json: false })
    if (resp && Number(resp.code) === 1) {
      const t = resp.data?.total
      secondHandPending.value = t != null ? Number(t) || 0 : 0
    }
  } catch (_) {}
}
const loadAllBadges = () => {
  loadStatusCount()
  loadSecondHandPending()
}
const onBadgesRefresh = () => {
  loadAllBadges()
}
const onAvatarUpdated = (e) => {
  const d = e?.detail
  const url = (d && d.url) || d || ''
  if (url) avatarSrc.value = url
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
  loadAllBadges()
  timer = window.setInterval(loadAllBadges, 15000)
})
onMounted(() => {
  window.addEventListener('avatar-updated', onAvatarUpdated)
  window.addEventListener('admin-badges-refresh', onBadgesRefresh)
})
onUnmounted(() => {
  window.removeEventListener('avatar-updated', onAvatarUpdated)
  window.removeEventListener('admin-badges-refresh', onBadgesRefresh)
  if (timer) window.clearInterval(timer)
})
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
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  display: flex;
  flex-direction: column;
}
.aside-head {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 18px 16px 14px;
  border-bottom: 1px solid var(--admin-border);
}
.aside-kicker {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.14em;
  color: var(--admin-primary);
  text-transform: uppercase;
}
.aside-title {
  font-family: var(--font-heading);
  font-size: 16px;
  font-weight: 700;
  color: var(--admin-text);
  line-height: 1.25;
}
.aside-menu {
  flex: 1;
  border-right: none !important;
  --el-menu-item-height: 44px;
}
.aside-menu :deep(.el-menu-item) {
  border-radius: 8px;
  margin: 2px 8px;
  width: auto;
  transition: background-color 0.2s ease, color 0.2s ease;
}
.aside-menu :deep(.el-menu-item.is-active) {
  font-weight: 600;
}
.header-title {
  font-family: var(--font-heading);
  font-size: 17px;
  font-weight: 600;
  color: var(--admin-text);
  letter-spacing: 0.02em;
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
