<!--suppress ALL -->
<script setup>
import {Pane, Splitpanes} from "splitpanes"
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
import {ArrowLeftBold, List} from "@element-plus/icons-vue";
import CreatePartitionButton from "@/components/question/CreatePartitionButton.vue";
import QuestionInfoPanel from "@/components/question/QuestionInfoPanel.vue";
import SelectPartitionsActionPop from "@/components/question/SelectPartitionsActionPop.vue";
import UIMeta from "@/utils/UI_Meta.js";
import ResponsiveSplitpane from "@/components/common/ResponsiveDoubleSplitpane.vue";
import {ElMessageBox} from "element-plus";

const {proxy} = getCurrentInstance();

QuestionCache.reset();
PartitionCache.reset();

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
                (questionInfo.type !== 'Question' && questionInfo.type !== 'QuestionGroup') || (
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
});

const localPartitionQuestionData = {};

function loadPartitionChildrenNode(partitionId, isPartitionEmpty, resolve, reject) {
    let createQuestionButtonData = {
        id: "create-question-" + partitionId,
        treeId: partitionId + "/createQuestion",
        leaf: true,
        data: {
            type: "createQuestion",
            partitionId: partitionId,
            treeId: partitionId + "/create-question"
        }
    };
    const localPartitionQuestionNodes = localPartitionQuestionData[partitionId];
    let data = [createQuestionButtonData];
    if (localPartitionQuestionNodes instanceof Array) {
        data.push(...localPartitionQuestionNodes);
    }
    localPartitionQuestionData[partitionId] = false;
    if (isPartitionEmpty) {
        resolve(data);
    } else {
        QuestionCache.getContentsAndIdsAsyncByPartitionId(partitionId).then((questionInfos) => {
            remoteLoop:for (let questionInfo of questionInfos) {
                if (localPartitionQuestionNodes instanceof Array) {
                    for (const localQuestionNode of localPartitionQuestionNodes) {
                        if (localQuestionNode.data.question.id === questionInfo.question.id) {
                            continue remoteLoop;
                        }
                    }
                }
                // questionInfo.value = questionInfo.content;
                const questionNode = QuestionCache.getQuestionNodeItemDataOf(questionInfo, partitionId);
                data.push(questionNode);
            }
            nextTick(() => {
                tree.value.filter(filterText.value);
            });
            resolve(data);
        }, (error) => {
            reject();
        });
    }
}

