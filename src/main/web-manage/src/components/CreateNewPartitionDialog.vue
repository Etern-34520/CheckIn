<script setup>
// import {ref,defineEmits,defineProps} from "vue";
import PartitionTempStorage from "@/data/PartitionTempStorage.js";

const createdPartitionName = ref("")

const props = defineProps({
    size: {
        type: String,
        default: "small",
    }
})

const emits = defineEmits(["onCancel","onConfirm"]);

const onConfirmCreating = () => {
    if (createdPartitionName.value) {
        PartitionTempStorage.create(createdPartitionName.value).then((resp) => {
            console.log(resp)
        });
        /*allPartitions.value.push({name: createdPartitionName.value});*/
        emits("onConfirm", createdPartitionName.value);
        createdPartitionName.value = "";
    }
}

</script>
<template>
    <div style="display: flex;flex-direction: row">
        <el-input
            style="flex:1;margin-right: 4px"
            v-model="createdPartitionName"
            placeholder="分区名"
            :size="size"
        />
        <el-button-group>
            <el-button type="primary" :size="size" @click="onConfirmCreating"
                       :disabled="createdPartitionName===''||PartitionTempStorage.has(createdPartitionName)">确定
            </el-button>
            <el-button :size="size" @click="$emit('onCancel');createdPartitionName = '';">取消</el-button>
        </el-button-group>
    </div>
</template>
