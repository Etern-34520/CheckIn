<script setup>
import router from "@/router/index.js";
import QuestionCard from "@/components/QuestionCard.vue";
import 'md-editor-v3/lib/style.css';
import UI_Meta from "@/UI_Meta.js";
import LockUtil from "@/Lock.js";
import _Loading_ from "@/components/_Loading_.vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {ArrowDown, ArrowUp} from "@element-plus/icons-vue";

const {proxy} = getCurrentInstance();

const examInfo = ref();
examInfo.value = proxy.$cookies.get("examInfo");

const lazyLoadQuestions = ref([]);

const answerData = ref(proxy.$cookies.get("submissions"));
if (!answerData.value) answerData.value = {};

const routeToResult = () => {
    proxy.$cookies.set("phase", "result", "7d");
    router.push({name: "result"});
}
onBeforeMount(() => {
    const result = proxy.$cookies.get("result");
    if (result) {
        routeToResult();
    }
})

// onMounted(() => {
watch(() => answerData.value, () => {
    proxy.$cookies.set("submissions", answerData.value, "7d");
    checkComplete(answerData.value);
}, {deep: true});
// });

const timestamps = ref({});
timestamps.value = proxy.$cookies.get("timestamps");
if (!timestamps.value) timestamps.value = {};

const loadedIndexes = new Set();
const refreshedQuestion = ref({});
let alerting = false;
const handleError = (data, actionDescription) => {
    if (data.message) {
        switch (data.message) {
            case "Exam invalided":
                if (!alerting)
                    ElMessageBox.alert(
                            "试题已无效", actionDescription,
                            {
                                type: "error",
                                draggable: true,
                                showClose: false,
                                confirmButtonText: "重新生成试题",
                            }
                    ).then(() => {
                        alerting = false;
                        proxy.$cookies.set("phase", "generate", "7d");
                        proxy.$cookies.remove("submissions");
                        proxy.$cookies.remove("timestamps");
                        proxy.$cookies.remove("examInfo");
                        router.push({name: "generate"});
                    });
                alerting = true;
                console.error("EXAM INVALIDED");
                break;
            case "Exam has already submitted":
                if (!alerting)
                    ElMessageBox.alert(
                            "试题已被提交", actionDescription,
                            {
                                type: "info",
                                draggable: true,
                                showClose: false,
                                confirmButtonText: "查看结果",
                            }
                    ).then(() => {
                        alerting = false;
                        proxy.$http.post("getResult", {
                            examId: examInfo.value.examId,
                        }).then((response) => {
                            if (response.type !== "error") {
                                proxy.$cookies.set("result", response, "7d");
                                routeToResult();
                                console.log(response);
                            } else {
                                handleError(response, "获取结果时出错");
                                submitting.value = false;
                            }
                        }, (error) => {
                            handleError(error, "获取结果时出错");
                            submitting.value = false;
                        })
                    });
                alerting = true;
                console.error("EXAM INVALIDED");
                break;
            case "Exam is not exist":
                if (!alerting)
                    ElMessageBox.alert(
                            "试题不存在", actionDescription,
                            {
                                type: "error",
                                draggable: true,
                                showClose: false,
                                confirmButtonText: "重新生成试题",
                            }
                    ).then(() => {
                        alerting = false;
                        proxy.$cookies.set("phase", "generate", "7d");
                        proxy.$cookies.remove("submissions");
                        proxy.$cookies.remove("timestamps");
                        proxy.$cookies.remove("examInfo");
                        router.push({name: "generate"});
                    })
                alerting = true;
                console.error("EXAM IS NOT EXIST");
                break;
            default:
                let message = data.message;
                if (data.response && data.response.data && data.response.data.message) {
                    message = data.response.data.message;
                }
                ElMessage({
                    type: "error",
                    message: "上传出错: " + message
                })
                console.error(message);
        }
    } else {
        console.error(actionDescription, data);
    }
}
const loadQuestionsByIndexes = (indexes, force = false) => {
    let resolveFunc;
    let rejectFunc;
    const promise = new Promise((resolve, reject) => {
        resolveFunc = resolve;
        rejectFunc = reject;
    });
    promise.resolve = resolveFunc;
    promise.reject = rejectFunc;
    promise.execute = (resolve, reject) => {
        if (!force) {
            indexes = indexes.filter(index => !loadedIndexes.has(index));
            if (indexes.length === 0) {
                resolve();
                return;
            }
        }
        proxy.$http.post("examQuestions", {
            examId: examInfo.value.examId,
            indexes: indexes,
        }).then((data) => {
            if (data.type !== "error") {
                for (const [index, question] of Object.entries(data.questions)) {
                    loadedIndexes.add(Number(index));
                    lazyLoadQuestions.value[Number(index)] = question;
                    if (!Boolean(answerData.value[index])) {
                        answerData.value[index] = {}
                    }
                    if (!timestamps.value[index]) {
                        timestamps.value[index] = question.lastModifiedTime[question.lastModifiedTime.length - 1];
                    } else if (timestamps.value[index] !== question.lastModifiedTime[question.lastModifiedTime.length - 1]) {
                        const currentQuestion = lazyLoadQuestions.value[Number(index)];
                        currentQuestion.refreshed = true;
                        refreshedQuestion.value[index.toString()] = currentQuestion;
                        currentQuestion.confirmRefresh = () => {
                            timestamps.value[index] = question.lastModifiedTime[question.lastModifiedTime.length - 1];
                            delete refreshedQuestion.value[index.toString()];
                            delete currentQuestion.refreshed;
                            delete currentQuestion.confirmRefresh;
                            proxy.$cookies.set("timestamps", timestamps.value, "7d");
                        }
                    }
                    proxy.$cookies.set("timestamps", timestamps.value, "7d");
                }
            } else {
                handleError(data, "获取题目时出错");
            }
            resolve();
        }, (error) => {
            handleError(error, "获取题目时出错");
            reject();
        })
    }
    LockUtil.synchronizeExecute("loadQuestions", promise);
    return promise;
}