const loadNode = (node, resolve, reject) => {
    let partitionId = node.data.id;
    if (node.level === 0) {
        let createPartitionButtonData = {
            id: "create-partition-" + partitionId,
            leaf: true,
            data: {type: "createPartition"}
        };
        PartitionCache.getRefPartitionsAsync().then((partitions) => {
            let data = [createPartitionButtonData];
            for (const partitionInfo of partitions.value) {
                data.push(getTreeNodeDataOfPartition(partitionInfo));
            }
            loading.value = false;
            resolve(data);
        });
    } else {
        if (node.data.data.loaded) {
            resolve(node.data.data.loadedChildren);
        } else {
            loadPartitionChildrenNode(partitionId, node.data.empty, resolve, reject);
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

PartitionCache.registerOnPartitionAdded((partition) => {
    partition.empty = true;
    tree.value.append(getTreeNodeDataOfPartition(partition), tree.value.root);
});
PartitionCache.registerOnPartitionDeleted((partition) => {
    tree.value.remove(tree.value.getNode(partition.id));
});

QuestionCache.registerOnQuestionUpdateLocal((questionInfo, differFromOriginal) => {
    for (let partitionId of questionInfo.question.partitionIds) {
        if (tree.value.getNode(partitionId + "/" + questionInfo.question.id) === null) {
            const questionNode = QuestionCache.getQuestionNodeItemDataOf(questionInfo, partitionId);
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
    for (const partition of PartitionCache.reactivePartitions.value) {
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

QuestionCache.registerOnQuestionDeleted((id, localDeleted) => {
    let questionInfo = QuestionCache.reactiveQuestionInfos.value[id];
    if (questionInfo && questionInfo.dirty || (!localDeleted && router.currentRoute.value.params.id === id)) {
        return;
    }
    if (router.currentRoute.value.params.id === id) {
        router.push({name: "questions"});
    }
    for (let partition of PartitionCache.reactivePartitions.value) {
        let questionNode = tree.value.getNode(partition.id + "/" + id);
        if (questionNode !== null) {
            tree.value.remove(questionNode);
        }
    }
});

const openEdit = (questionId) => {
    responsiveSplitpane.value.hideLeft();
    router.push("/manage/questions/" + questionId + "/");
}

const onEdit = (nodeObj, nodeItem, node, event) => {
    // console.log(nodeObj,nodeItem,node,event);
    nodeObj.data.type === 'Question' || nodeObj.data.type === 'QuestionGroup' ? openEdit(nodeObj.data.question.id) : null
}

const allowDrag = (node) => {
    const dataType = node.data.data.type;
    return dataType === 'Question' || dataType === "QuestionGroup";
}

const allowDrop = (draggingNode, dropNode, type) => {
    // console.log(draggingNode, dropNode, type);
    let nodeDataType = dropNode.data.data.type;
    return ((nodeDataType === 'partition' || nodeDataType === 'QuestionGroup') && type === "inner") ||
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
        QuestionCache.update(dragNodeData);
    }

    let dropPartitionId;
    delete dropNode.data.data.dragHover;
    const dataType = dropNodeData.type;
    if (dataType === "partition") {
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
    const authorQQ = Number(proxy.$cookies.get("qq"));
    let question = {
        id: randomUUIDv4(),
        content: "",
        enabled: false,
        partitionIds: [partitionId],
        authorQQ: authorQQ,
        upVoters: new Set(),
        downVoters: new Set(),
        localNew: true,
    };
    const questionInfo = QuestionCache.createQuestionGroup(question, undefined, authorQQ);
    tree.value.append(QuestionCache.getQuestionNodeItemDataOf(questionInfo, partitionId), tree.value.getNode(partitionId));
}

const createMultipleChoiceQuestion = (partitionId) => {
    let question = {
        id: randomUUIDv4(),
        content: "",
        type: "MultipleChoicesQuestion",
        enabled: false,
        partitionIds: [partitionId],
        authorQQ: Number(proxy.$cookies.get("qq")),
        upVoters: new Set(),
        downVoters: new Set(),
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
    tree.value.append(QuestionCache.getQuestionNodeItemDataOf(questionInfo, partitionId), tree.value.getNode(partitionId));
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
    let hasErrorQuestions = QuestionCache.getErrorQuestions().length > 0;
    if (hasErrorQuestions && showTree.value === true) {
        errorsDisplay.value = true;
        showTree.value = false;
    } else if (hasErrorQuestions && showTree.value === false) {
        uploading.value = true;
        QuestionCache.uploadAll().then(() => {
            uploading.value = false;
        });
    } else if (!hasErrorQuestions) {
        uploading.value = true;
        QuestionCache.uploadAll().then(() => {
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
        if (showTree.value === true)
            errorsDisplay.value = false;
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

function batchDo(questionInfoAction, questionNodeAction) {
    // const checkedKeys = tree.value.getCheckedKeys();
    const checkedNodes = tree.value.getCheckedNodes();
    const checkedQuestionIds = new Set();
    const checkedQuestionNodes = new Set();
    for (const node of checkedNodes) {
        if (node.data.type === "Question" || node.data.type === "QuestionGroup") {
            let split = node.treeId.split("/");
            const questionId = split[1];
            if (questionId === "createQuestion") continue;
            checkedQuestionIds.add(questionId);
            checkedQuestionNodes.add(node);
        } else if (node.data.type === "partition") {
            let zones = node.zones;
            for (const questionNode of zones) {
                if (questionNode.data.type === "Question" || questionNode.data.type === "QuestionGroup") {
                    checkedQuestionIds.add(questionNode.data.question.id);
                    checkedQuestionNodes.add(questionNode);
                }
            }
        }
    }
    QuestionCache.getAllAsync(Array.from(checkedQuestionIds)).then(questionInfos => {
        if (questionInfoAction instanceof Function)
            for (const questionInfo of questionInfos) {
                questionInfoAction(questionInfo);
            }
        if (questionNodeAction instanceof Function) {
            for (const questionNode of checkedQuestionNodes) {
                questionNodeAction(questionNode);
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
        name: "移动题目从所选分区到...",
        show: () => {
            return true;
        },
        action: (partitionIds) => {
            currentButton.menuVisible = false;
            const partitionIdsSet = new Set(partitionIds);
            batchDo(undefined, (questionNode) => {
                const partitionId = Number(questionNode.treeId.split("/")[0]);
                questionNode.data.question.partitionIds = questionNode.data.question.partitionIds.filter(id => id !== partitionId && !partitionIdsSet.has(id));
                questionNode.data.question.partitionIds.push(...partitionIds);
                QuestionCache.update(questionNode.data);
            });
        },
        menuVisible: false
    }, {
        name: "复制题目到...",
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
        name: "从所选分区删除题目",
        show: () => {
            return true;
        },
        action: () => {
            batchDo(undefined, (questionNode) => {
                if (questionNode.data.question.partitionIds.length === 1) {
                    QuestionCache.delete(questionNode.data.question.id);
                } else {
                    const partitionId = Number(questionNode.treeId.split("/")[0]);
                    questionNode.data.question.partitionIds = questionNode.data.question.partitionIds.filter(id => id !== partitionId);
                    QuestionCache.update(questionNode.data);
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
    }, {
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
    }]
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
    function rectify1(currentPartitionId) {
        const currentPartitionCheckedQuestionIds = new Set();
        for (const checkedNode of checkStatus.checkedNodes) {
            let partitionId = Number(checkedNode.treeId.split("/")[0]);
            if (partitionId === currentPartitionId && checkedNode.data.type === "Question") {
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

    const dataType = nodeObj.data.type;
    if (dataType === "Question" || dataType === "QuestionGroup") {
        const currentPartitionId = Number(nodeObj.treeId.split("/")[0]);
        rectify1(currentPartitionId);
    } else if (dataType === "partition") {
        //FIXME
        if (!QuestionCache.loadedPartitionIds.has(nodeObj.treeId)) {
            loadPartitionChildrenNode(nodeObj.treeId, nodeObj.data.empty, (data) => {
                nodeObj.data.loadedChildren = data;
                nodeObj.data.loaded = true;
                for (const questionNode of data) {
                    if (questionNode.data.type !== "createQuestion")
                        tree.value.append(questionNode, nodeObj.treeId);
                }
                nextTick(() => {
                    tree.value.setChecked(nodeObj.treeId, true, true);
                });
            }, () => {
            });
        }
    }
}

function onDeleteNode(nodeObj) {
    if (nodeObj.data.type === 'Question' || nodeObj.data.type === 'QuestionGroup') {

        if (nodeObj.data.question.localDeleted) {
            QuestionCache.restore(nodeObj.data.question.id);
        } else {
            QuestionCache.delete(nodeObj.data.question.id);
        }
    } else {
        // PartitionCache.getSync(nodeObj.id).then((partition) => {
        const partition = tree.value.getNode(nodeObj.id);
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
                QuestionCache.getContentsAndIdsAsyncByPartitionId(nodeObj.id).then((questionInfos) => {
                    const deletedQuestionIds = [];
                    for (const questionInfo of questionInfos) {
                        deletedQuestionIds.push(questionInfo.question.id);
                    }
                    QuestionCache.deleteAllById(deletedQuestionIds).then(() => {
                        tryDelete();
                    }, () => {
                        ElMessage({
                            type: "error",
                            message: "删除分区题目时出错"
                        });
                    });
                });
            }).catch(() => {
            });
        }
        const tryDelete = () => {
            PartitionCache.tryDeleteRemote(nodeObj.id).then((res) => {
            }, (error) => {
                error.disableNotification();
                alartNotEmpty();
            });
        };
        if (partition.childNodes.length > 1/*create button counts 1*/) {
            alartNotEmpty();
        } else {
            tryDelete();
        }
        // });
    }
}

const currentButton = ref({
    menuVisible: false
});

const responsiveSplitpane = ref(null)

router.afterEach((to, from) => {
    if (to.params.id === undefined) {
        if (responsiveSplitpane.value)
            responsiveSplitpane.value.showLeft();
    } else {
        if (responsiveSplitpane.value)
            responsiveSplitpane.value.hideLeft();
    }
});
</script>

<template>
    <responsive-splitpane ref="responsiveSplitpane" :left-loading="loading" show-left-label="题目列表">
        <template #left>
            <el-input prefix-icon="Search" v-model="filterText" placeholder="搜索 (以 &quot;,&quot; 分词)"/>
            <el-button type="primary" style="margin-top: 8px" @click="upload" :loading="uploading"
                       loading-icon="_Loading_"
                       :disabled="!QuestionCache.reactiveDirty.value">
                <HarmonyOSIcon_Upload/>
                <el-text>{{ showTree ? "上传题目更改" : "确认上传" }}</el-text>
            </el-button>
            <!--                    <el-scrollbar>-->
            <div class="slide-base"
                 style="display: flex;flex-direction: row;overflow-y:overlay;overflow-x: hidden;">
                <div class="question-tree-base" style="display: flex;flex-direction: column;overflow:overlay;"
                     :class="{hideLeft:!showTree}">
                    <div>
                        <el-button-group>
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
                                    v-show="currentButton.menuVisible"
                                    @on-confirm="currentButton.action"/>
                        </transition>
                    </div>
                    <div style="flex:1;overflow:overlay;">
                        <el-scrollbar>
                            <div style="flex: 1">
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
                                            <!--                                            TODO component-->
                                            <el-popover trigger="click" width="auto">
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
                                                                    @click="createQuestionGroup(nodeObj.data.partitionId)">
                                                                题组
                                                            </el-button>
                                                            <el-button
                                                                    @click="createMultipleChoiceQuestion(nodeObj.data.partitionId)">
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
                                            <div class="tree-node-item"
                                                 :class="{dragHover:nodeObj.data.dragHover}">
                                                <div class="point" :class="{
                                                            changed:nodeObj.data.dirty,
                                                            error:nodeObj.data.showError,
                                                            warning:nodeObj.data.showWarning
                                                        }"></div>
                                                <div class="question-tree-node">
                                                    <el-text class="question-tree-node-content"
                                                             :style="{
                                                                opacity: (nodeObj.data.question&&nodeObj.data.question.localDeleted)?0.5:1
                                                            }">
                                                        {{
                                                            nodeObj.data.type === 'partition' ?
                                                                    nodeObj.data.partition.name : nodeObj.data.question.content
                                                        }}
                                                    </el-text>
                                                </div>
                                                <el-button-group class="node-buttons">
                                                    <el-popover trigger="click"
                                                                v-model:visible="nodeObj.data.editing"
                                                                width="400"
                                                                v-if="nodeObj.data.type === 'partition'">
                                                        <template #reference>
                                                            <el-button class="node-button" size="small"
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
                                                    <el-button class="node-button" size="small"
                                                               @click.stop="onDeleteNode(nodeObj)">
                                                        <HarmonyOSIcon_Remove/>
                                                        <el-text
                                                                v-if="nodeObj.data.question?nodeObj.data.question.localDeleted:false">
                                                            撤销删除
                                                        </el-text>
                                                        <el-text v-else>删除</el-text>
                                                    </el-button>
                                                </el-button-group>
                                            </div>
                                        </template>
                                    </template>
                                </el-tree>
                            </div>
                        </el-scrollbar>
                    </div>
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
                        <div>
                            <transition-group name="slide-hide">
                                <question-info-panel class="disable-init-animate"
                                                     v-for="questionInfo of QuestionCache.getErrorQuestions()"
                                                     :key="questionInfo.question.id"
                                                     @click="openEdit(questionInfo.question.id)"
                                                     :question-info="questionInfo"/>
                            </transition-group>
                        </div>
                        <transition name="empty">
                            <el-empty v-show="QuestionCache.getErrorQuestions().length===0"/>
                        </transition>
                    </el-scrollbar>
                </div>
            </div>
        </template>
        <template #right-top>
            <el-button type="primary" @click="upload" :loading="uploading"
                       loading-icon="_Loading_" style="height: 24px"
                       :disabled="!QuestionCache.reactiveDirty.value">
                <HarmonyOSIcon_Upload/>
                <el-text>{{ showTree ? "上传题目更改" : "确认上传" }}</el-text>
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
    transition: padding-top 300ms var(--ease-in-out-quint),
    padding-bottom 300ms var(--ease-in-out-quint),
    height 300ms var(--ease-in-out-quint),
    filter 150ms var(--ease-in-out-quint) 350ms,
    opacity 150ms var(--ease-out-quint) 350ms !important;
}

/*noinspection CssUnusedSymbol*/
.batch-buttons-leave-active {
    transition: padding-top 300ms var(--ease-in-out-quint) 200ms,
    padding-bottom 300ms var(--ease-in-out-quint) 200ms,
    height 300ms var(--ease-in-out-quint) 200ms,
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
</style>
<style>
.mobile .node-buttons,
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