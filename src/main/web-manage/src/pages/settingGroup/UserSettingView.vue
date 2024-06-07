<script setup>
import UserDataInterface from "@/data/UserDataInterface.js";
import getAvatarUrlOf from "@/utils/Avatar.js";
import {ArrowRightBold, Finished} from "@element-plus/icons-vue";
import router from "@/router/index.js";
import Like from "@/components/icons/Like.vue";
import DisLike from "@/components/icons/DisLike.vue";
import HarmonyOSIcon_Quit from "@/components/icons/HarmonyOSIcon_Quit.vue";
import HarmonyOSIcon_Remove from "@/components/icons/HarmonyOSIcon_Remove.vue";
import HarmonyOSIcon_Rename from "@/components/icons/HarmonyOSIcon_Rename.vue";

// const user = UserDataInterface.getCurrentUser();

const groups = [
    {
        name: "我的",
        items: [
            {
                name: "我的题目", description: "", icon: Finished, action: () => {
                    router.push({
                        name: "my-data",
                        query: {tab: "my-questions"}
                    })
                }
            },
            {
                name: "我点赞的题目", description: "", icon: Like, action: () => {
                    router.push({
                        name: "my-data",
                        query: {tab: "my-likes"}
                    })
                }
            },
            {
                name: "我点踩的题目", description: "", icon: DisLike, action: () => {
                    router.push({
                        name: "my-data",
                        query: {tab: "my-dislikes"}
                    })
                }
            },
        ]
    },
    {
        name: "操作",
        items: [
            {
                name: "修改用户名",
                description: "",
                icon: HarmonyOSIcon_Rename,
                path: "/manage/user-setting/change-name/"
            },
            {
                name: "修改密码",
                description: "",
                icon: HarmonyOSIcon_Rename,
                path: "/manage/user-setting/change-password/"
            },
            {name: "退出", description: "", icon: HarmonyOSIcon_Quit, action: UserDataInterface.logout},
            {name: "删除账户", description: "", icon: HarmonyOSIcon_Remove, path: "/manage/user-setting/delete/"},
        ]
    }
]

const user = UserDataInterface.getCurrentUser();
</script>

<template>
    <div class="panel" style="display: flex;flex-direction: column;padding: 20px 32px;justify-items: stretch">
        <div style="display: flex;flex-direction: row;margin-bottom: 28px">
            <el-avatar shape="circle" style="width: 84px;height: 84px;margin-right: 8px;"
                       :src="getAvatarUrlOf(user.qq)"/>
            <div style="display:flex;flex-direction: column;justify-content: center;">
                <el-text style="font-size: 24px;align-self: baseline">{{ user.name }}</el-text>
                <el-text style="font-size: 16px;align-self: baseline" type="info">{{ user.qq }}</el-text>
                <el-text style="font-size: 16px;align-self: baseline" type="info">{{ user.role }}</el-text>
            </div>
        </div>
        <div v-for="group of groups" style="display: flex;flex-direction: column">
            <el-text style="font-size: 20px;align-self: baseline;margin-bottom: 4px">{{ group.name }}</el-text>
            <div v-for="item of group.items" :key="item.name" @click="item.action()"
                 class="panel-1 clickable" style="display: flex;padding: 16px 24px;align-items: center">
                <el-icon size="20" style="margin-right: 8px">
                    <component :is="item.icon"/>
                </el-icon>
                <div style="display:flex;flex-direction: column;justify-content: center;">
                    <el-text style="font-size: 16px;align-self: baseline">{{ item.name }}</el-text>
                    <el-text style="font-size: 12px;align-self: baseline" type="info">{{ item.description }}</el-text>
                </div>
                <div class="flex-blank-1"></div>
                <el-icon>
                    <ArrowRightBold/>
                </el-icon>
            </div>
        </div>
    </div>
</template>

<style scoped>

</style>