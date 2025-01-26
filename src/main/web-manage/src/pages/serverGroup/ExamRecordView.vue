<script setup>
import 'splitpanes/dist/splitpanes.css'
import ResponsiveSplitpane from "@/components/common/ResponsiveDoubleSplitpane.vue";

const loading = ref(false);
const dateRange1 = ref([]);
</script>

<template>
    <responsive-splitpane ref="responsiveSplitpane" :left-loading="loading" show-left-label="记录列表">
        <template #left>
            <div style="display: flex;flex-direction: row;">
                <el-date-picker type="daterange"
                                unlink-panels
                                range-separator="~"
                                start-placeholder="1"
                                end-placeholder="2"
                                style="flex:1"
                                v-model="dateRange1"/>
            </div>
            {{dateRange1}}
        </template>
        <template #right>
            <div></div>
        </template>
    </responsive-splitpane>
</template>

<style scoped>

</style>