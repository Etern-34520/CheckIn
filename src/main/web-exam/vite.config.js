import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import VueDevTools from 'vite-plugin-vue-devtools'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import ElementPlus from 'unplugin-element-plus/vite'
import { compression } from 'vite-plugin-compression2'

// https://vitejs.dev/config/
export default defineConfig({
  server: {
    port: 5172, host: '0.0.0.0', // 配置项目可以局域网访问
    cors: true, // 默认启用并允许任何源
    proxy: {
      '/checkIn/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  base: '/checkIn/',
  plugins: [
    vue(),
    VueDevTools(),
    AutoImport({
      imports: ['vue', 'vue-router'],
      resolvers: [ElementPlusResolver()],
    }),
    Components({
      resolvers: [ElementPlusResolver()],
    }),
    ElementPlus({}),
    compression({
      algorithms: ['gzip'],
      deleteOriginalAssets: true,
      threshold: 800
    })
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  build: {
    outDir:"../resources/static/view/exam",
    emptyOutDir: true,
  }
})