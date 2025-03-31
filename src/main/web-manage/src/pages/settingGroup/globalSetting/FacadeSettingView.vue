<script setup>
import WebSocketConnector from "@/api/websocket.js";
import {ElMessage} from "element-plus";
import {Picture} from "@element-plus/icons-vue";
import UIMeta from "@/utils/UI_Meta.js";
import {MdEditor} from "md-editor-v3";
import 'md-editor-v3/lib/style.css';
import HarmonyOSIcon_Remove from "@/components/icons/HarmonyOSIcon_Remove.vue";
import PermissionInfo from "@/auth/PermissionInfo.js";
import router from "@/router/index.js";

const editing = ref(false);
const mode = ref('预览');
const data = ref({});
const gradingData = ref({});
const extraData = ref({});
const loading = ref(true);
const error = ref(false);

let backup = {};
let backupJSON;
const cancel = () => {
    data.value = backup;
    editing.value = false;
    mode.value = "预览";
}
const startEditing = () => {
    mode.value = "编辑";
    backupJSON = JSON.stringify(data.value)
    backup = JSON.parse(backupJSON);
    editing.value = true;
}
const finishEditing = () => {
    editing.value = false;
    mode.value = "预览";
    if (backupJSON !== JSON.stringify(data.value)) {
        WebSocketConnector.send({
            type: "saveFacadeSetting",
            data: data.value
        }).then(() => {
            ElMessage({
                type: "success", message: "保存成功"
            })
        }, (err) => {
            ElMessage({
                type: "error", message: "保存失败"
            })
        })
    }
}

const getData = () => {
    loading.value = true;
    error.value = false;
    Promise.all([
        new Promise((resolve, reject) => {
            WebSocketConnector.send({
                type: "getFacadeSetting",
            }).then((response) => {
                data.value = response.data;
                extraData.value = response.extraData;
                resolve();
            }, (err) => {
                ElMessage({
                    type: "error", message: "获取设置失败"
                })
                loading.value = false;
                error.value = true;
            });
        }), new Promise((resolve) => {
            WebSocketConnector.send({
                type: "getGradingSetting",
            }).then((response) => {
                gradingData.value = response.data;
                if (!gradingData.value.levels) gradingData.value.levels = [];
                if (!gradingData.value.splits) gradingData.value.splits = [0];
                if (!gradingData.value.questionScore) gradingData.value.questionScore = 5;
                resolve();
            }, (err) => {
                ElMessage({
                    type: "error", message: "获取设置失败"
                });
                loading.value = false;
                error.value = true;
            });
        })
    ]).then(() => {
        loading.value = false;
    });
}
getData();

const iconUpload = ref();

const uploadImage = (event) => {
    // console.log(iconUpload.value);
    let reader = new FileReader();
    reader.readAsDataURL(event.target.files[0]);
    reader.onload = (event1) => {
        data.value.icon = event1.target.result;
        iconUpload.value.value = "";
    }
}

const deleteIcon = () => {
    delete data.value.icon;
}
</script>

