import {fileURLToPath, URL} from 'node:url'

import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'
import VueDevTools from 'vite-plugin-vue-devtools'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import {ElementPlusResolver} from 'unplugin-vue-components/resolvers'
import ElementPlus from 'unplugin-element-plus/vite'

// https://vitejs.dev/config/
export default defineConfig({
    server: {
        port: 5173, host: '0.0.0.0', // 配置项目可以局域网访问
        cors: true, // 默认启用并允许任何源
        proxy: {
            '/checkIn/login/api': {
                target: 'http://localhost:8080',
                changeOrigin: true
                // rewrite: (path) => path.replace(/^\/checkIn\/login\/api/, '')
            },
            "/checkIn/api/websocket/": {
                target: "http://localhost:8080",
                changeOrigin: true,
                ws: true
            }
        }
    }, base: '/checkIn/'
    , plugins: [vue(), VueDevTools(), AutoImport({
        imports: ['vue', 'vue-router'], resolvers: [ElementPlusResolver()],
    }), Components({
        resolvers: [ElementPlusResolver()],
    }), ElementPlus({})], resolve: {
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url))
        }
    }, build: {
        outDir: "../resources/static/view/manage",
        emptyOutDir: true,
    }
})