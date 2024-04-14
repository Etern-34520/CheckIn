<script setup>
import {RouterView} from "vue-router";
import WebSocketConnector from "@/api/websocket.js";
import Router from "@/router/index.js";
import {getCurrentInstance} from 'vue';
const { proxy } = getCurrentInstance();

function loginAs(user) {
    Router.push("/manage/");
    proxy.$cookies.set("name", response.name);
    proxy.$cookies.set("qq", response.qq);
}

</script>

<template>
    <router-view @loginAs="loginAs" v-slot="{ Component }">
        <transition name="main-router" mode="out-in">
            <component :is="Component"/>
        </transition>
    </router-view>
</template>

<style scoped>
.main-router-enter-active, .main-router-leave-active {
    transition: opacity 0.4s;
}

.main-router-enter-from, .main-router-leave-to {
    opacity: 0;
}

.main-router-enter-to, .main-router-leave-from {
    opacity: 1;
}
</style>