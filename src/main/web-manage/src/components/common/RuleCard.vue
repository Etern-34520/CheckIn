<script setup>
/**
 * 规则配置逻辑：
 * 根属性名与 questionInfo(.value).question.type相同
 * 子属性名可嵌套，与对应子属性名相同，如
 *     "question" 对应 questionInfo(.value).question
 *     "question.content.size" 对应 questionInfo(.value).question.content.size
 * 若父属性为数组，子属性需要区分是数组的属性还是子元素的属性
 * 1.“$...”为子元素属性，可以添加”*“进行总和统计，如 "$..." "$*..."
 *     特殊逻辑：若出现形如 "$xxx=xxx" 时，根据判断条件（===）过滤，若出现"&count"、"&min"、"&max"立刻终止并校对次数/最小值/最大值
 * 2."..."为数组属性，如 "length"
 * 校对类型的处理：
 * 1.普通属性/无前缀数组元素属性
 *     对于 max 校对类型（数字、字符串）：取所有元素中对应属性最大值
 *     对于 min 校对类型（数字、字符串）：取所有元素中对应属性最小值
 *     对于 empty 校对类型（数字、字符串）：【存在一个】元素对应属性为空时触发
 * 2.带”*“前缀
 *     对于 max 校对类型（数字、字符串）：取所有元素中对应属性【总和】最大值
 *     对于 min 校对类型（数字、字符串）：取所有元素中对应属性【总和】最小值
 *     对于 empty 校对类型（数字、字符串）：【所有】元素对应属性为空时触发
 * */
import PropertySelector from "@/components/common/PropertySelector.vue";

const props = defineProps({
    editing: {
        type: Boolean,
        default: false
    }
});

const model = defineModel({
    required: true,
});
if (!model.value.objectName) model.value.objectName = "MultipleChoicesQuestion";
if (!model.value.property) model.value.property = {};
if (!model.value.values) model.value.values = [];
const transitionCaller = ref(false);
const verificationOption = ref();
const optionValues = ["MultipleChoicesQuestion", "QuestionGroup"];
const optionNames = {
    MultipleChoicesQuestion: "选择题",
    QuestionGroup: "题组",
}

