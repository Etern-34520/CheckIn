<script setup>
import {DArrowRight} from "@element-plus/icons-vue";
import UserDataInterface from "@/data/UserDataInterface.js";
import router from "@/router/index.js";
import {ElMessage} from "element-plus";

const usernameOrQQ = ref('');
const password = ref('');
const {proxy} = getCurrentInstance();

const loginMessage = ref('');
const key = ref(true);
const requesting = ref(false);//FIXME

const props = defineProps({
    primary: {
        type: Boolean,
        default: false
    }
})

function login() {
    requesting.value = true;
    proxy.$http.post("/checkIn/login/api", {
        usernameOrQQ: usernameOrQQ.value,
        password: password.value
    }).then(response => {
        console.log(response);
        if (response.result === "success") {
            loginMessage.value = "";
            UserDataInterface.loginAs({
                name: response.name,
                qq: response.qq,
                role: response.role,
                token: response.token
            }).then(
                    () => {
                    }, () => {
                        requesting.value = false;
                        loginMessage.value = "服务器连接失败";
                    }
            )
        } else if (response.result === "fail") {
            requesting.value = false;
            loginMessage.value = response.message;
        } else {
            requesting.value = false;
            loginMessage.value = "请求时遇到错误"
        }
        key.value = !key.value;
    }, (error) => {
        console.error(error);
        requesting.value = false;
        loginMessage.value = "请求时遇到错误";
        key.value = !key.value;
    });
}
</script>

<template>
    <div id="login" style="z-index:2;display: flex;flex-direction: column;justify-content: stretch;">
        <el-input v-model="usernameOrQQ" autofocus placeholder="用户名 / QQ" class="login-input"
                  type="text" size="large"></el-input>
        <el-input v-model="password" placeholder="密码" type="password" show-password clearable class="login-input"
                  size="large"></el-input>
        <el-button :text="!primary" :type="primary?'primary':null" bg :disabled="requesting" :loading="requesting"
                   loading-icon="_Loading_" :icon="DArrowRight" style="margin-top: 8px"
                   @click="login">登录
        </el-button>
        <div style="height: 30px;display: flex;place-items: stretch;place-content: center;">
            <Transition name="message" mode="out-in">
                <el-text :key="key">{{ loginMessage }}</el-text>
            </Transition>
        </div>
    </div>
</template>

<style scoped>
#login > * {
    margin-top: 4px;
}

/*noinspection CssUnusedSymbol*/
.message-enter-active, .message-leave-active {
    transition: all 0.2s;
}

/*noinspection CssUnusedSymbol*/
.message-enter-from, .message-leave-to {
    filter: blur(8px);
    opacity: 0;
}
</style>


<style>
.login-input input {
    text-align: center;
}

.login-input .el-input__suffix {
    height: 38px;
    display: flex;
    align-items: center;
    justify-content: end;
}

.login-input .el-input__suffix-inner {
    position: absolute !important;
}
</style>