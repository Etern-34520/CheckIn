<script setup>
import {Splitpanes, Pane} from 'splitpanes'
import 'splitpanes/dist/splitpanes.css'
import WebSocketConnector from "@/api/websocket.js";
import {ref, watch} from "vue";
import router from "@/router/index.js";
import QuestionTempStorage from "@/QuestionTempStorage.js";

const props = {
    label: 'name',
    children: 'zones',
    isLeaf: 'leaf',
    data: {}
}

const tree = ref();

const filterText = ref('')

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
    if (node.level === 0)
        WebSocketConnector.send({
            type: "getPartitions",
        }).then((response) => {
            let data = []
            for (const partitionInfo of response.partitions) {
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
            resolve(data);
            /*console.log(response);*/
        });
    else
        QuestionTempStorage.getContentsAndIdsAsyncByPartitionId(node.data.id).then((questions) => {
            let data = []
            for (const questionInfo of questions) {
                // questionInfo.name = questionInfo.content;
                questionInfo.leaf = true;
                questionInfo.data = {
                    id: questionInfo.id,
                    type: "question",
                    name: questionInfo.content
                };
                data.push(questionInfo);
            }
            resolve(data);
            console.log(questions);
        }, (error) => {
            reject();
        });
    /*resolve(data);*/
}

const openEdit = (questionId) => {
    router.push("/manage/questions/" + questionId + "/");
}
</script>

<template>
    <div style="display: flex;flex-direction: row;align-content: stretch;align-items: stretch">
        <splitpanes style="flex: 1">
            <pane min-size="20" size="30">
                <div class="panel" style="display: flex">
                    <el-input
                        prefix-icon="Search"
                        v-model="filterText"
                        placeholder="搜索 (以 &quot;,&quot; 分词)"
                    />
                    <div style="display:flex;flex-direction: row">
                        <el-button text size="small">批量</el-button>
                    </div>
                    <div style="margin-top: 4px;height: 0;flex: 1;overflow: auto">
                        <el-tree
                            style="flex: 1;height: 0"
                            ref="tree"
                            :props="props"
                            :load="loadNode"
                            :filter-node-method="filterNode"
                            lazy>
                            <template #default="{ node : proxy, data : nodeObj }">
                                <div class="tree-node-item"
                                     @click="nodeObj.data.type==='question'?openEdit(nodeObj.id):null">
                                    <div class="point"></div>
                                    <div class="question_tree_node">
                                        <span class="question_tree_node_content">{{
                                                nodeObj.data.type === 'question' ? nodeObj.content : nodeObj.name
                                            }}</span>
                                    </div>
                                    <div class="flex_blank_1"></div>
                                    <el-button v-if="nodeObj.data.type==='question'" class="node_button" size="small"
                                               @click.stop="">
                                        删除
                                    </el-button>
                                </div>
                            </template>
                        </el-tree>
                    </div>
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
    </div >
</template>

<style scoped>
.question_tree_node {
    display: flex;
    flex-direction: row;
    align-items: flex-start;
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
    background: var(--panel-bg-color) !important;
    border: 2px var(--panel-bg-color-overlay) !important;
}

.node_button:hover {
    background: var(--panel-bg-color-overlay) !important;
}

.point {
    width: 4px;
    height: 4px;
    margin-right: 4px;
    border-radius: 50%;
}

.point.changed {
    background: var(--el-color-primary);
}

.point.error {
    background: var(--el-color-danger);
}

.point.deleted {
    background: var(--el-color-warning);
}

.question-editor-enter-active, .question-editor-leave-active {
    transition: all 0.4s ease-out;
}

.question-editor-enter-from, .question-editor-leave-to {
    opacity: 0;
    scale: 0.95;
}

</style>