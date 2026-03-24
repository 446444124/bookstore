<template>
  <el-card class="employees-card">
    <div class="toolbar">
      <el-input v-model="qRealName" placeholder="姓名" clearable @keyup.enter.native="onSearch" @clear="onSearch" class="keyword" />
      <el-input v-model="qPhone" placeholder="手机号" clearable @keyup.enter.native="onSearch" @clear="onSearch" class="keyword" />
      <el-select v-model="qStatus" placeholder="状态" clearable class="status">
        <el-option label="正常" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-input v-model="qPosition" placeholder="岗位" clearable @keyup.enter.native="onSearch" @clear="onSearch" class="keyword" />
      <el-button type="primary" @click="onSearch">搜索</el-button>
      <el-button type="success" @click="openCreate">新增员工</el-button>
    </div>
    <el-table
      v-loading="loading"
      :data="items"
      border
      stripe
      class="table"
      height="780"
      empty-text="暂无数据"
    >
      <el-table-column label="头像" width="100">
        <template #default="{ row }">
          <el-image :src="getAvatar(row)" fit="cover" class="emp-avatar" />
        </template>
      </el-table-column>
      <el-table-column prop="empNo" label="工号" width="140" />
      <el-table-column prop="realName" label="姓名" width="160" />
      <el-table-column prop="position" label="岗位" width="160" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="phone" label="手机" width="160" />
      <el-table-column label="状态" width="180">
        <template #default="{ row }">
          <div class="state-cell">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              active-text="正常"
              inactive-text="禁用"
              @change="onStatusChange(row, $event)"
            />
            <el-tag size="small" :type="statusTagType(row.status)" effect="light">{{ statusText(row.status) }}</el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" width="200" show-overflow-tooltip />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <div class="op">
            <el-button type="primary" size="small" @click="openEdit(row)">编辑</el-button>
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
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑员工' : '新增员工'" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="工号" prop="empNo">
          <el-input v-model="form.empNo" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="岗位">
          <el-input v-model="form.position" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="手机">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="onSubmit">保存</el-button>
          <el-button @click="dialogVisible=false">取消</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { http } from '../api/http'

const loading = ref(false)
const items = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const saving = ref(false)
const isEdit = ref(false)
const formRef = ref()
const qRealName = ref('')
const qPhone = ref('')
const qStatus = ref()
const qPosition = ref('')
const defaultAvatar = 'data:image/svg+xml;utf8,<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"32\" height=\"32\" viewBox=\"0 0 36 36\"><defs><style>.c{fill:%23e5e7eb}.d{fill:%239ca3af}</style></defs><circle cx=\"18\" cy=\"18\" r=\"18\" class=\"c\"/><circle cx=\"18\" cy=\"13\" r=\"6\" class=\"d\"/><path d=\"M6 30c2.8-6 9.2-7 12-7s9.2 1 12 7\" class=\"d\"/></svg>'
const statusText = (v) => (Number(v) === 1 ? '正常' : '禁用')
const statusTagType = (v) => (Number(v) === 1 ? 'success' : 'info')
const form = ref({
  employeeId: undefined,
  empNo: '',
  realName: '',
  position: '',
  email: '',
  phone: ''
})
const rules = {
  empNo: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

const qs = () => {
  const p = new URLSearchParams()
  p.set('page', String(page.value))
  p.set('pageSize', String(pageSize.value))
  if (qRealName.value) p.set('realName', qRealName.value)
  if (qPhone.value) p.set('phone', qPhone.value)
  if (qStatus.value !== undefined && qStatus.value !== null && qStatus.value !== '') p.set('status', String(qStatus.value))
  if (qPosition.value) p.set('position', qPosition.value)
  return p.toString()
}

const fetchData = async () => {
  loading.value = true
  try {
    const resp = await http(`/admin/admin/page?${qs()}`, { method: 'GET', json: false })
    if (resp && resp.code === 1) {
      const data = resp.data
      let rows = []
      if (Array.isArray(data)) rows = data
      else rows = data?.records || data?.list || data?.items || data?.rows || data?.data || []
      items.value = rows || []
      total.value = data?.total ?? data?.count ?? data?.totalCount ?? items.value.length
    } else {
      ElMessage.error(resp?.msg || '加载失败')
    }
  } catch (_) {
    ElMessage.error('请求失败')
  } finally {
    loading.value = false
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

onMounted(fetchData)

const getRowId = (row) => {
  if (!row || typeof row !== 'object') return undefined
  return row.employeeId ?? row.id ?? row.adminId ?? row.userId ?? row.uid ?? row.pkId
}
const getAvatar = (row) => {
  if (!row || typeof row !== 'object') return defaultAvatar
  return row.avatar || row.avatarUrl || defaultAvatar
}

const openCreate = () => {
  isEdit.value = false
  dialogVisible.value = true
  form.value = {
    employeeId: undefined,
    empNo: '',
    realName: '',
    position: '',
    email: '',
    phone: ''
  }
}
const openEdit = (row) => {
  isEdit.value = true
  dialogVisible.value = true
  form.value = {
    employeeId: getRowId(row),
    empNo: row.empNo ?? '',
    realName: row.realName ?? '',
    position: row.position ?? '',
    email: row.email ?? '',
    phone: row.phone ?? ''
  }
}
const onSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch (_) {
    return
  }
  saving.value = true
  try {
    const payload = {
      empNo: form.value.empNo,
      realName: form.value.realName,
      position: form.value.position,
      email: form.value.email,
      phone: form.value.phone
    }
    if (isEdit.value) payload.employeeId = form.value.employeeId
    const resp = await http('/admin/admin', { method: isEdit.value ? 'PUT' : 'POST', body: payload })
    if (resp && resp.code === 1) {
      ElMessage.success('保存成功')
      dialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(resp?.msg || '保存失败')
    }
  } catch (_) {
    ElMessage.error('网络错误')
  } finally {
    saving.value = false
  }
}
const onStatusChange = async (row, val) => {
  const old = row.status
  row.status = val
  const id = getRowId(row)
  if (id == null || id === '') {
    row.status = old
    ElMessage.error('缺少主键employeeId，无法更新状态')
    return
  }
  try {
    const resp = await http(`/admin/admin/status/${val}?id=${encodeURIComponent(id)}`, {
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
</script>

<style>
.employees-card {
  min-height: 92vh;
}
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.keyword {
  width: 260px;
}
.status {
  width: 180px;
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
.emp-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #f5f7fa;
  display: block;
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
