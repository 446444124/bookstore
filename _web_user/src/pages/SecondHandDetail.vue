<template>
  <div class="detail-page" v-loading="loading">
    <el-button class="back" link type="primary" @click="$router.push('/second-hand')">← 返回列表</el-button>
    <div v-if="row" class="wrap">
      <img :src="row.coverImage || defaultCover" class="cover" alt="" />
      <div class="info">
        <div class="badge">二手 · 个人回收</div>
        <h2>{{ row.bookTitle }}</h2>
        <div class="author">{{ row.bookAuthor }}</div>
        <div class="price-block">
          <span class="sale">¥ {{ toMoney(row.salePrice) }}</span>
          <span v-if="row.bookOriginalPrice" class="orig">原价 ¥{{ toMoney(row.bookOriginalPrice) }}</span>
          <span v-if="row.priceRatio != null" class="ratio">约为原价 {{ row.priceRatio }}%</span>
        </div>
        <div class="meta">
          <span>成色：{{ row.conditionGradeText }}</span>
        </div>
        <div v-if="conditionPhotos.length" class="user-photos">
          <div class="photos-title">卖家提供的实物参考</div>
          <div class="photos-grid">
            <el-image
              v-for="(u, i) in conditionPhotos"
              :key="u + i"
              :src="u"
              fit="cover"
              :lazy="false"
              :preview-src-list="conditionPhotos"
              :initial-index="i"
              class="ph"
            />
          </div>
        </div>
        <el-button type="primary" size="large" class="buy" @click="openOrder">立即购买</el-button>
      </div>
    </div>

    <el-dialog v-model="orderDialogVisible" title="购买二手书" width="560px">
      <el-form ref="orderFormRef" :model="orderForm" :rules="orderRules" label-width="120px">
        <el-form-item label="地址簿" prop="addressBookId">
          <div class="addr-row">
            <el-select v-model="orderForm.addressBookId" placeholder="请选择地址" class="addr-select">
              <el-option v-for="a in addresses" :key="String(a.id)" :label="formatAddr(a)" :value="a.id" />
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
        </el-form-item>
        <el-form-item label="预计送达时间" prop="estimatedDeliveryTime" v-if="orderForm.deliveryWay === 1 && orderForm.deliveryStatus === 0">
          <el-date-picker v-model="orderForm.estimatedDeliveryTime" type="datetime" placeholder="选择日期时间" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="付款方式" prop="payWay">
          <el-select v-model="orderForm.payWay" placeholder="请选择付款方式">
            <el-option :value="1" label="支付宝" />
            <el-option :value="2" :label="`钱包支付（余额 ¥${toMoney(walletBalance)}）`" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="orderForm.remark" type="textarea" />
        </el-form-item>
        <el-form-item label="金额">
          <el-input :model-value="toMoney(row?.salePrice)" disabled />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="orderDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="ordering" @click="onGoPay">去支付</el-button>
      </template>
    </el-dialog>
    <AddressBookDialog v-model="addrDialogVisible" @saved="onAddressSaved" />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import AddressBookDialog from '../components/AddressBookDialog.vue'
import { normalizeConditionImageUrls } from '../utils/secondHandImages.js'

const route = useRoute()
const defaultCover = '/default-book-cover.svg'
const row = ref(null)
const loading = ref(false)
const conditionPhotos = computed(() =>
  normalizeConditionImageUrls(row.value?.userConditionImages ?? row.value?.user_condition_images)
)
const orderDialogVisible = ref(false)
const orderFormRef = ref()
const ordering = ref(false)
const addrDialogVisible = ref(false)
const addressIdsBeforeAdd = ref(new Set())
const addresses = ref([])
const walletBalance = ref(0)

