<script setup>
import {Splitpanes, Pane} from 'splitpanes'
import 'splitpanes/dist/splitpanes.css'
import UserDataInterface from "@/data/UserDataInterface.js";
import router from "@/router/index.js";
import WebSocketConnector from "@/api/websocket.js";
import ExamRecordItem from "@/pages/manage/serverGroup/examRecord/ExamRecordItem.vue";
import UI_Meta from "@/utils/UI_Meta.js";
import Waterfall from "@/components/common/Waterfall.vue";
import QuestionInfoPanel from "@/components/question/QuestionInfoPanel.vue";
import QuestionCache from "@/data/QuestionCache.js";

const user = UserDataInterface.getCurrentUser();
const animate = ref(false);
if (router.options.history.state.back === "/login/") {
    animate.value = true;
}

const today = new Date();
const loadingTodayExamRecords = ref(true);
const loadingTodayExamRecordsError = ref(false);
const todayExamRecords = ref();
const loadTodayExamRecords = () => {
    loadingTodayExamRecords.value = true;
    loadingTodayExamRecordsError.value = false;
    WebSocketConnector.send({
        type: "getExamRecords",
        data: {
            from: today.toISOString(),
            to: today.toISOString()
        }
    }).then((response) => {
        todayExamRecords.value = response.data.examRecords;
        loadingTodayExamRecords.value = false;
    }, (errorResp) => {
        loadingTodayExamRecords.value = false;
        loadingTodayExamRecordsError.value = true;
    });
}
loadTodayExamRecords();

const loadingRecentUpdatedQuestions = ref(true);
const loadingRecentUpdatedQuestionsError = ref(false);
const recentUpdatedQuestions = ref();
const loadRecentUpdatedQuestions = () => {
    loadingRecentUpdatedQuestions.value = true;
    loadingRecentUpdatedQuestionsError.value = false;
    WebSocketConnector.send({
        type: "getRecentUpdatedQuestionIds"
    }).then((response) => {
        QuestionCache.getAllAsync(response.data.questionIds).then((questionInfos) => {
            recentUpdatedQuestions.value = questionInfos;
            loadingRecentUpdatedQuestions.value = false;
        }, (e) => {
            console.error(e);
            loadingRecentUpdatedQuestions.value = false;
            loadingRecentUpdatedQuestionsError.value = true;
        })
    }, () => {
        loadingRecentUpdatedQuestions.value = false;
        loadingRecentUpdatedQuestionsError.value = true;
    })
}
loadRecentUpdatedQuestions();

const loadingUsersBestExam = ref(true);
const loadingUsersBestExamError = ref(false);
const usersBestExam = ref();
const loadUsersBestExam = () => {
    loadingUsersBestExam.value = true;
    loadingUsersBestExamError.value = false;
    WebSocketConnector.send({
        type: "getUsersLatestExamRecord"
    }).then((response) => {
        usersBestExam.value = response.data.examData;
        loadingUsersBestExam.value = false;
    }, (message) => {
        message.disableNotification();
        loadingUsersBestExam.value = false;
        loadingUsersBestExamError.value = true;
    })
}
loadUsersBestExam();

function formatDate(timer) {
    const year = timer.getFullYear()
    const month = timer.getMonth() + 1
    const day = timer.getDate()
    return `${pad(year, 4)}-${pad(month)}-${pad(day)}`
}

function pad(timeEl, total = 2, str = '0') {
    return timeEl.toString().padStart(total, str)
}

const putItem = (date, examRecord) => {
    if (!todayExamRecords.value[date]) {
        return;
    }
    if (todayExamRecords.value[date][examRecord.id]) {
        todayExamRecords.value[date][examRecord.id] = examRecord;
    } else {
        let target = {}
        target[examRecord.id] = examRecord;
        todayExamRecords.value[date] = Object.assign(target, todayExamRecords.value[date]);//place in front
    }
}
const channel = WebSocketConnector.subscribe("examRecords", (data1) => {
    const examRecord = data1.data;
    const generateDate = examRecord.generateTime.split(" ")[0];
    const submitDate = examRecord.submitTime ? examRecord.submitTime.split(" ")[0] : null;
    putItem(generateDate, examRecord);
    if (submitDate && submitDate !== generateDate) {
        putItem(submitDate, examRecord);
    }
});

onBeforeUnmount(() => {
    channel.unsubscribe();
});

const mobile = UI_Meta.mobile;

const openRecord = (id) => {
    router.push({name: 'exam-record-detail', params: {id: id}});
}
</script>

