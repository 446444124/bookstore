<template>
  <el-card class="books-card">
    <div class="toolbar">
      <el-input v-model="title" placeholder="书名" clearable @keyup.enter.native="onSearch" @clear="onSearch" class="keyword" />
      <el-input v-model="author" placeholder="作者" clearable @keyup.enter.native="onSearch" @clear="onSearch" class="keyword" />
      <el-input v-model="isbn" placeholder="ISBN" clearable @keyup.enter.native="onSearch" @clear="onSearch" class="keyword" />
      <el-select v-model="status" placeholder="状态" clearable class="status">
        <el-option label="起售" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-select v-model="categoryId" placeholder="分类" clearable class="category">
        <el-option
          v-for="c in categories"
          :key="c.id"
          :label="c.name"
          :value="c.id"
        />
      </el-select>
      <el-button type="primary" @click="onSearch">搜索</el-button>
      <el-button type="success" @click="goCreate">新增图书</el-button>
      <el-button type="danger" :disabled="selectedIds.length === 0" @click="onDeleteSelected">批量删除</el-button>
    </div>
    <el-table
      ref="tableRef"
      v-loading="loading"
      :data="items"
      border
      stripe
      class="table"
      height="780"
      empty-text="暂无数据"
      @selection-change="onSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column label="封面" width="100">
        <template #default="{ row }">
          <el-image
            :src="row.coverImage || defaultCover"
            fit="cover"
            style="width:64px;height:64px;border-radius:6px; background:#f4f4f5"
          />
        </template>
      </el-table-column>
      <el-table-column prop="title" label="书名" show-overflow-tooltip />
      <el-table-column prop="author" label="作者" show-overflow-tooltip />
      <el-table-column prop="publisher" label="出版社" show-overflow-tooltip />
      <el-table-column prop="isbn" label="ISBN" width="160" />
      <el-table-column prop="price" label="价格" width="100" />
      <el-table-column prop="stock" label="库存" width="100" />
      <el-table-column label="分类" width="160">
        <template #default="{ row }">
          {{ categoryMap[row.categoryId] || '未分类' }}
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column label="状态" width="140">
        <template #default="{ row }">
          <div class="state-cell">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              active-text="起售"
              inactive-text="禁用"
              @change="onStatusChange(row, $event)"
            />
            <el-tag size="small" :type="bookStatusTagType(row.status)" effect="light">{{ bookStatusText(row.status) }}</el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <div class="op">
            <el-button type="primary" size="small" @click="goEdit(row.id)">编辑</el-button>
            <el-button type="danger" size="small" @click="onDeleteRow(row.id)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <div class="pager">
      <div class="total-text">共 {{ total }} 条记录</div>
      <el-pagination
        background
        layout="prev, pager, next, sizes"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pageSize"
        :current-page="page"
        @current-change="onPageChange"
        @size-change="onSizeChange"
      />
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { http } from '../api/http'
import { useRouter } from 'vue-router'
import { setBookIds, useBooksStore } from '../store/books'

const loading = ref(false)
const tableRef = ref()
const items = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const title = ref('')
const author = ref('')
const isbn = ref('')
const status = ref()
const categoryId = ref()
const router = useRouter()
const savedIdsOnce = ref(false)
const booksStore = useBooksStore()
const defaultCover = '/default-book-cover.svg'
const selectedIds = ref([])
const categories = ref([])
const categoryMap = {}
const bookStatusText = (v) => (Number(v) === 1 ? '起售' : '禁用')
const bookStatusTagType = (v) => (Number(v) === 1 ? 'success' : 'info')

const qs = () => {
  const p = new URLSearchParams()
  p.set('page', String(page.value))
  p.set('pageSize', String(pageSize.value))
  if (title.value) p.set('title', title.value)
  if (author.value) p.set('author', author.value)
  if (isbn.value) p.set('isbn', isbn.value)
  if (status.value !== undefined && status.value !== null && status.value !== '') p.set('status', String(status.value))
  if (categoryId.value !== undefined && categoryId.value !== null && categoryId.value !== '') p.set('categoryId', String(categoryId.value))
  return p.toString()
}

