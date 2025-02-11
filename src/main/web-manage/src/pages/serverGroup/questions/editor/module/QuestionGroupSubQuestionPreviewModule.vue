<script setup>
import QuestionPreview from "@/components/question/QuestionPreview.vue";

defineProps({
    questionInfo: {
        type: Object,
        required: true
    }
});
</script>

<template>
    <div class="panel-1" style="margin: 16px;padding-top: 32px" v-for="subQuestionInfo of questionInfo.questionInfos">
        <question-preview :question-info="subQuestionInfo" :force-mobile="true"/>
    </div>
</template>

<style scoped>

</style>