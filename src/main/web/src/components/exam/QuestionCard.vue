<script setup>
import QuestionView from "@/components/exam/QuestionView.vue";

const props = defineProps({
    question: {
        type: Object
    }
});

const model = defineModel({
    type: Object
});

const questionType = computed(() => {
    if (props.question) {
        switch (props.question.type) {
            case "MultipleChoicesQuestion":
                return "选择题";
            case "QuestionGroup":
                return "组合题";
        }
    }
    return null;
});

const choiceType = computed(() => {
    if (props.question) {
        switch (props.question.multipleChoiceType) {
            case "SINGLE_CORRECT":
                return "单选";
            case "MULTIPLE_CORRECT":
                return "多选";
        }
    }
    return null;
});

const subQuestionCount = computed(() => {
    if (props.question && props.question.type === "QuestionGroup") {
        return props.question.questionLinks.length;
    }
    return null;
});
</script>

<template>
<div style="display: flex;flex-direction: column">
    <div style="display: flex;flex-direction: row;align-items: center;margin-bottom: 4px;padding: 4px">
        <slot></slot>
        <el-tag type="info" size="large" style="margin-right: 0;border-radius: 4px 0 0 4px" v-if="questionType">{{ questionType }}</el-tag>
        <el-tag type="info" size="large" style="margin-right: 12px;border-radius: 0 4px 4px 0" v-if="choiceType">{{ choiceType }}</el-tag>
        <el-tag type="info" size="large" style="margin-right: 12px;border-radius: 0 4px 4px 0" v-else-if="subQuestionCount">{{ subQuestionCount }}</el-tag>
        <el-tag type="warning" size="large" v-if="question && question.refreshed">
            <div style="display: flex;flex-direction: row;align-items: center;">
                <el-text type="warning" style="margin-right: 8px;line-height: 16px;align-self: center">题目已更新</el-text>
                <el-button type="primary" style="height: 16px" link @click="question.confirmRefresh">确定</el-button>
            </div>
        </el-tag>
    </div>
    <div class="panel" style="flex: 1;height: 0;">
        <el-scrollbar view-style="display: contents;" v-loading="!question">
            <question-view style="height: 100%" v-if="question" :question="question" v-model="model"/>
        </el-scrollbar>
    </div>
</div>
</template>

<style scoped>

</style>