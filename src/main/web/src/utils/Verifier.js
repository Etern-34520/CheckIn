import WebSocketConnector from "@/api/websocket.js";
import {ElNotification} from "element-plus";
import QuestionCache from "@/data/QuestionCache.js";

let verificationRules;

WebSocketConnector.registerAction("updateVerificationRules", (response) => {
    console.debug("Verification rules updated from remote:", verificationRules);
    verificationRules = response.data;
    for (const questionInfo of Object.values(QuestionCache.reactiveQuestionInfos.value)) {
        if (!questionInfo.simple) {
            questionInfo.verify();
        }
    }
})

function loadRules() {
    WebSocketConnector.send({
        type: "getVerificationSetting"
    }).then((response) => {
        // verificationRules = [];
        verificationRules = response.data.data;
    }, (err) => {
        ElNotification({
            title: '获取校验设置失败', message: err, position: 'bottom-right', type: 'error', duration: 0
        });
    });
}

loadRules();

function from(object) {// 模拟 Java Stream api，构建一个可定制的校验器然后执行
    const nonNext = {
        type: "NonNext",
        detail: "无下一个处理单元"
    };

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
            do: () => {
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
                        handler(nonNext)
                    }
                    if (field === null || field === undefined) {
                        handler(nonNext)
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
                        handler(nonNext)
                    }
                    if (!(obj instanceof Array)) {
                        handler(nonNext)
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
                            handler(nonNext)
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
                    handler(nonNext)
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
                handler(nonNext)
            }
        };

        const internal1 = (name, value) => {
            currentStream.next = {
                name: name,
                do: emptyDo
            }
            currentStream = currentStream.next;
            let currentStreamReserved = currentStream;
            return {
                validate: (func, handler) => {
                    validate(func, handler);
                    currentStreamReserved.next.do(value, handler);
                }
            };
        }

        const max = () => {
            return internal1("max", maxValue);
        }

        const min = () => {
            return internal1("min", minValue);
        }

        const sum = () => {
            return internal1("sum", sumValue);
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


export default {
    verifyQuestionInfo: (questionInfo) => {
        try {
            const question = questionInfo.question;

            questionInfo.ableToEdit = QuestionCache.checkEditPermission(question)
            questionInfo.ableToDelete = QuestionCache.checkDeletePermission(question)
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
                if (rule.objectName === question.type) {
                    let ruleTrace = rule.property.trace;
                    // console.debug("verify: ruleTrace: " + ruleTrace);
                    let copied = false;
                    const copyIfNecessary = () => {
                        if (!copied) {
                            ruleTrace = JSON.parse(JSON.stringify(ruleTrace));
                            copied = true;
                        }
                    }
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
                                    // console.debug("verify: converting value (field: question.images) from: " + num);
                                    num = num / 1048576.0;//转换到MB
                                    num = Math.trunc(num * 100) / 100;
                                    // console.debug("verify: converting value (field: question.images) to: " + num);
                                }
                                return {num, data};
                            }

                            const validateNumberInternal = (num, data) => {
                                tipTemplate = rule.tipTemplate;
                                const unit = rule.values[1];
                                tipTemplate = tipTemplate.replace("${limit}", ` ${data} ${unit} `);
                                tipTemplate = tipTemplate.replace("${datum}", ` ${num} ${unit} `);
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
                                    case "empty" || "empty-":
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
                                // console.debug("verify: catch empty", data, rule);
                                if (!tipTemplate) {
                                    item = ruleTrace[index - 1];
                                    const {data, num} = calcData(item);
                                    tipTemplate = rule.tipTemplate;
                                    tipTemplate = tipTemplate.replace("${limit}", ` ${data} ${rule.values[1]} `);
                                    tipTemplate = tipTemplate.replace("${datum}", ` ${num} ${rule.values[1]} `);
                                    tipTemplate = tipTemplate.replace("${order}", ` 1 `);
                                }
                                if (data.type === "NonNext" && !rule.ignoreMissingField && (verificationTypeName === "empty" || verificationTypeName === "min")) {
                                    questionInfo.inputMeta[rule.targetInputName + "-" + invokeCount] = rule.level;
                                    target[rule.id + "-" + invokeCount.toString()] = {
                                        content: tipTemplate
                                    };
                                }
                            };

                            // console.debug("verify: final option", streamOption);
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
                            } else if (streamOption.mapToNumber && verificationTypeName === "empty" || verificationTypeName === "empty-") {
                                // console.log(rule);
                                streamOption = streamOption.mapToNumber();
                                streamOption.validate(validateNumberFunc, catchEmpty);
                            }
                        }
                    }
                    handleNext();
                }
            }
            questionInfo.showError = Object.keys(questionInfo.errors).length > 0;
            questionInfo.showWarning = Object.keys(questionInfo.warnings).length > 0;

            if (questionInfo.remoteDeleted) {
                // questionInfo.localNew = true;
                questionInfo.warnings.remoteDeleted = {
                    content: `该${name}已远程删除，上传以保存修改`, buttons: [{
                        content: "删除", action: () => {
                            QuestionCache.completelyRemove(questionInfo);
                        }, type: "danger",
                    }]
                };
                questionInfo.showWarning = true;
            }
            if (question.localDeleted) {
                if (questionInfo.localNew) {
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
            if (!questionInfo.simple && questionInfo.remoteUpdated) {
                questionInfo.warnings.remoteUpdated = {
                    content: `该${name}已远程更新`, buttons: [{
                        content: "同步到本地", action: () => {
                            QuestionCache.syncQuestion(questionInfo);
                        }, type: "primary",
                    }, {
                        content: "忽略", action: () => {
                            // delete QuestionCache.originalQuestionInfos[question.id];
                            questionInfo.localNew = true;
                            delete questionInfo.remoteUpdated;
                            QuestionCache.update(questionInfo);
                        }, type: "default",
                    }]
                };
                questionInfo.showWarning = true;
            }
            if (question.type === "QuestionGroup") {
                if (questionInfo.questionInfos && questionInfo.questionInfos.length !== 0) {
                    let order = 0;
                    for (let questionInfo1 of questionInfo.questionInfos) {
                        order++;
                        questionInfo1.verify();
                    }
                }
            }
            if (questionInfo.getGroup instanceof Function && (questionInfo.showWarning || questionInfo.showError)) {
                const questionGroup = questionInfo.getGroup();
                if (questionInfo.showWarning) {
                    questionGroup.showWarning = true;
                }
                if (questionInfo.showError) {
                    questionGroup.showError = true;
                }
            }
        } catch (e) {
            console.error(e);
            ElNotification({
                title: "校验出错", message: e, position: "bottom-right", type: "error", duration: 0
            });
        }
    },
    loadRules: loadRules
}