<script setup>
import {Splitpanes, Pane} from 'splitpanes'
import 'splitpanes/dist/splitpanes.css'
import UserDataInterface from "@/data/UserDataInterface.js";
import router from "@/router/index.js";

const user = UserDataInterface.getCurrentUser();
const animate = ref(false);
if (router.options.history.state.back === "/login/") {
    animate.value = true;
}

</script>

<template>
    <div style="display: flex;flex-direction: column;align-content: stretch;align-items: stretch">
        <div class="welcome" :class="{animate:animate}">
            <el-text style="font-size: 24px">
                欢迎回来，
            </el-text>
            <el-text style="font-size: 28px;color: var(--el-color-primary)">
                {{ user.name }}
            </el-text>
        </div>
        <splitpanes style="flex: 1">
            <pane min-size="20" size="30">
<!--                <div class="clickable panel my-record">
                    <el-text style="font-size: 16px">我的答题记录</el-text>
                </div>-->
                <div class="panel">
                    traffics
                </div>
            </pane>
            <pane min-size="50">
                <div class="panel">
                    new questions
                </div>
            </pane>
        </splitpanes>
    </div>
</template>

<style scoped>
.welcome {
    display: flex;
    flex-direction: row;
    box-sizing: border-box;
    align-items: end;
    overflow: hidden;
    max-height: 0;
}

.welcome.animate {
    animation: welcomeAnimation 4s var(--ease-in-out-quint);
    animation-delay: 600ms;
    animation-fill-mode: backwards;
}

@keyframes welcomeAnimation {
    0% {
        opacity: 0;
        transform: translateY(16px);
        filter: blur(8px);
        max-height: 0;
    }
    20% {
        opacity: 1;
        transform: translateY(0);
        filter: blur(0);
        max-height: 46px;
    }
    60% {
        opacity: 1;
        transform: translateY(0);
        filter: blur(0);
        max-height: 46px;
    }
    100% {
        opacity: 0;
        transform: translateY(0);
        filter: blur(0);
        max-height: 0;
    }
}

.welcome > *:first-child {
    margin-left: 16px;
}

.welcome > * {
    margin-bottom: 8px;
}

.my-record {
    display: flex;
    max-height: 30px;
    margin-bottom: 4px !important;
    justify-content: center;
}
</style>