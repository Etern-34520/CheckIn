<script setup>
import WebSocketConnector from "@/api/websocket.js";
import UserDataInterface from "@/data/UserDataInterface.js";
import QuestionInfoPanel from "@/components/question/QuestionInfoPanel.vue";
import QuestionCache from "@/data/QuestionCache.js";
import router from "@/router/index.js";
import Waterfall from "@/components/common/Waterfall.vue";
import PartitionCache from "@/data/PartitionCache.js";

const questionInfos = ref([]);
const loading = ref(true);

onBeforeMount(() => {
    try {
        PartitionCache.getRefPartitionsAsync().then(() => {
            WebSocketConnector.send({
                type: "getQuestionsByUserQQ",
                qq: UserDataInterface.getCurrentUser().qq
            }).then((response) => {
                loading.value = false;
                for (const question of response.questions) {
                    questionInfos.value.push(QuestionCache.wrapToQuestionInfo(question));
                }
            }, () => {
                loading.value = false;
            });
        }, () => {
            loading.value = false;
        });
    } catch (e) {
        loading.value = false;
    }
});
</script>

<template>
    <el-scrollbar v-loading="loading">
        <div style="display: flex;flex-direction: column;justify-items: stretch">
            <waterfall :data="questionInfos" :min-row-width="400">
                <template #item="{item,index}">
                    <question-info-panel :question-info="item" disable-error-and-warning
                                         @click="router.push('/manage/questions/'+item.question.id+'/')"/>
                </template>
            </waterfall>
            <!--        {{questions}}-->
        </div>
    </el-scrollbar>
</template>

<style scoped>

</style>