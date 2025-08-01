<script setup>
import WebSocketConnector from "@/api/websocket.js";
import {ElMessage} from "element-plus";
import 'md-editor-v3/lib/style.css';
import HarmonyOSIcon_Plus from "@/components/icons/HarmonyOSIcon_Plus.vue";
import {VueDraggable} from "vue-draggable-plus";
import HarmonyOSIcon_Handle from "@/components/icons/HarmonyOSIcon_Handle.vue";
import HarmonyOSIcon_Remove from "@/components/icons/HarmonyOSIcon_Remove.vue";
import GradingLevelCard from "@/pages/manage/settingGroup/globalSetting/GradingLevelCard.vue";
import randomUUIDv4 from "@/utils/UUID.js";
import PermissionInfo from "@/auth/PermissionInfo.js";

const editing = ref(false);
const data = ref({});
const extraData = ref();
const loading = ref(true);
const loadingError = ref(false);
const errors = ref([])
const hasError = ref(false);
let backup = {};
let backupJSON;
const cancel = () => {
    data.value = backup;
    editing.value = false;
}
const startEditing = () => {
    backupJSON = JSON.stringify(data.value)
    backup = JSON.parse(backupJSON);
    editing.value = true;
}
const finishEditing = () => {
    editing.value = false;
    if (backupJSON !== JSON.stringify(data.value)) {
        WebSocketConnector.send({
            type: "saveGradingSetting",
            data: {
                data: data.value
            }
        }).then(() => {
            ElMessage({
                type: "success", message: "保存成功"
            })
        }, (err) => {
            ElMessage({
                type: "error", message: "保存失败"
            })
        })
    }
}
const getData = () => {
    loading.value = true;
    loadingError.value = false;
    WebSocketConnector.send({
        type: "getGradingSetting",
    }).then((response) => {
        loading.value = false;
        data.value = response.data.data;
        if (!data.value.levels) data.value.levels = [];
        if (!data.value.splits || data.value.splits.length === 0) data.value.splits = [0];
        // if (!data.value.questionScore) data.value.questionScore = 5;
        extraData.value = response.data.extraData;
    }, (err) => {
        ElMessage({
            type: "error", message: "获取设置失败"
        });
        loading.value = false;
        loadingError.value = true;
    });
}
getData();

const predefine = ["#F56C6C", "#E6A23C", "#67C23A"];
const addLevel = () => {
    const examFullScore = extraData.value.questionScore * extraData.value.questionAmount;
    if (examFullScore === 0) {
        ElMessage({
            type: "error",
            message: "没有可用题目"
        })
    } else {
        if (data.value.splits.length < data.value.levels.length + 1) {
            const number = (examFullScore + data.value.splits[data.value.splits.length - 1]) / 2;
            data.value.splits.push(number);
        }
        data.value.levels.push({
            id: randomUUIDv4(),
            name: "new level",
            colorHex: predefine[data.value.levels.length],
            description: "",
            message: "",
            creatingUserStrategy: "NOT_CREATE"
        });
    }
}

const removeLevel = (index) => {
    data.value.levels.splice(index, 1);
    data.value.splits.splice(index, 1);
    data.value.splits[0] = 0;
}

const unwatch = watch(() => errors.value, () => {
    hasError.value = errors.value.find(item => item === true);
}, {deep: true});

onUnmounted(() => {
    unwatch();
})
</script>

