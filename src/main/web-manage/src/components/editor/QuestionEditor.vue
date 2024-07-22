<script setup>
// import {nextTick, onBeforeMount, ref, watch} from "vue";
import {useRoute} from "vue-router";
import QuestionCache from "@/data/QuestionCache.js";
import randomUUID from "@/utils/UUID.js";
import {VueDraggable} from "vue-draggable-plus";
import HarmonyOSIcon_Plus from "@/components/icons/HarmonyOSIcon_Plus.vue";
import HarmonyOSIcon_Remove from "@/components/icons/HarmonyOSIcon_Remove.vue";
import HarmonyOSIcon_Handle from "@/components/icons/HarmonyOSIcon_Handle.vue";
import BasicQuestionEditor from "@/components/editor/BasicQuestionEditor.vue";
import SubQuestionEditor from "@/components/editor/SubQuestionEditor.vue";
import MultipleChoicesEditorPlugin from "@/components/editor/module/MultipleChoicesEditorModule.vue";
import QuestionPreview from "@/components/question/QuestionPreview.vue";

const {proxy} = getCurrentInstance();
const route = useRoute();
let questionInfo = ref({});

const error = ref(false);
const errorMessage = ref("");
const ready = ref(false);
const loading = ref(false);
let requested = false;
import Responsive from "@/utils/Responsive.js";

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
watch(() => route.params.id, update);
watch(() => questionInfo.value.question, (newVal, oldVal) => {
    if (error.value) return;
    if (newVal && oldVal && oldVal.id === newVal.id) {
        QuestionCache.update(questionInfo);
    }
    questionInfo.value.check();
}, {deep: true});

watch(() => questionInfo.value.questionInfos, (newVal, oldVal) => {
    QuestionCache.update(questionInfo);
    questionInfo.value.check();
});

onBeforeMount(() => {
    update(route.params.id, null);
});

const createQuestion = () => {
    QuestionCache.appendToGroup(
        questionInfo,
        QuestionCache.create({
            id: randomUUID(),
            content: "",
            type: "MultipleChoicesQuestion",
            enabled: false,
            partitionIds: null,
            authorQQ: Number(proxy.$cookies.get("qq")),
            upVoters: new Set(),
            downVoters: new Set(),
            choices: [{
                id: randomUUID(),
                correct: true,
                content: ""
            }, {
                id: randomUUID(),
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

const view = ref('编辑');
watch(() => view.value, () => {
    nextTick(() => {
        view1.value = view.value !== '预览';
    })
})
const view1 = ref(true);
const forceMobilePreview = ref(Responsive.mobile.value);
</script>

<template>
    <div v-loading="loading" style="height: 100%;display: flex;flex-direction: column;">
        <div style="display: flex;margin-left: 8px;margin-top: 8px;margin-bottom:4px;flex: none">
            <el-segmented v-model="view" :options="['编辑','预览']" block/>
            <transition name="preview-type-switch">
                <div style="display: flex;margin-left: 16px" v-if="view==='预览'">
                    <el-text style="margin-right: 4px;">移动端预览</el-text>
                    <el-switch v-model="forceMobilePreview"/>
                </div>
            </transition>
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
                                    <div v-else-if="questionInfo.type === 'QuestionGroup'" :key="'questionGroup'"
                                         style="display: flex;flex-direction:column;justify-items: stretch">
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
                                                    <div style="min-height: 0;grid-column: 2;">
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
                            </div>
                        </transition>
                    </div>
                    <div v-else>
                        <transition name="switch-preview" mode="out-in">
                            <question-preview :key="questionInfo.question.id" :questionInfo="questionInfo" :force-mobile="forceMobilePreview"/>
                        </transition>
                    </div>
                </transition>
            </div>
        </el-scrollbar>
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
</style>