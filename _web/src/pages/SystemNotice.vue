<template>
  <el-card class="notice-card">
    <div class="head">
      <div class="title">系统公告</div>
      <div class="sub">配置用户端进入商城时的弹窗公告内容。</div>
    </div>

    <el-form label-width="120px" class="form" @submit.prevent>
      <el-form-item label="启用弹窗">
        <el-switch v-model="enabled" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="停用" />
      </el-form-item>
      <el-form-item label="标题">
        <el-input v-model="title" maxlength="128" show-word-limit placeholder="例如：系统公告 / 重要通知" />
      </el-form-item>
      <el-form-item label="内容">
        <el-input
          v-model="content"
          type="textarea"
          :rows="10"
          maxlength="4000"
          show-word-limit
          placeholder="支持换行；建议写清楚活动/维护时间/规则说明等"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
        <el-button :loading="loading" @click="load">刷新</el-button>
      </el-form-item>
    </el-form>

    <el-divider content-position="left">预览</el-divider>
    <div class="preview">
      <div class="pv-title">{{ (title || '').trim() || '系统公告' }}</div>
      <div class="pv-content">{{ (content || '').trim() || '（暂无内容）' }}</div>
      <div class="pv-tip" v-if="enabled === 1">当前为启用状态：用户进入商城首页将弹出公告。</div>
      <div class="pv-tip" v-else>当前为停用状态：用户端不会弹出公告。</div>
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { http } from '../api/http'

const loading = ref(false)
const saving = ref(false)

const enabled = ref(0)
const title = ref('')
const content = ref('')

const load = async () => {
  loading.value = true
  try {
    const resp = await http('/admin/systemNotice', { method: 'GET', json: false })
    if (resp && Number(resp.code) === 1) {
      enabled.value = resp.data?.enabled ?? 0
      title.value = resp.data?.title ?? ''
      content.value = resp.data?.content ?? ''
    } else {
      ElMessage.error(resp?.msg || '加载失败')
    }
  } catch (_) {
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}

const save = async () => {
  saving.value = true
  try {
    const resp = await http('/admin/systemNotice', {
      method: 'PUT',
      body: { enabled: enabled.value, title: title.value, content: content.value }
    })
    if (resp && Number(resp.code) === 1) {
      ElMessage.success('已保存')
      load()
    } else {
      ElMessage.error(resp?.msg || '保存失败')
    }
  } catch (_) {
    ElMessage.error('网络错误')
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style>
.notice-card { min-height: 92vh; }
.head { display: flex; flex-direction: column; gap: 6px; margin-bottom: 8px; }
.title { font-weight: 700; font-size: 18px; color: #0f172a; }
.sub { color: var(--admin-sub); font-size: 13px; }
.form { max-width: 860px; }
.preview {
  max-width: 860px;
  border: 1px solid var(--admin-border);
  border-radius: 12px;
  padding: 14px 16px;
  background: color-mix(in srgb, var(--admin-surface) 92%, #fff);
}
.pv-title { font-weight: 800; font-size: 16px; color: #0f172a; margin-bottom: 8px; }
.pv-content { white-space: pre-wrap; color: #334155; line-height: 1.6; font-size: 14px; }
.pv-tip { margin-top: 10px; color: var(--admin-sub); font-size: 12px; }
</style>

