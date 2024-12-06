<script setup>
import getAvatarUrlOf from "@/Avatar.js";
import HarmonyOSIcon_InfoCircle from "@/icons/HarmonyOSIcon_InfoCircle.vue";

const {proxy} = getCurrentInstance();
const props = defineProps({
    drawingData: {
        required: true,
        type: Object
    },
    partitions: {
        required: true,
        type: Object
    }
});
const selectablePartitions = [];
for (const [id, name] of Object.entries(props.partitions)) {
    if (!props.drawingData.requiredPartitions.includes(Number(id))) {
        selectablePartitions.push({
            id: id,
            name: name,
        })
    }
}

const selectedPartitions = ref([]);
const selectPartition = (partitionId) => {
    const index = selectedPartitions.value.indexOf(partitionId);
    if (index === -1) {
        selectedPartitions.value.push(partitionId);
    } else {
        selectedPartitions.value.splice(index, 1);
    }
}

const qqNumber = ref();

const startExam = () => {}

const validate1 = computed(() => selectedPartitions.value.length >= props.drawingData.partitionRange[0] && selectedPartitions.value.length <= props.drawingData.partitionRange[1]);
const validate2 = computed(() => qqNumber.value > 10000 && qqNumber.value < 100000000000);
</script>

<template>
    <div class="auto-padding-center">
        <el-text style="font-size: 24px;align-self: baseline">选择分区</el-text>
        <div class="panel" style="padding: 16px 24px;margin-top: 36px">
            <el-text size="large" style="align-self: baseline">必选分区</el-text>
            <div style="display: flex;flex-direction: row;flex-wrap: wrap;margin-top: 16px;">
                <el-tag size="large" type="info" style="font-size: 14px;margin: 2px"
                        v-for="requiredPartitionId of drawingData.requiredPartitions">
                    {{ partitions[requiredPartitionId] }}
                </el-tag>
            </div>
        </div>
        <div class="panel" style="padding: 16px 24px;margin-top: 24px">
            <div style="display: flex;flex-direction: row;flex-wrap: wrap;">
                <el-text size="large" style="margin-right: 8px;">可选分区</el-text>
                {{ selectedPartitions.length }}
                <el-text style="margin-left: 8px;"
                         :type="validate1?'info':'danger'">
                    请选择 {{ drawingData.partitionRange[0] }} ~ {{ drawingData.partitionRange[1] }} 个
                </el-text>
            </div>
            <div style="display: flex;flex-direction: row;flex-wrap: wrap;margin-top: 16px;">
                <el-check-tag size="large" type="info" style="font-size: 14px;margin: 2px"
                              v-for="partition of selectablePartitions"
                              :checked="selectedPartitions.includes(Number(partition.id))"
                              @click="selectPartition(Number(partition.id))">
                    {{ partition.name }}
                </el-check-tag>
            </div>
        </div>
        <div style="display: flex;flex-direction: row;align-items: center;margin-top: 36px">
            <el-text style="font-size: 24px;align-self: baseline;margin-right: 16px;">你的QQ号码</el-text>
            <el-popover trigger="hover" width="160">
                <template #reference>
                    <HarmonyOSIcon_InfoCircle :size="20"/>
                </template>
                <template #default>
                    <el-text>仅用于记录答题信息</el-text>
                </template>
            </el-popover>
        </div>
        <div style="display: flex;flex-direction: row;align-items: center;margin-top: 16px;margin-left: 16px;">
            <el-avatar style="width: 64px;height: 64px;margin-right: 16px" :src="getAvatarUrlOf(qqNumber)"/>
            <el-input-number :class="{error: validate2}" v-model="qqNumber"
                             :controls="false" style="min-width: min(70vw,200px)"/>
        </div>
        <el-button type="primary" size="large" style="margin-top: 36px;align-self: center" @click="startExam" :disabled="!(validate1 && validate2)">开始答题</el-button>
    </div>
</template>

<style scoped>

</style>