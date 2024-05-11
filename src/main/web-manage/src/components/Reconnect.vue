<script setup>
import WebSocketConnector from "@/api/websocket.js";
import {Loading} from "@element-plus/icons-vue";

const loading = ref(false);

const reconnect = () => {
    loading.value = true;
    WebSocketConnector.reconnect().then(() => {
        loading.value = false;
    }, () => {
        loading.value = false;
    });
}
</script>

<template>
<ElButton type="primary" @click="reconnect" :loading="loading" loading-icon="_Loading_">重新连接</ElButton>
</template>

<style scoped>

</style>