<script setup>
import getAvatarUrlOf from "@/utils/Avatar.js";
import HarmonyOSIcon_InfoCircle from "@/components/icons/HarmonyOSIcon_InfoCircle.vue";
import router from "@/router/index.js";
import {ElMessage, ElMessageBox} from "element-plus";
import {ArrowLeftBold, Link, PictureRounded, Close} from "@element-plus/icons-vue";
import _Loading_ from "@/components/common/_Loading_.vue";
import VueTurnstile from "vue-turnstile";
import WebSocketConnector from "@/api/websocket.js";
import {jwtDecode} from "jwt-decode";

const {proxy} = getCurrentInstance();
const props = defineProps({
    facadeData: {
        required: true,
        type: Object
    },
    extraData: {
        required: true,
        type: Object
    }
});

const requiredPartitionIds = props.extraData.requiredPartitionIds;
const selectablePartitionIds = props.extraData.selectablePartitionIds;

const selectedPartitionIds = ref([]);
const selectPartition = (partitionId) => {
    const index = selectedPartitionIds.value.indexOf(partitionId);
    if (index === -1) {
        selectedPartitionIds.value.push(partitionId);
    } else {
        selectedPartitionIds.value.splice(index, 1);
    }
}

proxy.$http.get("check-turnstile").then((resp) => {
    proxy.$cookies.set("verifyLogin", resp.enableTurnstileOnLogin, "7d", "/checkIn");
    proxy.$cookies.set("verifyExam", resp.enableTurnstileOnExam, "7d", "/checkIn");
    proxy.$cookies.set("siteKey", resp.siteKey, "7d", "/checkIn");
});

const qqNumber = ref();

const loadingExam = ref(false);
const startExam = () => {
    loadingExam.value = true;
    proxy.$http.post("generate", {
        qq: qqNumber.value,
        partitionIds: selectedPartitionIds.value,
        turnstileToken: token.value
    }).then((data) => {
        if (data.type !== "error") {
            proxy.$cookies.set("examInfo", JSON.stringify(data), "7d");
            proxy.$cookies.set("phase", "examine", "7d");
            proxy.$cookies.remove("submissions");
            proxy.$cookies.remove("timestamps");
            router.push({name: "examine"});
        } else {
            reset();
            loadingExam.value = false;
            ElMessageBox.alert(
                data.description ? data.description : data.exceptionType,
                "生成题目时出错", {
                    type: "error",
                    draggable: true,
                    showClose: false,
                    confirmButtonText: "返回修改生成选项"
                }
            )
        }
    }, (err) => {
        loadingExam.value = false;
        ElMessage({
            type: "error",
            message: "生成题目时出错" + ((err && err.message) ? err.message : "")
        })
    })
}

const validate1 = computed(() => selectedPartitionIds.value.length >= props.extraData.partitionRange[0] && selectedPartitionIds.value.length <= props.extraData.partitionRange[1]);
const validate2 = computed(() => qqNumber.value > 10000 && qqNumber.value < 100000000000);

const back = () => {
    proxy.$cookies.remove("phase");
    router.push({name: "facade"});
}

const token = ref("");
const turnstile = ref(null);

function reset() {
    if (turnstile.value)
        turnstile.value.reset();
}

const siteKey = ref(proxy.$cookies.get("siteKey"));
const verifyExam = ref(proxy.$cookies.get("verifyExam") === "true");
proxy.$http.get("check-turnstile").then((resp) => {
    proxy.$cookies.set("verifyLogin", resp.enableTurnstileOnLogin, "7d", "/checkIn");
    proxy.$cookies.set("verifyExam", resp.enableTurnstileOnExam, "7d", "/checkIn");
    proxy.$cookies.set("siteKey", resp.siteKey, "7d", "/checkIn");
    verifyExam.value = resp.enableTurnstileOnExam;
    siteKey.value = resp.siteKey;
});

onErrorCaptured((e) => {
    if (e.name === "TurnstileError") {
        // Turnstile 被禁用后的内部报错，可忽略
        console.trace("turnstile disabled", e);
        return false;
    }
})

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

const examToken = ref(proxy.$cookies.get("examToken", "/checkIn", "7d"));
const decodedHeaders = ref();
const allRequiredOAuth2HaveBend = ref({});

