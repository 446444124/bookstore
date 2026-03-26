<template>
  <div class="detail">
    <div class="header">
      <div class="title">{{ book.title || '图书详情' }}</div>
      <div class="ops">
        <el-button @click="goBack">返回</el-button>
      </div>
    </div>
    <div class="body" v-if="loaded">
      <img :src="book.coverImage || defaultCover" class="cover" alt="cover" />
      <div class="info">
        <div class="name">{{ book.title }}</div>
        <div class="author">作者：{{ book.author }}</div>
        <div class="publisher" v-if="book.publisher">出版社：{{ book.publisher }}</div>
        <div class="isbn" v-if="book.isbn">ISBN：{{ book.isbn }}</div>
        <div class="price">价格：¥ {{ book.price?.toFixed ? book.price.toFixed(2) : Number(book.price || 0).toFixed(2) }}</div>
        <div class="desc" v-if="book.description">{{ book.description }}</div>
        <div class="buttons">
          <el-button type="primary" @click="addToCart">加入购物车</el-button>
        </div>
      </div>
    </div>
    <div v-else class="loading">加载中...</div>
  </div>
  <FloatingCartButton />
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import FloatingCartButton from '../components/FloatingCartButton.vue'
import { refreshCartCount } from '../stores/cart'
const route = useRoute()
const router = useRouter()
const defaultCover = '/default-book-cover.svg'
const book = ref({})
const loaded = ref(false)
const isBizOk = (payload) => payload && typeof payload === 'object' && Number(payload.code) === 1
const loadDetail = async () => {
  try {
    const id = route.params.id
    const token = localStorage.getItem('token') || ''
    const resp = await fetch(`/user/book/${encodeURIComponent(id)}`, {
      method: 'GET',
      headers: token ? { authentication: token } : {}
    })
    const ct = resp.headers.get('content-type') || ''
    let data = {}
    if (ct.includes('application/json')) {
      try {
        data = await resp.json()
      } catch (_) {}
    }
    if (resp.status === 401) {
      loaded.value = true
      book.value = {}
      ElMessage.warning('登录已过期，请重新登录')
      router.push({ path: '/login', query: { redirect: route.fullPath, msg: '请先登录' } })
      return
    }
    if (resp.ok && isBizOk(data)) {
      const d = data?.data ?? data
      book.value = d || {}
      loaded.value = true
    } else {
      loaded.value = true
      book.value = {}
      ElMessage.error(data?.msg || '加载图书详情失败')
    }
  } catch (_) {
    loaded.value = true
    book.value = {}
    ElMessage.error('加载图书详情失败')
  }
}
onMounted(loadDetail)
const goBack = () => router.back()
const needLogin = () => {
  const tk = localStorage.getItem('token') || ''
  return !tk
}
const addToCart = () => {
  if (needLogin()) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  const id = route.params.id
  fetch(`/user/cart/add/${encodeURIComponent(id)}?num=1`, {
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
      router.push({ path: '/login', query: { redirect: route.fullPath, msg: '请先登录' } })
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
</script>

<style>
.detail {
  max-width: 1000px;
  margin: 0 auto;
  padding: 12px;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.title {
  font-weight: 600;
  font-size: 20px;
}
.body {
  display: grid;
  grid-template-columns: 240px 1fr;
  gap: 16px;
  background: #fff;
  border-radius: 12px;
  padding: 12px;
}
.cover {
  width: 220px;
  height: 220px;
  border-radius: 8px;
  background: #f5f7fa;
  object-fit: cover;
}
.info .name {
  font-weight: 700;
  font-size: 18px;
  margin-bottom: 8px;
}
.info .author, .info .isbn, .info .publisher {
  color: #606266;
  margin-bottom: 6px;
}
.info .price {
  color: #d97706;
  font-weight: 700;
  margin-bottom: 8px;
}
.buttons {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}
.loading {
  text-align: center;
  color: #606266;
}
</style>
