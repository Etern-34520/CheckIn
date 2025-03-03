<script setup>
// import {nextTick, onBeforeMount, ref, watch} from "vue";
import QuestionCache from "@/data/QuestionCache.js";
import randomUUIDv4 from "@/utils/UUID.js";
import {VueDraggable} from "vue-draggable-plus";
import HarmonyOSIcon_Plus from "@/components/icons/HarmonyOSIcon_Plus.vue";
import HarmonyOSIcon_Remove from "@/components/icons/HarmonyOSIcon_Remove.vue";
import HarmonyOSIcon_Handle from "@/components/icons/HarmonyOSIcon_Handle.vue";
import BasicQuestionEditor from "@/pages/serverGroup/questions/editor/BasicQuestionEditor.vue";
import SubQuestionEditor from "@/pages/serverGroup/questions/editor/SubQuestionEditor.vue";
import MultipleChoicesEditorPlugin from "@/pages/serverGroup/questions/editor/module/MultipleChoicesEditorModule.vue";
import QuestionPreview from "@/components/question/QuestionPreview.vue";
import router from "@/router/index.js";

const {proxy} = getCurrentInstance();
let questionInfo = ref({});

const error = ref(false);
const errorMessage = ref("");
const ready = ref(false);
const loading = ref(false);
let requested = false;
import UIMeta from "@/utils/UI_Meta.js";
import Like from "@/components/icons/Like.vue";
import DisLike from "@/components/icons/DisLike.vue";
import WebSocketConnector from "@/api/websocket.js";
import LinkPanel from "@/components/common/LinkPanel.vue";
import {ArrowRightBold, Check, RefreshLeft} from "@element-plus/icons-vue";
import PermissionInfo from "@/auth/PermissionInfo.js";
import UserDataInterface from "@/data/UserDataInterface.js";

// let ableToEditOwnQuestion = PermissionInfo.hasPermission('question', 'create and edit owns questions')
// let ableToEditOthersQuestion = PermissionInfo.hasPermission('question', 'edit others questions')
// let ableToEditOwnQuestionGroup = PermissionInfo.hasPermission('question group', 'create and edit owns question groups')
// let ableToEditOthersQuestionGroup = PermissionInfo.hasPermission('question group', 'edit others question groups')
// let ableToEdit = ref();
//
// const currentUser = UserDataInterface.getCurrentUser();

const view = ref('编辑');
watch(() => view.value, () => {
    nextTick(() => {
        view1.value = view.value !== '预览';
    })
})
const view1 = ref(true);

let update = (newVal, oldVal) => {
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
        // if (questionData.question)
        //     lastPartitionId = questionData.question.partitionIds[0];
        if (questionData.ableToEdit) {
            view.value = "编辑";
        } else {
            view.value = "预览";
        }
    }, (rejectData) => {
        requested = true;
        loading.value = false;
        if (rejectData.type === "question not found") {
            error.value = true;
            errorMessage.value = "题目不存在";
        }
    });
};
// let changeLoop1 = false;
watch(() => router.currentRoute.value.params.id, update);
watch(() => questionInfo.value.question, (newVal, oldVal) => {
    if (error.value) return;
    if (newVal && oldVal && oldVal.id === newVal.id) {
        QuestionCache.update(questionInfo);
    }
    questionInfo.value.verify();
}, {deep: true});

watch(() => questionInfo.value.questionInfos, (newVal, oldVal) => {
    QuestionCache.update(questionInfo);
    questionInfo.value.verify();
});

onBeforeMount(() => {
    update(router.currentRoute.value.params.id, null);
});

