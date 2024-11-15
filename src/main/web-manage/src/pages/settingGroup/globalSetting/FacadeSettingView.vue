<script setup>
import WebSocketConnector from "@/api/websocket.js";
import {ElMessage, ElLink} from "element-plus";
import {Picture} from "@element-plus/icons-vue";
import UIMeta from "@/utils/UI_Meta.js";
import {MdEditor} from "md-editor-v3";
import 'md-editor-v3/lib/style.css';
import HarmonyOSIcon_Remove from "@/components/icons/HarmonyOSIcon_Remove.vue";

const editing = ref(false);
const mode = ref('预览');
const data = ref({});
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
    /*WebSocketConnector.send({
        type: "saveVerificationSetting",
        data: data.value
    }).then(() => {
        ElMessage({
            type: "success", message: "保存成功"
        })
    }, (err) => {
        ElMessage({
            type: "error", message: "保存失败"
        })
    })*/
}

const getData = () => {
    loading.value = true;
    error.value = false;
    WebSocketConnector.send({
        type: "getFacadeSetting",
    }).then((response) => {
        data.value = response.data;
        loading.value = false;
    }, (err) => {
        ElMessage({
            type: "error", message: "获取设置失败"
        })
        loading.value = false;
        error.value = true;
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
    <div style="display: flex;flex-direction: column;padding-bottom: 16px">
        <el-text style="align-self:baseline;font-size: 24px">首页设置</el-text>
        <div style="display: flex;margin-top: 16px;margin-left: 8px;margin-bottom: 20px;">
            <transition-group name="blur-scale">
                <el-button-group key="button-group">
                    <transition-group name="blur-scale">
                        <el-button class="disable-init-animate" style="margin-right: 4px;"
                                   @click="editing ? finishEditing():startEditing()"
                                   :disabled="loading || error" key="edit">
                            {{ editing ? '完成' : '编辑' }}
                        </el-button>
                        <el-button class="disable-init-animate" style="margin-right: 24px;"
                                   @click="cancel" v-if="editing" key="cancel">
                            {{ editing ? '取消' : '编辑' }}
                        </el-button>
                    </transition-group>
                </el-button-group>
                <el-segmented v-model="mode" key="segmented" v-if="editing" :options="['预览','编辑']" block
                              style="margin-right: 16px"/>
            </transition-group>
        </div>
        <el-scrollbar>
            <div v-loading="loading">
                <transition name="blur-scale" mode="out-in">
                    <div v-if="!loading && !error"
                         style="border: solid 2px rgba(128,128,128,0.7);display:flex;flex-direction:column;align-items:center;border-radius:8px;min-height: 50vh;padding-top: 64px;background: var(--html-bg)">
                        <transition name="blur-scale" mode="out-in">
                            <div v-if="mode==='预览'"
                                 style="display: inherit;flex-direction: inherit;flex: inherit;align-items:center;width: 100%">
                                <div style="display: flex;flex-direction: row;flex-wrap: wrap;max-width: 80vw;width: 80vw;">
                                    <div v-if="data.icon" class="icon-image" style="position: relative">
                                        <el-image
                                                style="filter: saturate(2) blur(64px);transform: scale(3) translate3d(20%,5%,0); position: absolute"
                                                :src="data.icon" fit="contain"></el-image>
                                        <el-image :src="data.icon" fit="contain"></el-image>
                                    </div>
                                    <div style="display: flex;flex-direction: column;margin-right: 48px;margin-bottom: 48px;z-index: 1;justify-content: center;">
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
                                    <div class="panel-1"
                                         style="display: flex;flex-direction:column;align-items:stretch;justify-content:start;width: 400px;height: 120px;padding: 20px 32px;z-index: 1">
                                        <el-text size="large" style="align-self: start">答题信息</el-text>
                                        <div style="display: flex;flex-direction: row;align-items: center">
                                            <div style="display: flex;flex-direction: column;margin-right: 8px">
                                                <el-tag style="align-self: start" type="info">题量</el-tag>
                                                <el-tag style="align-self: start" type="info">分区数</el-tag>
                                            </div>
                                            <router-link class="el-link el-link--info is-underline" :to="{name:'drawing-setting'}">在抽取设置中更改</router-link>
                                        </div>
                                        <div class="flex-blank-1"></div>
                                        <div style="display: flex;flex-direction: row;align-items: center">
                                            <el-tag style="align-self: start" type="info">分数段</el-tag>
                                            <router-link class="el-link el-link--info is-underline" :to="{name:''}">在批改设置中更改</router-link>
                                        </div>
                                        <div class="score-bar">
                                            <div style="width: 60%;background: var(--el-color-danger);height: 6px"></div>
                                            <div style="width: 20%;background: var(--el-color-warning);height: 6px"></div>
                                            <div style="width: 20%;background: var(--el-color-success);height: 6px"></div>
                                        </div>
                                    </div>
                                </div>
                                <div style="flex:1;width: 100%;background: var(--html-bg) var(--lighting-effect-background-2);z-index: 1;margin-top: 64px;display: flex;flex-direction: column;align-items: center;padding-bottom: 128px">
                                    <md-editor no-upload-img placeholder="描述" v-model="data.description"
                                               class="preview-only"
                                               preview-theme="vuepress" :toolbars-exclude="['save','catalog','github']"
                                               style="height: 100vh;max-width:calc(90vw - 100px)"
                                               :theme="UIMeta.colorScheme.value"
                                               :show-toolbar-name="UIMeta.mobile.value"
                                               :preview="!UIMeta.mobile.value"/>
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
                                                            align-items: center;justify-content: center" :src="data.icon" fit="contain">
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
                                    <div class="flex-blank-1"></div>
                                    <div class="panel-1" style="width: 400px;height: 120px;padding: 20px 32px;">
                                        <el-text>答题信息</el-text>
                                    </div>
                                </div>
                                <md-editor no-upload-img placeholder="描述" v-model="data.description"
                                           preview-theme="vuepress" :toolbars-exclude="['save','catalog','github']"
                                           style="height: 100vh;margin-top: 100px;max-width:calc(90vw - 100px)"
                                           :theme="UIMeta.colorScheme.value" :show-toolbar-name="UIMeta.mobile.value"
                                           :preview="!UIMeta.mobile.value"/>
                            </div>
                        </transition>
                    </div>
                    <div v-else-if="error" style="display:flex;flex-direction: column">
                        <el-empty description="获取信息失败"></el-empty>
                        <el-button link type="primary" @click="getData" size="large">重试</el-button>
                    </div>
                </transition>
            </div>
        </el-scrollbar>
    </div>
</template>

<style scoped>
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

.score-bar {
    display: flex;
    position: relative;
    overflow: hidden;
    background: var(--panel-bg-color-overlay) var(--lighting-effect-background-2);
    height: 6px;
    border-radius: 3px;
    margin-top: 4px;
}
</style>