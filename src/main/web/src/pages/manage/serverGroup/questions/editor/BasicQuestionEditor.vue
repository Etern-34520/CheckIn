<script setup>
import getAvatarUrlOf from "@/utils/Avatar.js";
import HarmonyOSIcon_Plus from "@/components/icons/HarmonyOSIcon_Plus.vue";
import {ElNotification} from "element-plus";
import PartitionCache from "@/data/PartitionCache.js";
import CreateNewPartitionDialog from "@/components/question/CreateNewPartitionPop.vue";
import UserDataInterface from "@/data/UserDataInterface.js";
import Collapse from "@/components/common/Collapse.vue";
import ImageViewer from "@/components/viewer/ImagesViewer.vue";
import {MdEditor} from "md-editor-v3";
import UIMeta from "@/utils/UI_Meta.js";
import 'md-editor-v3/lib/style.css';
import PermissionInfo from "@/auth/PermissionInfo.js";
import {Link, Picture} from "@element-plus/icons-vue"
import {uuidv7} from "uuidv7";

const imageViewerVisible = ref(false);
const upload = ref();

const allUsers = ref({});

let partitions = PartitionCache.refPartitions;

UserDataInterface.getUsersAsync().then((users) => {
    allUsers.value = users;
});

const questionInfo = defineModel("questionInfo", {
    required: true,
    type: Object
})

const filter = function (rawFile) {
    if (rawFile.raw.type.startsWith("image")) {
        if (!questionInfo.value.question.images === undefined) {
            questionInfo.value.question.images = [];
        }
        upload.value.handleRemove(rawFile);
        convert(rawFile.raw).then((result) => {
            questionInfo.value.question.images.push(result);
        });
        return true;
    } else {
        ElNotification({
            title: "类型错误",
            message: "请选择图片",
            position: 'bottom-right',
            type: 'error',
        })
        return false;
    }
}

const fileToDataUrl = (file) => {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        const onLoad = () => {
            resolve({
                name: file.name,
                url: reader.result,
                size: file.size,
                status: "success"
            });
        };
        const onError = () => {
            reject();
        }
        reader.addEventListener("load", onLoad);
        reader.addEventListener("error", onError);
        reader.readAsDataURL(file);
    })
}

const convert = (file) => {
    return new Promise((resolve, reject) => {
        if (file instanceof File) {
            fileToDataUrl(file).then((result) => {
                // upload.value.handleRemove(rawFile);
                resolve(result);
            });
        } else {
            reject(data);
        }
    });
}

const isAdding = ref(false)

const onCreatingPartition = () => {
    isAdding.value = true
}

const hideCreating = () => {
    // createdPartitionName.value = ''
    isAdding.value = false
}


// let lastPartitionId = questionInfo.value.question?.partitionIds[0];

const preventEmptyPartition = (val) => {
    if (questionInfo.value.question.partitionIds.length === 0) {
        questionInfo.value.question.partitionIds.push(val);
    }
}

/*
/!**
 * For the bug that function preventEmptyPartition will not be called
 * when the selection of the multiple el-select is deleted by using backspace
 *
 * 修复多选el-select使用退格键删除选项时不会触发preventEmptyPartition的问题
 * *!/
const onPartitionChange = (val) => {
    if (val.length === 0) {
        questionInfo.value.question.partitionIds.push(lastPartitionId);
    } else {
        lastPartitionId = val[0];
    }
}
*/

const viewerIndex = ref(0);

const onPreview = (file) => {
    viewerIndex.value = questionInfo.value.question.images.findIndex(item => item.uid === file.uid);
    imageViewerVisible.value = true;
}

const ableToSwitchEnable = () => {
    let ableToSwitchEnableQuestion = PermissionInfo.hasPermission('question', 'enable and disable questions')
    let ableToSwitchEnableQuestionGroup = PermissionInfo.hasPermission('question group', 'enable and disable question groups')
    let ableToSwitchEnable;
    if (questionInfo.value.question.type === "QuestionGroup") {
        ableToSwitchEnable = ableToSwitchEnableQuestionGroup;
    } else {
        ableToSwitchEnable = ableToSwitchEnableQuestion;
    }
    return ableToSwitchEnable;
}

const ableToChangeAuthor = () => {
    let ableToChangeQuestionAuthor = PermissionInfo.hasPermission('question', 'change question author')
    let ableToChangeQuestionGroupAuthor = PermissionInfo.hasPermission('question group', 'change question group author')
    let ableToChangeAuthor;
    if (questionInfo.value.question.type === "QuestionGroup") {
        ableToChangeAuthor = ableToChangeQuestionGroupAuthor;
    } else {
        ableToChangeAuthor = ableToChangeQuestionAuthor;
    }
    return ableToChangeAuthor;
}

