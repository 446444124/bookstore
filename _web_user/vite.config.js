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
    proxy: devProxy
  },
  preview: {
    proxy: devProxy
  }
})
