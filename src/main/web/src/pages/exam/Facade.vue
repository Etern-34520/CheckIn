<script setup>
import {MdPreview} from "md-editor-v3";
import 'md-editor-v3/lib/style.css';
import UIMeta from "@/utils/UI_Meta.js";
import router from "@/router/index.js";
import {Close} from "@element-plus/icons-vue";

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
    proxy.$cookies.set("phase", "generate", "7d");
    router.push({name: 'generate'});
}

const getSplitsFlexRate = (index) => {
    const max = props.extraData.questionScore * props.extraData.questionAmount;
    const next = props.gradingData.splits[index + 1] !== undefined ? props.gradingData.splits[index + 1] : max;
    return next - props.gradingData.splits[index];
}
</script>

<template>
    <div class="facade">
        <div class="auto-padding-center" style="flex-direction: row">
            <div v-if="facadeData.icon" class="icon-image" style="position: relative">
                <el-image class="icon-blur"
                          style="filter: saturate(2) blur(64px);transform: scale(3) translate3d(20%,5%,0);position: absolute;width: 100%;height: 100%;"
                          :src="facadeData.icon" fit="contain"></el-image>
                <el-image class="icon-main" :src="facadeData.icon" fit="contain"
                          style="width: 100%;height: 100%;"></el-image>
            </div>
            <div style="margin: 32px 16px;z-index: 1;display: flex;flex-direction: column;justify-content: center;">
                <el-text class="title">{{ facadeData.title }}</el-text>
                <el-text class="subtitle">
                    {{ facadeData.subTitle }}
                </el-text>
            </div>
            <div class="exam-info">
                <el-text size="large" style="align-self: start;">答题信息</el-text>
                <div style="display: flex;flex-direction: row;align-items: center;margin-top: 16px;">
                    <div style="display: flex;flex-direction: column;margin-right: 8px">
                        <div style="display: flex;flex-direction: row;align-items: center;margin-bottom: 8px">
                            <el-text style="align-self: start;margin-right: 12px;" type="info">题量
                            </el-text>
                            <el-text>{{ extraData.questionAmount }}</el-text>
                        </div>
                        <div style="display: flex;flex-direction: row;align-items: center;margin-bottom: 8px;">
                            <el-text style="align-self: start;margin-right: 12px;" type="info">
                                分区数
                            </el-text>
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
                <div style="display: flex;align-items: center">
                    <el-text style="align-self: center;margin-right: 16px" type="info">分数段</el-text>
                    <div style="display: flex;flex: 1">
                        <div style="display: flex;flex-direction: column;flex: 1;margin-right: 16px;">
                            <div class="score-bar"
                                 style="background: rgba(0,0,0,0);overflow: visible">
                                <div style="width: 0;overflow: visible;display: flex;flex-direction: row;">
                                    <el-text style="text-wrap: nowrap">
                                        {{ gradingData.splits[0] }}
                                    </el-text>
                                </div>
                                <div style="flex: 1" v-if="!gradingData.levels || gradingData.levels.length === 0"></div>
                                <template v-for="(level,$index) of gradingData.levels">
                                    <div :style="{flex: getSplitsFlexRate($index)}"
                                         style="display: flex;flex-direction: column">
                                    </div>
                                    <div style="width: 0;overflow: visible;display: flex;flex-direction: row;">
                                        <el-text v-if="gradingData.splits[$index + 1]" style="text-wrap: nowrap">
                                            {{ gradingData.splits[$index + 1] }}
                                        </el-text>
                                    </div>
                                </template>
                                <div style="width: 0;overflow: visible;display: flex;flex-direction: row;">
                                    <el-text style="text-wrap: nowrap">
                                        {{ extraData.questionScore * extraData.questionAmount }}
                                    </el-text>
                                </div>
                            </div>
                            <div class="score-bar">
                                <template v-for="(level,$index) of gradingData.levels">
                                    <div :style="{
                                                 background: level.colorHex,
                                                 flex: getSplitsFlexRate($index)
                                             }"
                                         style="height: 6px"></div>
                                </template>
                            </div>
                            <div class="score-bar"
                                 style="background: rgba(0,0,0,0);overflow: visible;">
                                <template v-for="(level,$index) of gradingData.levels">
                                    <div :style="{flex: getSplitsFlexRate($index)}"
                                         style="display: flex;flex-direction: row;justify-content: start">
                                        <div style="width: 0;overflow: visible;display: flex;flex-direction: row;">
                                            <el-text style="text-wrap: nowrap;">{{level.name}}</el-text>
                                        </div>
                                    </div>
                                </template>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="facade-content">
            <md-preview no-upload-img placeholder="描述" v-model="facadeData.description"
                       class="preview-only auto-padding-center"
                       preview-theme="vuepress" :toolbars-exclude="['save','catalog','github']"
                       style="height: 100dvh;"
                       :theme="UIMeta.colorScheme.value"
                       :show-toolbar-name="UIMeta.touch.value"/>
            <div style="flex: 1;display: flex;flex-direction: column;align-items: center;justify-content: end">
                <el-button type="primary" size="large" style="margin-top: 36px;align-self: center;min-width: 180px"
                           :disabled="!extraData.serviceAvailable" @click="routeGenerateExam">
                    {{extraData.serviceAvailable?"生成题目":"服务暂不可用"}}
                </el-button>
            </div>
        </div>
    </div>
