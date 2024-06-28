import './assets/main.css'

import {createApp} from 'vue'
import App from './App.vue'
import VueCookies from 'vue3-cookies'
import axios from './api/axios'
import router from './router'
import 'element-plus/theme-chalk/dark/css-vars.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import _Loading_ from "@/components/common/_Loading_.vue";

import VMdPreview from '@kangc/v-md-editor/lib/preview';
import VueMarkdownEditor from '@kangc/v-md-editor';
import '@kangc/v-md-editor/lib/style/base-editor.css';
import '@/assets/vuePressDark.css';
import '@/assets/vuePressLight.css';
// noinspection JSFileReferences
import vuepressTheme from '@kangc/v-md-editor/lib/theme/vuepress.js';
import hljs from 'highlight.js';
import Prism from 'prismjs';
// import createTipPlugin from "@kangc/v-md-editor/es/plugins/tip/index.js";
// import createKatexPlugin from '@kangc/v-md-editor/lib/plugins/katex/cdn.js';
import createMermaidPlugin from '@kangc/v-md-editor/lib/plugins/mermaid/cdn.js';
import '@kangc/v-md-editor/lib/plugins/mermaid/mermaid.css';

VueMarkdownEditor.use(vuepressTheme, {
    Prism,
});

VMdPreview.use(vuepressTheme, {
    Hljs: hljs,
    Prism,
});


// VueMarkdownEditor.use(createKatexPlugin());
// noinspection JSValidateTypes
VueMarkdownEditor.use(createMermaidPlugin());

// VMdPreview.use(createKatexPlugin());
// noinspection JSValidateTypes
VMdPreview.use(createMermaidPlugin());

// VueMarkdownEditor.use(createTipPlugin());
const app = createApp(App)
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}
app.component("_Loading_", _Loading_);
app.use(router).use(VueCookies);
app.config.globalProperties.$http = axios
app.mount('#app');
app.use(VMdPreview);
app.use(VueMarkdownEditor);