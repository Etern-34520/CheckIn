<script setup>
import router from "@/router/index.js";

const page = ref(null);

const transitionName = ref("slide-left-to-right");
const className = ref("");
const stop = router.beforeEach((to, from) => {
    if (to.name === "global-setting-base") {
        className.value = "left-to-right";
        transitionName.value = "slide-left-to-right";
    } else if (from.name === "global-setting-base") {
        className.value = "";
        transitionName.value = "slide-right-to-left";
    } else {
        className.value = "";
        transitionName.value = "blur-scale";
    }
});
onUnmounted(() => {
    stop();
});
</script>

<template>
    <div class="panel" style="flex: 1;width: 0;padding: 32px 32px 0;">
        <div class="slide-switch-base" :class="className">
            <router-view v-slot="{ Component }">
                <transition :name="transitionName">
                    <component ref="page" :is="Component"/>
                </transition>
            </router-view>
        </div>
    </div>
</template>

<style scoped>/*
.transition-page-enter-active,
.transition-page-leave-active {
    transition: 400ms var(--ease-in-out-quint);
}

.transition-page-enter-from {
    transform: translateX(calc(-100% - 200px));
}

.transition-page-leave-to {
    transform: translateX(calc(100% + 200px));
}
*/</style>