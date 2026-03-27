import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

/** 开发 / preview 共用，否则 npm run preview 请求 /user 等会 404 */
const devProxy = {
  '/api': {
    target: 'http://127.0.0.1:8080',
    changeOrigin: true,
    secure: false
  },
  '/user': {
    target: 'http://127.0.0.1:8080',
    changeOrigin: true,
    secure: false
  },
  '/book': {
    target: 'http://127.0.0.1:8080',
    changeOrigin: true,
    secure: false
  },
  '/admin': {
    target: 'http://127.0.0.1:8080',
    changeOrigin: true,
    secure: false
  },
  '/common': {
    target: 'http://127.0.0.1:8080',
    changeOrigin: true,
    secure: false
  }
}

export default defineConfig({
  plugins: [vue()],
  server: {
    host: true,
    port: 5173,
    strictPort: true,
    // 内网穿透给外部用户演示时，为避免 HMR WebSocket 在代理链路中反复断开导致页面频闪刷新，直接关闭 HMR。
    hmr: false,
    // 兼容 Vite 的 Host 检查（不同版本字段略有差异；未知字段会被忽略）
    allowedHosts: ['rrniq3d7wg.fy.takin.cc'],
    proxy: devProxy
  },
  preview: {
    proxy: devProxy
  }
})
