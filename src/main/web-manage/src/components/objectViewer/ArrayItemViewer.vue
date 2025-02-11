<script setup>
import ObjectFieldViewer from "@/components/objectViewer/ObjectFieldViewer.vue";

const props = defineProps({
    data: {
        type: Array,
        required: true
    },
    showIndex: {
        type: Boolean,
        default: false
    }
});

</script>

<template>
    <div class="object-view" style="display: flex;flex-direction: column">
        <div style="display: flex;flex-direction: row;flex-wrap: wrap;margin-bottom: 8px" v-for="(item,index) in data">
            <el-text type="info" v-if="showIndex" style="align-self: start;">{{index}}</el-text>
            <array-item-viewer v-if="item instanceof Array" :data="item"/>
            <object-field-viewer v-else-if="typeof item === 'object'" :data="item"/>
            <el-text v-else-if="item" style="flex: 1;word-break: break-all;align-self: start;">
                {{item}}
            </el-text>
            <el-text v-else type="info">[null]</el-text>
        </div>
    </div>
</template>

<style scoped>

</style>