if (Boolean(examInfo.value)) {
    proxy.$cookies.set("phase", "examine", "7d");
} else {
    proxy.$cookies.set("phase", "generate", "7d");
    router.push({name: "generate"});
}

const scroll = ref(null);
const disableSnip = ref(false);
let clickedCount = 0;

const disableSnipTemporarily = () => {
    clickedCount++;
    disableSnip.value = true;
    const restoreLater = () => {
        setTimeout(() => {
            if (clickedCount <= 0) {
                disableSnip.value = false;
                clickedCount = 0;
            } else {
                clickedCount--;
                restoreLater();
            }
        }, 400);
    }
    restoreLater();
}

const offset = ref(0);
const recalculateOffset = () => {
    if (UI_Meta.mobile.value) {
        offset.value = window.innerHeight * 0.05;
    } else {
        offset.value = window.innerHeight * 0.125;
    }
};
watch(() => UI_Meta.mobile.value, recalculateOffset, {immediate: true});

window.addEventListener("resize", recalculateOffset);

const textTypes = ref([]);

watchEffect(() => {
    if (!examInfo.value) return;
    for (let i = 0; i < examInfo.value.questionItemCount; i++) {
        const question = lazyLoadQuestions.value[i];
        if (question && question.refreshed) {
            textTypes.value[i] = 'warning';
        } else if (question) {
            const answerItem = answerData.value[String(i)];
            if (question.type === "MultipleChoicesQuestion" && answerItem.selected && Object.keys(answerItem.selected).length > 0) {
                textTypes.value[i] = 'primary';
            } else if (question.type === "QuestionGroup" && question.questionLinks && Object.keys(answerItem).length > 0) {
                textTypes.value[i] = 'info';
                for (let j = 0; j < question.questionLinks.length; j++) {
                    const subQuestion = answerItem.questions[j];
                    if (subQuestion && subQuestion.selected && Object.keys(subQuestion.selected).length > 0) {
                        textTypes.value[i] = 'primary';
                        break;
                    }
                }
            } else {
                textTypes.value[i] = 'info';
            }
        } else {
            textTypes.value[i] = 'info';
        }
    }
})

