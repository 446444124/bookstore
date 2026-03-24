<template>
  <div class="app">
    <header class="header">
      <div class="brand">
        <img class="badge" :src="badgeSrc" @error="onBadgeError" alt="莆田学院校徽" />
        <span class="brand-text">莆田学院校园书店</span>
      </div>
      <div class="nav-actions">
        <div class="profile">
          <img class="avatar" :src="avatarSrc" @error="onAvatarError" alt="用户头像" />
          <span class="name">{{ displayName }}</span>
        </div>
        <el-button @click="goHome">回到首页</el-button>
        <el-button v-if="isLoggedIn" type="primary" link @click="onProfileClick">个人主页</el-button>
        <el-button v-if="isLoggedIn" type="primary" link @click="onOrdersClick">我的订单</el-button>
        <el-button v-if="!isLoggedIn" @click="goLogin">登录</el-button>
        <el-button v-else type="danger" @click="logout">退出登录</el-button>
      </div>
    </header>
    <main class="main">
      <router-view />
    </main>
    <footer class="footer">© {{ year }} 莆田学院校园书店</footer>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
const router = useRouter()
const year = new Date().getFullYear()
const badgeSrc = ref('/ptu-badge.png')
const goHome = () => {
  router.push('/')
}
const goLogin = () => {
  router.push('/login')
}
const onProfileClick = () => {
  const tk = localStorage.getItem('token') || ''
  const uid = localStorage.getItem('userId') || ''
  if (!tk || !uid) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: '/profile', msg: '请先登录' } })
    return
  }
  router.push('/profile')
}
const onOrdersClick = () => {
  const tk = localStorage.getItem('token') || ''
  const uid = localStorage.getItem('userId') || ''
  if (!tk || !uid) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: '/orders', msg: '请先登录' } })
    return
  }
  router.push('/orders')
}
const onBadgeError = () => {
  badgeSrc.value =
    'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 160 160"><defs><style>.b{fill:none;stroke:%232563eb;stroke-width:6}.c{fill:%233b82f6}.t{fill:%23ffffff;font-size:32px;font-family:Arial,Helvetica,sans-serif;font-weight:bold}</style></defs><circle cx="80" cy="80" r="74" class="b"/><circle cx="80" cy="80" r="46" class="c"/><text x="80" y="95" text-anchor="middle" class="t">PTU</text></svg>'
}
const isLoggedIn = ref(!!localStorage.getItem('token'))
const displayName = ref('游客')
const avatarSrc = ref('/default-avatar.svg')
const onAvatarError = () => {
  avatarSrc.value =
    'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 40 40"><defs><style>.c{fill:%23e5e7eb}.t{fill:%23606266;font-size:10px;font-family:Arial,Helvetica,sans-serif}</style></defs><circle cx="20" cy="20" r="19" class="c"/><circle cx="20" cy="16" r="7" fill="%23d1d5db"/><rect x="8" y="24" width="24" height="10" rx="5" fill="%23d1d5db"/></svg>'
}
const loadUser = async () => {
  const token = localStorage.getItem('token') || ''
  const userId = localStorage.getItem('userId') || ''
  if (!token || !userId) {
    displayName.value = '游客'
    isLoggedIn.value = false
    return
  }
  try {
    const resp = await fetch(`/user/user/${encodeURIComponent(userId)}`, {
      method: 'GET',
      headers: { authentication: token }
    })
    const ct = resp.headers.get('content-type') || ''
    let data = {}
    if (ct.includes('application/json')) {
      try { data = await resp.json() } catch (_) {}
    }
    const d = data?.data ?? data
    if (resp.ok && d) {
      displayName.value = d.username || d.realName || '游客'
      const av = d.avatar || d.avatarUrl || ''
      if (av) avatarSrc.value = av
      isLoggedIn.value = true
    } else {
      displayName.value = localStorage.getItem('userUsername') || localStorage.getItem('userRealName') || '游客'
      const av2 = localStorage.getItem('userAvatar') || ''
      if (av2) avatarSrc.value = av2
      isLoggedIn.value = false
      try {
        localStorage.removeItem('token')
        localStorage.removeItem('userId')
        localStorage.removeItem('userRealName')
        localStorage.removeItem('userAvatar')
        localStorage.removeItem('userUsername')
      } catch (_) {}
    }
  } catch (_) {
    displayName.value = localStorage.getItem('userUsername') || localStorage.getItem('userRealName') || '游客'
    const av2 = localStorage.getItem('userAvatar') || ''
    if (av2) avatarSrc.value = av2
    isLoggedIn.value = false
    try {
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      localStorage.removeItem('userRealName')
      localStorage.removeItem('userAvatar')
      localStorage.removeItem('userUsername')
    } catch (_) {}
  }
}
const onLoggedIn = () => {
  loadUser()
}
const onLoggedOut = () => {
  isLoggedIn.value = false
  displayName.value = '游客'
  avatarSrc.value = '/default-avatar.svg'
}
const logout = async () => {
  const token = localStorage.getItem('token') || ''
  try {
    if (token) {
      await fetch('/logout', {
        method: 'POST',
        headers: { authentication: token }
      }).catch(() => {})
    }
  } catch (_) {}
  try {
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('userRealName')
    localStorage.removeItem('userAvatar')
    localStorage.removeItem('userUsername')
  } catch (_) {}
  window.dispatchEvent(new CustomEvent('user-logged-out'))
  ElMessage.success('已退出登录')
}
onMounted(() => {
  loadUser()
  window.addEventListener('user-logged-in', onLoggedIn)
  window.addEventListener('user-logged-out', onLoggedOut)
})
onUnmounted(() => {
  window.removeEventListener('user-logged-in', onLoggedIn)
  window.removeEventListener('user-logged-out', onLoggedOut)
})
</script>

