<template>
  <el-card class="edit-card">
    <div class="title">{{ isCreate ? '新增图书' : '修改图书' }}</div>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="edit-form">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="书名" prop="title">
            <el-input v-model="form.title" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="作者" prop="author">
            <el-input v-model="form.author" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="出版社" prop="publisher">
            <el-input v-model="form.publisher" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="ISBN" prop="isbn">
            <el-input v-model="form.isbn" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="价格" prop="price">
            <el-input v-model.number="form.price" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="库存" prop="stock">
            <el-input v-model.number="form.stock" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="分类" prop="categoryId">
            <el-select v-model="form.categoryId" placeholder="请选择分类" filterable>
              <el-option
                v-for="c in categories"
                :key="c.id"
                :label="c.name"
                :value="c.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="封面" prop="coverImage">
            <div class="cover-edit">
              <el-image :src="form.coverImage || defaultCover" class="cover-preview" fit="cover" />
              <el-upload
                class="cover-uploader"
                action="/admin/common/upload"
                :headers="uploadHeaders"
                :show-file-list="false"
                :on-success="onCoverUploadSuccess"
                :on-error="onCoverUploadError"
                :before-upload="beforeCoverUpload"
                accept="image/*"
              >
                <el-button size="small">上传封面</el-button>
              </el-upload>
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="图书描述" prop="description">
            <el-input type="textarea" :rows="4" v-model="form.description" />
          </el-form-item>
        </el-col>
      </el-row>
      <div class="ops">
        <el-button type="primary" :loading="saving" @click="onSubmit">保存</el-button>
        <el-button @click="onCancel">取消</el-button>
      </div>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { http } from '../api/http'

const route = useRoute()
const router = useRouter()
const isCreate = !route.params.id
const id = isCreate ? null : Number(route.params.id)
const formRef = ref()
const saving = ref(false)
const categories = ref([])
const form = ref({
  id: id ?? undefined,
  title: '',
  author: '',
  publisher: '',
  isbn: '',
  price: null,
  stock: null,
  categoryId: null,
  coverImage: '',
  description: '',
  status: 0
})
const defaultCover = '/default-book-cover.svg'
const uploadHeaders = { token: localStorage.getItem('token') || '' }
const rules = {
  title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  author: [{ required: true, message: '请输入作者', trigger: 'blur' }],
  isbn: [{ required: true, message: '请输入ISBN', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
}

const fetchCategories = async () => {
  try {
    const resp = await http(`/admin/category/page?page=1&pageSize=1000`, { method: 'GET', json: false })
    if (resp && resp.code === 1) {
      const data = resp.data
      let rows = []
      if (Array.isArray(data)) rows = data
      else rows = data?.records || data?.list || data?.items || data?.rows || data?.data || []
      categories.value = rows || []
    }
  } catch (_) {}
}

onMounted(async () => {
  await fetchCategories()
  if (!isCreate && id) {
    const resp = await http(`/admin/book/${id}`, { method: 'GET', json: false })
    if (resp && resp.code === 1 && resp.data) {
      const b = resp.data
      form.value = {
        id: b.id,
        title: b.title ?? '',
        author: b.author ?? '',
        publisher: b.publisher ?? '',
        isbn: b.isbn ?? '',
        price: b.price ?? null,
        stock: b.stock ?? null,
        categoryId: b.categoryId ?? null,
        coverImage: b.coverImage ?? '',
        description: b.description ?? '',
        status: b.status ?? 1
      }
    } else {
      ElMessage.error(resp?.msg || '未能加载图书信息')
    }
  }
})

const beforeCoverUpload = (file) => {
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请上传图片文件')
    return false
  }
  return true
}
const onCoverUploadSuccess = (resp) => {
  const url = resp?.data?.url || resp?.data || ''
  if (!url) {
    ElMessage.error(resp?.msg || '上传失败')
    return
  }
  form.value.coverImage = url
  ElMessage.success('上传成功')
}
const onCoverUploadError = () => {
  ElMessage.error('上传失败')
}

const onSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      let resp = null
      if (isCreate) {
        const payload = { ...form.value }
        resp = await http('/admin/book/add', { method: 'POST', body: payload })
      } else {
        const payload = { ...form.value, id }
        resp = await http('/admin/book', { method: 'PUT', body: payload })
      }
      if (resp && resp.code === 1) {
        ElMessage.success(isCreate ? '新增成功' : '修改成功')
        router.push('/admin/books')
      } else {
        ElMessage.error(resp?.msg || (isCreate ? '新增失败' : '修改失败'))
      }
    } catch (e) {
      ElMessage.error('网络错误')
    } finally {
      saving.value = false
    }
  })
}
const onCancel = () => {
  router.push('/admin/books')
}
</script>

<style>
.edit-card {
  min-height: 60vh;
}
.title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 12px;
}
.edit-form .ops {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}
.cover-edit {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.cover-preview {
  width: 96px;
  height: 96px;
  border-radius: 6px;
  background: #f5f7fa;
}
</style>
