<script setup>
import getAvatarUrlOf from "@/utils/Avatar.js";
import HarmonyOSIcon_Plus from "@/components/icons/HarmonyOSIcon_Plus.vue";
import {ElNotification} from "element-plus";
import WebSocketConnector from "@/api/websocket.js";
import Like from "@/components/icons/Like.vue";
import DisLike from "@/components/icons/DisLike.vue";
import PartitionCache from "@/data/PartitionCache.js";
import CreateNewPartitionDialog from "@/components/question/CreateNewPartitionDialog.vue";
import UserDataInterface from "@/data/UserDataInterface.js";
import Collapse from "@/components/common/Collapse.vue";
import toolbar from "@/data/MarkdownEditorToolbar.js"
import Vditor from "vditor";
import "vditor/dist/index.css"

const {proxy} = getCurrentInstance();
const imageDialogVisible = ref(false);
const image = ref({name: "", data: ""});
const upload = ref();

const allUsers = ref([]);

let partitions = PartitionCache.reactivePartitions;

UserDataInterface.getUsersAsync().then((users) => {
    allUsers.value.push(...users);
});

const questionInfo = defineModel("questionInfo", {
    required: true,
    type: Object
})

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

const isAdding = ref(false)

const onCreatingPartition = () => {
    isAdding.value = true
}

const hideCreating = () => {
    // createdPartitionName.value = ''
    isAdding.value = false
}


let lastPartitionId = questionInfo.value.question.partitionIds[0];

const preventEmptyPartition = (val) => {
    if (questionInfo.value.question.partitionIds.length === 0) {
        questionInfo.value.question.partitionIds.push(val);
    }
}

/**
 * For the bug that function preventEmptyPartition will not be called
 * when the selection of the multiple el-select is deleted by using backspace
 *
 * 修复多选el-select使用退格键删除选项时不会触发preventEmptyPartition的问题
 * */
const onPartitionChange = (val) => {
    if (val.length === 0) {
        questionInfo.value.question.partitionIds.push(lastPartitionId);
    } else {
        lastPartitionId = val[0];
    }
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

/*
const vditor = ref();

onMounted(() => {
    const vditor1 = new Vditor('vditor', {
        height: 360,
        resize: {
            "enable": true
        },
        theme: "dark",
        preview: {
            theme: {
                current: "dark"
            },
            hljs: {
                style: "native"
            }
        },
        tab: "\t",
        cache: {
            "enable": false
        },
        mode: "ir",
        value: questionInfo.value.question.content,
        placeholder: "内容"
    });
    vditor.value = vditor1;
})*/
</script>

<template>
    <el-dialog
        v-model="imageDialogVisible"
        :title="image.name"
        width="500"
        align-center>
        <el-image :src="image.data"/>
    </el-dialog>
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
    <!--    <el-input class="question-content-input"
                  :class="{error:questionInfo.question.content===''||questionInfo.question.content===undefined}"
                  type="textarea"
                  placeholder="内容" v-model="questionInfo.question.content"
                  autosize></el-input>-->
    <div style="position: relative;">
        <div class="panel-1" style="padding: 4px 16px">
            <el-link href="https://markdown.com.cn/basic-syntax/" target="_blank" type="info"
                     style="margin-right: 16px">Markdown指南
            </el-link>
<!--            <el-link href="https://katex.org/docs/supported.html" target="_blank" type="info"
                     style="margin-right: 16px">KaTeX指南
            </el-link>-->
            <el-link href="https://mermaid.nodejs.cn/intro/getting-started.html" target="_blank" type="info"
                     style="margin-right: 16px">Mermaid指南
            </el-link>
        </div>
        <div class="question-content-input" style="display: flex;max-height: 800px;min-height: 200px !important;"
             :class="{error:questionInfo.question.content===''||questionInfo.question.content===undefined}">
                        <v-md-editor placeholder="内容" v-model="questionInfo.question.content" :toolbar="toolbar"
                                     left-toolbar="undo redo clear | h bold italic strikethrough quote tip | ul ol table hr | link code mermaid"
                        ></v-md-editor >
<!--            <div id="vditor" ref="vditor"></div>-->
        </div>
    </div>
    <collapse :content-background="false" :expanded="true">
        <template #title>
            <div style="display: flex;flex-direction: row;align-items: center">
                <el-text style="line-height: 32px;margin-left: 8px;margin-right: 8px">图片</el-text>
                <el-tag type="info">{{
                        questionInfo.question.images ? questionInfo.question.images.length : 0
                    }}
                </el-tag>
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
                           :on-preview="file => {image.name = file.name;image.data = file.url;imageDialogVisible = true}">
                    <HarmonyOSIcon_Plus :size="32"/>
                </el-upload>
            </div>
        </template>
    </collapse>
    <div style="display: flex;flex-direction: row">
        <el-select
            class="not-empty"
            :class="{error:questionInfo.question.partitionIds&&questionInfo.question.partitionIds.length===0}"
            v-model="questionInfo.question.partitionIds"
            placeholder="分区"
            multiple
            filterable
            @focusout="hideCreating"
            @remove-tag="preventEmptyPartition"
            @change="onPartitionChange"
            style="flex:4;width:0"
        >
            <el-option v-for="partition of partitions" :key="partition.id"
                       :label="partition.name" :value="partition.id"></el-option>
            <template #footer>
                <transition name="creatingPartition" mode="out-in">
                    <el-button v-if="!isAdding" text bg size="small" style="width: 100%"
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
                 style="padding: 2px;display: flex;flex-direction: row;align-items: center">
                <el-text style="margin-left: 4px;">
                    启用
                </el-text>
                <el-switch v-model="questionInfo.question.enabled"/>
                <div class="flex-blank-1"></div>
                <el-button-group>
                    <el-button link @click="switchLike" :disabled="questionInfo.localNew||questionInfo.remoteDeleted">
                        <like :filled="questionInfo.question.upVoters.has(currentUserQQ)"/>
                        <el-text style="margin-left: 4px;">{{ questionInfo.question.upVoters.size }}
                        </el-text>
                    </el-button>
                    <el-button link @click="switchDisLike"
                               :disabled="questionInfo.localNew||questionInfo.remoteDeleted">
                        <DisLike :filled="questionInfo.question.downVoters.has(currentUserQQ)"/>
                        <el-text style="margin-left: 4px;">{{ questionInfo.question.downVoters.size }}
                        </el-text>
                    </el-button>
                </el-button-group>
            </div>
        </div>
    </div>
</template>

<style scoped>
.question-image-upload > div {
    min-height: 152px;
}

/*noinspection CssUnusedSymbol*/
.alert-enter-active {
    transition: all 300ms var(--ease-in-bounce-1) 300ms,
    grid-template-columns 200ms var(--ease-in-out-quint),
    max-height 200ms var(--ease-in-out-quint),
    padding 200ms var(--ease-in-out-quint);
}

/*noinspection CssUnusedSymbol*/
.alert-leave-active {
    transition: all 300ms var(--ease-in-bounce-1) 0ms,
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

/*noinspection CssUnusedSymbol*/
.creatingPartition-enter-active, .creatingPartition-leave-active {
    transition: all 0.3s var(--ease-in-bounce-1);
}

/*noinspection CssUnusedSymbol*/
.creatingPartition-enter-from, .creatingPartition-leave-to {
    opacity: 0;
    scale: 0.95;
}

</style>

<style>
.alerts > * > * {
    min-width: 0;
}
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