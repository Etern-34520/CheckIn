<script setup>
const tip = ref({});
const tipTransitionCaller = ref(false);
const buttonsOption1 = ref([]);
const model = defineModel({required: true});
const props = defineProps({
    buttonsOption: {
        type: Array,
        default: () => {
            return []
        }
    },
    enableTip: {
        type: Boolean,
        default: true
    }
});
defineExpose({
    showTip: (content, type) => {
        tip.value.content = content;
        tip.value.type = type;
        tipTransitionCaller.value = !tipTransitionCaller.value;
    },
    clearTip: () => {
        tip.value = {};
    }
});
if (props.buttonsOption.length > 0) {
    buttonsOption1.value = props.buttonsOption;
} else {
    buttonsOption1.value = [{
        content: "关闭",
        type: "info",
        onclick: () => {
            model.value = false;
        }
    }];
}
</script>

<template>
    <el-dialog v-model="model" width="400" @open="tip = {}" align-center draggable append-to-body :show-close="false"
               @closed="tip={}">
        <slot></slot>
        <div v-if="enableTip" style="min-height: 40px;margin-top: 16px;">
            <transition name="blur-scale" mode="out-in">
                <el-text :key="tipTransitionCaller" style="margin-right: 16px"
                         :type="tip.type?tip.type:'info'">{{ tip.content }}
                </el-text>
            </transition>
        </div>
        <template #footer>
            <transition-group name="button-hide">
                <template v-for="buttonOption of buttonsOption1" :key="buttonOption.id?buttonOption.id:buttonOption.content">
                    <el-button v-if="buttonOption.hidden?(!buttonOption.hidden):true"
                               :type="buttonOption.type" @click="buttonOption.onclick"
                               :disabled="buttonOption.disabled">
                        {{ buttonOption.content }}
                    </el-button>
                </template>
            </transition-group>
        </template>
    </el-dialog>
</template>

<style scoped>

</style>