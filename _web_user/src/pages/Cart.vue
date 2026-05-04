<template>
  <div class="cart">
    <div class="title-bar">
      <div class="title">购物车</div>
      <div class="ops">
        <el-button @click="goBrowse">继续购物</el-button>
        <el-button type="danger" @click="onClean">清空购物车</el-button>
      </div>
    </div>
    <el-table v-if="items.length" :data="items" border style="width: 100%">
      <el-table-column label="封面" width="120">
        <template #default="{ row }">
          <img :src="row.coverImage || defaultCover" class="cover" alt="cover" />
        </template>
      </el-table-column>
      <el-table-column prop="title" label="书名" min-width="200" />
      <el-table-column prop="unitPrice" label="单价" width="120">
        <template #default="{ row }">¥ {{ toMoney(row.unitPrice) }}</template>
      </el-table-column>
      <el-table-column prop="num" label="数量" width="160">
        <template #default="{ row }">
          <div class="qty">
            <el-button size="small" @click="onQtyChange(row, -1)" :disabled="Number(row.num) <= 1">-</el-button>
            <span class="qty-val">{{ row.num }}</span>
            <el-button size="small" type="primary" @click="onQtyChange(row, 1)">+</el-button>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="小计" width="140">
        <template #default="{ row }">¥ {{ toMoney(row.lineAmount) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button type="danger" size="small" @click="onDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div v-else class="empty">购物车为空</div>
    <div class="summary">
      <div>共 {{ totalCount }} 件商品，合计 ¥ {{ toMoney(totalAmount) }}</div>
      <el-button type="primary" :disabled="!items.length" @click="openOrder">去下单</el-button>
    </div>
    <el-dialog v-model="orderDialogVisible" title="提交订单" width="560px">
      <el-form ref="orderFormRef" :model="orderForm" :rules="orderRules" label-width="120px">
        <el-form-item label="地址簿" prop="addressBookId">
          <div class="addr-row">
            <el-select v-model="orderForm.addressBookId" placeholder="请选择地址" class="addr-select">
              <el-option v-for="a in addresses" :key="String(a.id)" :label="a.label ? a.label + '（' + formatAddr(a) + '）' : formatAddr(a)" :value="a.id" />
            </el-select>
            <el-button type="primary" link @click="openAddrDialog">新增地址</el-button>
          </div>
        </el-form-item>
        <el-form-item label="配送方式" prop="deliveryWay">
          <el-radio-group v-model="orderForm.deliveryWay">
            <el-radio :label="1">配送</el-radio>
            <el-radio :label="0">自提</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="orderForm.deliveryWay === 1" label="配送时间" prop="deliveryStatus">
          <el-radio-group v-model="orderForm.deliveryStatus">
            <el-radio :label="1">立即送出</el-radio>
            <el-radio :label="0">选择具体时间</el-radio>
          </el-radio-group>
          <div v-if="orderForm.deliveryWay === 1 && orderForm.deliveryStatus === 1" class="delivery-hint">
            预计送达：提交订单后约 1 小时内（以系统记录时间为准）
          </div>
        </el-form-item>
        <el-form-item label="预计送达时间" prop="estimatedDeliveryTime" v-if="orderForm.deliveryWay === 1 && orderForm.deliveryStatus === 0">
          <el-date-picker
            v-model="orderForm.estimatedDeliveryTime"
            type="datetime"
            placeholder="选择日期时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="付款方式" prop="payWay">
          <el-radio-group v-model="orderForm.payWay" class="pay-way-group">
            <el-radio :label="2" border class="pay-way-option">
              <span class="pay-way-inner">
                <img class="pay-way-ic" src="/wallet.svg" alt="wallet" />
                <span class="pay-way-text">
                  <span class="pay-way-title">钱包支付</span>
                  <span class="pay-way-sub">余额 ¥{{ toMoney(walletBalance) }}</span>
                </span>
              </span>
            </el-radio>
            <el-radio :label="1" border class="pay-way-option">
              <span class="pay-way-inner">
                <img class="pay-way-ic" src="/alipay.svg" alt="alipay" />
                <span class="pay-way-text">
                  <span class="pay-way-title">支付宝</span>
                  <span class="pay-way-sub">推荐</span>
                </span>
              </span>
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="orderForm.remark" type="textarea" placeholder="填写备注（选填）" />
        </el-form-item>
        <el-form-item label="总金额">
          <el-input :model-value="toMoney(totalAmount)" disabled />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-ops">
          <el-button @click="orderDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="ordering" @click="onGoPay">去支付</el-button>
        </div>
      </template>
    </el-dialog>
    <AddressBookDialog v-model="addrDialogVisible" @saved="onAddressSaved" />
    <el-dialog v-model="orderSuccessVisible" title="下单成功" width="520px">
      <div class="order-success">
        <div>订单号：{{ orderSuccess.orderNumber }}</div>
        <div>订单金额：¥ {{ toMoney(orderSuccess.orderAmount) }}</div>
        <div>下单时间：{{ orderSuccess.orderTime }}</div>
        <div class="hint">可在“个人中心-订单”或消息通知中查看详细状态</div>
      </div>
      <template #footer>
        <div class="dialog-ops">
          <el-button type="primary" @click="orderSuccessVisible = false">我知道了</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { refreshCartCount, resetCartCount } from '../stores/cart'
