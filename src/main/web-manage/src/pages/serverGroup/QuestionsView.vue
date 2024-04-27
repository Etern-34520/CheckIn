<script setup>
import {Splitpanes, Pane} from 'splitpanes'
import 'splitpanes/dist/splitpanes.css'
import WebSocketConnector from "@/api/websocket.js";
import {ref, watch} from "vue";
import router from "@/router/index.js";
import QuestionTempStorage from "@/data/QuestionTempStorage.js";
import PartitionTempStorage from "@/data/PartitionTempStorage.js";

const props = {
    label: 'name',
    children: 'zones',
    isLeaf: 'leaf',
    data: {}
}

const tree = ref();

const filterText = ref('')

const loading = ref(true);

const filterNode = (value, data) => {
    if (!value) return true
    console.log(data)
    for (const v of value.split(",")) {
        if (v !== "" && (data.name.toUpperCase().includes(v.toUpperCase())) || data.id.toString().includes(v)) {
            return true
        }
    }
    return false
}

watch(filterText, (val) => {
    tree.value.filter(val)
})

const loadNode = (node, resolve, reject) => {
    if (node.level === 0) {
        PartitionTempStorage.getRefPartitionsAsync().then((partitions) => {
            let data = []
            for (const partitionInfo of partitions.value) {
                data.push({
                    id: partitionInfo.id,
                    name: partitionInfo.name,
                    leaf: partitionInfo.empty,
                    zones: [],
                    data: {
                        id: partitionInfo.id,
                        type: "partition"
                    }
                });
            }
            loading.value = false;
            resolve(data);
        });
    } else {
        QuestionTempStorage.getContentsAndIdsAsyncByPartitionId(node.data.id).then((questions) => {
            let data = [];
            for (const questionInfo of questions) {
                // questionInfo.name = questionInfo.content;
                questionInfo.leaf = true;
                questionInfo.data = {
                    id: questionInfo.id,
                    type: "question",
                    name: questionInfo.content,
                };
                data.push(questionInfo);
            }
            resolve(data);
            // console.log(questions);
        }, (error) => {
            reject();
        });
    }
    /*resolve(data);*/
}

PartitionTempStorage.registerOnPartitionAdded((partition) => {
    tree.value.append({
        id: partition.id,
        name: partition.name,
        leaf: true,
        zones: [],
        data: {
            id: partition.id,
            type: "partition",
        }
    }, tree.value.root);
})

/*
QuestionTempStorage.registerOnQuestionUpdateLocal((question, differFromOriginal) => {
    if (differFromOriginal) question.state="changed"
    else question.state=undefined
})
*/

const openEdit = (questionId) => {
    router.push("/manage/questions/" + questionId + "/");
}

/*
const updateQuestion = (question) => {
    console.log(question)
}
*/

const onDeleteQuestion = (questionId) => {
}

const onDeletePartition = (partitionId) => {
    PartitionTempStorage.deleteRemote(partitionId);
}
</script>

