import router from "@/router/index.js";
import randomUUIDv4 from "@/utils/UUID.js";
import {ElNotification} from "element-plus";
import WebSocketConnector from "@/api/websocket.js";
import PermissionInfo from "@/auth/PermissionInfo.js";
import UserDataInterface from "@/data/UserDataInterface.js";

// import {ref} from "vue";

function isQuestionsEqual(q1, q2) {
    if (q1.id === q2.id) {
        const bothHaveNoImages = (q1.images === undefined || q1.images.length === 0) && (q2.images === undefined || q2.images.length === 0);
        const bothHaveNoPartitions = (q1.partitionIds === undefined || q1.partitionIds === null) && (q2.partitionIds === undefined || q2.partitionIds === null);
        const basic = q1.localDeleted === q2.localDeleted && q1.enabled === q2.enabled && q1.randomOrdered === q2.randomOrdered && q1.content === q2.content && q1.authorQQ === q2.authorQQ && ((q1.choices === undefined && q2.choices === undefined) || q1.choices.length === q2.choices.length) && (bothHaveNoImages || q1.images !== undefined && q2.images !== undefined && q1.images.length === q2.images.length) && (bothHaveNoPartitions || q1.partitionIds && q2.partitionIds && q1.partitionIds.length === q2.partitionIds.length);
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

    let differenceQuestion = getDifferenceData(question, QuestionCache.originalQuestionInfos[question.id].question);
    differenceQuestion.id = question.id;
    differenceQuestion.type = question.type;
    // differenceQuestion.choices = question.choices;
    return differenceQuestion;
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
    if (questionInfo && questionInfo.dirty !== true && router.currentRoute.value.params.id !== questionId) {
        delete QuestionCache.reactiveQuestionInfos.value[questionId];
        delete QuestionCache.originalQuestionInfos[questionId];
    } else if (localDeleted) {
        delete QuestionCache.reactiveQuestionInfos.value[questionId];
        delete QuestionCache.originalQuestionInfos[questionId];
        localDeletedQuestionIds.delete(questionId);
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
let verificationRules;

WebSocketConnector.registerAction("updateVerificationRules", (response) => {
    console.debug("Verification rules updated from remote:",verificationRules);
    verificationRules = response.data;
    for (const questionInfo of Object.values(QuestionCache.reactiveQuestionInfos.value)) {
        if (!questionInfo.simple) {
            verify(questionInfo);
        }
    }
})

function loadRules() {
    WebSocketConnector.send({
        type: "getVerificationSetting"
    }).then((response) => {
        verificationRules = response.data.data;
        // console.log(verificationRules);
    }, (err) => {
        ElNotification({
            title: '获取校验设置失败', message: err, position: 'bottom-right', type: 'error', duration: 0
        });
    });
}

loadRules();

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
        verifyImmediately(questionInfo);
    }, 100);
}

