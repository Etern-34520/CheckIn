import  './assets/base.css';
import './assets/main.css';
import 'element-plus/theme-chalk/dark/css-vars.css';
import "./UI_Meta";
import VueCookies from 'vue3-cookies';

import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import axios from "./axios";

const app = createApp(App);

app.use(router).use(VueCookies);
app.config.globalProperties.$http = axios;
app.mount('#app');