<script setup>
import WebSocketConnector from "@/api/websocket.js";
import UserDataInterface from "@/data/UserDataInterface.js";
import QuestionInfoPanel from "@/components/question/QuestionInfoPanel.vue";
import QuestionCache from "@/data/QuestionCache.js";
import router from "@/router/index.js";
import Waterfall from "@/components/common/Waterfall.vue";

const questionInfos = ref([]);
const loading = ref(true);

onBeforeMount(() => {
    try {
        WebSocketConnector.send({
            type: "getLikeQuestionsByUserQQ",
            data: {
                qq: UserDataInterface.getCurrentUser().value.qq
            }
        }).then((response) => {
            loading.value = false;
            for (const question of response.data.questions) {
                questionInfos.value.push(QuestionCache.wrapToQuestionInfo(question));
            }
        }, (error) => {
            loading.value = false;
        });
    } catch (e) {
        loading.value = false;
    }
});
</script>

<template>
    <div style="display: flex;flex-direction: column;justify-items: stretch">
        <el-scrollbar v-loading="loading">
            <waterfall :data="questionInfos" :min-row-width="400">
                <template #item="{item,index}">
                    <question-info-panel :question-info="item" class="clickable" disable-error-and-warning
                                         @click="router.push({name:'question-detail',params: {id:item.question.id}})"/>
                </template>
            </waterfall>
        </el-scrollbar>
        <!--        {{questions}}-->
    </div>
</template>

<style scoped>

</style>