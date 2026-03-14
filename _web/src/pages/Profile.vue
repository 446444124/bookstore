<template>
  <el-card class="profile-card">
    <div class="header">
      <img class="avatar" :src="avatarSrc" @error="onAvatarError" alt="管理员头像" />
      <div class="info">
        <div class="line">姓名：{{ displayName }}</div>
        <div class="line">工号：{{ displayEmpNo }}</div>
      </div>
    </div>
    <el-descriptions title="个人信息" :column="2" size="large" border class="desc">
      <el-descriptions-item label="工号">{{ user?.empNo || '—' }}</el-descriptions-item>
      <el-descriptions-item label="姓名">{{ user?.realName || user?.name || '—' }}</el-descriptions-item>
      <el-descriptions-item label="岗位">{{ user?.position || '—' }}</el-descriptions-item>
      <el-descriptions-item label="邮箱">{{ user?.email || '—' }}</el-descriptions-item>
      <el-descriptions-item label="手机">{{ user?.phone || '—' }}</el-descriptions-item>
      <el-descriptions-item label="状态">{{ displayStatus }}</el-descriptions-item>
    </el-descriptions>
    <div class="edit-panel">
      <template v-if="!editing">
        <el-button type="primary" size="small" @click="onEdit">编辑个人信息</el-button>
      </template>
      <template v-else>
        <span class="label">头像</span>
        <div class="avatar-edit">
          <el-image :src="avatarSrc" class="avatar-preview" fit="cover" />
          <el-upload
            class="avatar-uploader"
            action="/admin/common/upload"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="onUploadSuccess"
            :on-error="onUploadError"
            :before-upload="beforeUpload"
            accept="image/*"
          >
            <el-button size="small">上传头像</el-button>
          </el-upload>
        </div>
        <span class="label">邮箱</span>
        <el-input v-model="email" class="field" />
        <span class="label">手机</span>
        <el-input v-model="phone" class="field" />
        <div class="ops">
          <el-button type="primary" :loading="saving" size="small" @click="onSave">保存</el-button>
          <el-button size="small" @click="onCancel">取消</el-button>
        </div>
      </template>
    </div>
  </el-card>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { http } from '../api/http'
import { useAuth } from '../store/auth'
import { ElMessage } from 'element-plus'

const auth = useAuth()
const user = ref(null)
const editing = ref(false)
const saving = ref(false)
const email = ref('')
const phone = ref('')
const avatar = ref('')
const displayName = computed(() => {
  if (!user.value) return '—'
  return user.value.realName || user.value.name || '—'
})
const displayEmpNo = computed(() => (user.value && user.value.empNo) ? user.value.empNo : (auth.userId || '—'))
const displayStatus = computed(() => {
  const s = user.value?.status
  if (s === 1) return '正常'
  if (s === 0) return '禁用'
  return s != null ? String(s) : '—'
})
const avatarSrc = ref('data:image/svg+xml;utf8,<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"72\" height=\"72\" viewBox=\"0 0 36 36\"><defs><style>.c{fill:%23e5e7eb}.d{fill:%239ca3af}</style></defs><circle cx=\"18\" cy=\"18\" r=\"18\" class=\"c\"/><circle cx=\"18\" cy=\"13\" r=\"6\" class=\"d\"/><path d=\"M6 30c2.8-6 9.2-7 12-7s9.2 1 12 7\" class=\"d\"/></svg>')
const onAvatarError = () => {
  avatarSrc.value = 'data:image/svg+xml;utf8,<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"72\" height=\"72\" viewBox=\"0 0 36 36\"><defs><style>.c{fill:%23e5e7eb}.d{fill:%239ca3af}</style></defs><circle cx=\"18\" cy=\"18\" r=\"18\" class=\"c\"/><circle cx=\"18\" cy=\"13\" r=\"6\" class=\"d\"/><path d=\"M6 30c2.8-6 9.2-7 12-7s9.2 1 12 7\" class=\"d\"/></svg>'
}
onMounted(async () => {
  if (auth.userId) {
    const resp = await http(`/admin/admin/${auth.userId}`)
    if (resp && resp.code === 1) {
      user.value = resp.data
      email.value = user.value?.email || ''
      phone.value = user.value?.phone || ''
      if (user.value?.avatar) {
        avatar.value = user.value.avatar
        avatarSrc.value = user.value.avatar
      } else if (user.value?.avatarUrl) {
        avatar.value = user.value.avatarUrl
        avatarSrc.value = user.value.avatarUrl
      }
    }
  }
})
const onEdit = () => {
  editing.value = true
}
const onCancel = () => {
  editing.value = false
  email.value = user.value?.email || ''
  phone.value = user.value?.phone || ''
  if (user.value?.avatar) {
    avatar.value = user.value.avatar
    avatarSrc.value = user.value.avatar
  } else if (user.value?.avatarUrl) {
    avatar.value = user.value.avatarUrl
    avatarSrc.value = user.value.avatarUrl
  }
}
const onSave = async () => {
  if (!user.value) return
  saving.value = true
  try {
    const payload = {
      employeeId: user.value.employeeId,
      email: email.value,
      phone: phone.value,
      avatar: avatar.value
    }
    const resp = await http('/admin/admin', { method: 'PUT', body: payload })
    if (resp && resp.code === 1) {
      ElMessage.success('保存成功')
      user.value = { ...user.value, email: email.value, phone: phone.value, avatar: avatar.value }
      editing.value = false
    } else {
      ElMessage.error(resp?.msg || '保存失败')
    }
  } catch (_) {
    ElMessage.error('网络错误')
  } finally {
    saving.value = false
  }
}
const uploadHeaders = { token: localStorage.getItem('token') || '' }
const beforeUpload = (file) => {
  if (!file.type.startsWith('image/')) {
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
  avatar.value = url
  avatarSrc.value = url
  ElMessage.success('上传成功')
  try {
    window.dispatchEvent(new CustomEvent('avatar-updated', { detail: { url } }))
  } catch (_) {}
}
const onUploadError = () => {
  ElMessage.error('上传失败')
}
</script>

<style>
.profile-card {
  min-height: 92vh;
}
.header {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 12px;
}
.avatar {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  object-fit: cover;
  background: #e5e7eb;
}
.info {
  display: flex;
  flex-direction: column;
}
.info .line {
  color: #606266;
  font-size: 22px;
  font-weight: 500;
}
.desc {
  margin-top: 16px;
  font-size: 22px;
}
.desc .el-descriptions__label,
.desc .el-descriptions__content {
  font-size: 22px;
}
.edit-panel {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #e5e7eb;
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}
.edit-panel .label {
  color: #606266;
  width: 60px;
  text-align: right;
}
.edit-panel .field {
  width: 360px;
}
.avatar-edit {
  display: flex;
  align-items: center;
  gap: 12px;
}
.avatar-preview {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: #f5f7fa;
}
.ops {
  display: flex;
  gap: 8px;
}
</style>
