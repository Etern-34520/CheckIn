<script setup>
// import {RouterView} from "vue-router";
import WebSocketConnector from "@/api/websocket.js";
import Router from "@/router/index.js";
// import {getCurrentInstance, onMounted} from 'vue';

const {proxy} = getCurrentInstance();

function loginAs(user) {
    proxy.$cookies.set("name", user.name);
    proxy.$cookies.set("qq", user.qq);
    WebSocketConnector.connect(user.qq, user.token);
    if (window.location.pathname === "/checkIn/login/")
        Router.push({name: "home"});
}

onMounted(() => {
    if (proxy.$cookies.get("token") !== null) {
        loginAs({name: proxy.$cookies.get("name"), qq: proxy.$cookies.get("qq"), token: proxy.$cookies.get("token")});
    } else {
        Router.push({name: "login"});
    }
});
</script>

<template>
    <router-view @loginAs="loginAs" v-slot="{ Component }">
        <transition name="main-router" mode="out-in" duration="1000">
            <component :is="Component"/>
        </transition>
    </router-view>
</template>

<style scoped>
/*noinspection CssUnusedSymbol*/
.main-router-enter-active, .main-router-leave-active {
    transition: opacity 0.4s;
}

/*noinspection CssUnusedSymbol*/
.main-router-enter-from, .main-router-leave-to {
    opacity: 0;
}

/*noinspection CssUnusedSymbol*/
.main-router-enter-to, .main-router-leave-from {
    opacity: 1;
}
</style>