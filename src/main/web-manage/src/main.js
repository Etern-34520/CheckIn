import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
import VueCookies from 'vue3-cookies'
import axios from './api/axios'
import router from './router'
import 'element-plus/theme-chalk/dark/css-vars.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

const app = createApp(App)
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}
app.use(router).use(VueCookies);
app.config.globalProperties.$http = axios
/*
app.config.warnHandler = (msg, vm, trace) => {
};
*/

app.mount('#app')