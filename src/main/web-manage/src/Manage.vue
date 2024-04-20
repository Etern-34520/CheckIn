<script setup>
import {RouterLink, RouterView} from 'vue-router'
import TopBar from "@/components/TopBar.vue";
import router from "@/router/index.js";
import topBar from "@/components/TopBar.vue";
import {ref, reactive, getCurrentInstance, onBeforeMount} from 'vue'
import SideMenu from "@/components/SideMenu.vue";

const {proxy} = getCurrentInstance();

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
        if (name !== undefined) {
            breadcrumbPathArray.push({path: absolutePath, name: name});
        } else if (notIgnored) {
            breadcrumbPathArray.push({path: absolutePath, name: toPathItem});
        }
    }
};
router.afterEach(updateBreadcrumbArray);

let userObj = {
    qq: proxy.$cookies.get("qq"),
    name: proxy.$cookies.get("name"),
};

const user = reactive(userObj);

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
.routePage-enter-active {
    transition: all 0.4s ease-out;
}

.routePage-leave-active {
    transition: all 0.4s ease-in;
}

.routePage-enter-from, .routePage-leave-to {
    opacity: 0;
    scale: 0.95;
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