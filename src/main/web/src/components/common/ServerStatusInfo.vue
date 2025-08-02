<script setup>
import WebSocketConnector from "@/api/websocket.js";

// FULLY_AVAILABLE,
// MAY_FAIL,
// UNAVAILABLE,
const props = defineProps({
    displayStatuses: {
        type: Array,
        default: () => ["UNAVAILABLE"]
        // default: () => ["UNAVAILABLE", "MAY_FAIL", "FULLY_AVAILABLE"]
    }
})

const serverStatuses = ref();
const loadServerStatuses = () => {
    WebSocketConnector.send({
        type: "getServiceStatuses"
    }).then(res => {
        serverStatuses.value = res.data.serverStatuses;
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
    <div v-if="serverStatuses" class="server-statuses">
        <el-alert :closable="false" style="border-radius: 4px;flex: 1;min-width: min(280px, 80dvw)"
                  v-if="displayStatuses.includes(serverStatuses.generateAvailability.status)"
                  :type="getAlertType(serverStatuses.generateAvailability.status)">
            <div style="display: flex;flex-direction: row;flex-wrap: wrap">
                <el-text :type="getAlertType1(serverStatuses.generateAvailability.status)" style="margin-right: 32px">
                    试题生成 {{ getAlertStatusDescription(serverStatuses.generateAvailability.status) }}
                </el-text>
                <el-text type="info" v-if="serverStatuses.generateAvailability.reason">
                    原因: {{ serverStatuses.generateAvailability.reason }}
                </el-text>
            </div>
        </el-alert>
        <el-alert :closable="false" style="border-radius: 4px;flex: 1;min-width: min(280px, 80dvw)"
                  v-if="displayStatuses.includes(serverStatuses.submitAvailability.status)"
                  :type="getAlertType(serverStatuses.submitAvailability.status)">
            <div style="display: flex;flex-direction: row;flex-wrap: wrap">
                <el-text :type="getAlertType1(serverStatuses.submitAvailability.status)" style="margin-right: 32px">
                    试题提交 {{ getAlertStatusDescription(serverStatuses.submitAvailability.status) }}
                </el-text>
                <el-text type="info" v-if="serverStatuses.submitAvailability.reason">
                    原因: {{ serverStatuses.submitAvailability.reason }}
                </el-text>
            </div>
        </el-alert>
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
    margin-bottom: 8px !important;
    gap: 2px;
}
</style>