<script setup>
import WebSocketConnector from "@/api/websocket.js";
import {ElMessage, ElMessageBox} from "element-plus";
import HarmonyOSIcon_Plus from "@/components/icons/HarmonyOSIcon_Plus.vue";
import CustomDialog from "@/components/common/CustomDialog.vue";
import UserDataInterface from "@/data/UserDataInterface.js";
import getAvatarUrlOf from "@/utils/Avatar.js";
import randomUUIDv4 from "../../../utils/UUID.js";
import HarmonyOSIcon_Remove from "@/components/icons/HarmonyOSIcon_Remove.vue";
import router from "@/router/index.js";

const editing = ref(false);
const data = ref({
    questionAmount: 0,
    questionScore: 5,
    partitionRange: [0, 100]
});
const loading = ref(true);
const error = ref(false);
let backup = {};
let backupJSON;
const cancel = () => {
    data.value = backup;
    editing.value = false;
}
const startEditing = () => {
    backupJSON = JSON.stringify(data.value)
    backup = JSON.parse(backupJSON);
    editing.value = true;
}
const finishEditing = () => {
    editing.value = false;
    if (backupJSON !== JSON.stringify(data.value)) {
        WebSocketConnector.send({
            type: "saveAdvanceSetting",
            data: data.value
        }).then((resp) => {
            ElMessage({
                type: "success", message: "保存成功"
            });
            data.value.deletedRobotTokenIds = [];
            data.value.createdRobotTokens = [];
            data.value.robotTokenItems = resp.currentTokens;
        }, (err) => {
            ElMessage({
                type: "error", message: "保存失败"
            })
        })
    }
}

const getData = () => {
    loading.value = true;
    error.value = false;
    WebSocketConnector.send({
        type: "getAdvanceSetting",
    }).then((response) => {
        data.value = response.data;
        data.value.deletedRobotTokenIds = [];
        data.value.createdRobotTokens = [];
        if (!data.value.ipSource) {
            data.value.ipSource = "request";
        }
        if (data.value.useRequestIpIfSourceIsNull === null ||
                data.value.useRequestIpIfSourceIsNull === undefined) {
            data.value.useRequestIpIfSourceIsNull = true;
        }
        loading.value = false;
    }, (err) => {
        ElMessage({
            type: "error", message: "获取设置失败"
        });
        loading.value = false;
        error.value = true;
    });
}
getData();

const showCreateNewTokenDialog = ref(false);
const newTokenDescription = ref("");
const onClose = () => {
    showCreateNewTokenDialog.value = false;
    newTokenDescription.value = "";
}
const createTokenButtonOption = ref([{
    content: "确定",
    type: "primary",
    onclick: () => {
        if (!(data.value.robotTokenItems instanceof Array)) {
            data.value.robotTokenItems = [];
        }
        const token = {
            id: randomUUIDv4(),
            token: null,
            description: newTokenDescription.value,
            generateTime: null,
            generateByUserQQ: UserDataInterface.getCurrentUser().value.qq
        };
        data.value.robotTokenItems.push(token);
        data.value.createdRobotTokens.push(token);
        onClose();
    }
}, {
    content: "取消",
    type: "info",
    onclick: onClose
}]);
const createNewToken = () => {
    showCreateNewTokenDialog.value = true;
}

const allUsers = ref({});
UserDataInterface.getUsersAsync().then((users) => {
    allUsers.value = users;
});

const deleteToken = (index) => {
    if (Boolean(data.value.robotTokenItems[index].token)) {
        ElMessageBox.confirm(
                "该 token 将无法再被使用",
                "确定删除 token",
                {
                    showClose: false,
                    draggable: true,
                    confirmButtonText: "确定",
                    cancelButtonText: "取消",
                    type: "warning",
                }
        ).then(() => {
            console.log(data.value.robotTokenItems[index]);
            data.value.deletedRobotTokenIds.push(data.value.robotTokenItems[index].id);
            data.value.robotTokenItems.splice(index, 1);
        }, () => {
        });
    } else {
        data.value.robotTokenItems.splice(index, 1);
    }
}
</script>

