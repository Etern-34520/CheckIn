import  './assets/base.css';
import './assets/main.css';
import 'element-plus/theme-chalk/dark/css-vars.css';
import VueCookies from 'vue3-cookies';
import * as ElementPlusIconsVue from '@element-plus/icons-vue';

import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import axios from "./axios";

import "@/UI_Meta.js";
const app = createApp(App);
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component);
}
app.use(router).use(VueCookies);
app.config.globalProperties.$http = axios;
app.mount('#app');