const lazyLoadScope = 6;
const lazyLoadOffset = -1;

const currentIndex = ref(0);

const loadContext = (questionIndex) => {
    if (examInfo.value) {
        const lazyLoadIndexes = [];
        for (let i = questionIndex + lazyLoadOffset; i < questionIndex + lazyLoadOffset + lazyLoadScope; i++) {
            if (i < 0) {
                continue
            }
            if (i >= examInfo.value.questionItemCount) {
                break;
            }
            if (!lazyLoadQuestions.value[i]) {
                lazyLoadIndexes.push(i);
            }
        }
        // console.log(lazyLoadIndexes);
        if (lazyLoadIndexes.length > 0) {
            loadQuestionsByIndexes(lazyLoadIndexes);
        }
    }
}

const onChange = (elementId) => {
    let questionIndex = Number(elementId.split("#question-")[1]);
    if (Number.isNaN(questionIndex)) {
        questionIndex = examInfo.value.questionItemCount;
        currentIndex.value = questionIndex;
    } else {
        currentIndex.value = questionIndex;
        loadContext(questionIndex);
    }
}
loadContext(0);

const uncompletedQuestionIndexData = ref([]);
const checkComplete = (answerData) => {
    let trace = [];
    let uncompletedQuestionIndexData1 = [];
    let mainIndexes = {};
    const handleObj = (obj) => {
        for (const [key, field] of Object.entries(obj)) {
            trace.push(key);
            const type = typeof field;
            switch (type) {
                case "undefined":
                case "bigint":
                case "boolean":
                case "number":
                case "function":
                case "string":
                case "symbol":
                    trace.splice(trace.length - 1, 1);
                    return;
                case "object":
                    if (field instanceof String) {
                        trace.splice(trace.length - 1, 1);
                        return;
                    } else {
                        if (Boolean(field.selected) && Object.keys(field.selected).length === 0) {
                            const indexDatum = {};
                            const mainIndex = Number(trace[0]);
                            indexDatum.mainIndex = mainIndex;
                            mainIndexes[mainIndex.toString()] = true;
                            if (trace[1] !== undefined) {
                                indexDatum.subIndex = Number(trace[1]);
                            }
                            uncompletedQuestionIndexData1.push(indexDatum);
                        } else if (Boolean(field.questions)) {
                            handleObj(field.questions);
                        }
                    }
                    break;
            }
            trace.splice(trace.length - 1, 1);
        }
    }
    handleObj(answerData);
    uncompletedQuestionIndexData.value = uncompletedQuestionIndexData1;
}
checkComplete(answerData.value);

const anchor = ref();

const jumpToQuestion = (index) => {
    disableSnipTemporarily();
    anchor.value.scrollTo('#question-' + index);
}

const forward = () => {
    const newIndex = currentIndex.value - 1;
    if (newIndex >= 0) {
        jumpToQuestion(newIndex);
    } else {
        jumpToQuestion(0);
    }
}

const afterward = () => {
    const newIndex = currentIndex.value + 1;
    if (newIndex < examInfo.value.questionItemCount) {
        jumpToQuestion(newIndex);
    } else {
        disableSnipTemporarily();
        anchor.value.scrollTo('#submit');
    }
}

