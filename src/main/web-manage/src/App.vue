<script setup>
// import {RouterView} from "vue-router";
import WebSocketConnector from "@/api/websocket.js";
import Router from "@/router/index.js";
import LoginDialog from "@/components/common/LoginDialog.vue";
import UserDataInterface from "@/data/UserDataInterface.js";
import {ElMessage} from "element-plus";
import router from "@/router/index.js";

const {proxy} = getCurrentInstance();
const showReLogin = ref(false);
const reLoginText = ref("登录失败");

watch(() => UserDataInterface.logined.value, () => {
    WebSocketConnector.showGlobalNotifications = UserDataInterface.logined.value;
    if (UserDataInterface.logined.value) {
        showReLogin.value = false;
    }
})

onMounted(() => {
    const user = proxy.$cookies.get("user", "/checkIn");
    if (user !== null) {
        console.debug("Login from cookie:", user);
        UserDataInterface.loginAs(user).then(() => {
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
            }
        });
    } else {
        Router.push({name: "login"});
    }
});
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
        <transition name="main-router" mode="out-in" duration="1000">
            <component v-if="Component" :is="Component"/>
            <div v-else class="global-loading">
                <Loading_ style="scale: 2"></Loading_>
            </div>
        </transition>
    </router-view>
</template>

<style scoped>
/*noinspection CssUnusedSymbol*/
.main-router-enter-active, .main-router-leave-active {
    transition: 0.4s var(--ease-in-out-quint);
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
    width: 100vw;
    height: 100vh;
}
</style>