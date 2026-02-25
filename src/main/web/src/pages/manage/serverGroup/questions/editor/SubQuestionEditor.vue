<script setup>
import getAvatarUrlOf from "@/utils/Avatar.js";
import HarmonyOSIcon_Plus from "@/components/icons/HarmonyOSIcon_Plus.vue";
import {ElNotification} from "element-plus";
import WebSocketConnector from "@/api/websocket.js";
import Like from "@/components/icons/Like.vue";
import DisLike from "@/components/icons/DisLike.vue";
import UserDataInterface from "@/data/UserDataInterface.js";
import Collapse from "@/components/common/Collapse.vue";
import MultipleChoicesEditorPlugin
    from "@/pages/manage/serverGroup/questions/editor/module/MultipleChoicesEditorModule.vue";
import QuestionCache from "@/data/QuestionCache.js";
import ImageViewer from "@/components/viewer/ImagesViewer.vue";
import UIMeta from "@/utils/UI_Meta.js";
import {MdEditor} from "md-editor-v3";
import PermissionInfo from "@/auth/PermissionInfo.js";
import {uuidv7} from "uuidv7";
import {Link, Picture} from "@element-plus/icons-vue";
import customSanitizeHtml from "@/utils/santize.js";

const imageDialogVisible = ref(false);
const viewerIndex = ref(0);
const upload = ref();

const allUsers = ref({});

UserDataInterface.getUsersAsync().then((users) => {
    allUsers.value = users;
});

const questionInfo = defineModel("questionInfo", {
    required: true,
    type: Object
});

const group = questionInfo.value.getGroup();
if (group) {
    group.verify();
} else {
    watch(() => questionInfo.value.getGroup(), (newVal, oldVal) => {
        questionInfo.value.getGroup().verify();
    }, {once: true});
}

watch(() => questionInfo.value.question, (newVal, oldVal) => {
    const groupInfo = questionInfo.value.getGroup();
    // groupInfo.verifyRaw();
    QuestionCache.update(groupInfo);
}, {deep: true});

