<script setup>
// import {ref,defineEmits,defineProps} from "vue";
import PartitionTempStorage from "@/data/PartitionTempStorage.js";

const props = defineProps({
    size: {
        type: String,
        default: "small",
    },
    partition: {
        type: Object,
        required: true,
    }
})

const newName = ref(props.partition.name);

const emits = defineEmits(["onOver"]);

const onConfirmCreating = () => {
    if (newName.value) {
        PartitionTempStorage.rename(props.partition,newName.value);
        /*allPartitions.value.push({name: createdPartitionName.value});*/
        // emits("onConfirm", createdPartitionName.value);
        emits("onOver");
        // newName.value = "";
    }
}

</script>
<template>
    <div style="display: flex;flex-direction: row">
        <el-input
            style="flex:1;margin-right: 4px"
            v-model="newName"
            placeholder="分区名"
            :size="size"
        />
        <el-button-group>
            <el-button type="primary" :size="size" @click="onConfirmCreating"
                       :disabled="PartitionTempStorage.has(newName)">确定
            </el-button>
            <el-button :size="size" @click="$emit('onOver')">取消</el-button>
        </el-button-group>
    </div>
</template>