const haveBend = (provider) => {
    return decodedHeaders.value && decodedHeaders.value.OAuth2 ? Boolean(decodedHeaders.value.OAuth2[provider.id]) : false;
}

const unwatch1 = watchEffect(() => {
    decodedHeaders.value = Boolean(examToken.value) ? jwtDecode(examToken.value, {header: true}) : null
});
const unwatch2 = watchEffect(() => {
    for (const oAuth2Provider of props.extraData.oAuth2Providers) {
        if (oAuth2Provider.required && !haveBend(oAuth2Provider)) {
            allRequiredOAuth2HaveBend.value = false;
            return;
        }
    }
    allRequiredOAuth2HaveBend.value = true;
});

onBeforeUnmount(() => {
    unwatch1();
    unwatch2();
})

proxy.$http.post("refresh-exam-token", {}).then(() => {
    examToken.value = proxy.$cookies.get("examToken", "/checkIn");
});

const switchBinding = (provider, index) => {
    if (!haveBend(provider)) {
        loadingIconIndex.value = index;
        proxy.$cookies.set("OAuth2Mode", "exam", "10m", "/checkIn");
        proxy.$cookies.set("OAuth2FallbackRouteName", proxy.$router.currentRoute.value.name, "10m", "/checkIn");
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
            proxy.$cookies.set("OAuth2Mode", "exam", "10m", "/checkIn");
            proxy.$cookies.set("OAuth2FallbackRouteName", proxy.$router.currentRoute.value.name, "10m", "/checkIn");
            proxy.$http.post("refresh-exam-token", {
                unbindOAuth2s: [provider.id]
            }).then(() => {
                delete decodedHeaders.value.OAuth2[provider.id];
                ElMessage({
                    type: "success",
                    message: "取消绑定成功",
                });
            }, (e) => {
                ElMessage({
                    type: "error",
                    message: "取消绑定失败：" + e.data,
                });
                console.error(e);
            })
        }).catch(() => {
        });
    }
}
</script>

