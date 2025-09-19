<script setup>
import WebSocketConnector from "@/api/websocket.js";
import {ElMessage} from "element-plus";
import PartitionCache from "@/data/PartitionCache.js";
import SpecialPartitionRule from "@/pages/manage/settingGroup/globalSetting/generatingSetting/SpecialPartitionRuleTag.vue";
import Waterfall from "@/components/common/Waterfall.vue";

const editing = ref(false);
const data = ref({
    questionAmount: 0,
    questionScore: 5,
    partitionRange: [0, 100]
});
const loading = ref(true);
const error = ref(false);
let backup = {};
let backupJSON;
let refPartitions;
const cancel = () => {
    data.value = backup;
    editing.value = false;
}
const startEditing = () => {
    backupJSON = JSON.stringify(data.value)
    backup = JSON.parse(backupJSON);
    editing.value = true;
}
const finishEditing = () => {
    editing.value = false;
    if (backupJSON !== JSON.stringify(data.value)) {
        WebSocketConnector.send({
            type: "saveGeneratingSetting",
            data: {
                data: data.value
            }
        }).then(() => {
            ElMessage({
                type: "success", message: "保存成功"
            })
        }, (err) => {
            ElMessage({
                type: "error", message: "保存失败"
            })
        })
    }
}

const partitionCount = computed(() => Object.keys(refPartitions.value).length - (data.value.requiredPartitions ? data.value.requiredPartitions.length : 0));

const getData = () => {
    loading.value = true;
    error.value = false;
    Promise.all([
        new Promise((resolve, reject) => {
            PartitionCache.getRefPartitionsAsync().then((partitions) => {
                refPartitions = partitions;
                resolve();
            }, (err) => {
                ElMessage({
                    type: "error", message: "获取分区失败"
                });
                loading.value = false;
                error.value = true;
            });
        }), new Promise((resolve, reject) => {
            WebSocketConnector.send({
                type: "getGeneratingSetting",
            }).then((response) => {
                data.value = response.data.data;
                if (!data.value.specialPartitionLimits) data.value.specialPartitionLimits = ref({});
                if (!data.value.specialLimitsEnabledPartitions) data.value.specialLimitsEnabledPartitions = ref([]);
                if (data.value.specialPartitionLimits) {
                    for (const key in data.value.specialPartitionLimits) {
                        data.value.specialLimitsEnabledPartitions.push(key);
                    }
                }
                resolve()
            }, (err) => {
                ElMessage({
                    type: "error", message: "获取设置失败"
                });
                loading.value = false;
                error.value = true;
            });
        })
    ]).then(() => {
        loading.value = false;
    });
}
getData();

const updateLimits = (partitionIds) => {
    const removedIds = [];
    for (const partitionId1 in data.value.specialPartitionLimits) {
        if (!partitionIds.includes(partitionId1)) {
            removedIds.push(partitionId1);
        }
    }
    for (const removedId of removedIds) {
        delete data.value.specialPartitionLimits[removedId];
    }
    for (const partitionId of partitionIds) {
        if(!data.value.specialPartitionLimits[partitionId]) {
            data.value.specialPartitionLimits[partitionId] = {
                partitionId: partitionId,
                minLimitEnabled: false,
                minLimit: 0,
                maxLimitEnabled: false,
                maxLimit: 100,
            };
        }
    }
}
</script>

