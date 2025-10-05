import {fileURLToPath, URL} from 'node:url'

import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'
import VueDevTools from 'vite-plugin-vue-devtools'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import {ElementPlusResolver} from 'unplugin-vue-components/resolvers'
import ElementPlus from 'unplugin-element-plus/vite'
import {VitePWA} from 'vite-plugin-pwa'

const appVersion = process.env.npm_package_version;

const target = "http://localhost:8080";
export default defineConfig({
    server: {
        port: 5173, host: '0.0.0.0', // 配置项目可以局域网访问
        cors: true, // 默认启用并允许任何源
        proxy: {
            '/checkIn/api': {
                target: target,
                changeOrigin: true
            },
            "/checkIn/api/websocket/": {
                target: target,
                changeOrigin: true,
                ws: true
            },
            "/checkIn/icons": {
                target: target,
                changeOrigin: true
            }
        },
        allowedHosts: ["test-checkin.com"]
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
                name: 'CheckIn',
                description: "CheckIn",
                lang: 'zh-CN',
                id: "CheckIn-" + appVersion,
                registerType: "autoUpdate",
                injectRegister: 'auto',
                display: "minimal-ui",
                "display_override": ["window-controls-overlay"],
                icons: [
                    {
                        src: "icons/icon-accent-light_192.png",
                        sizes: "192x192",
                        type: "image/png",
                        purpose: "any"
                    }, {
                        src: "icons/icon-accent-light_512.png",
                        sizes: "512x512",
                        type: "image/png",
                        purpose: "any"
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
                navigateFallbackDenylist: [
                    /^\/checkIn\/api\/*/
                ]
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
        outDir: "../resources/static/view/front-face",
        emptyOutDir: true,
    }, define: {
        '__APP_VERSION__': JSON.stringify(appVersion),
    }
})