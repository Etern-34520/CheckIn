<script setup>
import RuleCard from "@/components/common/RuleCard.vue";
import {VueDraggable} from "vue-draggable-plus";
import HarmonyOSIcon_Handle from "@/components/icons/HarmonyOSIcon_Handle.vue";
import HarmonyOSIcon_Plus from "@/components/icons/HarmonyOSIcon_Plus.vue";
import randomUUIDv4 from "@/utils/UUID.js";
import HarmonyOSIcon_Rename from "@/components/icons/HarmonyOSIcon_Rename.vue";
import HarmonyOSIcon_Remove from "@/components/icons/HarmonyOSIcon_Remove.vue";
import WebSocketConnector from "@/api/websocket.js";
import {ElMessage} from "element-plus";
import HarmonyOSIcon_InfoCircle from "@/components/icons/HarmonyOSIcon_InfoCircle.vue";

const editing = ref(false);
const data = ref([]);
const loading = ref(true);
const error = ref(false);
let backup = [];
const addRule = () => {
    data.value.push({id: randomUUIDv4()});
};
const cancel = () => {
    data.value = backup;
    editing.value = false;
}
const startEditing = () => {
    backup = JSON.parse(JSON.stringify(data.value));
    editing.value = true;
}
const finishEditing = () => {
    editing.value = false;
    WebSocketConnector.send({
        type: "saveVerificationSetting",
        data: data.value
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
const hasError = ref(false);
watch(() => data.value, () => {
    if (data.value && data.value.length > 0)
        hasError.value = data.value.some(datum => datum.error);
    else hasError.value = false;
}, {deep: true});

const getData = () => {
    loading.value = true;
    error.value = false;
    WebSocketConnector.send({
        type: "getVerificationSetting"
    }).then((response) => {
        loading.value = false;
        data.value = response.data;
    }, () => {
        ElMessage({
            type: "error", message: "获取规则信息失败"
        });
        loading.value = false;
        error.value = true;
    });
}

const varietiesData = [
    {name: "实际数据", reference: "${datum}"},
    {name: "规则限制", reference: "${limit}"},
    {name: "属性序号", reference: "${order}"},
]
</script>

<template>
    <div style="display: flex;flex-direction: column">
        <el-text style="align-self:baseline;font-size: 24px">上传校验设置</el-text>
        <div style="display: flex;margin-top: 16px;margin-left: 8px">
            <transition-group name="blur-scale">
                <el-button class="disable-init-animate" style="margin-right: 12px;" @click="editing ? finishEditing():startEditing()"
                           :disabled="(editing && hasError) || error" key="edit">
                    {{ editing ? '完成' : '编辑' }}
                </el-button>
                <div style="display: flex;flex-direction: row" v-if="editing" key="action">
                    <el-button class="disable-init-animate" @click="cancel">取消</el-button>
                    <el-button class="disable-init-animate" @click="addRule" style="margin-right: 12px;"
                               :icon="HarmonyOSIcon_Plus">
                        添加规则
                    </el-button>
                    <el-popover trigger="click">
                        <template #reference>
                            <el-button link class="disable-init-animate" :icon="HarmonyOSIcon_InfoCircle" type="info">
                                提示信息可用变量
                            </el-button>
                        </template>
                        <template #default>
                            <div class="varieties">
                                <div style="display: flex;flex-direction: row" v-for="datum of varietiesData">
                                    <el-text>{{ datum.reference }}</el-text>
                                    <div class="flex-blank-1" style="min-width: 12px;"></div>
                                    <el-text type="info">{{ datum.name }}</el-text>
                                </div>
                            </div>
                        </template>
                    </el-popover>
                </div>
            </transition-group>
        </div>
        <el-scrollbar>
            <div v-loading="loading" style="padding-bottom: 100px">
                <transition name="blur-scale" mode="out-in">
                    <vue-draggable
                            v-if="!loading && !error"
                            ref="draggable"
                            v-model="data"
                            :animation="150"
                            ghostClass="ghost"
                            handle=".handle">
                        <transition-group name="slide-hide">
                            <div class="slide-hide-base"
                                 v-for="(datum,index) in data" :key="datum.id">
                                <div class="panel-1 disable-init-animate"
                                     style="display: flex;min-height: 0;margin: 4px;padding: 4px 12px">
                                    <div style="width: 32px;display: flex;flex-direction: column;align-items: center;justify-content:center;margin-left: 8px">
                                        <transition name="blur-scale" mode="out-in">
                                            <div class="handle" style="cursor: grab;margin-bottom: 8px" v-if="editing">
                                                <HarmonyOSIcon_Handle :size="20"/>
                                            </div>
                                        </transition>
                                        <transition name="blur-scale">
                                            <el-button class="disable-init-animate" link v-if="editing"
                                                       @click="data.splice(index,1)">
                                                <HarmonyOSIcon_Remove :size="20"/>
                                            </el-button>
                                        </transition>
                                    </div>
                                    <rule-card :editing="editing" v-model="data[index]"/>
                                    <!--                                    <div class="flex-blank-1"></div>-->
                                    <div style="display: flex;flex-direction: column;margin: 8px 8px 8px 0;flex: 0">
                                        <el-tag size="large" type="info">{{
                                                "优先级：" + (index + 1)
                                            }}
                                        </el-tag>
                                        <transition name="blur-scale" mode="out-in">
                                            <el-tag v-if="data[index].error" size="large" type="danger"
                                                    style="margin-top: 2px">
                                                设置有误
                                            </el-tag>
                                        </transition>
                                        <!--                                        <el-text>{{ data[index] }}</el-text>-->
                                    </div>
                                </div>
                            </div>
                        </transition-group>
                    </vue-draggable>
                    <div v-else-if="error" style="display:flex;flex-direction: column">
                        <el-empty description="获取信息失败"></el-empty>
                        <el-button link type="primary" @click="getData" size="large">重试</el-button>
                    </div>
                </transition>
            </div>
        </el-scrollbar>
    </div>
</template>

<style scoped>
.varieties {
    display: flex;
    flex-direction: column;
}
</style>


<style>
.varieties .el-text {
    text-wrap: nowrap;
}
</style>