<template>
  <div class="login forgot-page">
    <div class="brand">
      <img class="badge" :src="badgeSrc" @error="onBadgeError" alt="校徽" />
      <div class="title">找回密码</div>
    </div>
    <el-card class="card" shadow="always">
      <div class="card-title">通过绑定邮箱重置</div>
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
        <el-form-item label="绑定邮箱" prop="email">
          <el-input v-model="form.email" placeholder="须与注册时填写完全一致（含大小写按系统存储）" clearable />
        </el-form-item>
        <el-form-item label="验证码" prop="code">
          <div class="code-row">
            <el-input v-model="form.code" placeholder="邮箱收到的6位验证码" maxlength="6" clearable />
            <el-button :disabled="sendCooldown > 0" :loading="sendLoading" @click="onSendCode">
              {{ sendCooldown > 0 ? `${sendCooldown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="form.newPassword" type="password" placeholder="至少6位" show-password />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="再次输入新密码" show-password />
        </el-form-item>
        <div class="ops">
          <el-button type="primary" :loading="loading" @click="onSubmit">重置密码</el-button>
          <el-button @click="goLogin">返回登录</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const badgeSrc = ref('/ptu-badge.png')
const onBadgeError = () => {
  badgeSrc.value =
    'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 160 160"><defs><style>.b{fill:none;stroke:%232563eb;stroke-width:6}.c{fill:%233b82f6}.t{fill:%23ffffff;font-size:32px;font-family:Arial,Helvetica,sans-serif;font-weight:bold}</style></defs><circle cx="80" cy="80" r="74" class="b"/><circle cx="80" cy="80" r="46" class="c"/><text x="80" y="95" text-anchor="middle" class="t">PTU</text></svg>'
}

const formRef = ref()
const form = ref({
  studentId: '',
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const emailPattern = /^[\w+.-]+@[\w.-]+\.[a-zA-Z]{2,}$/
const rules = {
  studentId: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    { pattern: /^\d{12}$/, message: '学号需为12位纯数字', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { pattern: emailPattern, message: '邮箱格式不正确', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码为6位数字', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_, v, cb) => {
        if (v !== form.value.newPassword) cb(new Error('两次密码不一致'))
        else cb()
      },
      trigger: 'blur'
    }
  ]
}

const sendLoading = ref(false)
const sendCooldown = ref(0)
let cooldownTimer = null
onUnmounted(() => {
  if (cooldownTimer) clearInterval(cooldownTimer)
})

const startCooldown = (sec) => {
  sendCooldown.value = sec
  if (cooldownTimer) clearInterval(cooldownTimer)
  cooldownTimer = setInterval(() => {
    sendCooldown.value -= 1
    if (sendCooldown.value <= 0) {
      clearInterval(cooldownTimer)
      cooldownTimer = null
    }
  }, 1000)
}

const parseJsonResult = async (resp) => {
  const ct = resp.headers.get('content-type') || ''
  let data = {}
  if (ct.includes('application/json')) {
    try {
      data = await resp.json()
    } catch (_) {}
  }
  return data
}

const onSendCode = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validateField(['studentId', 'email'])
  } catch (_) {
    return
  }
  sendLoading.value = true
  try {
    const resp = await fetch('/user/user/password/forgot/send-code', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ studentId: form.value.studentId, email: form.value.email })
    })
    const data = await parseJsonResult(resp)
    const code = data?.code
    const ok = code === 1 || code === 200
    if (ok) {
      if (data?.data?.devMailMock === true) {
        ElMessage({
          message:
            '当前为邮件模拟模式：未发真实邮件。请在运行后端的控制台查找「忘记密码」日志中的 6 位验证码；并确认 Redis 已启动。',
          type: 'warning',
          duration: 12000,
          showClose: true
        })
      } else {
        ElMessage.success(
          '请求已处理。学号与注册邮箱一致且已配置真实邮箱服务时会收到邮件；否则请核对信息。开发环境未配 SMTP 时请看页面下方说明与后端日志。'
        )
      }
      startCooldown(60)
    } else {
      ElMessage.error(data?.msg || '发送失败')
      if (String(data?.msg || '').includes('频繁')) {
        startCooldown(60)
      }
    }
  } catch (_) {
    ElMessage.error('网络错误')
  } finally {
    sendLoading.value = false
  }
}

const loading = ref(false)
const goLogin = () => router.push('/login')

const onSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch (_) {
    return
  }
  loading.value = true
  try {
    const resp = await fetch('/user/user/password/forgot/reset', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        studentId: form.value.studentId,
        email: form.value.email,
        code: form.value.code,
        newPassword: form.value.newPassword
      })
    })
    const data = await parseJsonResult(resp)
    const code = data?.code
    const ok = code === 1 || code === 200
    if (ok) {
      ElMessage.success('密码已重置，请使用新密码登录')
      router.push('/login')
    } else {
      ElMessage.error(data?.msg || '重置失败')
    }
  } catch (_) {
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.forgot-page .code-row {
  display: flex;
  gap: 10px;
  width: 100%;
}
.forgot-page .code-row .el-input {
  flex: 1;
}
</style>

<style>
/* 与登录页一致的背景与卡片样式（Login.vue 中为全局样式） */
.forgot-page.login {
  min-height: 92vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background-color: #f0f9ff;
  background-image:
    linear-gradient(rgba(3, 105, 161, 0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(3, 105, 161, 0.06) 1px, transparent 1px),
    radial-gradient(ellipse 900px 520px at 50% -15%, rgba(14, 165, 233, 0.2) 0%, transparent 55%),
    radial-gradient(ellipse 700px 400px at 100% 60%, rgba(56, 189, 248, 0.18) 0%, transparent 50%),
    radial-gradient(ellipse 600px 380px at 0% 70%, rgba(3, 105, 161, 0.12) 0%, transparent 48%),
    linear-gradient(175deg, #f8fafc 0%, #f0f9ff 28%, #e0f2fe 68%, #bae6fd 100%);
  background-size:
    36px 36px,
    36px 36px,
    100% 100%,
    100% 100%,
    100% 100%,
    100% 100%;
  background-position: center;
  background-repeat: repeat, repeat, no-repeat, no-repeat, no-repeat, no-repeat;
  padding: 24px 14px;
}
.forgot-page .brand {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}
.forgot-page .brand .badge {
  width: 44px;
  height: 44px;
  filter: drop-shadow(0 2px 8px rgba(3, 105, 161, 0.2));
}
.forgot-page .brand .title {
  font-family: var(--font-heading);
  font-weight: 700;
  font-size: 24px;
  letter-spacing: 0.02em;
  color: var(--text-main);
}
.forgot-page .card {
  width: min(420px, 100%);
  border-radius: var(--radius);
  border: 1px solid color-mix(in srgb, var(--primary) 22%, var(--border));
  box-shadow:
    0 4px 6px rgba(3, 105, 161, 0.06),
    var(--shadow-md);
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(8px);
}
.forgot-page .card .el-card__body {
  padding: 22px 22px 20px;
}
.forgot-page .login-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.forgot-page .card-title {
  font-family: var(--font-heading);
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 14px;
  text-align: center;
  color: var(--text-main);
}
.forgot-page .ops {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
  margin-top: 4px;
}
</style>