<style>
:root {
  --bg: #f6f8fb;
  --surface: #ffffff;
  --surface-soft: #fbfcfe;
  --text-main: #111827;
  --text-sub: #6b7280;
  --border: #e8ecf3;
  --primary: #5563f2;
  --primary-weak: #eef0ff;
  --success: #3f8f6b;
  --warning: #b7823a;
  --danger: #c85c5c;
  --radius: 8px;
  --space-1: 8px;
  --space-2: 16px;
  --space-3: 24px;
  --shadow: 0 8px 20px rgba(15, 23, 42, 0.06);

  --el-color-primary: var(--primary);
  --el-color-success: var(--success);
  --el-color-warning: var(--warning);
  --el-color-danger: var(--danger);
}

html {
  scroll-behavior: smooth;
}

body {
  margin: 0;
  font-family: Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
  background: var(--bg);
  color: var(--text-main);
}

* {
  box-sizing: border-box;
}

.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #f8faff 0%, var(--bg) 100%);
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-1);
  padding: var(--space-2);
  border-bottom: 1px solid var(--border);
  background: color-mix(in srgb, var(--surface) 92%, transparent);
  backdrop-filter: blur(8px);
  position: sticky;
  top: 0;
  z-index: 30;
}
.brand {
  font-weight: 700;
  font-size: 22px;
  display: flex;
  align-items: center;
  gap: 10px;
}
.brand .badge {
  width: 44px;
  height: 44px;
  display: block;
}
.brand-text { line-height: 1; }
.nav-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}
.profile {
  display: flex;
  align-items: center;
  gap: 8px;
}
.avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  object-fit: cover;
  background: #e5e7eb;
  overflow: hidden;
  display: block;
}
.name {
  color: var(--text-sub);
  font-size: 14px;
}
.main {
  flex: 1;
  background: transparent;
  padding: var(--space-2);
}
.footer {
  border-top: 1px solid var(--border);
  padding: 12px 16px;
  text-align: center;
  color: var(--text-sub);
  background: var(--surface-soft);
}

:global(.el-card),
:global(.el-dialog),
:global(.el-message-box) {
  border-radius: var(--radius);
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
}

:global(.el-button) {
  border-radius: var(--radius);
  height: 36px;
  padding: 0 14px;
  font-weight: 600;
  transition: transform .2s ease, box-shadow .2s ease, background-color .2s ease, border-color .2s ease;
}

:global(.el-button:hover) {
  transform: translateY(-1px);
  box-shadow: 0 8px 16px rgba(15, 23, 42, 0.08);
}

:global(.el-input__wrapper),
:global(.el-select__wrapper),
:global(.el-textarea__inner) {
  border-radius: var(--radius);
}

:global(.el-input__wrapper),
:global(.el-select__wrapper) {
  min-height: 36px;
}

:global(.el-form-item) {
  margin-bottom: var(--space-2);
}

:global(.el-form-item__label) {
  color: var(--text-sub);
  font-size: 13px;
  font-weight: 500;
}

:global(.el-table) {
  --el-table-header-bg-color: #f7f9fd;
  --el-table-row-hover-bg-color: #f8faff;
  --el-table-border-color: var(--border);
  border-radius: var(--radius);
  overflow: hidden;
}

:global(.el-table th.el-table__cell) {
  padding: 10px 0;
}

:global(.el-table td.el-table__cell) {
  padding: 11px 0;
}

:global(.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell) {
  background: #fcfdff;
}

:global(.title-bar) {
  min-height: 56px;
  padding: 0 2px;
  margin-bottom: var(--space-2);
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

:global(.title-bar .title),
:global(.title-row .title),
:global(.card-head) {
  font-size: 22px;
  font-weight: 700;
  line-height: 1.3;
  color: var(--text-main);
}

:global(.title-desc),
:global(.sub-title),
:global(.desc) {
  color: var(--text-sub);
  font-size: 13px;
}

@media (max-width: 900px) {
  .header {
    padding: 12px 14px;
    align-items: flex-start;
    flex-direction: column;
  }
  .brand {
    font-size: 18px;
  }
  .brand .badge {
    width: 36px;
    height: 36px;
  }
  .main {
    padding: 12px 10px 18px;
  }
}
</style>
