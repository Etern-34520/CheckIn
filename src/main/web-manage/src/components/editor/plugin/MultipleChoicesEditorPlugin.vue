<script setup>

import {VueDraggable} from "vue-draggable-plus";
import HarmonyOSIcon_Remove from "@/components/icons/HarmonyOSIcon_Remove.vue";
import HarmonyOSIcon_Handle from "@/components/icons/HarmonyOSIcon_Handle.vue";
import HarmonyOSIcon_Plus from "@/components/icons/HarmonyOSIcon_Plus.vue";
import randomUUID from "@/utils/UUID.js";

const questionInfo = defineModel('questionInfo',{
    required:true,
    type:Object,
});


const createChoice = () => {
    questionInfo.value.question.choices.push({content: "", correct: false, id: randomUUID()});
}

const removeChoice = (index) => {
    questionInfo.value.question.choices.splice(index, 1);
}

const dragging = ref(false);
const onStartDrag = () => {
    dragging.value = true;
}
const onEndDrag = () => {
    nextTick(() => {
        dragging.value = false;
    });
}

</script>

<template>
    <div style="display: flex;flex-direction: column;overflow: hidden">
        <VueDraggable
            ref="draggable"
            v-model="questionInfo.question.choices"
            :animation="150"
            ghostClass="ghost"
            handle=".handle"
            @start="onStartDrag"
            @end="onEndDrag"
        >
            <transition-group :name="dragging ? null:'drag'">
                <div class="choicePanel" v-for="(choice,$index) of questionInfo.question.choices"
                     :key="choice.id">
                    <div class="handle" style="cursor: grab">
                        <HarmonyOSIcon_Handle/>
                    </div>
                    <el-checkbox v-model="choice.correct" class="choice-correct-checkbox"
                                 size="large"></el-checkbox>
                    <el-input type="text" size="large" placeholder="选项内容"
                              v-model="choice.content"
                              :class="{error:choice.content===undefined || choice.content === ''}"></el-input>
                    <transition name="delete-choice-button">
                        <el-button class="remove-choice-button"
                                   v-show="questionInfo.question.choices.length>2" text
                                   @click="removeChoice($index)">
                            <HarmonyOSIcon_Remove/>
                        </el-button>
                    </transition>
                </div>
            </transition-group>
        </VueDraggable>
        <el-button @click="createChoice" size="large" text>
            <HarmonyOSIcon_Plus/>
            <el-text>添加选项</el-text>
        </el-button>
    </div>
</template>

<style scoped>
.choicePanel {
    display: flex;
    flex-direction: row;
    justify-content: stretch;
    justify-items: stretch;
    align-items: center;
    height: 40px;
    margin-bottom: 2px;
    overflow: hidden;
}

.remove-choice-button {
    overflow: hidden;
    padding: 0!important;
    width: 45px;
    height: 100%;
    margin: 0;
}

.choicePanel > .choice-correct-checkbox {
    margin-right: 4px;
}

/*noinspection CssUnusedSymbol*/
.delete-choice-button-enter-active,
.delete-choice-button-leave-active {
    transition: 300ms var(--ease-in-out-quint);
}

/*noinspection CssUnusedSymbol*/
.delete-choice-button-enter-from,
.delete-choice-button-leave-to {
    opacity: 0;
    width: 0;
    max-width: 0;
    transform: scale(0.8);
}

/*noinspection CssUnusedSymbol*/
.drag-move, .drag-enter-active, .drag-leave-active {
    transition: 0.4s;
    overflow: hidden;
}

/*noinspection CssUnusedSymbol*/
.drag-enter-from, .drag-leave-to {
    opacity: 0;
    height: 0;
    margin-bottom: 0;
}
</style>