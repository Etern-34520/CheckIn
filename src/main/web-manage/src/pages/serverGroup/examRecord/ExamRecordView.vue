<script setup>
import 'splitpanes/dist/splitpanes.css'
import ResponsiveSplitpane from "@/components/common/ResponsiveDoubleSplitpane.vue";
import WebSocketConnector from "@/api/websocket.js";
import Collapse from "@/components/common/Collapse.vue";
import ExamRecordItem from "@/components/common/ExamRecordItem.vue";
import router from "@/router/index.js";

const loading = ref(false);

const fromDate = new Date();
fromDate.setUTCDate(fromDate.getUTCDate() - 7);
fromDate.setUTCHours(16, 0, 0, 0);

const toDate = new Date();
// toDate.setUTCDate(toDate.getUTCDate() - 1);
toDate.setUTCHours(16, 0, 0, 0);

const dateRange = ref([fromDate, toDate]);

const data = ref({});

const getExamRecords = async () => {
    loading.value = true;
    WebSocketConnector.send({
        type: "getExamRecords",
        from: dateRange.value[0].toISOString(),
        to: dateRange.value[1].toISOString()
    }).then((response) => {
        data.value = response.examRecords;
        loading.value = false;
    });
}
getExamRecords();
const channel = WebSocketConnector.subscribe("examRecord", (data1) => {
    console.log(data1);
    const examRecord = data1.examRecord;
    const generateDate = examRecord.generateTime.split(" ")[0];
    const submitDate = examRecord.submitTime ? examRecord.submitTime.split(" ")[0] : null;
    console.log(generateDate);
    console.log(submitDate);
    console.log(data.value);
    if (data.value[generateDate]) {
        if (data.value[generateDate][examRecord.id]) {
            data.value[generateDate][examRecord.id] = examRecord;
        } else {
            let target = {}
            target[examRecord.id] = examRecord;
            data.value[generateDate] = Object.assign(target,data.value[generateDate]);//place in front
        }
    }
    if (submitDate && submitDate !== generateDate && data.value[submitDate]) {
        if (data.value[submitDate][examRecord.id]) {
            data.value[submitDate][examRecord.id] = examRecord;
        } else {
            let target = {}
            target[examRecord.id] = examRecord;
            data.value[submitDate] = Object.assign(target,data.value[submitDate]);//place in front
        }
    }
});

onBeforeUnmount(() => {
    channel.unsubscribe();
})

watch(() => dateRange, getExamRecords, {deep: true});

function formatDate(timer) {
    const year = timer.getFullYear()
    const month = timer.getMonth() + 1
    const day = timer.getDate()
    return `${pad(year, 4)}-${pad(month)}-${pad(day)}`
}

function pad(timeEl, total = 2, str = '0') {
    return timeEl.toString().padStart(total, str)
}

const getDates = (from, to) => {
    from = new Date(from);
    // from = from.setDate(from.getDate() + 1);
    to = new Date(to);
    to = to.setDate(to.getDate() + 1);

    let dates = [];
    for (let date = new Date(from); date < to; date.setDate(date.getDate() + 1)) {
        dates.push(formatDate(date));
    }
    return dates.reverse();
}

const openRecord = (id) => {
    router.push({name: 'exam-record-detail', params: {id: id}});
}

const filterText = ref("");
let textBlocks = [''];
watch(() => filterText.value, () => textBlocks = filterText.value.split(';'));
const doFilter = (filterText, record) => {
    for (const textBlock of textBlocks) {
        if (textBlock === '' || record.id.toLowerCase().includes(textBlock.toLowerCase()) ||
                record.qqNumber.toString().toLowerCase().includes(textBlock.toLowerCase()) ||
                record.status.toLowerCase().includes(textBlock.toLowerCase()) ||
                record.questionAmount.toString().includes(textBlock) ||
                record.examResult.score.toString().includes(textBlock) ||
                record.examResult.level.toString().includes(textBlock) ||
                record.examResult.message.toString().includes(textBlock)) {
            return true;
        }
    }
    return false;
}
</script>

<template>
    <responsive-splitpane ref="responsiveSplitpane" show-left-label="记录列表">
        <template #left>
            <div style="display: flex;flex-direction: column">
                <div style="display: flex;flex-direction: row;">
                    <el-date-picker type="daterange"
                                    unlink-panels
                                    range-separator="~"
                                    start-placeholder="1"
                                    end-placeholder="2"
                                    style="flex:1"
                                    v-model="dateRange"/>
                </div>
                <el-input prefix-icon="Search" v-model="filterText" placeholder="搜索 (以 &quot;,&quot; 分词)"/>
            </div>
            <el-scrollbar v-loading="loading">
                <div style="padding: 8px">
                    <collapse :expanded="true" v-for="dateString in getDates(dateRange[0],dateRange[1])"
                              :title-background="false"
                              :content-background="false" style="margin-top: 16px">
                        <template #title>
                            <div style="display: flex;flex-direction: row;align-items: center;height: 100%;">
                                <el-text style="align-self: center;margin-right: 16px;">
                                    {{ dateString }}
                                </el-text>
                                <el-tag type="info" size="small">{{
                                        data[dateString] ? Object.keys(data[dateString]).length : 0
                                    }}
                                </el-tag>
                            </div>
                        </template>
                        <template #content>
                            <template v-if="data[dateString]">
                                <transition-group name="slide-hide">
                                    <template v-for="(record,id,index) in data[dateString]">
                                        <div class="slide-hide-base" v-if="doFilter(filterText,record)"
                                             :key="record.id">
                                            <exam-record-item style="min-height: 0" :record="record"
                                                              class="clickable" @click="openRecord(record.id)"/>
                                        </div>
                                    </template>
                                </transition-group>
                            </template>
                            <el-text type="info" v-else>无数据</el-text>
                        </template>
                    </collapse>
                </div>
            </el-scrollbar>
        </template>
        <template #right>
            <router-view v-slot="{ Component }">
                <transition mode="out-in" name="question-editor">
                    <div v-if="!Component"
                         style="width: 100%;height: 100%;display: flex;flex-direction: column;align-items: center;justify-content: center">
                        <el-text type="info" size="large">
                            选择记录以查看
                        </el-text>
                    </div>
                    <component v-else :is="Component"/>
                </transition>
            </router-view>
        </template>
    </responsive-splitpane>
</template>

<style scoped>

</style>