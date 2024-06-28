<script setup>
import {ArrowRightBold} from "@element-plus/icons-vue";

defineProps({
    icon: {
        type: Object,
        required: true
    },
    name: {
        type: String,
        required: true
    },
    description: {
        type: String,
        required: true
    }
})
</script>

<template>
    <div class="panel-1 clickable" style="display: flex;padding: 16px 24px;align-items: center">
        <el-icon size="20" style="margin-right: 8px">
            <component :is="icon"/>
        </el-icon>
        <div style="display:flex;flex-direction: column;justify-content: center;">
            <el-text style="font-size: 16px;align-self: baseline">{{ name }}</el-text>
            <el-text style="font-size: 12px;align-self: baseline" type="info">{{ description }}</el-text>
        </div>
        <div class="flex-blank-1"></div>
        <el-icon>
            <ArrowRightBold/>
        </el-icon>
    </div>
</template>

<style scoped>

</style>