<template>
    <div style="display: flex;flex-direction: row;align-content: stretch;align-items: stretch">
        <splitpanes style="flex: 1">
            <pane min-size="20" size="30">
                <div class="panel" style="display: flex" v-loading="loading">
                    <el-input
                        prefix-icon="Search"
                        v-model="filterText"
                        placeholder="搜索 (以 &quot;,&quot; 分词)"
                    />
                    <el-button-group>
                        <el-button text size="small">
                            <svg width="20" height="20" viewBox="0 0 24 24">
                                <title>ic_gallery_material_select_checkbo</title>
                                <g id="_ic_gallery_material_select_checkbo" stroke="none" fill="none" stroke-width="1"
                                   fill-rule="evenodd">
                                    <path class="svg-text-fill"
                                          d="M18,2 C20.209139,2 22,3.790861 22,6 L22,18 C22,20.209139 20.209139,22 18,22 L6,22 C3.790861,22 2,20.209139 2,18 L2,6 C2,3.790861 3.790861,2 6,2 L18,2 Z M18,3.5 L6,3.5 C4.6745166,3.5 3.58996133,4.53153594 3.50531768,5.83562431 L3.5,6 L3.5,18 C3.5,19.3254834 4.53153594,20.4100387 5.83562431,20.4946823 L6,20.5 L18,20.5 C19.3254834,20.5 20.4100387,19.4684641 20.4946823,18.1643757 L20.5,18 L20.5,6 C20.5,4.6745166 19.4684641,3.58996133 18.1643757,3.50531768 L18,3.5 Z M17.5303301,8.46966991 C17.7965966,8.73593648 17.8208027,9.15260016 17.6029482,9.44621165 L17.5303301,9.53033009 L11.7374369,15.3232233 C11.0942204,15.9664398 10.0748689,16.004276 9.3874012,15.4367321 L9.26256313,15.3232233 L6.96966991,13.0303301 C6.6767767,12.7374369 6.6767767,12.2625631 6.96966991,11.9696699 C7.23593648,11.7034034 7.65260016,11.6791973 7.94621165,11.8970518 L8.03033009,11.9696699 L10.3232233,14.2625631 C10.4045825,14.3439224 10.5280669,14.3574822 10.6234678,14.3032427 L10.6767767,14.2625631 L16.4696699,8.46966991 C16.7625631,8.1767767 17.2374369,8.1767767 17.5303301,8.46966991 Z"
                                          id="_形状结合" fill-rule="nonzero"/>
                                </g>
                            </svg>
                            批量
                        </el-button>
                        <el-popover>
                            <template #reference>
                                <el-button text size="small">
                                    <svg width="20" height="20" viewBox="0 0 24 24">
                                        <title>Public/ic_public_add_norm</title>
                                        <defs>
                                            <path
                                                d="M12,1 C18.0751322,1 23,5.92486775 23,12 C23,18.0751322 18.0751322,23 12,23 C5.92486775,23 1,18.0751322 1,12 C1,5.92486775 5.92486775,1 12,1 Z M12,2.5 C6.75329488,2.5 2.5,6.75329488 2.5,12 C2.5,17.2467051 6.75329488,21.5 12,21.5 C17.2467051,21.5 21.5,17.2467051 21.5,12 C21.5,6.75329488 17.2467051,2.5 12,2.5 Z M12.75,17.25 C12.75,17.6642136 12.4142136,18 12,18 C11.5857864,18 11.25,17.6642136 11.25,17.25 L11.25,12.749 L6.75,12.75 C6.33578644,12.75 6,12.4142136 6,12 C6,11.5857864 6.33578644,11.25 6.75,11.25 L11.25,11.249 L11.25,6.75 C11.25,6.33578644 11.5857864,6 12,6 C12.4142136,6 12.75,6.33578644 12.75,6.75 L12.75,17.25 Z M17.25,11.25 C17.6642136,11.25 18,11.5857864 18,12 C18,12.4142136 17.6642136,12.75 17.25,12.75 L13.75,12.75 L13.75,11.25 L17.25,11.25 Z"
                                                id="_path-1"/>
                                        </defs>
                                        <g id="_Public/ic_public_add_norm" stroke="none" stroke-width="1" fill="none"
                                           fill-rule="evenodd">
                                            <mask id="_mask-2" class="svg-text-fill">
                                                <use xlink:href="#_path-1"/>
                                            </mask>
                                            <use id="_形状结合" class="svg-text-fill" fill-rule="nonzero"
                                                 xlink:href="#_path-1"/>
                                        </g>
                                    </svg>
                                    添加
                                </el-button>
                            </template>
                            <template #default>
                                <div style="display: flex;flex-direction: column" class="create-option">
                                    <el-button>题目</el-button>
                                    <el-button>分区</el-button>
                                </div>
                            </template>
                        </el-popover>
                    </el-button-group>
                    <el-scrollbar>
                        <div style="margin-top: 4px;overflow: auto">
                            <el-tree
                                ref="tree"
                                :props="props"
                                :load="loadNode"
                                node-key="id"
                                :filter-node-method="filterNode"
                                lazy>
                                <template #default="{ node : proxy, data : nodeObj }">
                                    <div class="tree-node-item"
                                         @click="nodeObj.data.type==='question'?openEdit(nodeObj.id):null">
                                        <div class="point" :class="{changed:nodeObj.status==='changed'}"></div>
                                        <div class="question_tree_node">
                                        <span class="question_tree_node_content">{{
                                                nodeObj.data.type === 'question' ? nodeObj.content : nodeObj.name
                                            }}</span>
                                        </div>
                                        <div class="flex_blank_1"></div>
                                        <el-button class="node_button" size="small"
                                                   @click.stop="
                                                   nodeObj.data.type==='question'?
                                                   onDeleteQuestion(nodeObj.id):onDeletePartition(nodeObj.id)">
                                                <svg width="20" height="20" viewBox="0 0 24 24">
                                                <defs>
                                                    <path d="M5.629,7.5 L6.72612901,18.4738834 C6.83893748,19.6019681 7.77211147,20.4662096 8.89848718,20.4990325 L8.96496269,20.5 L15.0342282,20.5 C16.1681898,20.5 17.1211231,19.6570911 17.2655686,18.5392856 L17.2731282,18.4732196 L18.1924161,9.2527383 L18.369,7.5 L19.877,7.5 L19.6849078,9.40262938 L18.7657282,18.6220326 C18.5772847,20.512127 17.0070268,21.9581787 15.1166184,21.9991088 L15.0342282,22 L8.96496269,22 C7.06591715,22 5.47142703,20.5815579 5.24265599,18.7050136 L5.23357322,18.6231389 L4.121,7.5 L5.629,7.5 Z M10.25,11.75 C10.6642136,11.75 11,12.0857864 11,12.5 L11,18.5 C11,18.9142136 10.6642136,19.25 10.25,19.25 C9.83578644,19.25 9.5,18.9142136 9.5,18.5 L9.5,12.5 C9.5,12.0857864 9.83578644,11.75 10.25,11.75 Z M13.75,11.75 C14.1642136,11.75 14.5,12.0857864 14.5,12.5 L14.5,18.5 C14.5,18.9142136 14.1642136,19.25 13.75,19.25 C13.3357864,19.25 13,18.9142136 13,18.5 L13,12.5 C13,12.0857864 13.3357864,11.75 13.75,11.75 Z M12,1.75 C13.7692836,1.75 15.2083571,3.16379796 15.2491124,4.92328595 L15.25,5 L21,5 C21.4142136,5 21.75,5.33578644 21.75,5.75 C21.75,6.14942022 21.43777,6.47591522 21.0440682,6.49872683 L21,6.5 L14.5,6.5 C14.1005798,6.5 13.7740848,6.18777001 13.7512732,5.7940682 L13.75,5.75 L13.75,5 C13.75,4.03350169 12.9664983,3.25 12,3.25 C11.0536371,3.25 10.2827253,4.00119585 10.2510148,4.93983756 L10.25,5 L10.25,5.75 C10.25,6.14942022 9.93777001,6.47591522 9.5440682,6.49872683 L9.5,6.5 L2.75,6.5 C2.33578644,6.5 2,6.16421356 2,5.75 C2,5.35057978 2.31222999,5.02408478 2.7059318,5.00127317 L2.75,5 L8.75,5 C8.75,3.20507456 10.2050746,1.75 12,1.75 Z" id="_path-3"/>
                                                </defs>
                                                <g id="_Public/ic_public_delete" stroke="none" stroke-width="1" fill="none" fill-rule="evenodd">
                                                    <mask class="svg-text-fill" id="_mask-3">
                                                        <use xlink:href="#_path-3"/>
                                                    </mask>
                                                    <use id="_形状结合" class="svg-text-fill" fill-rule="nonzero" xlink:href="#_path-3"/>
                                                </g>
                                            </svg>
                                            删除
                                        </el-button>
                                    </div>
                                </template>
                            </el-tree>
                        </div>
                    </el-scrollbar>
                </div>
            </pane>
            <pane min-size="50">
                <div class="panel">
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
.question_tree_node {
    display: flex;
    flex-direction: row;
    align-items: center;
}

.tree-node-item {
    display: flex;
    flex-direction: row;
    flex: 1;
}

.question_tree_node_content {
    width: 0;
    flex: 1;
}

.node_button {
    --button-bg-color: var(--el-color-info-light-9) !important;
    /*background: var(--el-button-bg-color) !important;*/
    border: 2px var(--panel-bg-color-overlay) !important;
}

.node_button:hover {
    --button-hover-bg-color: var(--el-color-info-light-8) !important;
    /*background: var(--panel-bg-color-overlay) !important;*/
}

.point {
    scale: 0;
    width: 4px;
    height: 4px;
    margin-right: 4px;
    border-radius: 50%;
    align-self: center;
    transition: scale 200ms var(--ease-in-bounce);
}

.point.changed {
    scale: 1;
    background: var(--el-color-primary);
}

.point.error {
    scale: 1;
    background: var(--el-color-danger);
}

.point.deleted {
    scale: 1;
    background: var(--el-color-warning);
}

.question-editor-enter-active, .question-editor-leave-active {
    transition: all 0.4s ease-out;
}

.question-editor-enter-from, .question-editor-leave-to {
    opacity: 0;
    scale: 0.95;
}

.create-option > * {
    margin: 0;
}
</style>