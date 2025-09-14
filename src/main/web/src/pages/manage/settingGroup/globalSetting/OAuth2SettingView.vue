<script setup lang="ts">
import HarmonyOSIcon_Plus from "@/components/icons/HarmonyOSIcon_Plus.vue";
import {onUnmounted, ref, watch} from "vue";
import { uuidv7 } from "uuidv7";
import {VueDraggable} from "vue-draggable-plus";
import WebSocketConnector from "@/api/websocket.js";
import {ElMessage} from "element-plus";

const editing = ref(false);
const loading = ref(true);
const error = ref(false);
const data = ref({});
const inputErrors = ref([]);
let backUp;
let backupJSON;
const loadData = () => {
    loading.value = true;
    WebSocketConnector.send({
        type: "getOAuth2Setting"
    }).then(
        (response) => {
            error.value = false;
            loading.value = false;
            data.value = response.data.data;
            inputErrors.value = [];
        },
        (err) => {
            error.value = true;
            loading.value = false;
        }
    )
}
const unwatch = watch(() => [data.value.providers?.length, data.value.appDomainURI],
    () => {
        inputErrors.value[0] = data.value.providers?.length > 0 && !data.value.appDomainURI;
    }, {
        deep: true
    }
);
onUnmounted(unwatch);
loadData();
const cancel = () => {
    data.value = backUp;
    editing.value = false;
};
const startEditing = () => {
    editing.value = true;
    backupJSON = JSON.stringify(data.value);
    backUp = JSON.parse(backupJSON);
}
const finishEditing = () => {
    editing.value = false;
    if (backupJSON !== JSON.stringify(data.value)) {
        WebSocketConnector.send({
            type: "saveOAuth2Setting",
            data: {
                data: data.value
            }
        }).then(() => {
            ElMessage({
                type: "success", message: "保存成功"
            })
        }, (err) => {
            ElMessage({
                type: "error", message: "保存失败"
            })
        });
    }
    //TODO
}
const addProvider = () => {
    data.value.providers.push(
        {
            id: uuidv7(),
            name: "",
            iconDomain: "",
            authorizationUri: "",
            tokenUri: "",
            userInfoUri: "",
            clientId: "",
            clientSecret: "",
            scope: [],
            userNameAttribute: "",
            enabled: false,
            otherAttributes: []
        }
    )
}
const removeProvider = (index) => {
    data.value.providers.splice(index, 1);
    inputErrors.value.splice(index + 1, 1);
}
</script>

<template>
    <div style="display: flex;flex-direction: column;">
        <div style="display: flex;flex-direction: row;margin-bottom: 32px;">
            <el-text style="align-self:baseline;font-size: 24px">OAuth2 设置</el-text>
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
                                取消
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
                         style="max-width: 1280px;width: min(95%,1280px);display: flex;flex-direction: column;align-items: stretch">
                        <div style="display: flex;flex-direction: column;align-items: start;margin-bottom: 12px">
                            <div style="margin-bottom: 8px;">
                                <el-text size="large" style="align-self: start;margin-right: 16px">当前根域名 URL
                                </el-text>
                                <el-text type="info" style="align-self: start;">用于OAuth2回调</el-text>
                            </div>
                            <el-input type="text" style="max-width: 400px;margin-bottom: 4px;"
                                      :class="{error: inputErrors[0]}"
                                      class="disable-init-animate"
                                      placeholder="如 https://test.domain.com"
                                      v-model="data.appDomainURI" :disabled="!editing"></el-input>
                            <div style="align-self: start;margin-bottom: 24px;height: 40px">
                                <transition name="blur-scale" mode="out-in">
                                    <el-text v-if="data.appDomainURI" type="info">
                                        可能需要在提供商后台配置回调地址为 <br/>
                                        {{ data.appDomainURI }}/checkIn/api/oauth2/code
                                    </el-text>
                                </transition>
                            </div>
                            <div style="margin-bottom: 8px;display: flex;flex-direction: row">
                                <el-text size="large" style="margin-right: 24px">
                                    OAuth2 提供商
                                </el-text>
                                <el-button class="disable-init-animate" v-if="editing"
                                           style="margin-left: 4px;" link @click="addProvider"
                                           :icon="HarmonyOSIcon_Plus">
                                    添加提供商
                                </el-button>
                            </div>
                            <transition name="blur-scale" mode="out-in">
                                <div
                                    style="display: flex;flex-direction: column;align-items: stretch;align-self: stretch"
                                    v-if="data.providers && data.providers.length > 0">
                                    <VueDraggable
                                        ref="draggable"
                                        v-model="data.providers"
                                        :animation="150"
                                        ghostClass="ghost"
                                        handle=".handle">
                                        <transition-group name="slide-hide">
                                            <o-auth2-provider-card v-for="(provider,index) of data.providers"
                                                                   style="margin: 2px"
                                                                   v-model="data.providers[index]"
                                                                   v-model:error="inputErrors[index + 1]"
                                                                   :key="provider.id" :editing="editing"
                                                                   @remove-provider="removeProvider(index)"/>
                                        </transition-group>
                                    </VueDraggable>
                                </div>
                                <el-empty description="无提供商" v-else style="align-self: stretch"/>
                            </transition>
                        </div>
                    </div>
                    <div v-else-if="error" style="display:flex;flex-direction: column">
                        <el-empty description="获取设置失败"></el-empty>
                        <el-button link type="primary" @click="loadData" size="large">重试</el-button>
                    </div>
                </transition>
            </div>
        </el-scrollbar>
    </div>
</template>

<style scoped>

</style>