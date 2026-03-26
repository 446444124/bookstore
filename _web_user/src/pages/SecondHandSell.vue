<template>
  <div class="sell-page">
    <div class="title">卖出二手书</div>
    <div class="desc">只能选择本店已在售的正版图书；提交后由店员评估成色并定价，上架后他人可购买。</div>
    <el-form :model="form" label-width="100px" class="form">
      <el-form-item label="选择图书" required>
        <el-select
          v-model="form.bookId"
          filterable
          remote
          clearable
          placeholder="输入书名或 ISBN 搜索"
          :remote-method="searchBooks"
          :loading="bookLoading"
          style="width: 100%; max-width: 420px"
        >
          <el-option
            v-for="b in bookOptions"
            :key="b.id"
            :label="bookOptionLabel(b)"
            :value="b.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="实物照片">
        <el-upload
          class="sh-upload"
          list-type="picture-card"
          :limit="5"
          accept="image/*"
          :http-request="uploadImage"
          :on-remove="onRemoveImage"
          :file-list="uploadFileList"
        >
          <span class="upload-plus">+</span>
        </el-upload>
        <div class="upload-hint">最多 5 张，便于店员判断成色（选填）</div>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.userNote" type="textarea" rows="3" placeholder="品相、笔记情况等（选填）" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="onSubmit">提交审核</el-button>
        <el-button @click="$router.push('/second-hand')">逛二手书</el-button>
      </el-form-item>
    </el-form>

    <div class="sub-title">我的回收记录</div>
    <el-table :data="myRows" v-loading="myLoading" border empty-text="暂无记录">
      <el-table-column prop="id" label="编号" width="80" />
      <el-table-column prop="bookTitle" label="书名" min-width="160" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">{{ row.statusText }}</template>
      </el-table-column>
      <el-table-column label="实拍图" width="200">
        <template #default="{ row }">
          <div v-if="rowPhotos(row).length" class="thumb-row">
            <el-image
              v-for="(u, i) in rowPhotos(row).slice(0, 3)"
              :key="row.id + '-' + i"
              :src="u"
              class="thumb"
              fit="cover"
              :lazy="false"
              :preview-src-list="rowPhotos(row)"
              :initial-index="i"
            />
            <span v-if="rowPhotos(row).length > 3" class="thumb-more">+{{ rowPhotos(row).length - 3 }}</span>
          </div>
          <span v-else class="cell-dash">-</span>
        </template>
      </el-table-column>
      <el-table-column label="店员评定" width="100">
        <template #default="{ row }">{{ row.conditionGradeText || '-' }}</template>
      </el-table-column>
      <el-table-column label="估价/售价" width="120">
        <template #default="{ row }">
          <span v-if="row.salePrice != null">¥ {{ toMoney(row.salePrice) }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" type="danger" link size="small" @click="onWithdraw(row)">撤回</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { normalizeConditionImageUrls } from '../utils/secondHandImages.js'

const form = ref({ bookId: undefined, userNote: '', userConditionImages: [] })
const uploadFileList = ref([])
const bookOptions = ref([])

const bookOptionLabel = (b) => {
  const base = `${b.title} — ${b.author || ''}`
  if (b.isbn) return `${base} · ${b.isbn}`
  return base
}
const bookLoading = ref(false)
const saving = ref(false)
const myRows = ref([])
const myLoading = ref(false)

const toMoney = (v) => (Number(v || 0)).toFixed(2)

const rowPhotos = (row) =>
  normalizeConditionImageUrls(row?.userConditionImages ?? row?.user_condition_images)

let searchTimer = null
const searchBooks = (q) => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(async () => {
    bookLoading.value = true
    try {
      const qs = new URLSearchParams({ page: '1', pageSize: '20', status: '1' })
      if (q && String(q).trim()) qs.set('title', String(q).trim())
      const resp = await fetch(`/user/book/page?${qs}`)
      const text = await resp.text()
      let data = {}
      try {
        if (text) data = JSON.parse(text)
      } catch {
        bookOptions.value = []
        return
      }
      if (resp.ok && Number(data?.code) === 1 && data?.data) {
        const d = data.data
        const rows = Array.isArray(d?.records) ? d.records : []
        bookOptions.value = rows.map(r => ({
          id: r.id,
          title: r.title,
          author: r.author || '',
          isbn: r.isbn || ''
        }))
      } else {
        bookOptions.value = []
      }
    } catch {
      bookOptions.value = []
    } finally {
      bookLoading.value = false
    }
  }, 280)
}

