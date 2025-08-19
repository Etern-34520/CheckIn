<script setup>
// import {nextTick, onBeforeMount, ref, watch} from "vue";
import QuestionCache from "@/data/QuestionCache.js";
import randomUUIDv4 from "@/utils/UUID.js";
import {VueDraggable} from "vue-draggable-plus";
import HarmonyOSIcon_Plus from "@/components/icons/HarmonyOSIcon_Plus.vue";
import HarmonyOSIcon_Remove from "@/components/icons/HarmonyOSIcon_Remove.vue";
import HarmonyOSIcon_Handle from "@/components/icons/HarmonyOSIcon_Handle.vue";
import BasicQuestionEditor from "@/pages/manage/serverGroup/questions/editor/BasicQuestionEditor.vue";
import SubQuestionEditor from "@/pages/manage/serverGroup/questions/editor/SubQuestionEditor.vue";
import MultipleChoicesEditorPlugin
    from "@/pages/manage/serverGroup/questions/editor/module/MultipleChoicesEditorModule.vue";
import QuestionPreview from "@/components/question/QuestionPreview.vue";
import router from "@/router/index.js";
import UIMeta from "@/utils/UI_Meta.js";
import Like from "@/components/icons/Like.vue";
import DisLike from "@/components/icons/DisLike.vue";
import WebSocketConnector from "@/api/websocket.js";
import LinkPanel from "@/components/common/LinkPanel.vue";
import {Check, RefreshLeft} from "@element-plus/icons-vue";
import UserDataInterface from "@/data/UserDataInterface.js";
import questionCache from "@/data/QuestionCache.js";
import Collapse from "@/components/common/Collapse.vue";
import {MdEditor} from "md-editor-v3";

let questionInfo = ref({});

const error = ref(false);
const errorMessage = ref("");
const ready = ref(false);
const loading = ref(false);
let requested = false;

const view = ref('编辑');
watch(() => view.value, () => {
    nextTick(() => {
        view1.value = view.value !== '预览';
    })
})
const view1 = ref(true);

let unwatch1;
let unwatch2;

let update = (newVal, oldVal) => {
    if (unwatch1)
        unwatch1();
    if (unwatch2)
        unwatch2();
    requested = false;
    setTimeout(() => {
        if (!requested) {
            loading.value = true;
        }
    }, 200);
    QuestionCache.getAsync(newVal).then((questionData) => {
        questionInfo.value = questionData;
        if (ready.value === false) ready.value = true;
        requested = true;
        loading.value = false;
        error.value = false;
        errorMessage.value = "";
        if (questionData.ableToEdit) {
            view.value = "编辑";
        } else {
            view.value = "预览";
        }
        unwatch1 = watch(() => questionInfo.value.question, (newVal, oldVal) => {
            if (error.value) return;
            if (newVal && oldVal && oldVal.id === newVal.id) {
                QuestionCache.update(questionInfo.value);
            }
        }, {deep: true});
        unwatch2 = watch(() => questionInfo.value.questionInfos, (newVal, oldVal) => {
            QuestionCache.update(questionInfo.value);
        })
    }, (rejectData) => {
        requested = true;
        loading.value = false;
        if (rejectData.message === "Question not found") {
            error.value = true;
            errorMessage.value = "题目不存在";
            questionInfo.value = {};
        } else {
            console.error(rejectData)
            error.value = true;
            errorMessage.value = rejectData.message;
        }
    });
};
// let changeLoop1 = false;
watch(() => router.currentRoute.value.params.id, update, {immediate: true});

const createQuestion = () => {
    QuestionCache.appendToGroup(
        questionInfo.value,
        QuestionCache.create({
            id: randomUUIDv4(),
            content: "",
            type: "MultipleChoicesQuestion",
            enabled: false,
            partitionIds: null,
            authorQQ: UserDataInterface.getCurrentUser().value.qq,
            upVoters: [],
            downVoters: [],
            inputMeta: {},
            choices: [{
                id: randomUUIDv4(),
                correct: true,
                content: ""
            }, {
                id: randomUUIDv4(),
                correct: false,
                content: ""
            }]
        }, false)
    );
    questionCache.update(questionInfo.value);
}

const removeQuestion = (index) => {
    questionInfo.value.questionInfos.splice(index, 1);
    questionCache.update(questionInfo.value);
}

