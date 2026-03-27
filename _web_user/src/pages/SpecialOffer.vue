<template>
  <div class="offer-page">
    <div class="head">
      <div class="title">特惠专区</div>
      <div class="sub">仅在本专区下单才会享受优惠。</div>
    </div>

    <el-table v-loading="loading" :data="offers" border style="width: 100%" empty-text="暂无特惠活动">
      <el-table-column prop="name" label="活动" min-width="220" />
      <el-table-column label="包含图书" min-width="360">
        <template #default="{ row }">
          <div class="items">
            <div v-for="it in (row.items || [])" :key="String(it.bookId)" class="it">
              <img :src="it.coverImage || defaultCover" class="cover" alt="" />
              <div class="meta">
                <div class="nm">{{ it.title }}</div>
                <div class="sm">¥{{ toMoney(it.unitPrice) }} × {{ it.quantity }}</div>
              </div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="原价/特惠" width="160">
        <template #default="{ row }">
          <div class="price">
            <div class="orig">原 ¥{{ toMoney(row.originalAmount) }}</div>
            <div class="deal">特惠 ¥{{ toMoney(row.dealAmount) }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button type="primary" @click="openOrder(row)">在专区下单</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dlg" title="特惠下单" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="地址簿" prop="addressBookId">
          <el-select v-model="form.addressBookId" placeholder="请选择地址" class="addr-select">
            <el-option v-for="a in addresses" :key="String(a.id)" :label="formatAddr(a)" :value="a.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="配送方式" prop="deliveryWay">
          <el-radio-group v-model="form.deliveryWay">
            <el-radio :label="1">配送</el-radio>
            <el-radio :label="0">自提</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="付款方式" prop="payWay">
          <el-radio-group v-model="form.payWay" class="pay-way-group">
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
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlg = false">取消</el-button>
        <el-button type="primary" :loading="ordering" @click="submit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const defaultCover = '/default-book-cover.svg'
const offers = ref([])
const loading = ref(false)
const walletBalance = ref(0)

const toMoney = (v) => (Number(v || 0)).toFixed(2)

const loadOffers = async () => {
  loading.value = true
  try {
    const token = localStorage.getItem('token') || ''
    const resp = await fetch('/user/specialOffer/list', { headers: token ? { authentication: token } : {} })
    const data = await resp.json().catch(() => ({}))
    if (resp.ok && Number(data?.code) === 1) offers.value = Array.isArray(data?.data) ? data.data : []
    else offers.value = []
  } catch (_) {
    offers.value = []
  } finally {
    loading.value = false
  }
}

const addresses = ref([])
const loadAddresses = async () => {
  try {
    const token = localStorage.getItem('token') || ''
    const resp = await fetch('/user/addressBook/list', { headers: token ? { authentication: token } : {} })
    const data = await resp.json().catch(() => ({}))
    const d = data?.data ?? {}
    const rows = Array.isArray(d) ? d : d?.records || d?.list || []
    addresses.value = rows || []
  } catch (_) {
    addresses.value = []
  }
}
const formatAddr = (r) => [r?.provinceName, r?.cityName, r?.districtName, r?.schoolPartition, r?.building, r?.houseNumber].filter(Boolean).join(' ')

const loadWalletBalance = async () => {
  try {
    const token = localStorage.getItem('token') || ''
    const resp = await fetch('/user/user/wallet/balance', {
      method: 'GET',
      headers: token ? { authentication: token } : {}
    })
    const data = await resp.json().catch(() => ({}))
    if (resp.ok) walletBalance.value = Number(data?.data ?? 0) || 0
  } catch (_) {}
}

const dlg = ref(false)
const ordering = ref(false)
const formRef = ref()
const form = ref({ offerId: null, count: 1, addressBookId: '', deliveryWay: 1, payWay: 1, remark: '' })
const rules = {
  addressBookId: [{ required: true, message: '请选择地址簿', trigger: 'change' }],
  deliveryWay: [{ required: true, message: '请选择配送方式', trigger: 'change' }],
  payWay: [{ required: true, message: '请选择付款方式', trigger: 'change' }]
}
const openOrder = async (row) => {
  await Promise.all([loadAddresses(), loadWalletBalance()])
  const def = addresses.value.find(a => Number(a.isDefault) === 1) || addresses.value[0]
  form.value = { offerId: row?.id, count: 1, addressBookId: def ? def.id : '', deliveryWay: 1, payWay: 1, remark: '' }
  dlg.value = true
}

const submit = async () => {
  if (!formRef.value) return
  try { await formRef.value.validate() } catch { return }
  ordering.value = true
  try {
    const token = localStorage.getItem('token') || ''
    const payload = { ...form.value, count: 1 }
    const resp = await fetch('/user/order/submitSpecialOffer', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...(token ? { authentication: token } : {}) },
      body: JSON.stringify(payload)
    })
    const data = await resp.json().catch(() => ({}))
    if (resp.ok && (data?.code === undefined || Number(data.code) === 1)) {
      const d = data?.data ?? data
      const o = d?.data ?? d
      const oid = o?.id ?? o?.orderId ?? ''
      const ono = o?.orderNumber ?? ''
      if (form.value.payWay === 1) {
        window.open(`/api/alipay/pay?id=${encodeURIComponent(oid || ono)}`, '_blank')
      } else {
        ElMessage.success('钱包支付成功')
      }
      dlg.value = false
      router.push('/orders')
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
  loadOffers()
})
</script>

<style>
.offer-page { max-width: 1200px; margin: 0 auto; padding: 12px; }
.head { margin-bottom: 10px; }
.title { font-weight: 800; font-size: 20px; }
.sub { color: #64748b; font-size: 13px; margin-top: 4px; }
.items { display: flex; flex-direction: column; gap: 8px; }
.it { display: flex; gap: 10px; align-items: center; }
.cover { width: 44px; height: 44px; border-radius: 8px; object-fit: cover; background: #f1f5f9; }
.nm { font-weight: 600; }
.sm { color: #64748b; font-size: 12px; margin-top: 2px; }
.price .orig { color: #94a3b8; text-decoration: line-through; font-size: 12px; }
.price .deal { color: #b45309; font-weight: 800; margin-top: 4px; }
.addr-select { width: 100%; }
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

