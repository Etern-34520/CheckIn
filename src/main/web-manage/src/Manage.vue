<script setup>
import TopBar from "@/components/common/TopBar.vue";
import router from "@/router/index.js";
import SideMenu from "@/components/common/SideMenu.vue";
import UserDataInterface from "@/data/UserDataInterface.js";
import {Loading} from "@element-plus/icons-vue";

onBeforeMount(() => {
    updateBreadcrumbArray({path: window.location.pathname});
});

const menuInlineStyle = ref(false);

const breadcrumbMap = {
    "traffic": "流量",
    "questions": "题库",
    "manage-user": "用户管理",
    "manage-group": "组管理",
    "global-setting": "服务器设置",
    "user-setting": "用户设置",
    "user-questions": "用户题目",
    "my-data": "我的",
    "verification-setting": "上传校验设置",
    "facade-setting": "首页设置",
    "drawing-setting": "抽取设置"
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
        <div id="manageBase">
            <side-menu v-model:inlineStyle="menuInlineStyle" :user="user"/>
            <router-view class="manage-page-router" v-slot="{ Component }">
                <transition name="routePage" mode="out-in">
                    <component :is="Component"/>
                </transition>
            </router-view>
        </div>
    </div>
</template>

<style scoped>
/*noinspection CssUnusedSymbol*/
/*.routePage-enter-active {
    transition: all 0.8s var(--ease-in-bounce-1);
}*/

/*noinspection CssUnusedSymbol*/
.routePage-enter-active {
    transition: all 0.4s var(--ease-out-quint);
}

/*noinspection CssUnusedSymbol*/
.routePage-leave-active {
    transition: all 0.4s var(--ease-in-quint);
}

/*noinspection CssUnusedSymbol*/
.routePage-enter-from, .routePage-leave-to {
    filter: blur(32px);
    opacity: 0;
}

/*noinspection CssUnusedSymbol*/
.routePage-enter-from {
    scale: 0.95;
}

/*noinspection CssUnusedSymbol*/
.routePage-leave-to {
    scale: 1.05;
}

#manageBase {
    display: flex;
    flex-direction: row;
    align-items: stretch;
    justify-items: stretch;
    flex: 1;
    height: 0;
}

#manageBase > *:nth-of-type(2) {
    flex: 1;
}
</style>