<!--suppress ALL -->
<script setup>
import {Pane, Splitpanes} from "splitpanes"
import "splitpanes/dist/splitpanes.css"
// import {getCurrentInstance, ref, watch} from "vue";
import router from "@/router/index.js";
import QuestionTempStorage from "@/data/QuestionTempStorage.js";
import PartitionTempStorage from "@/data/PartitionTempStorage.js";
import randomUUID from "@/utils/UUID.js";
import HarmonyOSIcon_Plus from "@/components/icons/HarmonyOSIcon_Plus.vue";
import HarmonyOSIcon_CheckBox from "@/components/icons/HarmonyOSIcon_CheckBox.vue";
import EditPartitionNameDialog from "@/components/EditPartitionNameDialog.vue";
import HarmonyOSIcon_Upload from "@/components/icons/HarmonyOSIcon_Upload.vue";
import HarmonyOSIcon_Remove from "@/components/icons/HarmonyOSIcon_Remove.vue";
import HarmonyOSIcon_Rename from "@/components/icons/HarmonyOSIcon_Rename.vue";
import {ArrowLeftBold} from "@element-plus/icons-vue";
import CreatePartitionButton from "@/components/CreatePartitionButton.vue";
import QuestionInfoPanel from "@/components/QuestionInfoPanel.vue";
import SelectPartitionsActionDialog from "@/components/SelectMoveToPartitionDialog.vue";

const {proxy} = getCurrentInstance();

QuestionTempStorage.reset();
PartitionTempStorage.reset();

const props = {
    label: 'name',
    children: 'zones',
    isLeaf: 'leaf',
    data: {}
}

const tree = ref();
const filterText = ref('')
const loading = ref(true);
const showTree = ref(true);
const errorsDisplay = ref(false);

const filterNode = (value, node) => {
    if (!value) return true;
    return filterQuestionInfo(node.data);
}

const filterQuestionInfo = (questionInfo) => {
    for (const v of filterText.value.split(",")) {
        if (v !== "" &&
            (questionInfo.type !== 'question') || (
                (questionInfo.question.content && questionInfo.question.content.toUpperCase().includes(v.toUpperCase())) ||
                (questionInfo.question.id && questionInfo.question.id.toUpperCase().includes(v.toUpperCase())))
        ) {
            return true;
        }
    }
    return false;
}

watch(filterText, (val) => {
    tree.value.filter(val);
})

const localPartitionQuestionData = {};

const loadNode = (node, resolve, reject) => {
    if (node.level === 0) {
        let createPartitionButtonData = {
            id: "create-partition-" + node.data.id,
            leaf: true,
            data: {type: "createPartition"}
        };
        PartitionTempStorage.getRefPartitionsAsync().then((partitions) => {
            let data = [createPartitionButtonData];
            for (const partitionInfo of partitions.value) {
                data.push(getTreeNodeDataOfPartition(partitionInfo));
            }
            loading.value = false;
            resolve(data);
        });
    } else {
        let createQuestionButtonData = {
            id: "create-question-" + node.data.id,
            treeId: node.data.id + "/createQuestion",
            leaf: true,
            data: {
                type: "createQuestion",
                partitionId: node.data.id,
                treeId: node.data.id + "/create-question"
            }
        };
        const localPartitionQuestionNodes = localPartitionQuestionData[node.data.id];
        let data = [createQuestionButtonData];
        if (localPartitionQuestionNodes instanceof Array) {
            data.push(...localPartitionQuestionNodes);
        }
        localPartitionQuestionData[node.data.id] = false;
        if (node.data.empty) {
            resolve(data);
        } else {
            QuestionTempStorage.getContentsAndIdsAsyncByPartitionId(node.data.id).then((questionInfos) => {
                remoteLoop:for (let questionInfo of questionInfos) {
                    if (localPartitionQuestionNodes instanceof Array) {
                        for (const localQuestionNode of localPartitionQuestionNodes) {
                            if (localQuestionNode.data.question.id === questionInfo.question.id) {
                                continue remoteLoop;
                            }
                        }
                    }
                    // questionInfo.name = questionInfo.content;
                    const questionNode = QuestionTempStorage.getQuestionNodeItemDataOf(questionInfo, node.data.id);
                    data.push(questionNode);
                }
                resolve(data);
            }, (error) => {
                reject();
            });
        }
    }
    /*resolve(data);*/
}

