<template>
  <el-card class="cfg-card">
    <div class="head">
      <div class="title">轮播图配置</div>
      <div class="sub">配置用户端首页轮播图；可选填写点击跳转路径（留空则仅展示图片）。</div>
    </div>

    <div class="toolbar">
      <el-button type="primary" @click="openCreate">新增轮播图</el-button>
      <el-button :loading="loading" @click="load">刷新</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" border stripe class="table" empty-text="暂无轮播图">
      <el-table-column prop="id" label="ID" width="78" />
      <el-table-column label="预览" width="180">
        <template #default="{ row }">
          <el-image :src="row.imageUrl" fit="cover" class="pv" :preview-src-list="[row.imageUrl]" />
        </template>
      </el-table-column>
      <el-table-column label="图片地址" min-width="260" show-overflow-tooltip>
        <template #default="{ row }">
          <div class="url-cell">
            <span class="url-text">{{ row.imageUrl }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="跳转路径" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">
          <span>{{ row.linkPath || '—' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="启用" width="150">
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

    <el-dialog v-model="dlg" :title="isEdit ? '编辑轮播图' : '新增轮播图'" width="560px" @closed="resetDlg">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="轮播图片" prop="imageUrl">
          <div class="img-edit">
            <el-image :src="form.imageUrl || placeholderImg" fit="cover" class="pv-lg" />
            <el-upload
              class="uploader"
              action="/admin/common/upload"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="onUploadSuccess"
              :on-error="onUploadError"
              :before-upload="beforeUpload"
              accept="image/*"
            >
              <el-button size="small">上传图片</el-button>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="跳转路径">
          <el-input v-model="form.linkPath" maxlength="255" show-word-limit placeholder="可选：例如 /second-hand 或 https://..." />
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
const uploadHeaders = { token: localStorage.getItem('token') || '' }
const placeholderImg =
  'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="360" height="180" viewBox="0 0 360 180"><rect width="360" height="180" fill="%23f1f5f9"/><path d="M130 120l26-30 24 28 22-20 28 32H130z" fill="%2394a3b8"/><circle cx="150" cy="70" r="10" fill="%2394a3b8"/><text x="180" y="98" text-anchor="middle" font-family="Arial" font-size="12" fill="%23647569">上传轮播图</text></svg>'

const dlg = ref(false)
const isEdit = ref(false)
const formRef = ref()
const form = ref({
  id: undefined,
  imageUrl: '',
  linkPath: '',
  enabled: 1,
  sort: 0
})
const rules = {
  imageUrl: [{ required: true, message: '请上传轮播图片', trigger: 'change' }]
}

const beforeUpload = (file) => {
  if (!file?.type?.startsWith('image/')) {
    ElMessage.error('请上传图片文件')
    return false
  }
  return true
}
const onUploadSuccess = (resp) => {
  const url = resp?.data?.url || resp?.data || ''
  if (!url) {
    ElMessage.error(resp?.msg || '上传失败')
    return
  }
  form.value.imageUrl = url
  ElMessage.success('上传成功')
  formRef.value?.validateField?.('imageUrl')
}
const onUploadError = () => {
  ElMessage.error('上传失败')
}

const load = async () => {
  loading.value = true
  try {
    const resp = await http('/admin/carouselBanner/list', { method: 'GET', json: false })
    if (resp && Number(resp.code) === 1) {
      rows.value = Array.isArray(resp.data) ? resp.data : []
    } else {
      ElMessage.error(resp?.msg || '加载失败')
    }
  } catch (_) {
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  isEdit.value = false
  form.value = { id: undefined, imageUrl: '', linkPath: '', enabled: 1, sort: 0 }
  dlg.value = true
}

const openEdit = (row) => {
  isEdit.value = true
  form.value = {
    id: row?.id,
    imageUrl: row?.imageUrl ?? '',
    linkPath: row?.linkPath ?? '',
    enabled: row?.enabled ?? 1,
    sort: row?.sort ?? 0
  }
  dlg.value = true
}

const resetDlg = () => {
  if (formRef.value) formRef.value.clearValidate?.()
}

const save = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch (_) {
    return
  }
  saving.value = true
  try {
    const payload = {
      imageUrl: form.value.imageUrl,
      linkPath: form.value.linkPath,
      enabled: form.value.enabled,
      sort: form.value.sort
    }
    const resp = await http(
      isEdit.value ? `/admin/carouselBanner/${encodeURIComponent(form.value.id)}` : '/admin/carouselBanner',
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
    saving.value = false
  }
}

const onToggle = async (row, val) => {
  const id = row?.id
  if (!id) return
  try {
    const resp = await http(`/admin/carouselBanner/${encodeURIComponent(id)}/enable?enabled=${val === 1}`, {
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
    await ElMessageBox.confirm('确认删除该轮播图？', '提示', { type: 'warning' })
  } catch (_) {
    return
  }
  try {
    const resp = await http(`/admin/carouselBanner/${encodeURIComponent(id)}`, { method: 'DELETE', json: false })
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
.toolbar { display: flex; gap: 12px; margin-bottom: 12px; }
.op { display: flex; gap: 8px; }
.img-edit {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.pv {
  width: 150px;
  height: 84px;
  border-radius: 10px;
  overflow: hidden;
  background: #f1f5f9;
}
.pv-lg {
  width: 360px;
  height: 180px;
  border-radius: 12px;
  overflow: hidden;
  background: #f1f5f9;
}
.pv-empty {
  color: var(--admin-sub);
  font-size: 12px;
}
.url-cell {
  display: flex;
  gap: 8px;
  align-items: center;
}
.url-text {
  color: #334155;
}
</style>

