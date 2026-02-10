<script setup>
import getAvatarUrlOf from "@/utils/Avatar.js";
import UIMeta from "@/utils/UI_Meta.js";
import {MdPreview} from "md-editor-v3";
import 'md-editor-v3/lib/style.css';
import router from "@/router/index.js";

const {proxy} = getCurrentInstance();

const result = proxy.$cookies.get("result");

proxy.$cookies.remove("submissions");
proxy.$cookies.remove("examInfo");
proxy.$cookies.remove("timestamps");

const backToExam = () => {
    proxy.$cookies.remove("phase");
    proxy.$cookies.remove("result");
    router.push({name: "generate"}).then(() => {
        proxy.$cookies.set("phase", "generate", "7d");
    })
}

const routeToSignUpOrLogin = () => {
    if (result.signUpCompletingType && result.signUpCompletingType !== 'INCOMPLETED') {
        router.push({name: "login"});
    } else {
        router.push({name: "sign-up"}).then(() => {
            proxy.$cookies.set("phase", "sign-up", "7d");
        })
    }
}
</script>

<template>
    <div class="result" style="flex:1;padding-bottom: 200px;">
        <div class="auto-padding-center" style="flex:1">
            <el-text style="font-size: 24px;align-self: baseline;margin-top: 64px">答题结果</el-text>
            <div style="display: flex;flex-direction: row;align-items: stretch;margin-top: 16px;margin-left: 16px;flex-wrap: wrap">
                <div style="display: flex;flex-direction: row;align-items: center;">
                    <el-avatar :size="64" style="margin-right: 16px" :src="getAvatarUrlOf(result.qq)"/>
                    <div style="display: flex;flex-direction: row;align-items: center;flex-wrap: wrap">
                        <el-text size="large" style="margin-right: 48px">{{ result.qq }}</el-text>
                        <div style="display: flex;flex-direction: row;align-items: center;">
                            <el-text type="primary" style="margin-right: 16px">
                                总分
                            </el-text>
                            <el-text style="font-size: 24px;margin-right: 16px">
                                {{ result.score }}
                            </el-text>
                        </div>
                    </div>
                </div>
            </div>
            <div style="display: flex;flex-direction: row;align-items: stretch;margin: 16px;flex-wrap: wrap">
                <div style="margin-right: 80px;margin-bottom: 12px;display: flex;flex-direction: row;align-items: stretch">
                    <div style="width: 6px;max-width: 6px;min-width: 6px;border-radius: 3px;margin-right: 32px;"
                         :style="{background: result.colorHex}"></div>
                    <el-text style="align-self: center" size="large">{{ result.level }}</el-text>
                </div>
                <div style="display: flex;flex-direction: row;margin-bottom: 12px;">
                    <el-statistic style="margin-right: 32px;" :value="result.correctCount">
                        <template #title>
                            <el-text type="success" size="small">
                                答对题数
                            </el-text>
                        </template>
                    </el-statistic>
                    <el-statistic style="margin-right: 32px;" :value="result.halfCorrectCount">
                        <template #title>
                            <el-text type="warning" size="small">
                                半对题数
                            </el-text>
                        </template>
                    </el-statistic>
                    <el-statistic style="margin-right: 32px;" title="答错题数" :value="result.wrongCount">
                        <template #title>
                            <el-text type="danger" size="small">
                                答错题数
                            </el-text>
                        </template>
                    </el-statistic>
                </div>
            </div>
        </div>
        <div style="flex:1;width: 100%;background: var(--html-bg) var(--lighting-effect-background-2);z-index: 1;margin-top: 64px;display: flex;flex-direction: column;align-items: center;padding-bottom: 200px">
            <md-preview no-upload-img placeholder="结果" v-model="result.message"
                       class="preview-only"
                       preview-theme="vuepress" :toolbars-exclude="['save','catalog','github']"
                       style="height: 100dvh;max-width:calc(90dvw - 100px);"
                       :theme="UIMeta.colorScheme.value"
                       :show-toolbar-name="UIMeta.touch.value"/>
        </div>
        <div class="flex-blank-1"></div>
        <div style="display: flex;flex-direction: row;justify-content: center;flex-wrap: wrap">
            <el-button @click="backToExam()" style="min-width: 180px">重新答题</el-button>
            <el-button v-if="result.showCreatingAccountGuide" style="min-width: 180px"
                       :type="result.signUpCompletingType && result.signUpCompletingType !== 'INCOMPLETED'?undefined:'primary'" @click="routeToSignUpOrLogin()">
                {{ result.signUpCompletingType && result.signUpCompletingType !== 'INCOMPLETED' ? "前往登录页" : "注册" }}
            </el-button>
        </div>
    </div>
</template>

<style scoped>

</style>