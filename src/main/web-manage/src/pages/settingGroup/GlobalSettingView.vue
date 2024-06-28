<script setup>
import {
    ArrowRightBold,
    Check,
    CircleCheck,
    Files,
    MoreFilled,
    Postcard,
    Upload
} from "@element-plus/icons-vue";
import LinkPanel from "@/components/common/LinkPanel.vue";

const groups = [
    {
        name: "答题设置",
        items: [
            {
                name: "首页自定义", description: "图标 问候语 描述", icon: Postcard, action: () => {
                }
            },
            {
                name: "抽取设置", description: "题量 分区数 必选分区 抽取策略 补足策略", icon: Files, action: () => {
                }
            },
            {
                name: "批改设置", description: "分数线 分级 消息", icon: Check, action: () => {
                }
            },
            {
                name: "高级设置", description: "外部API 自动创建用户", icon: MoreFilled, action: () => {
                }
            }
        ]
    },
    {
        name: "题库设置",
        items: [
            {
                name: "实时校验", description: "错误 警告" , icon: CircleCheck, action: () => {
                }
            },
            {
                name: "上传限制", description: "分区题目数上限 题目图片上限 题目内容长度上限 题组子题目上限", icon: Upload, action: () => {
                }
            },
        ]
    }
]
</script>

<template>
    <div class="panel" style="padding: 32px;height: 100%">
        <div v-for="group of groups" style="display: flex;flex-direction: column;margin-bottom: 16px">
            <el-text style="font-size: 20px;align-self: baseline;margin-bottom: 8px">{{ group.name }}</el-text>
            <link-panel @click="item.action()" v-for="item of group.items" :key="item.name" :description="item.description" :name="item.name" :icon="item.icon"/>
        </div>
    </div>
</template>

<style scoped>

</style>