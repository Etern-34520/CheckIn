<script setup>
import LoginDialog from "@/components/common/LoginDialog.vue";
import iconBlack from "@/assets/icons/icon-black.svg";
import iconWhite from "@/assets/icons/icon-white.svg";
import UI_Meta from "@/utils/UI_Meta.js";
import UserDataInterface from "@/data/UserDataInterface.js";

const colorScheme = UI_Meta.colorScheme;
UserDataInterface.logout();

const getIcon = () => {
    if (colorScheme.value === 'light') {
        return iconBlack;
    } else {
        return iconWhite;
    }
}
</script>

<template>
    <div style="width: 100%;height:100%;display: flex;align-items: center;align-content:center;justify-content: center;overflow: hidden">
        <div class="pwa-title-common"></div>
        <div style="z-index: 2">
            <div style="display: flex;flex-direction: row;margin-bottom: 4px;">
                <img :src="getIcon()" class="animate-0" alt="" width="64" height="64" style="margin-bottom: 16px;opacity: 0.6"/>
                <div style="display: flex;flex-direction: column;margin-left: 16px">
                    <el-text class="animate-1" style="align-self: baseline;font-size:1.5em;user-select: none">Check In</el-text>
                    <el-text class="animate-2" style="align-self: baseline;font-size:1.4em;user-select: none" size="large">登录</el-text>
                </div>
            </div>
            <login-dialog class="animate-3" style="width: 300px;"/>
        </div>
        <div class="background-font animate-0">
            <div style="font-size: 256px;">
                CHECKIN
            </div>
            <div style="font-size: 200px;margin-top: -200px;">
                LOGIN
            </div>
        </div>
    </div>
</template>

<style scoped>
.background-font {
    position: absolute;
    display: flex;
    flex-direction: column;
    align-content: center;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;
    color: var(--el-text-color);
    opacity: 0.07;
    user-select: none;
    overflow: hidden;
    filter: blur(16px);
}

@keyframes ani-0 {
    0% {
        filter: blur(16px);
        opacity: 0;
    }
    100% {
    }
}
@keyframes ani-1 {
    0% {
        transform: translate3d(-24px,0,0);
        opacity: 0;
    }
    100% {
        opacity: 1;
    }
}
@keyframes ani-2 {
    0% {
        transform: translate3d(-16px,0,0);
        opacity: 0;
    }
    100% {
        opacity: 1;
    }
}
@keyframes ani-3 {
    0% {
        filter: blur(16px);
        transform: translate3d(0,-16px,0);
        opacity: 0;
    }
    100% {
        filter: blur(0);
        opacity: 1;
    }
}

.animate-0 {
    animation: ani-0 0.8s 200ms var(--ease-out-quint);
    animation-fill-mode: backwards;
}
.animate-1 {
    animation: ani-1 0.6s 300ms var(--ease-out-quint);
    animation-fill-mode: backwards;
}
.animate-2 {
    animation: ani-2 0.6s 600ms var(--ease-out-quint);
    animation-fill-mode: backwards;
}
.animate-3 {
    animation: ani-3 1s 400ms var(--ease-out-quint);
    animation-fill-mode: backwards;
}
</style>