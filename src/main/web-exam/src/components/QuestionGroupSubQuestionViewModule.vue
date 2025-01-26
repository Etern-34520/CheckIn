<script setup>
import QuestionView from "@/components/QuestionView.vue";
import QuestionCard from "@/components/QuestionCard.vue";

const props = defineProps({
    question: {
        type: Object,
        required: true
    }
});

const model = defineModel({
    type: Object
});
if (!model.value.questions) {
    model.value.questions = {};
}
for (let i = 0; i < props.question.questionLinks.length; i++) {
    if (!model.value.questions[i]) {
        model.value.questions[i] = {};
    }
}
model.value.type = "QuestionGroup"
</script>

<template>
    <div style="margin: 16px;padding-top: 32px">
        <question-card v-for="(subQuestionLink,index) of question.questionLinks" style="margin-bottom: 32px" :question="subQuestionLink.source" v-model="model.questions[index]" :force-mobile="true">
            <el-tag type="info" size="large" style="margin-right: 12px;">{{ index + 1 }}</el-tag>
        </question-card>
    </div>
</template>

<style scoped>

</style>