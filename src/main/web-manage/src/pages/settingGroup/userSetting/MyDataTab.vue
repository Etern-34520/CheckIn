<script setup>
import {useRoute} from "vue-router";
import MyLikeQuestionsView from "@/pages/settingGroup/userSetting/MyLikeQuestionsView.vue";
import MyDislikeQuestionsView from "@/pages/settingGroup/userSetting/MyDislikeQuestionsView.vue";
import MyQuestionsView from "@/pages/settingGroup/userSetting/MyQuestionsView.vue";
import router from "@/router/index.js";

const route = useRoute();
/*watch(() => route.params.id , () => {
    activeName.value = route.params.id;
/!*
    switch (route.params.id) {
        case 'questions':
            activeName.value = '我的题目';
            break;
        case 'like-questions':
            activeName.value = '我点赞的题目';
            break;
        case 'dislike-questions':
            activeName.value = '我点踩的题目';
            break;
    }
*!/
})*/
const activeName = ref(route.query.tab);

const replaceHistory = (tab) => {
    router.replace({
        query: {
            tab: tab.paneName
        }
    });
}
</script>

<template>
    <div class="panel" style="padding: 20px 32px">
        <el-tabs v-model="activeName" class="demo-tabs" @tabClick="replaceHistory">
            <el-tab-pane label="我的题目" name="my-questions">
                <transition name="tab" mode="out-in">
                    <my-questions-view/>
                </transition>
            </el-tab-pane>
            <el-tab-pane label="我点赞的题目" name="like-questions">
                <transition name="tab" mode="out-in">
                    <my-like-questions-view/>
                </transition>
            </el-tab-pane>
            <el-tab-pane label="我点踩的题目" name="dislike-questions">
                <transition name="tab" mode="out-in">
                    <my-dislike-questions-view/>
                </transition>
            </el-tab-pane>
        </el-tabs>
    </div>
</template>

<style scoped>

</style>