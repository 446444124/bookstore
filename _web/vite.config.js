import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    host: true,
    allowedHosts: ['rrniq3d7wg.fy.takin.cc'],
    hmr: {
      host: 'rrniq3d7wg.fy.takin.cc',
      protocol: 'wss'
    },
    proxy: {
      '/admin/admin': {
        target: 'http://127.0.0.1:8090',
        changeOrigin: true,
        secure: false
      },
      '/admin/book': {
        target: 'http://127.0.0.1:8090',
        changeOrigin: true,
        secure: false
      },
      '/admin/category': {
        target: 'http://127.0.0.1:8090',
        changeOrigin: true,
        secure: false
      },
      '/admin/common': {
        target: 'http://127.0.0.1:8090',
        changeOrigin: true,
        secure: false
      }
    }
  }
})
