<script setup>
import getAvatarUrlOf from "@/Avatar.js";
import UIMeta from "@/UI_Meta.js";
import {MdEditor} from "md-editor-v3";

const {proxy} = getCurrentInstance();

const result = proxy.$cookies.get("result");

proxy.$cookies.remove("submissions");//TODO test
proxy.$cookies.remove("examInfo");
proxy.$cookies.remove("timestamps");
</script>

<template>
    <div class="result" style="flex:1">
        <div class="auto-padding-center" style="flex:1">
            <el-text style="font-size: 24px;align-self: baseline;margin-top: 64px">答题结果</el-text>
            <div style="display: flex;flex-direction: row;align-items: stretch;margin-top: 16px;margin-left: 16px;flex-wrap: wrap">
                <div style="display: flex;flex-direction: row;align-items: center;">
                    <el-avatar style="width: 64px;height: 64px;margin-right: 16px" :src="getAvatarUrlOf(result.qq)"/>
                    <el-text size="large" style="min-width: min(70vw,200px)">{{ result.qq }}</el-text>
                </div>
            </div>
            <div style="display: flex;flex-direction: row;align-items: stretch;margin-top: 16px;margin-left: 16px;flex-wrap: wrap">
                <div style="margin-right: 80px;display: flex;flex-direction: row;align-items: stretch">
                    <div style="width: 8px;max-width: 8px;min-width: 8px;border-radius: 4px;margin-right: 32px;"
                         :style="{background: result.colorHex}"></div>
                    <el-text style="align-self: center" size="large">{{ result.level }}</el-text>
                </div>
                <el-statistic style="margin-right: 80px;" title="总分" :value="result.score"/>
                <el-statistic style="margin-right: 32px;" title="答对题数" :value="result.correctCount"/>
                <el-statistic style="margin-right: 32px;" title="半对题数" :value="result.halfCorrectCount"/>
                <el-statistic style="margin-right: 32px;" title="答错题数" :value="result.wrongCount"/>
            </div>
        </div>
        <div style="flex:1;width: 100%;background: var(--html-bg) var(--lighting-effect-background-2);z-index: 1;margin-top: 64px;display: flex;flex-direction: column;align-items: center;padding-bottom: 200px">
            <md-editor no-upload-img placeholder="描述" v-model="result.message"
                       class="preview-only"
                       preview-theme="vuepress" :toolbars-exclude="['save','catalog','github']"
                       style="height: 100vh;max-width:calc(90vw - 100px);"
                       :theme="UIMeta.colorScheme.value"
                       :show-toolbar-name="UIMeta.mobile.value"
                       :preview="!UIMeta.mobile.value"/>
        </div>

        <!--        {{ result }}-->
    </div>
</template>

<style scoped>

</style>