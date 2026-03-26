<template>
  <div class="browse">
    <div class="title-bar">
      <div class="title">商品浏览</div>
      <div class="ops">
        <el-select v-model="searchType" style="width: 140px">
          <el-option label="书名" value="title" />
          <el-option label="ISBN" value="isbn" />
          <el-option label="作者" value="author" />
        </el-select>
        <el-input v-model="q" :placeholder="placeholder" clearable style="width: 220px" />
        <el-button type="primary" @click="onSearch">搜索</el-button>
        <el-button @click="goHome">返回首页</el-button>
      </div>
    </div>
    <div class="layout">
      <aside class="side">
        <el-menu :default-active="activeCat" @select="onCatSelect" class="cat-menu">
          <el-menu-item index="all">全部</el-menu-item>
          <el-menu-item v-for="c in categories" :key="String(c.id)" :index="String(c.id)">
            {{ c.name }}
          </el-menu-item>
        </el-menu>
      </aside>
      <section class="content">
        <div class="grid">
          <el-card v-for="b in books" :key="b.id" class="book-card" shadow="hover">
            <img :src="b.coverImage || defaultCover" class="cover" alt="cover" />
            <div class="name">{{ b.title }}</div>
            <div class="author">{{ b.author }}</div>
            <div class="price">¥ {{ b.price?.toFixed ? b.price.toFixed(2) : Number(b.price || 0).toFixed(2) }}</div>
            <div class="ops">
            <el-button type="primary" size="small" @click="onAddToCart(b)">加入购物车</el-button>
            <el-button size="small" @click="goDetail(b)">详情</el-button>
            </div>
          </el-card>
        </div>
        <div class="pager">
          <el-pagination
            background
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[12, 24, 36, 48]"
            layout="prev, pager, next, sizes, total, jumper"
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
          />
        </div>
      </section>
    </div>
  </div>
  <FloatingCartButton />
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import FloatingCartButton from '../components/FloatingCartButton.vue'
import { refreshCartCount } from '../stores/cart'
const route = useRoute()
const router = useRouter()
const defaultCover = '/default-book-cover.svg'
const q = ref(typeof route.query.q === 'string' ? route.query.q : '')
const books = ref([])
const categories = ref([])
const activeCat = ref(typeof route.query.categoryId === 'string' ? route.query.categoryId : 'all')
const currentPage = ref(1)
const pageSize = ref(24)
const total = ref(0)
const buildQs = () => {
  const p = new URLSearchParams()
  p.set('page', String(currentPage.value))
  p.set('pageSize', String(pageSize.value))
  if (q.value && q.value.trim()) {
    const val = q.value.trim()
    if (searchType.value === 'title') p.set('title', val)
    else if (searchType.value === 'isbn') p.set('isbn', val)
    else p.set('author', val)
  }
  if (activeCat.value && activeCat.value !== 'all') {
    p.set('categoryId', String(activeCat.value))
  }
  return p.toString()
}
const loadBooks = async () => {
  try {
    const token = localStorage.getItem('token') || ''
    const url = `/user/book/page?${buildQs()}`
    const resp = await fetch(url, {
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
      books.value = rows || []
      const t =
        d?.total ??
        d?.totalCount ??
        d?.count ??
        (Array.isArray(d?.records) ? d.records.length : Array.isArray(rows) ? rows.length : 0)
      total.value = Number(t || 0)
    } else {
      books.value = []
      total.value = 0
    }
  } catch (_) {
    books.value = []
    total.value = 0
  }
}
const loadCategories = async () => {
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
      categories.value = filtered
    } else {
      categories.value = []
    }
  } catch (_) {
    categories.value = []
  }
}
const searchType = ref(
  typeof route.query.st === 'string' && ['title', 'isbn', 'author'].includes(route.query.st)
    ? route.query.st
    : 'title'
)
const placeholder = computed(() => {
  if (searchType.value === 'isbn') return '按ISBN搜索'
  if (searchType.value === 'author') return '按作者搜索'
  return '按书名搜索'
})
const onSearch = () => {
  const query = {}
  if (q.value && q.value.trim()) query.q = q.value.trim()
  query.st = searchType.value
  router.replace({ path: '/browse', query })
  currentPage.value = 1
  loadBooks()
}
const goHome = () => router.push('/')
const goDetail = (b) => {
  if (b && b.id != null) {
    router.push(`/book/${b.id}`)
  }
}
const needLogin = () => {
  const tk = localStorage.getItem('token') || ''
  return !tk
}
const onAddToCart = (b) => {
  if (needLogin()) {
    ElMessage.warning('请先登录')
    router.push('/login')
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
      router.push({ path: '/login', query: { redirect: '/browse', msg: '请先登录' } })
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
onMounted(() => {
  loadCategories()
  loadBooks()
})
watch(
  [() => route.query.q, () => route.query.st, () => route.query.categoryId],
  ([nvQ, nvSt, nvCat]) => {
    q.value = typeof nvQ === 'string' ? nvQ : ''
    searchType.value =
      typeof nvSt === 'string' && ['title', 'isbn', 'author'].includes(nvSt) ? nvSt : 'title'
    activeCat.value = typeof nvCat === 'string' ? nvCat : 'all'
    currentPage.value = 1
    loadBooks()
  }
)
const onCatSelect = (index) => {
  activeCat.value = index
  currentPage.value = 1
  loadBooks()
}
const handlePageChange = (p) => {
  currentPage.value = p
  loadBooks()
}
const handleSizeChange = (s) => {
  pageSize.value = s
  currentPage.value = 1
  loadBooks()
}
</script>

<style>
.browse {
  max-width: 1240px;
  margin: 0 auto;
  padding: 8px 4px 20px;
}
.layout {
  display: grid;
  grid-template-columns: 200px 1fr;
  gap: 14px;
}
.side {
  background: var(--surface);
  border-radius: var(--radius);
  padding: 10px;
  border: 1px solid var(--border);
  box-shadow: var(--shadow-sm);
}
.content {
  background: var(--surface);
  border-radius: var(--radius);
  padding: 16px;
  border: 1px solid var(--border);
  box-shadow: var(--shadow-sm);
}
.cat-menu {
  border-right: none;
}
.title-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.title {
  font-weight: 700;
  font-size: 20px;
}
.grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 14px;
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
  transition: transform .2s ease, box-shadow .2s ease;
}
.book-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-md);
}
.cover {
  width: 150px;
  height: 150px;
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
  line-height: 20px;
  height: 40px;
}
.author {
  color: var(--text-sub);
  font-size: 13px;
  line-height: 16px;
  max-width: 180px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.price {
  color: #1f2937;
  font-weight: 700;
  margin-top: 2px;
}
.ops {
  display: flex;
  gap: 6px;
  margin-top: auto;
  justify-content: center;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
@media (max-width: 960px) {
  .layout {
    grid-template-columns: 1fr;
  }
  .side,
  .content {
    border-radius: var(--radius);
  }
  .side {
    padding: 6px;
  }
}
</style>
