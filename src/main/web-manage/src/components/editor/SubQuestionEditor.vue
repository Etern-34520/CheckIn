<script setup>
import getAvatarUrlOf from "@/utils/Avatar.js";
import HarmonyOSIcon_Plus from "@/components/icons/HarmonyOSIcon_Plus.vue";
import {ElNotification} from "element-plus";
import WebSocketConnector from "@/api/websocket.js";
import Like from "@/components/icons/Like.vue";
import DisLike from "@/components/icons/DisLike.vue";
import PartitionTempStorage from "@/data/PartitionTempStorage.js";
import CreateNewPartitionDialog from "@/components/CreateNewPartitionDialog.vue";
import UserDataInterface from "@/data/UserDataInterface.js";
import HarmonyOSIcon_Handle from "@/components/icons/HarmonyOSIcon_Handle.vue";
import Collapse from "@/components/Collapse.vue";
import MultipleChoicesEditorPlugin from "@/components/editor/plugin/MultipleChoicesEditorPlugin.vue";
import QuestionTempStorage from "@/data/QuestionTempStorage.js";

const {proxy} = getCurrentInstance();
const imageDialogVisible = ref(false);
const image = ref({name: "", data: ""});
const upload = ref();

const allUsers = ref([]);

UserDataInterface.getUsersAsync().then((users) => {
    allUsers.value.push(...users);
});

const questionInfo = defineModel("questionInfo", {
    required: true,
    type: Object
});

const group = questionInfo.value.getGroup();
if (group) {
    group.check();
} else {
    watch(() => questionInfo.value.getGroup(), (newVal, oldVal) => {
        questionInfo.value.getGroup().check();
    }, {once: true});
}

watch(() => questionInfo.value.question, (newVal, oldVal) => {
    const groupInfo = questionInfo.value.getGroup();
    groupInfo.check();
    QuestionTempStorage.update(groupInfo);
}, {deep: true});

