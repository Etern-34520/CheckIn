<script setup>
import router from "@/router/index.js";
import UI_Meta from "@/utils/UI_Meta.js";

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

const mobile = UI_Meta.mobile;
</script>

<template>
    <div class="panel global-setting-base" :class="{mobile: mobile}" style="flex: 1;width: 0;">
        <div class="slide-switch-base" :class="className">
            <router-view v-slot="{ Component }">
                <transition :name="transitionName">
                    <component ref="page" :is="Component"/>
                </transition>
            </router-view>
        </div>
    </div>
</template>

<style scoped>
.global-setting-base {
    transition: padding 0.3s var(--ease-in-out-quint);
    padding: 32px 32px 0;
}

.global-setting-base.mobile {
    padding: 12px 12px 0;
}
</style>