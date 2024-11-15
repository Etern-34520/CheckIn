<script setup>
import router from "@/router/index.js";
// import {getCurrentInstance,ref} from "vue";
import HarmonyOSIcon_Quit from "@/components/icons/HarmonyOSIcon_Quit.vue";
import getAvatarUrlOf from "@/utils/Avatar.js";
import UserDataInterface from "@/data/UserDataInterface.js";

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

function routeTo(path) {
    router.push('/manage/' + path);
    shrinkMenu();
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
        <div id="menu" class="container"
             :class="{'menu-inline': inlineBool,'menu-expand': expandBool || inlineBool}" @mouseenter="expandMenu"
             @mouseleave="shrinkMenu" @click.stop>
            <el-scrollbar view-style="overflow-x: hidden;">
                <div class="menu-group" v-for="(group,$index) in pageGroups">
                    <el-button v-for="pageItem of group.paths" text
                               @click="routeTo(pageItem.path)"
                               @mouseenter="expandMenu">
                        <el-icon :size="16" style="margin-right: 12px;margin-left: 2px;">
                            <Component :is="pageItem.icon"/>
                        </el-icon>
                        {{ pageItem.name }}
                    </el-button>
                    <!--                <el-divider v-if="$index!==pageGroups.length-1" style="margin-top: 2px;margin-bottom: 2px"></el-divider>-->
                </div>
            </el-scrollbar>
            <div style="flex: 1"></div>
            <div style="display: flex;flex-direction: row" class="default-hidden-menu">
                <el-button-group style="display:flex;flex-direction: row;align-items: stretch">
                    <el-button id="menu-avatar-button" @click="routeTo('user-setting/')" text
                               style="width: 150px;height: 52px;display: flex;flex-direction: row;padding: 4px;">
                        <div>
                            <el-avatar shape="circle" size="default"
                                       :src="getAvatarUrlOf(user.qq)"
                                       style="margin-right: 4px"></el-avatar>
                        </div>
                        <div style="text-align: left;flex: 1;overflow: hidden;">
                            <el-text size="large" style="display: block">{{ user.name }}</el-text>
                            <el-text size="large" type="info" style="display: block">{{ user.qq }}</el-text>
                        </div>
                    </el-button>
                    <el-button style="width: 50px;height: 52px" @click="UserDataInterface.logout()" text>
                        <div style="display: flex;flex-direction: column">
                            <el-icon size="20" style="align-self:center">
                                <HarmonyOSIcon_Quit/>
                            </el-icon>
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
    margin-right: 46px;
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
    border: 1px solid rgba(0, 0, 0, 0) !important;
    position: absolute;
    height: calc(100% - 44px);
    width: 36px;
    padding: 0 4px 4px;
    transition: all 300ms var(--ease-in-bounce-1);
    transition-delay: 100ms;
    overflow-x: hidden;
    overflow-y: auto;
    /*border-right-color: var(--el-border-color-lighter) !important;*/
    border-radius: 0;
    z-index: 2001;
}

.menu-group {
    display: flex;
    flex-direction: column;
    transition: margin-bottom 200ms var(--ease-in-out-quint);
    margin-bottom: 12px;
}

.menu-group button {
    transition: background var(--ease-in-out-quint) 300ms, padding var(--ease-in-out-quint) 500ms !important;
    display: flex;
    flex-direction: row;
    justify-content: flex-start;
    margin: 0;
    padding: 8px;
    height: 36px;
    overflow: hidden;
}

.menu-expand .menu-group button {
    transition: background var(--ease-in-out-quint) 300ms, padding var(--ease-in-out-quint) 500ms 800ms !important;
    padding-left: 16px;
}

.menu-inline .menu-group button {
    transition: background var(--ease-in-out-quint) 300ms, padding var(--ease-in-out-quint) 400ms !important;
}

/*.menuGroup .current {
    background: var(--panel-bg-color);
}*/

#menu button {
    transition: 0.2s var(--ease-in-out-quint);
}

/*noinspection CssUnusedSymbol*/
.mask-enter-active {
    transition: all 150ms var(--ease-in-out-quint);
}

/*noinspection CssUnusedSymbol*/
.mask-leave-active {
    transition: all 150ms var(--ease-in-out-quint);
}

/*noinspection CssUnusedSymbol*/
.mask-enter-from, .mask-leave-to {
    opacity: 0;
}

.menu-expand {
    background: var(--menu-bg-color-alpha) !important;
    width: 200px !important;
    backdrop-filter: blur(32px);
    transition-delay: 900ms !important;
    box-shadow: var(--menu-shadow);
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

.default-hidden-menu {
    opacity: 0;
    transition: transform 200ms ease-in-out 0ms, all 300ms var(--ease-in-out-quint) 0ms;
    transform: translateX(-100px);
}

.menu-expand .default-hidden-menu {
    opacity: 1;
    transition: transform 150ms var(--ease-out-quint) 0ms, all 300ms var(--ease-in-out-quint) 900ms;
    transform: translateX(0);
}

.menu-inline .default-hidden-menu {
    opacity: 1;
    transition: transform 150ms var(--ease-in-quint), all 300ms var(--ease-in-out-quint);
    transform: translateX(0);
}
</style>
<style>
.menu-group .el-button .el-icon {
    margin-left: 1px;
    margin-right: 5px;
}
</style>