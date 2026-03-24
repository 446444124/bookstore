<template>
  <div class="orders">
    <div class="title-bar">
      <div class="title">我的订单</div>
      <div class="ops">
        <el-button @click="goHome">返回首页</el-button>
      </div>
    </div>
    <div class="filters">
      <el-radio-group v-model="status" @change="onStatusChange">
        <el-radio-button :label="''">全部</el-radio-button>
        <el-radio-button :label="1">
          <span class="status-with-dot">待付款<span v-if="showDot(1)" class="red-dot"></span></span>
        </el-radio-button>
        <el-radio-button :label="2">
          <span class="status-with-dot">待接单<span v-if="showDot(2)" class="red-dot"></span></span>
        </el-radio-button>
        <el-radio-button :label="3">
          <span class="status-with-dot">已接单<span v-if="showDot(3)" class="red-dot"></span></span>
        </el-radio-button>
        <el-radio-button :label="4">
          <span class="status-with-dot">派送中<span v-if="showDot(4)" class="red-dot"></span></span>
        </el-radio-button>
        <el-radio-button :label="7">
          <span class="status-with-dot">退货审核中<span v-if="showDot(7)" class="red-dot"></span></span>
        </el-radio-button>
        <el-radio-button :label="5">已完成</el-radio-button>
        <el-radio-button :label="6">已取消</el-radio-button>
      </el-radio-group>
      <el-radio-group v-model="deliveryWay" @change="onDeliveryWayChange" class="type-filter">
        <el-radio-button :label="''">全部类型</el-radio-button>
        <el-radio-button :label="0">自提</el-radio-button>
        <el-radio-button :label="1">配送</el-radio-button>
      </el-radio-group>
    </div>
    <el-table :data="items" border v-loading="loading" class="table" empty-text="暂无订单">
      <el-table-column prop="id" label="订单号" min-width="180" />
      <el-table-column prop="orderTime" label="下单时间" min-width="180">
        <template #default="{ row }">{{ formatTime(row.orderTime) }}</template>
      </el-table-column>
      <el-table-column prop="totalAmount" label="金额" width="120">
        <template #default="{ row }">¥ {{ toMoney(row.totalAmount) }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">{{ statusText(row.status) }}</template>
      </el-table-column>
      <el-table-column prop="deliveryWay" label="订单类型" width="100">
        <template #default="{ row }">{{ deliveryWayText(row.deliveryWay) }}</template>
      </el-table-column>
      <el-table-column prop="payStatus" label="支付" width="100">
        <template #default="{ row }">{{ payText(row.payStatus) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="320">
        <template #default="{ row }">
          <el-button size="small" @click="viewDetail(row)">详情</el-button>
          <el-button size="small" type="primary" v-if="row.status === 1 && row.payStatus === 0" @click="goPay(row)">去支付</el-button>
          <el-button size="small" type="danger" v-if="row.status === 1 && row.payStatus === 0" @click="onCancel(row)">取消</el-button>
          <el-button size="small" type="warning" v-if="row.status === 5 && row.payStatus === 1" @click="onReturn(row)">申请退货</el-button>
          <el-button size="small" v-if="row.status !== 1 || row.payStatus !== 0" @click="onRebuy(row)">再来一单</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pager">
      <el-pagination
        background
        layout="prev, pager, next, sizes, total"
        :page-sizes="[5,10,20]"
        :total="total"
        v-model:page-size="pageSize"
        v-model:current-page="page"
        @current-change="loadOrders"
        @size-change="onSizeChange"
      />
    </div>

    <el-dialog v-model="detailVisible" title="订单详情" width="680px">
      <div v-if="detail">
        <div class="detail-head">
          <div>订单号：{{ detail.orderNumber }}</div>
          <div>下单时间：{{ formatTime(detail.orderTime) }}</div>
          <div>订单金额：¥ {{ toMoney(detail.totalAmount) }}</div>
          <div>状态：{{ statusText(detail.status) }} / {{ payText(detail.payStatus) }}</div>
          <div>订单类型：{{ deliveryWayText(detail.deliveryWay) }}</div>
          <div>支付方式：{{ payWayText(detail.payWay) }}</div>
          <div>支付时间：{{ formatTime(detail.payTime) || '-' }}</div>
          <div>配送时效：{{ deliveryStatusText(detail.deliveryStatus) }}</div>
          <div>预计送达：{{ formatTime(detail.estimatedDeliveryTime) || '-' }}</div>
          <div>实际送达：{{ formatTime(detail.deliveryTime) || '-' }}</div>
          <div>收货人：{{ detail.consignee || '-' }}</div>
          <div>联系电话：{{ detail.phone || '-' }}</div>
          <div class="wide">收货地址：{{ detail.address || '-' }}</div>
          <div class="wide">备注：{{ detail.remark || '-' }}</div>
          <div class="wide" v-if="detail.cancelReason">取消原因：{{ detail.cancelReason }}</div>
          <div class="wide" v-if="detail.rejectionReason">拒单原因：{{ detail.rejectionReason }}</div>
        </div>
        <el-table :data="detail.items || []" border>
          <el-table-column label="封面" width="100">
            <template #default="{ row }">
              <img :src="row.coverImage || defaultCover" class="cover" alt="cover" />
            </template>
          </el-table-column>
          <el-table-column prop="title" label="书名" />
          <el-table-column prop="price" label="小计" width="120">
            <template #default="{ row }">¥ {{ toMoney(row.price) }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="100" />
        </el-table>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const defaultCover = '/default-book-cover.svg'
