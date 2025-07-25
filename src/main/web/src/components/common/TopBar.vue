<script setup>
import {ArrowLeftBold, ArrowRight, ArrowRightBold} from "@element-plus/icons-vue";
import router from "@/router/index.js";
import getAvatarUrlOf from "@/utils/Avatar.js";
import UI_Meta from "@/utils/UI_Meta.js";
import iconWhite from "@/assets/icons/icon-white.svg";
import iconBlack from "@/assets/icons/icon-black.svg";

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

const colorScheme = UI_Meta.colorScheme;
const mobile = UI_Meta.mobile;
const inPWA = UI_Meta.inPWA;

const getIcon = () => {
    if (colorScheme.value === 'light') {
        return iconBlack;
    } else {
        return iconWhite;
    }
}

</script>
<template>
    <div id="top-bar">
        <el-button v-on:click="switchMenuStyle" class="menu-display-button" link
                   style="width: 30px;height: 30px;margin-left: 8px;margin-right: 4px;">
            <svg id="menuIcon" width="30" height="30" xmlns="http://www.w3.org/2000/svg">
                <g>
                    <rect rx="1" id="svg_1" height="2" width="16" y="19" x="7"/>
                    <rect rx="1" id="svg_2" height="2" width="16" y="14" x="7"/>
                    <rect rx="1" id="svg_3" height="2" width="16" y="9" x="7"/>
                </g>
            </svg>
        </el-button>
        <template v-if="inPWA">
            <el-button @click="router.back()"
                       style="width: 30px;height: 30px;margin-right: 2px;margin-left: 0;" link>
                <el-icon>
                    <ArrowLeftBold/>
                </el-icon>
            </el-button>
            <el-button @click="router.forward()"
                       style="width: 30px;height: 30px;margin-right: 8px;margin-left: 0;" link>
                <el-icon>
                    <ArrowRightBold/>
                </el-icon>
            </el-button>
        </template>
        <el-scrollbar style="margin-right: 8px;margin-left: 12px">
            <div class="init-animate"
                 style="height: 30px;margin-top: 2px;display: flex;flex-direction: row;align-items: center">
                <el-breadcrumb style="flex-shrink: 0;" :separator-icon="ArrowRight">
                    <!--suppress JSValidateTypes -->
                    <el-breadcrumb-item :to="{ name: 'home' }" style="line-height: 14px;">
                        <img :src="getIcon()" alt="" width="14" height="14" style="float: left;margin-right: 4px;"/>
                        checkIn
                    </el-breadcrumb-item>
                    <TransitionGroup name="breadcrumb-item">
                        <!--suppress JSValidateTypes -->
                        <el-breadcrumb-item :key="pathItem.name" v-for="(pathItem,$index) in breadcrumbPathArray"
                                            :to="$index!==breadcrumbPathArray.length-1?{path: pathItem.path}:undefined">
                            {{ pathItem.name }}
                        </el-breadcrumb-item>
                    </TransitionGroup>
                </el-breadcrumb>
            </div>
        </el-scrollbar>
        <template v-if="!mobile">
            <div class="flex-blank-1" style="-webkit-app-region: drag;height: 36px;min-width: 20px"></div>
            <el-button @click="router.push({name:'account-base'})" text
                       style="margin-right: 4px;padding: 4px;transition: 200ms var(--ease-in-out-quint)">
                <el-avatar shape="circle" size="small" :src="getAvatarUrlOf(user.qq)"
                           style="margin-right: 4px"></el-avatar>
                <el-text size="large">{{ user.name }}</el-text>
                <el-text size="large" type="info" style="margin-left: 4px;">{{ user.qq }}</el-text>
            </el-button>
        </template>
    </div>
</template>

<style scoped>
@import '../../assets/base.css';

#top-bar {
    height: 36px;
    display: flex;
    flex-direction: row;
    align-items: center;
    z-index: 3;
    margin-left: env(titlebar-area-x, 0);
    width: env(titlebar-area-width, 100%);
}

#menuIcon rect {
    fill: var(--el-text-color-primary);
    transition: 200ms var(--ease-in-out-quint);
}

button:hover #menuIcon rect {
    fill: var(--el-text-color-secondary);
}

/*noinspection CssUnusedSymbol*/
.breadcrumb-item-enter-active {
    transition: all 0.3s var(--ease-out-quint);
}

/*noinspection CssUnusedSymbol*/
.breadcrumb-item-leave-active {
    transition: all 0.3s var(--ease-in-quint);
}

/*noinspection CssUnusedSymbol*/
.breadcrumb-item-enter-from,
.breadcrumb-item-leave-to {
    margin-left: -10px;
    width: 0;
    overflow: visible;
    word-break: keep-all;
    opacity: 0;
    filter: blur(12px);
}
</style>

<style>
.el-breadcrumb-inner {
    line-height: 14px !important;
}
</style>