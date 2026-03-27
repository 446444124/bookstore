<template>
  <el-card class="cfg-card">
    <div class="head">
      <div class="title">特惠图书配置</div>
      <div class="sub">支持单本优惠与组合套餐优惠；用户必须在“特惠专区”下单才会享受优惠。</div>
    </div>

    <div class="toolbar">
      <el-button type="primary" @click="openCreate">新增活动</el-button>
      <el-button :loading="loading" @click="load">刷新</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" border stripe empty-text="暂无活动">
      <el-table-column prop="id" label="ID" width="78" />
      <el-table-column prop="name" label="名称" min-width="200" show-overflow-tooltip />
      <el-table-column label="类型" width="100">
        <template #default="{ row }">{{ row.offerType === 1 ? '单品' : '组合' }}</template>
      </el-table-column>
      <el-table-column label="优惠" width="140">
        <template #default="{ row }">{{ discountText(row) }}</template>
      </el-table-column>
      <el-table-column label="启用" width="150">
        <template #default="{ row }">
          <el-switch v-model="row.enabled" :active-value="1" :inactive-value="0" @change="onToggle(row, $event)" />
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="100" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <div class="op">
            <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="onDelete(row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dlg" :title="isEdit ? '编辑活动' : '新增活动'" width="720px" @closed="resetDlg">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" maxlength="80" show-word-limit />
        </el-form-item>
        <el-form-item label="活动类型" prop="offerType">
          <el-radio-group v-model="form.offerType">
            <el-radio :label="1">单品</el-radio>
            <el-radio :label="2">组合</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="优惠方式" prop="discountType">
          <el-select v-model="form.discountType" style="width: 220px">
            <el-option :value="1" label="折扣(%)" />
            <el-option :value="2" label="一口价(元)" />
            <el-option :value="3" label="立减(元)" />
          </el-select>
        </el-form-item>
        <el-form-item label="优惠值" prop="discountValue">
          <el-input-number v-model="form.discountValue" :min="0.01" :precision="2" :step="1" style="width: 220px" />
          <span class="hint" v-if="form.discountType === 1">例如 80 表示 8 折</span>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="9999" :step="10" style="width: 220px" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" />
        </el-form-item>

        <el-divider content-position="left">包含图书</el-divider>
        <div class="items">
          <div class="items-head">
            <el-button size="small" type="primary" @click="addItem">添加图书</el-button>
            <span class="hint">当前简化为输入 bookId（可从“图书管理”列表查看ID）。</span>
          </div>
          <el-table :data="form.items" border stripe empty-text="请添加图书">
            <el-table-column label="bookId" width="160">
              <template #default="{ row }">
                <el-input v-model="row.bookId" placeholder="例如 1001" />
              </template>
            </el-table-column>
            <el-table-column label="数量" width="140">
              <template #default="{ row }">
                <el-input-number v-model="row.quantity" :min="1" :max="999" :step="1" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="{ $index }">
                <el-button size="small" type="danger" @click="removeItem($index)">移除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dlg = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { http } from '../api/http'

const loading = ref(false)
const saving = ref(false)
const rows = ref([])

const dlg = ref(false)
const isEdit = ref(false)
const formRef = ref()
const form = ref({
  id: undefined,
  name: '',
  offerType: 1,
  discountType: 1,
  discountValue: 80,
  enabled: 1,
  sort: 0,
  items: [{ bookId: '', quantity: 1 }]
})
const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  offerType: [{ required: true, message: '请选择活动类型', trigger: 'change' }],
  discountType: [{ required: true, message: '请选择优惠方式', trigger: 'change' }],
  discountValue: [{ required: true, message: '请输入优惠值', trigger: 'change' }]
}

const discountText = (row) => {
  const t = Number(row?.discountType)
  const v = row?.discountValue
  if (t === 1) return `${Number(v || 0).toFixed(2)}%`
  if (t === 2) return `一口价 ¥${Number(v || 0).toFixed(2)}`
  if (t === 3) return `立减 ¥${Number(v || 0).toFixed(2)}`
  return '-'
}

