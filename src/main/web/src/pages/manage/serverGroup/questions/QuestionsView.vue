<!--suppress ALL -->
<script setup>
import "splitpanes/dist/splitpanes.css"
import router from "@/router/index.js";
import QuestionCache from "@/data/QuestionCache.js";
import PartitionCache from "@/data/PartitionCache.js";
import randomUUIDv4 from "@/utils/UUID.js";
import HarmonyOSIcon_Plus from "@/components/icons/HarmonyOSIcon_Plus.vue";
import HarmonyOSIcon_CheckBox from "@/components/icons/HarmonyOSIcon_CheckBox.vue";
import EditPartitionNameDialog from "@/components/question/EditPartitionNameDialog.vue";
import HarmonyOSIcon_Upload from "@/components/icons/HarmonyOSIcon_Upload.vue";
import HarmonyOSIcon_Remove from "@/components/icons/HarmonyOSIcon_Remove.vue";
import HarmonyOSIcon_Rename from "@/components/icons/HarmonyOSIcon_Rename.vue";
import {ArrowLeftBold, Lock, RefreshLeft} from "@element-plus/icons-vue";
import CreatePartitionButton from "@/components/question/CreatePartitionButton.vue";
import QuestionInfoPanel from "@/components/question/QuestionInfoPanel.vue";
import SelectPartitionsActionPop from "@/components/question/SelectPartitionsActionPop.vue";
import ResponsiveSplitpane from "@/components/common/ResponsiveDoubleSplitpane.vue";
import {ElMessageBox} from "element-plus";
import PermissionInfo from "@/auth/PermissionInfo.js";
import UserDataInterface from "@/data/UserDataInterface.js";
import _Loading_ from "@/components/common/_Loading_.vue";

const props = {
    label: 'name',
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
                (questionInfo.type === 'Partition') || (questionInfo.type === 'Question' && (
                        (questionInfo.question.content && questionInfo.question.content.toUpperCase().includes(v.toUpperCase()))))
        ) {
            return true;
        }
    }
    return false;
}

watch(filterText, (val) => {
    tree.value.filter(val);
});

function loadPartitionChildrenNode(partitionNodeObj) {
    return new Promise((resolve, reject) => {
        const partitionId = partitionNodeObj.treeId;
        let createQuestionButtonData = {
            treeId: partitionId + "/create-question",
            leaf: true,
            data: {
                type: "createQuestion",
                partitionId: partitionId
            }
        };
        if (partitionNodeObj.data.partition.loaded) {
            const resolveData = [createQuestionButtonData];
            resolveData.push(...Object.values(partitionNodeObj.data.partition.questionNodes));
            resolve(resolveData);
        } else {
            const previousQuestionNodes = partitionNodeObj.data.partition.questionNodes;
            let resolveData = [createQuestionButtonData];
            if (previousQuestionNodes) {
                resolveData.push(...Object.values(previousQuestionNodes));
            }
            QuestionCache.getQuestionSimpleDataByPartitionId(partitionId).then((questionInfos) => {
                for (let questionInfo of questionInfos) {
                    const questionId = questionInfo.question.id;
                    if (!previousQuestionNodes[questionId]) {
                        const questionNodeObj = QuestionCache.getQuestionNodeObjOf(questionInfo, partitionId);
                        resolveData.push(questionNodeObj);
                        partitionNodeObj.data.partition.questionNodes[questionId] = questionNodeObj;
                    }
                }
                nextTick(() => {
                    tree.value.filter(filterText.value);
                });
                partitionNodeObj.data.partition.loaded = true;
                resolve(resolveData);
            }, (error) => {
                reject();
            });
        }
    });
}

const loadNode = (node, resolve, reject) => {
    const nodeObj = node.data;
    if (node.level === 0) {
        let createPartitionButtonData = {
            treeId: "create-partition",
            leaf: true,
            data: {type: "createPartition"}
        };
        PartitionCache.getRefPartitionsAsync().then((partitions) => {
            let data = [];
            if (PermissionInfo.hasPermission('partition', 'create partition')) {
                data.push(createPartitionButtonData)
            }
            for (const [id, partition] of Object.entries(partitions.value)) {
                data.push(getTreeNodeDataOfPartition(partition));
            }
            loading.value = false;
            resolve(data);
        });
    } else {
        loadPartitionChildrenNode(nodeObj).then(
                (data) => {
                    resolve(data);
                }, (data) => {
                    reject(data);
                }
        );
    }
}

function getTreeNodeDataOfPartition(partition) {
    return {
        treeId: partition.id,
        data: {
            editing: false,
            partition: partition,
            type: "Partition"
        }
    };
}

