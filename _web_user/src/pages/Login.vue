<template>
  <div class="login">
    <div class="brand">
      <img class="badge" :src="badgeSrc" @error="onBadgeError" alt="校徽" />
      <div class="title">莆田学院校园书店</div>
    </div>
    <el-card class="card" shadow="always">
      <div class="card-title">用户登录</div>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        size="large"
        class="login-form"
      >
        <el-form-item label="学号" prop="studentId">
          <el-input v-model="form.studentId" placeholder="请输入12位学号" maxlength="12" clearable />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <div class="ops">
          <el-button type="primary" :loading="loading" @click="onSubmit">登录</el-button>
          <el-button type="success" @click="regVisible = true">注册</el-button>
          <el-button @click="goHome">返回首页</el-button>
        </div>
      </el-form>
    </el-card>
    <el-dialog v-model="regVisible" title="用户注册" width="520px">
      <el-form ref="regFormRef" :model="regForm" :rules="regRules" label-width="90px">
        <el-form-item label="学号" prop="studentId">
          <el-input v-model="regForm.studentId" placeholder="请输入12位学号" maxlength="12" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="regForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="regForm.confirmPassword" type="password" placeholder="请再次输入密码" show-password />
        </el-form-item>
        <el-form-item label="专业" prop="majorId">
          <el-select v-model="regForm.majorId" placeholder="请选择专业" filterable style="width: 100%">
            <el-option v-for="m in majors" :key="String(m.id)" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="regForm.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="昵称" prop="username">
          <el-input v-model="regForm.username" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="regForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="regForm.phone" placeholder="请输入手机号" type="tel" maxlength="11" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="regForm.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="0">女</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-ops">
          <el-button @click="regVisible = false">取消</el-button>
          <el-button type="primary" :loading="regLoading" @click="onRegister">注册</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { onMounted } from 'vue'