function getTreeNodeDataOfPartition(partition) {
    return {
        id: partition.id,
        treeId: partition.id,
        zones: [],
        empty: partition.empty,
        data: {
            editing: false,
            partition: partition,
            type: "partition"
        }
    };
}

PartitionTempStorage.registerOnPartitionAdded((partition) => {
    partition.empty = true;
    tree.value.append(getTreeNodeDataOfPartition(partition), tree.value.root);
});
PartitionTempStorage.registerOnPartitionDeleted((partition) => {
    tree.value.remove(tree.value.getNode(partition.id));
});

QuestionTempStorage.registerOnQuestionUpdateLocal((questionInfo, differFromOriginal) => {
    for (let partitionId of questionInfo.question.partitionIds) {
        if (tree.value.getNode(partitionId + "/" + questionInfo.question.id) === null) {
            const questionNode = QuestionTempStorage.getQuestionNodeItemDataOf(questionInfo, partitionId);
            if (localPartitionQuestionData[partitionId] === false)
                tree.value.append(questionNode, tree.value.getNode(partitionId));
            else {
                if (localPartitionQuestionData[partitionId] === undefined) {
                    localPartitionQuestionData[partitionId] = []
                    localPartitionQuestionData[partitionId].push(questionNode);
                } else {
                    let exist = false;
                    for (const questionInfo1 of localPartitionQuestionData[partitionId]) {
                        if (questionInfo1.data.question.id === questionNode.data.question.id) {
                            exist = true;
                        }
                    }
                    if (!exist) {
                        localPartitionQuestionData[partitionId].push(questionNode);
                    }
                }
            }
        }
    }
    for (const partition of PartitionTempStorage.reactivePartitions.value) {
        if (questionInfo.question.partitionIds && questionInfo.question.partitionIds.includes(partition.id)) {
            continue;
        }
        let localPartitionQuestionDatum1 = localPartitionQuestionData[partition.id];
        if (localPartitionQuestionDatum1 === false) {
            if (tree.value.getNode(partition.id) !== null) {
                let questionNode = tree.value.getNode(partition.id + "/" + questionInfo.question.id);
                if (questionNode !== null)
                    tree.value.remove(questionNode);
            }
        } else if (localPartitionQuestionDatum1) {
            let index = 0;
            let find = false;
            for (const questionNode of localPartitionQuestionDatum1) {
                if (questionNode.data.question.id === questionInfo.question.id) {
                    find = true;
                    break;
                }
                index++;
            }
            if (find) {
                localPartitionQuestionData[partition.id] = [];
            }
        }
    }
});

QuestionTempStorage.registerOnQuestionDeleted((id, localDeleted) => {
    let questionInfo = QuestionTempStorage.reactiveQuestionInfos.value[id];
    if (questionInfo && questionInfo.dirty || (!localDeleted && router.currentRoute.value.params.id === id)) {
        return;
    }
    if (router.currentRoute.value.params.id === id) {
        router.push("/manage/questions/");
    }
    for (let partition of PartitionTempStorage.reactivePartitions.value) {
        let questionNode = tree.value.getNode(partition.id + "/" + id);
        if (questionNode !== null) {
            tree.value.remove(questionNode);
        }
    }
});

const openEdit = (questionId) => {
    router.push("/manage/questions/" + questionId + "/");
}

const onEdit = (nodeObj, nodeItem, node, event) => {
    // console.log(nodeObj,nodeItem,node,event);
    nodeObj.data.type === 'question' ? openEdit(nodeObj.data.question.id) : null
}

