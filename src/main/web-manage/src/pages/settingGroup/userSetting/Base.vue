<script setup>
import UserDataInterface from "@/data/UserDataInterface.js";
import getAvatarUrlOf from "@/utils/Avatar.js";
import {ArrowRightBold, Finished} from "@element-plus/icons-vue";
import router from "@/router/index.js";
import Like from "@/components/icons/Like.vue";
import DisLike from "@/components/icons/DisLike.vue";
import HarmonyOSIcon_Quit from "@/components/icons/HarmonyOSIcon_Quit.vue";
import HarmonyOSIcon_Remove from "@/components/icons/HarmonyOSIcon_Remove.vue";
import HarmonyOSIcon_Rename from "@/components/icons/HarmonyOSIcon_Rename.vue";
import WebSocketConnector from "@/api/websocket.js";
import {ElMessage, ElMessageBox} from "element-plus";
import LinkPanel from "@/components/common/LinkPanel.vue";

// const user = UserDataInterface.getCurrentUser();

defineExpose({
    name: "Base"
});

function cancelDelete() {
    ElMessage({
        type: 'info',
        message: 'Delete canceled',
    })
}

const groups = [
    {
        name: "我的",
        items: [
            {
                name: "我的题目", description: "", icon: Finished, action: () => {
                    router.push({
                        name: "my-data",
                        query: {tab: "my-questions"}
                    })
                }
            },
            {
                name: "我点赞的题目", description: "", icon: Like, action: () => {
                    router.push({
                        name: "my-data",
                        query: {tab: "my-like-questions"}
                    })
                }
            },
            {
                name: "我点踩的题目", description: "", icon: DisLike, action: () => {
                    router.push({
                        name: "my-data",
                        query: {tab: "my-dislike-questions"}
                    })
                }
            },
        ]
    },
    {
        name: "操作",
        items: [
            {
                name: "修改用户名",
                description: "",
                icon: HarmonyOSIcon_Rename,
                action: () => {
                    name.value = user.name;
                    changeNameDialogVisible.value = true;
                }
            },
            {
                name: "修改密码",
                description: "",
                icon: HarmonyOSIcon_Rename,
                action: () => {
                    changePasswordDialogVisible.value = true;
                }
            },
            {name: "退出", description: "", icon: HarmonyOSIcon_Quit, action: UserDataInterface.logout},
            {
                name: "删除账户", description: "", icon: HarmonyOSIcon_Remove, action: () => {
                    ElMessageBox.confirm(
                        '确认删除账户？',
                        '高危操作警告',
                        {
                            distinguishCancelAndClose: true,
                            confirmButtonText: '取消操作',
                            cancelButtonText: '确认删除',
                            type: 'warning',
                        }
                    ).then(() => {
                        cancelDelete();
                    }).catch((action) => {
                        if (action === "cancel") {
                            ElMessage({
                                type: 'success',
                                message: 'Delete completed',
                            });
                        } else if (action === "close") {
                            cancelDelete();
                        }
                    })
                }
            },
        ]
    }
]

const user = UserDataInterface.getCurrentUser();
const changePasswordDialogVisible = ref(false);
const changeNameDialogVisible = ref(false);

const newPassword = ref("");
const oldPassword = ref("");
const newPasswordRepeat = ref('');
const name = ref("");
const message = ref("");
const messageKey = ref(true);
watch(() => message.value, () => {
    messageKey.value = !messageKey.value;
})
const done = ref(false);

const confirmChangePassword = () => {
    if (done.value) {
        closeDialog();
    } else {
        if (oldPassword.value === '') {
            message.value = "请输入原密码";
            return;
        } else if (newPassword.value !== newPasswordRepeat.value) {
            message.value = "两次输入的密码不一致";
            return;
        } else if (newPassword.value.length < 5) {
            message.value = "密码过短";
            return;
        } else if (newPassword.value === oldPassword.value) {
            message.value = "新密码与旧密码一致";
        }
        WebSocketConnector.send({
            type: "changeUserPassword",
            QQ: user.qq,
            oldPassword: oldPassword.value,
            newPassword: newPassword.value
        }).then((resp) => {
            if (resp.type === 'fail') {
                resp.failureType === 'previousPasswordIncorrect' ? message.value = '原密码错误' : message.value = '修改失败'
                // message.value = resp.message;
            } else {
                message.value = "修改成功";
                done.value = true;
            }
            // changePasswordDialogVisible.value = false;
        }, (err) => {
            message.value = err.message;
            return false;
        });
    }
}

