import router from "@/router/index.js";
import randomUUIDv4 from "@/utils/UUID.js";
import WebSocketConnector from "@/api/websocket.js";
import PermissionInfo from "@/auth/PermissionInfo.js";
import UserDataInterface from "@/data/UserDataInterface.js";
import verifier from "@/utils/Verifier.js";

// import {ref} from "vue";

function isQuestionsEqual(q1, q2) {
    if (q1.id === q2.id) {
        const bothHaveNoImages = (q1.images === undefined || q1.images.length === 0) && (q2.images === undefined || q2.images.length === 0);
        const bothHaveNoPartitions = (q1.partitionIds === undefined || q1.partitionIds === null) && (q2.partitionIds === undefined || q2.partitionIds === null);
        const basic =
            q1.localDeleted === q2.localDeleted && q1.enabled === q2.enabled && q1.randomOrdered === q2.randomOrdered &&
            q1.content === q2.content && q1.authorQQ === q2.authorQQ && (q1.explanation === q2.explanation || (!q1.explanation && !q2.explanation)) &&
            ((q1.choices === undefined && q2.choices === undefined) || q1.choices.length === q2.choices.length) &&
            (bothHaveNoImages || q1.images !== undefined && q2.images !== undefined && q1.images.length === q2.images.length) &&
            (bothHaveNoPartitions || q1.partitionIds && q2.partitionIds && q1.partitionIds.length === q2.partitionIds.length);
        if (basic) {
            let choiceNotEqual = false;
            let index = 0;
            if (q1.choices && q2.choices) {
                for (let q1Choice of q1.choices) {
                    let q2Choice = q2.choices[index];
                    const choiceBasic = // q1Choice.id === q2Choice.id &&
                        q1Choice.correct === q2Choice.correct && q1Choice.content === q2Choice.content;
                    if (!choiceBasic) {
                        choiceNotEqual = true;
                        break;
                    }
                    index++
                }
            } else {
                choiceNotEqual = q1.choices !== undefined || q2.choices !== undefined;
            }
            if (!choiceNotEqual) {
                index = 0;
                //TODO ignoreOrder
                let partitionsNotEqual = false;
                if (q1.partitionIds && q2.partitionIds) {
                    for (const q1PartitionId of q1.partitionIds) {
                        const q2PartitionId = q2.partitionIds[index];
                        if (q1PartitionId !== q2PartitionId) {
                            partitionsNotEqual = true;
                            break;
                        }
                        index++;
                    }
                } else {
                    partitionsNotEqual = (q1.partitionIds !== undefined && q1.partitionIds !== null) || (q2.partitionIds !== undefined && q1.partitionIds !== null);
                }
                if (!partitionsNotEqual && !bothHaveNoImages && q1.images.length !== 0) {
                    index = 0;
                    let q1ImagesEqualsQ2Images = true;
                    for (const /**Object*/q1Image of q1.images) {
                        const /**Object*/q2Image = q2.images[index];
                        const q1ImageEqQ2Image = q1Image.size === q2Image.size && q1Image.name === q2Image.name && q1Image.url === q2Image.url
                        if (!q1ImageEqQ2Image) {
                            q1ImagesEqualsQ2Images = false;
                            break;
                        }
                        index++;
                    }
                    return q1ImagesEqualsQ2Images;
                } else if (bothHaveNoImages) return !partitionsNotEqual;
            }
        }
    }
    return false;
}

function isQuestionGroupEqual(q1Info, q2Info) {
    let equal = isQuestionsEqual(q1Info.question, q2Info.question);
    if (equal) {
        let index = 0;
        for (const questionInfo of q1Info.questionInfos) {
            let q2QuestionInfo = q2Info.questionInfos[index];
            if (q2QuestionInfo) {
                equal = isQuestionsEqual(questionInfo.question, q2QuestionInfo.question);
                if (equal === false) {
                    break;
                }
            } else {
                return false;
            }
            index++;
        }
    }
    return equal;
}

