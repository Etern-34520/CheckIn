<script setup>
import {ArrowRight} from "@element-plus/icons-vue";
import router from "@/router/index.js";

const props = defineProps({
    user: {
        type: Object,
        required: true,
    },
    breadcrumbPathArray: {
        type: Array,
        required: true,
    },
});

const menuInlineStyle = defineModel("menuInlineStyle", {
    type: Boolean,
    default: false
});

function switchMenuStyle() {
    menuInlineStyle.value = !menuInlineStyle.value;
}

// changeUrl.value = changeUrlFunc;
</script>
<template>
    <div id="top-bar">
        <el-button v-on:click="switchMenuStyle" style="width: 30px;height: 30px;margin-left: 5px;margin-right: 4px"
                   link>
            <svg id="menuIcon" width="30" height="30" xmlns="http://www.w3.org/2000/svg">
                <g>
                    <rect rx="1" id="svg_1" height="2" width="16" y="19" x="8"/>
                    <rect rx="1" id="svg_2" height="2" width="16" y="14" x="8"/>
                    <rect rx="1" id="svg_3" height="2" width="16" y="9" x="8"/>
                </g>
            </svg>
        </el-button>
        <el-breadcrumb style="flex-shrink: 0;" :separator-icon="ArrowRight">
            <!--suppress JSValidateTypes -->
            <el-breadcrumb-item :to="{ path: '/manage/' }">checkIn</el-breadcrumb-item>
            <TransitionGroup name="breadcrumb-item">
                <!--suppress JSValidateTypes -->
                <el-breadcrumb-item :key="pathItem.name" v-for="(pathItem,$index) in breadcrumbPathArray" :to="$index!==breadcrumbPathArray.length-1?{path: pathItem.path}:undefined">
                    {{ pathItem.name }}
                </el-breadcrumb-item>
            </TransitionGroup>
        </el-breadcrumb>
        <div class="flex_blank_1"></div>
        <el-button @click="router.push('/manage/user-setting/')" text
                   style="margin-right: 2px;padding: 4px;transition: 200ms ease-in-out">
            <el-avatar shape="circle" size="small" :src="'https://q1.qlogo.cn/g?b=qq&nk='+user.qq+'&s=100'"
                       style="margin-right: 4px"></el-avatar>
            <el-text size="large">{{ user.name }}</el-text>
            <el-text size="large" type="info" style="margin-left: 4px;">{{ user.qq }}</el-text>
        </el-button>
    </div>
</template>

<style scoped>
@import './../assets/base.css';

#top-bar {
    height: 34px;
    display: flex;
    flex-direction: row;
    align-items: center;
    z-index: 3;
}

#menuIcon rect {
    fill: var(--el-text-color-primary);
    transition: 200ms ease-in-out;
}

button:hover #menuIcon rect {
    fill: var(--el-text-color-secondary);
}

.breadcrumb-item-enter-active {
    transition: all 0.3s ease-out;
}

.breadcrumb-item-leave-active {
    transition: all 0.3s ease-in;
}

.breadcrumb-item-enter-from,
.breadcrumb-item-leave-to {
    margin-left: -10px;
    width: 0;
    overflow: visible;
    word-break: keep-all;
    opacity: 0;
    filter: blur(6px);
}
</style>