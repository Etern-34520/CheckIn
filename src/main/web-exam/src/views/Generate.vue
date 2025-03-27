<script setup>
import getAvatarUrlOf from "@/Avatar.js";
import HarmonyOSIcon_InfoCircle from "@/icons/HarmonyOSIcon_InfoCircle.vue";
import router from "@/router/index.js";
import {ElMessage, ElMessageBox} from "element-plus";
import {ArrowLeftBold} from "@element-plus/icons-vue";
import _Loading_ from "@/components/_Loading_.vue";

const {proxy} = getCurrentInstance();
const props = defineProps({
    extraData: {
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
    if (!props.extraData.requiredPartitions.includes(Number(id))) {
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

const loadingExam = ref(false);
const startExam = () => {
    loadingExam.value = true;
    proxy.$http.post("generate", {
        qq: qqNumber.value,
        partitionIds: selectedPartitions.value
    }).then((data) => {
        if (data.type !== "error") {
            proxy.$cookies.set("examInfo", data, "7d");
            proxy.$cookies.set("phrase", "examining", "7d");
            proxy.$cookies.remove("submissions");
            proxy.$cookies.remove("timestamps");
            router.push({name: "examining"});
        } else {
            loadingExam.value = false;
            ElMessageBox.alert(
                    data.cnDescription?data.cnDescription:data.enDescription?data.enDescription:data.exceptionType,
                    "生成题目时出错", {
                        type: "error",
                        draggable: true,
                        showClose: false,
                        confirmButtonText: "返回修改生成选项"
                    }
            )
        }
    }, (err) => {
        loadingExam.value = false;
        ElMessage({
            type: "error",
            message: "生成题目时出错" + err.message
        })
    })
}

const validate1 = computed(() => selectedPartitions.value.length >= props.extraData.partitionRange[0] && selectedPartitions.value.length <= props.extraData.partitionRange[1]);
const validate2 = computed(() => qqNumber.value > 10000 && qqNumber.value < 100000000000);

const back = () => {
    proxy.$cookies.remove("phrase");
    router.push({name: "facade"});
}
</script>

<template>
    <div class="auto-padding-center" style="flex:1;padding-bottom: 200px;">
        <el-button link size="large" @click="back"
                   style="margin-top: 36px;align-self: baseline;padding: 8px 16px !important;font-size: 1em">
            <el-icon><ArrowLeftBold/></el-icon>返回
        </el-button>
        <template v-if="(extraData.requiredPartitions && extraData.requiredPartitions.length > 0) || (selectablePartitions.length > 0)">
            <el-text style="font-size: 24px;align-self: baseline;margin-top: 24px">选择分区</el-text>
            <div class="panel" style="padding: 16px 24px;margin-top: 36px" v-if="extraData.requiredPartitions && extraData.requiredPartitions.length > 0">
                <el-text size="large" style="align-self: baseline">必选分区</el-text>
                <div style="display: flex;flex-direction: row;flex-wrap: wrap;margin-top: 16px;">
                    <el-tag size="large" type="info" style="font-size: 14px;margin: 2px"
                            v-for="requiredPartitionId of extraData.requiredPartitions">
                        {{ partitions[requiredPartitionId] }}
                    </el-tag>
                </div>
            </div>
            <div class="panel" v-if="selectablePartitions.length > 0" style="padding: 16px 24px;margin-top: 24px">
                <div style="display: flex;flex-direction: row;flex-wrap: wrap;">
                    <el-text size="large" style="margin-right: 8px;">可选分区</el-text>
                    <el-text>{{ selectedPartitions.length }} / {{ selectablePartitions.length }}</el-text>
                    <el-text style="margin-left: 8px;"
                             :type="validate1?'info':'danger'">
                        请选择 {{ extraData.partitionRange[0] }} ~ {{ extraData.partitionRange[1] }} 个
                    </el-text>
                </div>
                <div style="display: flex;flex-direction: row;flex-wrap: wrap;margin-top: 16px;">
                    <el-check-tag size="large" type="info" style="font-size: 14px;margin: 2px;"
                                  v-for="partition of selectablePartitions"
                                  :checked="selectedPartitions.includes(Number(partition.id))"
                                  @click="selectPartition(Number(partition.id))">
                        {{ partition.name }}
                    </el-check-tag>
                </div>
            </div>
        </template>
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
            <el-input-number :class="{error: !validate2}" v-model="qqNumber"
                             :controls="false" style="min-width: min(70vw,200px)"/>
        </div>
        <div class="flex-blank-1"></div>
        <el-button type="primary" size="large" :loading="loadingExam" :loading-icon="_Loading_"
                   style="margin-top: 36px;align-self: center;min-width: 180px"
                   @click="startExam" :disabled="!(validate1 && validate2)">开始答题
        </el-button>
    </div>
</template>

<style scoped>

</style>