const confirmRename = () => {
    if (done.value) {
        closeDialog();
    } else {
        if (name.value === '') {
            message.value = "请输入新用户名";
            return;
        } else if (name.value === user.name) {
            message.value = "新用户名与原用户名相同";
            return;
        } else if (name.value.length < 3) {
            message.value = "用户名过短";
            return;
        }
        WebSocketConnector.send({
            type: "changeUserName",
            QQ: user.qq,
            newName: name.value
        }).then((resp) => {
            if (resp.type === 'fail') {
                message.value = resp.message;
            } else {
                message.value = "修改成功";
                done.value = true;
                user.name = name.value;
            }
            // changeNameDialogVisible.value = false;
        }, (err) => {
            message.value = err.message;
            return false;
        });
    }
}

const closeDialog = () => {
    changeNameDialogVisible.value = false;
    changePasswordDialogVisible.value = false;
    oldPassword.value = "";
    newPassword.value = "";
    newPasswordRepeat.value = "";
}

const onclose = () => {
    setTimeout(() => {
        done.value = false;
        message.value = "";
    }, 400);
}
</script>

<template>
    <div>
        <el-dialog v-model="changePasswordDialogVisible" width="400" align-center
                   @close="onclose">
            <template #header>
                <el-text style="font-size: 18px">修改密码</el-text>
            </template>
            <template #default>
                <el-input v-model="oldPassword" :disabled="done" type="password" placeholder="原密码"/>
                <el-input v-model="newPassword" :disabled="done" type="password" placeholder="新密码"/>
                <el-input v-model="newPasswordRepeat" :disabled="done" type="password" placeholder="重复新密码"/>
            </template>
            <template #footer>
                <div style="display: flex;justify-content: end">
                    <Transition name="message" mode="out-in">
                        <el-text :key="messageKey" style="margin-right: 16px">{{ message }}</el-text>
                    </Transition>
                    <el-button @click="closeDialog" v-if="!done">取消
                    </el-button>
                    <el-button type="primary" @click="confirmChangePassword">{{ done ? "完成" : "确定" }}</el-button>
                </div>
            </template>
        </el-dialog>
        <el-dialog v-model="changeNameDialogVisible" width="400" align-center @close="onclose">
            <template #header>
                <el-text style="font-size: 18px">修改用户名</el-text>
            </template>
            <template #default>
                <el-input v-model="name" :disabled="done" placeholder="新用户名"/>
            </template>
            <template #footer>
                <div style="display: flex;justify-content: end">
                    <Transition name="message" mode="out-in">
                        <el-text :key="messageKey" style="margin-right: 16px">{{ message }}</el-text>
                    </Transition>
                    <el-button @click="closeDialog" v-if="!done">取消</el-button>
                    <el-button type="primary" @click="confirmRename">{{ done ? "完成" : "确定" }}</el-button>
                </div>
            </template>
        </el-dialog>
        <div style="display: flex;flex-direction: row;margin-bottom: 28px">
            <el-avatar shape="circle" style="width: 84px;height: 84px;margin-right: 8px;"
                       :src="getAvatarUrlOf(user.qq)"/>
            <div style="display:flex;flex-direction: column;justify-content: center;">
                <el-text style="font-size: 24px;align-self: baseline">{{ user.name }}</el-text>
                <el-text style="font-size: 16px;align-self: baseline" type="info">{{ user.qq }}</el-text>
                <el-text style="font-size: 16px;align-self: baseline" type="info">{{ user.role }}</el-text>
            </div>
        </div>
        <div v-for="group of groups" style="display: flex;flex-direction: column;margin-bottom: 16px">
            <el-text style="font-size: 20px;align-self: baseline;margin-bottom: 8px">{{ group.name }}</el-text>
            <link-panel @click="item.action()" v-for="item of group.items" :key="item.name" :description="item.description" :name="item.name" :icon="item.icon"/>
        </div>
    </div>
</template>

<style scoped>

/*noinspection CssUnusedSymbol*/
.message-enter-active, .message-leave-active {
    transition: all 0.2s;
}

/*noinspection CssUnusedSymbol*/
.message-enter-from, .message-leave-to {
    filter: blur(8px);
    opacity: 0;
}

</style>