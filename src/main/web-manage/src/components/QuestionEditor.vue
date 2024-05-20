<script setup>
// import {nextTick, onBeforeMount, ref, watch} from "vue";
import {useRoute} from "vue-router";
import QuestionTempStorage from "@/data/QuestionTempStorage.js";
import {ElNotification} from "element-plus";
import PartitionTempStorage from "@/data/PartitionTempStorage.js";
import UserDataInterface from "@/data/UserDataInterface.js";
import randomUUID from "@/utils/UUID.js";
import getAvatarUrlOf from "@/utils/Avatar.js";
import {VueDraggable} from "vue-draggable-plus";
import HarmonyOSIcon_Plus from "@/components/icons/HarmonyOSIcon_Plus.vue";
import HarmonyOSIcon_Remove from "@/components/icons/HarmonyOSIcon_Remove.vue";
import HarmonyOSIcon_Handle from "@/components/icons/HarmonyOSIcon_Handle.vue";
import Like from "@/components/icons/Like.vue";
import DisLike from "@/components/icons/DisLike.vue";
import WebSocketConnector from "@/api/websocket.js";
import CreateNewPartitionDialog from "@/components/CreateNewPartitionDialog.vue";

const {proxy} = getCurrentInstance();
const route = useRoute();
let questionInfo = ref({});
const upload = ref();

const error = ref(false);
const errorMessage = ref("");
const ready = ref(false);
const imageDialogVisible = ref(false);
const image = ref({name: "", data: ""});
const loading = ref(false);
let requested = false;

let update = (newVal, oldVal) => {
  requested = false;
  setTimeout(() => {
    if (!requested) {
      loading.value = true;
    }
  }, 200);
  QuestionTempStorage.getAsync(newVal).then((questionData) => {
    questionInfo.value = questionData;
    if (ready.value === false) ready.value = true;
    requested = true;
    loading.value = false;
    error.value = false;
    errorMessage.value = "";
    if (questionData.question)
      lastPartitionId = questionData.question.partitionIds[0];
  }, (rejectData) => {
    requested = true;
    loading.value = false;
    if (rejectData.type === "question not found") {
      error.value = true;
      errorMessage.value = "题目不存在";
    }
  });
};
// let changeLoop1 = false;
watch(() => route.params.id, update);
watch(() => questionInfo.value.question, (newVal, oldVal) => {
  if (error.value) return;
  if (newVal && oldVal && oldVal.id === newVal.id) {
    // questionInfo.value.dirty = questionInfo.value.localNew || QuestionTempStorage.differFromOriginal(newVal);
    // else if (!differFromOriginal) newVal.status = ""
    // changeLoop1 = !changeLoop1;
    // if (changeLoop1)
    QuestionTempStorage.update(questionInfo.value);
  }
  // check(questionInfo.value);
  questionInfo.value.check();
}, {deep: true});

const allUsers = ref([]);

let partitions = PartitionTempStorage.reactivePartitions;

UserDataInterface.getUsersAsync().then((users) => {
  allUsers.value.push(...users);
});

onBeforeMount(() => {
  update(route.params.id, null);
});

const fileToDataUrl = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    const onLoad = () => {
      resolve({
        name: file.name,
        url: reader.result,
        size: file.size,
        status: "success"
      });
    };
    const onError = () => {
      reject();
    }
    reader.addEventListener("load", onLoad);
    reader.addEventListener("error", onError);
    reader.readAsDataURL(file);
  })
}

