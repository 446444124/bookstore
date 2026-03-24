<template>
  <el-card class="orders-card">
    <div class="toolbar">
      <el-input v-model="qOrderNo" placeholder="订单号" clearable class="keyword" @keyup.enter.native="onSearch" @clear="onSearch" />
      <el-input v-model="qPhone" placeholder="手机号" clearable class="keyword" @keyup.enter.native="onSearch" @clear="onSearch" />
      <el-select v-model="qStatus" placeholder="订单状态" clearable class="status">
        <el-option label="待付款" :value="1" />
        <el-option label="待接单" :value="2" />
        <el-option label="已接单" :value="3" />
        <el-option label="派送中" :value="4" />
        <el-option label="退货审核中" :value="7" />
        <el-option label="已完成" :value="5" />
        <el-option label="已取消" :value="6" />
        <el-option label="已退款" :value="8" />
      </el-select>
      <el-select v-model="qDeliveryWay" placeholder="订单类型" clearable class="status">
        <el-option label="自提" :value="0" />
        <el-option label="配送" :value="1" />
      </el-select>
      <el-button type="primary" @click="onSearch">搜索</el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="items"
      border
      stripe
      height="760"
      empty-text="暂无订单"
    >
      <el-table-column prop="id" label="订单号" min-width="200" />
      <el-table-column prop="orderTime" label="下单时间" min-width="180">
        <template #default="{ row }">{{ formatTime(row.orderTime) }}</template>
      </el-table-column>
      <el-table-column prop="totalAmount" label="金额" width="110">
        <template #default="{ row }">¥ {{ toMoney(row.totalAmount) }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)" effect="light">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="deliveryWay" label="类型" width="90">
        <template #default="{ row }">
          <el-tag :type="deliveryWayTagType(row.deliveryWay)" effect="light">{{ deliveryWayText(row.deliveryWay) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="consignee" label="收货人" width="120" />
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column label="操作" width="360" fixed="right">
        <template #default="{ row }">
          <div class="ops">
            <el-button size="small" @click="viewDetail(row)">详情</el-button>
            <el-button size="small" type="success" v-if="row.status === 2" @click="onConfirm(row)">接单</el-button>
            <el-button size="small" type="danger" v-if="row.status === 2" @click="onReject(row)">拒单</el-button>
            <el-button size="small" type="warning" v-if="row.status === 3" @click="onDelivery(row)">派送</el-button>
            <el-button size="small" type="primary" v-if="row.status === 4" @click="onComplete(row)">完成</el-button>
            <el-button size="small" type="success" v-if="row.status === 7" @click="onApproveReturn(row)">同意退货</el-button>
            <el-button size="small" type="danger" v-if="row.status === 7" @click="onRejectReturn(row)">驳回退货</el-button>
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

    <el-dialog v-model="detailVisible" title="订单详情" width="760px">
      <div v-if="detail" class="detail-wrap">
        <div class="detail-grid">
          <div>订单号：{{ detail.orderNumber || detail.id }}</div>
          <div>下单时间：{{ formatTime(detail.orderTime) }}</div>
          <div class="status-line">
            状态：
            <el-tag size="small" :type="statusTagType(detail.status)" effect="light">{{ statusText(detail.status) }}</el-tag>
          </div>
          <div class="status-line">
            支付：
            <el-tag size="small" :type="payTagType(detail.payStatus)" effect="light">{{ payText(detail.payStatus) }}</el-tag>
          </div>
          <div class="status-line">
            类型：
            <el-tag size="small" :type="deliveryWayTagType(detail.deliveryWay)" effect="light">{{ deliveryWayText(detail.deliveryWay) }}</el-tag>
          </div>
          <div class="status-line">
            支付方式：
            <el-tag size="small" :type="payWayTagType(detail.payWay)" effect="light">{{ payWayText(detail.payWay) }}</el-tag>
          </div>
          <div>收货人：{{ detail.consignee || '-' }}</div>
          <div>手机号：{{ detail.phone || '-' }}</div>
          <div class="wide">地址：{{ detail.address || '-' }}</div>
          <div class="wide">备注：{{ detail.remark || '-' }}</div>
        </div>
        <el-table :data="detail.items || []" border>
          <el-table-column prop="title" label="书名" />
          <el-table-column prop="quantity" label="数量" width="100" />
          <el-table-column prop="price" label="小计" width="120">
            <template #default="{ row }">¥ {{ toMoney(row.price) }}</template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { http } from '../api/http'
const route = useRoute()

const loading = ref(false)
const items = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const qOrderNo = ref('')
const qPhone = ref('')
const qStatus = ref()
const qDeliveryWay = ref()
const detailVisible = ref(false)
const detail = ref(null)

const toMoney = (v) => (Number(v || 0)).toFixed(2)
const formatTime = (v) => (v ? String(v).replace('T', ' ').slice(0, 19) : '')
const statusText = (s) => ({ 1: '待付款', 2: '待接单', 3: '已接单', 4: '派送中', 5: '已完成', 6: '已取消', 7: '退货审核中', 8: '已退款' }[s] || '未知')
const statusTagType = (s) => ({ 1: 'warning', 2: 'primary', 3: 'primary', 4: 'warning', 5: 'success', 6: 'info', 7: 'danger', 8: 'success' }[s] || 'info')
const deliveryWayText = (v) => ({ 0: '自提', 1: '配送' }[v] ?? '未知')
const deliveryWayTagType = (v) => ({ 0: 'info', 1: 'primary' }[v] ?? 'info')
const payText = (s) => ({ 0: '未支付', 1: '已支付', 2: '退款' }[s] ?? '未知')
const payTagType = (s) => ({ 0: 'warning', 1: 'success', 2: 'info' }[s] ?? 'info')
const payWayText = (v) => ({ 1: '支付宝', 2: '钱包支付' }[v] ?? '未知')
const payWayTagType = (v) => ({ 1: 'primary', 2: 'success' }[v] ?? 'info')
const syncStatusFromRoute = () => {
  const p = route.path || ''
  if (p.endsWith('/pending-confirm')) qStatus.value = 2
  else if (p.endsWith('/pending-delivery')) qStatus.value = 3
  else if (p.endsWith('/pending-complete')) qStatus.value = 4
  else if (p.endsWith('/return-review')) qStatus.value = 7
  else {
    // 回到订单总菜单时恢复顶部筛选默认态
    qStatus.value = undefined
    qDeliveryWay.value = undefined
    qOrderNo.value = ''
    qPhone.value = ''
  }
}

const qs = () => {
  const p = new URLSearchParams()
  p.set('page', String(page.value))
  p.set('pageSize', String(pageSize.value))
  if (qOrderNo.value) p.set('orderNumber', qOrderNo.value)
  if (qPhone.value) p.set('phone', qPhone.value)
  if (qStatus.value !== undefined && qStatus.value !== null && qStatus.value !== '') p.set('status', String(qStatus.value))
  if (qDeliveryWay.value !== undefined && qDeliveryWay.value !== null && qDeliveryWay.value !== '') p.set('deliveryWay', String(qDeliveryWay.value))
  return p.toString()
}

const fetchData = async () => {
  loading.value = true
  try {
    const resp = await http(`/admin/order/page?${qs()}`, { method: 'GET', json: false })
    if (resp && resp.code === 1) {
      const data = resp.data
      const rows = Array.isArray(data) ? data : (data?.records || data?.list || [])
      items.value = rows || []
      total.value = data?.total ?? rows.length
    } else {
      ElMessage.error(resp?.msg || '加载失败')
    }
  } catch (_) {
    ElMessage.error('请求失败')
  } finally {
    loading.value = false
  }
}

const viewDetail = async (row) => {
  if (!row?.id) return
  const resp = await http(`/admin/order/detail/${encodeURIComponent(row.id)}`, { method: 'GET', json: false })
  if (resp && resp.code === 1) {
    detail.value = resp.data
    detailVisible.value = true
  } else {
    ElMessage.error(resp?.msg || '加载详情失败')
  }
}

const callAction = async (path, successMsg) => {
  const resp = await http(path, { method: 'POST', json: false })
  if (resp && resp.code === 1) {
    ElMessage.success(successMsg)
    fetchData()
  } else {
    ElMessage.error(resp?.msg || '操作失败')
  }
}

const onConfirm = async (row) => callAction(`/admin/order/confirm/${encodeURIComponent(row.id)}`, '已接单')
const onDelivery = async (row) => callAction(`/admin/order/delivery/${encodeURIComponent(row.id)}`, '已设为派送中')
const onComplete = async (row) => callAction(`/admin/order/complete/${encodeURIComponent(row.id)}`, '订单已完成')
const onReject = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入拒单原因', '拒单', {
      inputPlaceholder: '例如：库存不足',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    })
    const reason = value ? `?reason=${encodeURIComponent(value)}` : ''
    await callAction(`/admin/order/reject/${encodeURIComponent(row.id)}${reason}`, '已拒单')
  } catch (_) {}
}
const onApproveReturn = async (row) => callAction(`/admin/order/return/approve/${encodeURIComponent(row.id)}`, '已通过退货并退款')
const onRejectReturn = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回退货', {
      inputPlaceholder: '例如：超出退货时效',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    })
    const reason = value ? `?reason=${encodeURIComponent(value)}` : ''
    await callAction(`/admin/order/return/reject/${encodeURIComponent(row.id)}${reason}`, '已驳回退货')
  } catch (_) {}
}

const onPageChange = (p) => {
  page.value = p
  fetchData()
}
const onSizeChange = (s) => {
  pageSize.value = s
  page.value = 1
  fetchData()
}
const onSearch = () => {
  page.value = 1
  fetchData()
}

onMounted(() => {
  syncStatusFromRoute()
  fetchData()
})
watch(() => route.path, () => {
  syncStatusFromRoute()
  page.value = 1
  fetchData()
})
</script>

<style>
.orders-card { min-height: 92vh; }
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}
.keyword { width: 260px; }
.status { width: 180px; }
.ops { display: flex; gap: 8px; flex-wrap: wrap; }
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
.pager .total-text {
  color: #606266;
  margin-right: 12px;
}
.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
  margin-bottom: 10px;
}
.detail-grid .wide { grid-column: 1 / span 2; }
.status-line {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
</style>