<template>
    <div style="display: flex;flex-direction: column;align-content: stretch;align-items: stretch">
        <div class="welcome" :class="{animate:animate}">
            <el-text style="font-size: 24px">
                欢迎回来，
            </el-text>
            <el-text style="font-size: 28px;color: var(--el-color-primary)">
                {{ user.name }}
            </el-text>
        </div>
        <el-scrollbar style="flex: 1" :view-style="mobile?'height: 200%':'height: 100%'">
            <splitpanes :horizontal="mobile">
                <pane min-size="20" size="30">
                    <div class="panel clickable" @click="openRecord(usersBestExam.id)"
                         style="padding: 0;flex: 0;min-height: 60px;display: flex;flex-direction: column;margin-bottom: 6px !important;"
                         v-if="!loadingUsersBestExam && usersBestExam">
                        <div v-if="loadingUsersBestExamError"
                             style="flex: 1;display: flex;justify-content: center;align-items: center;">
                            <el-text style="align-self: center;margin-right: 12px" type="info">加载个人答题记录时出错
                            </el-text>
                            <el-button link @click="loadUsersBestExam">重新加载</el-button>
                        </div>
                        <div style="padding: 16px;display: flex;flex-direction: row;align-items: stretch;flex: 1"
                             v-else-if="!loadingUsersBestExam">
                            <div style="margin: -4px 4px;width: 6px;border-radius: 3px"
                                 :style="{background: usersBestExam.result.colorHex}"></div>
                            <el-text size="large" style="margin-right: 8px;margin-left: 16px">
                                我的分数
                            </el-text>
                            <el-text size="large" type="primary">
                                {{ usersBestExam.result.score }}
                            </el-text>
                        </div>
                    </div>
                    <div style="padding: 8px 16px 0;flex:1" class="panel">
                        <el-text size="large" style="align-self: baseline;margin-top: 4px">今日答题记录</el-text>
                        <div v-loading="loadingTodayExamRecords"
                             style="display:flex;flex-direction:column;flex:1;height: 0;margin-top: 16px;">
                            <div style="display: flex;flex-direction: row;align-items:center;"
                                 v-if="loadingTodayExamRecordsError">
                                <el-text style="align-self: center;margin-right: 12px" type="info">加载数据时出错
                                </el-text>
                                <el-button link @click="loadTodayExamRecords">重新加载</el-button>
                            </div>
                            <el-scrollbar v-else-if="!loadingTodayExamRecords" style="flex:1">
                                <transition name="blur-scale">
                                    <div v-if="todayExamRecords && todayExamRecords[formatDate(today)]">
                                        <transition-group name="slide-hide">
                                            <div class="slide-hide-base"
                                                 v-for="(record,id,index) in todayExamRecords[formatDate(today)]"
                                                 :key="record.id">
                                                <div>
                                                    <exam-record-item style="min-height: 0"
                                                                      :record="record"
                                                                      class="clickable"
                                                                      @click="openRecord(record.id)"/>
                                                </div>
                                            </div>
                                        </transition-group>
                                    </div>
                                    <el-empty v-else-if="!loadingTodayExamRecords" description="无记录"/>
                                </transition>
                            </el-scrollbar>
                        </div>
                    </div>
                </pane>
                <pane min-size="40">
                    <div class="panel" style="display: flex;flex-direction: column;padding: 8px 16px 0">
                        <el-text size="large" style="align-self: baseline;margin-top: 4px">最近更新的题目</el-text>
                        <div v-loading="loadingRecentUpdatedQuestions"
                             style="display:flex;flex-direction:column;flex:1;height: 0;margin-top: 16px;">
                            <div style="display: flex;flex-direction: row;align-items:center"
                                 v-if="loadingRecentUpdatedQuestionsError">
                                <el-text style="align-self: center;margin-right: 12px" type="info">加载数据时出错
                                </el-text>
                                <el-button link @click="loadRecentUpdatedQuestions">重新加载</el-button>
                            </div>
                            <el-scrollbar v-else-if="recentUpdatedQuestions && recentUpdatedQuestions.length && !loadingRecentUpdatedQuestions"
                                          style="position: relative">
                                <waterfall :data="recentUpdatedQuestions" :min-row-width="400"
                                           style="max-width: calc(100% - 4px)">
                                    <template #item="{item,index}">
                                        <question-info-panel :question-info="item" class="clickable"
                                                             disable-error-and-warning
                                                             style="margin: 4px"
                                                             @click="router.push({name:'question-detail',params: {id:item.question.id}})"/>
                                    </template>
                                </waterfall>
                            </el-scrollbar>
                            <el-empty v-else-if="!loadingRecentUpdatedQuestions" description="无数据"/>
                        </div>
                    </div>
                </pane>
            </splitpanes>
        </el-scrollbar>
    </div>
</template>

<style scoped>
.welcome {
    display: flex;
    flex-direction: row;
    box-sizing: border-box;
    align-items: end;
    overflow: hidden;
    max-height: 0;
}

.welcome.animate {
    animation: welcomeAnimation 4s var(--ease-in-out-quint);
    animation-delay: 600ms;
    animation-fill-mode: backwards;
}

@keyframes welcomeAnimation {
    0% {
        opacity: 0;
        transform: translateY(16px);
        filter: blur(8px);
        max-height: 0;
    }
    20% {
        opacity: 1;
        transform: translateY(0);
        filter: blur(0);
        max-height: 46px;
    }
    60% {
        opacity: 1;
        transform: translateY(0);
        filter: blur(0);
        max-height: 46px;
    }
    100% {
        opacity: 0;
        transform: translateY(0);
        filter: blur(0);
        max-height: 0;
    }
}

.welcome > *:first-child {
    margin-left: 16px;
}

.welcome > * {
    margin-bottom: 8px;
}
</style>