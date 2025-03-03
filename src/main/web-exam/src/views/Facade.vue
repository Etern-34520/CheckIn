<script setup>
import {MdEditor} from "md-editor-v3";
import 'md-editor-v3/lib/style.css';
import UIMeta from "@/UI_Meta.js";
import router from "@/router/index.js";

const {proxy} = getCurrentInstance();

const props = defineProps({
    facadeData: {
        type: Object,
        required: true
    }, gradingData: {
        type: Object,
        required: true
    }, extraData: {
        type: Object,
        required: true
    },
})

const routeGenerateExam = () => {
    proxy.$cookies.set("phrase", "generating", "7d");
    router.push({name: 'generate'});
}
</script>

<template>
    <div class="facade">
        <div class="auto-padding-center" style="flex-direction: row">
            <div v-if="facadeData.icon" class="icon-image" style="position: relative">
                <el-image
                        style="filter: saturate(2) blur(64px);transform: scale(3) translate3d(20%,5%,0); position: absolute"
                        :src="facadeData.icon" fit="contain"></el-image>
                <el-image :src="facadeData.icon" fit="contain"></el-image>
            </div>
            <div style="display: flex;flex-direction: column;margin-left: 32px;margin-right: 16px;margin-bottom: 48px;z-index: 1;justify-content: center;">
                <el-text
                        style="font-size: 44px;font-weight: bolder;height: 60px;margin-top:20px;min-width: 300px;align-self: baseline;"
                        :type="facadeData.title?undefined:'info'">{{
                        facadeData.title ? facadeData.title : "[无标题]"
                    }}
                </el-text>
                <el-text
                        style="font-size: 24px;height: 40px;margin-top:20px;min-width: 240px;margin-bottom: 40px;align-self: baseline;"
                        :type="facadeData.subTitle?undefined:'info'">
                    {{ facadeData.subTitle ? facadeData.subTitle : "[无副标题]" }}
                </el-text>
            </div>
            <div class="flex-blank-1"></div>
            <div class="panel-1"
                 style="display: flex;flex-direction:column;align-items:stretch;justify-content:start;width: 400px;padding: 20px 32px;z-index: 1;align-self: center;margin-left: 32px;">
                <el-text size="large" style="align-self: start;">答题信息</el-text>
                <div style="display: flex;flex-direction: row;align-items: center;margin-top: 8px;">
                    <div style="display: flex;flex-direction: column;margin-right: 8px">
                        <div style="display: flex;flex-direction: row;align-items: center">
                            <el-tag style="align-self: start;margin-right: 12px;" type="info">题量
                            </el-tag>
                            <el-text>{{ extraData.questionAmount }}</el-text>
                        </div>
                        <div style="display: flex;flex-direction: row;align-items: center">
                            <el-tag style="align-self: start;margin-right: 12px;" type="info">
                                分区数
                            </el-tag>
                            <el-text v-if="extraData.partitionRange" style="margin-right: 4px">
                                {{ extraData.partitionRange[0] }}
                            </el-text>
                            <el-text v-if="extraData.partitionRange" style="margin-right: 4px">~
                            </el-text>
                            <el-text v-if="extraData.partitionRange">
                                {{ extraData.partitionRange[1] }}
                            </el-text>
                        </div>
                    </div>
                </div>
                <div style="display: flex;flex-direction: row;align-items: center;margin-bottom: 4px;">
                    <el-tag style="align-self: start" type="info">分数段</el-tag>
                </div>
<!--                {{extraData}}-->
                <div style="display: flex">
                    <el-text style="margin-right: 8px;">{{ gradingData.splits[0] }}</el-text>
                    <div style="display: flex;flex-direction: column;flex: 1;margin-right: 8px;">
                        <div class="score-bar"
                             style="background: rgba(0,0,0,0);margin-bottom: 4px;overflow: visible">
                            <template v-for="(level,$index) of gradingData.levels">
                                <div :style="{flex: gradingData.splits[$index+1] ? gradingData.splits[$index+1] : extraData.questionScore * extraData.questionAmount - gradingData.splits[$index]}"
                                     style="display: flex;flex-direction: column">
                                    <el-text>{{ level.name }}</el-text>
                                </div>
                                <el-text v-if="gradingData.splits[$index + 1]">
                                    {{ gradingData.splits[$index + 1] }}
                                </el-text>
                            </template>
                        </div>
                        <div class="score-bar" style="margin-bottom: 16px;">
                            <template v-for="(level,$index) of gradingData.levels">
                                <div :style="{
                                                 background: level.colorHex,
                                                 flex: gradingData.splits[$index+1] ? gradingData.splits[$index+1] : extraData.questionScore * extraData.questionAmount - gradingData.splits[$index]
                                             }"
                                     style="height: 6px"></div>
                            </template>
                        </div>
                    </div>
                    <el-text>{{ extraData.questionScore * extraData.questionAmount }}</el-text>
                </div>
            </div>
        </div>
        <div style="flex:1;width: 100%;background: var(--html-bg) var(--lighting-effect-background-2);z-index: 1;margin-top: 64px;display: flex;flex-direction: column;align-items: center;padding-bottom: 200px">
            <md-editor no-upload-img placeholder="描述" v-model="facadeData.description"
                       class="preview-only"
                       preview-theme="vuepress" :toolbars-exclude="['save','catalog','github']"
                       style="height: 100vh;max-width:calc(90vw - 100px);"
                       :theme="UIMeta.colorScheme.value"
                       :show-toolbar-name="UIMeta.mobile.value"
                       :preview="true"/>
            <div>
                <el-button type="primary" size="large" @click="routeGenerateExam">生成题目</el-button>
            </div>
        </div>
    </div>
</template>

<style scoped>
.facade {
    display: inherit;
    flex-direction: inherit;
    flex: inherit;
    align-items: center;
    width: 100%;
    margin-top: 10vh;
}

.icon-image {
    width: 20vh;
    min-width: 160px;
    min-height: 160px;
    aspect-ratio: 1;
    height: 20vh;
    margin-right: 40px;
    margin-bottom: 48px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
}

.score-bar {
    display: flex;
    position: relative;
    overflow: hidden;
    background: var(--panel-bg-color-overlay) var(--lighting-effect-background-2);
    min-height: 6px;
    border-radius: 3px;
    margin-top: 4px;
}
</style>