const allowDrag = (node) => {
    console.log(node);
    return node.data.data.type === 'question';
}

const allowDrop = (draggingNode, dropNode, type) => {
    // console.log(draggingNode, dropNode, type);
    let nodeDataType = dropNode.data.data.type;
    return ((nodeDataType === 'partition' || nodeDataType === 'questionGroup') && type === "inner") ||
        (nodeDataType !== 'partition' && nodeDataType !== "createPartition" && type === "next");
}

const onDrop = (dragNode, dropNode, place, event) => {
    let dragNodeData = dragNode.data.data;
    let dropNodeData = dropNode.data.data;
    console.log(dragNodeData, dropNodeData, place, event);

    function replacePartitionId(dropPartitionId) {
        let treeIdBlock = dragNode.data.treeId.split("/");
        let originalItemPartitionId = treeIdBlock[0];
        let index = 0;
        for (let partitionId of dragNodeData.question.partitionIds) {
            if (Number(originalItemPartitionId) === partitionId) {
                dragNode.data.treeId = partitionId + "/" + dragNodeData.question.id;
                dragNode.data.data.question.partitionIds.splice(index, 1, Number(dropPartitionId));
                break;
            }
            index++
        }
        dragNodeData.question.partitionIds = Array.from(new Set(dragNodeData.question.partitionIds));
        QuestionTempStorage.update(dragNodeData);
    }

    let dropPartitionId;
    delete dropNode.data.data.dragHover;
    if (dropNodeData.type === "partition") {
        dropPartitionId = dropNodeData.partition.id;
    } else if (dropNodeData.type === "question") {
        dropPartitionId = dropNode.data.treeId.split("/")[0];
    } else if (dropNodeData.type === "createQuestion") {
        dropPartitionId = dropNode.data.data.partitionId;
    } else {
        return;
    }
    if (dragNodeData.simple) {
        QuestionTempStorage.getAsync(dragNodeData.question.id).then((questionInfo) => {
            dragNode.data.data = questionInfo;
            replacePartitionId(dropPartitionId);
        });
    } else {
        replacePartitionId(dropPartitionId);
    }
}

const onDragEnd = (node, dropNode, event) => {
    delete dropNode.data.data.dragHover;
}

const onDeleteQuestion = (questionId) => {
    QuestionTempStorage.delete(questionId);
}

const onDeletePartition = (partitionId) => {
    PartitionTempStorage.deleteRemote(partitionId);
}

const createQuestion = (partitionId, type) => {
    console.log(partitionId + " create:" + type);
    let question = {
        id: randomUUID(),
        content: "",
        enabled: false,
        partitionIds: [partitionId],
        authorQQ: Number(proxy.$cookies.get("qq")),
        upVoters: new Set(),
        downVoters: new Set(),
        choices: [{
            id: randomUUID(),
            correct: true,
            content: ""
        }, {
            id: randomUUID(),
            correct: false,
            content: ""
        }]
    };
    const questionInfo = QuestionTempStorage.create(question);
    tree.value.append(QuestionTempStorage.getQuestionNodeItemDataOf(questionInfo, partitionId), tree.value.getNode(partitionId));
}

const dragEnter = (dragNode, enterNode, event) => {
    if (enterNode.data.data.type === 'partition')
        enterNode.data.data.dragHover = true;
}

const dragLeave = (dragNode, leaveNode, event) => {
    if (leaveNode.data.data.type === 'partition')
        delete leaveNode.data.data.dragHover;
}

const uploading = ref(false);

const upload = () => {
    let hasErrorQuestions = QuestionTempStorage.getErrorQuestions().length > 0;
    if (hasErrorQuestions && showTree.value === true) {
        errorsDisplay.value = true;
        showTree.value = false;
    } else if (hasErrorQuestions && showTree.value === false) {
        uploading.value = true;
        QuestionTempStorage.uploadAll().then(() => {
            uploading.value = false;
        });
    } else if (!hasErrorQuestions) {
        uploading.value = true;
        QuestionTempStorage.uploadAll().then(() => {
            uploading.value = false;
        }, () => {
            uploading.value = false;
        });
    }
    // QuestionTempStorage.uploadAll();
}

