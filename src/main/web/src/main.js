import './assets/main.css';

import {createApp} from 'vue';
import App from './App.vue';
import VueCookies from 'vue3-cookies';
import axios from './api/axios';
import router from './router';
import 'element-plus/theme-chalk/dark/css-vars.css';
import * as ElementPlusIconsVue from '@element-plus/icons-vue';
import _Loading_ from "@/components/common/_Loading_.vue";

import '@/utils/UI_Meta.js';

import {config} from 'md-editor-v3';
import mermaid from 'mermaid';
import katex from 'katex';
import hljs from 'highlight.js';
import Cropper from 'cropperjs';
import screenfull from 'screenfull';
import * as prettier from 'prettier';
import parserMarkdown from 'prettier/plugins/markdown';
import UI_Meta from "@/utils/UI_Meta.js";

const app = createApp(App);

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component);
}
app.config.globalProperties.$http = axios;
app.component("Loading_", _Loading_);
app.use(router).use(VueCookies);
app.mount('#app');

mermaid.initialize({
    theme: UI_Meta.colorScheme.value === 'dark' ? 'dark' : 'default',
})
config({
    editorExtensions: {
        mermaid: { instance: mermaid, enableZoom: true },
        katex: {instance: katex},
        highlight: {instance: hljs},
        cropper: {instance: Cropper},
        screenfull: {instance: screenfull},
        prettier: {
            prettierInstance: prettier,
            parserMarkdownInstance: parserMarkdown
        }
    }
});
