<script setup>
import {ArrowLeftBold} from "@element-plus/icons-vue";
import UserQuestionsView from "@/components/user/UserQuestionsView.vue";
import router from "@/router/index.js";
import UserDataInterface from "@/data/UserDataInterface.js";

const qq = ref(Number(router.currentRoute.value.params.id));
const name = ref("");
UserDataInterface.getUserOfQQAsync(qq.value).then((user) => {
    name.value = user.name;
});
</script>

<template>
    <div>
        <div style="display: flex;flex-direction: row;margin-bottom: 8px;">
            <el-button style="margin-right: 16px" @click="router.push({name: 'user-detail'})">
                <el-icon>
                    <ArrowLeftBold/>
                </el-icon>
                <el-text>返回</el-text>
            </el-button>
            <el-text style="font-size: 24px">{{ name }} 的题目</el-text>
        </div>
        <user-questions-view style="margin-top: 4px;" :qq="qq"/>
    </div>
</template>

<style scoped>

</style>