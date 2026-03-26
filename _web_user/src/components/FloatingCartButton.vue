<template>
  <div class="cart-fab">
    <el-button circle type="primary" size="large" aria-label="打开购物车" @click="goCart">
      <svg class="cart-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
        <circle cx="9" cy="21" r="1" />
        <circle cx="20" cy="21" r="1" />
        <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 1.94-1.51L23 6H6" />
      </svg>
    </el-button>
    <span v-if="count > 0" class="badge" aria-live="polite">{{ count }}</span>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { cartCount as count, refreshCartCount } from '../stores/cart'
const router = useRouter()
const goCart = () => {
  const tk = localStorage.getItem('token') || ''
  if (!tk) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  router.push('/cart')
}
onMounted(() => {
  refreshCartCount()
})
</script>

<style>
.cart-fab {
  position: fixed;
  right: 20px;
  bottom: 20px;
  z-index: 1000;
}
.cart-fab :deep(.el-button) {
  cursor: pointer;
}
.cart-icon {
  width: 26px;
  height: 26px;
  display: block;
}
.cart-fab .el-button.is-circle {
  width: 56px;
  height: 56px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}
.cart-fab .badge {
  position: absolute;
  right: 10px;
  top: 4px;
  min-width: 18px;
  height: 18px;
  padding: 0 4px;
  border-radius: 9px;
  background: #ef4444;
  color: #fff;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 0 0 2px #fff;
}
</style>
