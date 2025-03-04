<script setup>
import PartitionCache from "../../data/PartitionCache.js";
import Collapse from "@/components/common/Collapse.vue";
import router from "@/router/index.js";
import getAvatarUrlOf from "@/utils/Avatar.js";

const props = defineProps({
    questionInfo: Object,
    disableErrorAndWarning: {
        type: Boolean,
        default: false,
    },
    subQuestionExpanded: {
        type: Boolean,
        default: false,
    }
});

const partitionNames = ref({});

if (props.questionInfo.question.partitionIds instanceof Array) {
    PartitionCache.getNamesSyncByIds(props.questionInfo.question.partitionIds).then((names) => {
        partitionNames.value = names;
    });
}
/*
for (const partitionId of props.questionInfo.question.partitionIds) {
    PartitionCache.getNameSync(partitionId).then((value) => {
        partitionNames.value.push(value);
    });
}
*/
const getTypeName = (type) => {
    switch (type) {
        case "MultipleChoicesQuestion":
            return "选择题";
        case "QuestionGroup":
            return "题组";
        default :
            return type;
    }
}
</script>


<template>
    <div class="panel-1 question-info-panel">
        <div class="grid1">
            <div class="padding">
                <div style="display: flex;flex-direction: row;flex-wrap: wrap;">
                    <div style="width: 6px;height: 6px;align-self: center;border-radius: 3px;margin: 8px;"
                         :style="questionInfo.question.enabled?'background: var(--el-color-primary);':'background: var(--el-color-info);'"></div>
                    <div style="display: flex;flex-direction: row;flex: 1">
                        <el-text type="info" size="small" style="margin-right: 16px">类型</el-text>
                        <el-text size="small">
                            {{
                                getTypeName(questionInfo.question.type ? questionInfo.question.type : questionInfo.type)
                            }}
                        </el-text>
                    </div>
                    <transition name="blur-scale">
                        <div style="display: flex;flex-direction: row;flex: 1" v-if="questionInfo.question.authorQQ">
                            <el-text type="info" size="small" style="margin-right: 16px">作者</el-text>
                            <el-button
                                    @click.stop="router.push({name:'user-detail', params: {id: questionInfo.question.authorQQ}})"
                                    text
                                    style="margin-right: 6px;padding: 4px;transition: 200ms var(--ease-in-out-quint)">
                                <el-avatar shape="circle" :src="getAvatarUrlOf(questionInfo.question.authorQQ)"
                                           style="margin-right: 4px;width: 20px;height: 20px"></el-avatar>
                                <el-text style="margin-left: 4px;">{{ questionInfo.question.authorQQ }}</el-text>
                            </el-button>
                        </div>
                    </transition>
                </div>
                <div class="question-content flex-blank-1 disable-init-animate">
                    <el-text type="info" size="small" style="margin-right: 16px">内容</el-text>
                    <el-scrollbar :max-height="120" style="padding: 0;flex: 1;margin-bottom: 8px">
                        <el-text>
                            <pre style="word-wrap: break-word;white-space: pre-wrap;">{{
                                    questionInfo.question.content
                                }}</pre>
                        </el-text>
                    </el-scrollbar>
                </div>
                <div style="display: flex;flex-direction: row;margin-bottom: 4px"
                     v-if="questionInfo.question.choices!==undefined&&questionInfo.question.choices!==null">
                    <el-text type="info" size="small" style="margin-right: 16px;word-break: keep-all">选项</el-text>
                    <div>
                        <el-tag v-for="choice of questionInfo.question.choices"
                                style="margin-bottom: 4px;margin-right: 4px"
                                :type="choice.correct?'success':'danger'">
                            {{ choice.content }}
                        </el-tag>
                    </div>
                </div>
                <div v-if="questionInfo.question.type==='QuestionGroup'" style="margin-bottom: 4px">
                    <collapse @click.stop :expanded="subQuestionExpanded" :content-background="false">
                        <template #title>
                            <el-text style="line-height: 32px;margin-left: 8px;">子题目</el-text>
                        </template>
                        <template #content>
                            <transition-group name="slide-hide">
                                <QuestionInfoPanel v-for="subQuestionInfo of questionInfo.questionInfos"
                                                   :key="subQuestionInfo.question.id" :disable-error-and-warning="disableErrorAndWarning"
                                                   :question-info="subQuestionInfo" :clickable="false"/>
                            </transition-group>
                        </template>
                    </collapse>
                </div>
                <div v-if="questionInfo.question.partitionIds!==undefined&&questionInfo.question.partitionIds!==null">
                    <el-text type="info" size="small" style="margin-right: 16px">分区</el-text>
                    <el-tag
                            v-for="(partitionName,partitionId,index) in partitionNames"
                            type="info">
                        {{ partitionName }}
                    </el-tag>
                </div>
                <div class="errorsDescription" v-if="!disableErrorAndWarning">
                    <transition-group name="errorDescriptions">
                        <el-text v-for="error of questionInfo.errors"
                                 :key="error.content?error.content:''"
                                 type="danger">
                            {{ error.content }}
                        </el-text>
                    </transition-group>
                </div>
                <slot/>
            </div>
        </div>
    </div>
</template>

<style scoped>

.dragHover * {
    color: var(--front-color-dark) !important;
}

.question-info-panel {
    display: grid;
    margin-top: 2px;
    padding: 0;
    min-height: 0;
    grid-template-rows: 1fr;
    transition: transform 0.2s var(--ease-in-out-quint),
    background-color 0.2s var(--ease-in-out-quint);
}

.question-info-panel.clickable:not(:has(.clickable:active)):active {
    transform: scale(0.98);
}

.padding {
    padding: 8px;
}

.question-info-panel > .grid1 > .padding > div {
    min-height: 25px;
}

.question-info-panel > .grid1 {
    min-height: 0;
}

.errorsDescription > * {
    margin-right: 8px;
}

.choicesList > * {
    margin-right: 4px;
}

.question-content {
    display: flex;
    flex-direction: row;
    min-height: 30px !important;
}

.errorDescriptions-enter-active, .errorDescriptions-leave-active {
    transition: 0.2s var(--ease-in-out-quint);
}

.errorDescriptions-enter-from, .errorDescriptions-leave-to {
    opacity: 0;
    scale: 0.9;
    filter: blur(8px);
}
</style>