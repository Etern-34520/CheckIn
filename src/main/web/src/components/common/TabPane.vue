<script setup>
const emit = defineEmits([
    "onSwitchTab"
])

const props = defineProps({
    tabs: {
        type: Object,
        required: true
    }
});

const tabNames = ref([]);

watch(() => props.tabs, (newVal) => {
    tabNames.value = Object.keys(newVal);
}, {immediate: true});
const selectTab = defineModel({
    type: String,
    // default: tabNames[0]
});
const leftToRight = ref(false);
const currentComponent = shallowRef(props.tabs[tabNames.value[0]]);

watch(() => selectTab.value, (newVal, oldValue) => {
    leftToRight.value = tabNames.value.indexOf(oldValue) > tabNames.value.indexOf(newVal);
    nextTick(() => {
        currentComponent.value = props.tabs[newVal];
        emit("onSwitchTab", newVal);
    })
}, {immediate: true});
</script>

<template>
    <div style="display: flex;flex-direction: column">
        <div style="margin-bottom: 8px">
            <el-scrollbar>
                <el-segmented v-model="selectTab" :options="tabNames"/>
            </el-scrollbar>
        </div>
        <div class="slide-switch-base" style="flex: 1;" :class="{'left-to-right':leftToRight}">
            <transition :name="leftToRight?'slide-left-to-right':'slide-right-to-left'">
                <component :is="currentComponent"/>
            </transition>
        </div>
    </div>
</template>

<style scoped>

</style>