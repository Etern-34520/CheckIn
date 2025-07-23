<script setup>
import WebSocketConnector from "@/api/websocket.js";
import _Loading_ from "@/components/common/_Loading_.vue";

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
    <div style="display: flex;flex-direction: column;margin-top: 32px">
        <ElButton style="flex: 1" type="primary" @click="reconnect" :loading="loading" :loading-icon="_Loading_">重新连接</ElButton>
    </div>
</template>

<style scoped>

</style>