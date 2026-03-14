<template>
  <div class="home">
    <section class="hero">
      <el-carousel height="360px" indicator-position="outside">
        <el-carousel-item v-for="(img, i) in banners" :key="i">
          <img :src="img" class="banner-image" alt="banner" />
        </el-carousel-item>
      </el-carousel>
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
        <el-button type="primary" link @click="onMoreClick">查看更多</el-button>
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

onMounted(loadBooks)
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
onMounted(loadHotCategories)
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
  }).then(resp => {
    if (resp.ok) {
      ElMessage.success('已加入购物车')
      refreshCartCount()
    } else {
      ElMessage.error('加入购物车失败')
    }
  }).catch(() => ElMessage.error('加入购物车失败'))
}
const onDetail = (b) => {
  if (needLogin()) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
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
  max-width: 1200px;
  margin: 0 auto;
  padding: 12px;
}
.banner-image {
  width: 100%;
  height: 360px;
  object-fit: cover;
  border-radius: 12px;
}
.quick-cats, .featured {
  margin-top: 16px;
  background: #fff;
  border-radius: 12px;
  padding: 12px;
}
.title {
  font-weight: 600;
  font-size: 18px;
  margin-bottom: 8px;
}
.title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.cats {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.cat-tag {
  cursor: pointer;
}
.grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
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
  gap: 6px;
  min-height: 280px;
}
.cover {
  width: 150px;
  height: 150px;
  border-radius: 8px;
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
  color: #606266;
  font-size: 12px;
  height: 16px;
}
.price {
  color: #d97706;
  font-weight: 700;
}
.ops {
  display: flex;
  gap: 6px;
  margin-top: auto;
  justify-content: center;
}
</style>
