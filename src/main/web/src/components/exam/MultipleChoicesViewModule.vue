<script setup>
import Waterfall from "@/components/common/Waterfall.vue";

const props = defineProps({
    question: {
        type: Object,
        required: true
    }
});

const model = defineModel({
    type: Object
});
if (!model.value.selected) {model.value.selected = {}}
else {
    for (const key of Object.keys(model.value.selected)) {
        if (Number(key) >= props.question.choices.length) {
            delete model.value.selected[key];
        }
    }
}
model.value.type="MultipleChoice";

const select = (index) => {
    const selected = Boolean(model.value.selected[String(index)]);
    /** SINGLE_CORRECT or MULTIPLE_CORRECT */
    if (props.question.multipleChoiceType === "SINGLE_CORRECT") {
        model.value.selected = {};
        if (!selected) {
            model.value.selected[String(index)] = !selected;
        }
    } else {
        if (selected) {
            delete model.value.selected[String(index)];
        } else {
            model.value.selected[String(index)] = true;
        }
    }
}
</script>

<template>
    <waterfall :data="question.choices" :min-row-width="400" :even="true">
        <template #item="{item,index}">
            <div style="display: flex;flex-direction: column;padding: 6px" v-if="model.selected">
                <el-check-tag class="choice" type="info" style="flex: 1;" :checked="model.selected[String(index)]"
                              @click="select(index);">
                    <el-text>{{ item.content}}</el-text>
                </el-check-tag>
            </div>
        </template>
    </waterfall>
</template>
<style scoped>
.choice {
    background-color: rgba(128,128,128,0.03);
}

.choice:hover {
    background-color: rgba(128,128,128,0.05);
}

.choice::after {
    display: none;
}
</style>

<style>
.choice.is-checked {
    background: var(--el-color-primary) !important;
    .el-text {
        font-weight: bold;
        text-wrap: wrap !important;
        word-break: auto-phrase !important;
        color: var(--front-color-reverse) !important;
    }
}
</style>