const unregister1 = PartitionCache.registerOnPartitionAdded((partition) => {
    partition.questionNodes = {};
    tree.value.append(getTreeNodeDataOfPartition(partition), tree.value.root);
});
const unregister2 = PartitionCache.registerOnPartitionDeleted((partition) => {
    tree.value.remove(tree.value.getNode(partition.id));
    QuestionCache.removePartitionFromAllQuestions(partition);
});
const unregister3 = QuestionCache.registerOnQuestionUpdateLocal((questionInfo, differFromOriginal) => {
    for (let partitionId of questionInfo.question.partitionIds) {
        if (tree.value.getNode(partitionId + "/" + questionInfo.question.id) === null) {
            const partitionNode = tree.value.getNode(partitionId);
            const questionNodeObj = QuestionCache.getQuestionNodeObjOf(questionInfo, partitionId);
            if (partitionNode !== null)
                tree.value.append(questionNodeObj, partitionNode);
            partitionNode.data.data.partition.questionNodes[questionNodeObj.data.question.id] = questionNodeObj;
        }
    }
    for (const [id, partition] of Object.entries(PartitionCache.refPartitions.value)) {
        if (questionInfo.question.partitionIds && questionInfo.question.partitionIds.includes(partition.id)) {
            continue;
        }
        const partitionNode = tree.value.getNode(partition.id);
        if (tree.value.getNode(partition.id) !== null) {
            let questionNode = tree.value.getNode(partition.id + "/" + questionInfo.question.id);
            if (questionNode !== null) {
                tree.value.remove(questionNode);
            }
        }
        delete partitionNode.data.data.partition.questionNodes[questionInfo.question.id];
    }
});

const unregister4 = QuestionCache.registerOnQuestionDeleted((id, localDeleted) => {//FIXME won't remove questions in closed partitions
    let questionInfo = QuestionCache.reactiveQuestionInfos.value[id];
    if ((questionInfo && questionInfo.dirty) || (!localDeleted && router.currentRoute.value.params.id === id)) {
        return;
    }
    if (router.currentRoute.value.params.id === id) {
        router.push({name: "questions"});
    }
    for (const [partitionId, partition] of Object.entries(PartitionCache.refPartitions.value)) {
        let questionNode = tree.value.getNode(partition.id + "/" + id);
        if (questionNode !== null) {
            nextTick(() => {
                tree.value.remove(questionNode);
            });
        }
    }
});

onBeforeUnmount(() => {
    unregister1();
    unregister2();
    unregister3();
    unregister4();
});

const openEdit = (questionId) => {
    router.push({
        name: 'question-detail', params: {
            id: questionId
        }
    });
}

const onEdit = (nodeObj, nodeItem, node, event) => {
    nodeObj.data.type === 'Question' || nodeObj.data.type === 'QuestionGroup' ? openEdit(nodeObj.data.question.id) : null
}

const allowDrag = (node) => {
    const dataType = node.data.data.type;
    return dataType === 'Question' || dataType === "QuestionGroup";
}

