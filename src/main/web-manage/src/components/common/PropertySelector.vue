<script setup>
import {ArrowRight} from "@element-plus/icons-vue";

const props = defineProps({
    properties: {
        type: Object,
        required: true
    },
    editing: {
        type: Boolean,
        default: false
    },
    /*
        nameMapping: {
            type: Object
        }
    */
});
const model = defineModel({
    type: Object,
    required: true,
});
if (!model.value.trace) {
    model.value.trace = [];
}
const propertiesDataTrace = ref([]);
const updateDataTrace = () => {
    propertiesDataTrace.value = [];
    // delete model.value.verificationTypeName;
    let currentProperty = props.properties;
    propertiesDataTrace.value.push(currentProperty);
    for (const [index, traceItem] of model.value.trace.entries()) {
        if (traceItem && currentProperty) {
            const currentPropertyElement = currentProperty[traceItem];
            if (currentPropertyElement) {
                currentProperty = currentPropertyElement.properties;
                propertiesDataTrace.value.push(currentProperty);
            } else {
                // console.log(currentProperty);
                model.value.trace.splice(index, 1);
            }
        } else {
            model.value.trace.splice(index, 1);
        }
    }
};
watch(() => model.value.trace, updateDataTrace, {deep: true});
watch(() => props.properties, updateDataTrace, {deep: true});
updateDataTrace();
</script>

<template>
    <template v-for="order in (model.trace.length+1)" :key="order">
        <transition name="fadeMove" mode="out-in" appear>
            <div style="display: flex;flex-direction: row;margin-right: 12px;align-items: center"
                 v-if="propertiesDataTrace[order-1]">
                <el-icon style="margin-right: 6px;align-self: center">
                    <ArrowRight/>
                </el-icon>
<!--                <el-text style="margin-right: 4px;text-wrap: nowrap" v-if="editing">属性</el-text>-->
<!--                <el-tag type="info" style="margin-right: 4px;">{{ order }}</el-tag>-->
                <el-select v-model="model.trace[order-1]" v-if="editing" style="width: 100px;margin-right: 8px" :class="{error:!(model.trace[order-1])}"
                           placeholder="选择属性" @change="delete model.verificationTypeName">
                    <el-option v-for="(property,key) in propertiesDataTrace[order-1]" :key="key" :label="property.name"
                               :value="key"/>
                </el-select>
                <el-text v-else style="margin-left: 4px;margin-right: 8px">
                    {{ propertiesDataTrace[order-1][model.trace[order-1]].name }}
<!--                    {{ model.trace[order - 1] }}-->
                </el-text>
            </div>
            <div v-else-if="propertiesDataTrace[propertiesDataTrace.length-2][model.trace[model.trace.length-1]].verificationTypes"
                 style="display: flex">
                <el-icon style="margin-right: 6px;align-self: center">
                    <ArrowRight/>
                </el-icon>
<!--                <el-text style="margin-right: 4px;text-wrap: nowrap" v-if="editing">限制类型</el-text>-->
                <el-select v-model="model.verificationTypeName" style="width: 100px" placeholder="选择限制类型"
                           v-if="editing" :class="{error:!(model.verificationTypeName)}">
                    <el-option
                            v-for="(verificationType,key) in propertiesDataTrace[propertiesDataTrace.length-2][model.trace[model.trace.length-1]].verificationTypes"
                            :key="key" :label="verificationType.type"
                            :value="key"/>
                </el-select>
                <el-text v-else>
                    {{ propertiesDataTrace[propertiesDataTrace.length-2][model.trace[model.trace.length-1]].verificationTypes[model.verificationTypeName].type }}
                </el-text>
            </div>
        </transition>
    </template>
</template>

<style scoped>
.fadeMove-enter-active, .fadeMove-leave-active {
    transition: all 0.4s var(--ease-in-out-quint);
}

.fadeMove-enter-from, .fadeMove-leave-to {
    opacity: 0;
    transform: translateX(-100%);
}
</style>