const filter = function (rawFile) {
    if (rawFile.raw.type.startsWith("image")) {
        if (questionInfo.value.question.images)
            for (const image of questionInfo.value.question.images) {
                if (image.size === rawFile.raw.size) {
                    ElNotification({
                        title: "重复",
                        message: "图片已存在",
                        position: 'bottom-right',
                        type: 'warning',
                    })
                    upload.value.handleRemove(rawFile);
                    return false;
                }
            }
        else questionInfo.value.question.images = [];
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

const currentUserQQ = Number(proxy.$cookies.get("qq"));
// const like = ref(false);

// const dislike = ref(false);
const switchLike = () => {
    const like = questionInfo.value.question.upVoters.has(currentUserQQ);
    if (like) {
        questionInfo.value.question.upVoters.delete(currentUserQQ);
        WebSocketConnector.send({
            type: "restoreVote",
            questionId: questionInfo.value.question.id
        });
    } else {
        WebSocketConnector.send({
            type: "upVote",
            questionId: questionInfo.value.question.id
        });
        questionInfo.value.question.upVoters.add(currentUserQQ);
        questionInfo.value.question.downVoters.delete(currentUserQQ);
    }
}

const switchDisLike = () => {
    const disLike = questionInfo.value.question.downVoters.has(currentUserQQ);
    if (disLike) {
        WebSocketConnector.send({
            type: "restoreVote",
            questionId: questionInfo.value.question.id
        });
        questionInfo.value.question.downVoters.delete(currentUserQQ);
    } else {
        WebSocketConnector.send({
            type: "downVote",
            questionId: questionInfo.value.question.id
        });
        questionInfo.value.question.downVoters.add(currentUserQQ);
        questionInfo.value.question.upVoters.delete(currentUserQQ);
    }
}
</script>

<template>
    <collapse style="flex:1;overflow: hidden" :content-background="false" :title-background="false">
        <template #title>
            <div style="display: flex;flex-direction: column">
                <div style="display: flex;margin-bottom: 4px" class="alerts">
                    <transition-group name="alert">
                        <el-tag v-for="error of questionInfo.errors" :key="'error'+error.content" type="danger" :closable="false">
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
                        <el-tag v-for="warning of questionInfo.warnings" :key="'warning'+warning.content" type="warning" :closable="false">
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
                    <el-input class="question-content-input"
                              :class="{error:questionInfo.question.content===''||questionInfo.question.content===undefined}"
                              type="textarea" style="flex:4"
                              placeholder="内容" v-model="questionInfo.question.content"
                              autosize></el-input>
                    <div
                        style="flex-grow:1;width: 60px;margin-left: 2px;display: flex;flex-direction: column;justify-content: stretch">
                        <el-select v-model="questionInfo.question.authorQQ" filterable clearable placeholder="作者"
                                   :class="{warning:questionInfo.question.authorQQ===undefined||questionInfo.question.authorQQ===null}">
                            <el-option v-for="user of allUsers" :key="user.qq" :label="user.name" :value="user.qq">
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
                             style="padding: 2px;display: flex;flex-direction: row;align-items: center;flex:1">
                            <el-button-group>
                                <el-button link @click="switchLike" :disabled="questionInfo.localNew||questionInfo.remoteDeleted">
                                    <like :filled="questionInfo.question.upVoters.has(currentUserQQ)"/>
                                    <el-text style="margin-left: 4px;">{{ questionInfo.question.upVoters.size }}
                                    </el-text>
                                </el-button>
                                <el-button link @click="switchDisLike" :disabled="questionInfo.localNew||questionInfo.remoteDeleted">
                                    <DisLike :filled="questionInfo.question.downVoters.has(currentUserQQ)"/>
                                    <el-text style="margin-left: 4px;">{{ questionInfo.question.downVoters.size }}
                                    </el-text>
                                </el-button>
                            </el-button-group>
                        </div>
                    </div>
                </div>
            </div>
        </template>
        <template #content>
            <div style="margin-right: 32px;">
                <collapse :content-background="false">
                    <template #title>
                        <el-text style="line-height: 32px;margin-left: 8px;">图片</el-text>
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
                                       :on-preview="file => {image.name = file.name;image.data = file.url;imageDialogVisible = true}">
                                <HarmonyOSIcon_Plus :size="32"/>
                            </el-upload>
                        </div>
                    </template>
                </collapse>
                <multiple-choices-editor-plugin v-if="questionInfo.question.type === 'MultipleChoicesQuestion'"
                                                :question-info="questionInfo"/>
            </div>
        </template>
    </collapse>
    <el-dialog
        v-model="imageDialogVisible"
        :title="image.name"
        width="500"
        align-center>
        <el-image :src="image.data"/>
    </el-dialog>
</template>

<style scoped>
.question-image-upload > div {
    min-height: 152px;
}

/*noinspection CssUnusedSymbol*/
.alert-enter-active {
    transition:
        all 300ms var(--ease-in-bounce-1) 300ms,
        max-width 200ms var(--ease-in-out-quint),
        max-height 200ms var(--ease-in-out-quint),
        padding 200ms var(--ease-in-out-quint);
}

/*noinspection CssUnusedSymbol*/
.alert-leave-active {
    transition:
        all 300ms var(--ease-in-bounce-1) 0ms,
        max-width 200ms var(--ease-in-out-quint) 350ms,
        max-height 200ms var(--ease-in-out-quint) 350ms,
        padding 200ms var(--ease-in-out-quint) 350ms;
}

/*noinspection CssUnusedSymbol*/
.alert-enter-from, .alert-leave-to {
    opacity: 0;
    /*    margin-top: -40px;*/
    overflow: hidden;
    filter: blur(16px);
    max-width: 0 !important;
    max-height: 0 !important;
    padding: 0;
}

.alerts > * {
    max-height: 24px;
    max-width: 500px;
}

.alerts > *:not(:last-child) {
    margin-right: 4px;
}
</style>