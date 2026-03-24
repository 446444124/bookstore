<template>
  <div class="ps">
    <el-result icon="success" title="支付成功" sub-title="订单已支付成功，可在“我的订单”查看最新状态">
      <template #extra>
        <el-button type="primary" @click="goOrders">查看我的订单</el-button>
        <el-button @click="goHome">返回首页</el-button>
      </template>
    </el-result>
  </div>
  </template>

<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
const goOrders = () => router.push('/orders')
const goHome = () => router.push('/')
</script>

<style>
.ps {
  max-width: 960px;
  margin: 0 auto;
  padding: 24px;
  background: #fff;
  border-radius: 12px;
}
</style>
