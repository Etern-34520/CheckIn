<script setup>
const props = defineProps({
    minRowWidth: {
        type: Number,
        default: 300
    },
    data: {
        required: true
    },
    even: {
        default: false
    }
});

const waterfall = ref();
const rowCount = ref(1);
let observer;

onMounted(() => {
    nextTick(() => {
        if (waterfall.value) {
            observer = new ResizeObserver((entries) => {
                for (const entry of entries) {
                    const width = entry.contentRect.width;
                    let value1 = Math.max(1, Math.min(Math.floor(width / props.minRowWidth), props.data.length));
                    if (props.even && value1 !== 1 && value1 % 2 !== 0) {
                        value1 = value1 - 1;
                    }
                    rowCount.value = value1;
                }
            });
            observer.observe(waterfall.value);
        }
    });
});
onDeactivated(() => {
    observer.disconnect();
})
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