<template>
    <div style="display: flex;flex-direction: column;">
        <div style="display: flex;flex-direction: row;flex-wrap: wrap">
            <el-text style="align-self:baseline;font-size: 24px;margin-right: 32px;">评级设置</el-text>
            <div style="display: flex;"
                 v-if="PermissionInfo.hasPermission('setting','save grading setting')">
                <transition-group name="blur-scale">
                    <el-button-group key="button-group" style="margin: 2px 24px 2px 0;">
                        <transition-group name="blur-scale">
                            <el-button class="disable-init-animate" style="margin-right: 4px;"
                                       @click="editing ? finishEditing():startEditing()"
                                       :disabled="loading || loadingError || hasError" key="edit">
                                {{ editing ? '完成' : '编辑' }}
                            </el-button>
                            <el-button class="disable-init-animate"
                                       @click="cancel" v-if="editing" key="cancel">
                                取消
                            </el-button>
                        </transition-group>
                    </el-button-group>
                    <el-button class="disable-init-animate" key="action" v-if="editing"
                               style="margin-left: 4px;" link @click="addLevel" :icon="HarmonyOSIcon_Plus">
                        添加等级
                    </el-button>
                </transition-group>
            </div>
        </div>
        <div style="display: flex;flex-direction: column;flex:1;height:0;max-width: 1080px;width: min(95%, 1080px);align-self: center">
            <div v-if="!loading && !loadingError" style="display: flex;flex-wrap: wrap;margin-top: 20px;">
                <div style="display: flex;margin-right: 32px;align-items: center">
                    <el-text type="info" style="margin-right: 16px;">题数</el-text>
                    <el-text>{{ extraData.questionAmount }}</el-text>
                </div>
                <div style="display: flex;margin-right: 32px;align-items: center">
                    <el-text type="info" style="margin-right: 16px;">每题分值</el-text>
                    <el-text>{{ extraData.questionScore }}</el-text>
                </div>
                <div style="display: flex;margin-right: 32px;align-items: center">
                    <el-text type="info" style="margin-right: 16px;">总分值</el-text>
                    <el-text>{{ extraData.questionScore * extraData.questionAmount }}</el-text>
                </div>
            </div>
            <div class="score-bar" style="margin-bottom: 16px;" v-if="!loading && !loadingError">
                <div v-for="(level,$index) of data.levels"
                     :style="{
                background: level.colorHex,
                flex: data.splits[$index+1] ? data.splits[$index+1] : extraData.questionScore * extraData.questionAmount - data.splits[$index]
                }"
                     style="height: 6px"></div>
            </div>
            <el-scrollbar style="flex: 1" v-loading="loading">
                <div style="display: flex;flex-direction: column;align-items: stretch">
                    <transition name="blur-scale" mode="out-in">
                        <div v-if="!loading && !loadingError"
                             style="display:flex;flex-direction:column;flex: 1">
                            <VueDraggable
                                    ref="draggable"
                                    v-model="data.levels"
                                    :animation="150"
                                    :disabled="!editing"
                                    ghostClass="ghost"
                                    handle=".handle">
                                <transition-group name="slide-hide">
                                    <div class="slide-hide-base" style="display: grid;margin-top: 8px;"
                                         v-for="(level,$index) of data.levels" :key="level.id">
                                        <div style="display: flex;padding-right: 16px;"
                                             class="panel-1 disable-init-animate ">
                                            <div :style="{background: data.levels[$index].colorHex}"
                                                 style="width: 4px;align-self: stretch;border-radius: 4px 0 0 4px"></div>
                                            <grading-level-card :disabled="!editing" v-model="data.levels[$index]"
                                                                v-model:split="data.splits"
                                                                v-model:error="errors[$index]"
                                                                :min="0"
                                                                :max="extraData.questionScore * extraData.questionAmount"
                                                                :predefine="predefine"
                                                                :index="$index">

                                                <div class="handle"
                                                     style="width: 32px;height: 32px;display: flex;align-items: center;justify-content: center;margin-right: 16px"
                                                     :style="{cursor: editing?'grab':'not-allowed'}">
                                                    <HarmonyOSIcon_Handle :size="24"/>
                                                </div>
                                                <transition name="blur-scale">
                                                    <el-button class="disable-init-animate"
                                                               style="min-height: 32px;"
                                                               v-show="editing" link
                                                               @click="removeLevel($index)">
                                                        <HarmonyOSIcon_Remove/>
                                                    </el-button>
                                                </transition>
                                            </grading-level-card>
                                        </div>
                                    </div>
                                </transition-group>
                            </VueDraggable>
                            <div style="margin-top: 32px">
                                <el-text size="large">
                                    题目判分标准
                                </el-text>
                                <div style="display: flex;flex-direction: column;margin-top: 16px;margin-bottom: 20dvh;margin-left: 8px">
                                    <el-text style="align-self: start">
                                        选择题（单选/多选）
                                    </el-text>
                                    <el-radio-group :disabled="!editing" v-model="data.multipleChoicesQuestionsCheckingStrategy">
                                        <el-radio label="全部正确才可得分" value="all_correct"/>
                                        <el-radio label="错误时不得分，部分正确时按正确选项占比" value="correct_rated"/>
                                        <el-radio label="按正确选项和错误选项占比" value="correct_rated_and_wrong_rated"/>
                                        <el-radio label="按正确选项占比和错误双倍占比" value="correct_rated_and_wrong_double_rated"/>
                                    </el-radio-group>
                                    <div>
                                        <el-text type="info" style="margin-right: 12px">启用倒扣分制</el-text>
                                        <el-switch :disabled="!editing" v-model="data.enableLosePoints"/>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div v-else-if="loadingError" style="display:flex;flex-direction: column">
                            <el-empty description="获取设置失败"></el-empty>
                            <el-button link type="primary" @click="getData" size="large">重试</el-button>
                        </div>
                    </transition>
                </div>
            </el-scrollbar>
        </div>
    </div>
</template>

<style scoped>
/*noinspection CssUnusedSymbol*/
.filter-enter-active {
    overflow: hidden;
    transition: transform 0.3s var(--ease-out-quint) 0.2s,
    max-height 0.2s var(--ease-in-out-quint);
}

/*noinspection CssUnusedSymbol*/
.filter-leave-active {
    overflow: hidden;
    transition: transform 0.3s var(--ease-in-quint),
    max-height 0.2s var(--ease-in-out-quint) 0.3s;
}

/*noinspection CssUnusedSymbol*/
.filter-enter-from, .filter-leave-to {
    max-height: 0;
    transform: translateX(-100%);
}

/*noinspection CssUnusedSymbol*/
.filter-leave-from, .filter-enter-to {
    max-height: 38px;
    transform: translateX(0);
}

.score-bar {
    display: flex;
    position: relative;
    overflow: hidden;
    background: var(--panel-bg-color-overlay) var(--lighting-effect-background-2);
    height: 6px;
    border-radius: 3px;
    margin-top: 4px;
}
</style>