const backToTree = () => {
    showTree.value = true;
    setTimeout(() => {
        errorsDisplay.value = false;
    }, 600);
}

const showCheckBox = ref(false);
const switchShowCheckBox = () => {
    showCheckBox.value = !showCheckBox.value;
}

const disabled = ref(false);

onMounted(() => {
    function watchKeys() {
        watch(() => tree.value.getCheckedKeys(), () => {
            if (tree.value.getCheckedKeys().length > 0) disabled.value = false;
            else disabled.value = true;
        });
    }

    if (tree.value !== undefined) {
        watchKeys();
    } else {
        watch(() => tree.value, () => {
            watchKeys();
        }, {once: true});
    }
})

const batchActionMenuButtons = ref([
    {
        name: "移动题目从所选分区到...",
        show: () => {
            return true;
        },
        action: () => {
        },
        menuVisible: false
    }, {
        name: "复制题目到...",
        show: () => {
            return true;
        },
        action: () => {
        },
        menuVisible: false
    }]
);

const batchActionButtons = ref([
    {
        name: "从所选分区删除题目",
        show: () => {
            return true;
        },
        action: () => {
            const checkedKeys = tree.value.getCheckedKeys();
            console.log(checkedKeys);
            for (const key of checkedKeys) {
                let split = key.toString().split("/");
                if (split.length === 2) {
                    const partitionId = Number(split[0]);
                    const questionId = split[1];
                    if (questionId === "createQuestion") continue;
                    QuestionTempStorage.getAsync(questionId).then((questionInfo) => {
                        questionInfo.question.partitionIds = questionInfo.question.partitionIds.filter(id => id !== partitionId);
                        QuestionTempStorage.update(questionInfo);
                    });
                }
            }
        },
    }]
);

const switchMenuVisible = (button) => {
    for (const button1 of batchActionMenuButtons.value) {
        button1.menuVisible = false;
    }
    button.menuVisible = !button.menuVisible
}

const rectifyCheck = (nodeObj, checkStatus) => {
    if (nodeObj.data.type === "question") {
        const currentPartitionId = Number(nodeObj.treeId.split("/")[0]);
        const currentPartitionCheckedQuestionIds = new Set();
        for (const checkedNode of checkStatus.checkedNodes) {
            let partitionId = Number(checkedNode.treeId.split("/")[0]);
            if (partitionId === currentPartitionId && checkedNode.data.type === "question") {
                currentPartitionCheckedQuestionIds.add(checkedNode.data.question.id);
            }
        }
        for (const halfCheckedNode of checkStatus.halfCheckedNodes) {
            if (halfCheckedNode.data.type === "partition" && halfCheckedNode.id === currentPartitionId) {
                if (currentPartitionCheckedQuestionIds.size === 0) {
                    tree.value.setChecked(halfCheckedNode.id + "/createQuestion", false, false);
                }
                if (currentPartitionCheckedQuestionIds.size === tree.value.getNode(currentPartitionId).childNodes.length - 1) {
                    tree.value.setChecked(halfCheckedNode.id + "/createQuestion", true, false);
                }
                break;
            }
        }
    }
}
</script>

