<script setup>
import getAvatarUrlOf from "@/utils/Avatar.js";
import {MoreFilled} from "@element-plus/icons-vue";
import WebSocketConnector from "@/api/websocket.js";

const props = defineProps({
    userGroup: {
        type: Object,
        required: true
    }
});

const deleteGroup = () => {
    WebSocketConnector.send({
        type: "deleteRole",
        roleType: props.userGroup.type
    });
}
</script>

<template>
    <div class="panel-1"
         style="display: flex;flex-direction: row;padding: 8px 12px;align-items: center;margin-bottom: 2px;height: 20px">
            <el-text style="font-size: 16px;align-self: baseline;max-height:21px;overflow: hidden;">{{ userGroup.type }}</el-text>
        <div class="flex-blank-1"></div>
        <el-dropdown trigger="click">
            <el-button link @click.stop class="no-init-animate">
                <el-icon>
                    <more-filled/>
                </el-icon>
            </el-button>
            <template #dropdown>
                <el-dropdown-item @click="deleteGroup"><el-text type="danger">删除</el-text></el-dropdown-item>
            </template>
        </el-dropdown>
    </div>
</template>

<style scoped>

</style>