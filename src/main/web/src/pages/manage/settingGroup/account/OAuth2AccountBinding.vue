<script setup>
import WebSocketConnector from "@/api/websocket.js";
import {PictureRounded} from "@element-plus/icons-vue";
import {ElMessage, ElMessageBox} from "element-plus";
import _Loading_ from "@/components/common/_Loading_.vue";

const {proxy} = getCurrentInstance();

const providers = ref();
const oAuth2Bindings = ref();
const loading = ref(true);
const error = ref(false);

const loadingIconIndex = ref(-1);

const errorMessage = proxy.$cookies.get("OAuth2ErrorMessage", "/checkIn");
if (errorMessage !== null) {
    ElMessageBox.alert(
        errorMessage,
        "绑定失败",
        {
            type: "error",
            draggable: true,
            showClose: false,
            confirmButtonText: "确定"
        }
    ).then(() => {
        proxy.$cookies.remove("OAuth2ErrorMessage", "/checkIn");
    });
}

const loadData = () => {
    loading.value = true;
    Promise.all([
        WebSocketConnector.send({
            type: "getOAuth2ProvidersSimpleInfo"
        }).then(resp => {
            providers.value = resp.data.providers
        }).catch(err => {
            err.disableNotification();
        }),
        WebSocketConnector.send({
            type: "getCurrentUserBindingInfo"
        }).then(resp => {
            oAuth2Bindings.value = resp.data.oAuth2Bindings
        }).catch(err => {
            err.disableNotification();
        })
    ]).then(() => {
        loading.value = false;
        error.value = false;
    }).catch((err) => {
        loading.value = false;
        error.value = true;
    });
}
loadData();

const switchBinding = (provider) => {
    if (oAuth2Bindings.value[provider.id] === undefined) {
        proxy.$cookies.set("inOAuth2Binding", "true", "10m", "/checkIn");
        window.location.href = `${window.location.origin}/checkIn/api/oauth2/authorization/${provider.id}`;
    } else {
        ElMessageBox.confirm(
            "可重新绑定",
            "确认解绑？",
            {
                type: "warning",
                draggable: true,
                showClose: false,
                confirmButtonText: "确认解绑",
                cancelButtonText: "取消操作"
            },
        ).then(() => {
            WebSocketConnector.send({
                type: "unbindOAuth2Account",
                data: {
                    providerId: provider.id
                }
            }).then(() => {
                ElMessage({
                    type: "success",
                    message: "解绑成功"
                });
                delete oAuth2Bindings.value[provider.id];
            }).catch((err) => {
            });
        }).catch(() => {
        });
    }
}
</script>

<template>
    <div>
        <el-scrollbar style="flex: 1">
            <div style="display: flex;flex-direction: column;align-items: center;width: calc(100% - 4px)">
                <div style="max-width: 1080px; width: min(95%, 1080px); display: flex; flex-direction: column;">
                    <el-text style="font-size: 24px;align-self: baseline;margin-bottom: 16px">账户关联</el-text>
                    <template v-if="!loading && !error && providers && providers.length > 0">
                        <div class="panel-1" style="padding: 12px 20px;display: flex" v-for="(provider, index) of providers">
                            <el-image :src="'https://favicon.im/' + provider.iconDomain"
                                      style="width: 24px;height: 24px;padding: 8px">
                                <template #error>
                                    <el-icon :size="24">
                                        <PictureRounded/>
                                    </el-icon>
                                </template>
                            </el-image>
                            <el-text style="align-self: center;margin-left: 4px;" size="large">
                                {{ provider.name }}
                            </el-text>
                            <div class="flex-blank-1"></div>
                            <el-text style="margin-right: 8px" type="info">
                                {{ oAuth2Bindings[provider.id] === undefined ? "未绑定" : "已绑定" }}
                            </el-text>
                            <el-button :loading="loadingIconIndex === index" :disabled="loadingIconIndex !== -1"
                                       :loading-icon="_Loading_" link :type="oAuth2Bindings[provider.id] === undefined ? 'primary' : 'danger'" style="align-self: center" @click="switchBinding(provider)">
                                {{ oAuth2Bindings[provider.id] === undefined ? "绑定" : "解绑" }}
                            </el-button>
                        </div>
                    </template>
                    <el-empty description="无提供商" v-else style="align-self: stretch"/>
                </div>
            </div>
        </el-scrollbar>
    </div>
</template>

<style scoped>
</style>