<template>
    <div class="auto-padding-center" style="flex:1;padding-bottom: 200px;">
        <el-button link size="large" @click="back"
                   style="margin-top: 36px;align-self: baseline;padding: 8px 16px !important;font-size: 1em">
            <el-icon>
                <ArrowLeftBold/>
            </el-icon>
            返回
        </el-button>
        <template v-if="((requiredPartitionIds && requiredPartitionIds.length > 0)
                        || (selectablePartitionIds && selectablePartitionIds.length > 0))">
            <el-text style="font-size: 24px;align-self: baseline;margin-top: 24px">选择分区</el-text>
            <div class="panel" style="padding: 16px 24px;margin-top: 36px"
                 v-if="requiredPartitionIds && requiredPartitionIds.length > 0">
                <el-text size="large" style="align-self: baseline">必选分区</el-text>
                <div style="display: flex;flex-direction: row;flex-wrap: wrap;margin-top: 16px;">
                    <el-tag size="large" type="info" style="font-size: 14px;margin: 2px"
                            v-for="requiredPartitionId of requiredPartitionIds">
                        {{ extraData.partitions[requiredPartitionId] }}
                    </el-tag>
                </div>
            </div>
            <div class="panel" v-if="selectablePartitionIds && selectablePartitionIds.length > 0"
                 style="padding: 16px 24px;margin-top: 24px">
                <div style="display: flex;flex-direction: row;flex-wrap: wrap;">
                    <el-text size="large" style="margin-right: 8px;">可选分区</el-text>
                    <el-text>{{ selectedPartitionIds.length }} / {{ selectablePartitionIds.length }}</el-text>
                    <el-text style="margin-left: 8px;"
                             :type="validate1?'info':'danger'">
                        请选择 {{ extraData.partitionRange[0] }} ~
                        {{ Math.min(selectablePartitionIds.length, extraData.partitionRange[1]) }} 个
                    </el-text>
                </div>
                <div style="display: flex;flex-direction: row;flex-wrap: wrap;margin-top: 16px;">
                    <el-check-tag size="large" type="info" style="font-size: 14px;margin: 2px;"
                                  v-for="partitionId of selectablePartitionIds"
                                  :checked="selectedPartitionIds.includes(partitionId)"
                                  @click="selectPartition(partitionId)">
                        {{ extraData.partitions[partitionId] }}
                    </el-check-tag>
                </div>
            </div>
        </template>
        <template v-if="extraData.oAuth2Providers?.length > 0">
            <div style="display: flex;flex-direction: row;align-items: center;margin-top: 36px">
                <el-text style="font-size: 24px;align-self: baseline;margin-right: 16px;">绑定第三方账户</el-text>
                <el-popover trigger="hover" width="250">
                    <template #reference>
                        <HarmonyOSIcon_InfoCircle :size="20"/>
                    </template>
                    <template #default>
                        <el-text>仅用于记录答题信息及后续用户登录</el-text>
                    </template>
                </el-popover>
            </div>
            <div style="display: flex;flex-direction: row;align-items: start;margin-top: 16px;gap: 8px;flex-wrap: wrap">
                <div class="panel-1" style="padding: 12px 20px;display: flex;flex: 1;max-width: 240px"
                     v-for="(provider, index) of extraData.oAuth2Providers">
                    <el-image :src="'https://favicon.im/' + provider.iconDomain"
                              style="width: 30px;height: 30px;padding: 4px">
                        <template #error>
                            <el-icon :size="32">
                                <PictureRounded/>
                            </el-icon>
                        </template>
                    </el-image>
                    <div style="display: flex;flex-direction: column;justify-content: center;margin-left: 4px;">
                        <el-text style="align-self: start;" size="large">
                            {{ provider.name }}
                        </el-text>
                        <el-text v-if="provider.required"
                                 :type="haveBend(provider) ? 'info' : 'danger'"
                                 style="align-self: start;">
                            必须
                        </el-text>
                        <el-text v-else type="info" style="align-self: start;">
                            可选
                        </el-text>
                    </div>
                    <div class="flex-blank-1" style="min-width: 20px"></div>
                    <el-button :loading="loadingIconIndex === index" :disabled="loadingIconIndex !== -1"
                               :icon="haveBend(provider) ? Close : Link"
                               :loading-icon="_Loading_" link
                               :type="haveBend(provider) ? 'danger' : 'primary'"
                               style="align-self: center" @click="switchBinding(provider, index)">
                        {{ haveBend(provider) ? "解绑" : "绑定" }}
                    </el-button>
                </div>
            </div>
        </template>
        <div style="display: flex;flex-direction: row;align-items: center;margin-top: 36px">
            <el-text style="font-size: 24px;align-self: baseline;margin-right: 16px;">你的QQ号码</el-text>
            <el-popover trigger="hover" width="250">
                <template #reference>
                    <HarmonyOSIcon_InfoCircle :size="20"/>
                </template>
                <template #default>
                    <el-text>仅用于记录答题信息及后续用户登录</el-text>
                </template>
            </el-popover>
        </div>
        <div style="display: flex;flex-direction: row;align-items: center;margin-top: 16px;margin-left: 16px;">
            <el-avatar :size="64" style="margin-right: 16px" :src="getAvatarUrlOf(qqNumber)"/>
            <el-input-number :class="{error: !validate2}" v-model="qqNumber"
                             :controls="false" style="min-width: min(70dvw,200px)"/>
        </div>
        <div class="flex-blank-1"></div>
        <div style="height: 65px;width:300px;display: flex;">
            <vue-turnstile ref="turnstile" v-if="verifyExam" appearance="interaction-only" @error="reset"
                           :site-key="siteKey" v-model="token" size="normal"/>
        </div>
        <el-button type="primary" size="large" :loading="Boolean(loadingExam || (token.length === 0 && verifyExam))"
                   :loading-icon="_Loading_"
                   style="margin-top: 36px;align-self: center;min-width: 180px"
                   :disabled="!extraData.serviceAvailable || !(validate1 && validate2) || (token.length === 0 && verifyExam) || !allRequiredOAuth2HaveBend"
                   @click="startExam">
            {{
                extraData.serviceAvailable ? token.length === 0 && verifyExam ? "等待 Cloudflare 验证" : "生成题目" : "服务暂不可用"
            }}
        </el-button>
    </div>
</template>

<style scoped>

</style>