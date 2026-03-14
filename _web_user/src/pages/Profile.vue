<template>
  <div class="profile-page">
    <div class="title-bar">
      <div class="title">个人信息</div>
      <div class="ops">
        <el-button @click="goHome">返回首页</el-button>
        <el-button v-if="!isEditing" type="primary" @click="startEdit">编辑</el-button>
        <template v-else>
          <el-button @click="onCancel">取消</el-button>
          <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
        </template>
      </div>
    </div>
    <el-card class="card profile-card" shadow="always">
      <div class="layout">
        <div class="avatar-block">
          <div class="avatar-preview">
            <img :src="form.avatar || defaultAvatar" @error="onAvatarError" alt="avatar" />
          </div>
          <el-upload
            v-if="isEditing"
            class="upload"
            :show-file-list="false"
            :auto-upload="false"
            :on-change="onAvatarChange"
            accept="image/*"
          >
            <el-button>选择头像</el-button>
          </el-upload>
        </div>
        <div class="info-block">
          <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="profile-form">
            <el-form-item label="学号">
              <el-input v-model="form.studentId" disabled />
            </el-form-item>
            <el-form-item label="姓名" prop="realName">
              <el-input v-model="form.realName" placeholder="请输入姓名" :disabled="!isEditing" />
            </el-form-item>
            <el-form-item label="昵称" prop="username">
              <el-input v-model="form.username" placeholder="请输入昵称" :disabled="!isEditing" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" :disabled="!isEditing" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" type="tel" maxlength="11" :disabled="!isEditing" />
            </el-form-item>
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="form.gender" :disabled="!isEditing">
                <el-radio :label="1">男</el-radio>
                <el-radio :label="0">女</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="专业" prop="majorId">
              <el-select v-model="form.majorId" placeholder="请选择专业" filterable style="width: 100%" :disabled="!isEditing">
                <el-option v-for="m in majors" :key="String(m.id)" :label="m.name" :value="m.id" />
              </el-select>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </el-card>
    <div class="card address-section">
      <div class="addr-header">
        <div class="addr-title">收货地址</div>
        <div class="ops">
          <el-button type="primary" @click="openAddAddr">新增地址</el-button>
        </div>
      </div>
      <el-table v-if="addresses.length" :data="addresses" border style="width: 100%">
        <el-table-column prop="consignee" label="收货人" width="90" />
        <el-table-column prop="phone" label="手机号" width="110" />
        <el-table-column label="地址" min-width="260">
          <template #default="{ row }">
            <span class="addr-text">{{ formatAddr(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="标签" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.label" size="small">{{ row.label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="默认" width="70">
          <template #default="{ row }">
            <el-tag v-if="row.isDefault === 1" type="success" size="small">默认</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEditAddr(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteAddr(row)">删除</el-button>
            <el-button size="small" type="primary" @click="setDefaultAddr(row)" :disabled="row.isDefault === 1">设为默认</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-else class="addr-empty">暂无地址</div>
    </div>
  </div>
  <FloatingCartButton />
  <el-dialog v-model="addrDialogVisible" title="地址" width="640px">
    <el-form ref="addrFormRef" :model="addrForm" :rules="addrRules" label-width="110px">
      <el-form-item label="收货人" prop="consignee">
        <el-input v-model="addrForm.consignee" placeholder="请输入收货人" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="addrForm.phone" placeholder="请输入手机号" maxlength="11" />
      </el-form-item>
      <el-form-item label="性别" prop="sex">
        <el-radio-group v-model="addrForm.sex">
          <el-radio :label="1">男</el-radio>
          <el-radio :label="0">女</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="省份">
        <el-input v-model="addrForm.provinceName" disabled />
      </el-form-item>
      <el-form-item label="城市">
        <el-input v-model="addrForm.cityName" disabled />
      </el-form-item>
      <el-form-item label="区县" prop="districtName">
        <el-select v-model="addrForm.districtName" placeholder="请选择区县" @change="onDistrictChange">
          <el-option label="城厢区" value="城厢区" />
          <el-option label="荔城区" value="荔城区" />
        </el-select>
      </el-form-item>
      <el-form-item label="学校分区">
        <el-input v-model="addrForm.schoolPartition" disabled />
      </el-form-item>
      <el-form-item label="宿舍楼" prop="building">
        <el-input v-model="addrForm.building" placeholder="请输入宿舍楼" />
      </el-form-item>
      <el-form-item label="宿舍号" prop="houseNumber">
        <el-input v-model="addrForm.houseNumber" placeholder="请输入宿舍号" />
      </el-form-item>
      <el-form-item label="标签">
        <el-input v-model="addrForm.label" placeholder="如 家/公司/学校" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-ops">
        <el-button @click="addrDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="addrSaving" @click="onAddrSave">保存</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import FloatingCartButton from '../components/FloatingCartButton.vue'
const router = useRouter()
const defaultAvatar =
  'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="200" height="200" viewBox="0 0 40 40"><circle cx="20" cy="20" r="19" fill="%23e5e7eb"/><circle cx="20" cy="16" r="7" fill="%23d1d5db"/><rect x="8" y="24" width="24" height="10" rx="5" fill="%23d1d5db"/></svg>'
const formRef = ref()
const saving = ref(false)
const isEditing = ref(false)
const originalForm = ref(null)
const form = ref({
  userId: '',
  studentId: '',
  realName: '',
  username: '',
  email: '',
  phone: '',
  gender: 1,
  majorId: '',
  avatar: ''
})
const majors = ref([])
const rules = {
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  username: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  majorId: [{ required: true, message: '请选择专业', trigger: 'change' }]
}
const onAvatarError = (e) => {
  e.target.src = defaultAvatar
}
const goHome = () => router.push('/')
const loadMajors = async () => {
  try {
    const token = localStorage.getItem('token') || ''
    const resp = await fetch('/user/major/page?page=1&pageSize=200', {
      method: 'GET',
      headers: token ? { authentication: token } : {}
    })
    const ct = resp.headers.get('content-type') || ''
    let data = {}
    if (ct.includes('application/json')) {
      try { data = await resp.json() } catch (_) {}
    }
    if (resp.ok) {
      const d = data?.data ?? data
      let rows = []
      if (Array.isArray(d)) rows = d
      else rows = d?.records || d?.list || d?.items || d?.rows || d?.data || []
      const filtered = (rows || []).filter(r => r && (r.status === undefined || r.status === 1))
      filtered.sort((a, b) => {
        const sa = a?.sort ?? 0
        const sb = b?.sort ?? 0
        if (sa !== sb) return sa - sb
        const na = String(a?.name || '')
        const nb = String(b?.name || '')
        return na.localeCompare(nb)
      })
      majors.value = filtered
    } else {
      majors.value = []
    }
  } catch (_) {
    majors.value = []
  }
}
const loadProfile = async () => {
  const token = localStorage.getItem('token') || ''
  const userId = localStorage.getItem('userId') || ''
  if (!token || !userId) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: '/profile', msg: '请先登录' } })
    return
  }
  try {
    const resp = await fetch(`/user/user/${encodeURIComponent(userId)}`, {
      method: 'GET',
      headers: { authentication: token }
    })
    const ct = resp.headers.get('content-type') || ''
    let data = {}
    if (ct.includes('application/json')) {
      try { data = await resp.json() } catch (_) {}
    }
    const d = data?.data ?? data
    if (resp.ok && d) {
      form.value.userId = d.userId || d.id || userId
      form.value.studentId = d.studentId || ''
      form.value.realName = d.realName || ''
      form.value.username = d.username || ''
      form.value.email = d.email || ''
      form.value.phone = d.phone || ''
      form.value.gender = d.gender === 0 ? 0 : 1
      form.value.majorId = d.majorId || ''
      form.value.avatar = d.avatar || d.avatarUrl || ''
      originalForm.value = JSON.parse(JSON.stringify(form.value))
    } else {
      ElMessage.error('加载个人信息失败')
    }
  } catch (_) {
    ElMessage.error('加载个人信息失败')
  }
}
const onAvatarChange = async (file) => {
  const f = file?.raw || file
  if (!f) return
  if (f.size > 2 * 1024 * 1024) {
    ElMessage.error('头像大小不能超过2MB')
    return
  }
  const token = localStorage.getItem('token') || ''
  const fd = new FormData()
  fd.append('file', f)
  try {
    const resp = await fetch('/user/common/upload', {
      method: 'POST',
      headers: token ? { authentication: token } : {},
      body: fd
    })
    const ct = resp.headers.get('content-type') || ''
    let data = {}
    if (ct.includes('application/json')) {
      try { data = await resp.json() } catch (_) {}
    }
    if (resp.ok) {
      const d = data?.data ?? data
      const u = (() => {
        if (typeof d === 'string') return d
        if (typeof d?.data === 'string') return d.data
        return d?.avatar || d?.url || d?.avatarUrl || d?.link || d?.path || d?.data?.url || ''
      })()
      if (u && String(u).trim()) {
        form.value.avatar = u
        ElMessage.success('头像上传成功')
        return
      }
    }
    ElMessage.error('头像上传失败')
  } catch (_) {
    ElMessage.error('头像上传失败')
  }
}
const onSave = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch (_) {
    return
  }
  saving.value = true
  try {
    const token = localStorage.getItem('token') || ''
    const payload = {
      userId: form.value.userId || localStorage.getItem('userId') || '',
      realName: form.value.realName,
      username: form.value.username,
      email: form.value.email,
      phone: form.value.phone,
      gender: form.value.gender,
      majorId: form.value.majorId,
      avatar: form.value.avatar
    }
    const tryUpdate = async (url) => {
      const resp = await fetch(url, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json', ...(token ? { authentication: token } : {}) },
        body: JSON.stringify(payload)
      })
      const ct = resp.headers.get('content-type') || ''
      let data = {}
      if (ct.includes('application/json')) {
        try { data = await resp.json() } catch (_) {}
      }
      return { ok: resp.ok, data }
    }
    let r = await tryUpdate('/user/user/update')
    if (!r.ok) r = await tryUpdate('/user/user')
    if (r.ok) {
      ElMessage.success('保存成功')
      if (form.value.realName) localStorage.setItem('userRealName', form.value.realName)
      if (form.value.username) localStorage.setItem('userUsername', form.value.username)
      if (form.value.avatar) localStorage.setItem('userAvatar', form.value.avatar)
      window.dispatchEvent(new CustomEvent('user-logged-in'))
      originalForm.value = JSON.parse(JSON.stringify(form.value))
      isEditing.value = false
    } else {
      ElMessage.error(r.data?.msg || '保存失败')
    }
  } catch (_) {
    ElMessage.error('网络错误')
  } finally {
    saving.value = false
  }
}
const startEdit = () => {
  isEditing.value = true
}
const onCancel = () => {
  if (originalForm.value) {
    form.value = JSON.parse(JSON.stringify(originalForm.value))
  }
  isEditing.value = false
}
const addresses = ref([])
const addrDialogVisible = ref(false)
const addrFormRef = ref()
const addrSaving = ref(false)
const addrForm = ref({
  id: '',
  userId: '',
  consignee: '',
  phone: '',
  sex: 1,
  provinceCode: '',
  provinceName: '',
  cityCode: '',
  cityName: '',
  districtCode: '',
  districtName: '',
  label: '',
  isDefault: 0,
  schoolPartition: '',
  building: '',
  houseNumber: ''
})
const addrRules = {
  consignee: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  sex: [{ required: true, message: '请选择性别', trigger: 'change' }],
  districtName: [{ required: true, message: '请选择区县', trigger: 'change' }],
  building: [{ required: true, message: '请输入宿舍楼', trigger: 'blur' }],
  houseNumber: [{ required: true, message: '请输入宿舍号', trigger: 'blur' }]
}
const onDistrictChange = () => {
  if (addrForm.value.districtName === '荔城区') addrForm.value.schoolPartition = '紫霄校区'
  else if (addrForm.value.districtName === '城厢区') addrForm.value.schoolPartition = '学园校区'
  else addrForm.value.schoolPartition = ''
}
const formatAddr = (r) => {
  const parts = [
    r?.provinceName,
    r?.cityName,
    r?.districtName,
    r?.schoolPartition,
    (r?.building && r?.houseNumber) ? `${r.building} ${r.houseNumber}` : (r?.building || r?.houseNumber || '')
  ]
  return parts.filter(Boolean).join(' ')
}
const loadAddresses = async () => {
  try {
    const token = localStorage.getItem('token') || ''
    const resp = await fetch('/user/addressBook/list', {
      method: 'GET',
      headers: token ? { authentication: token } : {}
    })
    const ct = resp.headers.get('content-type') || ''
    let data = {}
    if (ct.includes('application/json')) {
      try { data = await resp.json() } catch (_) {}
    }
    if (resp.ok) {
      const d = data?.data ?? data
      let rows = []
      if (Array.isArray(d)) rows = d
      else rows = d?.records || d?.list || d?.items || d?.rows || d?.data || []
      addresses.value = (rows || []).map(r => ({
        id: r?.id,
        userId: r?.userId,
        consignee: r?.consignee || '',
        phone: r?.phone || '',
        sex: Number(r?.sex ?? 1),
        provinceCode: r?.provinceCode || '',
        provinceName: r?.provinceName || '',
        cityCode: r?.cityCode || '',
        cityName: r?.cityName || '',
        districtCode: r?.districtCode || '',
        districtName: r?.districtName || '',
        label: r?.label || '',
        isDefault: Number(r?.isDefault ?? 0),
        schoolPartition: r?.schoolPartition || '',
        building: r?.building || '',
        houseNumber: r?.houseNumber || ''
      }))
    } else {
      addresses.value = []
    }
  } catch (_) {
    addresses.value = []
  }
}
const openAddAddr = () => {
  addrForm.value = {
    id: '',
    userId: localStorage.getItem('userId') || '',
    consignee: '',
    phone: '',
    sex: 1,
    provinceCode: '',
    provinceName: '福建省',
    cityCode: '',
    cityName: '莆田市',
    districtCode: '',
    districtName: '',
    label: '',
    isDefault: 0,
    schoolPartition: '',
    building: '',
    houseNumber: ''
  }
  addrDialogVisible.value = true
}
const openEditAddr = (row) => {
  addrForm.value = {
    ...(row || {}),
    userId: localStorage.getItem('userId') || '',
    provinceName: '福建省',
    cityName: '莆田市'
  }
  onDistrictChange()
  addrDialogVisible.value = true
}
const onAddrSave = async () => {
  if (!addrFormRef.value) return
  try {
    await addrFormRef.value.validate()
  } catch (_) {
    return
  }
  addrSaving.value = true
  try {
    const token = localStorage.getItem('token') || ''
    const hasId = !!addrForm.value.id
    const payload = hasId ? addrForm.value : { ...addrForm.value, isDefault: 0 }
    const resp = await fetch('/user/addressBook' + (hasId ? '' : ''), {
      method: hasId ? 'PUT' : 'POST',
      headers: { 'Content-Type': 'application/json', ...(token ? { authentication: token } : {}) },
      body: JSON.stringify(payload)
    })
    const ok = resp.ok
    if (ok) {
      ElMessage.success(hasId ? '地址已更新' : '地址已新增')
      addrDialogVisible.value = false
      loadAddresses()
    } else {
      ElMessage.error('保存地址失败')
    }
  } catch (_) {
    ElMessage.error('保存地址失败')
  } finally {
    addrSaving.value = false
  }
}
const setDefaultAddr = async (row) => {
  if (!row || !row.id) return
  try {
    const token = localStorage.getItem('token') || ''
    const resp = await fetch('/user/addressBook/default', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json', ...(token ? { authentication: token } : {}) },
      body: JSON.stringify({ id: row.id })
    })
    if (resp.ok) {
      ElMessage.success('已设置默认地址')
      loadAddresses()
    } else {
      ElMessage.error('设置默认地址失败')
    }
  } catch (_) {
    ElMessage.error('设置默认地址失败')
  }
}
const deleteAddr = async (row) => {
  if (!row || !row.id) return
  try {
    const token = localStorage.getItem('token') || ''
    const id = row.id
    const resp = await fetch(`/user/addressBook?id=${encodeURIComponent(id)}`, {
      method: 'DELETE',
      headers: token ? { authentication: token } : {}
    })
    if (resp.ok) {
      ElMessage.success('已删除地址')
      loadAddresses()
    } else {
      ElMessage.error('删除地址失败')
    }
  } catch (_) {
    ElMessage.error('删除地址失败')
  }
}
onMounted(() => {
  loadMajors()
  loadProfile()
  loadAddresses()
})
</script>

<style>
.profile-page {
  min-height: 92vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  padding: 20px;
}
.title-bar {
  width: 860px;
  max-width: 96vw;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.title {
  font-weight: 600;
  font-size: 18px;
}
.card {
  width: 860px;
  max-width: 96vw;
}
.layout {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 16px;
}
.avatar-block { display: flex; flex-direction: column; gap: 10px; align-items: center; }
.avatar-preview {
  width: 200px;
  height: 200px;
  border-radius: 50%;
  background: #f5f7fa;
  overflow: hidden;
}
.avatar-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.info-block { width: 100%; }
.profile-card .el-card__body { padding: 8px 16px; }
.address-section {
  margin-top: 16px;
}
.addr-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.addr-title {
  font-weight: 600;
}
.addr-empty {
  text-align: center;
  color: #606266;
}
.addr-text {
  display: block;
  white-space: normal;
  word-break: break-word;
  line-height: 20px;
}
</style>
