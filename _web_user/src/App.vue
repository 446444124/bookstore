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
.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  border-bottom: 1px solid #e5e7eb;
  background: #ffffff;
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
  gap: 10px;
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
  color: #303133;
  font-size: 14px;
}
.main {
  flex: 1;
  background: #f5f7fa;
}
.footer {
  border-top: 1px solid #e5e7eb;
  padding: 12px 16px;
  text-align: center;
  color: #606266;
  background: #fff;
}
</style>