function verifyImmediately(questionInfo) {
    try {
        const question = questionInfo.question;

        questionInfo.ableToEdit = checkEditPermission(question)
        questionInfo.ableToDelete = checkDeletePermission(question)
        questionInfo.errors = {};
        questionInfo.warnings = {};
        questionInfo.inputMeta = {};
        questionInfo.showWarning = false;

        let name;
        if (question.type === "QuestionGroup") {
            name = "题组";
        } else {
            name = "题目";
        }

        for (const rule of verificationRules) {
            function from(object) {// 模拟 Java Stream api，构建一个可定制的校验器然后执行
                const stream = {
                    name: "from",
                    data: object,
                    do: (obj, handler) => {
                        if (stream.next)
                            stream.next.do(object, handler);
                    }
                };

                let currentStream = stream;

                function count() {
                    let count = 0;
                    currentStream.next = {
                        name: "count",
                        do: (obj, handler) => {
                            count++;
                        }
                    };
                    currentStream = currentStream.next;
                    let currentStreamReserved = currentStream;
                    return {
                        validate: (func, handler) => {
                            validate(func, handler);
                            currentStreamReserved.next.do(count, handler);
                        }
                    };
                }

                function field(fieldName) {
                    let stream1 = currentStream;
                    currentStream.next = {
                        name: "field",
                        data: fieldName,
                        do: (obj, handler) => {
                            const field = obj[fieldName];
                            if (stream1.next.next && (field !== null && field !== undefined)) {
                                stream1.next.next.do(field, handler);
                            } else {
                                if (!stream1.next.next) {
                                    handler({
                                        type: "NonNext",
                                        detail: "无下一个处理单元"
                                    })
                                }
                                if (field === null || field === undefined) {
                                    handler({
                                        type: "FieldIsNull",
                                        detail: `子字段 "${fieldName}" 为空`
                                    })
                                }
                            }
                        }
                    };
                    currentStream = currentStream.next;
                    return {field, count, each, filter, mapToString, mapToNumber};
                }

                function each() {
                    let stream1 = currentStream;
                    currentStream.next = {
                        name: "each",
                        do: (obj, handler) => {
                            if (stream1.next.next && obj instanceof Array)
                                for (const item of obj) {
                                    stream1.next.next.do(item, handler);
                                }
                            else {
                                if (!stream1.next.next) {
                                    handler({
                                        type: "NonNext",
                                        detail: "无下一个处理单元"
                                    })
                                }
                                if (!obj instanceof Array) {
                                    handler({
                                        type: "NotArray",
                                        detail: "字段不是数组"
                                    })
                                }
                            }
                        }
                    };
                    currentStream = currentStream.next;
                    return {field, count, each, filter, mapToString, mapToNumber};
                }

                function filter(func) {
                    let stream1 = currentStream;
                    currentStream.next = {
                        name: "filter",
                        data: func,
                        do: (obj, handler) => {
                            if (func) {
                                if (func(obj)) {
                                    if (stream1.next.next)
                                        stream1.next.next.do(obj, handler);
                                    else {
                                        handler({
                                            type: "NonNext",
                                            detail: "无下一个处理单元"
                                        })
                                    }
                                }
                            } else {
                                handler({
                                    type: "FilterIsNotFunction",
                                    detail: "过滤参数非函数"
                                })
                            }
                        }
                    };
                    currentStream = currentStream.next;
                    return {field, count, each, filter, mapToString, mapToNumber};
                }

                function mapToString() {
                    let stream1 = currentStream;
                    currentStream.next = {
                        name: "mapToString",
                        do: (obj, handler) => {
                            if (stream1.next.next)
                                stream1.next.next.do(obj.toString(), handler);
                            else {
                                handler({
                                    type: "NonNext",
                                    detail: "无下一个处理单元"
                                })
                            }
                        }
                    };
                    currentStream = currentStream.next;
                    return {validate};
                }

                function mapToNumber() {
                    let maxValue = Number.MAX_VALUE;
                    let minValue = Number.MIN_VALUE;
                    let sumValue = 0;
                    let stream1 = currentStream;

                    const emptyDo = (obj, handler) => {
                        if (!currentStream.next) {
                            handler({
                                type: "NonNext",
                                detail: "无下一个处理单元"
                            })
                        }
                    };

                    const max = () => {
                        currentStream.next = {
                            name: "max",
                            do: emptyDo
                        }
                        currentStream = currentStream.next;
                        let currentStreamReserved = currentStream;
                        return {
                            validate: (func, handler) => {
                                validate(func, handler);
                                currentStreamReserved.next.do(maxValue, handler);
                            }
                        };
                    }

                    const min = () => {
                        currentStream.next = {
                            name: "min",
                            do: emptyDo
                        }
                        currentStream = currentStream.next;
                        let currentStreamReserved = currentStream;
                        return {
                            validate: (func, handler) => {
                                validate(func, handler);
                                currentStreamReserved.next.do(minValue, handler);
                            }
                        };
                    }

                    const sum = () => {
                        currentStream.next = {
                            name: "sum",
                            do: emptyDo
                        }
                        currentStream = currentStream.next;
                        let currentStreamReserved = currentStream;
                        return {
                            validate: (func, handler) => {
                                validate(func, handler);
                                currentStreamReserved.next.do(sumValue, handler);
                            }
                        };
                    }
                    currentStream.next = {
                        name: "mapToNumber",
                        do: (obj, handler) => {
                            if (stream1.next.next) {
                                const number = Number(obj);
                                if (number > minValue) minValue = number;
                                if (number < maxValue) maxValue = number;
                                sumValue += number;
                                stream1.next.next.do(number, handler);
                            }
                        }
                    };
                    currentStream = currentStream.next;
                    return {validate, max, min, sum};
                }

                function validate(func, handler) {
                    currentStream.next = {
                        name: "validate",
                        do: (obj, handler) => {
                            func(obj);
                        }
                    };
                    if (!handler) {
                        handler = (data) => {
                            console.error(data);
                        }
                    }
                    stream.do(null, handler);
                }

                return {stream, field, filter, mapToString, mapToNumber};
            }

            if (rule.objectName === question.type) {
                let ruleTrace = rule.property.trace;
                console.debug("verify: ruleTrace: " + ruleTrace);
                let copied = false;
                const copyIfNecessary = () => {
                    if (!copied) {
                        ruleTrace = JSON.parse(JSON.stringify(ruleTrace));
                        copied = true;
                    }
                }
                const handleRuleTrace = () => {
                    let index = 0;
                    let streamOption = from(questionInfo);
                    let countMode = false;
                    let minMode = false;
                    let maxMode = false;
                    let sumMode = false;
                    // let obj = questionInfo.question;
                    const handleNext = () => {
                        let item = ruleTrace[index];
                        if (index < ruleTrace.length) {
                            if (item === "&count") {
                                countMode = true;
                                return;
                            } else if (item === "&min") {
                                minMode = true;
                                return;
                            } else if (item === "&max") {
                                maxMode = true;
                                return;
                            } else if (item.includes(".")) {
                                copyIfNecessary();
                                ruleTrace.splice(index, 1, ...item.split("."));
                                handleNext();
                            } else if (item.startsWith("$")) {
                                if (item.startsWith("$*")) sumMode = true;
                                item = item.replace("*", "").replace("$", "");
                                copyIfNecessary();
                                ruleTrace.splice(index, 1, item);
                                streamOption = streamOption.each();
                                handleNext();
                            } else if (item.includes("=")) {
                                const split = item.split("=");
                                let fieldName = split[0];
                                copyIfNecessary();
                                ruleTrace.splice(index, 1, fieldName);
                                let target = split[1];
                                if (target === "true") {
                                    target = true;
                                } else if (target === "false") {
                                    target = false;
                                } else if (!isNaN(Number(target))) {
                                    target = Number(target);
                                }
                                const constTarget = target;
                                streamOption = streamOption.filter((obj) => {
                                    return obj[fieldName] === constTarget;
                                });
                            } else {
                                streamOption = streamOption.field(item);
                            }
                            index++;
                            handleNext();
                        } else {
                            let invokeCount = 0;

                            let target;
                            if (rule.level === "error") {
                                target = questionInfo.errors;
                            } else if (rule.level === "warning") {
                                target = questionInfo.warnings;
                            }

                            const verificationTypeName = rule.property.verificationTypeName;
                            let tipTemplate;

                            function calcData(obj) {
                                let num;
                                if ((typeof obj).toLowerCase() === "string") {
                                    num = obj.length;
                                } else if (!isNaN(Number(obj))) {
                                    num = obj;
                                }
                                const data = rule.values[0];
                                if (rule.property.trace.includes("question.images") && (rule.property.trace.includes("$size") || rule.property.trace.includes("$*size"))) {
                                    console.debug("verify: converting value (field: question.images) from: " + num);
                                    num = num / 1048576.0;//转换到MB
                                    num = Math.trunc(num * 100) / 100;
                                    console.debug("verify: converting value (field: question.images) to: " + num);
                                }
                                return {num, data};
                            }

                            const validateNumberInternal = (num, data) => {
                                tipTemplate = rule.tipTemplate;
                                tipTemplate = tipTemplate.replace("${limit}", ` ${data} ${rule.values[1]} `);
                                tipTemplate = tipTemplate.replace("${datum}", ` ${num} ${rule.values[1]} `);
                                tipTemplate = tipTemplate.replace("${order}", ` ${invokeCount + 1} `);
                                const addNotification = () => {
                                    const previousLevel = questionInfo.inputMeta[rule.targetInputName + "-" + invokeCount];
                                    if (!previousLevel || (previousLevel === "warning" && rule.level === "error")) {
                                        questionInfo.inputMeta[rule.targetInputName + "-" + invokeCount] = rule.level;
                                    }
                                    target[rule.id + "-" + invokeCount.toString()] = {
                                        id: rule.id,
                                        content: tipTemplate
                                    };
                                }
                                switch (verificationTypeName) {
                                    case "min":
                                        if (num < data) {
                                            addNotification();
                                        }
                                        break;
                                    case "max":
                                        if (num > data) {
                                            addNotification();
                                        }
                                        break;
                                    case "empty":
                                        if (num === 0) {
                                            addNotification();
                                        }
                                        break;
                                }
                            }

                            const validateNumberFunc = (obj) => {
                                let {num, data} = calcData(obj);
                                validateNumberInternal(num, data);
                                invokeCount++;
                            };
                            const catchEmpty = (data) => {
                                console.debug("verify: catch empty", data, rule);
                                if (!tipTemplate) {
                                    item = ruleTrace[index - 1];
                                    const {data, num} = calcData(item);
                                    tipTemplate = rule.tipTemplate;
                                    tipTemplate = tipTemplate.replace("${limit}", ` ${data} ${rule.values[1]} `);
                                    tipTemplate = tipTemplate.replace("${datum}", ` ${num} ${rule.values[1]} `);
                                    tipTemplate = tipTemplate.replace("${order}", ` 1 `);
                                }
                                if (data.type === "FieldIsNull" && (verificationTypeName === "empty" || verificationTypeName === "min")) {
                                    questionInfo.inputMeta[rule.targetInputName + "-" + invokeCount] = rule.level;
                                    target[rule.id + "-" + invokeCount.toString()] = {
                                        content: tipTemplate
                                    };
                                }
                            };

                            console.debug("verify: final option", streamOption);
                            if (streamOption.validate) {
                                // streamOption.validate(validateNumberFunc, catchEmpty);
                            } else if (countMode || minMode || maxMode) {
                                if (countMode) {
                                    streamOption = streamOption.count();
                                } else if (minMode) {
                                    streamOption = streamOption.min();
                                } else if (maxMode) {
                                    streamOption = streamOption.max();
                                }
                                streamOption.validate((count) => {
                                    const data = rule.values[0];
                                    validateNumberInternal(count, data);
                                }, catchEmpty);
                            } else if (streamOption.mapToNumber &&
                                (verificationTypeName === "min" ||
                                    verificationTypeName === "max")) {
                                streamOption = streamOption.mapToNumber();
                                if (sumMode) {
                                    streamOption = streamOption.sum();
                                }
                                streamOption.validate(validateNumberFunc, catchEmpty);
                            } else if (streamOption.mapToNumber && verificationTypeName === "empty") {
                                console.log(rule);
                                streamOption = streamOption.mapToNumber();
                                streamOption.validate(validateNumberFunc, catchEmpty);
                            }
                        }
                    }
                    handleNext();
                };
                handleRuleTrace();
            }
        }
        questionInfo.showError = Object.keys(questionInfo.errors).length > 0;
        questionInfo.showWarning = Object.keys(questionInfo.warnings).length > 0;

        if (questionInfo.remoteDeleted) {
            questionInfo.localNew = true;
            questionInfo.warnings.remoteDeleted = {
                content: `该${name}已远程删除，上传以保存修改`, buttons: [{
                    content: "删除", action: () => {
                        // delete QuestionTempStorage.reactiveQuestionInfos.value[question.id];
                        QuestionCache.delete(question.id);
                    }, type: "danger",
                }]
            };
            questionInfo.showWarning = true;
        }
        if (question.localDeleted) {
            if (question.localNew) {
                questionInfo.warnings.localDeleted = {
                    content: `该新建${name}已删除`, buttons: [{
                        content: "撤销", action: () => {
                            QuestionCache.restoreDelete(question.id);
                        }, type: "info",
                    }]
                };
            } else {
                questionInfo.warnings.localDeleted = {
                    content: `该${name}已删除，上传后同步`, buttons: [{
                        content: "撤销", action: () => {
                            QuestionCache.restoreDelete(question.id);
                        }, type: "info",
                    }]
                };
            }
        }
        if (questionInfo.remoteUpdated) {
            questionInfo.warnings.remoteUpdated = {
                content: `该${name}已远程更新`, buttons: [{
                    content: "同步到本地", action: () => {
                        questionInfo.downloadRemoteUpdated = true;
                        QuestionCache.getAsync(question.id).then((questionInfo1) => {
                            questionInfo = questionInfo1;
                            questionInfo.verify();
                        });
                    }, type: "primary",
                }, {
                    content: "忽略", action: () => {
                        delete QuestionCache.originalQuestionInfos[question.id];
                        questionInfo.localNew = true;
                        delete questionInfo.remoteUpdated;
                        // questionInfo.verify();
                        QuestionCache.update(questionInfo);
                    }, type: "default",
                }]
            };
            questionInfo.showWarning = true;
        }
        if (question.type === "QuestionGroup") {
            if (questionInfos.length === 0) {
            } else {
                let order = 0;
                for (let questionInfo1 of questionInfo.questionInfos) {
                    order++;
                    questionInfo1.verify();
                }
            }
        }
    } catch (e) {
        console.error(e);
        ElNotification({
            title: "校验出错", message: e, position: "bottom-right", type: "error", duration: 0
        });
    }
}