import AddressBookDialog from '../components/AddressBookDialog.vue'
const router = useRouter()
const defaultCover = '/default-book-cover.svg'
const items = ref([])
const toMoney = (v) => {
  const n = Number(v || 0)
  return n.toFixed(2)
}
const loadCart = async () => {
  try {
    const token = localStorage.getItem('token') || ''
    const resp = await fetch('/user/cart/list', {
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
      const rows = Array.isArray(d) ? d : d?.records || d?.list || d?.items || d?.rows || d?.data || []
      items.value = (rows || []).map(r => {
        const num = Number(r?.quantity ?? r?.num ?? 1) || 1
        const lineAmount = Number(r?.amount ?? 0) || 0
        const unitPrice = Number(r?.price ?? 0) || (num > 0 ? lineAmount / num : 0)
        return {
        id: r?.id,
        bookId: r?.bookId,
        title: r?.title,
        coverImage: r?.coverImage,
        unitPrice,
        lineAmount,
        num
      }
      })
    } else {
      items.value = []
    }
  } catch (_) {
    items.value = []
  }
}
const onClean = async () => {
  try {
    const token = localStorage.getItem('token') || ''
    const resp = await fetch('/user/cart/clean', {
      method: 'DELETE',
      headers: token ? { authentication: token } : {}
    })
    if (resp.ok) {
      ElMessage.success('已清空购物车')
      loadCart()
      resetCartCount()
    } else {
      ElMessage.error('清空失败')
    }
  } catch (_) {
    ElMessage.error('清空失败')
  }
}
const goBrowse = () => router.push('/browse')
const totalCount = computed(() => items.value.reduce((sum, it) => sum + (Number(it.num) || 0), 0))
const totalAmount = computed(() =>
  items.value.reduce((sum, it) => sum + (Number(it.lineAmount) || 0), 0)
)
const onQtyChange = async (row, delta) => {
  if (!row || row.bookId == null) return
  if (delta < 0 && Number(row.num) <= 1) return
  try {
    const token = localStorage.getItem('token') || ''
    const resp = await fetch(`/user/cart/add/${encodeURIComponent(row.bookId)}?num=${encodeURIComponent(delta)}`, {
      method: 'POST',
      headers: token ? { authentication: token } : {}
    })
    let data = {}
    if (resp.headers.get('content-type')?.includes('application/json')) {
      try {
        data = await resp.json()
      } catch (_) {}
    }
    if (resp.status === 401) {
      ElMessage.warning('登录已过期，请重新登录')
      router.push({ path: '/login', query: { redirect: '/cart', msg: '请先登录' } })
      return
    }
    if (resp.ok && Number(data.code) === 1) {
      loadCart()
      refreshCartCount()
    } else {
      ElMessage.error(data?.msg || '数量更新失败')
    }
  } catch (_) {
    ElMessage.error('数量更新失败')
  }
}
const onDelete = async (row) => {
  if (!row || row.id == null) return
  try {
    const token = localStorage.getItem('token') || ''
    const resp = await fetch(`/user/cart/delete/${encodeURIComponent(row.id)}`, {
      method: 'DELETE',
      headers: token ? { authentication: token } : {}
    })
    if (resp.ok) {
      ElMessage.success('已删除该商品')
      loadCart()
      refreshCartCount()
    } else {
      ElMessage.error('删除失败')
    }
  } catch (_) {
    ElMessage.error('删除失败')
  }
}
const addrDialogVisible = ref(false)
const addressIdsBeforeAdd = ref(new Set())
const orderDialogVisible = ref(false)
const orderFormRef = ref()
const ordering = ref(false)
const addresses = ref([])
const walletBalance = ref(0)
const orderForm = ref({
  addressBookId: '',
  payWay: 1,
  remark: '',
  estimatedDeliveryTime: '',
  deliveryStatus: 1,
  totalAmount: 0,
  deliveryWay: 1
})
const orderSuccessVisible = ref(false)
const orderSuccess = ref({
  id: '',
  orderNumber: '',
  orderAmount: 0,
  orderTime: ''
})
const orderRules = {
  addressBookId: [{ required: true, message: '请选择地址簿', trigger: 'change' }],
  deliveryWay: [{ required: true, message: '请选择配送方式', trigger: 'change' }],
  payWay: [{ required: true, message: '请选择付款方式', trigger: 'change' }],
  deliveryStatus: [{ required: true, message: '请选择配送时间', trigger: 'change' }],
  estimatedDeliveryTime: [
    {
      validator: (_, v, cb) => {
        if (orderForm.value.deliveryWay === 1 && orderForm.value.deliveryStatus === 0 && (!v || !String(v).trim())) cb(new Error('请选择预计送达时间'))
        else cb()
      },
      trigger: 'change'
    }
  ]
}
const formatAddr = (r) => {
  const parts = [
    r?.provinceName,
    r?.cityName,
    r?.districtName,
    r?.schoolPartition,
    (r?.building && r?.houseNumber) ? `${r.building} ${r.houseNumber}` : (r?.building || r?.houseNumber || '')
  ]
  return parts.filter(Boolean).join(' ')
}
const loadAddresses = async () => {
  try {
    const token = localStorage.getItem('token') || ''
    const resp = await fetch('/user/addressBook/list', {
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
      addresses.value = (rows || []).map(r => ({
        id: r?.id,
        provinceName: r?.provinceName || '',
        cityName: r?.cityName || '',
        districtName: r?.districtName || '',
        schoolPartition: r?.schoolPartition || '',
        building: r?.building || '',
        houseNumber: r?.houseNumber || '',
        label: r?.label || '',
        isDefault: Number(r?.isDefault ?? 0)
      }))
    } else {
      addresses.value = []
    }
  } catch (_) {
    addresses.value = []
  }
}
const loadWalletBalance = async () => {
  try {
    const token = localStorage.getItem('token') || ''
    const resp = await fetch('/user/user/wallet/balance', {
      method: 'GET',
      headers: token ? { authentication: token } : {}
    })
    const ct = resp.headers.get('content-type') || ''
    let data = {}
    if (ct.includes('application/json')) {
      try { data = await resp.json() } catch (_) {}
    }
    if (resp.ok) {
      const d = data?.data ?? 0
      walletBalance.value = Number(d || 0)
    }
  } catch (_) {}
}
const openAddrDialog = () => {
  addressIdsBeforeAdd.value = new Set(addresses.value.map((a) => String(a.id)))
  addrDialogVisible.value = true
}
const onAddressSaved = async () => {
  await loadAddresses()
  const newAddr = addresses.value.find((a) => !addressIdsBeforeAdd.value.has(String(a.id)))
  if (newAddr) orderForm.value.addressBookId = newAddr.id
}
const openOrder = async () => {
  await Promise.all([loadAddresses(), loadWalletBalance()])
  const def = addresses.value.find(a => a.isDefault === 1) || addresses.value[0]
  orderForm.value.addressBookId = def ? def.id : ''
  orderForm.value.deliveryWay = 1
  orderForm.value.deliveryStatus = 1
  orderForm.value.payWay = 1
  orderForm.value.estimatedDeliveryTime = ''
  orderForm.value.remark = ''
  orderForm.value.totalAmount = Number(totalAmount.value.toFixed(2))
  orderDialogVisible.value = true
}
const onGoPay = async () => {
  if (!orderFormRef.value) return
  try {
    await orderFormRef.value.validate()
  } catch (_) {
    return
  }
  ordering.value = true
  try {
    const token = localStorage.getItem('token') || ''
    const payload = {
      addressBookId: orderForm.value.addressBookId,
      payWay: orderForm.value.payWay,
      remark: orderForm.value.remark,
      estimatedDeliveryTime:
        (orderForm.value.deliveryWay === 1 && orderForm.value.deliveryStatus === 0)
          ? orderForm.value.estimatedDeliveryTime
          : '',
      deliveryStatus: orderForm.value.deliveryStatus,
      totalAmount: Number(totalAmount.value.toFixed(2)),
      deliveryWay: orderForm.value.deliveryWay
    }
    const resp = await fetch('/user/order/submit', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...(token ? { authentication: token } : {}) },
      body: JSON.stringify(payload)
    })
    const ct = resp.headers.get('content-type') || ''
    let data = {}
    if (ct.includes('application/json')) {
      try { data = await resp.json() } catch (_) {}
    }
    const bizOk = resp.ok && (data?.code === undefined || data?.code === 1)
    if (bizOk) {
      const d = data?.data ?? data
      const o = d?.data ?? d
      const oid = o?.id ?? o?.orderId ?? ''
      const ono = o?.orderNumber ?? o?.no ?? o?.number ?? ''
      if (!oid && !ono) {
        ElMessage.error('下单失败，请重试')
        return
      }
      const oamt = Number((o?.orderAmount ?? o?.amount ?? o?.totalAmount ?? orderForm.value.totalAmount) ?? 0) || 0
      const otm = o?.orderTime ?? o?.createTime ?? o?.createdAt ?? ''
      orderSuccess.value = { id: String(oid || ''), orderNumber: String(ono || ''), orderAmount: oamt, orderTime: String(otm || '') }
      if (orderForm.value.payWay === 1) {
        const payWin = window.open('', '_blank')
        try {
          const token2 = localStorage.getItem('token') || ''
          const payResp = await fetch(`/api/alipay/pay?id=${encodeURIComponent(oid || ono)}`, {
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
              if (!url) url = `/api/alipay/pay?id=${encodeURIComponent(oid || ono)}`
              if (payWin) payWin.location.href = url
            }
          }
          ElMessage.success('订单已创建，前往支付')
        } catch (_) {
          if (payWin) payWin.location.href = `/api/alipay/pay?id=${encodeURIComponent(oid || ono)}`
        }
        orderDialogVisible.value = false
        loadCart()
      } else {
        ElMessage.success('钱包支付成功，订单已创建')
        orderDialogVisible.value = false
        loadCart()
        loadWalletBalance()
      }
    } else {
      ElMessage.error(data?.msg || '下单失败')
    }
  } catch (_) {
    ElMessage.error('网络错误')
  } finally {
    ordering.value = false
  }
}
onMounted(() => {
  loadCart()
  refreshCartCount()
})
</script>

