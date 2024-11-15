<script setup>
import CreateNewPartitionDialog from "@/components/question/CreateNewPartitionPop.vue";
import HarmonyOSIcon_Plus from "@/components/icons/HarmonyOSIcon_Plus.vue";

const createPartitionVisible = ref(false);
</script>
<template>
    <el-popover trigger="click" v-model:visible="createPartitionVisible"
                width="400">
        <template #reference>
            <el-button text size="small" :icon="HarmonyOSIcon_Plus"
                       class="flex-blank-1 disable-tree-item-hover disable-tree-item-focus disable-tree-checkbox">
                <el-text>创建分区</el-text>
            </el-button>
        </template>
        <template #default>
            <create-new-partition-dialog
                @on-confirm="createPartitionVisible = false;"
                @on-cancel="createPartitionVisible = false;" size="default"/>
        </template>
    </el-popover>
</template>
<style scoped>

.dragHover * {
    color: var(--front-color-dark) !important;
}

.question-info-panel > .grid1 > .padding > div {
    min-height: 25px;
}

.errorsDescription > * {
    margin-right: 8px;
}

.choicesList > * {
    min-width: 40px;
    margin-right: 4px;
}

</style>