const orderForm = ref({
  addressBookId: '',
  payWay: 1,
  remark: '',
  estimatedDeliveryTime: '',
  deliveryStatus: 1,
  deliveryWay: 1
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

const toMoney = (v) => (Number(v || 0)).toFixed(2)

const formatAddr = (r) => {
  const parts = [r?.provinceName, r?.cityName, r?.districtName, r?.schoolPartition, r?.building, r?.houseNumber].filter(Boolean)
  return parts.join(' ')
}

const loadDetail = async () => {
  const id = route.params.id
  if (!id) return
  loading.value = true
  try {
    const resp = await fetch(`/user/secondHand/detail/${encodeURIComponent(id)}`)
    const text = await resp.text()
    let data = {}
    try {
      if (text) data = JSON.parse(text)
    } catch {
      ElMessage.error('详情加载异常，请确认后端与数据库脚本已就绪')
      row.value = null
      return
    }
    if (!resp.ok) {
      ElMessage.error(data?.msg || data?.message || `加载失败（HTTP ${resp.status}）`)
      row.value = null
      return
    }
    if (data?.code !== undefined && data?.code !== null && Number(data.code) !== 1) {
      ElMessage.error(data?.msg || '加载失败')
      row.value = null
      return
    }
    const detail = data?.data
    if (!detail) {
      ElMessage.error(data?.msg || '加载失败')
      row.value = null
      return
    }
    row.value = detail
  } catch (_) {
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}

const loadAddresses = async () => {
  const token = localStorage.getItem('token') || ''
  const resp = await fetch('/user/addressBook/list', { headers: token ? { authentication: token } : {} })
  const data = await resp.json().catch(() => ({}))
  if (resp.ok) {
    const d = data?.data ?? data
    const rows = Array.isArray(d) ? d : d?.records || []
    addresses.value = rows.map(r => ({ ...r }))
  }
}

const loadWallet = async () => {
  const token = localStorage.getItem('token') || ''
  const resp = await fetch('/user/user/wallet/balance', { headers: token ? { authentication: token } : {} })
  const data = await resp.json().catch(() => ({}))
  if (resp.ok) walletBalance.value = Number(data?.data ?? 0) || 0
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
  const token = localStorage.getItem('token') || ''
  if (!token) {
    ElMessage.warning('请先登录后再购买')
    return
  }
  await Promise.all([loadAddresses(), loadWallet()])
  const def = addresses.value.find(a => Number(a.isDefault) === 1) || addresses.value[0]
  orderForm.value.addressBookId = def ? def.id : ''
  orderForm.value.deliveryWay = 1
  orderForm.value.deliveryStatus = 1
  orderForm.value.payWay = 1
  orderForm.value.remark = ''
  orderForm.value.estimatedDeliveryTime = ''
  orderDialogVisible.value = true
}

const onGoPay = async () => {
  if (!orderFormRef.value) return
  try {
    await orderFormRef.value.validate()
  } catch (_) {
    return
  }
  const listingId = Number(route.params.id)
  if (!listingId) return
  ordering.value = true
  try {
    const token = localStorage.getItem('token') || ''
    const payload = {
      listingId,
      addressBookId: orderForm.value.addressBookId,
      payWay: orderForm.value.payWay,
      remark: orderForm.value.remark,
      estimatedDeliveryTime:
        (orderForm.value.deliveryWay === 1 && orderForm.value.deliveryStatus === 0)
          ? orderForm.value.estimatedDeliveryTime
          : '',
      deliveryStatus: orderForm.value.deliveryStatus,
      deliveryWay: orderForm.value.deliveryWay
    }
    const resp = await fetch('/user/order/submitSecondHand', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', authentication: token },
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
      const ono = o?.orderNumber ?? ''
      if (!oid && !ono) {
        ElMessage.error('下单失败')
        return
      }
      if (orderForm.value.payWay === 1) {
        const payWin = window.open('', '_blank')
        const payResp = await fetch(`/api/alipay/pay?id=${encodeURIComponent(oid || ono)}`, {
          headers: token ? { authentication: token } : {}
        })
        const text = await payResp.text()
        const ct2 = payResp.headers.get('content-type') || ''
        const looksHtml = ct2.includes('text/html') || text.trim().startsWith('<')
        if (looksHtml && payWin) {
          payWin.document.open()
          payWin.document.write(text)
          payWin.document.close()
        } else if (payWin) payWin.location.href = `/api/alipay/pay?id=${encodeURIComponent(oid || ono)}`
        ElMessage.success('订单已创建，前往支付')
      } else {
        ElMessage.success('钱包支付成功')
      }
      orderDialogVisible.value = false
      loadDetail()
    } else {
      ElMessage.error(data?.msg || '下单失败')
    }
  } catch (_) {
    ElMessage.error('网络错误')
  } finally {
    ordering.value = false
  }
}

onMounted(loadDetail)
</script>

<style scoped>
.detail-page {
  max-width: 960px;
  margin: 0 auto;
  padding: 16px;
}
.back {
  margin-bottom: 12px;
}
.wrap {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 32px;
  align-items: start;
}
@media (max-width: 720px) {
  .wrap {
    grid-template-columns: 1fr;
  }
}
.cover {
  width: 100%;
  aspect-ratio: 3 / 4;
  max-height: 420px;
  object-fit: contain;
  display: block;
  border-radius: 12px;
  background: #f1f5f9;
}
.badge {
  display: inline-block;
  padding: 4px 10px;
  background: #fef3c7;
  color: #b45309;
  border-radius: 999px;
  font-size: 12px;
  margin-bottom: 8px;
}
.author {
  color: #64748b;
  margin-bottom: 16px;
}
.price-block {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 12px;
  margin: 16px 0;
}
.sale {
  font-size: 28px;
  font-weight: 700;
  color: #b45309;
}
.orig {
  text-decoration: line-through;
  color: #94a3b8;
}
.ratio {
  font-size: 13px;
  color: #64748b;
}
.meta {
  margin-bottom: 20px;
  color: #475569;
}
.buy {
  min-width: 160px;
}
.user-photos {
  margin: 16px 0 20px;
}
.photos-title {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 8px;
}
.photos-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.photos-grid .ph {
  width: 88px;
  height: 88px;
  border-radius: 8px;
  overflow: hidden;
  background: #f1f5f9;
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
</style>
