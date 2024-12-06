<script setup>
import UserDataInterface from "@/data/UserDataInterface.js";
import getAvatarUrlOf from "@/utils/Avatar.js";
import {Finished} from "@element-plus/icons-vue";
import router from "@/router/index.js";
import Like from "@/components/icons/Like.vue";
import DisLike from "@/components/icons/DisLike.vue";
import HarmonyOSIcon_Quit from "@/components/icons/HarmonyOSIcon_Quit.vue";
import HarmonyOSIcon_Remove from "@/components/icons/HarmonyOSIcon_Remove.vue";
import HarmonyOSIcon_Rename from "@/components/icons/HarmonyOSIcon_Rename.vue";
import WebSocketConnector from "@/api/websocket.js";
import {ElMessage, ElMessageBox} from "element-plus";
import LinkPanel from "@/components/common/LinkPanel.vue";
import CustomDialog from "@/components/common/CustomDialog.vue";
import Waterfall from "@/components/common/Waterfall.vue";

// const user = UserDataInterface.getCurrentUser();

defineExpose({
    name: "Base"
});

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
                            qq: user.qq
                        }).then(() => {
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
    if (done.value) {
        closeDialog();
    } else {
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
                qq: user.qq,
                oldPassword: oldPassword.value,
                newPassword: newPassword.value
            }).then((resp) => {
                if (resp.type === 'fail') {
                    resp.failureType === 'previousPasswordIncorrect' ? changePasswordDialog.value.showTip('原密码错误', "error") : changePasswordDialog.value.showTip('修改失败', "error");
                    // message.value = resp.message;
                } else {
                    changePasswordDialog.value.showTip('修改成功', "success")
                    done.value = true;
                }
                // changePasswordDialogVisible.value = false;
            }, (err) => {
                changePasswordDialog.value.showTip(err.message, "error")
                return false;
            });
        }
    }
}

watch(() => name.value, () => {
    if (name.value === '') {
        changeUserNameDialog.value.showTip("请输入新用户名", "error")
    } else if (name.value === user.name) {
        changeUserNameDialog.value.showTip("新用户名与原用户名相同", "error")
    } else if (name.value.length < 3) {
        changeUserNameDialog.value.showTip("用户名过短", "error")
    } else {
        changeUserNameDialog.value.clearTip();
    }
});

const confirmRename = () => {
    if (done.value) {
        closeDialog();
    } else {
        WebSocketConnector.send({
            type: "changeUserName",
            qq: user.qq,
            newName: name.value
        }).then((resp) => {
            if (resp.type === 'fail') {
                changeUserNameDialog.value.showTip(resp.message, "error")
            } else {
                changeUserNameDialog.value.showTip("修改成功", "success")
                done.value = true;
                user.name = name.value;
            }
            // changeNameDialogVisible.value = false;
        }, (err) => {
            changeUserNameDialog.value.showTip(err.message, "error")
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
    done.value = false;
}

const buttonsOption1 = ref([
    {
        content: "取消",
        type: "info",
        onclick: () => {
            changePasswordDialogVisible.value = false;
        },
        hidden: computed(() => {
            return done.value;
        })
    }, {
        content: computed(() => done.value ? "完成" : "确定"),
        type: "primary",
        onclick: () => {
            if (done.value) {
                done.value = false;
                changePasswordDialogVisible.value = false;
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
        content: "取消",
        type: "info",
        onclick: () => {
            changeNameDialogVisible.value = false;
        },
        hidden: computed(() => {
            return done.value;
        })
    }, {
        content: computed(() => done.value ? "完成" : "确定"),
        type: "primary",
        onclick: () => {
            if (done.value) {
                done.value = false;
                changeNameDialogVisible.value = false;
            } else {
                confirmRename();
            }
        },
        disabled: computed(() => {
            return name.value === '' || name.value === user.name || name.value.length < 3;
        })
    }
]);
</script>

<template>
    <div style="display: flex;flex-direction: column;align-items: center;">
        <div style="max-width: 1280px; width: min(75vw, 1280px); display: flex; flex-direction: column;">
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
            <custom-dialog v-model="changeNameDialogVisible" ref="changeUserNameDialog" @closed="onclose" title="修改用户名"
                           :buttons-option="buttonsOption2">
                <div class="dialog-input-label">
                    <el-text>新用户名</el-text>
                </div>
                <el-input v-model="name" :disabled="done" placeholder=""/>
            </custom-dialog>
            <div style="display: flex;flex-direction: row;margin-bottom: 28px">
                <el-avatar shape="circle" style="width: 84px;height: 84px;margin-right: 8px;"
                           :src="getAvatarUrlOf(user.qq)"/>
                <div style="display:flex;flex-direction: column;justify-content: center;">
                    <el-text style="font-size: 24px;align-self: baseline">{{ user.name }}</el-text>
                    <el-text style="font-size: 16px;align-self: baseline" type="info">{{ user.qq }}</el-text>
                    <el-text style="font-size: 16px;align-self: baseline" type="info">{{ user.role }}</el-text>
                </div>
            </div>
            <!--        <waterfall :data="groups">-->
            <!--            <template #item="{item:group,index}">-->
            <div v-for="group of groups" style="display: flex;flex-direction: column;margin-bottom: 16px;margin-left: 8px;margin-right: 8px">
                <el-text style="font-size: 20px;align-self: baseline;margin-bottom: 8px">{{ group.name }}</el-text>
                <link-panel @click="item.action()" v-for="item of group.items" :key="item.name"
                            :description="item.description" :name="item.name" :icon="item.icon"/>
            </div>
            <!--            </template>-->
            <!--        </waterfall>-->
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