const allowDrop = (draggingNode, dropNode, type) => {
    let nodeDataType = dropNode.data.data.type;
    return ((nodeDataType === "Partition" || nodeDataType === "QuestionGroup") && type === "inner") ||
            (nodeDataType !== "Partition" && nodeDataType !== "createPartition" && type === "next");
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
            if (originalItemPartitionId === partitionId) {
                dragNode.treeId = partitionId + "/" + dragNodeData.question.id;
                dragNode.data.data.question.partitionIds.splice(index, 1, dropPartitionId);
                break;
            }
            index++
        }
        dragNodeData.question.partitionIds = Array.from(new Set(dragNodeData.question.partitionIds));
        QuestionCache.update(dragNodeData);
    }

    let dropPartitionId;
    delete dropNode.data.data.dragHover;
    const dataType = dropNodeData.type;
    if (dataType === "Partition") {
        dropPartitionId = dropNodeData.partition.id;
    } else if (dataType === "Question" || dataType === "QuestionGroup") {
        dropPartitionId = dropNode.data.treeId.split("/")[0];
    } else if (dataType === "createQuestion") {
        dropPartitionId = dropNode.data.data.partitionId;
    } else {
        return;
    }
    if (dragNodeData.simple) {
        QuestionCache.getAsync(dragNodeData.question.id).then((questionInfo) => {
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

const createQuestionGroup = (partitionId) => {
    if (PermissionInfo.hasPermission('question group', 'create and edit owns question groups')) {
        const authorQQ = Number(UserDataInterface.currentUser.value.qq);
        const id = randomUUIDv4();
        let question = {
            id: id,
            content: "",
            enabled: false,
            partitionIds: [partitionId],
            authorQQ: authorQQ,
            upVoters: [],
            downVoters: [],
            localNew: true,
        };
        const questionInfo = QuestionCache.createQuestionGroup(question, undefined, authorQQ);
        const questionNodeObj = QuestionCache.getQuestionNodeObjOf(questionInfo, partitionId);
        const partitionNode = tree.value.getNode(partitionId);
        partitionNode.data.data.partition.questionNodes[id] = questionNodeObj;
        tree.value.append(questionNodeObj, partitionNode);
    } else {
        console.error('Permission denied: create and edit owns question groups');
    }
}

const createMultipleChoiceQuestion = (partitionId) => {
    if (PermissionInfo.hasPermission('question', 'create and edit owns questions')) {
        const id = randomUUIDv4();
        let question = {
            id: id,
            content: "",
            type: "MultipleChoicesQuestion",
            enabled: false,
            partitionIds: [partitionId],
            authorQQ: Number(UserDataInterface.currentUser.value.qq),
            upVoters: [],
            downVoters: [],
            localNew: true,
            choices: [{
                id: randomUUIDv4(),
                correct: true,
                content: ""
            }, {
                id: randomUUIDv4(),
                correct: false,
                content: ""
            }]
        };
        const questionInfo = QuestionCache.create(question);
        const partitionNode = tree.value.getNode(partitionId);
        const questionNode = QuestionCache.getQuestionNodeObjOf(questionInfo, partitionId);
        partitionNode.data.data.partition.questionNodes[id] = questionNode;
        tree.value.append(questionNode, partitionNode);
    } else {
        console.error('Permission denied: create and edit owns questions');
    }
}

const dragEnter = (dragNode, enterNode, event) => {
    if (enterNode.data.data.type === "Partition")
        enterNode.data.data.dragHover = true;
}

const dragLeave = (dragNode, leaveNode, event) => {
    if (leaveNode.data.data.type === "Partition")
        delete leaveNode.data.data.dragHover;
}

const uploading = ref(false);

const failedQuestionIdReason = ref({});
const showFailedQuestionIdReason = ref(false);

const upload = () => {
    let hasErrorQuestions = QuestionCache.getDirtyErrorQuestions().length > 0;
    const doUpload = () => {
        QuestionCache.uploadAll().then((data) => {
            uploading.value = false;
            console.log(data);
            console.log(data.succeedUpdatedQuestionIds);
            console.log(data.failedQuestionIdReason);
            failedQuestionIdReason.value = data.failedQuestionIdReason;
            let showFailedQuestionIdReason1 = Object.keys(failedQuestionIdReason.value).length > 0;
            showFailedQuestionIdReason.value = showFailedQuestionIdReason1;
            showTree.value = !showFailedQuestionIdReason1;
            errorsDisplay.value = !showFailedQuestionIdReason1;
            if (showFailedQuestionIdReason1) {
                responsiveSplitpane.value.showLeft();
            }
        }, (err) => {
            uploading.value = false;
        });
    }
    if (hasErrorQuestions && showTree.value === true) {
        errorsDisplay.value = true;
        showTree.value = false;
        responsiveSplitpane.value.showLeft();
    } else if (hasErrorQuestions && showTree.value === false) {
        uploading.value = true;
        doUpload();
    } else if (!hasErrorQuestions) {
        uploading.value = true;
        doUpload();
    }
    // QuestionTempStorage.uploadAll();
}

const backToTree = () => {
    showTree.value = true;
    setTimeout(() => {
        if (showTree.value === true) {
            errorsDisplay.value = false;
            showFailedQuestionIdReason.value = false;
            failedQuestionIdReason.value = {};
        }
    }, 600);
}

const showCheckBox = ref(false);
const switchShowCheckBox = () => {
    showCheckBox.value = !showCheckBox.value;
    if (showCheckBox.value === false && currentButton && currentButton.value) {
        currentButton.value.menuVisible = false;
    }
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

function batchDo(questionInfoAction, questionNodeObjAction) {
    const checkedNodes = tree.value.getCheckedNodes();
    const checkedQuestionIds = new Set();
    const checkedQuestionNodes = new Set();
    for (const nodeObj of checkedNodes) {
        if (nodeObj.data.type === "Question" || nodeObj.data.type === "QuestionGroup") {
            let split = nodeObj.treeId.split("/");
            const questionId = split[1];
            if (questionId === "create-question") continue;
            checkedQuestionIds.add(questionId);
            checkedQuestionNodes.add(nodeObj);
        } else if (nodeObj.data.type === "Partition" && nodeObj.data.partition.loaded) {
            for (const questionNode of Object.values(nodeObj.data.partition.questionNodes)) {
                if (questionNode.data.type === "Question" || questionNode.data.type === "QuestionGroup") {
                    checkedQuestionIds.add(questionNode.data.question.id);
                    checkedQuestionNodes.add(questionNode);
                }
            }
        }
    }
    QuestionCache.getAllAsync(Array.from(checkedQuestionIds)).then(questionInfos => {
        if (questionInfoAction instanceof Function) {
            for (const questionInfo of questionInfos) {
                questionInfoAction(questionInfo);
            }
        }
        if (questionNodeObjAction instanceof Function) {
            for (const questionNode of checkedQuestionNodes) {
                questionNodeObjAction(questionNode);
            }
        }
    });
}

function batchSetEnable(enable) {
    batchDo((questionInfo) => {
        questionInfo.question.enabled = enable;
        QuestionCache.update(questionInfo);
    });
}

function batchSetEnableRandom(enable) {
    batchDo((questionInfo) => {
        questionInfo.question.randomOrdered = enable;
        QuestionCache.update(questionInfo);
    });
}

const batchActionSelectPartitionMenuButtons = ref([
    {
        name: "从所选分区移动到...",
        show: () => {
            return true;
        },
        action: (partitionIds) => {
            currentButton.menuVisible = false;
            const partitionIdsSet = new Set(partitionIds);
            batchDo(undefined, (questionNodeObj) => {
                const partitionId = questionNodeObj.treeId.split("/")[0];
                questionNodeObj.data.question.partitionIds = questionNodeObj.data.question.partitionIds.filter(id => id !== partitionId && !partitionIdsSet.has(id));
                questionNodeObj.data.question.partitionIds.push(...partitionIds);
                nextTick(() => {
                    QuestionCache.update(questionNodeObj.data);
                });
            });
        },
        menuVisible: false
    }, {
        name: "添加至分区...",
        show: () => {
            return true;
        },
        action: (partitionIds) => {
            currentButton.menuVisible = false;
            const partitionIdsSet = new Set(partitionIds);
            batchDo((questionInfo) => {
                questionInfo.question.partitionIds = questionInfo.question.partitionIds.filter(id => !partitionIdsSet.has(id));
                questionInfo.question.partitionIds.push(...partitionIds);
                QuestionCache.update(questionInfo);
            });
        },
        menuVisible: false
    }]
);

const batchActionButtons = ref([
    {
        name: "从所选分区删除",
        show: () => {
            return true;
        },
        action: () => {
            batchDo(undefined, (questionNodeObj) => {
                if (questionNodeObj.data.question.partitionIds.length === 1) {
                    QuestionCache.delete(questionNodeObj.data.question.id);
                } else {
                    const partitionId = questionNodeObj.treeId.split("/")[0];
                    questionNodeObj.data.question.partitionIds = questionNodeObj.data.question.partitionIds.filter(id => id !== partitionId);
                    nextTick(() => {
                        QuestionCache.update(questionNodeObj.data);
                    });
                }
            });
        },
    }, {
        name: "启用",
        show: () => {
            return true;
        },
        action: () => {
            batchSetEnable(true);
        },
    }, {
        name: "禁用",
        show: () => {
            return true;
        },
        action: () => {
            batchSetEnable(false);
        },
    }, /*{
        name: "启用乱序",
        show: () => {
            return true;
        },
        action: () => {
            batchSetEnableRandom(true);
        },
    }, {
        name: "禁用乱序",
        show: () => {
            return true;
        },
        action: () => {
            batchSetEnableRandom(false);
        },
    }*/]
);

const switchMenuVisible = (button) => {
    if (button.menuVisible) {
        button.menuVisible = false;
    } else {
        for (const button1 of batchActionSelectPartitionMenuButtons.value) {
            button1.menuVisible = false;
        }
        button.menuVisible = true;
    }
}

const rectifyCheck = (nodeObj, checkStatus) => {
    function rectify1(currentPartitionNode) {
        const childNodes = currentPartitionNode.childNodes;
        const hasQuestion = childNodes.find((childNode) => childNode.type === "Question" || childNode.type === "QuestionGroup");
        const hasNotCheckedQuestion = childNodes.find((childNode) => childNode.data.data.type !== "createQuestion" && (!childNode.checked));
        const hasCheckedQuestion = childNodes.find((childNode) => childNode.data.data.type !== "createQuestion" && childNode.checked);
        const allQuestionChecked = !hasNotCheckedQuestion;
        const allQuestionNotChecked = !hasCheckedQuestion;
        console.log(allQuestionChecked, allQuestionNotChecked);
        if (hasQuestion) {
            if (allQuestionChecked) {
                tree.value.setChecked(childNodes[0].data.treeId, true, false);
            } else if (allQuestionNotChecked) {
                tree.value.setChecked(childNodes[0].data.treeId, false, false);
            }
        } else {
            tree.value.setChecked(childNodes[0].data.treeId, false, false);
        }
        console.log(currentPartitionNode);
    }

    const dataType = nodeObj.data.type;
    const currentPartitionId = nodeObj.treeId.split("/")[0];
    const partitionNode = tree.value.getNode(currentPartitionId);
    if (dataType === "Question" || dataType === "QuestionGroup") {
        rectify1(partitionNode);
    } else if (dataType === "Partition") {
        if (!nodeObj.data.partition.loaded) {
            loadPartitionChildrenNode(nodeObj).then((data) => {
                for (const questionNode of data) {
                    tree.value.append(questionNode, nodeObj);
                }
                nextTick(() => {
                    tree.value.setChecked(nodeObj.treeId, true, true);
                });
            });
        } else if (!partitionNode.checked) {
            rectify1(partitionNode);
        }
    } else if (nodeObj.treeId !== "create-partition") {
        rectify1(partitionNode);
    }
}

function onDeleteNode(nodeObj) {
    if (nodeObj.data.type === 'Question' || nodeObj.data.type === 'QuestionGroup') {
        if (nodeObj.data.question.localDeleted) {
            QuestionCache.restoreDelete(nodeObj.data.question.id);
        } else {
            QuestionCache.delete(nodeObj.data.question.id);
        }
    } else {
        const partition = tree.value.getNode(nodeObj.treeId).data.data.partition;
        const alartNotEmpty = () => {
            ElMessageBox.confirm(
                    "删除分区时将把所属的所有题目从分区中移除，若题目无其他隶属分区，则将被删除",
                    "该分区非空",
                    {
                        showClose: false,
                        draggable: true,
                        confirmButtonText: "确定",
                        cancelButtonText: "取消",
                        type: "warning"
                    }
            ).then(() => {
                doDelete();
            }).catch(() => {
            });
        }
        const doDelete = () => {
            PartitionCache.tryDeleteRemote(partition.id);
        }
        if (partition.questionAmount !== 0) {
            alartNotEmpty();
        } else {
            doDelete();
        }
        // });
    }
}

const currentButton = ref({
    menuVisible: false
});

const responsiveSplitpane = ref();

const stop = router.afterEach((to, from) => {
    if (errorsDisplay.value || to.params.id === undefined) {
        responsiveSplitpane.value.showLeft();
    } else {
        responsiveSplitpane.value.hideLeft();
    }
});

onUnmounted(() => {
    stop();
});

const restoreAllErrorUploadChanges = () => {
    for (const id of Object.keys(failedQuestionIdReason.value)) {
        QuestionCache.restoreChanges(id);
    }
}

const getTypeName = (obj) => {
    if (obj.question) {
        const type1 = obj.question.type;
        if (type1 === "MultipleChoicesQuestion") return "选择题";
        else if (type1 === "QuestionGroup") return "题组";
        else return "未知";
    } else if (obj.partition) {
        return "分区";
    } else {
        return "未知";
    }
}
</script>

<template>
    <responsive-splitpane ref="responsiveSplitpane" :left-loading="loading" show-left-label="题目列表">
        <template #left>
            <el-input prefix-icon="Search" v-model="filterText" placeholder="搜索 (以 &quot;;&quot; 分词)"/>
            <el-button type="primary" style="margin-top: 8px" @click="upload" :loading="uploading"
                       :loading-icon="_Loading_" :icon="HarmonyOSIcon_Upload"
                       :disabled="!QuestionCache.reactiveDirty.value">
                <el-text>{{ errorsDisplay && !showTree ? "确认上传" : "上传题目更改" }}</el-text>
            </el-button>
            <!--                    <el-scrollbar>-->
            <div class="slide-base"
                 style="display: flex;flex-direction: row;overflow-y:overlay;overflow-x: hidden;">
                <div class="question-tree-base" style="display: flex;flex-direction: column;overflow:overlay;"
                     :class="{hideLeft:!showTree}">
                    <div>
                        <el-button-group style="display: flex;flex-wrap: wrap">
                            <el-button text style="width:106px" @click="switchShowCheckBox">
                                <HarmonyOSIcon_CheckBox style="margin: 0;margin-right: 4px"/>
                                <el-text v-if="!showCheckBox">批量</el-text>
                                <el-text v-else>取消批量</el-text>
                            </el-button>
                            <transition-group name="batch-buttons">
                                <el-button text v-show="showCheckBox&&button.show()"
                                           class="action-button disable-init-animate"
                                           :class="{selected:button.menuVisible}"
                                           v-for="button in batchActionSelectPartitionMenuButtons" :key="button"
                                           :disabled="disabled"
                                           @click="currentButton=button;switchMenuVisible(button);">
                                    <el-text>{{ button.name }}</el-text>
                                </el-button>
                                <el-button text v-show="showCheckBox&&button.show()"
                                           class="action-button disable-init-animate"
                                           v-for="button in batchActionButtons"
                                           :key="button" :disabled="disabled" @click="button.action">
                                    <el-text>{{ button.name }}</el-text>
                                </el-button>
                            </transition-group>
                        </el-button-group>
                        <transition name="batch-buttons">
                            <select-partitions-action-pop
                                    v-show="currentButton.menuVisible && !disabled"
                                    @on-confirm="currentButton.action"/>
                        </transition>
                    </div>
                    <div style="flex:1;overflow:overlay;">
                        <el-scrollbar>
                            <div style="flex: 1">
                                <!--TODO Replace with virtual tree-->
                                <el-tree ref="tree" icon="ArrowRightBold" node-key="treeId" :props="props"
                                         @node-click="onEdit" :show-checkbox="showCheckBox"
                                         draggable :allow-drag="allowDrag" :allow-drop="allowDrop"
                                         @node-drop="onDrop"
                                         @node-drag-enter="dragEnter" @node-drag-leave="dragLeave"
                                         @node-drag-end="onDragEnd" @check="rectifyCheck"
                                         lazy :load="loadNode" :filter-node-method="filterNode">
                                    <template #default="{ node : proxy, data : nodeObj }">
                                        <template v-if="nodeObj.data.type==='createPartition'">
                                            <create-partition-button/>
                                        </template>
                                        <template v-else-if="nodeObj.data.type==='createQuestion'">
                                            <!--TODO component-->
                                            <el-popover trigger="click" popper-style="min-width: 80px;width: auto;">
                                                <template #reference>
                                                    <el-button text size="small" :icon="HarmonyOSIcon_Plus"
                                                               class="flex-blank-1 disable-tree-item-hover disable-tree-item-focus disable-tree-checkbox">
                                                        <el-text>创建题目</el-text>
                                                    </el-button>
                                                </template>
                                                <template #default>
                                                    <div class="no-pop-padding create-selection">
                                                        <el-button-group>
                                                            <el-button
                                                                    :disabled="!PermissionInfo.hasPermission('question group','create and edit owns question groups')"
                                                                    @click="createQuestionGroup(nodeObj.data.partitionId)">
                                                                题组
                                                            </el-button>
                                                            <el-button
                                                                    :disabled="!PermissionInfo.hasPermission('question','create and edit owns questions')"
                                                                    @click="createMultipleChoiceQuestion(nodeObj.data.partitionId)">
                                                                选择题
                                                            </el-button>
                                                            <!--<el-button disabled>排序题</el-button>-->
                                                            <!--<el-button disabled>填空题</el-button>-->
                                                        </el-button-group>
                                                    </div>
                                                </template>
                                            </el-popover>
                                        </template>
                                        <template v-else>
                                            <!--TODO component-->
                                            <div class="tree-node-item"
                                                 :class="{
                                                dragHover:nodeObj.data.dragHover
                                            }">
                                                <div class="pointer" :class="{
                                                            changed:nodeObj.data.dirty,
                                                            error:nodeObj.data.showError,
                                                            warning:nodeObj.data.showWarning
                                                        }"></div>
                                                <!--                                                :class="{'disable-tree-checkbox': !(nodeObj.data.type === 'Partition'?true:nodeObj.data.ableToEdit)}"-->
                                                <div class="question-tree-node"
                                                     :class="{'local-deleted': nodeObj.data.question&&nodeObj.data.question.localDeleted}">
                                                    <el-icon style="margin-right: 4px" v-if="nodeObj.data.question&&!nodeObj.data.ableToEdit">
                                                        <lock/>
                                                    </el-icon>
                                                    <el-text size="small" style="margin-right: 4px;"
                                                             :type="nodeObj.data.question && nodeObj.data.question.enabled?'primary':'info'">
                                                        {{ getTypeName(nodeObj.data) }}
                                                    </el-text>
                                                    <el-text class="question-tree-node-content"
                                                             :tag="nodeObj.data.question&&nodeObj.data.question.localDeleted?'del':undefined"
                                                             :class="{
                                                        'unable-to-edit': nodeObj.data.question&&!nodeObj.data.ableToEdit,
                                                             }">
                                                        {{
                                                            nodeObj.data.type === 'Partition' ?
                                                                    nodeObj.data.partition.name : nodeObj.data.question.content
                                                        }}
                                                    </el-text>
                                                    <el-button-group class="node-buttons" v-if="nodeObj.data.type === 'Question'">
                                                        <el-button class="node-button" size="small"
                                                                   v-if="nodeObj.data.ableToDelete"
                                                                   @click.stop="onDeleteNode(nodeObj)">
                                                            <el-icon :size="16" color="var(--front-color)"
                                                                     v-if="nodeObj.data.question?nodeObj.data.question.localDeleted:false">
                                                                <RefreshLeft/>
                                                            </el-icon>
                                                            <HarmonyOSIcon_Remove v-else/>
                                                        </el-button>
                                                    </el-button-group>
                                                    <el-button-group class="node-buttons" style="position: initial" v-if="nodeObj.data.type === 'Partition'">
                                                        <el-popover trigger="click"
                                                                    v-if="PermissionInfo.hasPermission('partition','edit partition name')"
                                                                    v-model:visible="nodeObj.data.editing"
                                                                    width="400">
                                                            <template #reference>
                                                                <el-button class="node-button" size="small"
                                                                           @click.stop="nodeObj.data.editing = false">
                                                                    <HarmonyOSIcon_Rename/>
                                                                </el-button>
                                                            </template>
                                                            <template #default>
                                                                <EditPartitionNameDialog
                                                                        @on-over="nodeObj.data.editing = false"
                                                                        :partition="nodeObj.data.partition"
                                                                        size="default"/>
                                                            </template>
                                                        </el-popover>
                                                        <el-button class="node-button" size="small"
                                                                   v-if="PermissionInfo.hasPermission('partition','delete partition')"
                                                                   @click.stop="onDeleteNode(nodeObj)">
                                                            <HarmonyOSIcon_Remove/>
                                                        </el-button>
                                                    </el-button-group>
                                                </div>
                                            </div>
                                        </template>
                                    </template>
                                </el-tree>
                            </div>
                        </el-scrollbar>
                    </div>
                </div>
                <transition :name="showTree?null:'blur-scale'" mode="out-in">
                    <div class="errorsList" v-if="errorsDisplay" :class="{hideRight:showTree}">
                        <el-scrollbar>
                            <el-alert type="warning" :closable="false">
                                <div style="display: flex;flex-wrap: wrap;width: 100%">
                                    <el-button style="padding: 8px;margin: 4px" text @click="backToTree">
                                        <el-icon>
                                            <ArrowLeftBold/>
                                        </el-icon>
                                    </el-button>
                                    <el-text type="warning" style="margin: 4px;align-self: center;">
                                        这些有错误的题目将不会上传
                                    </el-text>
                                </div>
                            </el-alert>
                            <transition name="blur-scale">
                                <div v-if="errorsDisplay">
                                    <transition-group name="slide-hide">
                                        <question-info-panel class="disable-init-animate clickable"
                                                             v-for="questionInfo of QuestionCache.getDirtyErrorQuestions()"
                                                             :key="questionInfo.question.id"
                                                             @click="openEdit(questionInfo.question.id)"
                                                             :question-info="questionInfo"/>
                                    </transition-group>
                                </div>
                            </transition>
                            <transition name="empty">
                                <el-empty v-show="QuestionCache.getDirtyErrorQuestions().length===0"/>
                            </transition>
                        </el-scrollbar>
                    </div>
                    <div class="errorsList" v-else-if="showFailedQuestionIdReason" :class="{hideRight:showTree}">
                        <el-scrollbar>
                            <el-alert type="error" :closable="false">
                                <div style="display: flex;flex-wrap: wrap;width: 100%">
                                    <el-button style="padding: 8px;margin: 4px" text @click="backToTree">
                                        <el-icon>
                                            <ArrowLeftBold/>
                                        </el-icon>
                                    </el-button>
                                    <el-text type="danger" style="margin: 4px;margin-right: 12px;align-self: center;">
                                        上传出错
                                    </el-text>
                                    <el-button link type="primary" :icon="RefreshLeft"
                                               style="margin: 4px;align-self: center;"
                                               @click="restoreAllErrorUploadChanges">
                                        重置所有更改
                                    </el-button>
                                </div>
                            </el-alert>
                            <transition name="blur-scale">
                                <div>
                                    <transition-group name="slide-hide">
                                        <question-info-panel class="disable-init-animate clickable"
                                                             v-for="[questionId, reasons] of Object.entries(failedQuestionIdReason)"
                                                             :key="questionId" disable-error-and-warning hide-status
                                                             @click="openEdit(questionId)"
                                                             :question-info="QuestionCache.reactiveQuestionInfos.value[questionId]">
                                            <div style="margin-top: 8px;">
                                                <transition-group name="blur-scale">
                                                    <el-text type="danger" style="margin-right: 16px"
                                                             v-for="reason of reasons" :key="reason">{{ reason }}
                                                    </el-text>
                                                </transition-group>
                                            </div>
                                        </question-info-panel>
                                    </transition-group>
                                </div>
                            </transition>
                        </el-scrollbar>
                    </div>
                </transition>
            </div>
        </template>
        <template #right-top>
            <el-button type="primary" @click="upload" :loading="uploading"
                       :loading-icon="_Loading_" style="height: 24px;width: 160px"
                       :icon="HarmonyOSIcon_Upload"
                       :disabled="!QuestionCache.reactiveDirty.value">
                <el-text>{{ errorsDisplay && !showTree ? "确认上传" : "上传题目更改" }}</el-text>
            </el-button>
        </template>
        <template #right>
            <router-view v-slot="{ Component }">
                <transition mode="out-in" name="question-editor">
                    <div v-if="!Component"
                         style="width: 100%;height: 100%;display: flex;flex-direction: column;align-items: center;justify-content: center">
                        <el-text type="info" size="large">
                            选择题目以编辑
                        </el-text>
                    </div>
                    <component v-else :is="Component"/>
                </transition>
            </router-view>
        </template>
    </responsive-splitpane>
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
    height: 100%;
}

