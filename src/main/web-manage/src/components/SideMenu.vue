<script setup>
import router from "@/router/index.js";
// import {getCurrentInstance,ref} from "vue";
import HarmonyOSIcon_Quit from "@/components/icons/HarmonyOSIcon_Quit.vue";

const {proxy} = getCurrentInstance();

const pageGroups = [
    {
        groupName: "服务器",
        paths: [
            {path: '', name: '首页', icon: "House"},
            {path: 'traffic/', name: '流量', icon: "Odometer"},
            {path: 'questions/', name: '题库', icon: "Finished"}
        ]
    }, {
        groupName: "管理",
        paths: [
            {path: 'manage-user/', name: '用户管理', icon: "User"},
            {path: 'manage-group/', name: '组管理', icon: "Files"},
        ]
    }, {
        groupName: "设置",
        paths: [
            {path: 'global-setting/', name: '服务器设置', icon: "Tools"},
            {path: 'user-setting/', name: '用户设置', icon: "SetUp"},
        ]
    }
]

const inlineBool = defineModel("inlineStyle", {
    type: Boolean,
    default: false
})

const expandBool = defineModel("expand", {
    type: Boolean,
    default: false
})

function expandMenu() {
    expandBool.value = true;
}

function shrinkMenu() {
    expandBool.value = false;
}

const currentGroupIndex = ref(0);
const currentButtonIndex = ref(0);

function routeTo(path) {
    router.push('/manage/' + path);
    shrinkMenu();
}

function logout() {
    proxy.$cookies.remove("token");
    proxy.$cookies.remove("name");
    proxy.$cookies.remove("qq");
    router.push('/login/');
}

const props = defineProps({
    user: {
        type: Object,
        required: true,
    }
})

</script>

<template>
    <div id="menu-container" :class="{'menu-container-inline': inlineBool}">
        <div id="menu" class="container border"
             :class="{'menu-inline': inlineBool,'menu-expand': expandBool || inlineBool}" @mouseenter="expandMenu"
             @mouseleave="shrinkMenu" @click.stop>
            <div class="menuGroup" v-for="(group,$index) in pageGroups">
                <el-button v-for="(pageItem,$buttonIndex) of group.paths" text :icon="pageItem.icon"
                           @click="routeTo(pageItem.path)"
                           @mouseenter="expandMenu">
                    {{ pageItem.name }}
                </el-button>
                <el-divider v-if="$index!==pageGroups.length-1" style="margin-top: 2px;margin-bottom: 2px"></el-divider>
            </div>
            <div style="flex: 1"></div>
            <div style="display: flex;flex-direction: row" class="default-hidden-menu">
                <el-button-group style="display:flex;flex-direction: row;align-items: stretch">
                    <el-button id="menu-avatar-button" @click="routeTo('user-setting/')" text
                               style="width: 150px;height: 52px;display: flex;flex-direction: row;padding: 4px;">
                        <div>
                            <el-avatar shape="circle" size="default"
                                       :src="'https://q1.qlogo.cn/g?b=qq&nk='+user.qq+'&s=100'"
                                       style="margin-right: 4px"></el-avatar>
                        </div>
                        <div style="text-align: left;flex: 1;overflow: hidden;">
                            <el-text size="large" style="display: block">{{ user.name }}</el-text>
                            <el-text size="large" type="info" style="display: block">{{ user.qq }}</el-text>
                        </div>
                    </el-button>
                    <el-button style="width: 50px;height: 52px" @click="logout" text>
                        <div style="display: flex;flex-direction: column">
                            <HarmonyOSIcon_Quit :size="20"/>
                            <div>
                                退出
                            </div>
                        </div>
                    </el-button>
                </el-button-group>
            </div>
        </div>
    </div>
</template>

<style scoped>
#menu-container {
    transition: all 300ms var(--ease-in-bounce-1);
    transition-delay: 100ms;
    margin-right: 44px;
}

.menu-container-inline {
    margin-right: 216px !important;
    transition-delay: 0ms !important;
}

#menu,
#menu > div {
    display: flex;
    flex-direction: column;
    align-items: stretch;
}

#menu {
    background: rgba(0, 0, 0, 0);
    border: 1px solid rgba(0, 0, 0, 0);
    position: absolute;
    height: calc(100% - 44px);
    width: 32px;
    padding: 4px;
    transition: all 300ms var(--ease-in-bounce-1);
    transition-delay: 100ms;
    overflow-x: hidden;
    overflow-y: auto;
    /*border-right-color: var(--el-border-color-lighter) !important;*/
    border-radius: 0;
    z-index: 114514;
}

.menuGroup {
    margin-bottom: 4px;
}

.menuGroup button {
    display: flex;
    flex-direction: row;
    justify-content: flex-start;
    margin: 0;
    padding: 8px;
    height: 32px;
    overflow: hidden;
}

/*.menuGroup .current {
    background: var(--panel-bg-color);
}*/

#menu button {
    transition: 0.2s ease-in-out;
}

#closeMenuButton {
    width: 32px;
    height: 32px;
    margin: -14px 0 0 -14px;
    padding: 8px;
    transition: all 0.2s ease-in-out;
}

/*noinspection CssUnusedSymbol*/
.mask-enter-active {
    transition: all 150ms ease-in-out;
}

/*noinspection CssUnusedSymbol*/
.mask-leave-active {
    transition: all 150ms ease-in-out;
}

/*noinspection CssUnusedSymbol*/
.mask-enter-from, .mask-leave-to {
    opacity: 0;
}

.menu-expand {
    background: var(--bg-color-alpha) !important;
    width: 200px !important;
    backdrop-filter: blur(32px);
    transition-delay: 500ms !important;
    box-shadow: 4px 0 32px rgba(0, 0, 0, 0.7);
    border-radius: 8px !important;
}

.menu-inline {
    background: rgba(0, 0, 0, 0) !important;
    width: 200px !important;
    /*backdrop-filter: blur(8px);*/
    transition-delay: 0ms !important;
    box-shadow: none;
    /*border-right-color: var(--el-border-color-lighter) !important;*/
    border-radius: 0;
}

#menu .default-hidden-menu {
    opacity: 0;
    transition: all 150ms ease-in-out;
}

.menu-expand .default-hidden-menu {
    opacity: 1 !important;
    transition-delay: 600ms !important;
}

.menu-inline .default-hidden-menu {
    opacity: 1 !important;
    transition-delay: 150ms !important;
}
</style>