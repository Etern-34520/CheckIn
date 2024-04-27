<script setup>
import {watch, onBeforeMount, ref, reactive, defineEmits, computed} from "vue";
import {useRoute} from "vue-router";
import QuestionTempStorage from "@/data/QuestionTempStorage.js";
import {ElNotification} from "element-plus";
import {Plus} from "@element-plus/icons-vue";
import {VueDraggableNext} from 'vue-draggable-next'
import PartitionTempStorage from "@/data/PartitionTempStorage.js";
import UserDataInterface from "@/data/UserDataInterface.js";
import randomUUID from "@/utils/UUID.js";
import getAvatarUrlOf from "@/utils/Avatar.js";

const route = useRoute();
let question = ref({});
const upload = ref();

const errors = ref([]);
const warnings = ref([]);
const ready = ref(false);
const imageDialogVisible = ref(false);
const image = ref({name: "", data: ""});

// const emits = defineEmits(["updateQuestion"]);

let update = (newVal, oldVal) => {
    console.log(oldVal + " to " + newVal);
    QuestionTempStorage.getAsync(newVal).then((questionData) => {
        question.value = questionData;
        if (ready.value === false) ready.value = true;
    });
};
watch(() => route.params.id, update);
watch(() => question.value, (newVal, oldVal) => {
    if (newVal && oldVal && oldVal.id === newVal.id) {
        let differFromOriginal = QuestionTempStorage.differFromOriginal(newVal);
        if (differFromOriginal && newVal.status !== "changed") newVal.status = "changed"
        else if (!differFromOriginal) newVal.status = ""
        QuestionTempStorage.update(newVal);
    }
    check(newVal);
}, {deep: true});

const check = (question) => {
    errors.value = [];
    if (question.content === "" || question.content === undefined) errors.value.push("题目内容为空")
}

const allUsers = ref([]);

let partitions = PartitionTempStorage.partitions;

UserDataInterface.getUsersAsync().then((users) => {
    allUsers.value.push(...users);
});

onBeforeMount(() => {
    update(route.params.id, null);
});

const filter = function (rawFile) {
    if (rawFile.raw.type.startsWith("image")) {
        for (const image of question.value.images) {
            if (image.size === rawFile.raw.size) {
                ElNotification({
                    title: "重复",
                    message: "图片已存在",
                    position: 'bottom-right',
                    type: 'warning',
                })
                upload.value.handleRemove(rawFile);
                return false;
            }
        }
        return true;
    } else {
        ElNotification({
            title: "类型错误",
            message: "请选择图片",
            position: 'bottom-right',
            type: 'error',
        })
        return false;
    }
}

const removeImage = function (file, fileList) {
    console.log(file, fileList);
}

const isAdding = ref(false)
const createdPartitionName = ref("")

const onCreatingPartition = () => {
    isAdding.value = true
}

const onConfirmCreating = () => {
    if (createdPartitionName.value) {
        PartitionTempStorage.create(createdPartitionName.value).then((resp) => {
            console.log(resp)
        });
        /*allPartitions.value.push({name: createdPartitionName.value});*/
        hideCreating();
    }
}

const hideCreating = () => {
    createdPartitionName.value = ''
    isAdding.value = false
}

const createChoice = () => {
    question.value.choices.push({content: "", correct: false, id: randomUUID()});
}

const removeChoice = (index) => {
    question.value.choices.splice(index, 1);
}
</script>

