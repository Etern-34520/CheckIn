<script setup>
// import {RouterView} from "vue-router";
import WebSocketConnector from "@/api/websocket.js";
import Router from "@/router/index.js";
// import {getCurrentInstance, onMounted} from 'vue';

const {proxy} = getCurrentInstance();

function loginAs(user) {
    proxy.$cookies.set("name", user.name, "8h", "/checkIn");
    proxy.$cookies.set("qq", user.qq, "8h", "/checkIn");
    proxy.$cookies.set("token", user.token, "8h", "/checkIn");
    if (window.location.pathname === "/checkIn/login/") {
        Router.push({name: "home"}).then(WebSocketConnector.connect(user.qq, user.token));
    } else {
        WebSocketConnector.connect(user.qq, user.token);
    }
}

onMounted(() => {
    if (proxy.$cookies.get("token") !== null) {
        const name = proxy.$cookies.get("name","/checkIn");
        const qq = proxy.$cookies.get("qq","/checkIn");
        const token = proxy.$cookies.get("token","/checkIn");
        const user = {name: name, qq: qq, token: token};
        console.log(user);
        loginAs(user);
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