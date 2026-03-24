<template>
  <el-card class="categories-card">
    <div class="toolbar">
      <el-button type="primary" @click="openCreate">新增分类</el-button>
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
      <el-table-column prop="name" label="分类名称" show-overflow-tooltip />
      <el-table-column prop="sort" label="排序" width="120" />
      <el-table-column label="状态" width="140">
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
      <el-table-column prop="createTime" label="创建时间" width="200" show-overflow-tooltip />
      <el-table-column prop="updateTime" label="更新时间" width="200" show-overflow-tooltip />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <div class="op">
            <el-button type="primary" size="small" @click="openEdit(row)">编辑</el-button>
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
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑分类' : '新增分类'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="999999" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { http } from '../api/http'

const loading = ref(false)
const items = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref()
const form = ref({
  id: undefined,
  name: '',
  sort: null
})
const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
}
const statusText = (v) => (Number(v) === 1 ? '正常' : '禁用')
const statusTagType = (v) => (Number(v) === 1 ? 'success' : 'info')

const qs = () => {
  const p = new URLSearchParams()
  p.set('page', String(page.value))
  p.set('pageSize', String(pageSize.value))
  return p.toString()
}

const fetchData = async () => {
  loading.value = true
  try {
    const resp = await http(`/admin/category/page?${qs()}`, { method: 'GET', json: false })
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

const openCreate = () => {
  isEdit.value = false
  dialogVisible.value = true
  form.value = { id: undefined, name: '', sort: null }
}
const openEdit = (row) => {
  isEdit.value = true
  dialogVisible.value = true
  form.value = {
    id: row.id,
    name: row.name ?? '',
    sort: typeof row.sort === 'number' ? row.sort : (row.sort ? Number(row.sort) : null)
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
      id: isEdit.value ? form.value.id : undefined,
      name: form.value.name,
      sort: form.value.sort != null ? Number(form.value.sort) : null
    }
    const resp = await http('/admin/category', { method: isEdit.value ? 'PUT' : 'POST', body: payload })
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
const onDeleteRow = async (id) => {
  if (!id) return
  try {
    await ElMessageBox.confirm('确定删除该分类？', '提示', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch (_) {
    return
  }
  try {
    const p = new URLSearchParams()
    p.set('id', String(id))
    const resp = await http(`/admin/category?${p.toString()}`, { method: 'DELETE', json: false })
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

const onStatusChange = async (row, val) => {
  const old = row.status
  row.status = val
  try {
    const resp = await http(`/admin/category/status/${val}?id=${encodeURIComponent(row.id)}`, {
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

onMounted(fetchData)
</script>

<style>
.categories-card {
  min-height: 92vh;
}
.toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
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
