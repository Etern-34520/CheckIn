<script setup>
import TopBar from "@/components/common/TopBar.vue";
import router from "@/router/index.js";
import SideMenu from "@/components/common/SideMenu.vue";
import UserDataInterface from "@/data/UserDataInterface.js";
import {Loading} from "@element-plus/icons-vue";
import LoginDialog from "@/components/common/LoginDialog.vue";

onBeforeMount(() => {
    updateBreadcrumbArray({path: window.location.pathname});
});

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
    "account": "用户设置",
    "user-questions": "用户题目",
    "my-data": "我的",
    "verification-setting": "上传校验设置",
    "facade-setting": "首页设置",
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
onUnmounted(() => {
    stop();
});

const user = UserDataInterface.getCurrentUser();

</script>

<template>
    <div id="manageMain" style="display: flex;flex-direction: column;flex: 1;width: 100%;height: 100%">
        <top-bar v-model:menu-inline-style="menuInlineStyle" :user="user"
                 :breadcrumb-path-array="breadcrumbPathArray"></top-bar>
        <div id="manage-base">
            <side-menu v-model:inlineStyle="menuInlineStyle" :user="user"/>
            <router-view class="manage-page-router" v-slot="{ Component }">
                <transition name="route-page" mode="out-in">
                    <component :is="Component"/>
                </transition>
            </router-view>
        </div>
    </div>
</template>

<style scoped>
/*noinspection CssUnusedSymbol*/
.route-page-enter-active {
    transition: all 0.4s var(--ease-out-quint);
}

/*noinspection CssUnusedSymbol*/
.route-page-leave-active {
    transition: all 0.4s var(--ease-in-quint);
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