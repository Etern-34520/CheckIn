<script setup>
import {RouterView} from 'vue-router'
import router from "@/router/index.js";

const {proxy} = getCurrentInstance();

const oAuth2FallbackRouteName = proxy.$cookies.get("OAuth2FallbackRouteName", "/checkIn");
proxy.$cookies.remove("OAuth2Mode", "/checkIn");
proxy.$cookies.remove("OAuth2FallbackRouteName", "/checkIn");
if (oAuth2FallbackRouteName) {
    router.push({name: oAuth2FallbackRouteName});
}
</script>

<template>
    <RouterView/>
</template>

<style scoped>
</style>