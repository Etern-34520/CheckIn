<script setup>
import {useRoute} from "vue-router";
import WebSocketConnector from "@/api/websocket.js";
import getAvatarUrlOf from "@/utils/Avatar.js";
import Collapse from "@/components/common/Collapse.vue";
import {MdEditor} from "md-editor-v3";
import UI_Meta from "@/utils/UI_Meta.js";
import PartitionCache from "@/data/PartitionCache.js";
import router from "@/router/index.js";
import QuestionCache from "@/data/QuestionCache.js";
import QuestionInfoPanel from "@/components/question/QuestionInfoPanel.vue";
import 'md-editor-v3/lib/style.css';
import LinkPanel from "@/components/common/LinkPanel.vue";
import {ElMessageBox} from "element-plus";
import PermissionInfo from "@/auth/PermissionInfo.js";

const route = useRoute();
const data = ref();
const loading = ref(true);
const showLoading = ref(true);
const showError = ref(false);

const partitionNames = ref();
const questionInfos = ref([]);


const displayLoading = () => {
    loading.value = true;
    setTimeout(() => {
        showLoading.value = loading.value;
    }, 200);
}

const reloadRelatedData = () => {
    const partitionIds = [];
    partitionIds.push(...data.value.requiredPartitionIds);
    partitionIds.push(...data.value.selectedPartitionIds);
    Promise.all([
        new Promise((resolve) =>
                PartitionCache.getNamesSyncByIds(partitionIds).then((names) => {
                    partitionNames.value = names;
                    resolve();
                })
        ),
        new Promise((resolve) =>
                QuestionCache.getAllAsync(data.value.questionIds).then((questionInfos1) => {
                    questionInfos.value = questionInfos1;
                    resolve();
                })
        )
    ]).then(() => {
        loading.value = false;
        showLoading.value = false;
    });
}

const update = () => {
    displayLoading();
    showError.value = false;
    WebSocketConnector.send({
        type: "getExamRecordDetail",
        data: {
            id: route.params.id
        }
    }).then((response) => {
        data.value = response.examData;
        reloadRelatedData();
    }, (error) => {
        error.disableNotification();
        showError.value = true;
        showLoading.value = false;
        loading.value = false;
    });
}
watch(() => route.params.id, update, {immediate: true});

const channel = WebSocketConnector.subscribe("examRecord", (data1) => {
    const examRecord = data1.examRecord;
    if (examRecord.id === data.value.id) {
        update();
    }
});

onBeforeUnmount(() => {
    channel.unsubscribe();
});

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

const invalidExam = () => {
    ElMessageBox.confirm("此操作不可撤销", "确定无效化", {
        showClose: false,
        draggable: true,
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
    }).then(() => {
        WebSocketConnector.send({
            type: "InvalidExam",
            examId: data.value.id,
        }).then(() => {
        });
    }).catch(() => {
    });
}

const banQQ = () => {
    //TODO
}

const slideWay = ref(router.currentRoute.value.name === "related-requests");
const remove = router.afterEach((to,from,failure) => {
    setTimeout(() => {
        slideWay.value = to.name === "related-requests";
    }, 400);
});
onBeforeUnmount(() => {
    remove();
})
</script>

