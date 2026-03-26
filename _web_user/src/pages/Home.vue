<template>
  <div class="home">
    <section class="hero">
      <el-carousel height="360px" indicator-position="outside">
        <el-carousel-item v-for="(img, i) in banners" :key="i">
          <img :src="img" class="banner-image" alt="banner" />
        </el-carousel-item>
      </el-carousel>
    </section>
    <section class="second-hand-banner" aria-label="二手书专区入口">
      <div class="sh-banner-inner">
        <div class="sh-banner-text">
          <div class="sh-banner-kicker">校园循环 · 估价透明</div>
          <div class="sh-banner-title">二手书专区</div>
          <div class="sh-banner-desc">本店在售图书可回收估价上架；按成色自动折算原价，同学闲置更实惠。</div>
        </div>
        <div class="sh-banner-actions">
          <el-button type="warning" size="large" round class="sh-btn-main" @click="onSecondHandClick">进入二手书专区</el-button>
          <el-button size="large" round class="sh-btn-sub" @click="onSecondHandSellClick">我要卖书</el-button>
        </div>
      </div>
    </section>
    <section class="quick-cats">
      <div class="title">热门分类</div>
      <div class="cats">
        <el-tag
          v-for="c in hotCategories"
          :key="String(c.id)"
          size="large"
          effect="plain"
          class="cat-tag"
          @click="onCatClick(c)"
        >{{ c.name }}</el-tag>
      </div>
    </section>
    <section class="featured">
      <div class="title-row">
        <div class="title">精选图书</div>
        <div class="title-actions">
          <el-button type="primary" @click="onMoreClick">查看全部</el-button>
        </div>
      </div>
      <div class="grid">
        <el-card v-for="b in featuredBooks" :key="b.id" class="book-card" shadow="hover">
          <img :src="b.coverImage || defaultCover" class="cover" alt="cover" />
          <div class="name">{{ b.title }}</div>
          <div class="author">{{ b.author }}</div>
          <div class="price">¥ {{ b.price.toFixed(2) }}</div>
          <div class="ops">
            <el-button type="primary" size="small" @click="onAddToCart(b)">加入购物车</el-button>
            <el-button size="small" @click="onDetail(b)">详情</el-button>
          </div>
        </el-card>
      </div>
    </section>
  </div>
  <FloatingCartButton />

  <el-dialog v-model="noticeVisible" width="520px" :close-on-click-modal="false" :show-close="false">
    <template #header>
      <div class="notice-head">
        <div class="notice-title">{{ noticeTitle }}</div>
        <div class="notice-sub">系统公告</div>
      </div>
    </template>
    <div class="notice-content">{{ noticeContent }}</div>
    <div class="notice-actions">
      <el-checkbox v-model="dontShowAgain">不再提示</el-checkbox>
      <div class="notice-btns">
        <el-button type="primary" @click="onNoticeOk">我知道了</el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import FloatingCartButton from '../components/FloatingCartButton.vue'
import { refreshCartCount } from '../stores/cart'
const route = useRoute()
const router = useRouter()
const defaultCover = '/default-book-cover.svg'
const banners = ref([
  'https://images.unsplash.com/photo-1512820790803-83ca734da794?q=80&w=1920&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1521587760476-6c12a4b040da?q=80&w=1920&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1495446815901-a7297e633e8d?q=80&w=1920&auto=format&fit=crop'
])
const hotCategories = ref([])
const featuredBooks = ref([])

const noticeVisible = ref(false)
const notice = ref(null)
const dontShowAgain = ref(false)
const noticeTitle = ref('系统公告')
const noticeContent = ref('')
const NOTICE_DISMISS_KEY = 'systemNoticeDismissed'

const shouldShowNotice = (n) => {
  if (!n || n.id == null) return false
  const enabled = n.enabled
  if (enabled != null && Number(enabled) !== 1) return false
  const sig = `${n.id}_${n.updateTime || ''}`
  const dismissed = localStorage.getItem(NOTICE_DISMISS_KEY) || ''
  return dismissed !== sig
}

const loadNotice = async () => {
  try {
    const token = localStorage.getItem('token') || ''
    const resp = await fetch('/user/systemNotice/active', {
      method: 'GET',
      headers: token ? { authentication: token } : {}
    })
    const ct = resp.headers.get('content-type') || ''
    let data = {}
    if (ct.includes('application/json')) {
      try { data = await resp.json() } catch (_) {}
    }
    if (!resp.ok) return
    if (!(Number(data?.code) === 1)) return
    const n = data?.data || null
    notice.value = n
    if (shouldShowNotice(n)) {
      noticeTitle.value = (n?.title || '').trim() || '系统公告'
      noticeContent.value = (n?.content || '').trim()
      dontShowAgain.value = false
      noticeVisible.value = true
    }
  } catch (_) {}
}

