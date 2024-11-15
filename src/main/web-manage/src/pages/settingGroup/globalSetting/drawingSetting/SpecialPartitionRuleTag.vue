<script setup>
import PartitionCache from "../../../../data/PartitionCache.js";

const model = defineModel({
    type: Object,
    required: true,
});
const prop = defineProps({
    disabled: {type: Boolean, default: false},
});
const QuestionCount = computed(() => 0);
const loaded = ref(false);
let partition;
PartitionCache.getSync(model.value.partitionId).then((partition1) => {
    loaded.value = true;
    partition = partition1;
})
</script>

<template>
    <div style="min-height: 72px" v-loading="!loaded">
        <template v-if="loaded">
            <el-text>{{ partition.name }}</el-text>
            <div style="display: flex;padding: 8px 0;flex-wrap: wrap">
                <div style="display: flex;margin-right: 32px;">
                    <el-text style="margin-right: 8px;">下限</el-text>
                    <el-switch v-model="model.minLimitEnabled" :disabled="disabled"
                               style="margin-right: 8px;"></el-switch>
                    <el-input-number :disabled="disabled || !model.minLimitEnabled" v-model="model.minLimit" :min="0"
                                     :max="model.maxLimit" class="no-init-animate"></el-input-number>
                </div>
                <div style="display: flex">
                    <el-text style="margin-right: 8px;">上限</el-text>
                    <el-switch v-model="model.maxLimitEnabled" :disabled="disabled"
                               style="margin-right: 8px;"></el-switch>
                    <el-input-number :disabled="disabled || !model.maxLimitEnabled" v-model="model.maxLimit"
                                     :min="model.minLimit" class="no-init-animate"></el-input-number>
                </div>
            </div>
        </template>
    </div>
</template>

<style scoped>

</style>