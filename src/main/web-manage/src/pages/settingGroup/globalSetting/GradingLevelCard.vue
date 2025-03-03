<script setup>
import 'md-editor-v3/lib/style.css';
import {MdEditor} from "md-editor-v3";
import UIMeta from "@/utils/UI_Meta.js";
import Collapse from "@/components/common/Collapse.vue";

const model = defineModel({
    type: Object,
    required: true
});
const split = defineModel("split", {
    type: Array,
    required: true
});

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

</script>

<template>
    <div style="padding: 16px 0;display: flex;flex-direction: column;align-items: stretch;justify-content: stretch;flex: 1">
        <div style="display: flex;flex-direction: row;align-items: center;justify-content: start;">
            <el-color-picker size="large" style="margin-left: 16px;margin-right: 12px;" :predefine="predefine"
                             v-model="model.colorHex" :disabled="disabled"></el-color-picker>
            <div style="display: flex;flex-direction: column;align-items: stretch;justify-content: stretch;flex: 1">
                <el-text class="field-label disable-init-animate" style="margin-top: 0 !important;">名称</el-text>
                <el-input v-model="model.name" class="disable-init-animate" :disabled="disabled"
                          :class="{error: !(model.name)}"></el-input>
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
                           :theme="UIMeta.colorScheme.value" :show-toolbar-name="UIMeta.mobile.value"
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
    </div>
</template>

<style scoped>
.field-label {
    align-self: baseline;
    margin-top: 16px;
    margin-bottom: 4px;
}
</style>