.question-tree-node-content {
    width: 0;
    flex: 1;
    margin-left: 4px;
}

.node-button {
    margin-top: 1px;
    /*border: 2px var(--panel-bg-color-overlay) !important;*/
    overflow: hidden;
    @media (prefers-color-scheme: dark) {
        background: #1b1b1b !important;
    }
    @media (prefers-color-scheme: light) {
        background: #e4e4e4 !important;
    }
}

.node-button:hover {
    background: var(--el-color-primary-1) !important;
}

.pointer {
    scale: 0;
    width: 4px;
    height: 4px;
    margin-right: 4px;
    border-radius: 2px;
    align-self: center;
    transition: 400ms var(--ease-in-out-quint);
}

.pointer.changed {
    scale: 1;
    height: 12px;
    background: var(--el-color-primary);
}

.pointer.warning {
    scale: 1;
    background: var(--el-color-warning);
}

.pointer.error {
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
    transition: padding-top 300ms var(--ease-in-out-quint),
    padding-bottom 300ms var(--ease-in-out-quint),
    height 300ms var(--ease-in-out-quint),
    filter 150ms var(--ease-in-out-quint) 400ms,
    opacity 150ms var(--ease-out-quint) 400ms !important;
}

/*noinspection CssUnusedSymbol*/
.batch-buttons-leave-active {
    transition: padding-top 300ms var(--ease-in-out-quint) 400ms,
    padding-bottom 300ms var(--ease-in-out-quint) 400ms,
    height 300ms var(--ease-in-out-quint) 400ms,
    filter 150ms var(--ease-in-out-quint),
    opacity 150ms var(--ease-out-quint) !important;
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
    height: 32px !important;
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
    transition: 450ms var(--ease-in-out-quint);
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
    --phrase-1: 300ms;
    --phrase-2: 200ms;
    transition: margin-left var(--phrase-1) var(--ease-in-quint),
    margin-right var(--phrase-1) var(--ease-in-quint),
    grid-template-rows var(--phrase-2) var(--ease-in-out-quint) var(--phrase-1) !important;
    overflow: hidden;
}

