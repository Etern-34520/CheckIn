<script setup>
import WebSocketConnector from "@/api/websocket.js";
import Loading_ from "@/components/common/_Loading_.vue";
import ServerStatus from "@/data/ServerStatus.js";

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

const loadServerStatuses = () => {
    ServerStatus.load();
}

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
            <div v-if="ServerStatus.loading" class="smooth-height-base" key="loading">
                <div style="display: flex;justify-content: center;align-items: center;">
                    <el-icon style="margin-left: 4px">
                        <Loading_/>
                    </el-icon>
                    <el-text style="margin-left: 8px" type="info">{{ props.loadingText }}</el-text>
                </div>
            </div>
            <div v-else-if="ServerStatus.error" class="smooth-height-base" key="error">
                <div style="flex: 1;display: flex;justify-content: center;align-items: center;">
                    <el-text style="align-self: center;margin-right: 12px" type="info">
                        加载服务状态时出错
                    </el-text>
                    <el-button class="disable-init-animate" link @click="loadServerStatuses">重新加载</el-button>
                </div>
            </div>
            <div class="smooth-height-base" v-else key="main" style="flex: 1">
                <div class="alerts" style="display: flex;flex-direction: row;flex-wrap: wrap;flex: 1;gap: 2px;">
                    <transition-group name="smooth-height">
                        <div class="smooth-height-base" key="generateAvailability" style="flex: 1;"
                             v-if="ServerStatus.generateAvailability && displayStatuses.includes(ServerStatus.generateAvailability.status)">
                            <div style="min-width: min(50%, 140px, 80dvw);display: flex;flex-direction: column;">
                                <el-alert :closable="false" style="border-radius: 4px;flex: 1"
                                          :type="getAlertType(ServerStatus.generateAvailability.status)">
                                    <div style="display: flex;flex-direction: row;flex-wrap: wrap">
                                        <el-text :type="getAlertType1(ServerStatus.generateAvailability.status)"
                                                 style="margin-right: 32px">
                                            试题生成
                                            {{ getAlertStatusDescription(ServerStatus.generateAvailability.status) }}
                                        </el-text>
                                        <el-text type="info" v-if="ServerStatus.generateAvailability.reason">
                                            原因: {{ ServerStatus.generateAvailability.reason }}
                                        </el-text>
                                    </div>
                                </el-alert>
                            </div>
                        </div>
                        <div class="smooth-height-base" key="submitAvailability" style="flex: 1;"
                             v-if="ServerStatus.submitAvailability && displayStatuses.includes(ServerStatus.submitAvailability.status)">
                            <div style="min-width: min(50%, 140px, 80dvw);display: flex;flex-direction: column;">
                                <el-alert :closable="false" style="border-radius: 4px;flex: 1"
                                          :type="getAlertType(ServerStatus.submitAvailability.status)">
                                    <div style="display: flex;flex-direction: row;flex-wrap: wrap">
                                        <el-text :type="getAlertType1(ServerStatus.submitAvailability.status)"
                                                 style="margin-right: 32px">
                                            试题提交 {{
                                                getAlertStatusDescription(ServerStatus.submitAvailability.status)
                                            }}
                                        </el-text>
                                        <el-text type="info" v-if="ServerStatus.submitAvailability.reason">
                                            原因: {{ ServerStatus.submitAvailability.reason }}
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

.server-statuses:has(.alerts > *) {
    margin-bottom: 8px !important;
}
</style>