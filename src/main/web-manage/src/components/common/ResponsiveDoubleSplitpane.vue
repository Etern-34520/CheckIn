<script setup>
import {ArrowLeftBold, List} from "@element-plus/icons-vue";
import router from "@/router/index.js";
import UIMeta from "@/utils/UI_Meta.js";
import {Splitpanes, Pane} from 'splitpanes'

defineProps({
    leftLoading: {
        type: Boolean,
        default: false,
    },
    RightLoading: {
        type: Boolean,
        default: false,
    },
    showLeftLabel: {
        type: String,
        required: true,
    }
});

const showLeft = () => {
    mobileShowLeftView.value = true;
}

const hideLeft = () => {
    mobileShowLeftView.value = false;
}

defineExpose({
    showLeft:showLeft,
    hideLeft:hideLeft
})

const mobile = ref(false);
const mobileShowLeftView = ref(Object.keys(router.currentRoute.value.params).length === 0);
const noTreeViewTransition = ref(true);

watch(UIMeta.mobile, (val) => {
    mobile.value = val;
    if (val) {
        noTreeViewTransition.value = true;
        setTimeout(() => {
            noTreeViewTransition.value = false;
        }, 100);
    }
}, {immediate: true});
</script>

<template>
    <div :class="{mobile:mobile}" style="display: flex;flex-direction: row;align-content: stretch;align-items: stretch">
        <div class="mobile-view-mask" v-if="mobile&&mobileShowLeftView" @click="mobileShowLeftView = false"></div>
        <splitpanes style="flex: 1">
            <pane :min-size="mobile?0:20" :size="mobile?0:30">
                <div class="panel left-view" :class="{show:mobileShowLeftView,'no-transition':noTreeViewTransition}"
                     style="display: flex;" v-loading="leftLoading">
                    <div style="flex: none;height: 28px" v-if="mobile">
                        <el-button @click="mobileShowLeftView = false" link><el-icon><arrow-left-bold/></el-icon>返回</el-button>
                    </div>
                    <slot name="left"/>
                </div>
            </pane>
            <pane :min-size="mobile?100:50" :size="mobile?100:70">
                <div class="panel"
                     style="display:flex;flex-direction:row;flex: none;overflow:hidden;align-items:center;height: 24px;padding: 4px;margin-bottom: 4px!important;"
                     v-if="mobile">
                    <el-button @click="mobileShowLeftView = true" link>
                        <el-icon>
                            <list/>
                        </el-icon>
                        {{ showLeftLabel }}
                    </el-button>
                    <slot name="right-top"/>
                </div>
                <div class="panel" style="padding: 0;display: flex;flex-direction: column" v-loading="RightLoading">
                    <slot name="right"/>
                </div>
            </pane>
        </splitpanes>
    </div>
</template>

<style scoped>

.left-view {
    position: relative;
}

.mobile .left-view {
    position: fixed;
    backdrop-filter: blur(32px) brightness(0.5);
    height: 100%;
    width: 80%;
    z-index: 2003;
    transform: translateX(calc(-100% - 500px));
    transition: transform 300ms var(--ease-in-out-quint);
}

.mobile .left-view.no-transition {
    transition: none;
}

.mobile .left-view.show {
    transform: translateX(0);
}

.mobile-view-mask {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
    z-index: 2002;
}
</style>

<style>
.mobile > .splitpanes .splitpanes__splitter {
    display: none !important;
}
</style>