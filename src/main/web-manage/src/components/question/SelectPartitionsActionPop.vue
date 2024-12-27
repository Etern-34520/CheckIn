<script setup>
// import {ref,defineEmits,defineProps} from "vue";
import PartitionCache from "@/data/PartitionCache.js";
import CreateNewPartitionDialog from "@/components/question/CreateNewPartitionPop.vue";

const props = defineProps({
    size: {
        type: String,
        default: "default",
    },
    showCreatePartition: {
        type: Boolean,
        default: true
    }
})

const emits = defineEmits(["onConfirm"]);

const isCreating = ref(false);
const partitionIds = ref([]);
let partitions = null;
PartitionCache.getRefPartitionsAsync().then((resp) => {
    partitions = resp;
});
defineExpose({
    "clear": () => {
        partitionIds.value = [];
    }
});
</script>
<template>
    <div style="display: flex;flex-direction: row" v-loading="partitions === null">
        <el-select
            class="not-empty"
            v-model="partitionIds"
            v-if="partitions !== null"
            placeholder="选择分区"
            multiple
            filterable
            @focusout="isCreating = false"
            style="flex:4;width:0"
        >
            <el-option v-for="(partition,id) in partitions" :key="partition.id"
                       :label="partition.name" :value="partition.id"></el-option>
            <template #footer v-if="showCreatePartition">
                <transition name="creatingPartition" mode="out-in">
                    <el-button v-if="!isCreating" text bg size="small" style="width: 100%"
                               @click="isCreating = true">
                        创建新分区
                    </el-button>
                    <template v-else>
                        <createNewPartitionDialog @on-cancel="isCreating = false" @on-confirm="isCreating = false"/>
                    </template>
                </transition>
            </template>
        </el-select>
        <el-button type="primary" :size="size" @click="$emit('onConfirm',partitionIds)"
                   :disabled="partitionIds.length===0">确定
        </el-button>
    </div>
</template>

<style scoped>
/*noinspection CssUnusedSymbol*/
.creatingPartition-enter-active, .creatingPartition-leave-active {
    transition: all 0.3s var(--ease-in-bounce-1);
}

/*noinspection CssUnusedSymbol*/
.creatingPartition-enter-from, .creatingPartition-leave-to {
    opacity: 0;
    scale: 0.95;
}
</style>