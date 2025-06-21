<script setup>
import 'md-editor-v3/lib/style.css';
import {MdEditor} from "md-editor-v3";
import UIMeta from "@/utils/UI_Meta.js";
import Collapse from "@/components/common/Collapse.vue";
import UserDataInterface from "@/data/UserDataInterface.js";
import PermissionInfo from "@/auth/PermissionInfo.js";

const model = defineModel({
    type: Object,
    required: true
});
const split = defineModel("split", {
    type: Array,
    required: true
});
const error = defineModel("error", {
    type: Boolean
})

const props = defineProps({
    predefine: {
        type: Array,
        default: ["#F56C6C", "#E6A23C", "#67C23A"]
    },
    index: {
        type: Number,
        required: true
    },
    disabled: {
        type: Boolean,
        required: true
    },
    min: {
        type: Number,
        required: true
    },
    max: {
        type: Number,
        required: true
    }
});

const userGroups = UserDataInterface.userGroups;
UserDataInterface.getReactiveUserGroupsAsync();


const error1 = ref(false);
const error2 = ref(false);

const updateError = () => {
    error1.value = model.value.name.length === 0;
    error2.value = model.value.creatingUserStrategy !== "NOT_CREATE" && !Boolean(model.value.creatingUserRole);
    error.value = error1.value || error2.value;
};
const unwatch = watch(model.value, updateError, {deep: true});
onUnmounted(() => {
    unwatch();
});
</script>

<template>
    <div style="padding: 8px 8px 8px 16px;display: flex;flex-direction: column;align-items: stretch;justify-content: stretch;flex: 1">
        <div style="display: flex;flex-direction: row;flex-wrap: wrap;align-items: center;justify-content: start;">
            <el-color-picker size="large" style="margin-right: 12px;" :predefine="predefine"
                             v-model="model.colorHex" :disabled="disabled"></el-color-picker>
            <slot/>
        </div>
        <div style="display: flex;flex-direction: row;flex-wrap: wrap;align-items: center;justify-content: start;">
            <div style="display: flex;flex-direction: column;align-items: stretch;justify-content: stretch;flex: 1;min-width: 160px">
                <el-text class="field-label disable-init-animate" style="margin-top: 0 !important;">ID</el-text>
                <el-text class="field-label disable-init-animate" style="margin-top: 0 !important;">{{ model.id }}
                </el-text>
            </div>
            <div style="display: flex;flex-direction: column;align-items: stretch;justify-content: stretch;flex: 1;min-width: 160px">
                <el-text class="field-label disable-init-animate" style="margin-top: 0 !important;">名称</el-text>
                <el-input v-model="model.name" class="disable-init-animate" :disabled="disabled"
                          :class="{error: error1}"></el-input>
            </div>
        </div>
        <el-text class="field-label disable-init-animate">描述</el-text>
        <el-input v-model="model.description" class="disable-init-animate" :disabled="disabled"
                  style="min-height: 64px;margin-bottom: 32px;"></el-input>
        <collapse style="flex: 1">
            <template #title>
                <el-text style="margin-left: 20px;height: 30px;display: block;line-height: 30px">消息</el-text>
            </template>
            <template #content>
                <md-editor no-upload-img placeholder="消息" v-model="model.message"
                           :key="UIMeta.colorScheme" preview-theme="vuepress" :disabled="disabled"
                           :toolbars-exclude="['save','catalog','github']"
                           :theme="UIMeta.colorScheme.value" :show-toolbar-name="UIMeta.touch.value"
                           :preview="!UIMeta.mobile.value"/>
            </template>
        </collapse>
        <el-text class="field-label disable-init-animate">分数</el-text>
        <div style="display: flex;flex-direction: row;flex-wrap: wrap">
            <el-input-number class="disable-init-animate" :min="min + index" :max="max - (split.length - index)"
                             v-model="split[index]"
                             :disabled="index === 0 || disabled" style="margin-right: 16px"></el-input-number>
            <el-text size="large" style="margin-right: 16px">~</el-text>
            <el-input-number class="disable-init-animate" :min="min + index" :max="max - (split.length - index)"
                             v-if="split[index+1]"
                             :disabled="disabled" v-model="split[index+1]"></el-input-number>
            <el-input-number class="disable-init-animate" v-else :model-value="max" disabled></el-input-number>
        </div>
        <el-text class="field-label disable-init-animate">用户创建策略</el-text>
        <el-radio-group v-model="model.creatingUserStrategy" style="padding: 4px 20px"
                        :disabled="disabled">
            <el-radio value="NOT_CREATE">不创建用户</el-radio>
            <el-radio value="CREATE_AND_DISABLED">创建用户但不启用</el-radio>
            <el-radio value="CREATE_AND_ENABLED_AFTER_VALIDATED">创建用户并在验证后启用</el-radio>
            <el-radio value="CREATE_AND_ENABLED">创建用户并启用</el-radio>
        </el-radio-group>
        <transition name="blur-scale" mode="out-in">
            <div v-if="model.creatingUserStrategy !== 'NOT_CREATE'">
                <el-text>
                    选择新用户所属组
                </el-text>
                <el-select v-model="model.creatingUserRole"
                           :disabled="disabled"
                           :class="{error: error2}"
                           placeholder="选择用户组"
                           filterable style="max-width: 200px;margin-left: 8px">
                    <el-option v-for="[userGroupType,userGroup] of Object.entries(userGroups)"
                               :disabled="!PermissionInfo.hasPermission('role','operate role ' + userGroup.type)"
                               :value="userGroup.type" :label="userGroup.type"/>
                </el-select>
            </div>
        </transition>
    </div>
</template>

<style scoped>
.field-label {
    align-self: baseline;
    margin-top: 16px;
    margin-bottom: 4px;
}
</style>