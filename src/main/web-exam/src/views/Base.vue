<script setup>
import {RouterView} from "vue-router";

const {proxy} = getCurrentInstance();
const facadeData = ref({});
const gradingData = ref({});
const drawingData = ref({});
const partitions = ref({});
const loading = ref(true);
const error = ref(false);

const getData = () => {
    loading.value = true;
    error.value = false;
    const phrase = proxy.$cookies.get("phrase");
    if (phrase) {
        if (phrase === "generate") {//TODO
            proxy.$http.get("examData").then((response) => {
                facadeData.value = response.facadeData.data;
                gradingData.value = response.gradingData.data;
                drawingData.value = response.drawingData.data;
                partitions.value = response.partitions;
                loading.value = false;
            }, () => {
                loading.value = false;
                error.value = true;
            });
        } else if (phrase === "exam") {
            proxy.$http.get("examData").then((response) => {
                facadeData.value = response.facadeData.data;
                gradingData.value = response.gradingData.data;
                drawingData.value = response.drawingData.data;
                partitions.value = response.partitions;
                loading.value = false;
            }, () => {
                loading.value = false;
                error.value = true;
            });
        }
    } else {
        proxy.$http.get("examData").then((response) => {
            facadeData.value = response.facadeData.data;
            gradingData.value = response.gradingData.data;
            drawingData.value = response.drawingData.data;
            partitions.value = response.partitions;
            loading.value = false;
        }, () => {
            loading.value = false;
            error.value = true;
        });
    }
}
getData();
</script>

<template>
    <div style="display: flex;flex-direction: column;width:100vw;height:100vh">
        <el-scrollbar v-loading="loading">
            <div class="center-base">
                <template v-if="!loading && !error">
                    <router-view v-slot="{ Component }">
                        <transition name="route-page" mode="out-in">
                            <component :is="Component" :facade-data="facadeData" :grading-data="gradingData"
                                       :drawing-data="drawingData" :partitions="partitions"/>
                        </transition>
                    </router-view>
                </template>
                <div v-else-if="error"
                     style="display:flex;flex-direction: column;min-width: 100vw;min-height: 100vh;align-items: center;justify-content: center">
                    <el-empty description="加载失败"></el-empty>
                    <el-button link type="primary" @click="getData" size="large">重试</el-button>
                </div>
            </div>
        </el-scrollbar>
    </div>
</template>

<style scoped>
/*noinspection CssUnusedSymbol*/
.route-page-enter-active {
    transition: all 0.4s var(--ease-out-quint);
}

/*noinspection CssUnusedSymbol*/
.route-page-leave-active {
    transition: all 0.4s var(--ease-in-quint);
}

/*noinspection CssUnusedSymbol*/
.route-page-enter-from, .route-page-leave-to {
    filter: blur(32px);
    opacity: 0;
}

/*noinspection CssUnusedSymbol*/
.route-page-enter-from {
    scale: 0.95;
}

/*noinspection CssUnusedSymbol*/
.route-page-leave-to {
    scale: 1.05;
}
</style>