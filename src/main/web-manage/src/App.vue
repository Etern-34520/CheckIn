<script setup>
// import {RouterView} from "vue-router";
import WebSocketConnector from "@/api/websocket.js";
import Router from "@/router/index.js";
import LoginDialog from "@/components/common/LoginDialog.vue";
import UserDataInterface from "@/data/UserDataInterface.js";
import {ElMessage, ElMessageBox} from "element-plus";
import router from "@/router/index.js";
// import {getCurrentInstance, onMounted} from 'vue';

const {proxy} = getCurrentInstance();
const showReLogin = ref(false);

UserDataInterface.onLoginFailed = (err) => {
    if (err === "token expired") {
        showReLogin.value = true;
    } else {
        if (router.currentRoute.value.name !== "login")
            showReLogin.value = true;
        ElMessage({
            type: "error",
            message: "登录失败",
        });
    }
}
UserDataInterface.onLoginSucceed = () => {
    showReLogin.value = false;
}

onMounted(() => {
    if (proxy.$cookies.get("token") !== null) {
        const name = proxy.$cookies.get("name", "/checkIn");
        const qq = proxy.$cookies.get("qq", "/checkIn");
        const token = proxy.$cookies.get("token", "/checkIn");
        const user = {name: name, qq: Number(qq), token: token};
        console.log(user);
        UserDataInterface.loginAs(user);
    } else {
        Router.push({name: "login"});
    }
});
</script>

<template>
    <el-dialog :model-value="showReLogin" :width="400" align-center :show-close="false" draggable
               :close-on-click-modal="false" :close-on-press-escape="false">
        <template #header>
            登录已过期
        </template>
        <template #default>
            <el-text size="large">重新登录</el-text>
            <login-dialog style="margin-top: 16px" primary/>
        </template>
    </el-dialog>
    <router-view v-slot="{ Component }">
        <transition name="main-router" mode="out-in" duration="1000">
            <component :is="Component"/>
        </transition>
    </router-view>
</template>

<style scoped>
/*noinspection CssUnusedSymbol*/
.main-router-enter-active, .main-router-leave-active {
    transition: opacity 0.4s;
}

/*noinspection CssUnusedSymbol*/
.main-router-enter-from, .main-router-leave-to {
    opacity: 0;
}

/*noinspection CssUnusedSymbol*/
.main-router-enter-to, .main-router-leave-from {
    opacity: 1;
}
</style>