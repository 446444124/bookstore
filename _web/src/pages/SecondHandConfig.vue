<template>
  <el-card class="cfg-card">
    <div class="head">
      <div class="title">二手书回收配置</div>
      <div class="sub">回收价按店内原价 * 档位回收百分比；售价在回收价基础上加服务费。</div>
    </div>

    <el-divider content-position="left">服务费</el-divider>
    <div class="fee-row">
      <el-form :inline="true" class="fee-form">
        <el-form-item label="服务费百分比">
          <el-input-number v-model="serviceFeePercent" :min="0" :max="100" :precision="2" :step="0.5" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="savingFee" @click="saveFee">保存</el-button>
          <el-button :loading="loading" @click="load">刷新</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-divider content-position="left">成色档位</el-divider>
    <div class="toolbar">
      <el-button type="primary" @click="openCreate">新增档位</el-button>
    </div>

    <el-table v-loading="loading" :data="grades" border stripe class="table" empty-text="暂无档位">
      <el-table-column prop="id" label="ID" width="72" />
      <el-table-column prop="name" label="名称" min-width="160" show-overflow-tooltip />
      <el-table-column label="回收百分比" width="140">
        <template #default="{ row }">{{ toPercent(row.recyclePercent) }}</template>
      </el-table-column>
      <el-table-column label="启用" width="140">
        <template #default="{ row }">
          <el-switch
            v-model="row.enabled"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="停用"
            @change="onToggle(row, $event)"
          />
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

    <el-dialog v-model="dlg" :title="isEdit ? '编辑档位' : '新增档位'" width="520px" @closed="resetDlg">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" maxlength="64" show-word-limit placeholder="例如：近新 / 良好 / 破损" />
        </el-form-item>
        <el-form-item label="回收百分比" prop="recyclePercent">
          <el-input-number v-model="form.recyclePercent" :min="0" :max="100" :precision="2" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="9999" :step="10" style="width: 100%" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlg = false">取消</el-button>
        <el-button type="primary" :loading="savingGrade" @click="saveGrade">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { http } from '../api/http'

const loading = ref(false)
const savingFee = ref(false)
const savingGrade = ref(false)

const serviceFeePercent = ref(0)
const grades = ref([])

const dlg = ref(false)
const isEdit = ref(false)
const formRef = ref()
const form = ref({
  id: undefined,
  name: '',
  recyclePercent: 50,
  enabled: 1,
  sort: 0
})
const rules = {
  name: [{ required: true, message: '请输入档位名称', trigger: 'blur' }],
  recyclePercent: [{ required: true, message: '请输入回收百分比', trigger: 'change' }]
}

const toPercent = (v) => {
  const n = Number(v ?? 0)
  if (!Number.isFinite(n)) return '-'
  return `${n.toFixed(2)}%`
}

const load = async () => {
  loading.value = true
  try {
    const resp = await http('/admin/secondHand/config', { method: 'GET', json: false })
    if (resp && Number(resp.code) === 1) {
      serviceFeePercent.value = Number(resp.data?.serviceFeePercent ?? 0) || 0
      grades.value = Array.isArray(resp.data?.grades) ? resp.data.grades : []
    } else {
      ElMessage.error(resp?.msg || '加载失败')
    }
  } catch (_) {
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}

const saveFee = async () => {
  savingFee.value = true
  try {
    const resp = await http('/admin/secondHand/config/serviceFee', {
      method: 'PUT',
      body: { serviceFeePercent: serviceFeePercent.value }
    })
    if (resp && Number(resp.code) === 1) {
      ElMessage.success('已保存')
      load()
    } else ElMessage.error(resp?.msg || '保存失败')
  } catch (_) {
    ElMessage.error('网络错误')
  } finally {
    savingFee.value = false
  }
}

const openCreate = () => {
  isEdit.value = false
  form.value = { id: undefined, name: '', recyclePercent: 50, enabled: 1, sort: 0 }
  dlg.value = true
}

const openEdit = (row) => {
  isEdit.value = true
  form.value = {
    id: row.id,
    name: row.name ?? '',
    recyclePercent: Number(row.recyclePercent ?? 0) || 0,
    enabled: row.enabled ?? 1,
    sort: row.sort ?? 0
  }
  dlg.value = true
}

const resetDlg = () => {
  if (formRef.value) formRef.value.clearValidate?.()
}

const saveGrade = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch (_) {
    return
  }
  savingGrade.value = true
  try {
    const payload = {
      name: form.value.name,
      recyclePercent: form.value.recyclePercent,
      enabled: form.value.enabled,
      sort: form.value.sort
    }
    const resp = await http(
      isEdit.value ? `/admin/secondHand/config/grades/${encodeURIComponent(form.value.id)}` : '/admin/secondHand/config/grades',
      { method: isEdit.value ? 'PUT' : 'POST', body: payload }
    )
    if (resp && Number(resp.code) === 1) {
      ElMessage.success('已保存')
      dlg.value = false
      load()
    } else ElMessage.error(resp?.msg || '保存失败')
  } catch (_) {
    ElMessage.error('网络错误')
  } finally {
    savingGrade.value = false
  }
}

const onToggle = async (row, val) => {
  const id = row?.id
  if (!id) return
  try {
    const resp = await http(`/admin/secondHand/config/grades/${encodeURIComponent(id)}/enable?enabled=${val === 1}`, {
      method: 'PUT',
      json: false
    })
    if (!(resp && Number(resp.code) === 1)) {
      ElMessage.error(resp?.msg || '更新失败')
      load()
    }
  } catch (_) {
    ElMessage.error('网络错误')
    load()
  }
}

const onDelete = async (row) => {
  const id = row?.id
  if (!id) return
  try {
    await ElMessageBox.confirm(`确认删除档位「${row?.name || id}」？`, '提示', { type: 'warning' })
  } catch (_) {
    return
  }
  try {
    const resp = await http(`/admin/secondHand/config/grades/${encodeURIComponent(id)}`, { method: 'DELETE', json: false })
    if (resp && Number(resp.code) === 1) {
      ElMessage.success('已删除')
      load()
    } else ElMessage.error(resp?.msg || '删除失败')
  } catch (_) {
    ElMessage.error('网络错误')
  }
}

onMounted(load)
</script>

<style>
.cfg-card { min-height: 92vh; }
.head { display: flex; flex-direction: column; gap: 6px; margin-bottom: 8px; }
.title { font-weight: 700; font-size: 18px; color: #0f172a; }
.sub { color: var(--admin-sub); font-size: 13px; }
.fee-row { margin-bottom: 8px; }
.fee-form { display: flex; flex-wrap: wrap; gap: 12px; align-items: center; }
.toolbar { display: flex; gap: 12px; margin-bottom: 12px; }
.op { display: flex; gap: 8px; }
</style>