const items = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const status = ref('')
const deliveryWay = ref('')
const statusCount = ref({})
const detailVisible = ref(false)
const detail = ref(null)
let latestLoadSeq = 0

const toMoney = (v) => {
  const n = Number(v || 0)
  return n.toFixed(2)
}
const formatTime = (s) => {
  if (!s) return ''
  try { return String(s).replace('T', ' ').slice(0, 19) } catch (_) { return String(s) }
}
const statusText = (s) => {
  const map = { 1: '待付款', 2: '待接单', 3: '已接单', 4: '派送中', 5: '已完成', 6: '已取消', 7: '退货审核中' }
  return map[s] || '未知'
}
const payText = (s) => {
  const map = { 0: '未支付', 1: '已支付', 2: '退款' }
  return map[s] != null ? map[s] : '未知'
}
const deliveryWayText = (v) => {
  const map = { 0: '自提', 1: '配送' }
  return map[v] != null ? map[v] : '未知'
}
const payWayText = (v) => {
  const map = { 1: '支付宝' }
  return map[v] != null ? map[v] : '未知'
}
const deliveryStatusText = (v) => {
  const map = { 0: '定时送达', 1: '立即送出' }
  return map[v] != null ? map[v] : '-'
}
const showDot = (s) => {
  if (s === 5 || s === 6) return false
  return Number(statusCount.value?.[s] || 0) > 0
}
const loadStatusCount = async () => {
  try {
    const token = localStorage.getItem('token') || ''
    if (!token) return
    const resp = await fetch('/user/order/statusCount', {
      method: 'GET',
      headers: { authentication: token }
    })
    const ct = resp.headers.get('content-type') || ''
    let data = {}
    if (ct.includes('application/json')) {
      try { data = await resp.json() } catch (_) {}
    }
    if (resp.ok) {
      const d = data?.data ?? {}
      statusCount.value = d || {}
    }
  } catch (_) {}
}
const loadOrders = async () => {
  const currentSeq = ++latestLoadSeq
  loading.value = true
  try {
    const token = localStorage.getItem('token') || ''
    if (!token) {
      ElMessage.warning('请先登录')
      router.push({ path: '/login', query: { redirect: '/orders', msg: '请先登录' } })
      loading.value = false
      return
    }
    const qs = new URLSearchParams({ page: String(page.value), pageSize: String(pageSize.value) })
    if (status.value !== '' && status.value !== null && status.value !== undefined) {
      qs.append('status', String(status.value))
    }
    if (deliveryWay.value !== '' && deliveryWay.value !== null && deliveryWay.value !== undefined) {
      qs.append('deliveryWay', String(deliveryWay.value))
    }
    const resp = await fetch(`/user/order/page?${qs.toString()}`, {
      method: 'GET',
      headers: { authentication: token }
    })
    const ct = resp.headers.get('content-type') || ''
    let data = {}
    if (ct.includes('application/json')) {
      try { data = await resp.json() } catch (_) {}
    }
    if (resp.ok) {
      if (currentSeq !== latestLoadSeq) return
      const d = data?.data ?? data
      total.value = Number(d?.total ?? 0) || 0
      const rows = Array.isArray(d?.records) ? d.records : (Array.isArray(d) ? d : [])
      items.value = rows
      loadStatusCount()
    } else {
      if (currentSeq !== latestLoadSeq) return
      ElMessage.error(data?.msg || '加载失败')
    }
  } catch (_) {
    if (currentSeq !== latestLoadSeq) return
    ElMessage.error('网络错误')
  } finally {
    if (currentSeq === latestLoadSeq) {
      loading.value = false
    }
  }
}
const onSizeChange = (n) => {
  pageSize.value = n
  page.value = 1
  loadOrders()
}
const onStatusChange = () => {
  page.value = 1
  loadOrders()
}
const onDeliveryWayChange = () => {
  page.value = 1
  loadOrders()
}
const viewDetail = async (row) => {
  if (!row || !row.id) return
  try {
    const token = localStorage.getItem('token') || ''
    const resp = await fetch(`/user/order/orderDetail/${encodeURIComponent(row.id)}`, {
      method: 'GET',
      headers: { authentication: token }
    })
    const ct = resp.headers.get('content-type') || ''
    let data = {}
    if (ct.includes('application/json')) {
      try { data = await resp.json() } catch (_) {}
    }
    if (resp.ok) {
      const d = data?.data ?? data
      detail.value = d
      detailVisible.value = true
    } else {
      ElMessage.error(data?.msg || '加载详情失败')
    }
  } catch (_) {
    ElMessage.error('网络错误')
  }
}
const onCancel = async (row) => {
  if (!row || !row.id) return
  try {
    await ElMessageBox.confirm(`确认取消订单 ${row.id} 吗？`, '提示', { type: 'warning' })
  } catch (_) { return }
  try {
    const token = localStorage.getItem('token') || ''
    const resp = await fetch(`/user/order/cancel/${encodeURIComponent(row.id)}`, {
      method: 'PUT',
      headers: { authentication: token }
    })
    if (resp.ok) {
      ElMessage.success('已取消订单')
      loadOrders()
      loadStatusCount()
    } else {
      const ct = resp.headers.get('content-type') || ''
      let data = {}
      if (ct.includes('application/json')) {
        try { data = await resp.json() } catch (_) {}
      }
      ElMessage.error(data?.msg || '取消失败')
    }
  } catch (_) {
    ElMessage.error('网络错误')
  }
}
const onRebuy = async (row) => {
  if (!row || !row.id) return
  try {
    const token = localStorage.getItem('token') || ''
    const resp = await fetch(`/user/order/repetition/${encodeURIComponent(row.id)}`, {
      method: 'POST',
      headers: { authentication: token }
    })
    if (resp.ok) {
      ElMessage.success('商品已加入购物车')
      router.push('/cart')
    } else {
      const ct = resp.headers.get('content-type') || ''
      let data = {}
      if (ct.includes('application/json')) {
        try { data = await resp.json() } catch (_) {}
      }
      ElMessage.error(data?.msg || '操作失败')
    }
  } catch (_) {
    ElMessage.error('网络错误')
  }
}
const onReturn = async (row) => {
  if (!row || !row.id) return
  let reason = ''
  try {
    const res = await ElMessageBox.prompt('请填写退货原因（选填）', '申请退货', {
      confirmButtonText: '提交',
      cancelButtonText: '取消',
      inputPlaceholder: '例如：图书破损/内容不符'
    })
    reason = (res?.value || '').trim()
  } catch (_) {
    return
  }
  try {
    const token = localStorage.getItem('token') || ''
    const qs = reason ? `?reason=${encodeURIComponent(reason)}` : ''
    const resp = await fetch(`/user/order/return/${encodeURIComponent(row.id)}${qs}`, {
      method: 'POST',
      headers: { authentication: token }
    })
    const ct = resp.headers.get('content-type') || ''
    let data = {}
    if (ct.includes('application/json')) {
      try { data = await resp.json() } catch (_) {}
    }
    if (resp.ok) {
      ElMessage.success('已提交退货申请，等待商家审批')
      loadOrders()
      loadStatusCount()
    } else {
      ElMessage.error(data?.msg || '退货申请失败')
    }
  } catch (_) {
    ElMessage.error('网络错误')
  }
}
const goPay = async (row) => {
  if (!row || !row.id) {
    ElMessage.error('缺少订单号，无法支付')
    return
  }
  const payWin = window.open('', '_blank')
  try {
    const token2 = localStorage.getItem('token') || ''
    const payResp = await fetch(`/api/alipay/pay?id=${encodeURIComponent(row.id)}`, {
      method: 'GET',
      headers: token2 ? { authentication: token2 } : {}
    })
    const ct2 = payResp.headers.get('content-type') || ''
    const text = await payResp.text()
    const looksHtml = ct2.includes('text/html') || text.trim().startsWith('<') || text.toLowerCase().includes('<html')
    if (looksHtml && payWin) {
      payWin.document.open()
      payWin.document.write(text)
      payWin.document.close()
    } else {
      const m = text.trim().match(/^redirect\s*:\s*(.+)$/i)
      if (m && m[1]) {
        const url = m[1].trim()
        if (payWin) payWin.location.href = url
      } else {
        let url = ''
        try {
          const j = JSON.parse(text)
          url = j?.url || j?.payUrl || j?.data?.url || j?.data?.payUrl || ''
        } catch (_) {
          url = ''
        }
        if (!url) url = `/api/alipay/pay?id=${encodeURIComponent(row.id)}`
        if (payWin) payWin.location.href = url
      }
    }
    ElMessage.success('前往支付')
  } catch (_) {
    if (payWin) payWin.location.href = `/api/alipay/pay?id=${encodeURIComponent(row.id)}`
  }
}
const goHome = () => router.push('/')
onMounted(() => {
  loadStatusCount()
  loadOrders()
})
</script>

<style>
.orders {
  max-width: 1100px;
  margin: 0 auto;
  padding: 12px;
}
.title-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.title {
  font-weight: 600;
  font-size: 18px;
}
.filters {
  margin: 8px 0 12px;
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}
.type-filter {
  margin-left: 4px;
}
.status-with-dot {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.red-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #f56c6c;
  display: inline-block;
}
.table {
  background: #fff;
}
.pager {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
.detail-head {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 6px;
  margin-bottom: 10px;
}
.detail-head .wide {
  grid-column: 1 / span 2;
}
.cover {
  width: 64px;
  height: 64px;
  border-radius: 6px;
  background: #f5f7fa;
  object-fit: cover;
}
</style>
