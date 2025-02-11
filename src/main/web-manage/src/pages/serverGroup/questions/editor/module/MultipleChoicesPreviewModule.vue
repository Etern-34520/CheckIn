<script setup>
import Waterfall from "@/components/common/Waterfall.vue";

const props = defineProps({
    questionInfo: {
        type: Object,
        required: true
    }
});
</script>

<template>
    <waterfall :data="questionInfo.question.choices" :min-row-width="400">
        <template #item="{item,index}">
            <div style="padding: 4px;">
                <el-button style="width: 100%;height: 100%">
                    <el-text>{{item.content}}</el-text>
                </el-button>
            </div>
        </template>
    </waterfall>
</template>

<style scoped>

</style>