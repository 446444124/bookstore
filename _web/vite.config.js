import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    host: true,
    port: 5174,
    strictPort: true,
    allowedHosts: ['rrniq3d7wg.fy.takin.cc'],
    hmr: {
      host: 'rrniq3d7wg.fy.takin.cc',
      protocol: 'wss'
    },
    proxy: {
      // 统一管理端接口，避免新增 /admin/xxx 时漏配代理导致 404
      '/admin': {
        target: 'http://127.0.0.1:8090',
        changeOrigin: true,
        secure: false
      }
    }
  }
})