</template>

<style scoped>
.facade {
    display: flex;
    flex-direction: column;
    flex: 1;
    align-items: center;
    width: 100%;
    margin-top: 10dvh;
    min-height: 100dvh;
}

.icon-image {
    width: 20dvh;
    min-width: 160px;
    min-height: 160px;
    aspect-ratio: 1;
    height: 20dvh;
    margin-right: 40px;
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

@keyframes ani-0 {
    0% {
        filter: blur(4px);
        transform: translate3d(-40px, 0, 0);
        opacity: 0;
    }
    100% {
        opacity: 1;
    }
}

@keyframes ani-1 {
    0% {
        opacity: 0;
    }
    100% {
        opacity: 1;
    }
}

@keyframes ani-2 {
    0% {
        filter: blur(16px);
        scale: 0.95;
        opacity: 0;
    }
    100% {
        opacity: 1;
    }
}

.title {
    font-size: 44px;
    height: auto;
    font-weight: bolder;
    margin-top: 20px;
    min-width: 300px;
    animation: ani-0 600ms 400ms var(--ease-out-quint);
    animation-fill-mode: backwards;
    text-wrap: wrap;
    word-break: break-all;
    align-self: baseline;
}

.subtitle {
    font-size: 24px;
    height: auto;
    margin-top: 20px;
    min-width: 240px;
    margin-bottom: 40px;
    animation: ani-0 600ms 800ms var(--ease-out-quint);
    animation-fill-mode: backwards;
    text-wrap: wrap;
    word-break: break-all;
    align-self: baseline;
}

.icon-blur {
    animation: ani-1 800ms 400ms var(--ease-out-quint);
    animation-fill-mode: backwards;
}

.icon-main {
    animation: ani-1 500ms var(--ease-out-quint);
    animation-fill-mode: backwards;
}

.exam-info {
    display: flex;
    flex: 1;
    flex-direction: column;
    align-items: stretch;
    justify-content: start;
    min-width: min(400px,80dvw);
    max-width: max(500px,50dvw);
    z-index: 1;
    align-self: center;

    animation: ani-2 600ms 800ms var(--ease-out-quint);
    animation-fill-mode: backwards;
}

.facade-content {
    flex: 1;
    width: 100%;
    background: var(--html-bg) var(--lighting-effect-background-2);
    z-index: 1;
    margin-top: 64px;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding-bottom: 200px;

    animation: ani-2 1000ms 900ms var(--ease-out-quint);
    animation-fill-mode: backwards;
}
</style>