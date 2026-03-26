<template>
  <div class="orders">
    <div class="title-bar">
      <div class="title">我的订单</div>
      <div class="ops">
        <el-button @click="goHome">返回首页</el-button>
      </div>
    </div>
    <div class="order-scope">
      <el-radio-group v-model="orderScope">
        <el-radio-button label="book">普通图书订单</el-radio-button>
        <el-radio-button label="secondHand">二手书订单</el-radio-button>
      </el-radio-group>
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
        <el-radio-button :label="8">已退款</el-radio-button>
      </el-radio-group>
      <el-radio-group v-model="deliveryWay" @change="onDeliveryWayChange" class="type-filter">
        <el-radio-button :label="''">全部类型</el-radio-button>
        <el-radio-button :label="0">自提</el-radio-button>
        <el-radio-button :label="1">配送</el-radio-button>
      </el-radio-group>
    </div>
    <el-table :data="items" :row-key="orderRowKey" border v-loading="loading" class="table" empty-text="暂无订单">
      <el-table-column prop="id" label="订单号" min-width="180" />
      <el-table-column prop="orderTime" label="下单时间" min-width="180">
        <template #default="{ row }">{{ formatTime(row.orderTime) }}</template>
      </el-table-column>
      <el-table-column prop="totalAmount" label="金额" width="120">
        <template #default="{ row }">¥ {{ toMoney(row.totalAmount) }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)" effect="light">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="deliveryWay" label="订单类型" width="100">
        <template #default="{ row }">
          <el-tag :type="deliveryWayTagType(row.deliveryWay)" effect="light">{{ deliveryWayText(row.deliveryWay) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="payStatus" label="支付" width="100">
        <template #default="{ row }">
          <el-tag :type="payTagType(row.payStatus)" effect="light">{{ payText(row.payStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="320">
        <template #default="{ row }">
          <el-button size="small" @click="viewDetail(row)">详情</el-button>
          <el-button size="small" type="primary" v-if="row.status === 1 && row.payStatus === 0" @click="goPay(row)">去支付</el-button>
          <el-button size="small" type="danger" v-if="row.status === 1 && row.payStatus === 0" @click="onCancel(row)">取消</el-button>
          <el-button size="small" type="warning" v-if="row.status === 5 && row.payStatus === 1" @click="onReturn(row)">申请退货</el-button>
          <el-button
            size="small"
            v-if="orderScope !== 'secondHand' && (row.status !== 1 || row.payStatus !== 0)"
            @click="onRebuy(row)"
          >再来一单</el-button>
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
          <div>订单号：{{ detail.orderNumber || detail.id }}</div>
          <div>下单时间：{{ formatTime(detail.orderTime) }}</div>
          <div>订单金额：¥ {{ toMoney(detail.totalAmount) }}</div>
          <div class="status-line">
            状态：
            <el-tag size="small" :type="statusTagType(detail.status)" effect="light">{{ statusText(detail.status) }}</el-tag>
            <el-tag size="small" :type="payTagType(detail.payStatus)" effect="light">{{ payText(detail.payStatus) }}</el-tag>
          </div>
          <div class="status-line">
            订单类型：
            <el-tag size="small" :type="deliveryWayTagType(detail.deliveryWay)" effect="light">{{ deliveryWayText(detail.deliveryWay) }}</el-tag>
          </div>
          <div class="status-line">
            支付方式：
            <el-tag size="small" :type="payWayTagType(detail.payWay)" effect="light">{{ payWayText(detail.payWay) }}</el-tag>
          </div>
          <div>支付时间：{{ formatTime(detail.payTime) || '-' }}</div>
          <div class="status-line">
            配送时效：
            <el-tag size="small" :type="deliveryStatusTagType(detail.deliveryStatus)" effect="light">{{ deliveryStatusText(detail.deliveryStatus) }}</el-tag>
          </div>
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
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const defaultCover = '/default-book-cover.svg'
const orderScope = ref('book')
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
  const map = { 1: '待付款', 2: '待接单', 3: '已接单', 4: '派送中', 5: '已完成', 6: '已取消', 7: '退货审核中', 8: '已退款' }
  return map[s] || '未知'
}
const statusTagType = (s) => {
  const map = { 1: 'warning', 2: 'primary', 3: 'primary', 4: 'warning', 5: 'success', 6: 'info', 7: 'danger', 8: 'success' }
  return map[s] || 'info'
}
const payText = (s) => {
  const map = { 0: '未支付', 1: '已支付', 2: '退款' }
  return map[s] != null ? map[s] : '未知'
}
const payTagType = (s) => {
  const map = { 0: 'warning', 1: 'success', 2: 'info' }
  return map[s] ?? 'info'
}
const deliveryWayText = (v) => {
  const map = { 0: '自提', 1: '配送' }
  return map[v] != null ? map[v] : '未知'
}
const deliveryWayTagType = (v) => {
  const map = { 0: 'info', 1: 'primary' }
  return map[v] ?? 'info'
}
const payWayText = (v) => {
  const map = { 1: '支付宝', 2: '钱包支付' }
  return map[v] != null ? map[v] : '未知'
}
const payWayTagType = (v) => {
  const map = { 1: 'primary', 2: 'success' }
  return map[v] ?? 'info'
}
const deliveryStatusText = (v) => {
  const map = { 0: '定时送达', 1: '立即送出' }
  return map[v] != null ? map[v] : '-'
}
const deliveryStatusTagType = (v) => {
  const map = { 0: 'info', 1: 'warning' }
  return map[v] ?? 'info'
}
/** 后端 Result：code===1 成功；HTTP 200 时仍可能 code=0（业务失败） */
const isBizOk = (payload) => {
  if (!payload || typeof payload !== 'object') return false
  return Number(payload.code) === 1
}

const orderRowKey = (row) => row?.id ?? row?.orderNumber ?? '-'

const showDot = (s) => {
  if (s === 5 || s === 6 || s === 8) return false
  const key = s
  const m = statusCount.value || {}
  const n = m[key] ?? m[String(key)] ?? 0
  return Number(n) > 0
}
const loadStatusCount = async () => {
  try {
    const token = localStorage.getItem('token') || ''
    if (!token) return
    const countUrl =
      orderScope.value === 'secondHand' ? '/user/secondHandOrder/statusCount' : '/user/order/statusCount'
    const resp = await fetch(countUrl, {
      method: 'GET',
      headers: { authentication: token }
    })
    const ct = resp.headers.get('content-type') || ''
    let data = {}
    if (ct.includes('application/json')) {
      try { data = await resp.json() } catch (_) {}
    }
    if (resp.ok && isBizOk(data)) {
      const d = data?.data ?? {}
      statusCount.value = d && typeof d === 'object' ? d : {}
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
    const pageUrl =
      orderScope.value === 'secondHand' ? '/user/secondHandOrder/page' : '/user/order/page'
    const resp = await fetch(`${pageUrl}?${qs.toString()}`, {
      method: 'GET',
      headers: { authentication: token }
    })
    const ct = resp.headers.get('content-type') || ''
    let data = {}
    if (ct.includes('application/json')) {
      try { data = await resp.json() } catch (_) {}
    }
    if (resp.ok && isBizOk(data)) {
      if (currentSeq !== latestLoadSeq) return
      const d = data?.data ?? data
      total.value = Number(d?.total ?? 0) || 0
      const rows = Array.isArray(d?.records) ? d.records : (Array.isArray(d) ? d : [])
      items.value = rows
      loadStatusCount()
    } else {
      if (currentSeq !== latestLoadSeq) return
      items.value = []
      total.value = 0
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
watch(orderScope, () => {
  page.value = 1
  loadOrders()
})

const viewDetail = async (row) => {
  const oid = row?.id ?? row?.orderNumber
  if (!row || !oid) {
    ElMessage.warning('订单号缺失')
    return
  }
  try {
    const token = localStorage.getItem('token') || ''
    const resp = await fetch(`/user/order/orderDetail/${encodeURIComponent(String(oid))}`, {
      method: 'GET',
      headers: { authentication: token }
    })
    const ct = resp.headers.get('content-type') || ''
    let data = {}
    if (ct.includes('application/json')) {
      try { data = await resp.json() } catch (_) {}
    }
    if (resp.ok && isBizOk(data)) {
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
  const oid = row?.id ?? row?.orderNumber
  if (!row || !oid) return
  try {
    await ElMessageBox.confirm(`确认取消订单 ${oid} 吗？`, '提示', { type: 'warning' })
  } catch (_) { return }
  try {
    const token = localStorage.getItem('token') || ''
    const resp = await fetch(`/user/order/cancel/${encodeURIComponent(String(oid))}`, {
      method: 'PUT',
      headers: { authentication: token }
    })
    let data = {}
    if (resp.headers.get('content-type')?.includes('application/json')) {
      try { data = await resp.json() } catch (_) {}
    }
    if (resp.ok && isBizOk(data)) {
      ElMessage.success('已取消订单')
      loadOrders()
      loadStatusCount()
    } else {
      ElMessage.error(data?.msg || '取消失败')
    }
  } catch (_) {
    ElMessage.error('网络错误')
  }
}
const onRebuy = async (row) => {
  const oid = row?.id ?? row?.orderNumber
  if (!row || !oid) return
  try {
    const token = localStorage.getItem('token') || ''
    const resp = await fetch(`/user/order/repetition/${encodeURIComponent(String(oid))}`, {
      method: 'POST',
      headers: { authentication: token }
    })
    let data = {}
    if (resp.headers.get('content-type')?.includes('application/json')) {
      try { data = await resp.json() } catch (_) {}
    }
    if (resp.ok && isBizOk(data)) {
      ElMessage.success('商品已加入购物车')
      router.push('/cart')
    } else {
      ElMessage.error(data?.msg || '操作失败')
    }
  } catch (_) {
    ElMessage.error('网络错误')
  }
}
const onReturn = async (row) => {
  const oid = row?.id ?? row?.orderNumber
  if (!row || !oid) return
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
    const resp = await fetch(`/user/order/return/${encodeURIComponent(String(oid))}${qs}`, {
      method: 'POST',
      headers: { authentication: token }
    })
    const ct = resp.headers.get('content-type') || ''
    let data = {}
    if (ct.includes('application/json')) {
      try { data = await resp.json() } catch (_) {}
    }
    if (resp.ok && isBizOk(data)) {
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
  const oid = row?.id ?? row?.orderNumber
  if (!row || !oid) {
    ElMessage.error('缺少订单号，无法支付')
    return
  }
  const payWin = window.open('', '_blank')
  try {
    const token2 = localStorage.getItem('token') || ''
    const payResp = await fetch(`/api/alipay/pay?id=${encodeURIComponent(String(oid))}`, {
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
        if (!url) url = `/api/alipay/pay?id=${encodeURIComponent(String(oid))}`
        if (payWin) payWin.location.href = url
      }
    }
    ElMessage.success('前往支付')
  } catch (_) {
    if (payWin) payWin.location.href = `/api/alipay/pay?id=${encodeURIComponent(String(oid))}`
  }
}
const goHome = () => router.push('/')
onMounted(() => {
  const q = route.query || {}
  const scope = q.scope
  if (scope === 'secondHand' || scope === 'book') {
    orderScope.value = scope
  }
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
.order-scope {
  margin-bottom: 10px;
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
.status-line {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.cover {
  width: 64px;
  height: 64px;
  border-radius: 6px;
  background: #f5f7fa;
  object-fit: cover;
}
</style>