const getDifferenceQuestion = (question) => {
    const getDifferenceData = (obj, originalObj) => {
        let diff = {};
        let keys = Object.keys(obj);
        for (let originalKey of Object.keys(originalObj)) {
            if (!keys.includes(originalKey)) keys.push(originalKey);
        }
        for (let key of keys) {
            if (typeof obj[key] === "object" && typeof originalObj[key] === "object" && obj[key] && originalObj[key]) {
                let vChildren = getDifferenceData(obj[key], originalObj[key]);
                if (Object.keys(vChildren).length > 0) {
                    diff[key] = obj[key];
                }
            } else if (obj[key] !== originalObj[key]) {
                diff[key] = obj[key];
            }
        }
        return diff;
    }

    const originalQuestionInfo = QuestionCache.originalQuestionInfos[question.id];
    if (originalQuestionInfo) {
        let differenceQuestion = getDifferenceData(question, originalQuestionInfo.question);
        differenceQuestion.id = question.id;
        differenceQuestion.type = question.type;
        // differenceQuestion.choices = question.choices;
        return differenceQuestion;
    } else {
        return question;
    }
}

WebSocketConnector.registerAction("updateQuestions", (response) => {
    if (uploading.value) {
        watch(() => uploading.value, (value) => {
            if (!value) {
                update();
            }
        }, {once: true});
    } else {
        update();
    }

    function update() {
        let newQuestionIds = [];
        for (const question of response.data) {
            if (localUploadedQuestionIds.has(question.id)) {
                console.debug("update by local upload: " + question.id);
                QuestionCache.originalQuestionInfos[question.id].question.lastModifiedTime = question.lastModifiedTime;
                QuestionCache.reactiveQuestionInfos.value[question.id].question.lastModifiedTime = question.lastModifiedTime;
                localUploadedQuestionIds.delete(question.id);
                continue;
            }
            if (QuestionCache.originalQuestionInfos[question.id]) {
                console.debug("has conflict: " + question.id);
                let questionInfo = QuestionCache.reactiveQuestionInfos.value[question.id];
                questionInfo.remoteUpdated = true;
                questionInfo.verify();
            } else if (QuestionCache.reactiveQuestionInfos.value[question.id]) {
                console.debug("has loaded: " + question.id);
                QuestionCache.reactiveQuestionInfos.value[question.id].question = question;
                QuestionCache.reactiveQuestionInfos.value[question.id].remoteUpdated = true;
                QuestionCache.update(QuestionCache.reactiveQuestionInfos.value[question.id]);
            } else {
                console.debug("new added: " + question.id);
                newQuestionIds.push(question.id);
            }
        }
        if (newQuestionIds.length > 0) {
            QuestionCache.getAllAsync(newQuestionIds).then((questionInfos) => {
                for (const questionInfo of questionInfos) {
                    QuestionCache.update(questionInfo);
                }
            }, (error) => {
                console.error(error);
            });
        }
    }

    console.log(response.data);
});
const deleteQuestionInternal = (questionId) => {
    let questionInfo = QuestionCache.reactiveQuestionInfos.value[questionId];
    let localDeleted = localDeletedQuestionIds.has(questionId);
    localDeletedQuestionIds.delete(questionId);
    if (localDeleted || questionInfo && questionInfo.dirty !== true && router.currentRoute.value.params.id !== questionId) {
        delete QuestionCache.reactiveQuestionInfos.value[questionId];
        delete QuestionCache.originalQuestionInfos[questionId];
    } else if (questionInfo && (questionInfo.dirty || router.currentRoute.value.params.id === questionId)) {
        questionInfo.remoteDeleted = true;
        delete QuestionCache.originalQuestionInfos[questionId];
        // questionInfo.verify();
        questionInfo.showWarning = true;
        QuestionCache.update(questionInfo);
    }
    for (const action of onDelete) {
        action(questionId, localDeleted);
    }
}

WebSocketConnector.registerAction("deleteQuestions", (response) => {
    console.log(response.data);
    for (const questionId of response.data) {
        deleteQuestionInternal(questionId);
    }
});

