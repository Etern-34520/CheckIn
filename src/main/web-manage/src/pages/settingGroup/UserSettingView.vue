<script setup>
import Base from "@/pages/settingGroup/userSetting/Base.vue";

const page = ref(null);

const switchType = ref(false);
onMounted(() => {
    watch(() => page.value, () => {
        setTimeout(() => {
            switchType.value = !(page.value !== null && page.value.name === "Base");
        },400);
    },{immediate: true});
})
</script>

<template>
    <div class="panel" style="padding: 32px;height: 100%">
        <div class="slide-switch-base" :class="{'left-to-right':switchType}">
            <router-view v-slot="{ Component }">
                <transition :name="switchType?'slide-left-to-right':'slide-right-to-left'">
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