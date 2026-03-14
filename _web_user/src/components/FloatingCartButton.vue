<template>
  <div class="cart-fab" @click="goCart">
    <el-button circle type="primary" size="large">🛒</el-button>
    <span v-if="count > 0" class="badge">{{ count }}</span>
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
.cart-fab .el-button.is-circle {
  width: 56px;
  height: 56px;
  font-size: 22px;
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
