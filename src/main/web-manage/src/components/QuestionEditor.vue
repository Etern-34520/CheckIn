<script setup>
import {watch, reactive, onBeforeMount, ref, getCurrentInstance, defineEmits} from "vue";
import {useRoute} from "vue-router";
import QuestionTempStorage from "@/QuestionTempStorage.js";
import {ElNotification} from "element-plus";
import {Plus} from "@element-plus/icons-vue";

const route = useRoute();
const question = ref({value: {content: ""}});
const upload = ref();

let update = (newVal, oldVal) => {
    console.log(oldVal + " to " + newVal);
    QuestionTempStorage.getAsync(newVal).then((questionData) => {
        question.value = questionData;
    });
};
watch(() => route.params.id, update);
watch(() => question.value, (newVal, oldVal) => {
    QuestionTempStorage.update(newVal);
});

onBeforeMount(() => {
    update(route.params.id, null);
});

const filter = function (rawFile) {
    if (rawFile.raw.type.startsWith("image")) {
        for (const image of question.value.images) {
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

const removeImage = function (file, fileList) {
    console.log(file, fileList);
}

</script>

<template>
    <div style="display: flex;flex-direction: column">
        <el-input class="question-content-input" type="textarea" v-model="question.content"
                  autosize></el-input>
        <div class="question-image-upload panel" style="padding: 16px">
            <el-upload ref="upload"
                       v-model:file-list="question.images"
                       list-type="picture-card"
                       action="ignore"
                       :auto-upload="false"
                       :on-change="filter"
                       :on-remove="removeImage">
                <el-icon>
                    <Plus/>
                </el-icon>
            </el-upload>
        </div>
        <div style="display: flex;flex-direction: column;overflow: hidden">
            <TransitionGroup name="choice">
                <div v-for="choice of question.choices" :key="choice.content" class="choicePanel">
                    <el-checkbox v-model="choice.correct" size="large"></el-checkbox>
                    <el-input type="text" size="large" v-model="choice.content"></el-input>
                    <el-button text>-</el-button>
                </div>
            </TransitionGroup>
        </div>
    </div>
</template>

<style scoped>
.choicePanel {
    display: flex;
    flex-direction: row;
    height: 40px;
    margin-bottom: 2px;
    overflow: hidden;
}

.choice-move, .choice-enter-active, .choice-leave-active {
    transition: 0.4s;
    overflow: hidden;
}

.choice-enter-from, .choice-leave-to {
    opacity: 0;
    height: 0;
    margin-bottom: 0;
}

.question-image-upload > div {
    height: 148px !important;
}
</style>