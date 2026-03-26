<template>
  <div class="sh-page">
    <div class="head">
      <div class="title">二手书 · 校园回收</div>
      <div class="ops">
        <el-input v-model="keyword" placeholder="搜索书名" clearable class="search" @keyup.enter="load" @clear="load" />
        <el-button type="primary" @click="load">搜索</el-button>
        <el-button @click="goSell">我要卖书</el-button>
        <el-button @click="goHome">回首页</el-button>
      </div>
    </div>
    <div class="hint">以下图书均为同学闲置经本店评估后上架，价格按成色对原价打折。</div>
    <el-empty v-if="!loading && !items.length" description="暂无在售二手书" />
    <div v-else class="grid" v-loading="loading">
      <el-card v-for="row in items" :key="row.id" class="card" shadow="hover" @click="goDetail(row.id)">
        <img :src="row.coverImage || defaultCover" class="cover" alt="" />
        <div class="name">{{ row.bookTitle }}</div>
        <div class="sub">{{ row.bookAuthor }}</div>
        <div class="price-row">
          <span class="sale">¥ {{ toMoney(row.salePrice) }}</span>
          <span class="orig" v-if="row.bookOriginalPrice">原价 ¥{{ toMoney(row.bookOriginalPrice) }}</span>
        </div>
        <el-tag size="small" effect="light">{{ row.conditionGradeText }}</el-tag>
      </el-card>
    </div>
    <div class="pager">
      <el-pagination
        background
        layout="prev, pager, next, total"
        :total="total"
        v-model:current-page="page"
        :page-size="pageSize"
        @current-change="onPageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const defaultCover = '/default-book-cover.svg'
const items = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(12)
const keyword = ref('')
const loading = ref(false)

const toMoney = (v) => (Number(v || 0)).toFixed(2)

const onPageChange = () => {
  load()
}

const load = async () => {
  loading.value = true
  try {
    const qs = new URLSearchParams({ page: String(page.value), pageSize: String(pageSize.value) })
    if (keyword.value.trim()) qs.set('title', keyword.value.trim())
    const resp = await fetch(`/user/secondHand/onSale?${qs}`)
    const text = await resp.text()
    let data = {}
    try {
      if (text) data = JSON.parse(text)
    } catch {
      ElMessage.error('服务器返回异常，请确认用户端已启动且已执行 sql/second_hand_listing.sql')
      return
    }
    if (!resp.ok) {
      ElMessage.error(data?.msg || data?.message || `加载失败（HTTP ${resp.status}）`)
      return
    }
    const code = data?.code
    if (code !== undefined && code !== null && Number(code) !== 1) {
      ElMessage.error(data?.msg || data?.message || '加载失败')
      return
    }
    const pr = data?.data
    if (!pr || typeof pr !== 'object') {
      ElMessage.error(data?.msg || '加载失败：数据格式异常')
      return
    }
    const records = pr.records
    items.value = Array.isArray(records) ? records : []
    total.value = Number(pr.total) || 0
  } catch (_) {
    ElMessage.error('网络错误，请检查本机网络或开发代理是否指向 store-main:8080')
  } finally {
    loading.value = false
  }
}

const goDetail = (id) => router.push(`/second-hand/${id}`)
const goSell = () => router.push('/second-hand/sell')
const goHome = () => router.push('/')

onMounted(load)
</script>

<style scoped>
.sh-page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 16px;
}
.head {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}
.title {
  font-weight: 600;
  font-size: 20px;
}
.ops {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}
.search {
  width: 200px;
}
.hint {
  color: #64748b;
  font-size: 13px;
  margin-bottom: 16px;
}
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}
.card {
  cursor: pointer;
  border-radius: 12px;
}
.cover {
  width: 100%;
  height: 180px;
  object-fit: cover;
  border-radius: 8px;
  background: #f1f5f9;
}
.name {
  font-weight: 600;
  margin-top: 8px;
  font-size: 15px;
}
.sub {
  color: #64748b;
  font-size: 13px;
  margin: 4px 0;
}
.price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin: 8px 0;
}
.sale {
  color: #b45309;
  font-weight: 700;
  font-size: 18px;
}
.orig {
  font-size: 12px;
  color: #94a3b8;
  text-decoration: line-through;
}
.pager {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
