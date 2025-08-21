<script setup>
import WebSocketConnector from "@/api/websocket.js";
import Loading_ from "@/components/common/_Loading_.vue";

// FULLY_AVAILABLE,
// MAY_FAIL,
// UNAVAILABLE,
const props = defineProps({
    displayStatuses: {
        type: Array,
        default: () => ["UNAVAILABLE"]
        // default: () => ["UNAVAILABLE", "MAY_FAIL", "FULLY_AVAILABLE"]
    },
    loadingText: {
        type: String,
        default: "加载中"
    }
})

const loading = ref(false);
const loaded = ref(false);
const error = ref(false);
const serverStatuses = ref({});
const loadServerStatuses = () => {
    loaded.value = false;
    setTimeout(() => {
        if (loaded.value === false) {
            loading.value = true;
        }
    }, 100);
    WebSocketConnector.send({
        type: "getServiceStatuses"
    }).then((res) => {
        loading.value = false;
        loaded.value = true;
        serverStatuses.value = res.data.serverStatuses;
    }, (e) => {
        e.disableNotification();
        loading.value = false;
        loaded.value = true;
        error.value = true;
    });
}
loadServerStatuses();

const getAlertType = (status) => {
    switch (status) {
        case "UNAVAILABLE":
            return "error";
        case "MAY_FAIL":
            return "warning";
        case "FULLY_AVAILABLE":
            return "success";
    }
}

const getAlertType1 = (status) => {
    switch (status) {
        case "UNAVAILABLE":
            return "danger";
        case "MAY_FAIL":
            return "warning";
        case "FULLY_AVAILABLE":
            return "success";
    }
}

const getAlertStatusDescription = (status) => {
    switch (status) {
        case "UNAVAILABLE":
            return "不可用";
        case "MAY_FAIL":
            return "可能失败";
        case "FULLY_AVAILABLE":
            return "可用";
    }
}
</script>

<template>
    <div class="server-statuses">
        <transition name="smooth-height" mode="out-in">
            <div v-if="loading" class="smooth-height-base" key="loading">
                <div style="display: flex;justify-content: center;align-items: center;">
                    <el-icon style="margin-left: 4px">
                        <Loading_/>
                    </el-icon>
                    <el-text style="margin-left: 8px" type="info">{{ props.loadingText }}</el-text>
                </div>
            </div>
            <div v-else-if="error" class="smooth-height-base" key="error">
                <div style="flex: 1;display: flex;justify-content: center;align-items: center;">
                    <el-text style="align-self: center;margin-right: 12px" type="info">
                        加载服务状态时出错
                    </el-text>
                    <el-button class="disable-init-animate" link @click="loadServerStatuses">重新加载</el-button>
                </div>
            </div>
            <div class="smooth-height-base" v-else key="main" style="flex: 1">
                <div style="display: flex;flex-direction: row;flex-wrap: wrap;flex: 1;gap: 2px;">
                    <transition-group name="smooth-height">
                        <div class="smooth-height-base" key="generateAvailability" style="flex: 1;"
                             v-if="serverStatuses.generateAvailability && displayStatuses.includes(serverStatuses.generateAvailability.status)">
                            <div style="min-width: min(140px, 80dvw);display: flex;flex-direction: column;">
                                <el-alert :closable="false" style="border-radius: 4px;flex: 1"
                                          :type="getAlertType(serverStatuses.generateAvailability.status)">
                                    <div style="display: flex;flex-direction: row;flex-wrap: wrap">
                                        <el-text :type="getAlertType1(serverStatuses.generateAvailability.status)"
                                                 style="margin-right: 32px">
                                            试题生成
                                            {{ getAlertStatusDescription(serverStatuses.generateAvailability.status) }}
                                        </el-text>
                                        <el-text type="info" v-if="serverStatuses.generateAvailability.reason">
                                            原因: {{ serverStatuses.generateAvailability.reason }}
                                        </el-text>
                                    </div>
                                </el-alert>
                            </div>
                        </div>
                        <div class="smooth-height-base" key="submitAvailability" style="flex: 1;"
                             v-if="serverStatuses.submitAvailability && displayStatuses.includes(serverStatuses.submitAvailability.status)">
                            <div style="min-width: min(140px, 80dvw);display: flex;flex-direction: column;">
                                <el-alert :closable="false" style="border-radius: 4px;flex: 1"
                                          :type="getAlertType(serverStatuses.submitAvailability.status)">
                                    <div style="display: flex;flex-direction: row;flex-wrap: wrap">
                                        <el-text :type="getAlertType1(serverStatuses.submitAvailability.status)"
                                                 style="margin-right: 32px">
                                            试题提交 {{
                                                getAlertStatusDescription(serverStatuses.submitAvailability.status)
                                            }}
                                        </el-text>
                                        <el-text type="info" v-if="serverStatuses.submitAvailability.reason">
                                            原因: {{ serverStatuses.submitAvailability.reason }}
                                        </el-text>
                                    </div>
                                </el-alert>
                            </div>
                        </div>
                    </transition-group>
                </div>
            </div>
        </transition>
    </div>
</template>

<style scoped>
.server-statuses {
    display: flex;
    flex-wrap: wrap;
    flex-direction: row;
    padding: 0;
    flex: initial;
    height: auto;
    margin-bottom: 0 !important;
    transition: margin-bottom 200ms var(--ease-in-out-quint);
}

.server-statuses:has(*) {
    margin-bottom: 8px !important;
}
</style>