const addUrlImageVisible = ref(false);
const newImageURL = ref();
const newImageError = ref(false);
const newImageVerified = ref(false);
const confirmAddUrlImage = () => {
    if (!questionInfo.value.question.images) {
        questionInfo.value.question.images = [];
    }
    questionInfo.value.question.images.push({
        name: uuidv7(),
        size: newImageURL.value.length * 0.75,
        url: newImageURL.value,
    });
    addUrlImageVisible.value = false;
}

watch(() => newImageURL.value, () => {
    newImageError.value = false;
})

const newImageLoadError = () => {
    newImageError.value = true;
    newImageVerified.value = true;
}
const newImageLoaded = () => {
    newImageVerified.value = true;
}
</script>

<template>
    <image-viewer v-if="questionInfo.question.images" :images="questionInfo.question.images"
                  v-model="imageViewerVisible" v-model:index="viewerIndex"/>
    <collapse :content-background="false" class="question-input" :class="questionInfo.inputMeta['content-0']" expanded>
        <template #title>
            <div style="padding: 8px 16px;display: flex;flex-wrap: wrap">
                <el-text style="margin-right: 24px">题目内容</el-text>
                <div style="display: flex">
                    <el-link href="https://markdown.com.cn/basic-syntax/" target="_blank" type="info"
                             style="margin-right: 16px" @click.stop>Markdown指南
                    </el-link>
                    <el-link href="https://mermaid.nodejs.cn/intro/getting-started.html" target="_blank" type="info"
                             style="margin-right: 16px" @click.stop>Mermaid指南
                    </el-link>
                </div>
                <div class="flex-blank-1"></div>
                <el-text type="info">字数 {{ questionInfo.question.content.length }}</el-text>
            </div>
        </template>
        <template #content>
            <div style="display: flex;min-height: 450px !important;">
                <md-editor no-upload-img placeholder="内容" v-model="questionInfo.question.content"
                           preview-theme="vuepress" :toolbars-exclude="['save','catalog','github']"
                           style="height: 50dvh;min-height: 450px;"
                           :theme="UIMeta.colorScheme.value" :show-toolbar-name="UIMeta.touch.value"
                           :preview="!UIMeta.mobile.value" :footers="['scrollSwitch']"/>
            </div>
        </template>
    </collapse>
    <collapse :content-background="false"
              :expanded="questionInfo.question.images && questionInfo.question.images.length > 0"
              class="question-input" :class="questionInfo.inputMeta['images-0']">
        <template #title>
            <div style="display: flex;flex-direction: row;align-items: center">
                <el-text style="line-height: 32px;margin-left: 16px;margin-right: 8px">图片</el-text>
                <div class="flex-blank-1"></div>
                <el-text type="info" style="margin-right: 16px">
                    数量
                    {{
                        questionInfo.question.images ? questionInfo.question.images.length : 0
                    }}
                </el-text>
            </div>
        </template>
        <template #content>
            <div class="question-image-upload panel-1" style="padding: 16px">
                <el-upload ref="upload"
                           v-model:file-list="questionInfo.question.images"
                           list-type="picture-card"
                           multiple
                           :auto-upload="false"
                           action="ignore"
                           :on-change="filter"
                           :on-preview="onPreview">
                    <div style="display: flex;flex-direction: column;gap: 4px">
                        <div style="display: flex;flex-direction: row;align-items: center;justify-content: center">
                            <HarmonyOSIcon_Plus style="align-self: center;margin-right: 4px"/>
                            <el-text style="align-self: center">选择文件</el-text>
                        </div>
                        <el-text type="info">或</el-text>
                        <el-popover trigger="click" @click.stop popper-style="width: 260px" v-model:visible="addUrlImageVisible">
                            <template #reference>
                                <el-button link @click.stop="newImageURL = ''" :icon="Link" class="disable-init-animate">
                                    <el-text type="primary">使用 URL</el-text>
                                </el-button>
                            </template>
                            <template #default>
                                <div class="no-pop-padding" style="display: flex;flex-direction: column;width: 260px">
                                    <el-image style="width: 260px;min-height: 180px;border-radius: 4px;margin-bottom: 4px"
                                              :src="newImageURL"
                                              @error="newImageLoadError()" @load="newImageLoaded()">
                                        <template #error>
                                            <div style="display: flex;align-items: center;justify-content: center;height: 180px">
                                                <el-icon style="align-self: center;justify-self: center" :size="32">
                                                    <Picture/>
                                                </el-icon>
                                            </div>
                                        </template>
                                    </el-image>
                                    <div style="display: flex;flex-direction: row;width: 260px">
                                        <el-input placeholder="URL" class="disable-init-animate" style="margin-right: 4px"
                                                  v-model="newImageURL"/>
                                        <el-button type="primary" class="disable-init-animate"
                                                   :disabled="!newImageURL || !newImageVerified || newImageError"
                                                   @click="confirmAddUrlImage">
                                            完成
                                        </el-button>
                                    </div>
                                </div>
                            </template>
                        </el-popover>
                    </div>
                </el-upload>
            </div>
        </template>
    </collapse>
    <div style="display: flex;flex-direction: row;margin-bottom: 8px">
        <el-select
            class="not-empty" :class="questionInfo.inputMeta['partitions-0']"
            v-model="questionInfo.question.partitionIds"
            placeholder="分区"
            multiple
            filterable
            @focusout="hideCreating"
            @remove-tag="preventEmptyPartition"
            style="flex:4;width:0">
            <el-option v-for="(partition,id) in partitions" :key="partition.id"
                       :label="partition.name" :value="partition.id"></el-option>
            <template #footer v-if="PermissionInfo.hasPermission('partition', 'create partition')">
                <transition name="creatingPartition" mode="out-in">
                    <el-button v-if="!isAdding" class="disable-init-animate" text bg style="width: 100%"
                               @click="onCreatingPartition">
                        创建新分区
                    </el-button>
                    <template v-else>
                        <createNewPartitionDialog @on-cancel="hideCreating" @on-confirm="hideCreating"/>
                    </template>
                </transition>
            </template>
        </el-select>
        <div style="flex-grow:1;width: 60px;margin-left: 2px;display: flex;flex-direction: column">
            <el-select v-model="questionInfo.question.authorQQ" :class="questionInfo.inputMeta['author-0']" filterable
                       clearable placeholder="作者" :disabled="!ableToChangeAuthor(questionInfo)">
                <template #label="{ label, value }">
                    <div style="display: flex;align-items: center;justify-items: stretch">
                        <el-avatar shape="circle" :size="24" style="margin: 4px;min-width: 24px;" fit="cover"
                                   :src="getAvatarUrlOf(value)"></el-avatar>
                        <span>{{ label }}</span>
                    </div>
                </template>
                <el-option v-for="(user,i) in allUsers" :key="user.qq" :label="user.name" :value="user.qq">
                    <div style="display: flex;align-items: stretch;justify-items: stretch">
                        <el-avatar shape="circle" :size="28" style="margin: 4px" fit="cover"
                                   :src="getAvatarUrlOf(user.qq)"></el-avatar>
                        <span>{{ user.name }}</span>
                        <span style="margin-left: 4px;color:var(--el-text-color-secondary)">{{
                                user.qq
                            }}</span>
                    </div>
                </el-option>
            </el-select>
            <div class="panel-1"
                 style="padding: 2px;display: flex;flex-direction: row;align-items: center">
                <el-text style="margin: 0 8px;">
                    启用
                </el-text>
                <el-switch v-model="questionInfo.question.enabled" :disabled="!ableToSwitchEnable(questionInfo)"/>
            </div>
        </div>
    </div>