const propertySelector = ref();
const emptyVerification = (type, label, defaultTip, targetInputName) => {
    return {
        type: type,
        inputType: {
            types: {
                label: {
                    dataPosition: 0,
                    text: label
                }
            }
        },
        targetInputName: targetInputName,
        defaultTip: defaultTip
    }
}
const contentVerification = (type, defaultTip, targetInputName) => {
    return {
        type: type,
        inputType: {
            types: {
                /*                slider: {
                                    dataPosition: 0
                                }, */number: {
                    dataPosition: 0
                }, label: {
                    dataPosition: 1,
                    text: "字符"
                }
            },
            dataOption: [
                {
                    default: 400,
                    min: 1,
                    max: 21845,
                }
            ]
        },
        targetInputName: targetInputName,
        defaultTip: defaultTip
    };
}
const contentOption = {
    name: "内容",
    properties: {
        length: {
            name: "长度",
            verificationTypes: {
                max: contentVerification("上限", "内容长度超过${limit}", "content"),
                min: contentVerification("下限", "内容长度低于${limit}", "content"),
                empty: emptyVerification("无内容", "当内容为空时", "无内容", "content")
            }
        }
    }
};
const singleImageSizeVerification = (type, defaultTip, targetInputName) => {
    return {
        type: type,
        inputType: {
            types: {
                number: {
                    dataPosition: 0
                }, label: {
                    dataPosition: 1,
                    text: "MB"
                }
            },
            dataOption: [
                {
                    default: 4,
                    min: 0,
                    max: 16,
                }
            ]
        },
        targetInputName: targetInputName,
        defaultTip: defaultTip
    }
};
const allImagesSizeVerification = (type, defaultTip, targetInputName) => {
    return {
        type: type,
        inputType: {
            types: {
                number: {
                    dataPosition: 0
                }, label: {
                    dataPosition: 1,
                    text: "MB"
                }
            },
            dataOption: [
                {
                    default: 16,
                    min: 0,
                    max: 128,
                }
            ]
        },
        targetInputName: targetInputName,
        defaultTip: defaultTip
    }
};
const imagesQuantityVerification = (type, defaultTip, targetInputName) => {
    return {
        type: type,
        inputType: {
            types: {
                number: {
                    dataPosition: 0
                }, label: {
                    dataPosition: 1,
                    text: "张"
                }
            },
            dataOption: [
                {
                    default: 8,
                    min: 0,
                    max: 20,
                }
            ]
        },
        targetInputName: targetInputName,
        defaultTip: defaultTip
    }
};
const imageOption = {
    name: "图片",
    properties: {
        "$size": {
            name: "单张大小",
            verificationTypes: {
                max: singleImageSizeVerification("上限", "第${order}张图片大小超过${limit}", "images"),
                min: singleImageSizeVerification("下限", "第${order}张图片大小低于${limit}", "images")
            }
        },
        "$*size": {
            name: "总大小",
            verificationTypes: {
                max: allImagesSizeVerification("上限", "图片总大小超过${limit}", "images"),
                min: allImagesSizeVerification("下限", "图片总大小低于${limit}", "images")
            }
        },
        length: {
            name: "数量",
            verificationTypes: {
                max: imagesQuantityVerification("上限", "图片数量超过${limit}", "images"),
                min: imagesQuantityVerification("下限", "图片数量少于${limit}", "images"),
                empty: emptyVerification("无图片", "无图片时", "无图片", "images")
            }
        }
    }
};
const choiceLengthVerification = (type, defaultTip, targetInputName) => {
    return {
        type: type,
        inputType: {
            types: {
                number: {
                    dataPosition: 0
                }, label: {
                    dataPosition: 1,
                    text: "字符"
                }
            },
            dataOption: [
                {
                    default: 100,
                    min: 1,
                    max: 256,
                }
            ]
        },
        targetInputName: targetInputName,
        defaultTip: defaultTip
    }
};
const quantityVerification = (type, default1, min, max, defaultTip, targetInputName) => {
    return {
        type: type,
        inputType: {
            types: {
                slider: {
                    dataPosition: 0
                }, number: {
                    dataPosition: 0
                }, label: {
                    dataPosition: 1,
                    text: "个"
                }
            },
            dataOption: [
                {
                    default: default1,
                    min: min,
                    max: max
                }
            ]
        },
        targetInputName: targetInputName,
        defaultTip: defaultTip
    }
};
const choiceOption = {
    name: "选项",
    properties: {
        "$content": {
            name: "内容",
            properties: {
                length: {
                    name: "长度",
                    verificationTypes: {
                        max: choiceLengthVerification("上限", "第${order}选项内容长度超过${limit}", "choice$content"),
                        min: choiceLengthVerification("下限", "第${order}选项内容长度低于${limit}", "choice$content"),
                        empty: emptyVerification("无内容", "当内容为空时", "第${order}选项内容为空", "choice$content")
                    }
                }
            }
        },
        "$*correct=true": {
            name: "正确选项",
            properties: {
                "&count": {
                    name: "数量",
                    verificationTypes: {
                        max: quantityVerification("上限", 5, 1, 20, "正确选项数量超过${limit}", "choices"),
                        min: quantityVerification("下限", 1, 1, 20, "正确选项数量少于${limit}", "choices"),
                        empty: emptyVerification("无正确选项", "没有正确选项时", "无正确选项", "choices"),
                    }
                }
            }
        },
        "$*correct=false": {
            name: "错误选项",
            properties: {
                "&count": {
                    name: "数量", verificationTypes: {
                        max: quantityVerification("上限", 5, 1, 20, "错误选项数量超过${limit}", "choices"),
                        min: quantityVerification("下限", 5, 1, 20, "错误选项数量少于${limit}", "choices"),
                        empty: emptyVerification("无错误选项", "无错误选项时", "无错误选项", "choices"),
                    }
                }
            }
        },
        length: {
            name: "数量",
            verificationTypes: {
                max: quantityVerification("上限", 10, 2, 20, "选项数量超过${limit}", "choices"),
                min: quantityVerification("下限", 2, 2, 20, "选项数量少于${limit}", "choices"),
            }
        }
    }
};
const partitionOption = {
    name: "分区",
    properties: {
        length: {
            name: "数量",
            verificationTypes: {
                max: quantityVerification("上限", 10, 1, 30, "分区数量超过${limit}", "partitions"),
                min: quantityVerification("下限", 10, 1, 30, "分区数量少于${limit}", "partitions"),
                empty: emptyVerification("无分区", "没有所属分区时", "没有所属分区", "partitions")
            }
        }
    }
}
const subQuestionOption = {
    name: "子题目",
    properties: {
        length: {
            name: "数量",
            verificationTypes: {
                max: quantityVerification("上限", 10, 1, 30, "子题目数量超过${limit}", "subquestions"),
                min: quantityVerification("下限", 10, 1, 30, "子题目数量少于${limit}", "subquestions"),
                empty: emptyVerification("无子题目", "没有子题目时", "没有子题目", "subquestions")
            }
        }
    }
};
const authorOption = {
    name: "作者",
    verificationTypes: {
        empty: emptyVerification("无作者", "作者字段为空时", "无作者", "author")
    }
}
const objectMap = {
    MultipleChoicesQuestion: {
        properties: {
            "question.content": contentOption,
            "question.partitionIds": partitionOption,
            "question.images": imageOption,
            "question.authorQQ": authorOption,
            "question.choices": choiceOption,
        }
    },
    QuestionGroup: {
        properties: {
            "question.content": contentOption,
            "question.partitionIds": partitionOption,
            "question.images": imageOption,
            "question.authorQQ": authorOption,
            "questionInfos": subQuestionOption,
        },
    }
}
const getOptionOrDefault = (index, field, defaultValue) => {
    let dataOptionElement = verificationOption.value.inputType.dataOption[index];
    let value = dataOptionElement[field];
    return value === undefined ? defaultValue : value;
}
let firstWatch = true;
watch(() => model.value.property.verificationTypeName, (value, oldValue, onCleanup) => {
    if (model.value.property.trace && model.value.property.verificationTypeName) {
        let currentOption = objectMap[model.value.objectName];
        for (const traceItem of model.value.property.trace) {
            currentOption = currentOption.properties[traceItem];
        }
        // const lastVerificationOption = oldValue;
        verificationOption.value = currentOption.verificationTypes[model.value.property.verificationTypeName];
        model.value.targetInputName = verificationOption.value.targetInputName;
        if (!firstWatch) {
            model.value.values = [];
            if (verificationOption.value.inputType.dataOption && verificationOption.value.inputType.types) {
                for (const [key, typeItem] of Object.entries(verificationOption.value.inputType.types)) {
                    if (!isNaN(typeItem.dataPosition)) {
                        const optionItem = verificationOption.value.inputType.dataOption[typeItem.dataPosition];
                        if (optionItem && optionItem.default) {
                            model.value.values[typeItem.dataPosition] = optionItem.default;
                        }
                    }
                }
            }
        }
        // if ((!model.value.tipTemplate) || model.value.tipTemplate.length === 0 ||
        //         (lastVerificationOption && lastVerificationOption.defaultTip && model.value.tipTemplate === lastVerificationOption.defaultTip))
            model.value.tipTemplate = verificationOption.value.defaultTip;
    } else {
        verificationOption.value = undefined;
    }
    firstWatch = false;
}, {immediate: true});
watchEffect(() => {
    if (!model.value.level ||
            !model.value.objectName ||
            !model.value.property ||
            !model.value.property.trace ||
            !model.value.property.verificationTypeName ||
            !model.value.values ||
            !model.value.tipTemplate) {
        model.value.error = true;
        return;
    } else {
        for (const valueItem of model.value.values) {
            if (valueItem === null) {
                model.value.error = true;
                return;
            }
        }
    }
    model.value.error = false;
});

