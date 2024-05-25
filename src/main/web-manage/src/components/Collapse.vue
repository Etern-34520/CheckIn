<script setup>
import {ArrowDownBold} from "@element-plus/icons-vue";

const showContent = ref(false);
const expanded = ref(false);
const borderChanged = ref(false);
const props = defineProps({
    expanded: {
        type: Boolean,
        default: false,
    }
});
onMounted(() => {
    if (props.expanded) {
        expanded.value = props.expanded;
    }
});
const switchExpand = () => {
    expanded.value = !expanded.value;
}

function expand() {
    expanded.value = true;
}

function shrink() {
    expanded.value = false;
}

watch(() => expanded.value, (newVal, oldVal) => {
    if (newVal) {
        showContent.value = true;
        setTimeout(() => {
            if (expanded.value)
                borderChanged.value = true;
        }, 400);
    } else {
        setTimeout(() => {
            if (!expanded.value)
                borderChanged.value = false;
        }, 100);
        setTimeout(() => {
            if (!expanded.value)
                showContent.value = false;
        }, 400);
    }
});
</script>

<template>
    <div class="collapse" :class="{expanded:expanded}">
        <div class="collapse-title" :class="{'border-changed':borderChanged}">
            <div class="collapse-title-inner">
                <slot name="title"/>
            </div>
            <el-button @click="switchExpand" link class="action-button">
                <transition name="pointer">
                    <span class="action-button-pointer">
                        <el-icon>
                            <ArrowDownBold/>
                        </el-icon>
                    </span>
                </transition>
            </el-button>
        </div>
        <div class="collapse-content">
            <div class="collapse-content-inner" v-show="showContent">
                <slot name="content"/>
            </div>
        </div>
    </div>
</template>

<style scoped>
.collapse {
    display: grid;
    grid-template-rows: 1fr 0fr;
    transition: 400ms var(--ease-in-out-bounce) 200ms;
}

.collapse.expanded {
    grid-template-rows: 1fr 1fr;
    transition: 400ms var(--ease-in-out-bounce);
}

.collapse-title {
    padding: 4px 8px;
    background: var(--panel-bg-color) linear-gradient(180deg, rgba(255, 255, 255, 0.09) 0, transparent 2px calc(100% - 3px), rgba(0, 0, 0, 0.2) 100%);
    border-radius: 4px;
    display: flex;
    flex-direction: row;
}

.collapse-title > .collapse-title-inner {
    flex: 1 !important;
}

.collapse.expanded .collapse-title {
    background: var(--panel-bg-color) linear-gradient(180deg, rgba(255, 255, 255, 0.09) 0, transparent 2px 100%);
}

.collapse-title.border-changed {
    border-radius: 4px 4px 0 0;
}

.action-button-pointer {
    transition: transform 500ms var(--ease-in-bounce-1);
}

.collapse-title > .action-button {
    aspect-ratio: 1;
}

.collapse-content {
    display: flex;
    flex-direction: column;
    align-items: stretch;
    justify-content: end;
    min-height: 0;
    max-height: 0;
    overflow: hidden;
    transition: 250ms var(--ease-in-quint) 0ms;
}

.collapse.expanded .collapse-content {
    max-height: 100%;
    border-radius: 0 0 4px 4px;
    background: var(--panel-bg-color) linear-gradient(180deg, transparent 0px calc(100% - 3px), rgba(0, 0, 0, 0.2) 100%);
    transition: 250ms var(--ease-out-quint) 200ms;
}

.collapse-content-inner {
    padding: 4px 8px;
    width: 100%;
}

.collapse.expanded .action-button-pointer {
    transform: rotate(180deg);
}
</style>