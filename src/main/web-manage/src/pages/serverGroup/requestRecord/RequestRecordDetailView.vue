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
import ObjectFieldViewer from "@/components/objectViewer/ObjectFieldViewer.vue";

const route = useRoute();
const data = ref();
const loading = ref(true);
const showLoading = ref(true);
watch(() => route.params.id, update, {immediate: true});

function update() {
    loading.value = true;
    setTimeout(() => {
        showLoading.value = loading.value;
    }, 200);
    WebSocketConnector.send({
        type: "getRequestRecordDetail",
        id: route.params.id
    }).then((response) => {
        data.value = response.requestRecord;
        loading.value = false;
        showLoading.value = false;
    });
}

const routeToRelatedExamRecord = (id) => {
    router.push({name: "exam-record-detail", params: {id: id}});
}
</script>

<template>
    <div style="display: flex;flex-direction: column;flex:1;height: 0" v-loading="showLoading">
        <div style="padding: 32px 16px 4px;display: flex;flex-direction: column;align-items: stretch;flex:1;height: 0"
             v-if="data">
            <transition name="blur-scale" mode="out-in">
                <div style="display: flex;flex-direction: row;align-items: center;margin-left: 16px;flex-wrap: wrap"
                     :key="data.id">
                    <div style="display: flex;flex-direction: column;margin-bottom: 16px;">
                        <div style="min-width: min(70vw,120px);display: flex;flex-direction: row;align-items: center;margin-bottom: 4px"
                             v-if="data.qqNumber">
                            <el-avatar size="small" style="align-self: center;margin-right: 8px;"
                                       :src="getAvatarUrlOf(data.qqNumber)"/>
                            <el-text size="large"
                                     style="align-self: center;">
                                {{ data.qqNumber }}
                            </el-text>
                        </div>
                        <div v-if="data.ipString" style="min-width: min(70vw,120px);display: flex;flex-direction: row;align-items: center;margin-bottom: 4px;margin-right: 4px">
                            <el-text style="align-self: baseline">
                                {{ data.ipString }}
                            </el-text>
                            <el-link type="info" target="_blank" style="margin-left: 8px;" :href="'https://iplark.com/'+data.ipString">
                                在Iplark上查询
                            </el-link>
                        </div>
                    </div>
                    <div style="display: flex;flex-direction: column;margin-right: 16px;margin-bottom: 16px;align-self: center;">
                        <el-tag style="align-self: baseline;margin-bottom: 8px;">{{ data.type }}</el-tag>
                        <el-tag style="align-self: baseline;" :type="data.status === 'SUCCESS'?'success':'danger'">
                            {{ data.status }}
                        </el-tag>
                    </div>
                    <div style="display: flex;flex-direction: column;margin-right: 16px;margin-bottom: 16px;flex:1;">
                        <div style="display: flex;flex-direction: row;align-items: center;margin-bottom: 8px;">
                            <el-text style="margin-right: 12px;" type="info">请求时间</el-text>
                            <el-text>{{ data.time }}</el-text>
                        </div>
                        <div style="display: flex;flex-direction: row;align-items: center;margin-bottom: 8px;">
                            <el-text style="margin-right: 12px;" type="info">Session ID</el-text>
                            <el-text>{{ data.sessionId }}</el-text>
                        </div>
                    </div>
                </div>
            </transition>
            <el-scrollbar style="flex: 1;height: 0;">
                <div style="display: flex;flex-direction: column;width:calc(100% - 4px);">
                    <transition name="smooth-height" mode="out-in">
                        <div class="smooth-height-base" v-if="data.relatedExamDataId">
                            <div>
                                <link-panel style="margin-top: 16px;" name="相关试题"
                                            description="结果 时间 题目 提交内容"
                                            icon="Link" @click="routeToRelatedExamRecord(data.relatedExamDataId)"/>
                            </div>
                        </div>
                    </transition>
                    <collapse style="padding: 16px;margin-bottom: 12px" :content-background="false"
                              :title-background="false" expanded>
                        <template #title>
                            <div style="margin-top: 12px;margin-bottom: 12px">
                                <el-text size="large">
                                    Request Headers
                                </el-text>
                            </div>
                        </template>
                        <template #content>
                            <object-field-viewer :data="data.requestHeaders"/>
                        </template>
                    </collapse>
                    <collapse style="padding: 16px;margin-bottom: 12px" :content-background="false"
                              :title-background="false" expanded>
                        <template #title>
                            <div style="margin-top: 12px;margin-bottom: 12px">
                                <el-text size="large">
                                    Request Attributes
                                </el-text>
                            </div>
                        </template>
                        <template #content>
                            <el-empty
                                    v-if="!Boolean(data.requestAttributes) || Object.keys(data.requestAttributes).length === 0"></el-empty>
                            <object-field-viewer v-else :data="data.requestAttributes"
                                                 name-style="width: max(30vw,240px);"/>
                        </template>
                    </collapse>
                    <collapse style="padding: 16px;margin-bottom: 12px" :content-background="false"
                              :title-background="false" expanded>
                        <template #title>
                            <div style="margin-top: 12px;margin-bottom: 12px">
                                <el-text size="large">
                                    Response Headers
                                </el-text>
                            </div>
                        </template>
                        <template #content>
                            <el-empty
                                    v-if="!Boolean(data.responseHeaders) || Object.keys(data.responseHeaders).length === 0"></el-empty>
                            <object-field-viewer v-else :data="data.responseHeaders"/>
                        </template>
                    </collapse>
                    <collapse style="padding: 16px;margin-bottom: 12px" :content-background="false"
                              :title-background="false" expanded>
                        <template #title>
                            <div style="margin-top: 12px;margin-bottom: 12px">
                                <el-text size="large">
                                    Extra Data
                                </el-text>
                            </div>
                        </template>
                        <template #content>
                            <el-empty
                                    v-if="!Boolean(data.extraData) || Object.keys(data.extraData).length === 0"></el-empty>
                            <object-field-viewer v-else :data="data.extraData"/>
                        </template>
                    </collapse>
                </div>
            </el-scrollbar>
        </div>
    </div>
</template>

<style scoped>
</style>