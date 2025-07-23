<script setup>
import ArrayItemViewer from "@/components/viewer/ArrayItemViewer.vue";

const props = defineProps({
    data: {
        type: Object,
        required: true
    },
    nameStyle: {
        type: String,
        default: 'width: max(10vw,120px);'
    }
});

</script>

<template>
    <div class="object-view" style="display: flex;flex-direction: column">
        <div style="display: flex;flex-direction: row;flex-wrap: wrap;" v-for="(field,name,index) in data">
            <el-text class="object-view-name" type="info" style="word-break: break-all;align-self: start;margin-bottom: 8px" :style="nameStyle">
                {{name}}
            </el-text>
            <array-item-viewer class="object-view-element" v-if="field instanceof Array" :data="field" style="margin-bottom: 8px"/>
            <object-field-viewer class="object-view-element" v-else-if="typeof field === 'object' && Boolean(field)" :data="field" style="margin-bottom: 8px"/>
            <el-text class="object-view-element" v-else-if="field !== null && field !== undefined" style="flex: 1;word-break: break-all;align-self: start;margin-bottom: 8px">
                {{field}}
            </el-text>
            <el-text class="object-view-element" v-else type="info" style="margin-bottom: 8px">[null]</el-text>
        </div>
    </div>
</template>

<style>
.object-view .object-view .object-view-name {
    margin-left: 16px;
}
.object-view .object-view .object-view-element {
    margin-left: 32px;
}
</style>