<script setup>
import 'splitpanes/dist/splitpanes.css'
import ResponsiveSplitpane from "@/components/common/ResponsiveDoubleSplitpane.vue";

const loading = ref(false);

const fromDate = new Date();
fromDate.setUTCDate(fromDate.getUTCDate()-7);
fromDate.setUTCHours(16,0,0,0);

const toDate = new Date();
toDate.setUTCDate(toDate.getUTCDate());
toDate.setUTCHours(16,0,0,0);

const from = fromDate.toISOString();
const to = toDate.toISOString();

const dateRange = ref([from,to]);
</script>

<template>
    <responsive-splitpane ref="responsiveSplitpane" show-left-label="记录列表">
        <template #left>
            <div style="display: flex;flex-direction: row;">
                <el-date-picker type="daterange"
                                unlink-panels
                                range-separator="~"
                                start-placeholder="1"
                                end-placeholder="2"
                                style="flex:1"
                                v-model="dateRange"/>
            </div>
            <div v-loading="loading">
                {{dateRange}}
            </div>
        </template>
        <template #right>
            <div></div>
        </template>
    </responsive-splitpane>
</template>

<style scoped>

</style>