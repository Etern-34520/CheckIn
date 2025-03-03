<script setup>
import router from "@/router/index.js";
import {ArrowLeftBold} from "@element-plus/icons-vue";
import QuestionInfoPanel from "@/components/question/QuestionInfoPanel.vue";
import ExamRecordItem from "@/pages/serverGroup/examRecord/ExamRecordItem.vue";
import WebSocketConnector from "@/api/websocket.js";
import QuestionCache from "@/data/QuestionCache.js";

const back = () => {
    router.push({name: "question-detail"});
}

const loading = ref(true);
const error = ref(false);

const data = ref({});
const currentQuestionInfo = ref();

const loadData = () => {
    QuestionCache.getAsync(router.currentRoute.value.params.id).then((questionInfo) => {
        currentQuestionInfo.value = questionInfo;
        WebSocketConnector.send({
            type: "get related exam records and answers",
            questionId: currentQuestionInfo.value.question.id
        }).then((response) => {
            data.value = response;
            loading.value = false;
        }, () => {
            loading.value = false;
            error.value = true;
        });
    });
}

loadData();

const getDisplayAnswerData = (questionInfo, answer) => {
    const answerChoicesMap = {};
    if (answer && answer.selectedChoices) {
        for (const answerElement of answer.selectedChoices) {
            answerChoicesMap[answerElement.id] = answerElement;
        }
        const correctChoicesMap = {};
        for (const choice of questionInfo.question.choices) {
            if (choice.correct) {
                correctChoicesMap[choice.id] = choice;
            }
        }
        const choiceDisplayData = [];
        for (const choice of questionInfo.question.choices) {
            const choiceData = {
                id: choice.id,
                content: choice.content,
                selected: Boolean(answerChoicesMap[choice.id]),
                matched: Boolean((answerChoicesMap[choice.id] && correctChoicesMap[choice.id]) || (!answerChoicesMap[choice.id] && !correctChoicesMap[choice.id])),
            };
            choiceDisplayData.push(choiceData);
        }
        return choiceDisplayData;
    } else if (answer && answer.answers) {
        const answers = [];
        for (const index in answer.answers) {
            const subQuestionInfo = questionInfo.questionInfos[index];
            answers.push(getDisplayAnswerData(subQuestionInfo, answer.answers[index]));
        }
        return answers;
    }
}

const getTagType = (checkedResultType) => {
    if (checkedResultType === "CORRECT") return "success";
    else if (checkedResultType === "HALF_CORRECT") return "warning";
    else if (checkedResultType === "WRONG") return "danger";
}
</script>

<template>
    <div v-loading="loading" style="height: 100%;display: flex;flex-direction: column">
        <div style="display: flex;flex-direction: column;padding: 16px;flex: 1">
            <div style="display: flex;flex-direction: row">
                <el-button @click="back" style="margin-right: 16px">
                    <el-icon>
                        <ArrowLeftBold/>
                    </el-icon>
                    返回
                </el-button>
                <el-text size="large">使用到该题的测试</el-text>
            </div>
            <question-info-panel v-if="currentQuestionInfo" :question-info="currentQuestionInfo" sub-question-expanded disable-error-and-warning/>
            <el-scrollbar style="flex:1">
                <div style="display:flex;flex-direction: column;padding: 16px;" v-if="error">
                    <el-text type="danger">获取失败</el-text>
                    <el-button link type="primary" style="margin-top: 8px" @click="loadData">重试</el-button>
                </div>
                <div v-else-if="!loading" style="width: calc(100% - 4px);">
                    <div style="display:flex;flex-direction: column;margin: 32px -4px -4px;"
                         v-if="data.examDataAnswerList.length > 0">
                        <div style="display:flex;flex-direction: row">
                            <el-text size="large"
                                     style="align-self: center;margin-left: 4px;margin-bottom: 8px;">
                                测试数据
                            </el-text>
                        </div>
                        <div style="display: flex;flex-direction: row;flex-wrap: wrap;margin-bottom: 16px"
                             v-for="item of data.examDataAnswerList">
                            <exam-record-item style="flex: 1;margin: 4px;min-width: 240px"
                                              class="clickable" :record="item.examData"
                                              @click="router.push({name:'exam-record-detail',params: {id:item.examData.id}})"/>
                            <div class="panel-1" style="flex: 1;margin: 4px;padding: 4px"
                                 v-if="true">
                                <div style="padding: 4px">
                                    <div style="display: flex;flex-direction: column;align-items:baseline;"
                                         v-if="currentQuestionInfo.question.type === 'QuestionGroup'">
                                        <div v-for="(answerDatum,index) of getDisplayAnswerData(currentQuestionInfo,item.answer)">
                                            <div style="display: flex;flex-direction: row;margin-bottom: 8px;margin-right: 32px;">
                                                <el-tag type="info" size="large"
                                                        style="align-self: center;border-radius: 4px 0 0 4px;">
                                                    {{ index }}
                                                </el-tag>
                                                <el-tag style="margin-right: 12px;align-self: center;border-radius: 0 4px 4px 0;"
                                                        size="large"
                                                        :type="getTagType(item.answer.answers[index].result.checkedResultType)">
                                                    {{
                                                        item.answer.answers[index].result.score
                                                    }}
                                                    /
                                                    {{
                                                        item.answer.answers[index].result.maxScore
                                                    }}
                                                </el-tag>
                                                <div class="panel-1"
                                                     style="padding: 4px;display: flex;flex-direction: row;flex: 1">
                                                    <el-tag style="margin: 2px;"
                                                            v-for="(subAnswerDatum,index) of answerDatum"
                                                            :type="subAnswerDatum.selected?'primary':'info'">
                                                        {{ subAnswerDatum.content }}
                                                    </el-tag>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div style="display: flex;flex-wrap: wrap;align-items:baseline;"
                                         v-else>
                                        <el-tag v-for="(answerDatum,index) of getDisplayAnswerData(currentQuestionInfo,item.answer)"
                                                style="margin: 2px"
                                                :type="answerDatum.selected?'primary':'info'">
                                            {{ answerDatum.content }}
                                        </el-tag>
                                    </div>
                                    <div style="margin-top: 8px;display: flex;flex-direction: row">
                                        <el-tag type="info" size="large"
                                                style="border-radius: 4px 0 0 4px;">
                                            总分
                                        </el-tag>
                                        <el-tag size="large" style="border-radius: 0 4px 4px 0;"
                                                :type="getTagType(item.answer.result.checkedResultType)">
                                            {{ item.answer.result.score }}
                                            /
                                            {{ item.answer.result.maxScore }}
                                        </el-tag>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </el-scrollbar>
        </div>
    </div>
</template>

<style scoped>

</style>