const filter = function (rawFile) {
  if (rawFile.raw.type.startsWith("image")) {
    if (questionInfo.value.question.images)
      for (const image of questionInfo.value.question.images) {
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
    else questionInfo.value.question.images = [];
    upload.value.handleRemove(rawFile);
    convert(rawFile.raw).then((result) => {
      questionInfo.value.question.images.push(result);
    });
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

const convert = (file) => {
  return new Promise((resolve, reject) => {
    if (file instanceof File) {
      fileToDataUrl(file).then((result) => {
        // upload.value.handleRemove(rawFile);
        resolve(result);
      });
    } else {
      reject(data);
    }
  });
}

const removeImage = function (file, fileList) {
  // console.log(file, fileList);
}

const isAdding = ref(false)

const onCreatingPartition = () => {
  isAdding.value = true
}

const hideCreating = () => {
  // createdPartitionName.value = ''
  isAdding.value = false
}

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

let lastPartitionId;

const preventEmptyPartition = (val) => {
  if (questionInfo.value.question.partitionIds.length === 0) {
    questionInfo.value.question.partitionIds.push(val);
  }
}

/**
 * For the bug that function preventEmptyPartition will not be called
 * when the selection of the multiple el-select is deleted by using backspace
 *
 * 修复多选el-select使用退格键删除选项时不会触发preventEmptyPartition的问题
 * */
const onPartitionChange = (val) => {
  if (val.length === 0) {
    questionInfo.value.question.partitionIds.push(lastPartitionId);
  } else {
    lastPartitionId = val[0];
  }
}

const currentUserQQ = Number(proxy.$cookies.get("qq"));
// const like = ref(false);

// const dislike = ref(false);
const switchLike = () => {
  const like = questionInfo.value.question.upVoters.has(currentUserQQ);
  if (like) {
    questionInfo.value.question.upVoters.delete(currentUserQQ);
    WebSocketConnector.send({
      type:"restoreVote",
      questionId:questionInfo.value.question.id
    });
  } else {
    WebSocketConnector.send({
      type:"upVote",
      questionId:questionInfo.value.question.id
    });
    questionInfo.value.question.upVoters.add(currentUserQQ);
    questionInfo.value.question.downVoters.delete(currentUserQQ);
  }
}

const switchDisLike = () => {
  const disLike = questionInfo.value.question.downVoters.has(currentUserQQ);
  if (disLike) {
    WebSocketConnector.send({
      type:"restoreVote",
      questionId:questionInfo.value.question.id
    });
    questionInfo.value.question.downVoters.delete(currentUserQQ);
  } else {
    WebSocketConnector.send({
      type:"downVote",
      questionId:questionInfo.value.question.id
    });
    questionInfo.value.question.downVoters.add(currentUserQQ);
    questionInfo.value.question.upVoters.delete(currentUserQQ);
  }
}
</script>

<template>
  <div v-loading="loading" style="width: 100%;height: 100%">
    <transition name="page-content" mode="out-in">
      <el-empty v-if="error" style="height: 100%;"></el-empty>
      <el-scrollbar v-else-if="ready">
        <el-dialog
            v-model="imageDialogVisible"
            :title="image.name"
            width="500"
            align-center>
          <el-image :src="image.data"/>
        </el-dialog>
        <div style="display: flex;flex-direction: column;padding: 8px">
          <transition-group name="alert">
            <el-alert v-for="error of questionInfo.errors" :key="'error'+error" type="error"
                      :closable="false">
              <template #title>
                <div style="display: flex;flex-direction: row;">
                  <el-text type="danger" style="margin-right: 16px">
                    {{ error.content }}
                  </el-text>
                  <el-button-group>
                    <el-button v-for="errorButton of error.buttons"
                               @click="errorButton.action"
                               :text="errorButton.isText" :type="errorButton.type">
                      {{ errorButton.content }}
                    </el-button>
                  </el-button-group>
                </div>
              </template>
            </el-alert>
            <el-alert v-for="warning of questionInfo.warnings" :key="'warning'+warning"
                      type="warning" :closable="false">
              <template #title>
                <div style="display: flex;flex-direction: row;">
                  <el-text type="warning" style="margin-right: 16px">
                    {{ warning.content }}
                  </el-text>
                  <el-button-group>
                    <el-button v-for="warningButton of warning.buttons"
                               @click="warningButton.action"
                               :text="warningButton.isText" :type="warningButton.type">
                      {{ warningButton.content }}
                    </el-button>
                  </el-button-group>
                </div>
              </template>
            </el-alert>
          </transition-group>
          <el-input class="question-content-input"
                    :class="{error:questionInfo.question.content===''||questionInfo.question.content===undefined}"
                    type="textarea"
                    placeholder="题目内容" v-model="questionInfo.question.content"
                    autosize></el-input>
          <div class="question-image-upload panel-1" style="padding: 16px">
            <el-upload ref="upload"
                       v-model:file-list="questionInfo.question.images"
                       list-type="picture-card"
                       multiple
                       :auto-upload="false"
                       action="ignore"
                       :on-change="filter"
                       :on-remove="removeImage"
                       :on-preview="file => {image.name = file.name;image.data = file.url;imageDialogVisible = true}">
              <HarmonyOSIcon_Plus :size="32"/>
            </el-upload>
          </div>
          <div style="display: flex;flex-direction: row">
            <el-select
                class="not-empty"
                :class="{error:questionInfo.question.partitionIds.length===0}"
                v-model="questionInfo.question.partitionIds"
                placeholder="选择分区"
                multiple
                filterable
                @focusout="hideCreating"
                @remove-tag="preventEmptyPartition"
                @change="onPartitionChange"
                style="flex:4;width:0"
            >
              <el-option v-for="partition of partitions" :key="partition.id"
                         :label="partition.name" :value="partition.id"></el-option>
              <template #footer>
                <transition name="creatingPartition" mode="out-in">
                  <el-button v-if="!isAdding" text bg size="small" style="width: 100%"
                             @click="onCreatingPartition">
                    创建新分区
                  </el-button>
                  <template v-else>
                    <createNewPartitionDialog @on-cancel="hideCreating" @on-confirm="hideCreating"/>
                  </template>
                </transition>
              </template>
            </el-select>
            <div style="flex-grow:1;width: 60px;margin-left: 2px;display: flex;flex-direction: column">
              <el-select v-model="questionInfo.question.authorQQ" filterable clearable placeholder="作者"
                         :class="{warning:questionInfo.question.authorQQ===undefined}">
                <el-option v-for="user of allUsers" :key="user.qq" :label="user.name" :value="user.qq">
                  <div style="display: flex;align-items: stretch;justify-items: stretch">
                    <el-avatar shape="circle" :size="28" style="margin: 4px" fit="cover"
                               :src="getAvatarUrlOf(user.qq)"></el-avatar>
                    <span>{{ user.name }}</span>
                    <span style="margin-left: 4px;color:var(--el-text-color-secondary)">{{
                        user.qq
                      }}</span>
                  </div>
                </el-option>
              </el-select>
              <div class="panel-1" style="padding: 2px;display: flex;flex-direction: row;align-items: center">
                <el-text style="margin-left: 4px;">
                  启用
                </el-text>
                <el-switch v-model="questionInfo.question.enabled"/>
                <div class="flex-blank-1"></div>
                <el-button-group>
                  <el-button link @click="switchLike">
                    <like :filled="questionInfo.question.upVoters.has(currentUserQQ)"/>
                    <el-text style="margin-left: 4px;">{{questionInfo.question.upVoters.size}}</el-text>
                  </el-button>
                  <el-button link @click="switchDisLike">
                    <DisLike :filled="questionInfo.question.downVoters.has(currentUserQQ)"/>
                    <el-text style="margin-left: 4px;">{{questionInfo.question.downVoters.size}}</el-text>
                  </el-button>
                </el-button-group>
              </div>
            </div>
          </div>
          <div style="display: flex;flex-direction: column;overflow: hidden">
            <VueDraggable
                ref="draggable"
                v-model="questionInfo.question.choices"
                :animation="150"
                ghostClass="ghost"
                @start="onStartDrag"
                @end="onEndDrag"
            >
              <transition-group :name="dragging ? null:'choice'">
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
              <HarmonyOSIcon_Plus :size="24"/>
            </el-button>
          </div>
        </div>
      </el-scrollbar>
    </transition>
  </div>
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
  overflow: hidden;
}

.choicePanel > .el-button {
  height: 100%;
  margin-left: 2px;
}

.choicePanel > .choice-correct-checkbox {
  margin-right: 4px;
}

/*noinspection CssUnusedSymbol*/
.choice-move, .choice-enter-active, .choice-leave-active {
  transition: 0.4s;
  overflow: hidden;
}

/*noinspection CssUnusedSymbol*/
.choice-enter-from, .choice-leave-to {
  opacity: 0;
  height: 0;
  margin-bottom: 0;
}

/*noinspection CssUnusedSymbol*/
.creatingPartition-enter-active, .creatingPartition-leave-active {
  transition: all 0.3s var(--ease-in-bounce-1);
}

/*noinspection CssUnusedSymbol*/
.creatingPartition-enter-from, .creatingPartition-leave-to {
  opacity: 0;
  scale: 0.95;
}

/*noinspection CssUnusedSymbol*/
.alert-enter-active, .alert-leave-active {
  transition: 400ms var(--ease-in-bounce-1);
}

/*noinspection CssUnusedSymbol*/
.alert-enter-from, .alert-leave-to {
  opacity: 0;
  margin-top: -40px;
  overflow: hidden;
  filter: blur(16px);
}

.question-image-upload > div {
  min-height: 152px;
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
.page-content-enter-active,
.page-content-leave-active {
  transition: 400ms var(--ease-in-out-quint);
}

/*noinspection CssUnusedSymbol*/
.page-content-enter-from,
.page-content-leave-to {
  filter: blur(32px);
  scale: 0.9;
  opacity: 0;
}
</style>