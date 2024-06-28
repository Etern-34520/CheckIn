<script setup>
import getAvatarUrlOf from "@/utils/Avatar.js";
import {MoreFilled} from "@element-plus/icons-vue";

defineProps({
    user: {
        type: Object,
        required: true
    }
})
</script>

<template>
    <div class="panel-1"
         style="display: flex;flex-direction: row;padding: 8px 12px;align-items: center;margin-bottom: 2px">
        <el-avatar shape="circle" style="width: 36px;height: 36px;margin-right: 8px;flex: none;"
                   :src="getAvatarUrlOf(user.qq)"/>
        <div style="display:flex;flex-direction: column;justify-content: center;">
            <el-text style="font-size: 16px;align-self: baseline;max-height:21px;overflow: hidden;">{{ user.name }}</el-text>
            <el-text style="font-size: 12px;align-self: baseline" type="info">{{ user.qq }}</el-text>
        </div>
        <div class="flex-blank-1"></div>
        <el-tag style="font-size: 12px" type="info">{{ user.role }}</el-tag>
        <el-dropdown trigger="click">
            <el-button link>
                <el-icon>
                    <more-filled/>
                </el-icon>
            </el-button>
            <template #dropdown>
                <el-dropdown-item>删除</el-dropdown-item>
            </template>
        </el-dropdown>
    </div>
</template>
<style scoped>

</style>