const dragging = ref(false);
const onStartDrag = () => {
    dragging.value = true;
}
const onEndDrag = () => {
    nextTick(() => {
        dragging.value = false;
    });
}

const forceMobilePreview = ref(UIMeta.mobile.value);

const switchLike = () => {
    const upVoters = questionInfo.value.question.upVoters;
    const downVoters = questionInfo.value.question.downVoters;
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

const currentUserQQ = UserDataInterface.getCurrentUser().value.qq;

const restoreCurrent = () => {
    QuestionCache.restoreChanges(questionInfo.value.question.id);
}

const routeToRelatedExamRecords = () => {
    router.push({name: 'question-related-exams'})
}

const page = ref(null);

const switchType = ref(false);
onMounted(() => {
    watch(() => page.value, () => {
        setTimeout(() => {
            switchType.value = page.value !== null;
        }, 400);
    }, {immediate: true});
})
</script>

<template>
    <el-watermark :font="{color: 'rgba(128,128,128,0.01)', fontSize: 16}" :content="['',currentUserQQ]" :rotate="-45"
                  :zIndex="2000" :gap="[150,150]">
        <div style="height: 100%">
            <div class="slide-switch-base" style="height: 100%" :class="{'left-to-right':switchType}">
                <router-view v-slot="{ Component }">
                    <transition :name="switchType?'slide-left-to-right':'slide-right-to-left'">
                        <component ref="page" v-if="Component" :is="Component"/>
                        <div v-else v-loading="loading" style="height: 100%;display: flex;flex-direction: column;">
                            <div class="alerts" v-if="questionInfo.question">
                                <transition-group name="alert">
                                    <div v-if="questionInfo.ableToEdit" key="page-switch">
                                        <el-segmented v-model="view"
                                                      style="height: 26px;margin-top: 2px;margin-bottom: 2px;"
                                                      :options="['编辑','预览']" block/>
                                    </div>
                                    <div key="preview-type-switch" v-if="view==='预览'">
                                        <div style="display: flex">
                                            <el-text style="margin-right: 4px;margin-left: 8px;align-self: center">
                                                移动端预览
                                            </el-text>
                                            <el-switch style="align-self: center;margin-right: 8px;"
                                                       v-model="forceMobilePreview"/>
                                        </div>
                                    </div>
                                    <div v-for="(error,id) in questionInfo.errors" :key="'error-'+id"
                                         class="alert-item">
                                        <el-tag type="danger"
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
                                    </div>
                                    <div v-for="(warning,id) in questionInfo.warnings" :key="'warning-'+id"
                                         class="alert-item">
                                        <el-tag type="warning"
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
                                    </div>
                                </transition-group>
                            </div>
                            <el-scrollbar style="flex: 1">
                                <transition name="blur-scale">
                                    <div v-if="questionInfo.question"
                                         style="display: flex;margin: 4px 8px;flex-wrap: wrap">
                                        <transition name="blur-scale">
                                            <div style="display: flex;align-items: center;margin-right: 24px"
                                                 v-if="questionInfo.question.lastModifiedTime">
                                                <el-text type="info" style="margin-right: 8px">最后编辑时间</el-text>
                                                <el-text>
                                                    {{ questionInfo.question.lastModifiedTime }}
                                                </el-text>
                                            </div>
                                        </transition>
                                        <div style="display: flex;align-items: center;">
                                            <el-text type="info">评价</el-text>
                                            <el-button-group>
                                                <el-button link @click="switchLike" style="margin: 4px 0"
                                                           :disabled="questionInfo.localNew||questionInfo.remoteDeleted">
                                                    <like
                                                        :filled="questionInfo.question.upVoters.includes(currentUserQQ)"/>
                                                    <el-text style="margin-left: 4px;">
                                                        {{ questionInfo.question.upVoters.length }}
                                                    </el-text>
                                                </el-button>
                                                <el-button link @click="switchDisLike" style="margin: 4px 0"
                                                           :disabled="questionInfo.localNew||questionInfo.remoteDeleted">
                                                    <DisLike
                                                        :filled="questionInfo.question.downVoters.includes(currentUserQQ)"/>
                                                    <el-text style="margin-left: 4px;">{{
                                                            questionInfo.question.downVoters.length
                                                        }}
                                                    </el-text>
                                                </el-button>
                                            </el-button-group>
                                        </div>
                                        <div class="flex-blank-1"></div>
                                        <transition name="blur-scale">
                                            <el-button @click="restoreCurrent" v-if="questionInfo.dirty"
                                                       :icon="RefreshLeft"
                                                       class="disable-init-animate" link type="info">
                                                重置更改
                                            </el-button>
                                        </transition>
                                    </div>
                                </transition>
                                <div class="slide-switch-base" :class="{'left-to-right':view==='编辑'}">
                                    <transition :name="view==='编辑'?'slide-left-to-right':'slide-right-to-left'">
                                        <div v-if="view1">
                                            <transition name="page-content" mode="out-in">
                                                <el-empty v-if="error" :description="errorMessage"
                                                          style="height: 100%;"></el-empty>
                                                <div v-else-if="ready"
                                                     style="display: flex;flex-direction: column;padding: 4px 8px 8px 8px;box-sizing: border-box;height: 100%">
                                                    <basic-question-editor v-model:question-info="questionInfo"/>
                                                    <transition name="page-content" mode="out-in">
                                                        <multiple-choices-editor-plugin
                                                            v-if="questionInfo.question.type === 'MultipleChoicesQuestion'"
                                                            :question-info="questionInfo" :key="'multipleChoice'"/>
                                                        <div class="panel-1 question-input"
                                                             :class="questionInfo.inputMeta['subquestions-0']"
                                                             v-else-if="questionInfo.question.type === 'QuestionGroup'"
                                                             :key="'questionGroup'"
                                                             style="display: flex;flex-direction:column;justify-items: stretch;padding: 12px 20px;position: relative">
                                                            <div
                                                                style="display: flex;flex-direction: row;align-items: center">
                                                                <el-text>子题目</el-text>
                                                                <div class="flex-blank-1"></div>
                                                                <!--
                                                                                                            <el-text type="info" style="margin-right: 8px;">启用乱序子题目</el-text>
                                                                                                            <el-switch v-model="questionInfo.question.randomOrdered"/>
                                                                -->
                                                            </div>
                                                            <VueDraggable ref="draggable"
                                                                          v-model="questionInfo.questionInfos"
                                                                          :animation="150"
                                                                          handle=".handle"
                                                                          ghostClass="ghost"
                                                                          @start="onStartDrag"
                                                                          @end="onEndDrag"
                                                            >
                                                                <transition-group name="slide-hide">
                                                                    <div class="slide-hide-base"
                                                                         style="display: grid;margin-top: 4px;grid-template-columns: 0fr 1fr;"
                                                                         v-for="(questionInfo1, $index) of questionInfo.questionInfos"
                                                                         :key="questionInfo1.question.id">
                                                                        <div style="min-height: 0;grid-column: 1;">
                                                                            <div class="handle" style="cursor: grab">
                                                                                <HarmonyOSIcon_Handle/>
                                                                            </div>
                                                                            <transition name="delete-question-button">
                                                                                <el-button
                                                                                    class="remove-question-button"
                                                                                    v-show="questionInfo.questionInfos.length>2"
                                                                                    link
                                                                                    @click="removeQuestion($index)">
                                                                                    <HarmonyOSIcon_Remove/>
                                                                                </el-button>
                                                                            </transition>
                                                                        </div>
                                                                        <div style="min-height: 0;grid-column: 2"
                                                                             class="question-input"
                                                                             :class="'TODO'">
                                                                            <sub-question-editor
                                                                                v-model:question-info="questionInfo.questionInfos[$index]"/>
                                                                        </div>
                                                                    </div>
                                                                </transition-group>
                                                            </VueDraggable>
                                                            <el-button @click="createQuestion" style="margin-top: 4px"
                                                                       size="large" text>
                                                                <HarmonyOSIcon_Plus/>
                                                                <el-text>
                                                                    添加子题目
                                                                </el-text>
                                                            </el-button>
                                                        </div>
                                                    </transition>
                                                    <collapse style="margin-top: 48px;" expanded :content-background="false">
                                                        <template #title>
                                                            <div style="padding: 8px 16px;display: flex;flex-wrap: wrap">
                                                                <el-text style="margin-right: 24px">题目解析</el-text>
                                                                <div class="flex-blank-1"></div>
                                                                <el-text type="info">字数 {{ questionInfo.question.explanation?questionInfo.question.explanation.length:0 }}</el-text>
                                                            </div>
                                                        </template>
                                                        <template #content>
                                                            <el-input type="textarea" placeholder="解析" style="min-height: 200px"
                                                                      class="question-input explanation" autosize
                                                                      v-model="questionInfo.question.explanation"/>
                                                        </template>
                                                    </collapse>
                                                    <transition name="blur-scale">
                                                        <div
                                                            style="margin-top: 40px;min-height: 320px;padding: 4px 8px 8px 8px;"
                                                            v-if="questionInfo.question">
                                                            <el-text>
                                                                统计信息
                                                            </el-text>
                                                            <div style="margin-top: 12px">
                                                                <div class="panel-1"
                                                                     style="display: flex;flex-direction: row;flex-wrap: wrap;margin-bottom: 8px;">
                                                                    <el-statistic style="margin: 16px 32px;"
                                                                                  title="抽取次数"
                                                                                  :value="questionInfo.question.statistic?questionInfo.question.statistic.drewCount:0"></el-statistic>
                                                                    <el-statistic style="margin: 16px 32px;"
                                                                                  title="提交次数"
                                                                                  :value="questionInfo.question.statistic?questionInfo.question.statistic.submittedCount:0"></el-statistic>
                                                                    <el-statistic style="margin: 16px 32px;"
                                                                                  title="答对次数"
                                                                                  :value="questionInfo.question.statistic?questionInfo.question.statistic.correctCount:0"></el-statistic>
                                                                    <el-statistic style="margin: 16px 48px 16px 32px;"
                                                                                  title="答错次数"
                                                                                  :value="questionInfo.question.statistic?questionInfo.question.statistic.wrongCount:0"></el-statistic>
                                                                    <div class="flex-blank-1"></div>
                                                                </div>
                                                                <link-panel
                                                                    @click="routeToRelatedExamRecords"
                                                                    :description="'共 ' + (questionInfo.question.statistic?questionInfo.question.statistic.examDataCount:0) + ' 个'"
                                                                    name="使用了该题的测试" :icon="Check"></link-panel>
                                                            </div>
                                                        </div>
                                                    </transition>
                                                </div>
                                            </transition>
                                        </div>
                                        <div v-else>
                                            <transition name="switch-preview" mode="out-in">
                                                <div v-if="questionInfo.question" :key="questionInfo.question.id">
                                                    <question-preview :questionInfo="questionInfo"
                                                                      :force-mobile="forceMobilePreview"/>
                                                    <transition name="blur-scale">
                                                        <div
                                                            style="margin-top: 40px;min-height: 320px;padding: 4px 8px 8px 8px;"
                                                            v-if="questionInfo.question">
                                                            <el-text>
                                                                统计信息
                                                            </el-text>
                                                            <div style="margin-top: 12px">
                                                                <div class="panel-1"
                                                                     style="display: flex;flex-direction: row;flex-wrap: wrap;margin-bottom: 8px;">
                                                                    <el-statistic style="margin: 16px 32px;"
                                                                                  title="抽取次数"
                                                                                  :value="questionInfo.question.statistic?questionInfo.question.statistic.drewCount:0"></el-statistic>
                                                                    <el-statistic style="margin: 16px 32px;"
                                                                                  title="提交次数"
                                                                                  :value="questionInfo.question.statistic?questionInfo.question.statistic.submittedCount:0"></el-statistic>
                                                                    <el-statistic style="margin: 16px 32px;"
                                                                                  title="答对次数"
                                                                                  :value="questionInfo.question.statistic?questionInfo.question.statistic.correctCount:0"></el-statistic>
                                                                    <el-statistic style="margin: 16px 48px 16px 32px;"
                                                                                  title="答错次数"
                                                                                  :value="questionInfo.question.statistic?questionInfo.question.statistic.wrongCount:0"></el-statistic>
                                                                    <div class="flex-blank-1"></div>
                                                                </div>
                                                                <link-panel
                                                                    @click="routeToRelatedExamRecords"
                                                                    :description="'共 ' + (questionInfo.question.statistic?questionInfo.question.statistic.examDataCount:0) + ' 个'"
                                                                    name="使用了该题的测试" :icon="Check"></link-panel>
                                                            </div>
                                                        </div>
                                                    </transition>
                                                </div>
                                                <el-empty v-else/>
                                            </transition>
                                        </div>
                                    </transition>
                                </div>
                            </el-scrollbar>
                        </div>
                    </transition>
                </router-view>
            </div>
        </div>
    </el-watermark>
</template>

<style scoped>
.handle {
    width: 30px;
    aspect-ratio: 1;
    padding: 0;
    display: flex;
    align-items: center;
    justify-items: center;
    align-content: center;
    justify-content: center;
}

/*noinspection CssUnusedSymbol*/
.drag-move, .drag-enter-active, .drag-leave-active {
    transition: 0.4s;
    overflow: hidden;
}

/*noinspection CssUnusedSymbol*/
.drag-enter-from, .drag-leave-to {
    opacity: 0;
    height: 0;
    margin-bottom: 0 !important;
    margin-top: 0 !important;
}

/*noinspection CssUnusedSymbol*/
.page-content-enter-active,
.page-content-leave-active {
    transition: 400ms var(--ease-in-out-quint);
}

/*noinspection CssUnusedSymbol*/
.page-content-enter-from,
.page-content-leave-to {
    filter: blur(32px);
    scale: 0.9;
    opacity: 0;
}

/*noinspection CssUnusedSymbol*/
.delete-question-button-enter-active,
.delete-question-button-leave-active {
    transition: 300ms var(--ease-in-out-quint);
}

/*noinspection CssUnusedSymbol*/
.delete-question-button-enter-from,
.delete-question-button-leave-to {
    opacity: 0;
    transform: scale(0.8);
}

.remove-question-button {
    width: 30px;
    padding: 0;
    aspect-ratio: 1;
}

/*!*noinspection CssUnusedSymbol*!
.preview-type-switch-enter-active,
.preview-type-switch-leave-active {
    transition: 250ms var(--ease-in-out-quint);
}

!*noinspection CssUnusedSymbol*!
.preview-type-switch-enter-from,
.preview-type-switch-leave-to {
    filter: blur(8px);
    opacity: 0;
}*/

/*noinspection CssUnusedSymbol*/
.switch-preview-enter-active, .switch-preview-leave-active {
    transition: all 0.3s var(--ease-in-out-quint);
}

/*noinspection CssUnusedSymbol*/
.switch-preview-enter-from, .switch-preview-leave-to {
    filter: blur(32px);
    opacity: 0;
    transform: scale(0.98);
}


/*noinspection CssUnusedSymbol*/
.alert-enter-active {
    transition: grid-template-columns 200ms var(--ease-in-out-quint),
    max-height 200ms var(--ease-in-out-quint),
    opacity 300ms var(--ease-in-bounce) 350ms,
    filter 300ms var(--ease-in-bounce) 350ms;

    > * {
        transition: margin 200ms var(--ease-in-out-quint) 0ms,
        padding 200ms var(--ease-in-out-quint) 0ms;
    }
}

/*noinspection CssUnusedSymbol*/
.alert-leave-active {
    transition: grid-template-columns 200ms var(--ease-in-out-quint) 350ms,
    max-height 200ms var(--ease-in-out-quint) 350ms,
    opacity 300ms var(--ease-in-out-quint) 0ms,
    filter 300ms var(--ease-in-out-quint) 0ms;

    > * {
        transition: margin 200ms var(--ease-in-out-quint) 350ms,
        padding 200ms var(--ease-in-out-quint) 350ms;
    }
}

/*noinspection CssUnusedSymbol*/
.alert-enter-from, .alert-leave-to {
    opacity: 0;
    filter: blur(16px);
    grid-template-columns: 0fr !important;
    max-height: 0 !important;

    > * {
        margin: 0 !important;
        padding: 0 !important;
    }
}

/*noinspection CssUnusedSymbol*/
.alert-enter-to, .alert-leave-from {
    max-height: 36px !important;
}

.alerts {
    display: flex;
    justify-self: stretch;
    flex-wrap: wrap;
    margin: 4px 8px 8px;
}

.alert-item > * {
    margin-top: 4px !important;
    margin-bottom: 4px !important;
}

.alerts > * {
    max-height: 36px;
    display: grid;
    grid-template-columns: 1fr;
    /*    max-width: 500px;*/
}

.alerts > *:not(:last-child) > * {
    margin-right: 4px;
}
</style>
<style>
.alerts > * > * {
    min-width: 0;
}

.explanation textarea {
    padding: 24px;
    min-height: 200px;
}
</style>