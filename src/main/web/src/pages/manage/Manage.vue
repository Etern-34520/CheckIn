<script setup>
import TopBar from "@/components/common/TopBar.vue";
import router from "@/router/index.js";
import SideMenu from "@/components/common/SideMenu.vue";
import UserDataInterface from "@/data/UserDataInterface.js";
import PermissionInfo from "@/auth/PermissionInfo.js";

onBeforeMount(() => {
    updateBreadcrumbArray({path: window.location.pathname});
});

const showLoading = ref(false);

setTimeout(() => {
    showLoading.value = true;
}, 800);

const menuInlineStyle = ref(false);

const breadcrumbMap = {
    "request-record": "请求记录",
    "exam-record": "答题记录",
    "related-requests": "相关请求",
    "questions": "题库",
    "related-exams": "相关试题",
    "manage-user": "用户管理",
    "manage-group": "组管理",
    "global-setting": "服务器设置",
    "account": "账户",
    "user-questions": "用户题目",
    "user-exam-records": "用户答题记录",
    "my-data": "我的",
    "verification-setting": "上传校验设置",
    "facade-setting": "前台自定义",
    "generating-setting": "生成设置",
    "grading-setting": "评级设置",
    "advance-setting": "高级设置",
    "about": "关于"
}

let breadcrumbPathArray = reactive([]);

let ignoredPathItems = ["", "checkIn", "manage"];

let updateBreadcrumbArray = (to) => {
    breadcrumbPathArray.splice(0, breadcrumbPathArray.length);
    let absolutePath = "/manage/";
    for (let toPathItem of to.path.split("/")) {
        let notIgnored = !ignoredPathItems.includes(toPathItem);
        if (notIgnored) {
            absolutePath += toPathItem + "/";
        }
        let name = breadcrumbMap[toPathItem];
        if (name instanceof Function) {
            breadcrumbPathArray.push({path: absolutePath, name: name()});
        } else if (name !== undefined) {
            breadcrumbPathArray.push({path: absolutePath, name: name});
        } else if (notIgnored) {
            breadcrumbPathArray.push({path: absolutePath, name: decodeURI(toPathItem)});
        }
    }
};
const stop = router.afterEach(updateBreadcrumbArray);

const user = UserDataInterface.getCurrentUser();

const permissionLoaded = ref(false);
PermissionInfo.waitingForInitialize().then(() => {
    permissionLoaded.value = true;
});

onBeforeUnmount(() => {
    stop();
    UserDataInterface.logout();
})
</script>

<template>
    <div id="manageMain" style="display: flex;flex-direction: column;flex: 1;width: 100%;height: 100%">
        <top-bar v-model:menu-inline-style="menuInlineStyle" :user="user"
                 :breadcrumb-path-array="breadcrumbPathArray"></top-bar>
        <div id="manage-base">
            <side-menu v-model:inlineStyle="menuInlineStyle" v-if="permissionLoaded" :user="user"/>
            <router-view class="manage-page-router" v-slot="{ Component }">
                <transition name="route-page" mode="out-in">
                    <component :is="Component" v-if="permissionLoaded"/>
                    <div v-else-if="showLoading" class="global-loading">
                        <Loading_ style="width: 32px !important;"></Loading_>
                    </div>
                </transition>
            </router-view>
        </div>
    </div>
</template>

<style scoped>
/*noinspection CssUnusedSymbol*/
.route-page-enter-active {
    transition: all 0.4s var(--ease-out-quint) !important;
}

/*noinspection CssUnusedSymbol*/
.route-page-leave-active {
    transition: all 0.4s var(--ease-in-quint) !important;
}

/*noinspection CssUnusedSymbol*/
.route-page-enter-from, .route-page-leave-to {
    filter: blur(32px);
    opacity: 0;
}

/*noinspection CssUnusedSymbol*/
.route-page-enter-from {
    scale: 0.95;
}

/*noinspection CssUnusedSymbol*/
.route-page-leave-to {
    scale: 1.05;
}

#manage-base {
    display: flex;
    flex-direction: row;
    align-items: stretch;
    justify-items: stretch;
    flex: 1;
    height: 0;
}

#manage-base > *:nth-of-type(2) {
    flex: 1;
    width: 0;
}
</style>
<style>
.main-router-enter-active .manage-page-router {
    transition: all 0.6s var(--ease-in-bounce) !important;
    transition-delay: 0.4s;
}

.main-router-enter-from .manage-page-router {
    opacity: 0;
    transform: scale(0.95);
}

.global-loading {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    position: absolute;
    left: 0;
    top: 0;
    width: 100dvw;
    height: 100dvh;
}
</style>