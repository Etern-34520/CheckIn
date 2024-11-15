<script setup>
import {
    ArrowRightBold,
    Check,
    CircleCheck,
    Files,
    MoreFilled, Open,
    Postcard,
    Upload
} from "@element-plus/icons-vue";
import LinkPanel from "@/components/common/LinkPanel.vue";
import Waterfall from "@/components/common/Waterfall.vue";
import router from "@/router/index.js";
import HarmonyOSIcon_InfoCircle from "@/components/icons/HarmonyOSIcon_InfoCircle.vue";

defineExpose({
    name: "Base"
});

const groups = [
    {
        name: "答题设置",
        items: [
            {
                name: "首页自定义", description: "图标 问候语 描述", icon: Postcard, action: () => {
                    router.push({name: "facade-setting"});
                }
            },
            {
                name: "抽取设置",
                description: "题量 分区数 必选分区 抽取策略 补足策略",
                icon: Files,
                action: () => {
                    router.push({name: "drawing-setting"});
                }
            },
            {
                name: "批改设置 TODO", description: "分数线 分级 消息", icon: Check, action: () => {
                    router.push({name: "facade-setting"});
                }
            },
            {
                name: "高级设置 TODO", description: "外部API 自动创建用户", icon: MoreFilled, action: () => {
                    router.push({name: "facade-setting"});
                }
            }
        ]
    }, {
        name: "题库设置",
        items: [
            {
                name: "功能 TODO", description: "题组 图片 markdown", icon: Open, action: () => {
                }
            },
            {
                name: "上传校验", description: "上限 下限 警告 错误", icon: CircleCheck, action: () => {
                    router.push({name: "verification-setting"});
                }
            },
        ]
    }, {
        name: "其它",
        items: [
            {
                name: "关于",
                description: "作者 使用到的开源项目 Github",
                icon: HarmonyOSIcon_InfoCircle,
                action: () => {
                    router.push({name: "verification-setting"});
                }
            }
        ]
    }
]
</script>

<template>
    <waterfall :data="groups" :min-row-width="700">
        <template #item="{item:group,index}">
            <div style="display: flex;flex-direction: column;margin-bottom: 16px;margin-left: 8px;margin-right: 8px">
                <el-text style="font-size: 20px;align-self: baseline;margin-bottom: 8px">{{ group.name }}</el-text>
                <link-panel @click="item.action()" v-for="item of group.items" :key="item.name"
                            :description="item.description" :name="item.name" :icon="item.icon"/>
            </div>
        </template>
    </waterfall>
</template>

<style scoped>

</style>