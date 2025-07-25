<script setup>
import WebSocketConnector from "@/api/websocket.js";
import Router from "@/router/index.js";
import LoginDialog from "@/components/common/LoginDialog.vue";
import UserDataInterface from "@/data/UserDataInterface.js";
import {ElMessage} from "element-plus";
import router from "@/router/index.js";
import PermissionInfo from "@/auth/PermissionInfo.js";

const {proxy} = getCurrentInstance();
const showReLogin = ref(false);
const reLoginText = ref("登录失败");

watch(() => UserDataInterface.logined.value, () => {
    WebSocketConnector.showGlobalNotifications = UserDataInterface.logined.value;
    if (UserDataInterface.logined.value) {
        showReLogin.value = false;
    }
})

function autoLogin() {
    return new Promise((resolve, reject) => {
        const user = proxy.$cookies.get("user", "/checkIn");
        if (user !== null) {
            console.debug("Login from cookie:", user);
            UserDataInterface.loginAs(user).then(() => {
                resolve();
            }, (err) => {
                if (err === "token expired") {
                    showReLogin.value = true;
                    reLoginText.value = "登录已过期"
                } else {
                    reLoginText.value = "登录失败";
                    if (router.currentRoute.value.name !== "login") {
                        showReLogin.value = true;
                    }
                    ElMessage({
                        type: "error",
                        message: "登录失败",
                    });
                    reject();
                }
            });
        } else {
            Router.push({name: "login"});
            reject();
        }
    })
}

// onBeforeMount(async () => {
    if (!UserDataInterface.logined.value) {
        autoLogin();
    }
// });
</script>

<template>
    <el-dialog :model-value="showReLogin" :width="400" align-center :show-close="false" draggable
               :close-on-click-modal="false" :close-on-press-escape="false">
        <template #header>
            {{ reLoginText }}
        </template>
        <template #default>
            <el-text size="large">重新登录</el-text>
            <login-dialog style="margin-top: 16px" primary/>
        </template>
    </el-dialog>
    <router-view v-slot="{ Component }">
        <transition name="main-router" mode="out-in" :duration="800">
            <component v-if="Component" :is="Component"/>
        </transition>
    </router-view>
</template>

<style scoped>
/*noinspection CssUnusedSymbol*/
.main-router-enter-active, .main-router-leave-active {
    transition: 0.3s var(--ease-in-out-quint);
}

/*noinspection CssUnusedSymbol*/
.main-router-enter-from, .main-router-leave-to {
    opacity: 0;
}

/*noinspection CssUnusedSymbol*/
.main-router-enter-to, .main-router-leave-from {
    opacity: 1;
}

.global-loading {
    opacity: 0.5;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    position: absolute;
    left: 0;
    top: 0;
    width: 100vw;
    height: 100vh;
}
</style>