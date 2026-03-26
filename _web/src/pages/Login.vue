<template>
  <div class="login-container">
    <div class="logo-title">
      <img class="badge" :src="badgeSrc" @error="onBadgeError" alt="莆田学院校徽" />
      <div class="system-title">莆田学院校园书店系统</div>
    </div>
    <el-card class="login-card">
      <div class="title">管理员登录</div>
      <el-form class="login-form" label-position="top" :model="form" :rules="rules" ref="formRef" label-width="clamp(100px, 8vw, 160px)">
        <el-form-item label="工号" prop="empNo">
          <el-input v-model="form.empNo" placeholder="请输入工号" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password size="large" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" size="large" @click="onSubmit">登录</el-button>
          <el-button size="large" @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { http } from '../api/http'
import { useRouter } from 'vue-router'
import { setAuth } from '../store/auth'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const form = ref({ empNo: '', password: '' })
const badgeSrc = ref('/ptu-badge.png')
const onBadgeError = () => {
  badgeSrc.value =
    'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="160" height="160" viewBox="0 0 160 160"><defs><style>.a{fill:%23ffffff;opacity:0.92}.b{fill:none;stroke:%23ffffff;stroke-width:6}.c{fill:%23ffffff;font-size:18px;font-family:Arial,Helvetica,sans-serif;font-weight:bold;letter-spacing:2px}</style></defs><circle cx="80" cy="80" r="74" class="b"/><rect x="45" y="62" width="70" height="40" rx="6" class="a"/><text x="80" y="30" text-anchor="middle" class="c">PUTIAN</text><text x="80" y="142" text-anchor="middle" class="c">UNIVERSITY</text></svg>'
}
const rules = {
  empNo: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const onSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const resp = await http('/admin/admin/login', {
        method: 'POST',
        body: {
          empNo: form.value.empNo,
          password: form.value.password
        }
      })
      if (resp && resp.code === 1 && resp.data && resp.data.token) {
        setAuth(resp.data.token, resp.data.id)
        ElMessage.success('登录成功')
        router.push('/admin')
      } else {
        ElMessage.error(resp?.msg || '登录失败')
      }
    } catch (e) {
      ElMessage.error('网络错误')
    } finally {
      loading.value = false
    }
  })
}

const onReset = () => {
  formRef.value.resetFields()
}
</script>

<style>
html, body, #app {
  height: 100%;
}
.login-container {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background-color: #0369a1;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.05) 1px, transparent 1px),
    radial-gradient(ellipse 100% 70% at 50% -25%, rgba(255, 255, 255, 0.22) 0%, transparent 52%),
    radial-gradient(ellipse 55% 45% at 100% 85%, rgba(14, 165, 233, 0.45) 0%, transparent 55%),
    radial-gradient(ellipse 50% 42% at 0% 75%, rgba(2, 132, 199, 0.38) 0%, transparent 52%),
    linear-gradient(165deg, #0c4a6e 0%, #0369a1 40%, #0284c7 100%);
  background-size:
    40px 40px,
    40px 40px,
    100% 100%,
    100% 100%,
    100% 100%,
    100% 100%;
  background-position: center;
  background-repeat: repeat, repeat, no-repeat, no-repeat, no-repeat, no-repeat;
  background-attachment: fixed;
}
.logo-title {
  position: absolute;
  top: clamp(24px, 6vh, 64px);
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  gap: clamp(10px, 2vw, 18px);
}
.badge {
  width: clamp(72px, 12vw, 120px);
  height: auto;
  filter: drop-shadow(0 4px 14px rgba(0, 0, 0, 0.2));
}
.system-title {
  font-family: var(--font-heading);
  color: #f8fafc;
  text-shadow: 0 2px 12px rgba(12, 74, 110, 0.45);
  font-size: clamp(20px, 3vw, 34px);
  font-weight: 700;
  letter-spacing: 0.06em;
}
.login-card {
  width: min(540px, calc(100vw - 28px));
  padding: 14px;
  min-height: auto;
  border-radius: var(--admin-radius);
  border: 1px solid color-mix(in srgb, #ffffff 55%, #bae6fd);
  background-color: rgba(255, 255, 255, 0.94);
  box-shadow:
    0 4px 6px rgba(12, 74, 110, 0.08),
    0 22px 48px rgba(3, 105, 161, 0.22);
  backdrop-filter: blur(10px);
}
.login-card .el-card__body {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 12px 12px 4px;
}
.login-form {
  width: min(400px, 100%);
  margin: 0 auto;
}
.title {
  font-family: var(--font-heading);
  font-size: clamp(24px, 2.6vw, 32px);
  font-weight: 700;
  margin-bottom: 16px;
  text-align: center;
  color: #0f172a;
}
.el-form-item {
  margin-bottom: clamp(18px, 2.4vw, 32px);
}
.login-form .el-form-item__label {
  font-size: clamp(18px, 2.2vw, 24px);
}
.login-form .el-input__inner {
  font-size: clamp(18px, 2.2vw, 24px);
}
.login-form .el-input__inner::placeholder {
  font-size: inherit;
}
@media (max-width: 680px) {
  .logo-title {
    top: 26px;
  }
  .system-title {
    font-size: 20px;
    letter-spacing: 1px;
  }
}
@media (prefers-reduced-motion: reduce) {
  .login-container {
    background-attachment: scroll;
  }
}
</style>