<style>
.cart {
  max-width: 1200px;
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
.ops {
  display: flex;
  gap: 8px;
}
.cover {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  background: #f5f7fa;
  object-fit: cover;
}
.empty {
  background: #fff;
  border-radius: 12px;
  text-align: center;
  padding: 24px;
  color: #606266;
}
.delivery-hint {
  margin-top: 6px;
  font-size: 12px;
  color: #6b7280;
  line-height: 1.4;
}
.summary {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
  font-weight: 600;
  gap: 8px;
}
.order-success {
  display: grid;
  gap: 6px;
  padding: 8px 4px;
}
.order-success .hint {
  color: #606266;
  font-size: 12px;
}
.qty {
  display: flex;
  align-items: center;
  gap: 6px;
}
.qty-val {
  min-width: 24px;
  text-align: center;
}
.addr-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  width: 100%;
}
.addr-select {
  flex: 1;
  min-width: 200px;
}
.pay-way-group {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  width: 100%;
}
.pay-way-option {
  margin: 0 !important;
  width: 100%;
  height: auto !important;
  padding: 10px 12px !important;
  border-radius: 10px !important;
  border-color: #e5e7eb !important;
}
.pay-way-option.is-checked {
  border-color: #3b82f6 !important;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.12);
}
.pay-way-inner {
  display: flex;
  align-items: center;
  gap: 10px;
}
.pay-way-ic {
  width: 22px;
  height: 22px;
  flex: 0 0 auto;
}
.pay-way-text {
  display: flex;
  flex-direction: column;
  line-height: 1.15;
}
.pay-way-title {
  font-weight: 600;
  color: #111827;
}
.pay-way-sub {
  font-size: 12px;
  color: #6b7280;
  margin-top: 2px;
}
@media (max-width: 520px) {
  .pay-way-group {
    grid-template-columns: 1fr;
  }
}
</style>
