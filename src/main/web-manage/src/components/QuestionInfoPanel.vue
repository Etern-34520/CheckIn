<template>
    <div class="panel-1 error-question-info-panel" :class="{clickable:clickable}">
        <div class="grid1">
            <div class="padding">
                <div class="question-content panel-1 flex-blank-1" style="padding: 4px 16px;">
                    <el-text>
                        {{ questionInfo.question.content }}
                    </el-text>
                </div>
                <div class="choicesList"
                     v-if="questionInfo.question.choices!==undefined&&questionInfo.question.choices!==null">
                    <el-tag v-for="choice of questionInfo.question.choices"
                            :type="choice.correct?'success':'danger'">
                        {{ choice.content }}
                    </el-tag>
                </div>
                <div v-if="questionInfo.type==='QuestionGroup'">
                    <collapse @click.stop :content-background="false">
                        <template #title>
                            <el-text style="line-height: 32px;">题组内的题目</el-text>
                        </template>
                        <template #content>
                            <transition-group name="slide-hide">
                                <QuestionInfoPanel v-for="subQuestionInfo of questionInfo.questionInfos"
                                                   :key="subQuestionInfo.question.id"
                                                   :question-info="subQuestionInfo" :clickable="false"/>
                            </transition-group>
                        </template>
                    </collapse>
                </div>
                <div>
                    <el-tag type="info" style="margin-right: 16px">
                        {{ questionInfo.question.type ? questionInfo.question.type : questionInfo.type }}
                    </el-tag>
                    <template
                        v-if="questionInfo.question.partitionIds!==undefined&&questionInfo.question.partitionIds!==null">
                        <el-tag
                            v-for="partitionId of questionInfo.question.partitionIds"
                            type="info">
                            {{ PartitionCache.getName(partitionId) }}
                        </el-tag>
                    </template>
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
            </div>
        </div>
    </div>
</template>
<script setup>
import PartitionCache from "../data/PartitionCache.js";
import Collapse from "@/components/Collapse.vue";

defineProps({
    questionInfo: Object,
    clickable: {
        type: Boolean,
        default: true,
    },
    disableErrorAndWarning: {
        type: Boolean,
        default: false,
    }
})
</script>
<style scoped>

.dragHover * {
    color: var(--front-color-dark) !important;
}

.error-question-info-panel {
    display: grid;
    margin-top: 2px;
    padding: 0;
    min-height: 0;
    grid-template-rows: 1fr;
}

.padding {
    padding: 8px;
}

.error-question-info-panel > .grid1 > .padding > div {
    min-height: 25px;
}

.error-question-info-panel > .grid1 {
    min-height: 0;
}

.errorsDescription > * {
    margin-right: 8px;
}

.choicesList > * {
    /*min-width: 40px;*/
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