const addToValues = (datum, index) => {
    model.value.values[index] = datum;
    return datum;
}
</script>

<template>
    <div class="rule-card-base">
        <div style="display: flex;flex-direction: row;flex-wrap: wrap;min-height: 42px;align-items: center">
            <div style="display: flex;margin-right: 8px;flex-direction: row;flex-wrap: wrap;">
                <div style="display: flex;flex-direction: row;" v-if="editing">
                    <el-text style="margin-right: 4px;">对象</el-text>
                    <el-segmented :options="optionValues" v-model="model.objectName">
                        <template #default="{item}">
                            <el-text>{{ optionNames[item] }}</el-text>
                        </template>
                    </el-segmented>
                </div>
                <el-text v-else style="margin-right: 12px;margin-bottom: 2px;margin-top: 2px">
                    {{ optionNames[model.objectName] }}
                </el-text>
                <property-selector ref="propertySelector"
                                   v-if="objectMap[model.objectName] && objectMap[model.objectName].properties"
                                   :properties="objectMap[model.objectName].properties" v-model="model.property"
                                   :editing="editing"/>
            </div>
            <template v-if="!editing && model.level">
                <div class="flex-blank-1"></div>
                <div style="display: flex;flex-direction: row;align-items:center">
                    <el-text type="info"
                             style="display: flex;padding: 0 12px;margin: 4px 4px 4px 0;">提示信息
                    </el-text>
                    <el-text :type="model.level==='error'?'danger':model.level">{{ model.tipTemplate }}</el-text>
                </div>
            </template>
        </div>
        <transition name="blur-scale" mode="out-in">
            <div style="display: flex;flex-direction: row;flex-wrap: wrap;margin-top: 8px;min-height: 42px"
                 v-if="verificationOption"
                 :key="transitionCaller">
                <transition name="blur-scale" mode="out-in">
                    <div style="display: flex;flex-wrap: wrap;margin-bottom: 8px"
                         :key="verificationOption.inputType.types">
                        <template v-for="(option,type,index) in verificationOption.inputType.types">
                            <el-slider v-if="type==='slider'"
                                       v-model="model.values[option.dataPosition]"
                                       class="disable-init-animate" :disabled="!editing"
                                       :min="getOptionOrDefault(option.dataPosition,'min',0)"
                                       :max="getOptionOrDefault(option.dataPosition,'max',1000)"
                                       :step="getOptionOrDefault(option.dataPosition,'step',1)"
                                       style="margin: 0 16px;max-width:400px"/>
                            <el-input-number v-else-if="type==='number'"
                                             v-model="model.values[option.dataPosition]"
                                             class="disable-init-animate" :disabled="!editing"
                                             :min="getOptionOrDefault(option.dataPosition,'min',0)"
                                             :max="getOptionOrDefault(option.dataPosition,'max',1000)"
                                             :step="getOptionOrDefault(option.dataPosition,'step',1)"/>
                            <el-text v-else-if="type==='label'" style="margin-left: 8px;margin-right: 52px;">
                                {{ addToValues(option.text, option.dataPosition) }}
                            </el-text>
                        </template>
                    </div>
                </transition>
                <div class="flex-blank-1"></div>
                <transition name="blur-scale" mode="out-in">
                    <div style="display: flex;margin-bottom: 8px;flex-wrap: wrap" v-if="editing">
                        <div style="display: flex;flex-direction: row;margin-right: 32px">
                            <el-text style="margin-right: 8px;text-wrap: nowrap;">级别</el-text>
                            <el-select v-model="model.level" :class="{error:!(model.level)}"
                                       style="min-width: 100px;width:100px"
                                       placeholder="选择级别" class="disable-init-animate">
                                <el-option value="warning" label="警告">
                                    <el-text type="warning">警告</el-text>
                                </el-option>
                                <el-option value="error" label="错误">
                                    <el-text type="danger">错误</el-text>
                                </el-option>
                            </el-select>
                        </div>
                        <div style="display: flex;flex-direction: row;">
                            <el-text style="margin-right: 8px;text-wrap: nowrap;">提示信息</el-text>
                            <el-input v-model="model.tipTemplate" class="disable-init-animate"
                                      style="max-width: 350px;"/>
                        </div>
                    </div>
                </transition>
            </div>
        </transition>
    </div>
</template>

<style scoped>
.rule-card-base {
    min-height: 80px;
    padding: 8px;
    display: flex;
    flex-direction: column;
    flex: 1;
}
</style>