<template>
    <el-scrollbar v-if="ready">
        <div style="display: flex;flex-direction: column">
            <transition-group name="alert">
                <el-alert v-for="error of errors" :key="'error'+error" :title="error" type="error"
                          :closable="false"></el-alert>
                <el-alert v-for="warning of warnings" :key="'warning'+warning" :title="warning" type="warning"
                          :closable="false"></el-alert>
            </transition-group>
            <el-input class="question-content-input" type="textarea" placeholder="题目内容" v-model="question.content"
                      autosize></el-input>
            <div class="question-image-upload panel-1" style="padding: 16px">
                <el-upload ref="upload"
                           v-model:file-list="question.images"
                           list-type="picture-card"
                           action="ignore"
                           :auto-upload="false"
                           :on-change="filter"
                           :on-remove="removeImage"
                           :on-preview="file => {image.name = file.name;image.data = file.url;imageDialogVisible = true}">
                    <el-icon>
                        <Plus/>
                    </el-icon>
                </el-upload>
            </div>
            <div style="display: flex;flex-direction: row">
                <el-select
                    class="flex-select"
                    v-model="question.partitionIds"
                    multiple
                    placeholder="选择分区"
                    filterable
                    @focusout="hideCreating"
                    style="flex:4;width:0"
                >
                    <el-option v-for="partition of partitions" :key="partition.id"
                               :label="partition.name"
                               :value="partition.id"></el-option>
                    <template #footer>
                        <transition name="creatingPartition" mode="out-in">
                            <el-button v-if="!isAdding" text bg size="small" @click="onCreatingPartition">
                                创建新分区
                            </el-button>
                            <template v-else>
                                <div style="display: flex;flex-direction: row">
                                    <el-input
                                        style="flex:1;margin-right: 4px"
                                        v-model="createdPartitionName"
                                        placeholder="分区名"
                                        size="small"
                                    />
                                    <el-button-group>
                                        <el-button type="primary" size="small" @click="onConfirmCreating"
                                                   :disabled="createdPartitionName===''">确定
                                        </el-button>
                                        <el-button size="small" @click="hideCreating">取消</el-button>
                                    </el-button-group>
                                </div>
                            </template>
                        </transition>
                    </template>
                </el-select>
                <el-select v-model="question.authorQQ" filterable placeholder="作者"
                           style="flex-grow:1;width: 60px;margin-left: 2px">
                    <el-option v-for="user of allUsers" :key="user.qq" :label="user.name" :value="user.qq">
                        <div style="display: flex;align-items: stretch;justify-items: stretch">
                            <el-avatar shape="circle" :size="28" style="margin: 4px" fit="cover"
                                       :src="getAvatarUrlOf(user.qq)"></el-avatar>
                            <span>{{ user.name }}</span>
                            <span style="margin-left: 4px;color:var(--el-text-color-secondary)">{{ user.qq }}</span>
                        </div>
                    </el-option>
                </el-select>
            </div>
            <div style="display: flex;flex-direction: column;overflow: hidden">
                <vue-draggable-next v-model="question.choices" tag="transition-group" :component-data="{name:'choice'}"
                                    handle=".handle">
                    <transition-group name="choice">
                        <div class="choicePanel" v-for="(choice,$index) of question.choices" :key="choice.id">
                            <div class="handle" style="cursor: grab">
                                <svg width="24" height="24" viewBox="0 0 24 24">
                                    <defs>
                                        <path
                                            d="M2.75,9.75 C2.33578644,9.75 2,9.41421356 2,9 C2,8.58578644 2.33578644,8.25 2.75,8.25 L21.25,8.25 C21.6642136,8.25 22,8.58578644 22,9 C22,9.41421356 21.6642136,9.75 21.25,9.75 L2.75,9.75 Z M21.25,14.25 C21.6642136,14.25 22,14.5857864 22,15 C22,15.4142136 21.6642136,15.75 21.25,15.75 L2.75,15.75 C2.33578644,15.75 2,15.4142136 2,15 C2,14.5857864 2.33578644,14.25 2.75,14.25 L21.25,14.25 Z"
                                            id="_path-2"/>
                                    </defs>
                                    <g id="_Public/ic_public_drag_handle" stroke="none" stroke-width="1" fill="none"
                                       fill-rule="evenodd">
                                        <use id="_move" class="svg-text-fill" fill="#000000" fill-rule="nonzero"
                                             xlink:href="#_path-2"/>
                                    </g>
                                </svg>
                            </div>
                            <el-checkbox v-model="choice.correct" class="choice-correct-checkbox"
                                         size="large"></el-checkbox>
                            <el-input type="text" size="large" placeholder="选项内容"
                                      v-model="choice.content"></el-input>
                            <el-button class="remove-choice-button" text @click="removeChoice($index)">
                                <svg width="18" height="18" viewBox="0 0 24 24">
                                    <defs>
                                        <path
                                            d="M5.629,7.5 L6.72612901,18.4738834 C6.83893748,19.6019681 7.77211147,20.4662096 8.89848718,20.4990325 L8.96496269,20.5 L15.0342282,20.5 C16.1681898,20.5 17.1211231,19.6570911 17.2655686,18.5392856 L17.2731282,18.4732196 L18.1924161,9.2527383 L18.369,7.5 L19.877,7.5 L19.6849078,9.40262938 L18.7657282,18.6220326 C18.5772847,20.512127 17.0070268,21.9581787 15.1166184,21.9991088 L15.0342282,22 L8.96496269,22 C7.06591715,22 5.47142703,20.5815579 5.24265599,18.7050136 L5.23357322,18.6231389 L4.121,7.5 L5.629,7.5 Z M10.25,11.75 C10.6642136,11.75 11,12.0857864 11,12.5 L11,18.5 C11,18.9142136 10.6642136,19.25 10.25,19.25 C9.83578644,19.25 9.5,18.9142136 9.5,18.5 L9.5,12.5 C9.5,12.0857864 9.83578644,11.75 10.25,11.75 Z M13.75,11.75 C14.1642136,11.75 14.5,12.0857864 14.5,12.5 L14.5,18.5 C14.5,18.9142136 14.1642136,19.25 13.75,19.25 C13.3357864,19.25 13,18.9142136 13,18.5 L13,12.5 C13,12.0857864 13.3357864,11.75 13.75,11.75 Z M12,1.75 C13.7692836,1.75 15.2083571,3.16379796 15.2491124,4.92328595 L15.25,5 L21,5 C21.4142136,5 21.75,5.33578644 21.75,5.75 C21.75,6.14942022 21.43777,6.47591522 21.0440682,6.49872683 L21,6.5 L14.5,6.5 C14.1005798,6.5 13.7740848,6.18777001 13.7512732,5.7940682 L13.75,5.75 L13.75,5 C13.75,4.03350169 12.9664983,3.25 12,3.25 C11.0536371,3.25 10.2827253,4.00119585 10.2510148,4.93983756 L10.25,5 L10.25,5.75 C10.25,6.14942022 9.93777001,6.47591522 9.5440682,6.49872683 L9.5,6.5 L2.75,6.5 C2.33578644,6.5 2,6.16421356 2,5.75 C2,5.35057978 2.31222999,5.02408478 2.7059318,5.00127317 L2.75,5 L8.75,5 C8.75,3.20507456 10.2050746,1.75 12,1.75 Z"
                                            id="_path-3"/>
                                    </defs>
                                    <g id="_Public/ic_public_delete" stroke="none" stroke-width="1" fill="none"
                                       fill-rule="evenodd">
                                        <mask class="svg-text-fill" id="_mask-3">
                                            <use xlink:href="#_path-3"/>
                                        </mask>
                                        <use id="_形状结合" class="svg-text-fill" fill-rule="nonzero"
                                             xlink:href="#_path-3"/>
                                    </g>
                                </svg>
                            </el-button>
                        </div>
                    </transition-group>
                </vue-draggable-next>
                <el-button @click="createChoice" size="large" text>
                    <svg width="24" height="24" viewBox="0 0 24 24">
                        <defs>
                            <path
                                d="M12,2 C12.5522847,2 13,2.44771525 13,3 L13,21 C13,21.5522847 12.5522847,22 12,22 C11.4477153,22 11,21.5522847 11,21 L11,13 L3,13 C2.44771525,13 2,12.5522847 2,12 C2,11.4477153 2.44771525,11 3,11 L11,11 L11,3 C11,2.44771525 11.4477153,2 12,2 Z M21,11 C21.5522847,11 22,11.4477153 22,12 C22,12.5522847 21.5522847,13 21,13 L13.999,13 L13.999,11 L21,11 Z"
                                id="_path-5"/>
                        </defs>
                        <g id="_Public/ic_public_add_filled" stroke="none" stroke-width="1" fill="none"
                           fill-rule="evenodd">
                            <mask id="_mask-5" fill="white">
                                <use xlink:href="#_path-5"/>
                            </mask>
                            <use id="_Combined-Shape" class="svg-text-fill" fill="#000000" fill-rule="nonzero"
                                 xlink:href="#_path-5"/>
                        </g>
                    </svg>
                </el-button>
            </div>
            <el-dialog
                v-model="imageDialogVisible"
                :title="image.name"
                width="500"
                align-center
            >
                <el-image :src="image.data"/>
            </el-dialog>
        </div>
    </el-scrollbar>
