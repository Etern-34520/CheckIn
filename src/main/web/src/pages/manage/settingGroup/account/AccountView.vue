<script setup>
import UserDataInterface from "@/data/UserDataInterface.js";
import getAvatarUrlOf from "@/utils/Avatar.js";
import {Finished, Link, MessageBox} from "@element-plus/icons-vue";
import router from "@/router/index.js";
import Like from "@/components/icons/Like.vue";
import DisLike from "@/components/icons/DisLike.vue";
import HarmonyOSIcon_Quit from "@/components/icons/HarmonyOSIcon_Quit.vue";
import HarmonyOSIcon_Remove from "@/components/icons/HarmonyOSIcon_Remove.vue";
import HarmonyOSIcon_Rename from "@/components/icons/HarmonyOSIcon_Rename.vue";
import WebSocketConnector from "@/api/websocket.js";
import {ElMessageBox} from "element-plus";
import LinkPanel from "@/components/common/LinkPanel.vue";
import CustomDialog from "@/components/common/CustomDialog.vue";

defineExpose({
    name: "Base"
});

const groups = [
    {
        name: "我的",
        items: [
            {
                name: "我的答题记录", description: "", icon: MessageBox, action: () => {
                    router.push({
                        name: "my-data",
                        query: {tab: "my-exam-records"}
                    })
                }
            },
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
        name: "账户",
        items: [
            {
                name: "第三方关联登录",
                description: "",
                icon: Link,
                action: () => {
                    router.push({name: "oauth2-binding"})
                }
            },
            {
                name: "修改用户名",
                description: "",
                icon: HarmonyOSIcon_Rename,
                action: () => {
                    name.value = user.value.name;
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
                            '你将无法恢复账户',
                            '确认删除账户',
                            {
                                showClose: false,
                                draggable: true,
                                confirmButtonText: '确认删除',
                                cancelButtonText: '取消操作',
                                type: 'warning',
                            }
                    ).then(() => {
                        WebSocketConnector.send({
                            type: "deleteUser",
                            data: {
                                qq: user.value.qq
                            }
                        }).then((response) => {
                            console.log(response.data);
                            UserDataInterface.logout();
                        });
                    }).catch(() => {
                    });
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
const changePasswordDialog = ref(null);
const changeUserNameDialog = ref(null);
// const message = ref({});
// const messageKey = ref(true);
// watch(() => message.value, () => {
//     messageKey.value = !messageKey.value;
// })
const done = ref(false);

const confirmChangePassword = () => {
    if (oldPassword.value === '') {
        changePasswordDialog.value.showTip("请输入原密码", "error")
    } else if (newPassword.value !== newPasswordRepeat.value) {
        changePasswordDialog.value.showTip("两次输入的密码不一致", "error")
    } else if (newPassword.value.length < 5) {
        changePasswordDialog.value.showTip("密码过短", "error")
    } else if (newPassword.value === oldPassword.value) {
        changePasswordDialog.value.showTip("新密码与旧密码一致", "error")
    } else {
        WebSocketConnector.send({
            type: "changeUserPassword",
            data: {
                oldPassword: oldPassword.value,
                newPassword: newPassword.value
            }
        }).then(() => {
            changePasswordDialog.value.showTip('修改成功', "success")
            done.value = true;
        }, (err) => {
            err.disableNotification();
            if (resp.message === 'Previous password incorrect') {
                changePasswordDialog.value.showTip('原密码错误', "error");
            } else {
                changePasswordDialog.value.showTip('修改失败: ' + err.message, "error");
            }
            return false;
        });
    }
}

watch(() => name.value, () => {
    if (name.value === '') {
        changeUserNameDialog.value.showTip("请输入新用户名", "error")
    } else if (name.value === user.value.name) {
        changeUserNameDialog.value.showTip("新用户名与原用户名相同", "error")
    } else if (name.value.length < 3) {
        changeUserNameDialog.value.showTip("用户名过短", "error")
    } else {
        changeUserNameDialog.value.clearTip();
    }
});

const confirmRename = () => {
    WebSocketConnector.send({
        type: "changeUserName",
        data: {
            qq: user.value.qq,
            newName: name.value
        }
    }).then(() => {
        changeUserNameDialog.value.showTip("修改成功", "success")
        done.value = true;
        user.value.name = name.value;
    }, (err) => {
        changeUserNameDialog.value.showTip(err.data.message, "error")
        return false;
    });
}

const closeDialog = () => {
    changeNameDialogVisible.value = false;
    changePasswordDialogVisible.value = false;
    oldPassword.value = "";
    newPassword.value = "";
    newPasswordRepeat.value = "";
}

const onclose = () => {
    done.value = false;
}

const buttonsOption1 = ref([
    {
        id: "cancel",
        content: "取消",
        type: "info",
        onclick: () => {
            changePasswordDialogVisible.value = false;
            closeDialog();
        },
        hidden: computed(() => {
            return done.value;
        })
    }, {
        id: "confirm",
        content: computed(() => done.value ? "完成" : "确定"),
        type: "primary",
        onclick: () => {
            if (done.value) {
                closeDialog();
            } else {
                confirmChangePassword();
            }
        },
        disabled: computed(() => {
            return !(oldPassword.value && newPassword.value && (newPassword.value === newPasswordRepeat.value));
        })
    }
]);

const buttonsOption2 = ref([
    {
        id: "cancel",
        content: "取消",
        type: "info",
        onclick: () => {
            changeNameDialogVisible.value = false;
            closeDialog();
        },
        hidden: computed(() => {
            return done.value;
        })
    }, {
        id: "confirm",
        content: computed(() => done.value ? "完成" : "确定"),
        type: "primary",
        onclick: () => {
            if (done.value) {
                closeDialog();
            } else {
                confirmRename();
            }
        },
        disabled: computed(() => {
            return !done.value && (name.value === '' || name.value === user.value.name || name.value.length < 3);
        })
    }
]);
</script>

<template>
    <div>
        <el-scrollbar style="flex: 1">
            <div style="display: flex;flex-direction: column;align-items: center;width: calc(100% - 4px)">
                <div style="max-width: 1080px; width: min(95%, 1080px); display: flex; flex-direction: column;">
                    <custom-dialog v-model="changePasswordDialogVisible" ref="changePasswordDialog" @closed="onclose"
                                   title="修改密码" :buttons-option="buttonsOption1">
                        <div class="dialog-input-label">
                            <el-text>原密码</el-text>
                        </div>
                        <el-input v-model="oldPassword" :disabled="done" type="password" placeholder=""/>
                        <div class="dialog-input-label">
                            <el-text>新密码</el-text>
                        </div>
                        <el-input v-model="newPassword" :disabled="done" type="password" placeholder=""/>
                        <div class="dialog-input-label">
                            <el-text>重复新密码</el-text>
                        </div>
                        <el-input v-model="newPasswordRepeat" :disabled="done" type="password" placeholder=""/>
                    </custom-dialog>
                    <custom-dialog v-model="changeNameDialogVisible" ref="changeUserNameDialog" @closed="onclose"
                                   title="修改用户名"
                                   :buttons-option="buttonsOption2">
                        <div class="dialog-input-label">
                            <el-text>新用户名</el-text>
                        </div>
                        <el-input v-model="name" :disabled="done" placeholder=""/>
                    </custom-dialog>
                    <div style="display: flex;flex-direction: row;margin-bottom: 28px">
                        <el-avatar :size="84" shape="circle" style="margin-right: 16px;"
                                   :src="getAvatarUrlOf(user.qq)"/>
                        <div style="display:flex;flex-direction: column;justify-content: center;">
                            <el-text style="font-size: 24px;align-self: baseline;word-break: break-all">{{ user.name }}</el-text>
                            <el-text style="font-size: 16px;align-self: baseline" type="info">{{ user.qq }}</el-text>
                            <el-text style="font-size: 16px;align-self: baseline" type="info">{{ user.role }}</el-text>
                        </div>
                    </div>
                    <div v-for="group of groups"
                         style="display: flex;flex-direction: column;margin-bottom: 16px;margin-left: 8px;margin-right: 8px">
                        <el-text style="font-size: 20px;align-self: baseline;margin-bottom: 8px">{{
                                group.name
                            }}
                        </el-text>
                        <link-panel @click="item.action()" v-for="item of group.items" :key="item.name"
                                    :description="item.description" :name="item.name" :icon="item.icon"/>
                    </div>
                </div>
            </div>
        </el-scrollbar>
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