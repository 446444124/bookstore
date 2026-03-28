<template>
  <div class="ai-fab-root">
    <button type="button" class="ai-fab" aria-label="打开书店助手" @click="open = true">助手</button>
    <el-drawer v-model="open" title="书店助手" direction="rtl" size="min(420px, 100vw)" class="ai-drawer">
      <div class="ai-intro">可询问本书店公告、特惠专区活动等（回答基于当前店内数据）。</div>
      <el-scrollbar class="ai-scroll">
        <div v-for="(m, i) in messages" :key="i" :class="['ai-msg', m.role]">
          <div class="ai-msg-label">{{ m.role === 'user' ? '我' : '助手' }}</div>
          <div class="ai-msg-text">{{ m.text }}</div>
        </div>
        <div v-if="loading" class="ai-msg assistant">
          <div class="ai-msg-label">助手</div>
          <div class="ai-msg-text muted">正在思考…</div>
        </div>
      </el-scrollbar>
      <div class="ai-input-row">
        <el-input
          v-model="input"
          type="textarea"
          :rows="2"
          placeholder="例如：最近有什么优惠活动？"
          maxlength="2000"
          show-word-limit
          @keydown.enter.exact.prevent="onEnterSend"
        />
        <el-button type="primary" :loading="loading" :disabled="!input.trim()" @click="send">发送</el-button>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const open = ref(false)
const input = ref('')
const loading = ref(false)
const messages = ref([
  {
    role: 'assistant',
    text: '你好，我是莆田学院校园书店助手。可以问我当前特惠活动、系统公告等问题。'
  }
])

const onEnterSend = () => {
  if (!loading.value && input.value.trim()) send()
}

const send = async () => {
  const q = input.value.trim()
  if (!q || loading.value) return
  messages.value.push({ role: 'user', text: q })
  input.value = ''
  loading.value = true
  try {
    const token = localStorage.getItem('token') || ''
    const resp = await fetch('/user/ai/chat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { authentication: token } : {})
      },
      body: JSON.stringify({ message: q })
    })
    const ct = resp.headers.get('content-type') || ''
    let data = {}
    if (ct.includes('application/json')) {
      try {
        data = await resp.json()
      } catch (_) {}
    }
    const code = data?.code
    const ok = code === 1 || code === 200
    const reply = data?.data?.reply ?? data?.reply
    if (ok && reply) {
      messages.value.push({ role: 'assistant', text: String(reply) })
    } else {
      messages.value.push({
        role: 'assistant',
        text: data?.msg || '暂时无法回答，请稍后再试或检查后端是否已配置大模型 API Key。'
      })
    }
  } catch (_) {
    messages.value.push({ role: 'assistant', text: '网络错误，请检查网络或后端服务。' })
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.ai-fab-root {
  position: fixed;
  right: 20px;
  bottom: 88px;
  z-index: 50;
}
.ai-fab {
  cursor: pointer;
  border: none;
  border-radius: 999px;
  padding: 12px 16px;
  font-size: 14px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, #0369a1 0%, #0ea5e9 100%);
  box-shadow: 0 8px 24px rgba(3, 105, 161, 0.35);
  font-family: var(--font-body, system-ui, sans-serif);
}
.ai-fab:hover {
  filter: brightness(1.05);
}
.ai-intro {
  font-size: 12px;
  color: #64748b;
  margin: -8px 0 12px;
  line-height: 1.5;
}
.ai-scroll {
  height: calc(100vh - 220px);
  max-height: 520px;
  padding-right: 4px;
}
.ai-msg {
  margin-bottom: 14px;
  padding: 10px 12px;
  border-radius: 10px;
  line-height: 1.55;
  font-size: 14px;
}
.ai-msg.user {
  background: #e0f2fe;
  margin-left: 12px;
}
.ai-msg.assistant {
  background: #f1f5f9;
  margin-right: 12px;
}
.ai-msg-label {
  font-size: 11px;
  color: #64748b;
  margin-bottom: 4px;
  font-weight: 600;
}
.ai-msg-text {
  white-space: pre-wrap;
  word-break: break-word;
}
.ai-msg-text.muted {
  color: #94a3b8;
  font-style: italic;
}
.ai-input-row {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
@media (max-width: 640px) {
  .ai-fab-root {
    right: 12px;
    bottom: 76px;
  }
}
</style>
