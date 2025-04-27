<script setup>

import {ArrowLeftBold} from "@element-plus/icons-vue";
import router from "@/router/index.js";
import RequestRecordItem from "@/pages/serverGroup/requestRecord/RequestRecordItem.vue";
import WebSocketConnector from "@/api/websocket.js";

const data = ref({});
const loading = ref(true);
const error = ref(true);

const loadData = () => {
    loading.value = true;
    error.value = false;
    WebSocketConnector.send({
        type: "getRelatedRequestOfExamData",
        data: {
            examDataId: router.currentRoute.value.params.id
        }
    }).then((response) => {
        loading.value = false;
        data.value = response.data.requestRecords;
    }, (err) => {
        loading.value = false;
        error.value = true;
    })
}

watch(() => router.currentRoute.value.params.id, loadData, {immediate: true});

const routeToRequestRecord = (id) => {
    router.push({name: "request-record-detail", params: {id: id}});
}
</script>
<template>
    <div style="min-width: 100%;display: flex;flex-direction: column;">
        <div style="display: flex;flex-direction: row;">
            <el-button class="disable-init-animate" style="margin-right: 16px"
                       @click="router.push({name: 'exam-record-detail'})">
                <el-icon>
                    <arrow-left-bold/>
                </el-icon>
                返回
            </el-button>
            <el-text size="large">
                相关请求
            </el-text>
        </div>
        <el-scrollbar v-loading="loading" style="margin-top: 8px">
            <div style="display:flex;flex-direction: column;padding: 16px" v-if="!loading && error">
                <el-text type="danger">获取失败</el-text>
                <el-button link type="primary" style="margin-top: 8px" @click="loadData">重试</el-button>
            </div>
            <div style="display: flex;flex-direction: column;width:calc(100% - 4px)" v-else-if="!loading">
                <request-record-item v-for="record of data" :record="record"
                                     class="clickable" @click="routeToRequestRecord(record.id)"/>
            </div>
        </el-scrollbar>
    </div>
</template>
<style scoped>
</style>