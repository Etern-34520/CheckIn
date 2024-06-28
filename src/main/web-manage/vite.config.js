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
    base: '/checkIn/',
    optimizeDeps: {
        include: ['@kangc/v-md-editor/lib/theme/vuepress.js'],
    },
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
        ElementPlus({})
    ],
    resolve: {
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url))
        }
    },
    build: {
        outDir: "../resources/static/view/manage",
    },
/*
    server: {
        proxy: {
            '/checkIn/login/': {
                target: 'http://localhost:8080/checkIn/login',
                changeOrigin: true,
                rewrite: (path) => path.replace(/^\/checkIn\/login/, '')
            }
        }
    }
*/
})