<script setup>
import router from "@/router/index.js";
// import {getCurrentInstance,ref} from "vue";
import HarmonyOSIcon_Quit from "@/components/icons/HarmonyOSIcon_Quit.vue";
import getAvatarUrlOf from "@/utils/Avatar.js";
import UserDataInterface from "@/data/UserDataInterface.js";
import UI_Meta from "@/utils/UI_Meta.js";
import PermissionInfo from "@/auth/PermissionInfo.js";
import {Link} from "@element-plus/icons-vue";

const {proxy} = getCurrentInstance();

const pageGroups = [
    {
        groupName: "服务器",
        paths: [
            {pathName: 'home', name: '首页', icon: "House"},
            {pathName: 'exam-record', name: '答题记录', icon: "MessageBox"},
            {
                pathName: 'request-record', name: '请求记录', icon: "Link",
                show: PermissionInfo.hasPermission('request record', 'get request records')
            },
            {pathName: 'questions', name: '题库', icon: "Finished"}
        ]
    }, {
        groupName: "管理",
        paths: [
            {pathName: 'manage-user', name: '用户管理', icon: "User"},
            {pathName: 'manage-group', name: '组管理', icon: "Files"},
        ]
    }, {
        groupName: "设置",
        paths: [
            {pathName: 'global-setting-base', name: '服务器设置', icon: "Tools"},
            {pathName: 'account-base', name: '账户', icon: "SetUp"},
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
    if (!UI_Meta.mobile.value)
        expandBool.value = true;
}

function shrinkMenu() {
    if (!UI_Meta.mobile.value)
        expandBool.value = false;
}

function routeTo(name) {
    router.push({name: name});
    shrinkMenu();
    if (UI_Meta.mobile.value) {
        inlineBool.value = false;
    }
}

function routeToAccount() {
    router.push({name: 'account-base'});
    if (UI_Meta.mobile.value) {
        inlineBool.value = false;
    }
}

const props = defineProps({
    user: {
        type: Object,
        required: true,
    }
});

const unwatch = watch(() => UI_Meta.mobile.value, (value, oldValue, onCleanup) => {
    if (value && expandBool.value && !inlineBool.value) {
        expandBool.value = false;
    }
});

</script>

<template>
    <div id="menu-container" :class="{'menu-container-inline': inlineBool && (!UI_Meta.mobile.value)}">
        <div style="position: absolute;width: 100dvw;height: calc(100dvh - 32px);"
             @click="inlineBool = false" v-if="inlineBool && (UI_Meta.mobile.value)"></div>
        <div id="menu" class="container"
             :class="{'menu-inline': inlineBool,'menu-expand': expandBool || inlineBool}"
             @mouseenter="expandMenu" @mouseleave="shrinkMenu" @click.stop>
            <el-scrollbar style="margin-bottom: 8px;" view-style="overflow-x: hidden;">
                <div class="menu-group" v-for="(group,$index) in pageGroups">
                    <template v-for="pageItem of group.paths">
                        <el-button text v-if="pageItem.show === undefined?true:pageItem.show"
                                   @click="routeTo(pageItem.pathName)"
                                   @mouseenter="expandMenu">
                            <el-icon :size="16" style="margin-right: 12px;margin-left: 2px;">
                                <Component :is="pageItem.icon"/>
                            </el-icon>
                            {{ pageItem.name }}
                        </el-button>
                    </template>
                </div>
            </el-scrollbar>
            <div style="flex: 1"></div>
            <div style="display: flex;flex-direction: column;" class="default-hidden-menu">
                <el-button-group style="display:flex;flex-direction: row;align-items: stretch">
                    <el-button style="width: 100px;height: 32px" @click="UserDataInterface.logout();router.push({name:'facade'})" text>
                        <div style="display: flex;flex-direction: row">
                            <el-icon size="16" style="align-self:center;margin-right: 8px;">
                                <Link />
                            </el-icon>
                            <div style="align-self:center">
                                前往答题
                            </div>
                        </div>
                    </el-button>
                    <el-button style="width: 100px;height: 32px" @click="UserDataInterface.logout()" text>
                        <div style="display: flex;flex-direction: row">
                            <el-icon size="16" style="align-self:center;margin-right: 8px;">
                                <HarmonyOSIcon_Quit/>
                            </el-icon>
                            <div style="align-self:center">
                                退出
                            </div>
                        </div>
                    </el-button>
                </el-button-group>
                <el-button id="menu-avatar-button" @click="routeToAccount" text
                           style="width: 200px;display: flex;flex-direction: row;height: 40px;padding: 4px;">
                    <el-avatar shape="circle" :size="32"
                               :src="getAvatarUrlOf(user.qq)"
                               style="margin-left: 2px;margin-right: 8px;"></el-avatar>
                    <div style="text-align: left;flex: 1;overflow: hidden;">
                        <el-text size="large" style="display: block">{{ user.name }}</el-text>
                        <el-text size="large" type="info" style="display: block">{{ user.qq }}</el-text>
                    </div>
                </el-button>
            </div>
        </div>
        <div class="mobile-menu-mask" v-if="inlineBool" @click="inlineBool = false;"></div>
    </div>
</template>

<style scoped>
#menu-container {
    transition: all 300ms var(--ease-in-bounce);
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
    transition: all 300ms var(--ease-in-bounce);
    transition-delay: 100ms;
    overflow-x: hidden;
    overflow-y: auto;
    /*border-right-color: var(--el-border-color-lighter) !important;*/
    border-radius: 0;
    z-index: 2005;
}

.mobile {
    #menu-container {
        margin-right: 0;
    }
    #menu:not(.menu-expand) {
        width: 0;
    }
    .mobile-menu-mask {
        display: block;
        position: absolute;
        top: 38px;
        left: 0;
        width: 100dvw;
        height: calc(100dvh - 38px);
        z-index: 2004;
    }
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
    transition-delay: 200ms !important;
    box-shadow: none;
    /*border-right-color: var(--el-border-color-lighter) !important;*/
    border-radius: 0;
}

.default-hidden-menu {
    opacity: 0;
    transition: all 300ms var(--ease-in-out-quint) 0ms;
    transform: translateX(-100px);
}

.menu-expand .default-hidden-menu {
    opacity: 1;
    transition: all 300ms var(--ease-out-quint) 900ms;
    transform: translateX(0);
}

.menu-inline .default-hidden-menu {
    opacity: 1;
    transition: 300ms var(--ease-out-quint) 200ms;
    transform: translateX(0);
}
</style>
<style>
.menu-group .el-button .el-icon {
    margin-left: 1px;
    margin-right: 5px;
}
</style>