<template>
    <div style="display: flex;flex-direction: column;">
        <div style="display: flex;flex-direction: row;flex-wrap: wrap">
            <el-text style="align-self:baseline;font-size: 24px;margin-right: 32px;">首页设置</el-text>
            <template
                 v-if="PermissionInfo.hasPermission('setting','save facade setting')">
                <transition-group name="blur-scale">
                    <el-button-group key="button-group" style="margin: 2px 24px 2px 0;">
                        <transition-group name="blur-scale">
                            <el-button class="disable-init-animate" style="margin-right: 4px;"
                                       @click="editing ? finishEditing():startEditing()"
                                       :disabled="loading || error" key="edit">
                                {{ editing ? '完成' : '编辑' }}
                            </el-button>
                            <el-button class="disable-init-animate"
                                       @click="cancel" v-if="editing" key="cancel">
                                取消
                            </el-button>
                        </transition-group>
                    </el-button-group>
                    <el-segmented v-model="mode" key="segmented" v-if="editing" :options="['预览','编辑']" block
                                  style="margin: 2px 16px 2px 0;"/>
                </transition-group>
            </template>
        </div>
        <el-scrollbar v-loading="loading" style="margin-top: 20px;">
            <transition name="blur-scale" mode="out-in">
                <div v-if="!loading && !error" class="facade-base">
                    <transition name="blur-scale" mode="out-in">
                        <div v-if="mode==='预览'"
                             style="display: inherit;flex-direction: inherit;flex: inherit;align-items:center;width: 100%">
                            <div style="display: flex;flex-direction: row;flex-wrap: wrap;max-width: 80vw;width: 80vw;">
                                <div v-if="data.icon" class="icon-image" style="position: relative">
                                    <el-image
                                            style="width: 100%;height: 100%;filter: saturate(2) blur(64px);transform: scale(3) translate3d(20%,5%,0);position: absolute"
                                            :src="data.icon" fit="contain"></el-image>
                                    <el-image :src="data.icon" fit="contain"
                                              style="width: 100%;height: 100%;"></el-image>
                                </div>
                                <div style="display: flex;flex-direction: column;margin-left: 32px;margin-right: 16px;margin-bottom: 48px;z-index: 1;justify-content: center;">
                                    <el-text
                                            style="font-size: 44px;font-weight: bolder;height: 60px;margin-top:20px;min-width: 300px;align-self: baseline;"
                                            :type="data.title?undefined:'info'">{{
                                            data.title ? data.title : "[无标题]"
                                        }}
                                    </el-text>
                                    <el-text
                                            style="font-size: 24px;height: 40px;margin-top:20px;min-width: 240px;margin-bottom: 40px;align-self: baseline;"
                                            :type="data.subTitle?undefined:'info'">
                                        {{ data.subTitle ? data.subTitle : "[无副标题]" }}
                                    </el-text>
                                </div>
                                <div class="flex-blank-1"></div>
                                <div class="panel-1 exam-info">
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
                                        <div class="flex-blank-1"></div>
                                        <el-link class="el-link el-link--info is-underline"
                                                 @click="router.push({name:'generating-setting'})">生成设置
                                        </el-link>
                                    </div>
                                    <div style="display: flex;flex-direction: row;align-items: center;margin-bottom: 4px;">
                                        <el-tag style="align-self: start" type="info">分数段</el-tag>
                                        <div class="flex-blank-1"></div>
                                        <el-link class="el-link el-link--info is-underline"
                                                 @click="router.push({name:'grading-setting'})">
                                            评级设置
                                        </el-link>
                                    </div>
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
                                <md-editor no-upload-img placeholder="描述" v-model="data.description"
                                           class="preview-only"
                                           preview-theme="vuepress" :toolbars-exclude="['save','catalog','github']"
                                           style="height: 100vh;"
                                           :theme="UIMeta.colorScheme.value"
                                           :show-toolbar-name="UIMeta.mobile.value"
                                           :preview="true"/>
                            </div>
                        </div>
                        <div v-else
                             style="display: inherit;flex-direction: inherit;flex: inherit;justify-content: stretch;align-items:stretch;max-width: 80vw;width: 80vw;">
                            <div style="display: flex;flex-direction: row;flex-wrap: wrap;">
                                <input style="display: none;" id="icon-upload" type="file"
                                       accept="image/png, image/jpeg, .svg" @change="uploadImage" ref="iconUpload"/>
                                <div class="icon-upload-div">
                                    <label
                                            for="icon-upload"
                                            class="disable-init-animate el-button icon-upload-label">
                                        <el-image style="width: 20vh;min-width:160px;min-height:160px;aspect-ratio: 1;display: flex;flex-direction: column;
                                                            align-items: center;justify-content: center"
                                                  :src="data.icon" fit="contain">
                                            <template #error>
                                                <el-icon size="30">
                                                    <Picture/>
                                                </el-icon>
                                                <el-text style="margin-top: 16px">点击设置图标</el-text>
                                                <el-text style="margin-top: 4px" type="info" size="small">
                                                    （未设置图标将不会显示）
                                                </el-text>
                                            </template>
                                        </el-image>
                                    </label>
                                    <transition name="blur-scale" mode="out-in">
                                        <div v-if="data.icon" style="display: flex;">
                                            <el-text type="info"
                                                     style="display:block;text-align:center;height: 16px;margin-top: 4px;margin-bottom: -20px;">
                                                点击以修改图标
                                            </el-text>
                                            <div class="flex-blank-1"/>
                                            <el-button link type="info" class="disable-init-animate"
                                                       style="margin-bottom: -22px;margin-top: 6px;"
                                                       @click="deleteIcon">
                                                <HarmonyOSIcon_Remove :size="16"/>
                                                移除
                                            </el-button>
                                        </div>
                                    </transition>
                                </div>
                                <div style="display: flex;flex-direction: column;margin-right: 48px;margin-bottom: 48px">
                                    <el-input class="disable-init-animate" placeholder="标题"
                                              style="font-size: 40px;--el-input-height: 60px;margin-top:20px;width:30vw;min-width: 300px;"
                                              v-model="data.title"></el-input>
                                    <el-input class="disable-init-animate" placeholder="副标题"
                                              style="font-size: 24px;opacity:0.7;--el-input-height: 40px;margin-top:20px;width:30vw;min-width: 240px;"
                                              v-model="data.subTitle"></el-input>
                                </div>
                            </div>
                            <md-editor no-upload-img placeholder="描述" v-model="data.description"
                                       preview-theme="vuepress" :toolbars-exclude="['save','catalog','github']"
                                       style="height: 100vh;margin-top: 100px;margin-bottom: 120px"
                                       :theme="UIMeta.colorScheme.value" :show-toolbar-name="UIMeta.mobile.value"
                                       :preview="!UIMeta.mobile.value"/>
                        </div>
                    </transition>
                </div>
                <div v-else-if="error" style="display:flex;flex-direction: column">
                    <el-empty description="获取设置失败"></el-empty>
                    <el-button link type="primary" @click="getData" size="large">重试</el-button>
                </div>
            </transition>
        </el-scrollbar>
    </div>
</template>

<style scoped>
.facade-base {
    overflow: hidden;
    border: solid 2px rgba(128, 128, 128, 0.7);
    display: flex;
    flex-direction: column;
    align-items: center;
    border-radius: 8px;
    min-height: 50vh;
    padding-top: 64px;
    background: var(--html-bg);
}

.icon-upload-label {
    width: 20vh;
    min-width: 160px;
    min-height: 160px;
    aspect-ratio: 1;
    padding: 0 !important;
    height: 20vh;
    overflow: hidden;
}

.icon-upload-div {
    display: flex;
    flex-direction: column;
    justify-content: center;
    margin-right: 40px;
    margin-bottom: 48px;
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

.exam-info {
    display: flex;
    flex-direction: column;
    align-items: stretch;
    justify-content: start;
    width: 400px;
    padding: 20px 32px !important;
    z-index: 1;
    align-self: center;
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