<template>
    <div style="display: flex;flex-direction: row;align-content: stretch;align-items: stretch">
        <splitpanes style="flex: 1">
            <pane min-size="20" size="30">
                <div class="panel" style="display: flex" v-loading="loading">
                    <el-input prefix-icon="Search" v-model="filterText" placeholder="搜索 (以 &quot;,&quot; 分词)"/>
                    <el-button type="primary" style="margin-top: 8px" @click="upload" :loading="uploading"
                               loading-icon="_Loading_"
                               :disabled="!QuestionTempStorage.reactiveDirty.value">
                        <HarmonyOSIcon_Upload/>
                        <el-text>{{ showTree ? "上传题目更改" : "确认上传" }}</el-text>
                    </el-button>
                    <!--                    <el-button @click="showTree = !showTree">test</el-button>-->
                    <el-scrollbar>
                        <div class="slide-base" style="display: flex;flex-direction: row">
                            <div class="question-tree-base" :class="{hideLeft:!showTree}">
                                <el-button-group>
                                    <el-button text style="width:106px" @click="switchShowCheckBox">
                                        <HarmonyOSIcon_CheckBox style="margin: 0;margin-right: 4px"/>
                                        <el-text v-if="!showCheckBox">批量</el-text>
                                        <el-text v-else>取消批量</el-text>
                                    </el-button>
                                    <el-popover width="500" :visible="button.menuVisible"
                                                v-for="button in batchActionMenuButtons">
                                        <template #reference>
                                            <transition name="batch-buttons">
                                                <el-button text v-show="showCheckBox&&button.show()"
                                                           :disabled="disabled"
                                                           @click="switchMenuVisible(button);">
                                                    <el-text>{{ button.name }}</el-text>
                                                </el-button>
                                            </transition>
                                        </template>
                                        <template #default>
                                            <select-partitions-action-dialog
                                                @on-confirm="button.action"
                                                @on-cancel="button.menuVisible = false"
                                            />
                                        </template>
                                    </el-popover>
                                    <transition-group name="batch-buttons">
                                        <el-button text v-show="showCheckBox&&button.show()"
                                                   v-for="button in batchActionButtons"
                                                   :key="button" :disabled="disabled" @click="button.action">
                                            <el-text>{{ button.name }}</el-text>
                                        </el-button>
                                    </transition-group>
                                </el-button-group>
                                <el-tree ref="tree" icon="ArrowRightBold" node-key="treeId" :props="props"
                                         @node-click="onEdit" :show-checkbox="showCheckBox"
                                         draggable :allow-drag="allowDrag" :allow-drop="allowDrop" @node-drop="onDrop"
                                         @node-drag-enter="dragEnter" @node-drag-leave="dragLeave"
                                         @node-drag-end="onDragEnd" @check="rectifyCheck"
                                         lazy :load="loadNode" :filter-node-method="filterNode">
                                    <template #default="{ node : proxy, data : nodeObj }">
                                        <template v-if="nodeObj.data.type==='createPartition'">
                                            <create-partition-button/>
                                        </template>
                                        <template v-else-if="nodeObj.data.type==='createQuestion'">
                                            <!--                                            TODO component-->
                                            <el-popover trigger="click" width="280">
                                                <template #reference>
                                                    <el-button text size="small"
                                                               class="flex-blank-1 disable-tree-item-hover disable-tree-item-focus disable-tree-checkbox">
                                                        <HarmonyOSIcon_Plus style="margin: 8px"/>
                                                        <el-text>创建题目</el-text>
                                                    </el-button>
                                                </template>
                                                <template #default>
                                                    <div class="no-pop-padding create-selection">
                                                        <el-button-group>
                                                            <el-button disabled>题组</el-button>
                                                            <el-button
                                                                @click="createQuestion(nodeObj.data.partitionId,'select')">
                                                                选择题
                                                            </el-button>
                                                            <el-button disabled>排序题</el-button>
                                                            <el-button disabled>填空题</el-button>
                                                        </el-button-group>
                                                    </div>
                                                </template>
                                            </el-popover>
                                        </template>
                                        <template v-else>
                                            <!--                                            TODO component-->
                                            <div class="tree-node-item" :class="{dragHover:nodeObj.data.dragHover}">
                                                <div class="point" :class="{
                                                changed:nodeObj.data.dirty,
                                                error:nodeObj.data.showError,
                                                warning:nodeObj.data.showWarning
                                            }"></div>
                                                <div class="question-tree-node">
                                                    <el-text class="question-tree-node-content">
                                                        {{
                                                            nodeObj.data.type === 'question' ? nodeObj.data.question.content : nodeObj.data.partition.name
                                                        }}
                                                    </el-text>
                                                </div>
                                                <el-button-group>
                                                    <el-popover trigger="click" v-model:visible="nodeObj.data.editing"
                                                                width="400" v-if="nodeObj.data.type === 'partition'">
                                                        <template #reference>
                                                            <el-button class="node-button" size="small" link
                                                                       @click.stop="nodeObj.data.editing = false"
                                                                       v-if="nodeObj.data.type === 'partition'">
                                                                <HarmonyOSIcon_Rename/>
                                                                <el-text>重命名</el-text>
                                                            </el-button>
                                                        </template>
                                                        <template #default>
                                                            <EditPartitionNameDialog
                                                                @on-over="nodeObj.data.editing = false"
                                                                :partition="nodeObj.data.partition"
                                                                size="default"/>
                                                        </template>
                                                    </el-popover>
                                                    <el-button class="node-button" size="small" link
                                                               @click.stop="nodeObj.data.type==='question'?onDeleteQuestion(nodeObj.data.question.id):onDeletePartition(nodeObj.id)">
                                                        <HarmonyOSIcon_Remove/>
                                                        <el-text>删除</el-text>
                                                    </el-button>
                                                </el-button-group>
                                            </div>
                                        </template>
                                    </template>
                                </el-tree>
                            </div>
                            <div class="errorsList" v-show="errorsDisplay" :class="{hideRight:showTree}">
                                <el-scrollbar>
                                    <el-alert type="warning" :closable="false">
                                        <el-button style="padding: 8px" text @click="backToTree">
                                            <el-icon>
                                                <ArrowLeftBold/>
                                            </el-icon>
                                        </el-button>
                                        <el-text type="warning">
                                            这些有错误的题目将不会上传
                                        </el-text>
                                    </el-alert>
                                    <div class="errorQuestions">
                                        <transition-group name="errorQuestions">
                                            <template v-for="questionInfo of QuestionTempStorage.getErrorQuestions()"
                                                      :key="questionInfo.question.id">
                                                <question-info-panel @click="openEdit(questionInfo.question.id)"
                                                                     :question-info="questionInfo"/>
                                            </template>
                                        </transition-group>
                                    </div>
                                    <transition name="empty">
                                        <el-empty v-show="QuestionTempStorage.getErrorQuestions().length===0"/>
                                    </transition>
                                </el-scrollbar>
                            </div>
                        </div>
                    </el-scrollbar>
                </div>
            </pane>
            <pane min-size="50">
                <div class="panel" style="padding: 0">
                    <router-view v-slot="{ Component }">
                        <transition mode="out-in" name="question-editor">
                            <component :is="Component"/>
                        </transition>
                    </router-view>
                </div>
            </pane>
        </splitpanes>
    </div>