const onUpdateLocal = [];
const onUpdateRemote = [];
const onDelete = [];
const localDeletedQuestionIds = new Set();
const localUploadedQuestionIds = new Set();
const uploading = ref(false);

const debounceTimers = {}

function verify(questionInfo) {
    console.debug("verify task pushed", questionInfo);

    let question = questionInfo.question;
    if (!question) return;
    const debounceTimer = debounceTimers[questionInfo.question.id];
    if (debounceTimer) {
        clearTimeout(debounceTimer);
    }
    debounceTimers[questionInfo.question.id] = setTimeout(() => {
        console.debug("verify started", questionInfo);
        delete debounceTimers[questionInfo.question.id];
        verifier.verifyQuestionInfo(questionInfo);
    }, 100);
}

let retryCount = 0;

const currentUser = UserDataInterface.getCurrentUser();

function initInfoWithoutCaching(questionInfo, question) {
    if (questionInfo === undefined) {
        questionInfo = reactive({
            question: {},
            type: "Question",
            dirty: false,
            errors: question.errors ? question.errors : {},
            warnings: question.warnings ? question.warnings : {},
            ableToEdit: QuestionCache.checkEditPermission(question),
            ableToDelete: QuestionCache.checkDeletePermission(question),
            inputMeta: {},
            verify: () => {
                verify(questionInfo);
            }
        });
    }
    /*
        questionInfo.verify = () => {
            verify(questionInfo);
        }
    */
    questionInfo.simple = false;
    delete questionInfo.remoteUpdated;
    delete questionInfo.downloadRemoteUpdated;
    delete question.errors;
    delete question.warnings;
    for (let key in question) {
        if (key === "messageId") continue;
        /*
                if (key === "upVoters" || key === "downVoters") {
                    questionInfo.question[key] = new Set(question[key]);
                    continue;
                }
        */
        if (key === "questions") {
            questionInfo.questionInfos = [];
            for (const subQuestion of question[key]) {
                const subQuestionInfo = initQuestionInfo(undefined, subQuestion);
                subQuestionInfo.getGroup = () => {
                    return questionInfo;
                }
                questionInfo.questionInfos.push(subQuestionInfo);
            }
        } else {
            questionInfo.question[key] = question[key];
        }
    }
    delete question.questions;
    return questionInfo;
}

function initQuestionInfo(questionInfo, question) {
    questionInfo = initInfoWithoutCaching(questionInfo, question);
    let copiedQuestionInfo = JSON.parse(JSON.stringify(questionInfo));
    QuestionCache.reactiveQuestionInfos.value[question.id] = questionInfo;
    QuestionCache.originalQuestionInfos[question.id] = copiedQuestionInfo;
    questionInfo.verify();
    return questionInfo;
}