<template>
    <div style="display: flex;flex-direction: column;">
        <custom-dialog v-model="showCreateNewTokenDialog"
                       title="新建 token"
                       :buttons-option="createTokenButtonOption">
            <el-text>描述</el-text>
            <el-input style="margin-top: 8px" v-model="newTokenDescription"/>
        </custom-dialog>
        <div style="display: flex;flex-direction: row;margin-bottom: 32px;">
            <el-text style="align-self:baseline;font-size: 24px">高级设置</el-text>
            <div style="display: flex;margin-left: 32px;">
                <transition-group name="blur-scale">
                    <el-button-group key="button-group">
                        <transition-group name="blur-scale">
                            <el-button class="disable-init-animate" style="margin-right: 4px;"
                                       @click="editing ? finishEditing():startEditing()"
                                       :disabled="loading || error" key="edit">
                                {{ editing ? '完成' : '编辑' }}
                            </el-button>
                            <el-button class="disable-init-animate" style="margin-right: 24px;"
                                       @click="cancel" v-if="editing" key="cancel">
                                {{ editing ? '取消' : '编辑' }}
                            </el-button>
                        </transition-group>
                    </el-button-group>
                </transition-group>
            </div>
        </div>
        <el-scrollbar v-loading="loading">
            <div style="display: flex;flex-direction: column;align-items: center">
                <transition name="blur-scale" mode="out-in">
                    <div v-if="!loading && !error"
                         style="max-width: 1280px;width: min(70vw,1280px);display: flex;flex-direction: column;align-items: stretch">
                        <div style="display: flex;flex-direction: row;flex-wrap: wrap;align-items: center;margin-bottom: 8px">
                            <el-text size="large" style="align-self: center;margin-right: 16px">IP 兼容</el-text>
                            <el-radio-group v-model="data.ipSource" :disabled="!editing">
                                <el-radio value="request">使用传入 IP (默认)</el-radio>
                                <el-radio value="x_real_ip">从请求头 "x-real-ip" 解析 IP</el-radio>
                                <el-radio value="x_forwarded_for">从请求头 "x-forwarded-for" 解析 IP</el-radio>
                                <el-radio value="remote_host">从请求头 "remote-host" 解析 IP</el-radio>
                                <el-radio value="cf_connecting_ip">从请求头 "cf-connecting-ip" 解析 IP (Cloudflare)</el-radio>
                                <el-radio value="true_client_ip">从请求头 "true-client-ip" 解析 IP (Cloudflare Enterprise)</el-radio>
                            </el-radio-group>
                        </div>
                        <div style="display: flex;flex-direction: row;flex-wrap: wrap;align-items: center;margin-bottom: 8px">
                            <el-text size="large" style="align-self: center;margin-right: 16px">无法从请求头中获取 IP 时回退至请求</el-text>
                            <el-switch v-model="data.useRequestIpIfSourceIsNull" :disabled="!editing"></el-switch>
                        </div>
                        <div style="display: flex;flex-direction: row;flex-wrap: wrap;align-items: center;margin-bottom: 8px">
                            <el-text size="large" style="align-self: center;margin-right: 16px">robot tokens</el-text>
                            <transition name="blur-scale">
                                <el-button class="disable-init-animate" link @click="createNewToken" v-if="editing">
                                    <HarmonyOSIcon_Plus style="margin-right: 4px;"/>
                                    新建 token
                                </el-button>
                            </transition>
                        </div>
                        <transition name="blur-scale" mode="out-in">
                            <div style="display: flex;flex-direction: column"
                                 v-if="data.robotTokenItems && data.robotTokenItems.length > 0">
                                <transition-group name="smooth-height">
                                    <div class="smooth-height-base"
                                         v-for="(tokenItem,index) of data.robotTokenItems"
                                         :key="tokenItem.id">
                                        <div>
                                            <div style="display: flex;margin-bottom: 8px;flex-direction: column">
                                                <div class="panel-1 disable-init-animate"
                                                     style="padding: 4px 8px;display: flex;align-items: center;margin-bottom: 4px">
                                                    <el-text type="info" style="margin-right: 8px;">token</el-text>
                                                    <transition name="blur-scale" mode="out-in">
                                                        <el-text v-if="tokenItem.generateTime" type="primary"
                                                                 style="margin-left: 8px;margin-right: 8px;word-break: break-all">
                                                            {{ tokenItem.token }}
                                                        </el-text>
                                                        <el-text v-else type="info">等待保存后生成</el-text>
                                                    </transition>
                                                </div>
                                                <div style="display: flex;flex-wrap: wrap">
                                                    <div class="panel-1 disable-init-animate"
                                                         style="min-width: 200px;padding: 4px 8px;display: flex;align-items: center;margin-right: 4px">
                                                        <el-text type="info" style="margin-right: 8px;">生成时间
                                                        </el-text>
                                                        <transition name="blur-scale" mode="out-in">
                                                            <el-text v-if="tokenItem.generateTime"
                                                                     style="margin-left: 8px;margin-right: 8px;">
                                                                {{ tokenItem.generateTime }}
                                                            </el-text>
                                                            <el-text v-else type="info">等待保存后生成</el-text>
                                                        </transition>
                                                    </div>
                                                    <div class="panel-1 disable-init-animate"
                                                         style="min-width: 30px;flex: 1;padding: 4px 8px;display: flex;align-items: center;margin-right: 4px;">
                                                        <el-text type="info" style="margin-right: 8px;">描述</el-text>
                                                        <el-text v-if="tokenItem.description">{{
                                                                tokenItem.description
                                                            }}
                                                        </el-text>
                                                        <el-text v-else type="info">无</el-text>
                                                    </div>
                                                    <div class="panel-1 clickable disable-init-animate"
                                                         style="display: flex;flex-direction: row;padding: 4px 8px;margin-right: 4px;"
                                                         @click="router.push({name: 'user-detail',params: {id: tokenItem.generateByUserQQ}})">
                                                        <el-text type="info" style="margin-right: 8px;">创建用户
                                                        </el-text>
                                                        <el-avatar shape="circle" :size="24" fit="cover"
                                                                   :src="getAvatarUrlOf(tokenItem.generateByUserQQ)"/>
                                                        <el-text v-if="allUsers[tokenItem.generateByUserQQ]"
                                                                 style="margin-right: 4px;margin-left: 8px;align-self: center">
                                                            {{ allUsers[tokenItem.generateByUserQQ].name }}
                                                        </el-text>
                                                        <el-text type="info"
                                                                 style="margin-right: 4px;align-self: center">
                                                            {{ tokenItem.generateByUserQQ }}
                                                        </el-text>
                                                    </div>
                                                    <el-button @click="deleteToken(index)" :disabled="!editing"
                                                               style="height: 34px">
                                                        <HarmonyOSIcon_Remove/>
                                                        删除
                                                    </el-button>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </transition-group>
                            </div>
                            <el-text style="align-self: baseline" type="info" v-else>无数据</el-text>
                        </transition>
                        <!--                        <el-text size="large" class="field-label">自动创建用户</el-text>
                                                <el-radio-group v-model="data.autoCreateUserMode" size="large" style="padding: 4px 20px"
                                                                :disabled="!editing">
                                                    <el-radio value="disabled">禁用</el-radio>
                                                    <el-radio value="afterExam">答题后创建</el-radio>
                                                    <el-radio value="afterQualify">验证后创建</el-radio>
                                                </el-radio-group>
                                                <transition name="blur-scale">
                                                    <div v-if="data.autoCreateUserMode !== 'disabled'"
                                                         style="margin-left: 8px;display: flex;flex-direction: column;align-items: stretch;margin-top: 8px">
                                                        <el-select v-model="data.autoCreatedUserLevel"
                                                                   placeholder="选择答题结果级别"
                                                                   multiple
                                                                   filterable>
                                                            <el-option :value="'test'" :label="'test'"/>
                                                        </el-select>
                                                        <div style="display: flex;flex-direction: row;margin-top: 8px">
                                                            <el-text>创建后启用用户</el-text>
                                                            <el-switch v-model="data.autoCreatedUserEnabled"/>
                                                        </div>
                                                    </div>
                                                </transition>-->
                    </div>
                    <div v-else-if="error" style="display:flex;flex-direction: column">
                        <el-empty description="获取设置失败"></el-empty>
                        <el-button link type="primary" @click="getData" size="large">重试</el-button>
                    </div>
                </transition>
            </div>
        </el-scrollbar>
    </div>
</template>

<style scoped>
.field-label {
    align-self: baseline;
    margin-top: 16px;
}
</style>