</template>

<style scoped>
.choicePanel {
    display: flex;
    flex-direction: row;
    justify-content: stretch;
    justify-items: stretch;
    height: 40px;
    margin-bottom: 2px;
    overflow: hidden;
}

.handle {
    width: 20px;
    padding: 5px;
    display: flex;
    align-items: center;
    justify-items: center;
    align-content: center;
    justify-content: center;
}

.remove-choice-button {
    font-size: 20px;
}

.choicePanel > .el-button {
    height: 100%;
    margin-left: 2px;
}

.choicePanel > .choice-correct-checkbox {
    margin-right: 4px;
}

.choice-move, .choice-enter-active, .choice-leave-active {
    transition: 0.4s;
    overflow: hidden;
}

.choice-enter-from, .choice-leave-to {
    opacity: 0;
    height: 0;
    margin-bottom: 0;
}

.creatingPartition-enter-active, .creatingPartition-leave-active {
    transition: all 0.3s ease-out;
}

.creatingPartition-enter-from, .creatingPartition-leave-to {
    opacity: 0;
    scale: 0.95;
}

.alert-enter-active, .alert-leave-active {
    transition: 300ms var(--ease-in-bounce);
}

.alert-enter-from, .alert-leave-to {
    opacity: 0;
    margin-top: -40px;
    overflow: hidden;
    filter: blur(16px);
}
</style>