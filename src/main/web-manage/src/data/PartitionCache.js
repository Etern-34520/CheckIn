import WebSocketConnector from "@/api/websocket.js";
import LockUtil from "@/utils/Lock.js";
import QuestionCache from "@/data/QuestionCache.js";
// import {ref} from "vue"

const onPartitionAdded = [];
const onPartitionUpdated = [];
const onPartitionDeleted = [];
const onPartitionsAllChanged = [];

const updateAllPartition = () => {
    for (const callback of onPartitionsAllChanged) {
        callback(PartitionCache.refPartitions);
    }
};

const updatePartition = (partition) => {
    for (const callback of onPartitionUpdated) {
        callback(partition);
    }
    const oldPartition = PartitionCache.refPartitions.value[partition.id];
    for (const [key, value] of Object.entries(partition)) {
        oldPartition[key] = value;
    }
};

WebSocketConnector.registerAction("updatePartition", (message) => {
    updatePartition(message.data);
    updateAllPartition();
});

const addPartition = (partition) => {
    for (const callback of onPartitionAdded) {
        callback(partition);
    }
    PartitionCache.refPartitions.value[partition.id] = reactive(partition);
}
WebSocketConnector.registerAction("addPartition", (message) => {
    addPartition(message.data);
    updateAllPartition();
});
const deletePartition = (partition) => {
    for (const callback of onPartitionDeleted) {
        callback(partition);
    }
    delete PartitionCache.refPartitions.value[partition.id];
}
WebSocketConnector.registerAction("deletePartition", (message) => {
    deletePartition(message.data);
    updateAllPartition();
});
WebSocketConnector.registerAction("updatePartitions", (message) => {
    for (const [id, partition] of Object.entries(message.data)) {
        updatePartition(partition);
    }
    updateAllPartition();
});
WebSocketConnector.registerAction("addPartitions", (message) => {
    for (const [id, partition] of Object.entries(message.data)) {
        addPartition(partition);
    }
    updateAllPartition();
});
WebSocketConnector.registerAction("deletePartitions", (message) => {
    for (const [id, partition] of Object.entries(message.data)) {
        deletePartition(partition);
    }
    updateAllPartition();
});
const PartitionCache = {
    refPartitions: ref({}),
    reset() {
        PartitionCache.refPartitions.value = {};
        onPartitionAdded.length = 0;
        onPartitionDeleted.length = 0;
        onPartitionUpdated.length = 0;
        onPartitionsAllChanged.length = 0;
    },
    getRefPartitionsAsync() {
        const promise = LockUtil.buildExecutablePromise((resolve, reject) => {
            if (this.refPartitions.value.length > 0) {
                resolve(PartitionCache.refPartitions);
            } else {
                WebSocketConnector.send({
                    type: "getPartitions",
                }).then((response) => {
                    for (const partition of response.data.partitions) {
                        /*
                                                if (PartitionCache.refPartitions.value.find((p) => p.id === partition.id) === undefined)
                                                    PartitionCache.refPartitions.value.push(partition);
                        */
                        PartitionCache.refPartitions.value[partition.id] = reactive(partition);
                    }
                    resolve(PartitionCache.refPartitions);
                }, (error) => {
                    reject(error);
                });
            }
        });
        LockUtil.synchronizeExecute("getPartition", promise);
        return promise;
    },
    create(name) {
        return WebSocketConnector.send({
            type: "createPartition",
            data: {
                name: name
            }
        });
    },
    tryDeleteRemote(partitionId) {
        return WebSocketConnector.send({
            type: "deletePartition",
            data: {
                partitionId: partitionId
            }
        }).then((response) => {
            if (response.data.infectedQuestionIds instanceof Array) {
                const localInfectedQuestionInfos = QuestionCache.getAllLocal(response.data.infectedQuestionIds);
                for (const questionInfo of localInfectedQuestionInfos) {
                    if (questionInfo.warnings.remoteUpdated &&
                        questionInfo.warnings.remoteUpdated.buttons[0] &&
                        questionInfo.warnings.remoteUpdated.buttons[0].action instanceof Function) {
                        questionInfo.warnings.remoteUpdated.buttons[0].action();
                    }
                }
            }
        });
    },
    has(name) {
        for (const [id, refPartition] of Object.entries(PartitionCache.refPartitions.value)) {
            if (refPartition.name === name) {
                return true;
            }
        }
        return false;
    },
    getAsync(id) {
        return new Promise((resolve, reject) => {
            let resolved;
            const refPartition = PartitionCache.refPartitions.value[id];
            resolved = Boolean(refPartition);
            if (!resolved) {
                WebSocketConnector.send({
                    type: "getPartitionById",
                    data: {
                        id: id
                    }
                }).then((response) => {
                    const refPartition1 = reactive(response.data.partition);
                    PartitionCache.refPartitions.value[response.data.partition.id] = refPartition1;
                    resolve(refPartition1);
                }, () => {
                    reject();
                });
            } else {
                resolve(refPartition);
            }
        })
    },
    getNamesSyncByIds(ids) {
        const promise = LockUtil.buildExecutablePromise((resolve, reject) => {
            let names = {};
            let remoteRequireIds = new Set();
            for (const id of ids) {
                let found = false;
                let refPartition = PartitionCache.refPartitions.value[id];
                found = Boolean(refPartition);
                if (!found) {
                    remoteRequireIds.add(id);
                } else {
                    names[id] = refPartition.name;
                }
            }
            if (remoteRequireIds.size !== 0) {
                WebSocketConnector.send({
                    type: "getPartitionsByIds",
                    data: {
                        ids: Array.from(remoteRequireIds)
                    }
                }).then((response) => {
                    for (const partition of response.data.partitions) {
                        PartitionCache.refPartitions.value[partition.id] = reactive(partition);
                        names[partition.id] = partition.name;
                    }
                    resolve(names);
                }, () => {
                    reject();
                });
            } else {
                resolve(names);
            }
        });
        LockUtil.synchronizeExecute("PartitionCache", promise);
        return promise;
    },
    registerOnPartitionAdded(param) {
        if (param instanceof Function) {
            onPartitionAdded.push(param);
        }
    },
    registerOnPartitionDeleted(param) {
        if (param instanceof Function) {
            onPartitionDeleted.push(param);
        }
    },
    registerOnPartitionUpdated(param) {
        if (param instanceof Function) {
            onPartitionUpdated.push(param);
        }
    },
    registerOnPartitionsAllChanged(param) {
        if (param instanceof Function) {
            onPartitionsAllChanged.push(param);
        }
    },
    rename(partition, value) {
        return WebSocketConnector.send({
            type: "renamePartition",
            data: {
                partitionId: partition.id,
                newName: value,
            }
        });
    }
}
export default PartitionCache;