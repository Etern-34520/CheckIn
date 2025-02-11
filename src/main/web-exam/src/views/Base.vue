<script setup>
import {RouterView} from "vue-router";
import router from "@/router/index.js";

const {proxy} = getCurrentInstance();
const facadeData = ref({});
const gradingData = ref({});
const generatingData = ref({});
const partitions = ref({});
const loading = ref(true);
const error = ref(false);

const getData = () => {
    loading.value = true;
    error.value = false;
    const phrase = proxy.$cookies.get("phrase");
    proxy.$http.get("examData").then((response) => {
        facadeData.value = response.facadeData.data;
        gradingData.value = response.gradingData.data;
        generatingData.value = response.generatingData.data;
        partitions.value = response.partitions;
        console.log(phrase);
        if (phrase === "examining") {
            router.push({name: "examining"}).then(() => {
                loading.value = false;
            });
        } else if (phrase === "generating") {
            router.push({name: "generate"}).then(() => {
                loading.value = false;
            });
        } else if (phrase === "result") {
            router.push({name: "result"}).then(() => {
                loading.value = false;
            });
        } else {
            router.push({name: "facade"}).then(() => {
                loading.value = false;
            });
        }
    }, () => {
        loading.value = false;
        error.value = true;
    })
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
                                       :generating-data="generatingData" :partitions="partitions"/>
                        </transition>
                    </router-view>
                </template>
                <div v-else-if="error"
                     style="display:flex;flex-direction: column;flex: 1;align-items: center;justify-content: center">
                    <el-empty description="加载失败"></el-empty>
                    <el-button link type="primary" class="disable-init-animate" @click="getData" size="large">重试
                    </el-button>
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