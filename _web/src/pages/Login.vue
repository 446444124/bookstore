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
  background-image: linear-gradient(rgba(16, 28, 45, 0.45), rgba(16, 28, 45, 0.45)), url('https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?q=80&w=1920&auto=format&fit=crop');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
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
  filter: drop-shadow(0 4px 10px rgba(0,0,0,0.25));
}
.system-title {
  color: #ffffff;
  text-shadow: 0 2px 6px rgba(0,0,0,0.25);
  font-size: clamp(20px, 3vw, 34px);
  font-weight: 700;
  letter-spacing: 2px;
}
.login-card {
  width: clamp(460px, 36vw, 820px);
  padding: 24px;
  min-height: clamp(380px, 46vh, 560px);
  background-color: rgba(255, 255, 255, 0.92);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.18);
}
.login-card .el-card__body {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
}
.login-form {
  width: clamp(360px, 72%, 640px);
  margin: 0 auto;
}
.title {
  font-size: clamp(24px, 2.8vw, 36px);
  font-weight: 600;
  margin-bottom: clamp(18px, 2vw, 28px);
  text-align: center;
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
</style>