const load = async () => {
  loading.value = true
  try {
    const resp = await http('/admin/specialOffer/list', { method: 'GET', json: false })
    if (resp && Number(resp.code) === 1) rows.value = Array.isArray(resp.data) ? resp.data : []
    else ElMessage.error(resp?.msg || '加载失败')
  } catch (_) {
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  isEdit.value = false
  form.value = { id: undefined, name: '', offerType: 1, discountType: 1, discountValue: 80, enabled: 1, sort: 0, items: [{ bookId: '', quantity: 1 }] }
  dlg.value = true
}
const openEdit = (row) => {
  isEdit.value = true
  form.value = {
    id: row?.id,
    name: row?.name ?? '',
    offerType: row?.offerType ?? 1,
    discountType: row?.discountType ?? 1,
    discountValue: Number(row?.discountValue ?? 0) || 0,
    enabled: row?.enabled ?? 1,
    sort: row?.sort ?? 0,
    items: Array.isArray(row?.items) && row.items.length ? row.items.map(it => ({ bookId: String(it.bookId ?? ''), quantity: Number(it.quantity ?? 1) || 1 })) : [{ bookId: '', quantity: 1 }]
  }
  dlg.value = true
}
const resetDlg = () => {
  formRef.value?.clearValidate?.()
}
const addItem = () => form.value.items.push({ bookId: '', quantity: 1 })
const removeItem = (idx) => form.value.items.splice(idx, 1)

const save = async () => {
  if (!formRef.value) return
  try { await formRef.value.validate() } catch { return }
  if (!Array.isArray(form.value.items) || form.value.items.length === 0) {
    ElMessage.error('请至少添加一本图书')
    return
  }
  const payload = {
    name: form.value.name,
    offerType: form.value.offerType,
    discountType: form.value.discountType,
    discountValue: form.value.discountValue,
    enabled: form.value.enabled,
    sort: form.value.sort,
    items: form.value.items.map(it => ({ bookId: Number(it.bookId), quantity: Number(it.quantity) || 1 }))
  }
  saving.value = true
  try {
    const resp = await http(isEdit.value ? `/admin/specialOffer/${encodeURIComponent(form.value.id)}` : '/admin/specialOffer', {
      method: isEdit.value ? 'PUT' : 'POST',
      body: payload
    })
    if (resp && Number(resp.code) === 1) {
      ElMessage.success('已保存')
      dlg.value = false
      load()
    } else ElMessage.error(resp?.msg || '保存失败')
  } catch (_) {
    ElMessage.error('网络错误')
  } finally {
    saving.value = false
  }
}

const onToggle = async (row, val) => {
  const id = row?.id
  if (!id) return
  const resp = await http(`/admin/specialOffer/${encodeURIComponent(id)}/enable?enabled=${val === 1}`, { method: 'PUT', json: false })
  if (!(resp && Number(resp.code) === 1)) {
    ElMessage.error(resp?.msg || '更新失败')
    load()
  }
}

const onDelete = async (row) => {
  const id = row?.id
  if (!id) return
  try { await ElMessageBox.confirm('确认删除该活动？', '提示', { type: 'warning' }) } catch { return }
  const resp = await http(`/admin/specialOffer/${encodeURIComponent(id)}`, { method: 'DELETE', json: false })
  if (resp && Number(resp.code) === 1) {
    ElMessage.success('已删除')
    load()
  } else ElMessage.error(resp?.msg || '删除失败')
}

onMounted(load)
</script>

<style>
.cfg-card { min-height: 92vh; }
.head { display: flex; flex-direction: column; gap: 6px; margin-bottom: 8px; }
.title { font-weight: 700; font-size: 18px; color: #0f172a; }
.sub { color: var(--admin-sub); font-size: 13px; }
.toolbar { display: flex; gap: 12px; margin-bottom: 12px; }
.op { display: flex; gap: 8px; }
.items { margin-top: 6px; }
.items-head { display: flex; gap: 10px; align-items: center; margin-bottom: 10px; flex-wrap: wrap; }
.hint { color: var(--admin-sub); font-size: 12px; }
</style>

