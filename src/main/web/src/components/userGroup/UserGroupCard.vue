<script setup>
import {MoreFilled} from "@element-plus/icons-vue";
import WebSocketConnector from "@/api/websocket.js";
import PermissionInfo from "@/auth/PermissionInfo.js";
import {ElMessage, ElMessageBox} from "element-plus";
import CustomDialog from "@/components/common/CustomDialog.vue";
import UserDataInterface from "@/data/UserDataInterface.js";

const props = defineProps({
    userGroup: {
        type: Object,
        required: true
    }
});

const showDeleteRoleUserActions = ref(false);
const doDelete = (showNotification = true) => {
    return WebSocketConnector.send({
        type: "deleteRole",
        data: {
            roleType: props.userGroup.type
        }
    }).then(() => {
        ElMessage({
            type: 'success',
            message: '删除成功',
        });
    }, (err) => {
        if (!showNotification && err.message === "用户组非空") {
            err.disableNotification();
            showDeleteRoleUserActions.value = true;
        }
    });
}
const deleteGroup = () => {
    ElMessageBox.confirm(
            "用户组删除后不可恢复，若用户组中仍然有用户，你可在稍后进行处理",
            "确认删除用户组",
            {
                type: "warning",
                draggable: true,
                showClose: false,
                confirmButtonText: "确认删除",
                cancelButtonText: "取消操作"
            },
    ).then(() => {
        doDelete(false);
    }).catch(() => {
    });
}

const users = ref([]);
const optionType = ref("move");
const targetUserGroupName = ref();

const onClose = () => {
    showDeleteRoleUserActions.value = false;
    delete buttonsOption.value[0].loading;
}

const deleteRoleUserDialog = ref();

const buttonsOption = ref([{
    content: "确定",
    type: "primary",
    disabled: computed(() => optionType.value === 'move' && targetUserGroupName.value === undefined),
    onclick: () => {
        const doAction = () => {
            const qqList = [];
            console.log(users.value);
            for (const userQQ of Object.keys(users.value)) {
                qqList.push(Number(userQQ));
            }
            if (optionType.value === 'delete') {
                WebSocketConnector.send({
                    type: "delete users",
                    data: {
                        qqList: qqList,
                    }
                }).then(() => {
                    doDelete().then(() => {
                        onClose();
                    }, () => {
                        delete buttonsOption.value[0].loading;
                    });
                }, (err) => {
                    delete buttonsOption.value[0].loading;
                    deleteRoleUserDialog.value.showTip(err.message, "error");
                });
            } else {
                WebSocketConnector.send({
                    type: "change users role",
                    data: {
                        qqList: qqList,
                        targetRole: targetUserGroupName.value,
                    }
                }).then(() => {
                    delete buttonsOption.value[0].loading;
                    doDelete().then(() => {
                        onClose();
                    }, () => {
                        delete buttonsOption.value[0].loading;
                    });
                }, (err) => {
                    delete buttonsOption.value[0].loading;
                    deleteRoleUserDialog.value.showTip(err.message, "error");
                });
            }
        }
        buttonsOption.value[0].loading = true;
        if (users.value.length === 0) {
            UserDataInterface.getUsersOfUserGroupSync(props.userGroup.type).then((users1) => {
                users.value = users1;
                doAction();
            });
        } else {
            doAction();
        }
    }
}, {
    content: "取消",
    type: "info",
    onclick: onClose
}]);
</script>

<template>
    <div class="panel-1"
         style="display: flex;flex-direction: row;padding: 8px 12px;align-items: center;margin-bottom: 2px;">
        <custom-dialog title="用户组中仍有用户" v-model="showDeleteRoleUserActions"
                       :buttons-option="buttonsOption" @closed="onClose"
                       ref="deleteRoleUserDialog">
            <div style="min-height: 100px">
                <el-text>对于用户组中用户</el-text>
                <el-select style="margin-top: 8px;" placeholder="选择操作" v-model="optionType">
                    <el-option value="delete" label="删除"
                               :disabled="!PermissionInfo.hasPermission('manage user','delete user')"/>
                    <el-option value="move" label="移动到"
                               :disabled="!PermissionInfo.hasPermission('role','operate role ' + userGroup.type)"/>
                </el-select>
                <transition name="blur-scale">
                    <el-select filterable v-if="optionType === 'move'"
                               v-model="targetUserGroupName"
                               style="margin-top: 8px;" placeholder="用户组">
                        <template v-for="(userGroup1,i) in UserDataInterface.roles">
                            <el-option v-if="userGroup1.type !== userGroup.type"
                                       :disabled="!PermissionInfo.hasPermission('role','operate role ' + userGroup1.type)"
                                       :value="userGroup1.type" :label="userGroup1.type"></el-option>
                        </template>
                    </el-select>
                </transition>
            </div>
        </custom-dialog>
        <el-text style="font-size: 16px;align-self: baseline;overflow: hidden;text-wrap: wrap;word-break: break-all;">{{
                userGroup.type
            }}
        </el-text>
        <div class="flex-blank-1"></div>
        <el-dropdown trigger="click" v-if="PermissionInfo.hasPermission('role','delete role')">
            <el-button link @click.stop class="disable-init-animate">
                <el-icon>
                    <more-filled/>
                </el-icon>
            </el-button>
            <template #dropdown>
                <el-dropdown-item @click="deleteGroup">
                    <el-text type="danger">删除</el-text>
                </el-dropdown-item>
            </template>
        </el-dropdown>
    </div>
</template>

<style scoped>

</style>