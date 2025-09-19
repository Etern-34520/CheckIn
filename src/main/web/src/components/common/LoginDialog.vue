<script setup>
import UserDataInterface from "@/data/UserDataInterface.js";
import _Loading_ from "@/components/common/_Loading_.vue";
import VueTurnstile from 'vue-turnstile';
import {PictureRounded} from "@element-plus/icons-vue";
import Loading_ from "@/components/common/_Loading_.vue";

const usernameOrQQ = ref('');
const password = ref('');
const {proxy} = getCurrentInstance();

const loginMessage = ref('');
const key = ref(true);
const requesting = ref(false);
const oauth2LoginProceedingIndex = ref(-1);
const turnstile = ref(null);

function reset() {
    turnstile.value.reset();
}

const siteKey = ref(proxy.$cookies.get("siteKey", "/checkIn"));
const verifyLogin = ref(proxy.$cookies.get("verifyLogin", "/checkIn") === "true");
proxy.$http.get("check-turnstile").then((resp) => {
    proxy.$cookies.set("verifyLogin", resp.enableTurnstileOnLogin, "7d", "/checkIn");
    proxy.$cookies.set("verifyExam", resp.enableTurnstileOnExam, "7d", "/checkIn");
    proxy.$cookies.set("siteKey", resp.siteKey, "7d", "/checkIn");
    verifyLogin.value = resp.enableTurnstileOnLogin;
    siteKey.value = resp.siteKey;
});
proxy.$http.get("oauth2/providers").then((resp) => {
    // 期望 resp.providers 为后端返回的 provider 数组
    oauth2Providers.value = resp.providers || [];
}).catch((e) => {
    console.error(e);
})

const oauth2Providers = ref([]);

const token = ref("");

const props = defineProps({
    primary: {
        type: Boolean,
        default: false
    },
    oauth: {
        type: Boolean,
        default: true
    }
})

function startOAuth2(provider, index) {
    oauth2LoginProceedingIndex.value = index;
    proxy.$cookies.set("OAuth2Mode", "login", "10m", "/checkIn");
    proxy.$cookies.set("OAuth2FallbackRouteName", proxy.$router.currentRoute.value.name, "10m", "/checkIn");
    window.location.href = `${window.location.origin}/checkIn/api/oauth2/authorization/${provider.id}`;
}

const oAuth2ErrorMessage = proxy.$cookies.get("OAuth2ErrorMessage", "/checkIn");
if (oAuth2ErrorMessage !== null) {
    loginMessage.value = oAuth2ErrorMessage;
    proxy.$cookies.remove("OAuth2ErrorMessage", "/checkIn");
}

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
            key.value = !key.value;
            UserDataInterface.loginAs(response).then(
                () => {
                }, () => {
                    requesting.value = false;
                    loginMessage.value = "服务器连接失败";
                    key.value = !key.value;
                    reset();
                }
            )
        } else if (response.result === "fail") {
            requesting.value = false;
            loginMessage.value = response.message;
            key.value = !key.value;
            reset();
        } else {
            requesting.value = false;
            loginMessage.value = "请求时遇到错误"
            key.value = !key.value;
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
        console.trace("turnstile disabled", e);
        return false;
    }
});
</script>

<template>
    <div id="login" style="z-index:2;display: flex;flex-direction: column;justify-content: stretch;">
        <el-input v-model="usernameOrQQ" autofocus placeholder="用户名 / QQ" class="login-input"
                  type="text" size="large"></el-input>
        <el-input v-model="password" placeholder="密码" type="password" show-password clearable class="login-input"
                  size="large"></el-input>
        <el-button :text="!primary" :type="primary?'primary':null" bg
                   :disabled="Boolean(requesting || (token.length === 0 && verifyLogin))"
                   :loading="Boolean(requesting || (token.length === 0 && verifyLogin))"
                   :loading-icon="_Loading_" style="margin-top: 8px" @click="login">
            {{ token.length === 0 && verifyLogin ? "等待 Cloudflare 验证" : "登录" }}
        </el-button>
        <transition name="blur-scale">
            <div v-if="oauth2Providers && oauth2Providers.length > 0" style="margin-top: 16px">
                <div style="display: flex;flex-direction: row;justify-content: center;align-items: center;margin-bottom: 8px">
                    <el-text size="small" type="info">或用以下方式登录</el-text>
                </div>
                <div style="display: flex;flex-direction: row;flex-wrap: wrap;justify-content: center;align-items: center">
                    <el-button link v-for="(provider, index) in oauth2Providers"
                               style="margin: 0 !important;" @click="startOAuth2(provider, index)"
                               :disabled="oauth2LoginProceedingIndex !== -1">
                        <div style="display: flex;flex-direction: column;align-items: center">
                            <transition name="blur-scale" mode="out-in">
                                <el-icon v-if="oauth2LoginProceedingIndex === index" :size="24" style="padding: 4px;width: 24px;height: 24px;align-self: center">
                                    <Loading_/>
                                </el-icon>
                                <el-image v-else :src="'https://favicon.im/' + provider.iconDomain"
                                          style="width: 24px;height: 24px;padding: 4px">
                                    <template #error>
                                        <el-icon :size="24">
                                            <PictureRounded/>
                                        </el-icon>
                                    </template>
                                </el-image>
                            </transition>
                            <el-text size="small" type="info" style="align-self: center;margin: 0 !important;">{{ provider.name }}</el-text>
                        </div>
                    </el-button>
                </div>
            </div>
        </transition>
        <div style="min-height: 30px;display: flex;place-items: stretch;place-content: center;">
            <Transition name="message" mode="out-in">
                <el-text :key="key">{{ loginMessage }}</el-text>
            </Transition>
        </div>
        <div style="height: 65px;width:300px;display: flex;">
            <vue-turnstile ref="turnstile" v-if="verifyLogin" appearance="interaction-only" @error="reset"
                           :site-key="siteKey" v-model="token" size="normal"/>
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