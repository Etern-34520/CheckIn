<script setup>
import getAvatarUrlOf from "@/utils/Avatar.js";
import {MoreFilled} from "@element-plus/icons-vue";
import WebSocketConnector from "@/api/websocket.js";
import router from "@/router/index.js";
import UserDataInterface from "@/data/UserDataInterface.js";
import {ElMessage, ElMessageBox} from "element-plus";
import CustomDialog from "@/components/common/CustomDialog.vue";
import PermissionInfo from "@/auth/PermissionInfo.js";

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
            data: {
                qq: props.user.qq
            }
        });
    }).catch(() => {
    });
}

const showEditUserGroup = ref(false);
const showEditUserName = ref(false);
const newUserGroupName = ref(props.user.role);
const newUserName = ref(props.user.name);

const onClose = () => {
    showEditUserGroup.value = false;
    showEditUserName.value = false;
    delete editGroupButtonOption.value[0].loading;
    delete editNameButtonOption.value[0].loading;
}
const editGroupButtonOption = ref([{
    content: "确定",
    type: "primary",
    onclick: () => {
        editGroupButtonOption.value[0].loading = true;
        WebSocketConnector.send({
            type: "changeUserRole",
            data: {
                qq: props.user.qq,
                roleType: newUserGroupName.value
            }
        }).then(() => {
            showEditUserGroup.value = false;
            ElMessage({
                type: 'success',
                message: '修改成功',
            });
            delete editGroupButtonOption.value[0].loading;
        }, () => {
            ElMessage({
                type: 'error',
                message: '修改失败',
            });
            delete editGroupButtonOption.value[0].loading;
        });
    }
}, {
    content: "取消",
    type: "info",
    onclick: onClose
}]);
const editNameButtonOption = ref([{
    content: "确定",
    type: "primary",
    onclick: () => {
        editNameButtonOption.value[0].loading = true;
        WebSocketConnector.send({
            type: "changeUserName",
            data: {
                qq: props.user.qq,
                newName: newUserName.value
            }
        }).then(() => {
            showEditUserName.value = false;
            ElMessage({
                type: 'success',
                message: '修改成功',
            });
            delete editNameButtonOption.value[0].loading;
        }, () => {
            ElMessage({
                type: 'error',
                message: '修改失败',
            });
            delete editNameButtonOption.value[0].loading;
        });
    }
}, {
    content: "关闭",
    type: "info",
    onclick: onClose
}]);
const editUserGroup = () => {
    showEditUserGroup.value = true;
}
const editUserName = () => {
    showEditUserName.value = true;
}
const currentUser = UserDataInterface.getCurrentUser();

const showDropdown = (user) => {
    return (currentUser.value.qq !== user.qq && PermissionInfo.hasPermission('manage user', 'delete user')) ||
            (currentUser.value.qq !== user.qq && PermissionInfo.hasPermission('role', /^operate role /)) ||
            (currentUser.value.qq === user.qq || PermissionInfo.hasPermission('manage user', 'change user name'));
}
</script>

<template>
    <div class="panel-1"
         style="display: flex;flex-direction: row;padding: 8px 12px;align-items: center;margin-bottom: 2px">
        <custom-dialog v-model="showEditUserGroup" title="修改用户组" :buttons-option="editGroupButtonOption"
                       @close="onClose">
            <div style="display: flex;align-items: center;margin-bottom: 16px;">
                <el-avatar shape="circle" :size="36" style="margin-right: 8px;flex: none;"
                           :src="getAvatarUrlOf(user.qq)"/>
                <div style="line-height: 16px;">
                    <el-text style="font-size: 16px;align-self: baseline;max-height:21px;text-wrap: wrap;word-break: break-all;">{{
                            user.name
                        }}
                    </el-text>
                    <br/>
                    <el-text style="font-size: 12px;align-self: baseline;text-wrap: wrap;word-break: break-all" type="info">{{ user.qq }}</el-text>
                </div>
            </div>
            <el-select filterable v-model="newUserGroupName"
                       style="flex:1;margin-right: 4px" placeholder="用户组">
                <template v-for="(userGroup,i) in UserDataInterface.userGroups">
                    <el-option :disabled="!PermissionInfo.hasPermission('role','operate role ' + userGroup.type)"
                               :value="userGroup.type" :label="userGroup.type"></el-option>
                </template>
            </el-select>
        </custom-dialog>
        <custom-dialog v-model="showEditUserName" title="修改用户名" :buttons-option="editNameButtonOption"
                       @close="onClose">
            <div style="display: flex;align-items: center;margin-bottom: 16px;">
                <el-avatar shape="circle" :size="36" style=";margin-right: 8px;flex: none;"
                           :src="getAvatarUrlOf(user.qq)"/>
                <div style="line-height: 16px;">
                    <el-text style="font-size: 16px;align-self: baseline;max-height:21px;text-wrap: wrap">{{
                            user.name
                        }}
                    </el-text>
                    <br/>
                    <el-text style="font-size: 12px;align-self: baseline" type="info">{{ user.qq }}</el-text>
                </div>
            </div>
            <el-input
                    v-model="newUserName"
                    style="flex:1;margin-right: 4px"
                    placeholder="用户名称"
            />
        </custom-dialog>
        <el-avatar shape="circle" :size="36" style="margin-right: 8px;flex: none;"
                   :src="getAvatarUrlOf(user.qq)"/>
        <div style="line-height: 16px;">
            <el-text style="font-size: 16px;align-self: baseline;max-height:21px;text-wrap: wrap;word-break: break-all;">{{
                    user.name
                }}
            </el-text>
            <br/>
            <el-text style="font-size: 12px;align-self: baseline;text-wrap: wrap;word-break: break-all;" type="info">{{ user.qq }}</el-text>
        </div>
        <div class="flex-blank-1"></div>
        <el-tag style="font-size: 12px" type="info">{{ user.role }}</el-tag>
        <el-dropdown trigger="click" v-if="showDropdown(user)">
            <el-button link @click.stop>
                <el-icon>
                    <more-filled/>
                </el-icon>
            </el-button>
            <template #dropdown>
                <el-dropdown-item @click="editUserName"
                                  v-if="currentUser.qq === user.qq || PermissionInfo.hasPermission('manage user','change user name')">
                    修改用户名
                </el-dropdown-item>
                <el-dropdown-item @click="editUserGroup"
                                  v-if="currentUser.qq !== user.qq && PermissionInfo.hasPermission('role','operate role ' + user.role)">
                    修改用户组
                </el-dropdown-item>
                <el-dropdown-item
                        v-if="currentUser.qq !== user.qq && PermissionInfo.hasPermission('manage user','delete user')"
                        @click="deleteUser">
                    <el-text type="danger">删除</el-text>
                </el-dropdown-item>
            </template>
        </el-dropdown>
    </div>
</template>
<style scoped>

</style>