const submitting = ref(false);
const submitExam = () => {
    submitting.value = true;
    const indexes = Array.from(new Array(examInfo.value.questionItemCount).keys());
    const sendSubmit = () => {
        const handleMultipleChoiceQuestion = (answerItem, question) => {
            const answerData = [];
            for (const [indexString, selected] of Object.entries(answerItem.selected)) {
                if (selected) {
                    const choice = question.choices[indexString];
                    if (choice) {
                        answerData.push(choice.id);
                    }
                }
            }
            return answerData;
        }

        const mappedAnswerData = {};
        const handleAnswerData = (questions, mappedAnswerData1, answerData) => {
            for (const [indexString, answerItem] of Object.entries(answerData)) {
                const questionOrLink = questions[indexString];
                const question = questionOrLink.source ? questionOrLink.source : questionOrLink;
                mappedAnswerData1[indexString] = {};
                if (answerItem.type === "MultipleChoice") {
                    mappedAnswerData1[indexString] = handleMultipleChoiceQuestion(answerItem, question);
                } else if (answerItem.type === "QuestionGroup") {
                    mappedAnswerData1[indexString] = {};
                    handleAnswerData(question.questionLinks, mappedAnswerData1[indexString], answerItem.questions);
                }
            }
        }
        handleAnswerData(lazyLoadQuestions.value, mappedAnswerData, answerData.value);
        // console.log(mappedAnswerData);
        proxy.$http.post("submit", {
            examId: examInfo.value.examId,
            answer: mappedAnswerData,
        }).then((response) => {
            if (response.type !== "error") {
                proxy.$cookies.set("result", response, "7d");
                routeToResult();
                console.log(response);
            } else {
                handleError(response, "提交时出错");
                submitting.value = false;
            }
        }, (error) => {
            handleError(error, "提交时出错");
            submitting.value = false;
        });
    }
    loadQuestionsByIndexes(indexes, true).then(() => {
        if (Object.keys(refreshedQuestion.value).length > 0) {
            ElMessageBox.confirm(
                    "部分题目已更新，正确答案可能改变",
                    "题目更新",
                    {
                        type: "warning",
                        draggable: true,
                        showClose: false,
                        confirmButtonText: "返回修改",
                        cancelButtonText: "仍然提交"
                    },
            ).then(() => {
                submitting.value = false;
            }).catch(() => {
                sendSubmit();
            });
        } else {
            sendSubmit();
        }
    })
}

const confirmAllUpdate = () => {
    for (const [key, value] of Object.entries(refreshedQuestion.value)) {
        value.confirmRefresh();
    }
}
</script>

