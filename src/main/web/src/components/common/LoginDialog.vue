<script setup>
import UserDataInterface from "@/data/UserDataInterface.js";
import _Loading_ from "@/components/common/_Loading_.vue";
import VueTurnstile from 'vue-turnstile';

const usernameOrQQ = ref('');
const password = ref('');
const {proxy} = getCurrentInstance();

const loginMessage = ref('');
const key = ref(true);
const requesting = ref(false);

const turnstile = ref(null);
function reset() {
    turnstile.value.reset();
}
const siteKey = ref(proxy.$cookies.get("siteKey"));
const verifyLogin = ref(proxy.$cookies.get("verifyLogin"));
proxy.$http.get("checkTurnstile").then((resp) => {
    proxy.$cookies.set("verifyLogin", resp.enableTurnstileOnLogin, "7d", "/checkIn");
    proxy.$cookies.set("verifyExam", resp.enableTurnstileOnExam, "7d", "/checkIn");
    proxy.$cookies.set("siteKey", resp.siteKey, "7d", "/checkIn");
    verifyLogin.value = resp.enableTurnstileOnLogin;
    siteKey.value = resp.siteKey;
});

const token = ref("");

const props = defineProps({
    primary: {
        type: Boolean,
        default: false
    }
})

function login() {
    requesting.value = true;
    proxy.$http.post("login", {
        usernameOrQQ: usernameOrQQ.value,
        password: password.value,
        turnstileToken: token.value,
    }).then(response => {
        console.log(response);
        if (response.result === "success") {
            loginMessage.value = "";
            UserDataInterface.loginAs(response).then(
                    () => {
                    }, () => {
                        requesting.value = false;
                        loginMessage.value = "服务器连接失败";
                        reset();
                    }
            )
        } else if (response.result === "fail") {
            requesting.value = false;
            loginMessage.value = response.message;
            reset();
        } else {
            requesting.value = false;
            loginMessage.value = "请求时遇到错误"
            reset();
        }
        key.value = !key.value;
    }, (error) => {
        console.error(error);
        requesting.value = false;
        loginMessage.value = "请求时遇到错误";
        key.value = !key.value;
        reset();
    });
}

onErrorCaptured((e) => {
    if (e.name === "TurnstileError") {
        // Turnstile 被禁用后的内部报错，可忽略
        console.trace("turnstile disabled",e);
        return false;
    }
})
</script>

<template>
    <div id="login" style="z-index:2;display: flex;flex-direction: column;justify-content: stretch;">
        <el-input v-model="usernameOrQQ" autofocus placeholder="用户名 / QQ" class="login-input"
                  type="text" size="large"></el-input>
        <el-input v-model="password" placeholder="密码" type="password" show-password clearable class="login-input"
                  size="large"></el-input>
        <el-button :text="!primary" :type="primary?'primary':null" bg :disabled="Boolean(requesting || (token.length === 0 && verifyLogin))"
                   :loading="Boolean(requesting || (token.length === 0 && verifyLogin))"
                   :loading-icon="_Loading_" style="margin-top: 8px" @click="login">
            {{ token.length === 0 && verifyLogin ? "等待 Cloudflare 验证" : "登录"}}
        </el-button>
        <div style="height: 30px;display: flex;place-items: stretch;place-content: center;">
            <Transition name="message" mode="out-in">
                <el-text :key="key">{{ loginMessage }}</el-text>
            </Transition>
        </div>
        <div style="height: 65px;width:300px;display: flex;">
            <vue-turnstile ref="turnstile" v-if="verifyLogin" appearance="interaction-only" @error="reset" :site-key="siteKey" v-model="token" size="normal"/>
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