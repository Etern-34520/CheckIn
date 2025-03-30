<script setup>
import UserDataInterface from "@/data/UserDataInterface.js";
import getAvatarUrlOf from "@/utils/Avatar.js";

const props = defineProps({
    record: {
        type: Object,
        required: true
    },
    showIp: {
        type: Boolean,
        default: false,
    }
})
</script>

<template>
    <div class="panel-1 request-record-item disable-init-animate" :class="record.status.toLowerCase()">
        <div class="pointer-color"></div>
        <div>
            <div style="display: flex;flex-direction: row;align-items: center;flex-wrap: wrap;">
                <transition name="blur-scale">
                    <div style="display: flex;flex-direction: row;margin-right: 16px;" v-if="record.qqnumber">
                        <el-avatar :src="getAvatarUrlOf(record.qqnumber)" style="margin-right: 8px;margin-bottom: 8px"
                                   size="small"/>
                        <el-text style="margin-bottom: 8px;">{{ record.qqnumber }}</el-text>
                    </div>
                </transition>
                <transition name="blur-scale">
                    <el-text style="margin-bottom: 8px;" v-if="showIp || !Boolean(record.qqnumber)">{{ record.ipString ? record.ipString : "unknown" }}</el-text>
                </transition>
                <div class="flex-blank-1"></div>
                <el-tag :type="record.status === 'ERROR' ? 'danger' : 'info'" style="margin-bottom: 8px;margin-right: 4px;">{{ record.status }}</el-tag>
                <el-tag style="margin-bottom: 8px">{{ record.type }}</el-tag>
<!--                {{ record }}-->
            </div>
            <div class="flex-blank-1"
                 style="margin-right: 32px">
                <el-text style="margin-right: 8px;min-width: 40px;">请求时间</el-text>
                <el-text>{{ record.time }}</el-text>
            </div>
        </div>
    </div>
</template>

<style scoped>
.request-record-item {
    display: flex;
    flex-direction: row;
    padding: 8px 12px;
    margin-bottom: 4px;

    > div {
        display: flex;
        flex-direction: column;
        flex: 1;

        > div {
            display: flex;
            flex-direction: row;
        }
    }
}

.pointer-color {
    max-width: 4px;
    min-width: 4px;
    width: 4px;
    margin-left: -12px;
    margin-right: 8px;
    border-radius: 2px;
    background: var(--el-color-success);
}

.error .pointer-color {
    background: var(--el-color-danger);
}
.success .pointer-color{
    background: var(--el-color-success);
}
</style>