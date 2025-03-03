<script setup>
import WebSocketConnector from "@/api/websocket.js";
import router from "@/router/index.js";
import Waterfall from "@/components/common/Waterfall.vue";
import ExamRecordItem from "@/pages/serverGroup/examRecord/ExamRecordItem.vue";

const examRecords = ref([]);
const loading = ref(true);

const props = defineProps({
    qq: {
        type: Number,
        required: true,
    }
})

onBeforeMount(() => {
    try {
        WebSocketConnector.send({
            type: "getExamRecordsByQQ",
            qq: props.qq
        }).then((response) => {
            loading.value = false;
            examRecords.value = response.examRecords;
        }, (error) => {
            loading.value = false;
        });
    } catch (e) {
        loading.value = false;
    }
});
</script>

<template>
    <div style="display: flex;flex-direction: column;justify-items: stretch">
        <el-scrollbar v-loading="loading">
            <waterfall :data="examRecords" :min-row-width="600">
                <template #item="{item,index}">
                    <exam-record-item :record="item" class="clickable"
                                      @click="router.push({name:'exam-record-detail',params: {id:item.id}})"/>
                </template>
            </waterfall>
        </el-scrollbar>
        <!--        {{questions}}-->
    </div>
</template>

<style scoped>

</style>