const QuestionCache = {
    reactiveQuestionInfos: ref({}),
    originalQuestionInfos: {},
    originalQuestionGroupInfos: {},
    dirty: false,
    reactiveDirty: ref(false),
    dirtyQuestionInfos: {},
    reset() {
        this.reactiveQuestionInfos.value = {};
        this.originalQuestionInfos = {};
        this.dirtyQuestionInfos = {};
        this.dirty = false;
        this.reactiveDirty.value = false;
        onUpdateLocal.length = 0;
        onUpdateRemote.length = 0;
        onDelete.length = 0;
        verifier.loadRules();
    },
    getAsync(id) {
        return new Promise((resolve, reject) => {
            let questionInfo = QuestionCache.reactiveQuestionInfos.value[id];
            if (questionInfo === undefined || questionInfo.simple || (questionInfo.remoteUpdated && questionInfo.downloadRemoteUpdated)) {
                WebSocketConnector.send({
                    type: 'getQuestionInfo',
                    data: {
                        questionId: id
                    }
                }).then((response) => {
                    questionInfo = initQuestionInfo(questionInfo, response.data.question);
                    resolve(questionInfo);
                }, (error) => {
                    if (retryCount < 2) {
                        error.disableNotification();
                        retryCount++;
                        setTimeout(() => {
                            QuestionCache.getAsync(id).then((questionInfo1) => {
                                retryCount = 0;
                                questionInfo1 = initQuestionInfo(questionInfo1, response.data.question);
                                resolve(questionInfo1);
                            }, (error1) => {
                                reject(error1);
                            });
                        }, 500);
                    } else {
                        retryCount = 0;
                        error.disableNotification();
                        reject(error.data);
                    }
                });
            } else {
                resolve(questionInfo);
            }
        });
    },
    getAllAsync(questionIds) {
        let requestQuestionIds = [];
        let loadedQuestionInfos = [];
        return new Promise((resolve, reject) => {
            for (let id of questionIds) {
                let questionInfo = QuestionCache.reactiveQuestionInfos.value[id];
                if (questionInfo === undefined || questionInfo.simple || (questionInfo.remoteUpdated && questionInfo.downloadRemoteUpdated)) {
                    requestQuestionIds.push(id);
                } else {
                    loadedQuestionInfos.push(questionInfo);
                }
            }
            if (requestQuestionIds.length === 0) {
                resolve(loadedQuestionInfos);
            } else {
                WebSocketConnector.send({
                    type: 'getQuestionInfos',
                    data: {
                        questionIds: requestQuestionIds
                    }
                }).then((response) => {
                    for (let question of response.data.questions) {
                        let questionInfo = QuestionCache.reactiveQuestionInfos.value[question.id];
                        questionInfo = initQuestionInfo(questionInfo, question);
                        loadedQuestionInfos.push(questionInfo);
                    }
                    resolve(loadedQuestionInfos);
                });
            }
        });
    },
    getQuestionSimpleDataByPartitionId(id) {
        return new Promise((resolve, reject) => {
            WebSocketConnector.send({
                type: "getQuestionSimpleData",
                data: {
                    partitionId: id
                }
            }).then((response) => {
                let resolveData = [];
                for (const questionSimpleInfo1 of response.data.questionList) {
                    if (QuestionCache.reactiveQuestionInfos.value[questionSimpleInfo1.id] === undefined) {
                        const questionSimpleInfo = reactive({
                            question: {
                                id: questionSimpleInfo1.id,
                                content: questionSimpleInfo1.content,
                                type: questionSimpleInfo1.type,
                                authorQQ: questionSimpleInfo1.authorQQ,
                                enabled: questionSimpleInfo1.enabled,
                            },
                            type: "Question",
                            simple: true,
                            dirty: false,
                            errors: questionSimpleInfo1.errors,
                            showError: Object.keys(questionSimpleInfo1.errors).length > 0,
                            warnings: questionSimpleInfo1.warnings,
                            showWarning: Object.keys(questionSimpleInfo1.warnings).length > 0,
                            inputMeta: {},
                            ableToEdit: true,
                            ableToDelete: true,
                        });
                        questionSimpleInfo.verify = () => {
                            verify(questionSimpleInfo);
                        }
                        questionSimpleInfo.ableToEdit = QuestionCache.checkEditPermission(questionSimpleInfo.question);
                        questionSimpleInfo.ableToDelete = QuestionCache.checkDeletePermission(questionSimpleInfo.question);
                        // questionSimpleInfo.id = questionSimpleInfo1.id;
                        // questionSimpleInfo.content = questionSimpleInfo1.content;
                        // questionSimpleInfo.type = "question";
                        // questionSimpleInfo.simple = true;
                        // questionSimpleInfo.status = "";
                        QuestionCache.reactiveQuestionInfos.value[questionSimpleInfo1.id] = questionSimpleInfo;
                        resolveData.push(questionSimpleInfo);
                    } else {
                        resolveData.push(QuestionCache.reactiveQuestionInfos.value[questionSimpleInfo1.id]);
                    }
                }
                resolve(resolveData);
            }, (error) => {
                reject(error);
            });
        });
    },
    hasErrorQuestions: false,
    isQuestionInfoHasError(questionInfo) {
        if (!questionInfo.question.localDeleted &&
            questionInfo.errors !== undefined &&
            Object.keys(questionInfo.errors).length > 0 &&
            !questionInfo.getGroup) {
            QuestionCache.hasErrorQuestions = true;
            return true;
        } else if (questionInfo.question.type === "QuestionGroup" && questionInfo.questionInfos) {
            for (const questionInfo1 of questionInfo.questionInfos) {
                if (questionInfo1.errors !== undefined && Object.keys(questionInfo1.errors).length > 0) {
                    QuestionCache.hasErrorQuestions = true;
                    return true;
                }
            }
            return false;
        }
    },
    getDirtyErrorQuestions() {
        let data = [];
        for (const questionId in QuestionCache.reactiveQuestionInfos.value) {
            let questionInfo = QuestionCache.reactiveQuestionInfos.value[questionId];
            if (questionInfo.dirty && (!(questionInfo.localNew && questionInfo.question.localDeleted)) && QuestionCache.isQuestionInfoHasError(questionInfo, data)) {
                data.push(questionInfo);
            }
        }
        if (data.length === 0) {
            QuestionCache.hasErrorQuestions = false;
        }
        return data;
    },
    update(questionInfo) {
        // if (isRef(questionInfo)) questionInfo = questionInfo.value;
        const originalQuestionInfo = QuestionCache.originalQuestionInfos[questionInfo.question.id];
        if (
            questionInfo && originalQuestionInfo && originalQuestionInfo.question
            || questionInfo.localNew || questionInfo.remoteDeleted
            || questionInfo.remoteUpdated || questionInfo.question.localDeleted
        ) {
            questionInfo.verify()
            let differFromOriginal;
            if (questionInfo.remoteUpdated || (questionInfo.remoteDeleted && questionInfo.question.localDeleted)) {
                differFromOriginal = false
            } else {
                differFromOriginal =
                    questionInfo.question.localDeleted ||
                    questionInfo.localNew ||
                    questionInfo.remoteDeleted ||
                    (questionInfo.question.type !== "QuestionGroup"
                        && !isQuestionsEqual(questionInfo.question, originalQuestionInfo.question)) ||
                    (questionInfo.question.type === "QuestionGroup"
                        && !isQuestionGroupEqual(questionInfo, originalQuestionInfo))
            }
            for (const action of onUpdateLocal) {
                action(questionInfo, differFromOriginal);
            }
            questionInfo.dirty = differFromOriginal;
            if (differFromOriginal) {
                QuestionCache.dirtyQuestionInfos[questionInfo.question.id] = questionInfo;
            } else {
                delete QuestionCache.dirtyQuestionInfos[questionInfo.question.id];
            }
            QuestionCache.dirty = Object.keys(QuestionCache.dirtyQuestionInfos).length > 0;
            QuestionCache.reactiveDirty.value = QuestionCache.dirty;
            questionInfo.ableToEdit = QuestionCache.checkEditPermission(questionInfo.question);
            questionInfo.ableToDelete = QuestionCache.checkDeletePermission(questionInfo.question);
        }
    },
    /*    differFromOriginal(questionInfo) {
            let originalQuestionInfo = QuestionCache.originalQuestionInfos[questionInfo.id];
            return !isQuestionsEqual(questionInfo, originalQuestionInfo.question);
        },*/
    uploadAll() {
        uploading.value = true;
        let data = [];
        let questionInfos = [];
        for (const questionId in QuestionCache.reactiveQuestionInfos.value) {
            let questionInfo = QuestionCache.reactiveQuestionInfos.value[questionId];
            if (!questionInfo.simple && (questionInfo.dirty || questionInfo.localNew) &&
                !QuestionCache.isQuestionInfoHasError(questionInfo) &&
                !(questionInfo.getGroup instanceof Function)) {
                let target;
                questionInfos.push(questionInfo);
                if (questionInfo.question.type === "QuestionGroup") {
                    const copiedInfo = JSON.parse(JSON.stringify(questionInfo));
                    const questions = [];//TODO test
                    for (const questionInfo1 of copiedInfo.questionInfos) {
                        questions.push(questionInfo1.question);
                    }
                    copiedInfo.question.questions = questions;
                    target = copiedInfo.question;
                } else {
                    target = questionInfo.question;
                }
                if (!questionInfo.question.localDeleted) {
                    if (questionInfo.localNew) {
                        data.push(target);
                    } else {
                        data.push(getDifferenceQuestion(target));
                    }
                }
            }
        }
        const handleResponse = (response) => {
            const succeedUpdatedQuestionIds = response.data.succeedUpdatedQuestionIds;
            const succeedDeletedQuestionIds = response.data.succeedDeletedQuestionIds;
            const failedQuestionIdReasons = response.data.failedQuestionIdReason;
            for (const questionInfo of questionInfos) {
                if (succeedUpdatedQuestionIds.includes(questionInfo.question.id)) {
                    delete questionInfo.localNew;
                    delete questionInfo.question.localDeleted;
                    delete questionInfo.remoteDeleted;
                    delete questionInfo.remoteUpdated;
                    delete questionInfo.downloadRemoteUpdated;
                    QuestionCache.originalQuestionInfos[questionInfo.question.id] = JSON.parse(JSON.stringify(questionInfo));
                    // questionInfo.verify();
                    localUploadedQuestionIds.add(questionInfo.question.id);
                    QuestionCache.update(questionInfo);
                } else if (succeedDeletedQuestionIds.includes(questionInfo.question.id)) {
                    QuestionCache.completelyRemove(questionInfo);
                } else {
                    const failedReasons = failedQuestionIdReasons[questionInfo.question.id];
                    console.warn(questionInfo.question.id, failedReasons);
                }
            }
            /*
                        if (questionInfos.length === 0) {
                            QuestionCache.dirtyQuestionInfos = {};
                            QuestionCache.dirty = false;
                            QuestionCache.reactiveDirty.value = false;
                        }
            */
            uploading.value = false;
        }
        return new Promise((resolve, reject) => {
            WebSocketConnector.send({
                type: "updateQuestions",
                data: {
                    updatedQuestions: data,
                    deletedQuestionIds: Array.from(localDeletedQuestionIds),
                }
            }).then((response) => {
                handleResponse(response);
                resolve(response.data);
            }, (err) => {
                if (err.data.succeedUpdatedQuestionIds !== undefined &&
                    err.data.succeedDeletedQuestionIds !== undefined &&
                    err.data.failedQuestionIdReason !== undefined) {

                    err.disableNotification();
                    handleResponse(err);
                    resolve(err.data);
                } else {
                    reject(err);
                }
            })
        });
    },
    create(question, save = true) {
        let info = reactive({
            question: question,
            type: "Question",
            simple: false,
            dirty: true,
            errors: {},
            showError: false,
            warnings: {},
            showWarning: false,
            inputMeta: {},
            localNew: true,
            ableToEdit: QuestionCache.checkEditPermission(question),
            ableToDelete: QuestionCache.checkDeletePermission(question),
            verify: () => {
                verify(info);
            }
        });
        if (save) QuestionCache.reactiveQuestionInfos.value[question.id] = info;
        return info;
    },
    createQuestionGroup(group, questionInfos, authorQQ) {
        if (questionInfos === undefined) {
            questionInfos = [];
            for (let i = 0; i < 2; i++) {
                questionInfos.push(QuestionCache.create({
                    id: randomUUIDv4(),
                    content: "",
                    type: "MultipleChoicesQuestion",
                    enabled: false,
                    partitionIds: null,
                    authorQQ: authorQQ,
                    upVoters: [],
                    downVoters: [],
                    choices: [{
                        id: randomUUIDv4(), correct: true, content: ""
                    }, {
                        id: randomUUIDv4(), correct: false, content: ""
                    }]
                }, false));
            }
        }
        let info = reactive({
            question: group,
            questionInfos: questionInfos,
            type: "Question",
            simple: false,
            dirty: true,
            errors: {},
            showError: false,
            warnings: {},
            showWarning: false,
            inputMeta: {},
            localNew: true,
            ableToEdit: QuestionCache.checkEditPermission(group),
            ableToDelete: QuestionCache.checkDeletePermission(group),
            verify: () => {
                verify(info);
            }
        });
        group.type = "QuestionGroup";
        for (const questionInfo of info.questionInfos) {
            questionInfo.getGroup = () => info;
        }
        QuestionCache.reactiveQuestionInfos.value[group.id] = info;
        return info;
    },
    appendToGroup: (questionGroupInfo, subQuestionInfo) => {
        delete subQuestionInfo.partitionIds;
        subQuestionInfo.getGroup = () => questionGroupInfo;
        questionGroupInfo.value.questionInfos.push(subQuestionInfo);
    },
    completelyRemove(questionInfo) {
        const id = questionInfo.question.id;
        localDeletedQuestionIds.add(id);
        delete QuestionCache.reactiveQuestionInfos.value[id];
        delete QuestionCache.dirtyQuestionInfos[id];
        questionInfo.remoteDeleted = true;
        questionInfo.question.localDeleted = true;
        questionInfo.localNew = false;
        QuestionCache.dirty = Object.keys(QuestionCache.dirtyQuestionInfos).length > 0;
        QuestionCache.reactiveDirty.value = QuestionCache.dirty;
        deleteQuestionInternal(id);
    },
    delete(id) {
        let questionInfo = QuestionCache.reactiveQuestionInfos.value[id];
        if (questionInfo.simple) {
            QuestionCache.getAsync(id).then((questionInfo1) => {
                questionInfo = questionInfo1;
                deleteQuestion();
            });
        } else {
            deleteQuestion();
        }

        function deleteQuestion() {
            questionInfo.question.localDeleted = true;
            localDeletedQuestionIds.add(id);
            QuestionCache.update(questionInfo);
        }
    },
    restoreDelete(id) {
        let questionInfo = QuestionCache.reactiveQuestionInfos.value[id];
        delete questionInfo.question.localDeleted;
        QuestionCache.update(questionInfo);
        localDeletedQuestionIds.delete(id);
    },
    restoreChanges(id) {
        const reactiveQuestionInfo = QuestionCache.reactiveQuestionInfos.value[id];
        reactiveQuestionInfo.question = JSON.parse(JSON.stringify(QuestionCache.originalQuestionInfos[id].question));
        reactiveQuestionInfo.question.upVoters = [];
        reactiveQuestionInfo.question.downVoters = [];
        QuestionCache.update(reactiveQuestionInfo);
    },
    syncQuestion(questionInfo) {
        delete questionInfo.localNew;
        questionInfo.remoteUpdated = true;
        questionInfo.downloadRemoteUpdated = true;
        QuestionCache.getAsync(questionInfo.question.id).then((questionInfo1) => {
            questionInfo = questionInfo1;
            questionInfo.verify();
        });
    },
    registerOnQuestionUpdateLocal(action) {
        let length = onUpdateLocal.push(action);
        return () => {
            if (length) {
                onUpdateLocal.splice(length - 1, 1);
                length = undefined;
            } else {
                throw new Error("Listener has been removed");
            }
        }
    },
    registerOnQuestionUpdatedRemote(action) {
        let length = onUpdateRemote.push(action);
        return () => {
            if (length) {
                onUpdateRemote.splice(length - 1, 1);
                length = undefined;
            } else {
                throw new Error("Listener has been removed");
            }
        }
    },
    registerOnQuestionDeleted(action) {
        let length = onDelete.push(action);
        return () => {
            if (length) {
                onDelete.splice(length - 1, 1);
                length = undefined;
            } else {
                throw new Error("Listener has been removed");
            }
        }
    },
    getQuestionNodeObjOf(questionOrInfo, partitionId) {
        let data;
        let id;
        if (questionOrInfo.id) {//if it is a question
            let questionStorageInfo = QuestionCache.reactiveQuestionInfos.value[questionOrInfo.id];
            const simple = questionStorageInfo ? questionStorageInfo.simple : false;
            const dirty = questionStorageInfo ? questionStorageInfo.dirty : true;
            const errors = questionStorageInfo ? questionStorageInfo.errors : {};
            const warnings = questionStorageInfo ? questionStorageInfo.warnings : {};
            data = {
                question: questionOrInfo,
                type: "Question",
                simple: simple,
                dirty: dirty,
                errors: errors,
                showError: false,
                warnings: warnings,
                showWarning: false,
                inputMeta: {},
                ableToEdit: QuestionCache.checkEditPermission(questionOrInfo),
                ableToDelete: QuestionCache.checkDeletePermission(questionOrInfo),
            };
            // verify(data);
            id = questionOrInfo.id;
        } else {//if it is a warped question info
            data = questionOrInfo;
            id = questionOrInfo.question.id;
        }
        const node = {
            data: reactive(data),
            leaf: true,
            treeId: partitionId + "/" + id,
            disabled: !QuestionCache.checkEditPermission(data.question)
        };
        watch(() => node.data.question.ableToEdit, () => node.disabled = node.data.question.ableToEdit);
        return node;
    },
    wrapToQuestionInfo(question) {
        return initInfoWithoutCaching(undefined, question);
    },
    removePartitionFromAllQuestions(partition) {
        for (const [id, questionInfo] of Object.entries(QuestionCache.reactiveQuestionInfos.value)) {
            if (!questionInfo.simple) {
                const index = questionInfo.question.partitionIds.indexOf(partition.id);
                if (index > 0) {
                    questionInfo.question.partitionIds.splice(index, 1);
                }
            }
        }
    },
    getAllLocal(questionIds) {
        const loadedQuestionInfos = [];
        for (let id of questionIds) {
            let questionInfo = QuestionCache.reactiveQuestionInfos.value[id];
            if (questionInfo !== undefined && questionInfo.simple === false) {
                loadedQuestionInfos.push(questionInfo);
            }
        }
        return loadedQuestionInfos;
    },
    checkEditPermission(question) {
        if (!question) return true;
        let ableToEditOwnQuestion = PermissionInfo.hasPermission('question', 'create and edit owns questions')
        let ableToEditOthersQuestion = PermissionInfo.hasPermission('question', 'edit others questions')
        let ableToEditOwnQuestionGroup = PermissionInfo.hasPermission('question group', 'create and edit owns question groups')
        let ableToEditOthersQuestionGroup = PermissionInfo.hasPermission('question group', 'edit others question groups')
        let ableToEdit;
        if (question.type === "QuestionGroup") {
            if (question.authorQQ !== undefined && question.authorQQ === currentUser.value.qq) {
                ableToEdit = ableToEditOwnQuestionGroup;
            } else {
                ableToEdit = ableToEditOthersQuestionGroup;
            }
        } else {
            if (question.authorQQ !== undefined && question.authorQQ === currentUser.value.qq) {
                ableToEdit = ableToEditOwnQuestion;
            } else {
                ableToEdit = ableToEditOthersQuestion;
            }
        }
        return ableToEdit;
    }, checkDeletePermission(question) {
        if (!question) return true;
        let ableToDeleteOwnQuestion = PermissionInfo.hasPermission('question', 'delete owns questions')
        let ableToDeleteOthersQuestion = PermissionInfo.hasPermission('question', 'delete others question')
        let ableToDeleteOwnQuestionGroup = PermissionInfo.hasPermission('question group', 'delete owns question groups')
        let ableToDeleteOthersQuestionGroup = PermissionInfo.hasPermission('question group', 'delete others question group')
        let ableToDelete;
        if (question.type === "QuestionGroup") {
            if (question.authorQQ !== undefined && question.authorQQ === currentUser.value.qq) {
                ableToDelete = ableToDeleteOwnQuestionGroup;
            } else {
                ableToDelete = ableToDeleteOthersQuestionGroup;
            }
        } else {
            if (question.authorQQ !== undefined && question.authorQQ === currentUser.value.qq) {
                ableToDelete = ableToDeleteOwnQuestion;
            } else {
                ableToDelete = ableToDeleteOthersQuestion;
            }
        }
        return ableToDelete;
    }

}
export default QuestionCache