import { ElMessage } from 'element-plus'
const router = useRouter()
const route = useRoute()
const badgeSrc = ref('/ptu-badge.png')
const onBadgeError = () => {
  badgeSrc.value =
    'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 160 160"><defs><style>.b{fill:none;stroke:%232563eb;stroke-width:6}.c{fill:%233b82f6}.t{fill:%23ffffff;font-size:32px;font-family:Arial,Helvetica,sans-serif;font-weight:bold}</style></defs><circle cx="80" cy="80" r="74" class="b"/><circle cx="80" cy="80" r="46" class="c"/><text x="80" y="95" text-anchor="middle" class="t">PTU</text></svg>'
}
const formRef = ref()
const form = ref({ studentId: '', password: '' })
const rules = {
  studentId: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    { pattern: /^\d{12}$/, message: '学号需为12位纯数字', trigger: 'blur' }
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}
const loading = ref(false)
const goHome = () => router.push('/')
const onSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch (_) {
    return
  }
  loading.value = true
  try {
    const resp = await fetch('/user/user/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ studentId: form.value.studentId, password: form.value.password })
    })
    const ct = resp.headers.get('content-type') || ''
    let data = {}
    if (ct.includes('application/json')) {
      try { data = await resp.json() } catch (_) {}
    }
    if (resp.ok) {
      const token = data?.data?.token || data?.token || ''
      const userId = data?.data?.id || data?.data?.userId || data?.userId || ''
      if (token) {
        localStorage.setItem('token', token)
        if (userId) localStorage.setItem('userId', String(userId))
        try {
          if (userId) {
            const profResp = await fetch(`/user/user/${encodeURIComponent(userId)}`, {
              method: 'GET',
              headers: { authentication: token }
            })
            const profCt = profResp.headers.get('content-type') || ''
            let profData = {}
            if (profCt.includes('application/json')) {
              try { profData = await profResp.json() } catch (_) {}
            }
            const pd = profData?.data ?? profData
            const name = pd?.realName || pd?.username || ''
            const avatar = pd?.avatar || pd?.avatarUrl || ''
            if (name) localStorage.setItem('userRealName', name)
            const nickname = pd?.username || ''
            if (nickname) localStorage.setItem('userUsername', nickname)
            if (avatar) localStorage.setItem('userAvatar', avatar)
            window.dispatchEvent(new CustomEvent('user-logged-in', { detail: { userId, token, name, avatar } }))
          } else {
            window.dispatchEvent(new CustomEvent('user-logged-in', { detail: { userId, token } }))
          }
        } catch (_) {}
        ElMessage.success('登录成功')
        const redirect = typeof route?.query?.redirect === 'string' ? route.query.redirect : ''
        if (redirect) router.push(redirect)
        else router.push('/')
      } else {
        ElMessage.error(data?.msg || '登录失败')
      }
    } else {
      ElMessage.error(data?.msg || '登录失败')
    }
  } catch (_) {
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}
onMounted(() => {
  const msg = typeof route?.query?.msg === 'string' ? route.query.msg : ''
  if (msg) ElMessage.warning(msg)
})
const regVisible = ref(false)
const regFormRef = ref()
const majors = ref([])
const regForm = ref({ studentId: '', password: '', confirmPassword: '', majorId: '', realName: '', username: '', email: '', phone: '', gender: 1 })
const regRules = {
  studentId: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    { pattern: /^\d{12}$/, message: '学号需为12位纯数字', trigger: 'blur' }
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_, v, cb) => {
        if (v !== regForm.value.password) cb(new Error('两次密码不一致'))
        else cb()
      },
      trigger: 'blur'
    }
  ],
  majorId: [{ required: true, message: '请选择专业', trigger: 'change' }],
  realName: [{ required: true, message: '请输入姓名' }],
  username: [{ required: true, message: '请输入昵称' }],
  email: [{ required: true, message: '请输入邮箱' }],
  phone: [
    { required: true, message: '请输入手机号' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号' }
  ],
  gender: [{ required: true, message: '请选择性别' }]
}
const regLoading = ref(false)
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
onMounted(loadMajors)
const onRegister = async () => {
  if (!regFormRef.value) return
  try {
    await regFormRef.value.validate()
  } catch (_) {
    return
  }
  regLoading.value = true
  try {
    const resp = await fetch('/user/user/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        studentId: regForm.value.studentId,
        password: regForm.value.password,
        majorId: regForm.value.majorId,
        realName: regForm.value.realName,
        username: regForm.value.username,
        email: regForm.value.email,
        phone: regForm.value.phone,
        gender: regForm.value.gender
      })
    })
    const ct = resp.headers.get('content-type') || ''
    let data = {}
    if (ct.includes('application/json')) {
      try { data = await resp.json() } catch (_) {}
    }
    const code = data?.code
    const hasCode = code !== undefined && code !== null
    const success =
      (hasCode ? (code === 1 || code === 200) : (data?.success === true || data?.msg === 'ok' || data?.message === 'ok')) &&
      resp.ok
    if (success) {
      ElMessage.success('注册成功')
      regVisible.value = false
      form.value.studentId = regForm.value.studentId
      form.value.password = regForm.value.password
      onSubmit()
    } else {
      ElMessage.error(data?.msg || '注册失败')
    }
  } catch (_) {
    ElMessage.error('网络错误')
  } finally {
    regLoading.value = false
  }
}
</script>

<style>
.login {
  min-height: 92vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: radial-gradient(1200px 600px at 70% -20%, #e7ebff 0%, transparent 55%), var(--bg);
  padding: 24px 14px;
}
.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}
.brand .badge {
  width: 44px;
  height: 44px;
}
.brand .title {
  font-weight: 700;
  font-size: 24px;
  letter-spacing: .2px;
}
.card {
  width: min(420px, 100%);
  border-radius: var(--radius);
  border: 1px solid var(--border);
  box-shadow: var(--shadow-md);
}
.card .el-card__body {
  padding: 22px 22px 20px;
}
.login-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.card-title {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 14px;
  text-align: center;
}
.ops {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
  margin-top: 4px;
}
.dialog-ops {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}
@media (max-width: 640px) {
  .brand .title {
    font-size: 20px;
  }
  .card .el-card__body {
    padding: 16px 14px;
  }
}
</style>
