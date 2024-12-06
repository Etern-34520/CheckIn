<script setup>
import {useRoute} from "vue-router";
import UserDataInterface from "@/data/UserDataInterface.js";
import getAvatarUrlOf from "@/utils/Avatar.js";
import WebSocketConnector from "@/api/websocket.js";
import LinkPanel from "@/components/common/LinkPanel.vue";
import {Finished} from "@element-plus/icons-vue";
import QuestionInfoPanel from "@/components/question/QuestionInfoPanel.vue";
import QuestionCache from "@/data/QuestionCache.js";
import Waterfall from "@/components/common/Waterfall.vue";
import router from "@/router/index.js";
import HarmonyOSIcon_Rename from "@/components/icons/HarmonyOSIcon_Rename.vue";
import {ElMessage, ElMessageBox} from "element-plus";

const route = useRoute();
const user = ref(undefined);
const loading = ref(true);
const questionLoading = ref(true);
const transitionCaller = ref(false);
const questions = ref([]);

defineExpose({
    name: "Base"
});

const update = (newVal, oldVal) => {
    let questionLoaded = false;
    UserDataInterface.getUserOfQQAsync(route.params.id).then((resp) => {
        user.value = resp;
        loading.value = false;
        transitionCaller.value = !transitionCaller.value;
        if (!questionLoaded)
            questionLoading.value = true;
    });
    WebSocketConnector.send({
        type: "getQuestionsByUserQQ",
        qq: Number(route.params.id),
        limit: 6
    }).then((respData) => {
        questionLoaded = true;
        const questionInfos = [];
        for (const question of respData.questions) {
            questionInfos.push(QuestionCache.wrapToQuestionInfo(question));
        }
        questions.value = questionInfos;
        questionLoading.value = false;
    }, (err) => {
        questionLoading.value = false;
    });
}

watch(() => route.params.id, update);

onMounted(() => {
    update(route.params.id, null);
});

const newName = ref("");
const confirmRename = () => {
    WebSocketConnector.send({
        type: "changeUserName",
        qq: user.value.qq,
        newName: newName.value
    }).then(() => {
        ElMessage({
            type: 'success',
            message: '修改成功',
        })
    }, () => {
        ElMessage({
            type: 'error',
            message: '修改失败',
        })
    });
}

const newUserGroupName = ref("");
const moveGroupVisible = ref(false);
const confirmMoveUserGroup = () => {
    moveGroupVisible.value = false;
    WebSocketConnector.send({
        type: "changeUserRole",
        qq: user.value.qq,
        roleType: newUserGroupName.value
    }).then(() => {
        ElMessage({
            type: 'success',
            message: '修改成功',
        })
    }, () => {
        ElMessage({
            type: 'error',
            message: '修改失败',
        })
    });
}

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
        router.back();
        WebSocketConnector.send({
            type: "deleteUser",
            qq: Number(route.params.id)
        });
    }).catch(() => {
    });
}

const switching = ref(false);
const switchEnableUser = () => {
    switching.value = true;
    return new Promise ((resolve, reject) => {
        WebSocketConnector.send({
            type: "setEnableUser",
            qq: user.value.qq,
            enable: !user.value.enabled
        }).then(() => {
            ElMessage({
                type: 'success',
                message: '修改成功',
            });
            switching.value = false;
            resolve();
        }, () => {
            ElMessage({
                type: 'error',
                message: '修改失败',
            });
            reject();
        });
    });
}
</script>

<template>
    <div v-loading="loading" style="height: 100%;">
        <transition name="blur-scale" mode="out-in">
            <div :key="transitionCaller" v-if="user">
                <div style="display: flex;flex-direction: row;flex-wrap: wrap;">
                    <div style="display: flex;flex-direction: row;margin-bottom: 28px">
                        <el-avatar shape="circle" style="width: 84px;height: 84px;margin-right: 8px;"
                                   :src="getAvatarUrlOf(user.qq)"/>
                        <div style="display:flex;flex-direction: column;justify-content: center;">
                            <el-text style="font-size: 24px;align-self: baseline">{{ user.name }}</el-text>
                            <el-text style="font-size: 16px;align-self: baseline" type="info">{{ user.qq }}</el-text>
                            <el-text style="font-size: 16px;align-self: baseline" type="info">{{ user.role }}</el-text>
                        </div>
                    </div>
                    <div class="flex-blank-1"></div>
                    <div style="display: flex;flex-direction: row;flex-wrap: wrap;margin-bottom: 8px">
                        <el-popover trigger="click" :width="400" @before-enter="newName = user.name">
                            <template #reference>
                                <!--suppress JSValidateTypes -->
                                <el-button :icon="HarmonyOSIcon_Rename" style="margin-right: 0">
                                    修改名称
                                </el-button>
                            </template>
                            <template #default>
                                <div style="display: flex;flex-direction: row">
                                    <el-input
                                            v-model="newName"
                                            style="flex:1;margin-right: 4px"
                                            placeholder="用户名称"
                                    />
                                    <el-button-group>
                                        <el-button type="primary" @click="confirmRename"
                                                   :disabled="newName===''">
                                            确定
                                        </el-button>
                                    </el-button-group>
                                </div>
                            </template>
                        </el-popover>
                        <el-popover :visible="moveGroupVisible" v-if="UserDataInterface.currentUser.qq!==user.qq"
                                    :width="400" @before-enter="newUserGroupName = user.role">
                            <template #reference>
                                <el-button @click="moveGroupVisible = !moveGroupVisible">修改用户组</el-button>
                            </template>
                            <template #default>
                                <div style="display: flex;flex-direction: row;align-items: end">
                                    <el-select
                                            filterable
                                            v-model="newUserGroupName"
                                            style="flex:1;margin-right: 4px"
                                            placeholder="用户组">
                                        <template v-for="(userGroup,i) in UserDataInterface.userGroups">
                                            <el-option :value="userGroup.type" :label="userGroup.type"></el-option>
                                        </template>
                                    </el-select>
                                    <el-button-group>
                                        <el-button type="primary" @click="confirmMoveUserGroup"
                                                   :disabled="newUserGroupName===user.role">
                                            确定
                                        </el-button>
                                    </el-button-group>
                                </div>
                            </template>
                        </el-popover>
                        <el-button v-if="UserDataInterface.currentUser.qq!==user.qq">
                            <el-text type="danger" @click="deleteUser">删除</el-text>
                        </el-button>
                        <!--                        <el-button>修改特有权限</el-button>-->
                        <!--                        TODO-->
                        <div style="margin-left: 12px" v-if="UserDataInterface.currentUser.qq!==user.qq">
                            <el-text style="line-height: 20px;margin-right: 4px">启用</el-text>
                            <el-switch v-model="user.enabled" :loading="switching" :before-change="switchEnableUser"/>
                        </div>
                    </div>
                </div>
                <link-panel description="" name="TA的更多题目" :icon="Finished"
                            @click="router.push({name: 'user-questions',params: {id: user.qq}})"/>
                <div class="panel-1" v-loading="questionLoading">
                    <waterfall :data="questions" :min-row-width="400" style="padding: 8px 12px">
                        <template #item="{item,index}">
                            <question-info-panel :question-info="item"
                                                 @click="router.push({name:'question-detail',params: {id:item.question.id}})"/>
                        </template>
                    </waterfall>
                    <!--                    <question-info-panel v-for="questionInfo of questions" :question-info="QuestionCache.wrapToQuestionInfo(questionInfo)"/>-->
                </div>
                <link-panel description="" name="TA的答题记录" :icon="Finished"/>
            </div>
        </transition>
    </div>
</template>

<style scoped></style>