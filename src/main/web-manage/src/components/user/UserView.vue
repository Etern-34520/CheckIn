<script setup>
import {useRoute} from "vue-router";
import UserDataInterface from "@/data/UserDataInterface.js";
import getAvatarUrlOf from "@/utils/Avatar.js";
import WebSocketConnector from "@/api/websocket.js";
import LinkPanel from "@/components/common/LinkPanel.vue";
import {Finished} from "@element-plus/icons-vue";
import QuestionInfoPanel from "@/components/question/QuestionInfoPanel.vue";
import QuestionCache from "@/data/QuestionCache.js";
import Waterfall from "@/components/common/Waterfall.vue";
import router from "@/router/index.js";

const route = useRoute();
const user = ref(undefined);
const loading = ref(true);
const questionLoading = ref(true);
const transitionCaller = ref(false);
const questions = ref([]);

const update = (newVal, oldVal) => {
    let questionLoaded = false;
    UserDataInterface.getUserOfQQ(route.params.id).then((resp) => {
        user.value = resp;
        loading.value = false;
        transitionCaller.value = !transitionCaller.value;
        if (!questionLoaded)
            questionLoading.value = true;
    });
    WebSocketConnector.send({
        type: "getQuestionsByUserQQ",
        qq: Number(route.params.id),
        limit: 6
    }).then((respData) => {
        questionLoaded = true;
        const questionInfos = [];
        for (const question of respData.questions) {
            questionInfos.push(QuestionCache.wrapToQuestionInfo(question));
        }
        questions.value = questionInfos;
        questionLoading.value = false;
    }, (err) => {
        questionLoading.value = false;
    });
}

watch(() => route.params.id, update);

onMounted(() => {
    update(route.params.id, null);
});

</script>

<template>
    <div v-loading="loading" style="height: 100%;padding: 16px 32px">
        <transition name="blur-scale" mode="out-in">
            <div :key="transitionCaller" v-if="user">
                <div style="display: flex;flex-direction: row;margin-bottom: 28px">
                    <el-avatar shape="circle" style="width: 84px;height: 84px;margin-right: 8px;"
                               :src="getAvatarUrlOf(user.qq)"/>
                    <div style="display:flex;flex-direction: column;justify-content: center;">
                        <el-text style="font-size: 24px;align-self: baseline">{{ user.name }}</el-text>
                        <el-text style="font-size: 16px;align-self: baseline" type="info">{{ user.qq }}</el-text>
                        <el-text style="font-size: 16px;align-self: baseline" type="info">{{ user.role }}</el-text>
                    </div>
                </div>
                <link-panel description="" name="TA的更多题目" :icon="Finished"/>
                <div class="panel-1" v-loading="questionLoading">
                    <waterfall :data="questions" :min-row-width="400" style="padding: 8px 12px">
                        <template #item="{item,index}">
                            <question-info-panel :question-info="item" @click="router.push('/manage/questions/'+item.question.id+'/')"/>
                        </template>
                    </waterfall>
                    <!--                    <question-info-panel v-for="questionInfo of questions" :question-info="QuestionCache.wrapToQuestionInfo(questionInfo)"/>-->
                </div>
                <link-panel description="" name="TA的答题记录" :icon="Finished"/>
            </div>
        </transition>
    </div>
</template>

<style scoped>

</style>