<script setup>
import getAvatarUrlOf from "@/utils/Avatar.js";
import {MoreFilled} from "@element-plus/icons-vue";
import WebSocketConnector from "@/api/websocket.js";
import router from "@/router/index.js";
import UserDataInterface from "@/data/UserDataInterface.js";
import {ElMessageBox} from "element-plus";

const props = defineProps({
    user: {
        type: Object,
        required: true
    }
});

const deleteUser = () => {
    ElMessageBox.confirm(
            "用户删除后不可恢复",
            "确认删除用户",
            {
                type: "warning",
                draggable: true,
                showClose: false,
                confirmButtonText: "确认删除",
                cancelButtonText: "取消操作"
            },
    ).then(() => {
        if (Number(router.currentRoute.value.params.id) === props.user.qq) {
            router.back();
        }
        WebSocketConnector.send({
            type: "deleteUser",
            qq: props.user.qq
        });
    }).catch(() => {
    });
}
</script>

<template>
    <div class="panel-1"
         style="display: flex;flex-direction: row;padding: 8px 12px;align-items: center;margin-bottom: 2px">
        <el-avatar shape="circle" style="width: 36px;height: 36px;margin-right: 8px;flex: none;"
                   :src="getAvatarUrlOf(user.qq)"/>
        <div style="line-height: 16px;">
            <el-text style="font-size: 16px;align-self: baseline;max-height:21px;text-wrap: wrap">{{
                    user.name
                }}
            </el-text>
            <br/>
            <el-text style="font-size: 12px;align-self: baseline" type="info">{{ user.qq }}</el-text>
        </div>
        <div class="flex-blank-1"></div>
        <el-tag style="font-size: 12px" type="info">{{ user.role }}</el-tag>
        <el-dropdown trigger="click">
            <el-button link @click.stop>
                <el-icon>
                    <more-filled/>
                </el-icon>
            </el-button>
            <template #dropdown>
                <el-dropdown-item>修改用户组</el-dropdown-item>
                <el-dropdown-item v-if="UserDataInterface.getCurrentUser().qq !== user.qq" @click="deleteUser">
                    <el-text type="danger">删除</el-text>
                </el-dropdown-item>
            </template>
        </el-dropdown>
    </div>
</template>
<style scoped>

</style>