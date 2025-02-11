<script setup>
import UserDataInterface from "@/data/UserDataInterface.js";
import getAvatarUrlOf from "@/utils/Avatar.js";

const props = defineProps({
    record: {
        type: Object,
        required: true
    }
})
</script>

<template>
    <div class="panel-1 request-record-item disable-init-animate">
        <div style="max-width: 4px;min-width: 4px;width: 4px;margin-left: -12px;margin-right: 8px;border-radius: 2px"
             :style="{background: record.examResult?record.examResult.colorHex:(record.status === 'ONGOING'?'var(--el-color-primary)':'var(--el-color-info)')}"></div>
        <div>
            <div style="display: flex;flex-direction: row;align-items: center;flex-wrap: wrap">
                <el-avatar :src="getAvatarUrlOf(record.qqNumber)" style="margin-right: 8px;margin-bottom: 8px" size="small"/>
                <el-text style="margin-bottom: 8px">{{ record.qqNumber }}</el-text>
                <div class="flex-blank-1"></div>
                <div style="display: flex;flex-direction: row;margin-bottom: 8px;flex-wrap: wrap">
                    <transition name="blur-scale" mode="out-in">
                        <div style="display:flex;flex-direction: row" v-if="record.examResult">
                            <el-tag type="info" style="margin-right: 4px;">{{ record.examResult.level }}</el-tag>
                            <el-tag type="info" style="margin-right: 4px;">{{ record.examResult.score }}</el-tag>
                        </div>
                    </transition>
                    <transition name="blur-scale" mode="out-in">
                        <el-tag :key="record.status">{{ record.status }}</el-tag>
                    </transition>
                </div>
            </div>
            <div style="flex-wrap: wrap;">
                    <div class="flex-blank-1" :key="record.generateTime"
                         style="display: flex;flex-direction: row;margin-right: 32px">
                        <el-text style="margin-right: 8px;min-width: 40px;">生成时间</el-text>
                        <el-text>{{ record.generateTime }}</el-text>
                    </div>
                <transition name="blur-scale" mode="out-in">
                    <div class="flex-blank-1" v-if="record.submitTime" :key="record.submitTime"
                         style="display: flex;flex-direction: row;">
                        <el-text style="margin-right: 8px;min-width: 40px;">提交时间</el-text>
                        <el-text>{{ record.submitTime }}</el-text>
                    </div>
                    <div class="flex-blank-1" v-else-if="record.status === 'ONGOING'" :key="record.expireTime"
                         style="display: flex;flex-direction: row;">
                        <el-text style="margin-right: 8px;min-width: 40px;">过期时间</el-text>
                        <el-text>{{ record.expireTime }}</el-text>
                    </div>
                </transition>
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
</style>