<template>
  <div class="wrs">
    <el-result
      icon="success"
      title="充值成功"
      sub-title="钱包余额已更新，可在「个人中心」查看余额与流水记录"
    >
      <template #extra>
        <el-button type="primary" @click="goProfile">查看个人中心</el-button>
        <el-button @click="goHome">返回首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
const goProfile = () => router.push('/profile')
const goHome = () => router.push('/')
</script>

<style scoped>
.wrs {
  max-width: 960px;
  margin: 0 auto;
  padding: 24px;
  background: #fff;
  border-radius: 12px;
}
</style>