const createQuestion = () => {
    QuestionCache.appendToGroup(
            questionInfo,
            QuestionCache.create({
                id: randomUUIDv4(),
                content: "",
                type: "MultipleChoicesQuestion",
                enabled: false,
                partitionIds: null,
                authorQQ: Number(proxy.$cookies.get("qq")),
                upVoters: new Set(),
                downVoters: new Set(),
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
}

const removeQuestion = (index) => {
    questionInfo.value.questionInfos.splice(index, 1);
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
const currentUserQQ = Number(proxy.$cookies.get("qq"));

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
    <div style="height: 100%">
        <div class="slide-switch-base" style="height: 100%" :class="{'left-to-right':switchType}">
            <router-view v-slot="{ Component }">
                <transition :name="switchType?'slide-left-to-right':'slide-right-to-left'">
                    <component ref="page" v-if="Component" :is="Component"/>
                    <div v-else v-loading="loading" style="height: 100%;display: flex;flex-direction: column;">
                        <div style="display: flex;align-items: center;margin: 8px 8px 4px 2px;flex-wrap: wrap"
                             v-if="questionInfo.question">
                            <div style="display: flex;margin-left: 8px;margin-right:40px;flex: none;justify-content:stretch;flex-wrap: wrap">
                                <transition name="preview-type-switch">
                                    <el-segmented v-if="questionInfo.ableToEdit" v-model="view" :options="['编辑','预览']" block
                                                  style="margin-right: 16px"/>
                                </transition>
                                <transition name="preview-type-switch">
                                    <div style="display: flex;margin-left: 16px" v-if="view==='预览'">
                                        <el-text style="margin-right: 4px;">移动端预览</el-text>
                                        <el-switch v-model="forceMobilePreview"/>
                                    </div>
                                </transition>
                            </div>
                            <div class="flex-blank-1"></div>
                            <div style="display: flex;margin: 4px 8px">
                                <transition name="blur-scale">
                                    <el-button @click="restoreCurrent" v-if="questionInfo.dirty" :icon="RefreshLeft"
                                               class="disable-init-animate" link type="info">
                                        重置更改
                                    </el-button>
                                </transition>
                                <transition name="blur-scale">
                                    <div style="display: flex;align-items: center;margin-right: 24px"
                                         v-if="questionInfo.question.lastModifiedTime">
                                        <el-tag type="info">最后编辑时间</el-tag>
                                        <el-text>
                                            {{ questionInfo.question.lastModifiedTime }}
                                        </el-text>
                                    </div>
                                </transition>
                                <div style="display: flex;align-items: center;">
                                    <el-tag type="info">评价</el-tag>
                                    <el-button-group>
                                        <el-button link @click="switchLike" style="margin: 4px 0"
                                                   :disabled="questionInfo.localNew||questionInfo.remoteDeleted">
                                            <like :filled="questionInfo.question.upVoters.has(currentUserQQ)"/>
                                            <el-text style="margin-left: 4px;">{{ questionInfo.question.upVoters.size }}
                                            </el-text>
                                        </el-button>
                                        <el-button link @click="switchDisLike" style="margin: 4px 0"
                                                   :disabled="questionInfo.localNew||questionInfo.remoteDeleted">
                                            <DisLike :filled="questionInfo.question.downVoters.has(currentUserQQ)"/>
                                            <el-text style="margin-left: 4px;">{{ questionInfo.question.downVoters.size }}
                                            </el-text>
                                        </el-button>
                                    </el-button-group>
                                </div>
                            </div>
                        </div>
                        <div style="display: flex;justify-self: stretch;flex-wrap: wrap;margin-left: 8px;margin-right: 8px"
                             class="alerts">
                            <transition-group name="alert">
                                <el-tag v-for="(error,id) in questionInfo.errors" :key="'error-'+id" type="danger"
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
                                <el-tag v-for="(warning,id) in questionInfo.warnings" :key="'warning-'+id" type="warning"
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
                        <el-scrollbar style="flex: 1">
                            <div class="slide-switch-base" :class="{
                'left-to-right':view==='编辑'
            }">
                                <transition :name="view==='编辑'?'slide-left-to-right':'slide-right-to-left'">
                                    <div v-if="view1">
                                        <transition name="page-content" mode="out-in">
                                            <el-empty v-if="error" style="height: 100%;"></el-empty>
                                            <div v-else-if="ready"
                                                 style="display: flex;flex-direction: column;padding: 4px 8px 8px 8px;box-sizing: border-box;height: 100%">
                                                <basic-question-editor v-model:question-info="questionInfo"/>
                                                <transition name="page-content" mode="out-in">
                                                    <multiple-choices-editor-plugin
                                                            v-if="questionInfo.question.type === 'MultipleChoicesQuestion'"
                                                            :question-info="questionInfo" :key="'multipleChoice'"/>
                                                    <div class="panel-1 question-input"
                                                         :class="questionInfo.inputMeta['subquestions-0']"
                                                         v-else-if="questionInfo.question.type === 'QuestionGroup'" :key="'questionGroup'"
                                                         style="display: flex;flex-direction:column;justify-items: stretch;padding: 12px 20px;position: relative">
                                                        <div style="display: flex;flex-direction: row;align-items: center">
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
                                                            <transition-group :name="/*dragging ? null:'drag'*/'slide-hide'">
                                                                <div class="slide-hide-base"
                                                                     style="display: grid;margin-top: 4px;grid-template-columns: 0fr 1fr;"
                                                                     v-for="(questionInfo1, $index) of questionInfo.questionInfos"
                                                                     :key="questionInfo1.question.id">
                                                                    <div style="min-height: 0;grid-column: 1;">
                                                                        <div class="handle" style="cursor: grab">
                                                                            <HarmonyOSIcon_Handle/>
                                                                        </div>
                                                                        <transition name="delete-question-button">
                                                                            <el-button class="remove-question-button"
                                                                                       v-show="questionInfo.questionInfos.length>2" link
                                                                                       @click="removeQuestion($index)">
                                                                                <HarmonyOSIcon_Remove/>
                                                                            </el-button>
                                                                        </transition>
                                                                    </div>
                                                                    <div style="min-height: 0;grid-column: 2" class="question-input"
                                                                         :class="'TODO'">
                                                                        <sub-question-editor
                                                                                v-model:question-info="questionInfo.questionInfos[$index]"/>
                                                                    </div>
                                                                </div>
                                                            </transition-group>
                                                        </VueDraggable>
                                                        <el-button @click="createQuestion" style="margin-top: 4px" size="large" text>
                                                            <HarmonyOSIcon_Plus/>
                                                            <el-text>
                                                                添加子题目
                                                            </el-text>
                                                        </el-button>
                                                    </div>
                                                </transition>
                                                <div class="panel-1" style="padding: 12px 20px;margin-top: 40px;min-height: 320px">
                                                    <el-text>
                                                        统计信息
                                                    </el-text>
                                                    <div style="margin-top: 12px">
                                                        <div class="panel-1"
                                                             style="display: flex;flex-direction: row;flex-wrap: wrap;margin-bottom: 8px;">
                                                            <el-statistic style="margin: 16px 32px;" title="抽取次数"
                                                                          :value="questionInfo.question.statistic?questionInfo.question.statistic.drewCount:0"></el-statistic>
                                                            <el-statistic style="margin: 16px 32px;" title="提交次数"
                                                                          :value="questionInfo.question.statistic?questionInfo.question.statistic.submittedCount:0"></el-statistic>
                                                            <el-statistic style="margin: 16px 32px;" title="答对次数"
                                                                          :value="questionInfo.question.statistic?questionInfo.question.statistic.correctCount:0"></el-statistic>
                                                            <el-statistic style="margin: 16px 48px 16px 32px;" title="答错次数"
                                                                          :value="questionInfo.question.statistic?questionInfo.question.statistic.wrongCount:0"></el-statistic>
                                                            <div class="flex-blank-1"></div>
                                                            <!--                                            <el-button link size="large" style="margin: 8px 16px;min-height: 48px">
                                                                                                            <el-text size="large" style="margin-right: 8px;">
                                                                                                                查看作答详情
                                                                                                            </el-text>
                                                                                                            <el-icon size="large">
                                                                                                                <ArrowRightBold/>
                                                                                                            </el-icon>
                                                                                                        </el-button>-->
                                                        </div>
                                                        <link-panel
                                                                @click="routeToRelatedExamRecords"
                                                                :description="'共 ' + (questionInfo.question.statistic?questionInfo.question.statistic.examDataCount:0) + ' 个'"
                                                                name="使用了该题的测试" :icon="Check"></link-panel>
                                                    </div>
                                                </div>
                                            </div>
                                        </transition>
                                    </div>
                                    <div v-else>
                                        <transition name="switch-preview" mode="out-in">
                                            <question-preview v-if="questionInfo.question" :key="questionInfo.question.id"
                                                              :questionInfo="questionInfo" :force-mobile="forceMobilePreview"/>
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

/*noinspection CssUnusedSymbol*/
.preview-type-switch-enter-active,
.preview-type-switch-leave-active {
    transition: 250ms var(--ease-in-out-quint);
}

/*noinspection CssUnusedSymbol*/
.preview-type-switch-enter-from,
.preview-type-switch-leave-to {
    filter: blur(8px);
    opacity: 0;
}

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
    margin-top: 4px !important;
    margin-bottom: 4px !important;
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