const filter = function (rawFile) {
    if (rawFile.raw.type.startsWith("image")) {
        if (questionInfo.value.question.images === undefined) {
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

const currentUserQQ = UserDataInterface.getCurrentUser().value.qq;
// const like = ref(false);

// const dislike = ref(false);
const switchLike = () => {
    const downVoters = questionInfo.value.question.downVoters;
    const upVoters = questionInfo.value.question.upVoters;
    const like = upVoters.includes(currentUserQQ);
    if (like) {
        upVoters.splice(upVoters.indexOf(currentUserQQ), 1);
        WebSocketConnector.send({
            type: "restoreVote",
            data: {
                questionId: questionInfo.value.question.id
            }
        });
    } else {
        WebSocketConnector.send({
            type: "upVote",
            data: {
                questionId: questionInfo.value.question.id
            }
        });
        upVoters.push(currentUserQQ);
        downVoters.splice(downVoters.indexOf(currentUserQQ), 1);
    }
}

const switchDisLike = () => {
    const upVoters = questionInfo.value.question.upVoters;
    const downVoters = questionInfo.value.question.downVoters;
    const disLike = downVoters.includes(currentUserQQ);
    if (disLike) {
        downVoters.splice(downVoters.indexOf(currentUserQQ), 1);
        WebSocketConnector.send({
            type: "restoreVote",
            data: {
                questionId: questionInfo.value.question.id
            }
        });
    } else {
        WebSocketConnector.send({
            type: "downVote",
            data: {
                questionId: questionInfo.value.question.id
            }
        });
        downVoters.push(currentUserQQ);
        upVoters.splice(upVoters.indexOf(currentUserQQ), 1);
    }
}

const onPreview = (file) => {
    viewerIndex.value = questionInfo.value.question.images.findIndex(item => item.uid === file.uid);
    imageDialogVisible.value = true;
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
const sanitize = (html) => {
    const groupInfo = questionInfo.value.getGroup();
    if (groupInfo.question.unsafeXss) {
        return html;
    } else {
        return customSanitizeHtml(html);
    }
}
</script>

<template>
    <collapse style="flex:1;overflow: hidden" :content-background="false" :title-background="false" :clickable="false">
        <template #title>
            <div style="display: flex;flex-direction: column">
                <div style="display: flex;margin-bottom: 4px" class="alerts">
                    <transition-group name="alert">
                        <el-tag v-for="error of questionInfo.errors" :key="'error'+error.content" type="danger"
                                :closable="false">
                            <div style="display: flex;flex-direction: row;align-items: center;">
                                <el-text type="danger" style="margin: 4px">
                                    {{ error.content }}
                                </el-text>
                                <el-button-group>
                                    <el-button v-for="errorButton of error.buttons" link
                                               @click="errorButton.action" :type="errorButton.type">
                                        {{ errorButton.content }}
                                    </el-button>
                                </el-button-group>
                            </div>
                        </el-tag>
                        <el-tag v-for="warning of questionInfo.warnings" :key="'warning'+warning.content" type="warning"
                                :closable="false">
                            <div style="display: flex;flex-direction: row;align-items: center;">
                                <el-text type="warning" style="margin: 4px">
                                    {{ warning.content }}
                                </el-text>
                                <el-button-group>
                                    <el-button v-for="warningButton of warning.buttons" link
                                               @click="warningButton.action" :type="warningButton.type">
                                        {{ warningButton.content }}
                                    </el-button>
                                </el-button-group>
                            </div>
                        </el-tag>
                    </transition-group>
                </div>
                <div style="display: flex;flex-direction: row">
                    <div class="panel-1 question-input disable-init-animate"
                         :class="questionInfo.inputMeta['content-0']"
                         style="flex: 4;max-height: 80px;padding:8px 16px;margin: 0">
                        <el-scrollbar>
                            <div style="display: flex;flex-direction: column">
                                <el-text type="info" style="align-self: start">字数 {{ questionInfo.question.content.length }}</el-text>
                                <el-text style="align-self: start;word-break: break-all;word-wrap: break-word;white-space: break-spaces" >
                                    {{ questionInfo.question.content }}
                                </el-text>
                            </div>
                        </el-scrollbar>
                    </div>
                    <div style="flex-grow:1;width: 60px;margin-left: 2px;display: flex;flex-direction: column;justify-content: stretch">
                        <el-select v-model="questionInfo.question.authorQQ" filterable clearable placeholder="作者"
                                   :class="questionInfo.inputMeta['author-0']"
                                   :disabled="!ableToChangeAuthor(questionInfo)">
                            <template #label="{ label, value }">
                                <div style="display: flex;align-items: center;justify-items: stretch">
                                    <el-avatar shape="circle" :size="24" style="margin: 4px" fit="cover"
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
                        <div class="panel-1 disable-init-animate"
                             style="padding: 8px;display: flex;flex-direction: row;align-items: center;flex: 1">
                            <el-button-group>
                                <el-button link @click="switchLike" class="disable-init-animate"
                                           :disabled="questionInfo.localNew||questionInfo.remoteDeleted">
                                    <like :filled="questionInfo.question.upVoters.includes(currentUserQQ)"/>
                                    <el-text style="margin-left: 4px;">{{ questionInfo.question.upVoters.length }}
                                    </el-text>
                                </el-button>
                                <el-button link @click="switchDisLike" class="disable-init-animate"
                                           :disabled="questionInfo.localNew||questionInfo.remoteDeleted">
                                    <DisLike :filled="questionInfo.question.downVoters.includes(currentUserQQ)"/>
                                    <el-text style="margin-left: 4px;">{{ questionInfo.question.downVoters.length }}
                                    </el-text>
                                </el-button>
                            </el-button-group>
                        </div>
                    </div>
                </div>
            </div>
        </template>
        <template #content>
            <div style="margin-right: 30px;">
                <image-viewer v-if="questionInfo.question.images" :images="questionInfo.question.images"
                              v-model="imageDialogVisible" v-model:index="viewerIndex"/>
                <div style="position: relative;">
                    <div class="question-input" style="display: flex;min-height: 200px !important;"
                         :class="questionInfo.inputMeta['content-0']">
                        <md-editor no-upload-img placeholder="内容" v-model="questionInfo.question.content"
                                   :show-toolbar-name="UIMeta.touch.value" :footers="['scrollSwitch']"
                                   :key="UIMeta.colorScheme" preview-theme="vuepress" :sanitize="sanitize"
                                   :toolbars-exclude="['save','catalog','github']"
                                   :theme="UIMeta.colorScheme.value"/>
                    </div>
                </div>
                <collapse :content-background="false" class="question-input"
                          :class="questionInfo.inputMeta['images-0']">
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
                <multiple-choices-editor-plugin v-if="questionInfo.question.type === 'MultipleChoicesQuestion'"
                                                :question-info="questionInfo"/>
            </div>
        </template>
    </collapse>
</template>

<style scoped>
.question-image-upload > div {
    min-height: 152px;
}

/*noinspection CssUnusedSymbol*/
.alert-enter-active {
    transition: all 300ms var(--ease-in-bounce) 300ms,
    grid-template-columns 200ms var(--ease-in-out-quint),
    max-height 200ms var(--ease-in-out-quint),
    padding 200ms var(--ease-in-out-quint);
}

/*noinspection CssUnusedSymbol*/
.alert-leave-active {
    transition: all 300ms var(--ease-in-bounce) 0ms,
    grid-template-columns 200ms var(--ease-in-out-quint) 350ms,
    max-height 200ms var(--ease-in-out-quint) 350ms,
    padding 200ms var(--ease-in-out-quint) 350ms;
}

/*noinspection CssUnusedSymbol*/
.alert-enter-from, .alert-leave-to {
    opacity: 0;
    /*    margin-top: -40px;*/
    overflow: hidden;
    filter: blur(16px);
    grid-template-columns: 0fr !important;
    /*    max-width: 0 !important;*/
    max-height: 0 !important;
    padding: 0;
}

.alerts > * {
    max-height: 24px;
    display: grid;
    grid-template-columns: 1fr;
    /*    max-width: 500px;*/
}

.alerts > *:not(:last-child) {
    margin-right: 4px;
}
</style>
<style>
.alerts > * > * {
    min-width: 0;
}
</style>