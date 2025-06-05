<script setup>
import 'splitpanes/dist/splitpanes.css'
import ResponsiveSplitpane from "@/components/common/ResponsiveDoubleSplitpane.vue";
import Collapse from "@/components/common/Collapse.vue";
import WebSocketConnector from "@/api/websocket.js";
import RequestRecordItem from "@/pages/serverGroup/requestRecord/RequestRecordItem.vue";
import router from "@/router/index.js";

const loading = ref(false);

const fromDate = new Date();
fromDate.setUTCDate(fromDate.getUTCDate() - 2);

const toDate = new Date();

const dateRange = ref([fromDate, toDate]);

const data = ref({});
const error = ref(false);

const getRequestRecords = async () => {
    loading.value = true;
    error.value = false;
    WebSocketConnector.send({
        type: "getRequestRecords",
        data: {
            from: dateRange.value[0].toISOString(),
            to: dateRange.value[1].toISOString()
        }
    }).then((response) => {
        data.value = response.data.requestRecords;
        loading.value = false;
    }).catch((errorResp) => {
        loading.value = false;
        error.value = true;
    });
}

getRequestRecords();
const channel = WebSocketConnector.subscribe("requestRecords", (data1) => {
    // console.log(data1);
    const requestRecord = data1.data;
    const date = requestRecord.time.split(" ")[0];
    // console.log(data.value);
    if (data.value[date]) {
        let target = {}
        target[requestRecord.id] = requestRecord;
        data.value[date] = Object.assign(target, data.value[date]);//place in front
    }
});

onBeforeUnmount(() => {
    channel.unsubscribe();
})

watch(() => dateRange, getRequestRecords, {deep: true});

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

const filterText = ref("");
let textBlocks = [''];
watch(() => filterText.value, () => textBlocks = filterText.value.split(';'));
const doFilter = (filterText, record) => {
    for (const textBlock of textBlocks) {
        if (textBlock === '' || record.id.toLowerCase().includes(textBlock.toLowerCase()) ||
                (record.qqnumber && record.qqnumber.toString().includes(textBlock)) ||
                record.ipString.toLowerCase().includes(textBlock.toLowerCase()) ||
                record.type.toLowerCase().includes(textBlock.toLowerCase()) ||
                record.status.toLowerCase().includes(textBlock.toLowerCase()) ||
                record.time.toString().includes(textBlock)) {
            return true;
        }
    }
    return false;
}


const responsiveSplitpane = ref();

const stop = router.afterEach((to, from) => {
    if (to.params.id === undefined) {
        responsiveSplitpane.value.showLeft();
    } else {
        responsiveSplitpane.value.hideLeft();
    }
});

onUnmounted(() => {
    stop();
});

const openRecord = (id) => {
    router.push({name: 'request-record-detail', params: {id: id}});
}

const showEmptyDates = ref(false);
const showIP = ref(false);
</script>

<template>
    <responsive-splitpane ref="responsiveSplitpane" show-left-label="记录列表">
        <template #left>
            <el-scrollbar wrap-style="height: 100%" view-style="height: 100%">
                <div style="display: flex;flex-direction: column;min-width: 270px;height: 100%">
                    <div style="display: flex;flex-direction: column;">
                        <div style="display: flex;flex-direction: row;flex-wrap: wrap">
                            <el-date-picker type="daterange"
                                            unlink-panels
                                            range-separator="~"
                                            start-placeholder="1"
                                            end-placeholder="2"
                                            style="flex:1;min-width: 240px"
                                            :clearable="false"
                                            v-model="dateRange"/>
                            <div style="display: flex;flex-direction: row;margin-left: 16px">
                                <el-text style="align-self: center;margin-right: 8px;">显示空日期</el-text>
                                <el-switch v-model="showEmptyDates"/>
                            </div>
                            <div style="display: flex;flex-direction: row;margin-left: 16px">
                                <el-text style="align-self: center;margin-right: 8px;">始终显示IP</el-text>
                                <el-switch v-model="showIP"/>
                            </div>
                        </div>
                        <el-input prefix-icon="Search" v-model="filterText" style="margin-top: 8px;"
                                  placeholder="搜索 (以 &quot;;&quot; 分词)"/>
                    </div>
                    <el-scrollbar v-loading="loading">
                        <div style="display:flex;flex-direction: column;padding: 16px" v-if="error">
                            <el-text type="danger">获取失败</el-text>
                            <el-button link type="primary" style="margin-top: 8px" @click="getRequestRecords">重试
                            </el-button>
                        </div>
                        <div style="padding: 8px" v-else-if="!loading">
                            <transition-group name="smooth-height">
                                <template v-for="dateString in getDates(dateRange[0],dateRange[1])">
                                    <div class="smooth-height-base" :key="dateString"
                                         v-if="showEmptyDates || (data[dateString] && Object.keys(data[dateString]).length > 0)">
                                        <div>
                                            <collapse
                                                    v-if="showEmptyDates || Boolean(data[dateString])"
                                                    :expanded="Boolean(data[dateString])"
                                                    :title-background="false"
                                                    :content-background="false"
                                                    style="margin-top: 16px">
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
                                                                <div class="slide-hide-base"
                                                                     v-if="doFilter(filterText,record)"
                                                                     :key="record.id">
                                                                    <div>
                                                                        <request-record-item style="min-height: 0"
                                                                                             :show-ip="showIP"
                                                                                             :record="record"
                                                                                             class="clickable"
                                                                                             @click="openRecord(record.id)"/>
                                                                    </div>
                                                                </div>
                                                            </template>
                                                        </transition-group>
                                                    </template>
                                                    <el-text type="info" v-else>无数据</el-text>
                                                </template>
                                            </collapse>
                                        </div>
                                    </div>
                                </template>
                                <div class="smooth-height-base" v-if="!showEmptyDates && Object.keys(data).length === 0"
                                     key="empty">
                                    <div>
                                        <el-empty description="无数据"/>
                                    </div>
                                </div>
                            </transition-group>
                        </div>
                    </el-scrollbar>
                </div>
            </el-scrollbar>
        </template>
        <template #right>
            <router-view v-slot="{ Component }">
                <transition mode="out-in" name="blur-scale">
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