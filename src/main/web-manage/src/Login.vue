<script setup>
// import {ref,getCurrentInstance,defineEmits} from 'vue';
import Router from "@/router/index.js";
import {DArrowRight, Loading} from "@element-plus/icons-vue";
/*import WebSocketConnector from "@/websocket/websocket.js";*/
const usernameOrQQ = ref('');
const password = ref('');
const { proxy } = getCurrentInstance();

const loginMessage = ref('');
const key = ref(true);
const requesting = ref(false);
const emit = defineEmits(["loginAs"]);

function login() {
    requesting.value = true;
    proxy.$http.post("checkIn/login/", {
        usernameOrQQ: usernameOrQQ.value,
        password: password.value
    }).then(response => {
        console.log(response);
        if (response.result === "success") {
            proxy.$cookies.set("token", response.token);
            loginMessage.value = "";
            emit("loginAs",{name:response.name,qq:response.qq,token:response.token});
        } else if (response.result === "fail") {
            requesting.value = false;
            loginMessage.value = response.message;
        } else {
            requesting.value = false;
            loginMessage.value = "请求时遇到错误"
        }
        key.value = !key.value;
    }, error => {
        requesting.value = false;
        loginMessage.value = "请求时遇到错误";
        key.value = !key.value;
    });
}
</script>

<template>
    <div style="width: 100%;height:100%;display: flex;align-items: center;align-content:center;justify-content: center;overflow: hidden">
        <div id="login" style="z-index:2;width: 300px;display: flex;flex-direction: column;justify-content: stretch;">
            <el-input v-model="usernameOrQQ" autofocus placeholder="用户名或QQ" type="text" size="large"></el-input>
            <el-input v-model="password" placeholder="密码" type="password" show-password clearable
                      size="large"></el-input>
            <el-button text bg :disabled="requesting" :loading="requesting" loading-icon="_Loading_" :icon="DArrowRight" @click="login">登录</el-button>
            <div style="height: 30px;display: flex;place-items: stretch;place-content: center;">
                <Transition name="message" mode="out-in">
                    <el-text :key="key">{{ loginMessage }}</el-text>
                </Transition>
            </div>
        </div>
        <div class="background-font">
            <div style="font-size: 256px;color: var(--el-color-primary-3);opacity: 0.1;filter: blur(8px)">
                CHECKIN
            </div>
            <div style="font-size: 200px;margin-top: -200px;filter: blur(8px)">
                LOGIN
            </div>
        </div>
    </div>
</template>

<style scoped>
#login > * {
    margin-top: 4px;
}

.background-font {
    position: absolute;
    display: flex;
    flex-direction: column;
    align-content: center;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;
    color: var(--el-bg-color);
    user-select: none;
    overflow: hidden;
}

/*noinspection CssUnusedSymbol*/
.message-enter-active , .message-leave-active {
    transition: all 0.2s;
}

/*noinspection CssUnusedSymbol*/
.message-enter-from , .message-leave-to {
    filter: blur(8px);
    opacity: 0;
}
</style>