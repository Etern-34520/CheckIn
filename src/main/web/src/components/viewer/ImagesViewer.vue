<script setup>
import {ArrowLeft, ArrowLeftBold, ArrowRight, ArrowRightBold} from "@element-plus/icons-vue";

const props = defineProps({
    images: {
        type: Array,
        required: true
    },
});
const model = defineModel({
    type: Boolean
});
const index = defineModel("index", {
    type: Number
});
const slideWay = ref(0);
const nextIndex = ref(props.index);
watch(() => index.value, (value, oldValue) => {
    if (value > oldValue) slideWay.value = 0;
    else slideWay.value = 1
    setTimeout(() => {
        nextIndex.value = index.value;
    },100);
})
const onKeyDown1 = (event) => {
    if (event.code === "ArrowRight") {
        if (index.value < props.images.length - 1) index.value = index.value + 1;
    } else if (event.code === "ArrowLeft") {
        if (index.value > 0) index.value = index.value - 1;
    } else if (event.code === "Escape") {
        model.value = false;
    }
}
onMounted(() => {
    window.addEventListener("keydown", onKeyDown1);
});
onBeforeUnmount(() => {
    window.removeEventListener("keydown", onKeyDown1);
});
</script>

<template>
    <transition name="images-viewer" mode="out-in">
        <div class="images-viewer" v-if="model" @click="model=false">
            <el-empty v-if="!images[index]"/>
            <div v-else style="display: flex;flex: 1;align-items: center">
                <el-button link style="width: 120px;height: 40dvh" :disabled="index===0" @click.stop="index = index-1">
                    <el-icon size="40">
                        <ArrowLeft/>
                    </el-icon>
                </el-button>
                <div style="flex: 1;display: flex;align-items: center;justify-content: center">
                    <div class="slide-switch-base" :class="slideWay?'left-to-right':''">
                        <transition :name="slideWay?'slide-left-to-right':'slide-right-to-left'">
                            <el-image :src="images[nextIndex].url ? images[nextIndex].url : images[nextIndex]"
                                      :key="nextIndex" @click.stop
                                      style="height: calc(100dvh - 60px);" fit="contain"/>
                        </transition>
                    </div>
                    <div class="index-label panel">
                        <el-text>{{ index + 1 }} / {{ images.length }}</el-text>
                    </div>
                </div>
                <el-button link style="width: 120px;height: 40dvh" :disabled="index===images.length-1" @click.stop="index = index+1">
                    <el-icon size="40">
                        <ArrowRight/>
                    </el-icon>
                </el-button>
            </div>
        </div>
    </transition>
</template>

<style scoped>
.images-viewer {
    z-index: 2001;
    position: fixed;
    top: 0;
    right: 0;
    bottom: 0;
    left: 0;
    background: var(--el-mask-color);
    display: flex;
    justify-content: center;
}

.images-viewer-enter-active,
.images-viewer-leave-active {
    transition: 400ms var(--ease-in-out-quint);
}

.images-viewer-enter-from,
.images-viewer-leave-to {
    opacity: 0;
}

.index-label {
    transition: 400ms var(--ease-in-out-quint);
    background-color: var(--popper-bg-color);
    width: 120px;
    height: 40px;
    position: absolute;
    backdrop-filter: blur(32px);
    bottom: 40px;
    display: flex;
    flex-direction: column;
    justify-content: center;
}

.index-label:hover {
    opacity: 0;
}

.slide-left-to-right-enter-from,
.slide-left-to-right-leave-to,
.slide-right-to-left-enter-from,
.slide-right-to-left-leave-to {
    opacity: 0;
}
</style>