</template>

<style scoped>
.question-tree-node {
    display: flex;
    flex-direction: row;
    align-items: center;
    flex: 1;
}

.tree-node-item {
    display: flex;
    flex-direction: row;
    flex: 1;
    transition: 200ms var(--ease-in-out-quint);
    border-radius: 4px;
}

.question-tree-node-content {
    width: 0;
    flex: 1;
}

.node-button {
    border: 2px var(--panel-bg-color-overlay) !important;
    overflow: hidden;
    @media (prefers-color-scheme: dark) {
        background: #1b1b1b !important;
    }
    @media (prefers-color-scheme: light) {
        background: #e4e4e4 !important;
    }
}

.point {
    scale: 0;
    width: 4px;
    height: 4px;
    margin-right: 4px;
    border-radius: 50%;
    align-self: center;
    transition: 300ms var(--ease-in-bounce);
}

.point.changed {
    scale: 1;
    background: var(--el-color-primary);
}

.point.warning {
    scale: 1;
    background: var(--el-color-warning);
}

.point.error {
    scale: 1;
    background: var(--el-color-danger);
}

.dragHover {
    background: var(--el-color-primary-1) !important;
}

.dragHover * {
    color: var(--front-color-dark) !important;
}

/*noinspection CssUnusedSymbol*/
.question-editor-enter-active, .question-editor-leave-active {
    transition: all 600ms var(--ease-in-out-quint);
}