<template>
    <div style="display: flex;flex-direction: column;max-height: 100dvh;min-height: 100dvh"
         :class="{mobile:UI_Meta.mobile.value}"
         v-if="examInfo">
        <div style="display: flex;flex-direction: row;width: 100dvw;justify-content: center;height: 0;flex: 1">
            <div class="question-scroll" ref="scroll" :class="{'disable-snip':disableSnip}">
                <div style="min-height: 20dvh;"></div>
                <QuestionCard v-for="order in examInfo.questionItemCount" style="scroll-snap-align: center;"
                              v-model="answerData[String(order-1)]" :id="'question-'+String(order-1)"
                              :question="lazyLoadQuestions[order-1]" class="question-card">
                    <el-tag type="info" size="large" style="margin-right: 12px;">{{ order }}</el-tag>
                </QuestionCard>
                <div id="submit" class="question-card"
                     style="display: flex;flex-direction: column;padding: 64px;scroll-snap-align: center;box-sizing: border-box">
                    <transition-group name="smooth-height">
                        <div v-if="uncompletedQuestionIndexData.length > 0" class="smooth-height-base">
                            <div>
                                <el-text size="large" type="warning">以下题目未作答</el-text>
                                <div style="display: flex;flex-direction: row;flex-wrap: wrap;margin-top: 8px;margin-bottom: 32px;">
                                    <el-button v-for="datum of uncompletedQuestionIndexData" style="margin: 4px"
                                               @click="jumpToQuestion(datum.mainIndex)">
                                        {{ datum.mainIndex + 1 }}
                                        {{ datum.subIndex !== undefined ? " - " + (datum.subIndex + 1) : null }}
                                    </el-button>
                                </div>
                            </div>
                        </div>
                        <div v-if="Object.keys(refreshedQuestion).length > 0" class="smooth-height-base">
                            <div>
                                <div style="display: flex;flex-direction: row;flex-wrap: wrap">
                                    <el-text size="large" type="warning">以下题目已更新，正确答案可能改变</el-text>
                                    <el-button type="primary" style="margin-left: 16px" link @click="confirmAllUpdate">
                                        确认全部
                                    </el-button>
                                </div>
                                <div style="display: flex;flex-direction: row;flex-wrap: wrap;margin-top: 8px;">
                                    <el-button v-for="index of Object.keys(refreshedQuestion)" style="margin: 4px"
                                               @click="jumpToQuestion(Number(index))">
                                        {{ (Number(index) + 1) }}
                                    </el-button>
                                </div>
                            </div>
                        </div>
                        <div v-if="uncompletedQuestionIndexData.length === 0 && Object.keys(refreshedQuestion).length === 0"
                             class="smooth-height-base">
                            <div>
                                <el-text type="primary" size="large">所有题目已作答完毕</el-text>
                                <br/>
                                <el-text type="info">提交后将无法修改</el-text>
                            </div>
                            <!--                            <el-text>{{ answerData }}</el-text>-->
                        </div>
                    </transition-group>
                    <div class="flex-blank-1"></div>
                    <el-button type="primary" size="large" @click="submitExam" :loading="submitting"
                               :loading-icon="_Loading_">提交
                    </el-button>
                </div>
                <div style="min-height: 20dvh;"></div>
            </div>
            <div class="question-anchor">
                <div class="question-anchor-scroll">
                    <el-anchor
                            ref="anchor"
                            :container="scroll"
                            direction="vertical"
                            :bound="200"
                            :offset="offset"
                            :duration="300"
                            @click.prevent="disableSnipTemporarily"
                            @change="onChange"
                            style="margin-top: 32px;margin-bottom: 32px;"
                            type="default">
                        <el-anchor-link style="height: 32px;margin-bottom: 4px"
                                        v-for="order in examInfo.questionItemCount"
                                        :href="'#question-'+String(order - 1)">
                            <el-text :type="textTypes[order - 1]" size="large">
                                {{ order }}
                            </el-text>
                        </el-anchor-link>
                        <el-anchor-link href="#submit" title="提交"/>
                    </el-anchor>
                </div>
                <div style="display: flex;flex-direction: column;margin-bottom: 16px;">
                    <el-button link style="height: 40px;margin: 0;" @click="forward"
                               :disabled="currentIndex <= 0">
                        <el-icon :size="20">
                            <ArrowUp/>
                        </el-icon>
                    </el-button>
                    <el-button link style="height: 40px;margin: 0;" @click="afterward"
                               :disabled="currentIndex >= examInfo.questionItemCount">
                        <el-icon :size="20">
                            <ArrowDown/>
                        </el-icon>
                    </el-button>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.question-scroll {
    display: flex;
    flex-direction: column;
    overflow-y: auto;
    overflow-x: visible;
    width: 0;
    flex: 1;
    scrollbar-color: rgba(0, 0, 0, 0) rgba(0, 0, 0, 0);
    scrollbar-width: thin;
    scrollbar-gutter: stable both-edges;
}

.question-scroll:not(.disable-snip) {
    scroll-snap-type: y mandatory;
    scroll-behavior: smooth;
}

.question-anchor {
    display: flex;
    flex-direction: column;
    align-self: center;
    overflow: hidden;
    max-height: 100dvh;
    width: 54px;
    padding: 0 !important;
    margin-right: 8px;
}

.question-anchor-scroll {
    height: 0;
    flex: 1;
    overflow: auto;
    scrollbar-color: rgba(0, 0, 0, 0) rgba(0, 0, 0, 0);
    padding: 0 !important;
}

.question-anchor-scroll::before {
    display: block;
    position: absolute;
    content: '';
    background: linear-gradient(180deg, var(--html-bg), rgba(255, 255, 255, 0));
    pointer-events: none;
    height: 64px;
    width: 48px;
    top: 0;
    z-index: 1;
}

.question-anchor-scroll::after {
    display: block;
    position: absolute;
    content: '';
    background: linear-gradient(0deg, var(--html-bg), rgba(255, 255, 255, 0));
    pointer-events: none;
    height: 64px;
    width: 48px;
    bottom: 10dvh;
    z-index: 1;
}

.question-card {
    max-height: 75dvh;
    min-height: 75dvh;
    margin: 40px 5dvw 40px 5dvw;
    transition: all var(--ease-in-out-quint) 400ms;
}

.mobile {
    .question-card {
        max-height: 90dvh;
        min-height: 90dvh;
        margin: 40px 0 40px 20px;
    }
}

</style>