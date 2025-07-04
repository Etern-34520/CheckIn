import {fileURLToPath, URL} from 'node:url'

import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'
import VueDevTools from 'vite-plugin-vue-devtools'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import {ElementPlusResolver} from 'unplugin-vue-components/resolvers'
import ElementPlus from 'unplugin-element-plus/vite'
import { VitePWA } from 'vite-plugin-pwa'

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
        VitePWA({
            manifest: {
                "name": 'CheckIn Manage',
                "description": "CheckIn 后台管理",
                // "theme_color": "rgba(0,0,0,0)",

                icons: [
                    {
                        "src": "icons/icon-accent-light.svg",
                        "sizes": "192x192",
                        "type": "image/svg+xml"
                    },
                    {
                        "src": "icons/icon-accent-light.svg",
                        "sizes": "512x512",
                        "type": "image/svg+xml"
                    }
                ]
            },
            workbox: {
                runtimeCaching: [
                    {
                        urlPattern: /(.*?)\.(js|css|ts|html)/, // js /css /ts静态资源缓存
                        handler: 'CacheFirst',
                        options: {
                            cacheName: 'main-cache',
                        },
                    },
                    {
                        urlPattern: /(.*?)\.(png|jpe?g|svg|gif|bmp|psd|tiff|tga|eps)/, // 图片缓存
                        handler: 'CacheFirst',
                        options: {
                            cacheName: 'image-cache',
                        },
                    },
                ],
            },
            includeAssets: ['src/assets/icons/*.svg'],
            devOptions: {
                enabled: true,
                type: "module"
            }
        })
    ], resolve: {
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url))
        }
    }, build: {
        outDir: "../resources/static/view/manage",
        emptyOutDir: true,
    }
})