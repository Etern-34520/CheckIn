<script setup>
import {Splitpanes, Pane} from 'splitpanes'
import 'splitpanes/dist/splitpanes.css'
import UserDataInterface from "@/data/UserDataInterface.js";

const user = UserDataInterface.getCurrentUser();
</script>

<template>
    <div style="display: flex;flex-direction: column;align-content: stretch;align-items: stretch">
        <div class="welcome">
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

<style>
.main-router-enter-active .welcome {
    transition: 200ms var(--ease-in-out-quint);
    transition-delay: 600ms;
    max-height: 46px;
    opacity: 1;
}

.welcome {
    transition: 600ms var(--ease-in-out-quint);
    transition-delay: 1000ms;
    max-height: 0;
    opacity: 0;
}

.main-router-enter-from .welcome {
    opacity: 0;
    filter: blur(8px);
    transform: translateY(16px);
}
</style>