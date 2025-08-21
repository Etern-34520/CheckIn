<script setup>
import {
    Check,
    CircleCheck,
    Files, MoreFilled,
    Postcard,
} from "@element-plus/icons-vue";
import LinkPanel from "@/components/common/LinkPanel.vue";
import Waterfall from "@/components/common/Waterfall.vue";
import router from "@/router/index.js";
import HarmonyOSIcon_InfoCircle from "@/components/icons/HarmonyOSIcon_InfoCircle.vue";
import PermissionInfo from "@/auth/PermissionInfo.js";
import ServerStatusInfo from "@/components/common/ServerStatusInfo.vue";

defineExpose({
    name: "Base"
});

const showGeneratingSetting = ref(false);
const showAdvanceSetting = ref(false);
watch(() => PermissionInfo.permissions.value, () => {
    showGeneratingSetting.value = PermissionInfo.hasPermission('setting', 'get generating setting');
    showAdvanceSetting.value = PermissionInfo.hasPermission('setting', 'get advance setting');
}, {immediate: true, deep: true});

const groups = [
    {
        name: "服务状态"//占位
    }, {
        name: "题库设置",
        items: [
            //TODO
            /*{
                name: "功能", description: "题组 图片 markdown", icon: Open, action: () => {
                }
            },*/
            {
                name: "上传校验", description: "上限 下限 警告 错误", icon: CircleCheck, action: () => {
                    router.push({name: "verification-setting"});
                }
            },
        ]
    }, {
        name: "答题设置",
        items: [
            {
                name: "前台自定义", description: "图标 标题 副标题 描述", icon: Postcard, action: () => {
                    router.push({name: "facade-setting"});
                }
            },
            {
                name: "生成设置",
                show: showGeneratingSetting,
                description: "题量 分值 分区数 必选分区 特殊分区限制 抽取策略 补足策略",
                icon: Files,
                action: () => {
                    router.push({name: "generating-setting"});
                }
            },
            {
                name: "评级设置", description: "分数线 分级 消息 判分标准", icon: Check, action: () => {
                    router.push({name: "grading-setting"});
                }
            }
        ]
    }, {
        name: "其它",
        items: [
            {
                name: "关于",
                description: "使用到的开源项目 Github",
                icon: HarmonyOSIcon_InfoCircle,
                action: () => {
                    router.push({name: "about"});
                }
            },
            {
                name: "高级设置",
                show: showAdvanceSetting,
                description: "请求头IP兼容 外部API Turnstile",
                icon: MoreFilled,
                action: () => {
                    router.push({name: "advance-setting"});
                }
            }
        ]
    }
]
</script>

<template>
    <el-scrollbar>
        <waterfall :data="groups" :min-row-width="700">
            <template #item="{item:group,index}">
                <div v-if="index === 0" style="display: flex;flex-direction: column;margin-bottom: 16px;margin-left: 8px;margin-right: 8px">
                    <el-text style="font-size: 20px;align-self: baseline;margin-bottom: 8px">服务状态</el-text>
                    <server-status-info :display-statuses="['UNAVAILABLE', 'MAY_FAIL', 'FULLY_AVAILABLE']"/>
                </div>
                <div v-else style="display: flex;flex-direction: column;margin-bottom: 16px;margin-left: 8px;margin-right: 8px">
                    <el-text style="font-size: 20px;align-self: baseline;margin-bottom: 8px">{{ group.name }}</el-text>
                    <template v-for="item of group.items">
                        <link-panel @click="item.action()" :key="item.name"
                                    v-if="item.show === undefined?true:item.show.value"
                                    :description="item.description" :name="item.name" :icon="item.icon"/>
                    </template>
                </div>
            </template>
        </waterfall>
    </el-scrollbar>
</template>

<style scoped>

</style>