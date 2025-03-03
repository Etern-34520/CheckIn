<script setup>
import {useRoute} from "vue-router";
import MyLikeQuestionsView from "@/pages/settingGroup/userSetting/MyLikeQuestionsView.vue";
import MyDislikeQuestionsView from "@/pages/settingGroup/userSetting/MyDislikeQuestionsView.vue";
import MyQuestionsView from "@/pages/settingGroup/userSetting/MyQuestionsView.vue";
import router from "@/router/index.js";
import TabPane from "@/components/common/TabPane.vue";
import MyExamRecordsView from "@/pages/settingGroup/userSetting/MyExamRecordsView.vue";


const selectedTabName = ref("我的题目");

const CnEnMapping = {
    "我的答题记录": "my-exam-records",
    "我的题目": "my-questions",
    "我点赞的题目": "my-like-questions",
    "我点踩的题目": "my-dislike-questions"
}

const EnCnMapping = {
    "my-exam-records" : "我的答题记录",
    "my-questions": "我的题目",
    "my-like-questions": "我点赞的题目",
    "my-dislike-questions": "我点踩的题目"
}

if (router.currentRoute.value.query.tab) {
    selectedTabName.value = EnCnMapping[router.currentRoute.value.query.tab];
}

const onSwitchTab = (tab) => {
    router.replace({
        name: "my-data",
        query: {tab: CnEnMapping[tab]}
    });
}

const tabs = {
    '我的答题记录': MyExamRecordsView,
    '我的题目': MyQuestionsView,
    '我点赞的题目': MyLikeQuestionsView,
    '我点踩的题目': MyDislikeQuestionsView
}
/*
const route = useRoute();
const tabsName = [
    "我的题目",
    "我点赞的题目",
    "我点踩的题目"
]

const activeTabComponents = {
    "我的题目": MyQuestionsView,
    "我点赞的题目": MyLikeQuestionsView,
    "我点踩的题目": MyDislikeQuestionsView
}

const activeCNValue = ref("我的题目");

const switchType = ref(false);
watch(() => activeCNValue.value, (newVal,oldValue) => {
    setTimeout(() => {
        switchType.value = tabsName.indexOf(oldValue) < tabsName.indexOf(newVal) ;//FIXME
    },400);
},{immediate: true});*/
/*
watch(activeCNValue, (value) => {
    activeName.value = CnEnMapping[value];
});
*/
/*const activeName = ref(route.query.tab);
const EnCnMapping = {
    "my-questions": "我的题目",
    "my-like-questions": "我点赞的题目",
    "my-dislike-questions": "我点踩的题目"
}

const replaceHistory = (tab) => {
    router.replace({
        query: {
            tab: tab.paneName
        }
    });
}*/
</script>

<template>
    <tab-pane :tabs="tabs" @on-switch-tab="onSwitchTab" v-model="selectedTabName"/>
</template>

<style scoped>

</style>