/*noinspection CssUnusedSymbol*/
.batch-buttons-enter-active {
    transition: padding-top 400ms var(--ease-in-out-quint),
    padding-bottom 400ms var(--ease-in-out-quint),
    height 400ms var(--ease-in-out-quint),
    filter 200ms var(--ease-in-out-quint) 450ms,
    opacity 200ms var(--ease-out-quint) 450ms !important;
}

/*noinspection CssUnusedSymbol*/
.batch-buttons-leave-active {
    transition: padding-top 400ms var(--ease-in-out-quint) 250ms,
    padding-bottom 400ms var(--ease-in-out-quint) 250ms,
    height 400ms var(--ease-in-out-quint) 250ms,
    filter 200ms var(--ease-in-out-quint),
    opacity 200ms var(--ease-out-quint) !important;
}

/*noinspection CssUnusedSymbol*/
.batch-buttons-enter-from, .batch-buttons-leave-to {
    height: 0;
    padding-bottom: 0;
    padding-top: 0;
    filter: blur(8px);
    opacity: 0 !important;
}

/*noinspection CssUnusedSymbol*/
.batch-buttons-enter-to, .batch-buttons-leave-from {
    /*padding-bottom: 8px;*/
    /*    padding-top: 8px;*/
    height: 32px;
}

/*noinspection CssUnusedSymbol*/
.question-editor-enter-from, .question-editor-leave-to {
    opacity: 0;
    scale: 0.95;
    filter: blur(32px);
}

/*TODO width*/

.create-selection {
    display: flex;
    flex-direction: row;
}

/*noinspection CssUnusedSymbol*/

.question-tree-base, .errorsList {
    transition: 600ms var(--ease-in-bounce-1);
}

.slide-base .question-tree-base,
.slide-base .errorsList {
    width: 100%;
    min-width: 100%;
    max-width: 100%;
}

.question-tree-base.hideLeft {
    margin-left: -100%;
}

.errorsList.hideRight {
    margin-right: -100%;
}

.errorQuestions {
    display: grid;
    overflow: hidden;
    gap: 0;
    outline: 0;
}

.errorQuestions-leave-active {
    --phrase-1: 350ms;
    --phrase-2: 250ms;
    transition: margin-left var(--phrase-1) var(--ease-in-quint),
    margin-right var(--phrase-1) var(--ease-in-quint),
    grid-template-rows var(--phrase-2) var(--ease-in-out-quint) var(--phrase-1) !important;
    overflow: hidden;
}

.errorQuestions-enter-active {
    --phrase-1: 250ms;
    --phrase-2: 350ms;
    transition: margin-left var(--phrase-2) var(--ease-out-quint) var(--phrase-1),
    margin-right var(--phrase-2) var(--ease-out-quint) var(--phrase-1),
    grid-template-rows var(--phrase-1) var(--ease-in-out-quint) !important;
    overflow: hidden;
}

.errorQuestions-enter-from,
.errorQuestions-leave-to {
    margin-left: -100%;
    margin-right: 100%;
    grid-template-rows: 0fr;
}

.empty-enter-active,
.empty-leave-active {
    transition: 400ms var(--ease-in-out-quint);
}

.empty-enter-from,
.empty-leave-to {
    scale: 0.9;
    filter: blur(8px);
    opacity: 0;
}

.error-question-info-panel > .grid1 > .padding > div {
    min-height: 25px;
}

.errorsDescription > * {
    margin-right: 8px;
}

.choicesList > * {
    min-width: 40px;
    margin-right: 4px;
}
</style>