</template>

<style scoped>
.question-image-upload > div {
    min-height: 152px;
}

/*noinspection CssUnusedSymbol*/
.creatingPartition-enter-active, .creatingPartition-leave-active {
    transition: all 0.3s var(--ease-in-bounce);
}

/*noinspection CssUnusedSymbol*/
.creatingPartition-enter-from, .creatingPartition-leave-to {
    opacity: 0;
    scale: 0.95;
}

</style>

<style>

/*

#vditor {
    width: 100% !important;
    background: var(--panel-bg-color) linear-gradient(180deg, rgba(255, 255, 255, 0.09) 0, transparent 2px calc(100% - 3px), rgba(0, 0, 0, 0.2) 100%);
    border-radius: 4px;
}

.vditor-ir pre.vditor-reset {
    background: none !important;
}

.vditor-toolbar {
    background: none;
    padding: 6px 12px;
}

.vditor--fullscreen {
    background: var(--panel-bg-color);
    backdrop-filter: blur(64px);
    z-index: 10000 !important;
}

.vditor-tooltipped--hover::after,
.vditor-tooltipped:hover::after,
.vditor-tooltipped:active::after,
.vditor-tooltipped:focus::after {
    background: var(--panel-bg-color-overlay) linear-gradient(180deg, rgba(255, 255, 255, 0.08) 0, transparent 2px calc(100% - 2px), rgba(0, 0, 0, 0.07) 100%);
    backdrop-filter: blur(8px);
    border-radius: 4px;
}

.vditor-tooltipped--hover::before,
.vditor-tooltipped:hover::before,
.vditor-tooltipped:active::before,
.vditor-tooltipped:focus::before {
    border-top-color: var(--panel-bg-color-overlay) !important;
}
*/

</style>