<template>
    <div style="display: flex;flex-direction: column;flex:1;height: 0" v-loading="showLoading">
        <transition name="blur-scale" mode="out-in">
            <div v-if="showError" key="showError"
                 style="flex: 1;display: flex;align-items: center;justify-content: center;">
                <el-empty/>
            </div>
            <div style="padding: 32px 16px 4px;display: flex;flex-direction: column;align-items: stretch;flex:1;height: 0"
                 :key="data.id" v-else-if="data">
                <div style="display: flex;flex-direction: row;align-items: center;margin-left: 16px;flex-wrap: wrap">
                    <div style="display: flex;flex-direction: row;align-items: center;margin-bottom: 16px;">
                        <el-avatar style="width: 64px;height: 64px;margin-right: 16px"
                                   :src="getAvatarUrlOf(data.qqNumber)"/>
                        <div style="display: flex;flex-direction: column;min-width: min(70vw,120px);">
                            <el-text size="large"
                                     style="align-self: baseline;margin-bottom: 8px;">
                                {{ data.qqNumber }}
                            </el-text>
                            <el-tag style="align-self: baseline;margin-right: 8px">{{ data.status }}</el-tag>
                        </div>
                    </div>
                    <div style="display: flex;flex-direction: column;margin-right: 16px;margin-bottom: 16px;flex:1;">
                        <div style="display: flex;flex-direction: row;align-items: center;margin-bottom: 8px;">
                            <el-text style="margin-right: 12px;" type="info">生成时间</el-text>
                            <el-text>{{ data.generateTime }}</el-text>
                        </div>
                        <div style="display: flex;flex-direction: row;align-items: center;" v-if="data.submitTime">
                            <el-text style="margin-right: 12px;" type="info">提交时间</el-text>
                            <el-text>{{ data.submitTime }}</el-text>
                        </div>
                        <div style="display: flex;flex-direction: row;align-items: center;"
                             v-else-if="data.status === 'ONGOING'">
                            <el-text style="margin-right: 12px;" type="info">过期时间</el-text>
                            <el-text>{{ data.expireTime }}</el-text>
                        </div>
                        <div style="display: flex;flex-direction: row;align-items: center;" v-else>
                            <el-text style="margin-right: 12px;" type="info">已无效</el-text>
                        </div>
                    </div>
                    <div style="display: flex;flex-direction: row;align-items: stretch;margin-left: 16px;margin-bottom: 16px;flex-wrap: wrap"
                         v-if="data.status === 'SUBMITTED' && data.result">
                        <div style="margin-right: 80px;margin-bottom: 12px;display: flex;flex-direction: row;align-items: stretch">
                            <div style="width: 8px;max-width: 8px;min-width: 8px;border-radius: 4px;margin-right: 32px;"
                                 :style="{background: data.result.colorHex}"></div>
                            <el-text style="align-self: center" size="large">{{ data.result.level }}</el-text>
                        </div>
                        <el-statistic style="margin-right: 80px;margin-bottom: 12px;" :value="data.result.score">
                            <template #title>
                                <el-text type="primary" size="small">
                                    总分
                                </el-text>
                            </template>
                        </el-statistic>
                        <div style="display: flex;flex-direction: row;margin-bottom: 12px;">
                            <el-statistic style="margin-right: 32px;" :value="data.result.correctCount">
                                <template #title>
                                    <el-text type="success" size="small">
                                        答对题数
                                    </el-text>
                                </template>
                            </el-statistic>
                            <el-statistic style="margin-right: 32px;" :value="data.result.halfCorrectCount">
                                <template #title>
                                    <el-text type="warning" size="small">
                                        半对题数
                                    </el-text>
                                </template>
                            </el-statistic>
                            <el-statistic style="margin-right: 32px;" title="答错题数" :value="data.result.wrongCount">
                                <template #title>
                                    <el-text type="danger" size="small">
                                        答错题数
                                    </el-text>
                                </template>
                            </el-statistic>
                        </div>
                    </div>
                    <div v-else-if="data.status === 'ONGOING'" style="margin-right: 16px;">
                        <el-button-group>
                            <el-button @click="invalidExam">无效化该测试</el-button>
                            <!--                            <el-button @click="banQQ">将该用户 QQ 加入黑名单</el-button>-->
                        </el-button-group>
                    </div>
                </div>
                <div class="slide-switch-base" :class="slideWay?'left-to-right':''">
                    <router-view v-slot="{ Component }">
                        <transition :name="slideWay?'slide-left-to-right':'slide-right-to-left'">
                            <component v-if="Component" :is="Component"/>
                            <el-scrollbar v-else style="min-width: 100%;">
                                <div style="display: flex;flex-direction: column;width:calc(100% - 4px)">
                                    <collapse v-if="data.result">
                                        <template #title>
                                            <div style="height: 100%;margin-left: 8px;display: flex;flex-direction: row;align-items: center">
                                                <el-text>结果 Markdown</el-text>
                                            </div>
                                        </template>
                                        <template #content>
                                            <md-editor no-upload-img placeholder="结果" v-model="data.result.message"
                                                       class="preview-only"
                                                       preview-theme="vuepress"
                                                       :toolbars-exclude="['save','catalog','github']"
                                                       style="height: 100vh;max-width:calc(90vw - 100px);"
                                                       :theme="UI_Meta.colorScheme.value"
                                                       :show-toolbar-name="UI_Meta.mobile.value"
                                                       :preview="true"/>
                                        </template>
                                    </collapse>
                                    <div v-if="partitionNames && Object.keys(partitionNames).length > 0"
                                         style="display: flex;flex-direction: row;flex-wrap: wrap;margin: -4px">
                                        <div class="panel-1" style="padding: 12px 16px;flex: 1;margin: 16px 4px 4px;">
                                            <el-text style="align-self: baseline">必选分区</el-text>
                                            <div style="display: flex;flex-direction: row;flex-wrap: wrap;margin-top: 8px;">
                                                <el-button class="disable-init-animate"
                                                           style="font-size: 14px;margin: 2px"
                                                           @click="router.push({name:'questions'})"
                                                           v-for="requiredPartitionId of data.requiredPartitionIds">
                                                    {{ partitionNames[requiredPartitionId] }}
                                                </el-button>
                                            </div>
                                        </div>
                                        <div class="panel-1" style="padding: 12px 16px;flex: 1;margin: 16px 4px 4px;">
                                            <el-text style="align-self: baseline">用户选择分区</el-text>
                                            <div style="display: flex;flex-direction: row;flex-wrap: wrap;margin-top: 8px;">
                                                <el-button style="font-size: 14px;margin: 2px"
                                                           class="disable-init-animate"
                                                           @click="router.push({name:'questions'})"
                                                           v-for="selectedPartitionId of data.selectedPartitionIds">
                                                    {{ partitionNames[selectedPartitionId] }}
                                                </el-button>
                                            </div>
                                        </div>
                                    </div>
                                    <link-panel style="margin-top: 16px;" name="相关请求" description="生成 获取题目 提交"
                                                v-if="PermissionInfo.hasPermission('request record','get request records')"
                                                icon="Link" @click="router.push({name: 'related-requests'})"/>
                                    <div style="display:flex;flex-direction: column;margin: 32px -4px -4px;"
                                         v-if="questionInfos.length > 0">
                                        <div style="display:flex;flex-direction: row">
                                            <el-text size="large"
                                                     style="align-self: center;margin-left: 4px;margin-bottom: 8px;">
                                                测试数据
                                            </el-text>
                                            <el-text type="info"
                                                     style="align-self: center;margin-left: 4px;margin-bottom: 8px;">
                                                （题目可能已经更新）
                                            </el-text>
                                        </div>
                                        <div style="display: flex;flex-direction: row;flex-wrap: wrap;margin-bottom: 16px"
                                             v-for="(questionInfo) of questionInfos">
                                            <question-info-panel style="flex: 1;margin: 4px;min-width: 240px"
                                                                 @click="router.push({name:'question-detail',params: {id:questionInfo.question.id}})"
                                                                 class="clickable"
                                                                 :questionInfo="questionInfo"
                                                                 :sub-question-expanded="true"
                                                                 disable-error-and-warning/>
                                            <div class="panel-1" style="flex: 1;margin: 4px;padding: 4px"
                                                 v-if="data.answers">
                                                <div style="padding: 8px 12px 12px;">
                                                    <el-text>用户提交</el-text>
                                                    <div style="display: flex;flex-direction: column;align-items:baseline;margin-top: 8px"
                                                         v-if="questionInfo.question.type === 'QuestionGroup'">
                                                        <div v-for="(answerDatum,index) of getDisplayAnswerData(questionInfo,data.answers[questionInfo.question.id])">
                                                            <div style="display: flex;flex-direction: row;margin-bottom: 8px;margin-right: 32px;">
                                                                <el-tag type="info" size="large"
                                                                        style="align-self: center;border-radius: 4px 0 0 4px;">
                                                                    {{ index }}
                                                                </el-tag>
                                                                <el-tag style="margin-right: 12px;align-self: center;border-radius: 0 4px 4px 0;"
                                                                        size="large"
                                                                        :type="getTagType(data.answers[questionInfo.question.id].answers[index].result.checkedResultType)">
                                                                    {{
                                                                        data.answers[questionInfo.question.id].answers[index].result.score
                                                                    }}
                                                                    /
                                                                    {{
                                                                        data.answers[questionInfo.question.id].answers[index].result.maxScore
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
                                                    <div style="display: flex;flex-wrap: wrap;align-items:baseline;margin-top: 8px;"
                                                         v-else>
                                                        <el-tag v-for="(answerDatum,index) of getDisplayAnswerData(questionInfo,data.answers[questionInfo.question.id])"
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
                                                                :type="getTagType(data.answers[questionInfo.question.id].result.checkedResultType)">
                                                            {{ data.answers[questionInfo.question.id].result.score }}
                                                            /
                                                            {{ data.answers[questionInfo.question.id].result.maxScore }}
                                                        </el-tag>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
<!--                                        <div class="panel-1" style="padding: 16px">
                                            TODO action track
                                        </div>-->
                                    </div>
                                </div>
                            </el-scrollbar>
                        </transition>
                    </router-view>
                </div>
            </div>
        </transition>
    </div>
</template>

<style scoped>

</style>