const fetchData = async () => {
  loading.value = true
  try {
    const resp = await http(`/admin/book/page?${qs()}`, { method: 'GET', json: false })
    if (resp && resp.code === 1) {
      const data = resp.data
      let rows = []
      if (Array.isArray(data)) rows = data
      else rows = data?.records || data?.list || data?.items || data?.rows || data?.data || []
      items.value = rows || []
      total.value = data?.total ?? data?.count ?? data?.totalCount ?? items.value.length
      if (!savedIdsOnce.value && (!booksStore.ids || booksStore.ids.length === 0)) {
        const ids = items.value.map(b => b?.id).filter(Boolean)
        if (ids.length) {
          setBookIds(ids)
          savedIdsOnce.value = true
        }
      }
    } else {
      ElMessage.error(resp?.msg || '加载失败')
    }
  } catch (e) {
    ElMessage.error('请求失败')
  } finally {
    loading.value = false
  }
}

const fetchCategories = async () => {
  try {
    const resp = await http(`/admin/category/page?page=1&pageSize=1000`, { method: 'GET', json: false })
    if (resp && resp.code === 1) {
      const data = resp.data
      let rows = []
      if (Array.isArray(data)) rows = data
      else rows = data?.records || data?.list || data?.items || data?.rows || data?.data || []
      categories.value = rows || []
      rows.forEach(c => {
        if (c && typeof c.id !== 'undefined') categoryMap[c.id] = c.name || ''
      })
    }
  } catch (_) {}
}
const goEdit = (id) => {
  if (!id) return
  router.push(`/admin/books/${id}/edit`)
}
const goCreate = () => {
  router.push('/admin/books/create')
}
const onStatusChange = async (row, val) => {
  const old = row.status
  row.status = val
  try {
    const resp = await http(`/admin/book/status/${val}?id=${encodeURIComponent(row.id)}`, {
      method: 'POST',
      json: false
    })
    if (!(resp && resp.code === 1)) {
      row.status = old
      ElMessage.error(resp?.msg || '状态更新失败')
    }
  } catch (_) {
    row.status = old
    ElMessage.error('网络错误')
  }
}

const onPageChange = p => {
  page.value = p
  fetchData()
}
const onSizeChange = s => {
  pageSize.value = s
  page.value = 1
  fetchData()
}
const onSearch = () => {
  page.value = 1
  fetchData()
}

const onSelectionChange = (rows) => {
  selectedIds.value = (rows || []).map(r => r?.id).filter(Boolean)
}

const onDeleteSelected = async () => {
  if (!selectedIds.value.length) return
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 本图书？`, '提示', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch (_) {
    return
  }
  try {
    const fd = new FormData()
    selectedIds.value.forEach(id => fd.append('ids', id))
    const resp = await http('/admin/book', { method: 'DELETE', body: fd, json: false })
    if (resp && resp.code === 1) {
      ElMessage.success('删除成功')
      tableRef.value?.clearSelection()
      selectedIds.value = []
      fetchData()
    } else {
      ElMessage.error(resp?.msg || '删除失败')
    }
  } catch (_) {
    ElMessage.error('网络错误')
  }
}

const onDeleteRow = async (id) => {
  if (!id) return
  try {
    await ElMessageBox.confirm('确定删除该图书？', '提示', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch (_) {
    return
  }
  try {
    const fd = new FormData()
    fd.append('ids', id)
    const resp = await http('/admin/book', { method: 'DELETE', body: fd, json: false })
    if (resp && resp.code === 1) {
      ElMessage.success('删除成功')
      fetchData()
    } else {
      ElMessage.error(resp?.msg || '删除失败')
    }
  } catch (_) {
    ElMessage.error('网络错误')
  }
}

onMounted(async () => {
  await fetchCategories()
  await fetchData()
})
</script>

<style>
.books-card {
  min-height: 92vh;
}
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.keyword {
  width: 380px;
}
.status {
  width: 200px;
}
.category {
  width: 200px;
}
.table .el-table__row .op {
  display: flex;
  gap: 8px;
}
.state-cell {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
.pager .total-text {
  color: #606266;
  margin-right: 12px;
}
</style>