.errorQuestions-enter-active {
    --phrase-1: 200ms;
    --phrase-2: 300ms;
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

.question-info-panel > .grid1 > .padding > div {
    min-height: 25px;
}

.errorsDescription > * {
    margin-right: 8px;
}

.choicesList > * {
    min-width: 40px;
    margin-right: 4px;
}

.action-button::after {
    content: "";
    position: absolute;
    background: var(--el-color-primary);
    opacity: 0;
    width: 0;
    left: 50%;
    top: calc(100% - 2px);
    height: 2px;
    /*    border-radius: 1px;*/
    border-radius: 0 0 2px 2px;
    filter: blur(4px);
    transition: left 500ms cubic-bezier(.34, .26, .19, 1.18),
    width 500ms cubic-bezier(.34, .26, .19, 1.18),
    background-color 150ms var(--ease-in-out-quint),
    opacity 150ms var(--ease-in-quint) 100ms,
    filter 100ms var(--ease-in-out-quint) 100ms;
}

.action-button.selected::after {
    opacity: 1;
    width: 98%;
    left: 1%;
    filter: none;
}

.local-deleted {
    opacity: 0.6;
}

.unable-to-edit {
    opacity: 0.8;
}
</style>
<style>
/*.node-buttons {
    position: absolute;
}*/

/*
.touch .node-buttons {
    position: initial !important;
}
*/

.touch .node-buttons,
.el-tree-node__content:not(.is-disabled):hover .node-buttons {
    opacity: 1 !important;
}

.el-tree-node__content .node-buttons {
    transition: 300ms var(--ease-in-out-quint);
    opacity: 0;
}

#app .node-button:hover {
    @media (prefers-color-scheme: dark) {
        background: #262626 !important;
    }
    @media (prefers-color-scheme: light) {
        background: #d9d9d9 !important;
    }
}
</style>