const onNoticeOk = () => {
  const n = notice.value
  if (dontShowAgain.value && n && n.id != null) {
    const sig = `${n.id}_${n.updateTime || ''}`
    localStorage.setItem(NOTICE_DISMISS_KEY, sig)
  }
  noticeVisible.value = false
}

const buildQs = (page = 1, pageSize = 12) => {
  const p = new URLSearchParams()
  p.set('page', String(page))
  p.set('pageSize', String(pageSize))
  const q = route?.query?.q
  if (q && typeof q === 'string' && q.trim()) {
    p.set('title', q.trim())
  }
  return p.toString()
}

const loadBooks = async () => {
  try {
    const token = localStorage.getItem('token') || ''
    const resp = await fetch(`/user/book/page?${buildQs()}`, {
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
      let rows = []
      if (Array.isArray(d)) rows = d
      else rows = d?.records || d?.list || d?.items || d?.rows || d?.data || []
      featuredBooks.value = (rows || []).slice(0, 12)
    } else {
      featuredBooks.value = []
    }
  } catch (_) {
    featuredBooks.value = []
  }
}

const loadHotCategories = async () => {
  try {
    const token = localStorage.getItem('token') || ''
    const resp = await fetch(`/user/category/page?page=1&pageSize=100`, {
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
      let rows = []
      if (Array.isArray(d)) rows = d
      else rows = d?.records || d?.list || d?.items || d?.rows || d?.data || []
      const filtered = (rows || []).filter(r => r && (r.status === undefined || r.status === 1))
      filtered.sort((a, b) => {
        const sa = a?.sort ?? 0
        const sb = b?.sort ?? 0
        if (sa !== sb) return sa - sb
        const na = String(a?.name || '')
        const nb = String(b?.name || '')
        return na.localeCompare(nb)
      })
      hotCategories.value = filtered.slice(0, 10)
    } else {
      hotCategories.value = []
    }
  } catch (_) {
    hotCategories.value = []
  }
}
const loadHomeRecommend = async () => {
  try {
    const token = localStorage.getItem('token') || ''
    const resp = await fetch('/user/home/recommend?categoryLimit=10&bookLimit=12', {
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
      const cats = Array.isArray(d?.hotCategories) ? d.hotCategories : []
      const books = Array.isArray(d?.featuredBooks) ? d.featuredBooks : []
      if (cats.length) hotCategories.value = cats
      if (books.length) featuredBooks.value = books
      return
    }
  } catch (_) {}
  await Promise.all([loadHotCategories(), loadBooks()])
}
onMounted(() => {
  loadHomeRecommend()
  loadNotice()
})
watch(() => route.query.q, () => {
  loadBooks()
})
const goBrowse = () => {
  router.push('/browse')
}
const needLogin = () => {
  const tk = localStorage.getItem('token') || ''
  return !tk
}
const onMoreClick = () => {
  if (needLogin()) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: '/browse', msg: '请先登录' } })
    return
  }
  router.push('/browse')
}
const onSecondHandClick = () => {
  router.push('/second-hand')
}
const onSecondHandSellClick = () => {
  const tk = localStorage.getItem('token') || ''
  if (!tk) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: '/second-hand/sell', msg: '请先登录' } })
    return
  }
  router.push('/second-hand/sell')
}
const onAddToCart = (b) => {
  if (needLogin()) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: '/cart', msg: '请先登录' } })
    return
  }
  if (!b || b.id == null) return
  fetch(`/user/cart/add/${encodeURIComponent(b.id)}?num=1`, {
    method: 'POST',
    headers: (() => {
      const tk = localStorage.getItem('token') || ''
      return tk ? { authentication: tk } : {}
    })()
  }).then(async (resp) => {
    let data = {}
    if (resp.headers.get('content-type')?.includes('application/json')) {
      try {
        data = await resp.json()
      } catch (_) {}
    }
    if (resp.status === 401) {
      ElMessage.warning('登录已过期，请重新登录')
      router.push({ path: '/login', query: { redirect: '/', msg: '请先登录' } })
      return
    }
    if (resp.ok && Number(data.code) === 1) {
      ElMessage.success('已加入购物车')
      refreshCartCount()
    } else {
      ElMessage.error(data?.msg || '加入购物车失败')
    }
  }).catch(() => ElMessage.error('加入购物车失败'))
}
const onDetail = (b) => {
  if (b && b.id != null) {
    router.push(`/book/${b.id}`)
  }
}
const onCatClick = (c) => {
  if (!c || c.id == null) return
  const target = { path: '/browse', query: { categoryId: String(c.id) } }
  const tk = localStorage.getItem('token') || ''
  if (!tk) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: '/browse?categoryId=' + String(c.id), msg: '请先登录' } })
    return
  }
  router.push(target)
}
</script>

