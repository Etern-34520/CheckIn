<script setup>
import PartitionCache from "../../../../../data/PartitionCache.js";

const model = defineModel({
    type: Object,
    required: true,
});
const prop = defineProps({
    disabled: {type: Boolean, default: false},
});
const loaded = ref(false);
let partition;
PartitionCache.getAsync(model.value.partitionId).then((refPartition1) => {
    loaded.value = true;
    partition = refPartition1;
});
watch(() => model.value.minLimitEnabled,() => {
    if (model.value.minLimit > model.value.maxLimit) {
        model.value.minLimit = model.value.maxLimit;
    }
});
watch(() => model.value.maxLimitEnabled,() => {
    if (model.value.maxLimit < model.value.minLimit) {
        model.value.maxLimit = model.value.minLimit;
    }
});
</script>

<template>
    <div style="min-height: 72px" v-loading="!loaded">
        <template v-if="loaded">
            <div style="display: flex;flex-direction: row;align-items: center">
                <el-text style="margin-right: 24px">{{ partition.name }}</el-text>
                <el-tag type="info">可用小题数量：{{ partition.enabledQuestionCount }}</el-tag>
            </div>
            <div style="display: flex;padding: 8px 0;flex-wrap: wrap">
                <div style="display: flex;margin-right: 32px;">
                    <el-text style="margin-right: 8px;">下限</el-text>
                    <el-switch v-model="model.minLimitEnabled" :disabled="disabled"
                               style="margin-right: 8px;"></el-switch>
                    <el-input-number :disabled="disabled || !model.minLimitEnabled" v-model="model.minLimit" :min="0"
                                     :max="model.maxLimitEnabled?model.maxLimit:partition.enabledQuestionCount"
                                     class="no-init-animate"></el-input-number>
                </div>
                <div style="display: flex">
                    <el-text style="margin-right: 8px;">上限</el-text>
                    <el-switch v-model="model.maxLimitEnabled" :disabled="disabled"
                               style="margin-right: 8px;"></el-switch>
                    <el-input-number :disabled="disabled || !model.maxLimitEnabled" v-model="model.maxLimit"
                                     :min="model.minLimitEnabled?model.minLimit:undefined"
                                     :max="partition.enabledQuestionCount"
                                     class="no-init-animate"></el-input-number>
                </div>
            </div>
        </template>
    </div>
</template>

<style scoped>

</style>