<template>
    <div style="display: flex;flex-direction: column;">
        <div style="display: flex;flex-direction: row;margin-bottom: 32px;">
            <el-text style="align-self:baseline;font-size: 24px">生成设置</el-text>
            <div style="display: flex;margin-left: 32px;">
                <transition-group name="blur-scale">
                    <el-button-group key="button-group">
                        <transition-group name="blur-scale">
                            <el-button class="disable-init-animate" style="margin-right: 4px;"
                                       @click="editing ? finishEditing():startEditing()"
                                       :disabled="loading || error" key="edit">
                                {{ editing ? '完成' : '编辑' }}
                            </el-button>
                            <el-button class="disable-init-animate" style="margin-right: 24px;"
                                       @click="cancel" v-if="editing" key="cancel">
                                取消
                            </el-button>
                        </transition-group>
                    </el-button-group>
                </transition-group>
            </div>
        </div>
        <el-scrollbar v-loading="loading">
            <div style="display: flex;flex-direction: column;align-items: center">
                <transition name="blur-scale" mode="out-in">
                    <div v-if="!loading && !error"
                         style="max-width: 1280px;width: min(85%,1280px);display: flex;flex-direction: column">
                        <div style="display: flex;flex-direction: row;flex-wrap: wrap">
                            <div style="display: flex;flex-direction: column;margin-right: 32px">
                                <el-text size="large" class="field-label">题量</el-text>
                                <div style="display: flex;padding: 8px 0">
                                    <el-input-number v-model="data.questionAmount" :min="1" :disabled="!editing"></el-input-number>
                                </div>
                            </div>
                            <div style="display: flex;flex-direction: column;">
                                <el-text size="large" class="field-label">每题分数</el-text>
                                <div style="display: flex;padding: 8px 0">
                                    <el-input-number v-model="data.questionScore" :disabled="!editing"></el-input-number>
                                </div>
                            </div>
                        </div>
                        <div class="field-label" style="display: flex;flex-direction: row">
                            <el-text size="large" style="margin-right: 8px">可选分区数</el-text>
                            <el-text size="small" type="warning"
                                     v-if="partitionCount===0">
                                无可选分区
                            </el-text>
                        </div>
                        <div style="display: flex;padding: 8px 0;flex-wrap: wrap">
                            <el-slider range v-model="data.partitionRange" style="margin-right: 20px;"
                                       :max="partitionCount"
                                       :disabled="!editing || partitionCount === 0">
                            </el-slider>
                            <div style="display: flex;padding: 8px 0">
                                <el-input-number v-model="data.partitionRange[0]"
                                                 :disabled="!editing || partitionCount === 0"
                                                 :max="Math.min(partitionCount,data.partitionRange[1])"
                                                 style="margin-right: 8px"></el-input-number>
                                <el-text style="margin-right: 8px;font-size: 24px">~</el-text>
                                <el-input-number v-model="data.partitionRange[1]"
                                                 :disabled="!editing || partitionCount === 0"
                                                 :max="partitionCount">
                                </el-input-number>
                            </div>
                        </div>
                        <el-text size="large" class="field-label">必选分区</el-text>
                        <div style="display: flex;flex-direction: row;flex-wrap: wrap">
                            <el-text style="align-self: center" type="info">在生成时显示分区名称</el-text>
                            <el-switch style="margin-left: 8px;align-self: center" :disabled="!editing"
                                       v-model="data.showRequiredPartitions"/>
                        </div>
                        <el-select
                                v-model="data.requiredPartitions"
                                placeholder="必选分区"
                                multiple
                                filterable
                                size="large"
                                style="margin: 8px 0;"
                                :disabled="!editing"
                        >
                            <el-option v-for="(partition,id) in refPartitions" :key="partition.id"
                                       :label="partition.name" :value="partition.id"></el-option>
                        </el-select>
                        <el-text size="large" class="field-label">抽取策略</el-text>
                        <el-radio-group v-model="data.samplingStrategy" size="large" style="padding: 4px 20px"
                                        :disabled="!editing">
                            <el-radio value="weighted">以各分区题目数量为权重按比抽取</el-radio>
                            <el-radio value="random">随机从所选及必选分区抽取</el-radio>
                        </el-radio-group>
                        <div style="padding: 0 8px;display: flex;flex-direction: column">
                            <el-text class="field-label">特殊分区限制</el-text>
                            <div style="display: flex;flex-direction: column;margin-top: 4px">
                                <el-select
                                        v-model="data.specialLimitsEnabledPartitions"
                                        placeholder="特殊分区限制"
                                        multiple
                                        @change="updateLimits"
                                        filterable
                                        size="large"
                                        style="margin: 8px 0;"
                                        :disabled="!editing">
                                    <el-option v-for="(partition,id) in refPartitions" :key="partition.id"
                                               :label="partition.name" :value="partition.id"></el-option>
                                </el-select>
                                <waterfall :data="data.specialLimitsEnabledPartitions" :min-row-width="600">
                                    <template #item="{item,index}">
                                        <div class="panel-1 disable-init-animate"
                                             style="padding: 8px 24px;margin: 2px;display: flex;align-items: center"
                                             :key="index">
                                            <special-partition-rule :disabled="!editing" style="margin:2px;flex: 1"
                                                                    v-model="data.specialPartitionLimits[data.specialLimitsEnabledPartitions[index]]"/>
                                        </div>
                                    </template>
                                </waterfall>
                            </div>
                        </div>
                        <el-text size="large" class="field-label">补全策略</el-text>
                        <el-radio-group v-model="data.completingStrategy" size="large" style="padding: 4px 20px"
                                        :disabled="!editing">
                            <el-radio value="none">不补全</el-radio>
                            <el-radio value="required">尝试用必选分区补全</el-radio>
                            <el-radio value="all">用所有分区补全</el-radio>
                            <el-radio value="selected">
                                尝试用以下分区补全
                                <el-select
                                        v-model="data.completingPartitions"
                                        placeholder="补全分区"
                                        multiple
                                        filterable
                                        style="margin: 8px 0 8px 8px;min-width: min(400px,10%)"
                                        :class="{error:(editing && data.completingStrategy==='selected') && (!data.completingPartitions || data.completingPartitions.length===0)}"
                                        :disabled="!(editing && data.completingStrategy==='selected')">
                                    <el-option v-for="(partition,id) in refPartitions" :key="partition.id"
                                               :label="partition.name" :value="partition.id"></el-option>
                                </el-select>
                            </el-radio>
                        </el-radio-group>
                    </div>
                    <div v-else-if="error" style="display:flex;flex-direction: column">
                        <el-empty description="获取设置失败"></el-empty>
                        <el-button link type="primary" @click="getData" size="large">重试</el-button>
                    </div>
                </transition>
            </div>
        </el-scrollbar>
    </div>
</template>

<style scoped>
.field-label {
    align-self: baseline;
    margin-top: 16px;
}
</style>