<style>
.home {
  max-width: 1240px;
  margin: 0 auto;
  padding: 8px 4px 20px;
}
.banner-image {
  width: 100%;
  height: 360px;
  object-fit: cover;
  border-radius: var(--radius);
}
.second-hand-banner {
  margin-top: 20px;
  padding: 22px 24px;
  border-radius: var(--radius);
  border: 1px solid color-mix(in srgb, var(--el-color-warning) 45%, var(--border));
  background: linear-gradient(120deg, color-mix(in srgb, var(--el-color-warning-light-9) 85%, #fff) 0%, var(--surface) 55%, color-mix(in srgb, var(--el-color-warning-light-9) 70%, #fff) 100%);
  box-shadow: var(--shadow-sm);
}
.sh-banner-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  flex-wrap: wrap;
}
.sh-banner-text {
  flex: 1 1 280px;
  min-width: 0;
}
.sh-banner-kicker {
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--el-color-warning-dark-2);
  margin-bottom: 6px;
}
.sh-banner-title {
  font-family: var(--font-heading);
  font-size: 26px;
  font-weight: 800;
  letter-spacing: 0.04em;
  color: var(--text-main);
  line-height: 1.2;
  margin-bottom: 8px;
}
.sh-banner-desc {
  font-size: 14px;
  line-height: 1.55;
  color: var(--text-sub);
  max-width: 560px;
}
.sh-banner-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}
.sh-btn-main {
  font-weight: 700;
  padding: 12px 22px;
  font-size: 15px;
}
.sh-btn-sub {
  font-weight: 600;
  padding: 12px 22px;
  font-size: 15px;
}
.quick-cats, .featured {
  margin-top: 18px;
  background: var(--surface);
  border-radius: var(--radius);
  padding: 18px;
  border: 1px solid var(--border);
  box-shadow: var(--shadow-sm);
}
.title {
  font-family: var(--font-heading);
  font-weight: 700;
  font-size: 20px;
  margin-bottom: 12px;
  letter-spacing: 0.02em;
}
.title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.title-actions {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.cats {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.cat-tag {
  cursor: pointer;
  border-radius: 999px;
  transition: all .2s ease;
}
.cat-tag:hover {
  color: var(--primary);
  border-color: color-mix(in srgb, var(--primary) 45%, #d1d5db);
  background: var(--primary-weak);
}
.grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 14px;
}
.notice-head {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.notice-title {
  font-weight: 800;
  font-size: 16px;
  color: var(--text-main);
}
.notice-sub {
  font-size: 12px;
  color: var(--text-sub);
}
.notice-content {
  white-space: pre-wrap;
  color: var(--text-main);
  line-height: 1.7;
  font-size: 14px;
  padding: 4px 2px 2px;
}
.notice-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 16px;
}
.notice-btns {
  display: inline-flex;
  gap: 8px;
}
@media (max-width: 1200px) {
  .grid { grid-template-columns: repeat(5, 1fr); }
}
@media (max-width: 992px) {
  .grid { grid-template-columns: repeat(4, 1fr); }
}
@media (max-width: 768px) {
  .grid { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 576px) {
  .grid { grid-template-columns: repeat(2, 1fr); }
}
.book-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  min-height: 280px;
  border-radius: var(--radius);
  overflow: hidden;
  transition: box-shadow 0.22s ease, border-color 0.22s ease;
  border: 1px solid transparent;
}
.book-card:hover {
  box-shadow: var(--shadow-md);
  border-color: color-mix(in srgb, var(--primary) 22%, var(--border));
}
@media (prefers-reduced-motion: reduce) {
  .book-card {
    transition: none;
  }
}
.cover {
  width: 152px;
  height: 152px;
  border-radius: var(--radius);
  background: #f5f7fa;
  object-fit: cover;
}
.name {
  font-weight: 600;
  text-align: center;
  max-width: 180px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  height: 40px;
}
.author {
  color: var(--text-sub);
  font-size: 13px;
  height: 16px;
}
.price {
  color: #1f2937;
  font-weight: 700;
  font-size: 16px;
}
.ops {
  display: flex;
  gap: 6px;
  margin-top: auto;
  justify-content: center;
}
@media (max-width: 768px) {
  .second-hand-banner {
    margin-top: 14px;
    padding: 16px 14px;
  }
  .sh-banner-inner {
    flex-direction: column;
    align-items: stretch;
  }
  .sh-banner-title {
    font-size: 22px;
  }
  .sh-banner-actions {
    justify-content: center;
  }
  .sh-btn-main,
  .sh-btn-sub {
    flex: 1 1 auto;
    min-width: 140px;
    justify-content: center;
  }
  .quick-cats, .featured {
    padding: 14px;
    border-radius: var(--radius);
  }
  .title {
    font-size: 18px;
  }
  .banner-image {
    border-radius: var(--radius);
  }
}
</style>
