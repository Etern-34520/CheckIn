<script setup>
import getAvatarUrlOf from "@/Avatar.js";
import HarmonyOSIcon_InfoCircle from "@/icons/HarmonyOSIcon_InfoCircle.vue";
import router from "@/router/index.js";
import {ElMessage, ElMessageBox} from "element-plus";
import {ArrowLeftBold} from "@element-plus/icons-vue";
import _Loading_ from "@/components/_Loading_.vue";
import {resolveDirective} from "vue";

const {proxy} = getCurrentInstance();

const result = proxy.$cookies.get("result");

const username = ref();
const password = ref();
const repeatPassword = ref();
const tip = ref();
const disabled = ref(true);
const buttonLoading = ref(false);
const disableAllFields = ref(false);
const buttonText = ref("注册");

const back = () => {
    proxy.$cookies.set("phase", "result", "7d");
    router.push({name: "result"});
}

onMounted(() => {
    checkSignUpCompletingType(result.signUpCompletingType);
})

const onClick = () => {
    if (result.signUpCompletingType === "CREATE_AND_ENABLED") {
        window.location.href = window.location.protocol + "//" + window.location.host + "/checkIn/login/"
    } else if (!result.signUpCompletingType){
        signUp();
    } else {
        back();
    }
}

const checkSignUpCompletingType = (completingType) => {
    switch (completingType) {
        case "CREATE_AND_DISABLED":
            tip.value = {
                content: "注册成功，请等待管理员启用你的账户",
                type: "success"
            }
            disabled.value = false;
            disableAllFields.value = true;
            buttonText.value = "返回结果页"
            break;
        case "CREATE_AND_ENABLED_AFTER_VALIDATED":
            tip.value = {
                content: "注册成功，请等待验证通过后你的账户被启用",
                type: "success"
            }
            disabled.value = false;
            disableAllFields.value = true;
            buttonText.value = "返回结果页"
            break;
        case "CREATE_AND_ENABLED":
            tip.value = {
                content: "注册成功",
                type: "success"
            }
            disabled.value = false;
            disableAllFields.value = true;
            buttonText.value = "前往登录"
            break;
    }
}
const signUp = () => {
    buttonLoading.value = true;
    tip.value = null;
    const handleError = (err) => {
        buttonLoading.value = false;
        let errDescription = "unknown";
        if (typeof err === "string") {
            errDescription = err
        } else if (err.response && err.response.data.message) {
            errDescription = err.response.data.message;
        } else if (typeof err.message === "string") {
            errDescription = err.message;
        }
        tip.value = {
            content: "注册时遇到错误：" + errDescription,
            type: "danger"
        }
    }
    proxy.$http.post("sign-up", {
        examId: result.examDataId,
        name: username.value,
        password: password.value,
    }).then((response) => {
        buttonLoading.value = false;
        disableAllFields.value = true;
        if (response.type === "success") {
            result.signUpCompletingType = response.completingType;
            proxy.$cookies.set("result", result, "7d");
            checkSignUpCompletingType(response.completingType);
        } else {
            handleError(response)
        }
    }, (err) => {
        handleError(err);
    })
}

watch(repeatPassword, () => {
    if (repeatPassword.value === password.value) {
        checkDisable();
    }
});

const checkDisable = () => {
    if (!password.value) {
        disabled.value = true;
        tip.value = null;
    } else if (password.value.length < 6) {
        disabled.value = true;
        tip.value = {
            content: "密码过短",
        };
    } else if (repeatPassword.value && password.value !== repeatPassword.value) {
        disabled.value = true;
        tip.value = {
            content: "重复密码不一致",
        };
    } else if (!repeatPassword.value) {
        disabled.value = true;
        tip.value = null;
    } else {
        disabled.value = false;
        tip.value = null;
    }
}

</script>

<template>
    <div class="auto-padding-center" style="flex:1;padding-bottom: 200px;justify-content: center;align-items: center">
        <el-button link size="large" @click="back"
                   style="margin-top: 36px;align-self: baseline;padding: 8px 16px !important;font-size: 1em">
            <el-icon>
                <ArrowLeftBold/>
            </el-icon>
            返回
        </el-button>
        <div style="display: flex;flex-direction: row;align-items: center;margin-top: 16px;margin-left: 16px;">
            <el-avatar :size="64" style="margin-right: 16px" :src="getAvatarUrlOf(result.qq)"/>
            <el-text style="min-width: min(70vw,200px)">{{ result.qq }}</el-text>
        </div>
        <div style="display: flex;flex-direction: column;width: 300px;margin-top: 32px;">
            <el-text style="align-self: baseline">用户名</el-text>
            <el-input :disabled="disableAllFields" class="disable-init-animate" v-model="username"/>
        </div>
        <div style="display: flex;flex-direction: column;width: 300px;margin-top: 4px;">
            <el-text style="align-self: baseline">密码 *</el-text>
            <el-input :disabled="disableAllFields" class="disable-init-animate" type="password" @focusout="checkDisable" v-model="password"/>
        </div>
        <div style="display: flex;flex-direction: column;width: 300px">
            <el-text style="align-self: baseline">重复密码 *</el-text>
            <el-input :disabled="disableAllFields" class="disable-init-animate" type="password" @focusout="checkDisable" v-model="repeatPassword"/>
        </div>
        <div style="min-height: 32px;margin-top: 8px">
            <transition name="blur-scale" mode="out-in">
                <el-text truncated style="max-width: 300px" :key="tip.content" v-if="tip" :type="tip.type">
                    {{ tip.content }}
                </el-text>
            </transition>
        </div>
        <el-button type="primary" size="large" :loading="buttonLoading" :loading-icon="_Loading_"
                   style="margin-top: 24px;align-self: center;width: 300px" @click="onClick" :disabled="disabled">
            {{ buttonText }}
        </el-button>
    </div>
</template>

<style scoped>

</style>