<script setup>
// import {useResizeObserver} from "@vueuse/core";
const props = defineProps({
    minRowWidth: {
        type: Number,
        default: 300
    },
    data: {
        type: Array,
        required: true
    }
});

const waterfall = ref();
const rowCount = ref(1);

/*
useResizeObserver(() => {
    console.log("resize")
});
*/
onMounted(() => {
    // 确保在DOM更新完毕后再获取尺寸
    // setTimeout(() => {
    nextTick(() => {
        if (waterfall.value) {
            const observer = new ResizeObserver((entries) => {
                for (const entry of entries) {
                    const width = entry.contentRect.width;
                    rowCount.value = Math.floor(width / props.minRowWidth);
                }
            });
            observer.observe(waterfall.value);
        }
    });
});
</script>

<template>
    <div ref="waterfall" class="waterfall">
        <div v-if="data.length>0" v-for="i in rowCount">
            <template v-for="(item,index) of data">
                <template v-if="index % rowCount === i-1">
                    <slot name="item" :item="item" :index="index"/>
                </template>
            </template>
        </div>
        <el-empty v-else/>
    </div>
</template>

<style scoped>
.waterfall {
    display: flex;
}
.waterfall > div {
    flex: 1;
}
</style>