const uploadImage = async (opts) => {
  const token = localStorage.getItem('token') || ''
  if (!token) {
    ElMessage.warning('请先登录')
    opts.onError(new Error('no token'))
    return
  }
  const fd = new FormData()
  fd.append('file', opts.file)
  try {
    const resp = await fetch('/user/common/upload', {
      method: 'POST',
      headers: { authentication: token },
      body: fd
    })
    const text = await resp.text()
    let data = {}
    try {
      if (text) data = JSON.parse(text)
    } catch {
      ElMessage.error('上传响应异常')
      opts.onError(new Error('parse'))
      return
    }
    if (!resp.ok || Number(data.code) !== 1 || data.data == null || data.data === '') {
      ElMessage.error(data?.msg || '上传失败')
      opts.onError(new Error('fail'))
      return
    }
    const url = typeof data.data === 'string' ? data.data : String(data.data || '')
    if (!url.trim()) {
      ElMessage.error('上传返回地址无效')
      opts.onError(new Error('empty url'))
      return
    }
    form.value.userConditionImages.push(url)
    uploadFileList.value = [
      ...uploadFileList.value,
      {
        uid: `${Date.now()}-${Math.random().toString(36).slice(2, 11)}`,
        name: opts.file.name,
        url,
        status: 'success'
      }
    ]
    opts.onSuccess({ url })
  } catch (_) {
    ElMessage.error('上传失败')
    opts.onError(new Error('network'))
  }
}

const onRemoveImage = (file) => {
  const url = file.url
  form.value.userConditionImages = (form.value.userConditionImages || []).filter((u) => u !== url)
  uploadFileList.value = uploadFileList.value.filter((f) => f.url !== url)
}

const loadMy = async () => {
  const token = localStorage.getItem('token') || ''
  if (!token) return
  myLoading.value = true
  try {
    const resp = await fetch('/user/secondHand/my?page=1&pageSize=50', { headers: { authentication: token } })
    const text = await resp.text()
    let data = {}
    try {
      if (text) data = JSON.parse(text)
    } catch {
      ElMessage.error('无法解析回收记录，请检查数据库是否已执行二手书脚本')
      myRows.value = []
      return
    }
    if (!resp.ok || (data.code !== undefined && Number(data.code) !== 1)) {
      ElMessage.error(data?.msg || '加载我的回收记录失败')
      myRows.value = []
      return
    }
    const d = data?.data
    myRows.value = d && Array.isArray(d.records) ? d.records : []
  } catch (_) {
    myRows.value = []
  } finally {
    myLoading.value = false
  }
}

const onSubmit = async () => {
  if (!form.value.bookId) {
    ElMessage.warning('请选择图书')
    return
  }
  const token = localStorage.getItem('token') || ''
  if (!token) {
    ElMessage.warning('请先登录')
    return
  }
  saving.value = true
  try {
    const resp = await fetch('/user/secondHand/submit', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', authentication: token },
      body: JSON.stringify({
        bookId: form.value.bookId,
        userNote: form.value.userNote || '',
        userConditionImages: form.value.userConditionImages || []
      })
    })
    const text = await resp.text()
    let data = {}
    try {
      if (text) data = JSON.parse(text)
    } catch {
      ElMessage.error('提交响应异常')
      return
    }
    if (resp.ok && Number(data?.code) === 1) {
      ElMessage.success('已提交，请等待店员审核')
      form.value.userNote = ''
      form.value.userConditionImages = []
      uploadFileList.value = []
      loadMy()
    } else {
      ElMessage.error(data?.msg || '提交失败')
    }
  } catch (_) {
    ElMessage.error('网络错误')
  } finally {
    saving.value = false
  }
}

const onWithdraw = async (row) => {
  if (!row?.id) return
  try {
    await ElMessageBox.confirm('确定撤回该条申请？', '提示', { type: 'warning' })
  } catch {
    return
  }
  const token = localStorage.getItem('token') || ''
  const resp = await fetch(`/user/secondHand/withdraw/${row.id}`, {
    method: 'PUT',
    headers: { authentication: token }
  })
  const text = await resp.text()
  let data = {}
  try {
    if (text) data = JSON.parse(text)
  } catch {
    ElMessage.error('操作响应异常')
    return
  }
  if (resp.ok && Number(data?.code) === 1) {
    ElMessage.success('已撤回')
    loadMy()
  } else {
    ElMessage.error(data?.msg || '操作失败')
  }
}

onMounted(() => {
  searchBooks('')
  loadMy()
})
</script>

<style scoped>
.sell-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 16px;
}
.title {
  font-weight: 600;
  font-size: 20px;
  margin-bottom: 8px;
}
.desc {
  color: #64748b;
  margin-bottom: 20px;
  font-size: 14px;
}
.form {
  margin-bottom: 32px;
}
.sub-title {
  font-weight: 600;
  margin-bottom: 12px;
}
.upload-hint {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 8px;
}
.upload-plus {
  font-size: 28px;
  color: #94a3b8;
  line-height: 1;
}
.sh-upload :deep(.el-upload--picture-card) {
  width: 96px;
  height: 96px;
}
.thumb-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
}
.thumb {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  overflow: hidden;
  flex-shrink: 0;
}
.thumb-more {
  font-size: 12px;
  color: #909399;
}
.cell-dash {
  color: #c0c4cc;
}
</style>