let retryCount = 0;

const currentUser = UserDataInterface.getCurrentUser();

function checkEditPermission(question) {
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
}

function checkDeletePermission(question) {
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

function initInfoWithoutCaching(questionInfo, question) {
    if (questionInfo === undefined) {
        questionInfo = reactive({
            question: {},
            type: "Question",
            dirty: false,
            errors: {},
            warnings: {},
            ableToEdit: checkEditPermission(question),
            ableToDelete: checkDeletePermission(question),
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
    for (let key in question) {
        if (key === "messageId") continue;
        if (key === "upVoters" || key === "downVoters") {
            questionInfo.question[key] = new Set(question[key]);
            continue;
        }
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
/*    reset() {
        this.reactiveQuestionInfos.value = {};
        this.originalQuestionInfos = {};
        this.dirty = false;
        this.reactiveDirty.value = false;
        this.dirtyQuestionInfos = {};
        onUpdateLocal.length = 0;
        onUpdateRemote.length = 0;
        onDelete.length = 0;
        loadRules();
    },*/
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
    getContentsAndIdsAsyncByPartitionId(id) {
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
                            },
                            type: "Question",
                            simple: true,
                            dirty: false,
                            errors: {},
                            showError: false,
                            warnings: {},
                            showWarning: false,
                            inputMeta: {},
                            ableToEdit: true,
                            ableToDelete: true,
                        });
                        questionSimpleInfo.verify = () => {
                            verify(questionSimpleInfo);
                        }
                        questionSimpleInfo.ableToEdit = checkEditPermission(questionSimpleInfo.question);
                        questionSimpleInfo.ableToDelete = checkDeletePermission(questionSimpleInfo.question);
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
    getAllAsyncByPartitionId(id) {
        return new Promise((resolve, reject) => {
            WebSocketConnector.send({
                type: "getQuestionIdsByPartitionId",
                data: {
                    partitionId: id
                }
            }).then((response) => {
                QuestionCache.getAllAsync(response.ids).then((response1) => {
                    resolve(response1);
                }, (error) => {
                    reject(error);
                });
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
        }
    },
    getErrorQuestions() {
        let data = [];
        for (const questionId in QuestionCache.reactiveQuestionInfos.value) {
            let questionInfo = QuestionCache.reactiveQuestionInfos.value[questionId];
            if (QuestionCache.isQuestionInfoHasError(questionInfo, data)) {
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
            if (questionInfo.remoteUpdated || questionInfo.remoteDeleted && questionInfo.question.localDeleted) {
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
            questionInfo.ableToEdit = checkEditPermission(questionInfo.question);
            questionInfo.ableToDelete = checkDeletePermission(questionInfo.question);
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
                    copiedInfo.question.questionInfos = copiedInfo.questionInfos;
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
                    // delete questionInfo.question.localDeleted;
                    // delete questionInfo.localNew;
                    questionInfo.remoteDeleted = true;
                    questionInfo.question.localDeleted = true;
                    QuestionCache.update(questionInfo);
                    // delete questionInfo.remoteUpdated;
                    // delete questionInfo.downloadRemoteUpdated;
                    deleteQuestionInternal(questionInfo.question.id);
                } else {
                    const failedQuestionIdReason = failedQuestionIdReasons[questionInfo.question.id];
                    console.warn(questionInfo.question.id + ":" + failedQuestionIdReason);
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
            ableToEdit: checkEditPermission(question),
            ableToDelete: checkDeletePermission(question),
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
                    upVoters: new Set(),
                    downVoters: new Set(),
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
            ableToEdit: checkEditPermission(group),
            ableToDelete: checkDeletePermission(group),
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
        if (isRef(questionGroupInfo)) subQuestionInfo.getGroup = () => questionGroupInfo.value;
        else subQuestionInfo.getGroup = () => questionGroupInfo;
        questionGroupInfo.value.questionInfos.push(subQuestionInfo);
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
        reactiveQuestionInfo.question.upVoters = new Set();
        reactiveQuestionInfo.question.downVoters = new Set();
        QuestionCache.update(reactiveQuestionInfo);
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
        }    },
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
                ableToEdit: checkEditPermission(questionOrInfo),
                ableToDelete: checkDeletePermission(questionOrInfo),
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
            disabled: !checkEditPermission(data.question)
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
    }
}
export default QuestionCache