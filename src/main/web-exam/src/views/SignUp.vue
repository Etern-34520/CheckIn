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

const signingUp = ref(false);
const password = ref();
const repeatPassword = ref();
const tip = ref();

const back = () => {
    proxy.$cookies.set("phrase", "result", "7d");
    router.push({name: "result"});
}

const signUp = () => {
    //TODO
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
            <el-avatar style="width: 64px;height: 64px;margin-right: 16px" :src="getAvatarUrlOf(result.qq)"/>
            <el-text style="min-width: min(70vw,200px)">{{ result.qq }}</el-text>
        </div>
        <div style="display: flex;flex-direction: column;width: 300px;margin-top: 32px;">
            <el-text style="align-self: baseline">密码</el-text>
            <el-input v-model="password"/>
        </div>
        <div style="display: flex;flex-direction: column;width: 300px">
            <el-text style="align-self: baseline">重复密码</el-text>
            <el-input v-model="repeatPassword"/>
        </div>
<!--        <div class="flex-blank-1"></div>-->
        <div style="min-height: 32px">
            <transition name="blur-scale" mode="out-in">
                <el-text :key="tip" v-if="tip !== ''">{{ tip }}</el-text>
            </transition>
        </div>
        <el-button type="primary" size="large" :loading="signingUp" :loading-icon="_Loading_"
                   style="margin-top: 36px;align-self: center;width: 300px"
                   @click="signUp">注册
        </el-button>
    </div>
</template>

<style scoped>

</style>