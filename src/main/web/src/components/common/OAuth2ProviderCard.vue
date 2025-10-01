<script setup>
import {PictureRounded} from "@element-plus/icons-vue";
import HarmonyOSIcon_Remove from "@/components/icons/HarmonyOSIcon_Remove.vue";
import HarmonyOSIcon_Handle from "@/components/icons/HarmonyOSIcon_Handle.vue";
import HarmonyOSIcon_Plus from "@/components/icons/HarmonyOSIcon_Plus.vue";
import {ElMessage} from "element-plus";
import _Loading_ from "@/components/common/_Loading_.vue";
import {uuidv7} from "uuidv7";
import WebSocketConnector from "@/api/websocket.js";

const {proxy} = getCurrentInstance();

const model = defineModel({
    type: Object,
    required: true
});

const error = defineModel("error", {
    type: Boolean
});

const unwatch = watch(() => model.value,
    () => {
        error.value = !model.value.name ||
            !model.value.iconDomain ||
            !model.value.authorizationUri ||
            !model.value.jwksUri ||
            !model.value.userInfoUri ||
            !model.value.clientId ||
            !model.value.clientSecret ||
            !model.value.userIdAttributeName;
}, {
    deep: true,
    immediate: true
});

onBeforeUnmount(() => {
    unwatch();
});

const props = defineProps({
    editing: {
        type: Boolean,
        required: true
    }
});

const emit = defineEmits(['removeProvider']);
const addScopeItemVisible = ref(false);
const newScopeItemName = ref("");
const confirmAddScopeItem = () => {
    addScopeItemVisible.value = false;
    model.value.scope.push(newScopeItemName.value);
    newScopeItemName.value = "";
}
const removeScopeItem = (index) => {
    model.value.scope.splice(index, 1);
}

const addAttribute = () => {
    if (!model.value.otherAttributes) {
        model.value.otherAttributes = [];
    }
    model.value.otherAttributes.push({});
}
const removeAttribute = (index) => {
    model.value.otherAttributes.splice(index, 1);
}

const autoFilling = ref(false);
const autoFillOidc = () => {
    autoFilling.value = true;
    try {
        WebSocketConnector.send({
            type: "discoverOidc",
            data: {
                issuerUrl: model.value.issuerUri + "/.well-known/openid-configuration"
            }
        }).then((resp) => {
            const data = resp.data.data;
            model.value.authorizationUri = data.authorizationUri;
            model.value.userInfoUri = data.userInfoUri;
            model.value.jwksUri = data.jwksUri;
            if ((!model.value.scope || model.value.scope.length === 0) && data.supportedScopes) {
                model.value.scope = data.supportedScopes;
            }
            ElMessage.success("自动补全成功");
            autoFilling.value = false;
        }, (e) => {
            ElMessage.error("自动补全失败: " + (e?.response?.data || e?.message || e));
            autoFilling.value = false;
        });
    } catch (e) {
        ElMessage.error("自动补全失败: " + (e?.response?.data || e.message));
        autoFilling.value = false;
    }
};

const optionValues = ["disabled","required", "optional"];
const optionNames = {
    disabled: "禁用",
    required: "必须",
    optional: "可选",
}
</script>

<template>
    <div class="slide-hide-base">
        <div class="panel-1 disable-init-animate">
            <div class="oauth2-provider-card">
                <div class="title" style="display: flex;flex-direction: row;flex-wrap: wrap;align-items: stretch;gap: 0 12px">
                    <transition name="handle" mode="out-in">
                        <div v-if="editing" style="display: flex;flex-direction: row;align-items: center">
                            <div class="handle"
                                 style="cursor: grab;width: 20px;height: 32px;display: flex;align-items: center;margin-right: 16px">
                                <HarmonyOSIcon_Handle :size="20"/>
                            </div>
                            <el-button class="disable-init-animate"
                                       style="min-height: 32px;" link
                                       @click="emit('removeProvider')">
                                <HarmonyOSIcon_Remove/>
                            </el-button>
                        </div>
                    </transition>
                    <el-image :src="'https://favicon.im/' + model.iconDomain"
                              style="width: 24px;height: 24px;padding: 4px;margin-right: 8px;">
                        <template #error>
                            <el-icon :size="24">
                                <PictureRounded/>
                            </el-icon>
                        </template>
                    </el-image>
                    <div>
                        <el-text style="align-self: center" type="info">
                            后台登录
                        </el-text>
                        <el-switch :disabled="!editing" class="disable-init-animate"
                                   style="align-self: center" v-model="model.enabledInLogin"/>
                    </div>
                    <div>
                        <el-text style="align-self: center" type="info">
                            生成试题
                        </el-text>
                        <transition name="blur-scale" mode="out-in">
                            <el-segmented :options="optionValues" v-if="editing" v-model="model.examLoginMode">
                                <template #default="{item}">
                                    <el-text>{{ optionNames[item] }}</el-text>
                                </template>
                            </el-segmented>
                            <el-text v-else style="text-wrap: wrap;word-break: break-all">{{ optionNames[model.examLoginMode] }}</el-text>
                        </transition>
                    </div>
                </div>
                <div style="display: flex;flex-direction: row;flex-wrap: wrap;align-items: stretch">
                    <div class="provider-item">
                        <el-text type="info" style="text-wrap: nowrap">
                            名称
                        </el-text>
                        <transition name="blur-scale" mode="out-in">
                            <el-input class="disable-init-animate" v-if="editing" :class="{error: !model.name}"
                                      placeholder="注册名"
                                      style="height: 32px;flex: 1;max-width: 120px;min-width: 0" v-model="model.name"/>
                            <el-text v-else style="text-wrap: wrap;word-break: break-all">
                                {{ model.name || "null" }}
                            </el-text>
                        </transition>
                    </div>
                    <div class="provider-item">
                        <el-text type="info">
                            图标域名
                        </el-text>
                        <transition name="blur-scale" mode="out-in">
                            <el-input class="disable-init-animate" :class="{error: !model.iconDomain}" v-if="editing"
                                      placeholder="用于获取图标"
                                      style="height: 32px;flex: 1;max-width: 200px;min-width: 0"
                                      v-model="model.iconDomain"/>
                            <el-text v-else style="text-wrap: wrap;word-break: break-all">
                                {{ model.iconDomain || "null" }}
                            </el-text>
                        </transition>
                    </div>
                    <div class="provider-item">
                        <el-text type="info">
                            Issuer 根 URL
                        </el-text>
                        <transition name="blur-scale" mode="out-in">
                            <div style="min-height: 32px;flex: 1;min-width: 0;display: flex;flex-direction: row"
                                 v-if="editing">
                                <el-input class="disable-init-animate"
                                          placeholder="用于自动补全"
                                          style="height: 32px;flex: 1;max-width: 200px;min-width: 0"
                                          v-model="model.issuerUri"/>
                                <el-button
                                    v-if="editing"
                                    :loading="autoFilling"
                                    :loading-icon="_Loading_"
                                    link
                                    size="small"
                                    style="margin-left: 8px;align-self: center"
                                    @click="autoFillOidc"
                                >自动补全
                                </el-button>
                            </div>
                            <el-text v-else style="text-wrap: wrap;word-break: break-all">
                                {{ model.issuerUri || "null" }}
                            </el-text>
                        </transition>
                    </div>
                </div>
                <div style="display: flex;flex-direction: row;flex-wrap: wrap;align-items: stretch">
                    <div class="provider-item">
                        <el-text type="info">
                            Authorization URI
                        </el-text>
                        <transition name="blur-scale" mode="out-in">
                            <el-input class="disable-init-animate" v-if="editing"
                                      :class="{error: !model.authorizationUri}"
                                      placeholder="需参考提供商文档"
                                      style="height: 32px;flex: 1;max-width: 300px;min-width: 0"
                                      v-model="model.authorizationUri"/>
                            <el-text v-else style="text-wrap: wrap;word-break: break-all">
                                {{ model.authorizationUri || "null" }}
                            </el-text>
                        </transition>
                    </div>
                    <div class="provider-item">
                        <el-text type="info">
                            JWKs URI
                        </el-text>
                        <transition name="blur-scale" mode="out-in">
                            <el-input class="disable-init-animate" v-if="editing" :class="{error: !model.jwksUri}"
                                      placeholder="需参考提供商文档"
                                      style="height: 32px;flex: 1;max-width: 300px;min-width: 0"
                                      v-model="model.jwksUri"/>
                            <el-text v-else style="text-wrap: wrap;word-break: break-all">
                                {{ model.jwksUri || "null" }}
                            </el-text>
                        </transition>
                    </div>
                    <div class="provider-item">
                        <el-text type="info">
                            UserInfo URI
                        </el-text>
                        <transition name="blur-scale" mode="out-in">
                            <el-input class="disable-init-animate" v-if="editing" placeholder="需参考提供商文档"
                                      :class="{error: !model.userInfoUri}"
                                      style="height: 32px;flex: 1;max-width: 300px;min-width: 0"
                                      v-model="model.userInfoUri"/>
                            <el-text v-else style="text-wrap: wrap;word-break: break-all">
                                {{ model.userInfoUri || "null" }}
                            </el-text>
                        </transition>
                    </div>
                </div>
                <div style="display: flex;flex-direction: row;flex-wrap: wrap;align-items: stretch">
                    <div class="provider-item">
                        <el-text type="info">
                            Client ID
                        </el-text>
                        <transition name="blur-scale" mode="out-in">
                            <el-input class="disable-init-animate" v-if="editing" placeholder="需从提供商获取"
                                      :class="{error: !model.clientId}"
                                      style="height: 32px;flex: 1;max-width: 400px;min-width: 0"
                                      v-model="model.clientId"/>
                            <el-text v-else style="text-wrap: wrap;word-break: break-all">
                                {{ model.clientId || "null" }}
                            </el-text>
                        </transition>
                    </div>
                    <div class="provider-item">
                        <el-text type="info">
                            Client secret
                        </el-text>
                        <transition name="blur-scale" mode="out-in">
                            <el-input class="disable-init-animate" v-if="editing" placeholder="需从提供商获取"
                                      :class="{error: !model.clientSecret}"
                                      style="height: 32px;flex: 1;max-width: 400px;min-width: 0"
                                      v-model="model.clientSecret"/>
                            <el-text v-else style="text-wrap: wrap;word-break: break-all">
                                {{ model.clientSecret || "null" }}
                            </el-text>
                        </transition>
                    </div>
                </div>
                <div class="provider-item" style="margin-right: 0;">
                    <el-text type="info">
                        Scope
                    </el-text>
                    <div style="display: flex;flex-direction: row;flex-wrap: wrap;gap: 4px">
                        <el-tag v-for="(scopeItem,index) of model.scope"
                                :closable="editing" @close="removeScopeItem(index)" type="info">
                            {{ scopeItem || "null" }}
                        </el-tag>
                        <transition name="blur-scale" mode="out-in">
                            <el-popover trigger="click" v-if="editing" popper-style="width: 260px" v-model:visible="addScopeItemVisible">
                                <template #reference>
                                    <el-button link class="disable-init-animate">
                                        <el-icon>
                                            <HarmonyOSIcon_Plus/>
                                        </el-icon>
                                    </el-button>
                                </template>
                                <template #default>
                                    <div class="no-pop-padding" style="display: flex;flex-direction: row;width: 260px">
                                        <el-input placeholder="需参考提供商文档" class="disable-init-animate"
                                                  v-model="newScopeItemName"/>
                                        <el-button type="primary" class="disable-init-animate"
                                                   :disabled="!newScopeItemName || model.scope.includes(newScopeItemName)"
                                                   @click="confirmAddScopeItem">
                                            完成
                                        </el-button>
                                    </div>
                                </template>
                            </el-popover>
                        </transition>
                    </div>
                </div>
                <div class="provider-item">
                    <el-text type="info">
                        用户 ID 属性名
                    </el-text>
                    <transition name="blur-scale" mode="out-in">
                        <el-input class="disable-init-animate" v-if="editing" placeholder="需参考提供商文档"
                                  :class="{error: !model.userIdAttributeName}"
                                  style="height: 32px;flex: 1;max-width: 140px" v-model="model.userIdAttributeName"/>
                        <el-text v-else style="text-wrap: wrap;word-break: break-all">
                            {{ model.userIdAttributeName || "null" }}
                        </el-text>
                    </transition>
                </div>
                <div style="display: flex;flex-direction: column;align-items: stretch;margin-top: 12px">
                    <div class="provider-item">
                        <el-text style="align-self: start" type="info">其他参数</el-text>
                        <el-button class="disable-init-animate" v-if="editing"
                                   style="margin-left: 4px;align-self: start;height: 18px" link @click="addAttribute"
                                   :icon="HarmonyOSIcon_Plus">
                            添加参数
                        </el-button>
                    </div>
                    <transition-group name="smooth-height">
                        <div class="smooth-height-base" v-for="(attr,index) of model.otherAttributes"
                             :key="attr.tempId || (attr.tempId = uuidv7())">
                            <div>
                                <div class="provider-item"
                                     style="margin-right: 0;margin-bottom: 4px;flex-wrap: wrap;height: auto;">
                                    <el-input placeholder="name" class="disable-init-animate"
                                              style="margin-right: 4px;flex: 1;min-width: 100px"
                                              v-model="model.otherAttributes[index].name"/>
                                    <el-input placeholder="value" class="disable-init-animate"
                                              style="margin-right: 20px;flex: 2;min-width: 100px"
                                              v-model="model.otherAttributes[index].value"/>
                                    <el-text style="margin-right: 4px;text-wrap: nowrap">
                                        启用
                                    </el-text>
                                    <el-switch class="disable-init-animate" style="margin-right: 12px" :disabled="!editing"
                                               v-model="model.otherAttributes[index].enabled"/>
                                    <div style="width: 32px">
                                        <transition name="blur-scale" mode="out-in">
                                            <el-button class="disable-init-animate" v-if="editing"
                                                       link @click="removeAttribute(index)"
                                                       style="height: 32px">
                                                <HarmonyOSIcon_Remove/>
                                            </el-button>
                                        </transition>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </transition-group>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.oauth2-provider-card {
    padding: 8px 12px;
    min-height: 0;

    display: flex;
    flex-direction: column;
    align-items: stretch;

    > * {
        display: flex;
        flex-direction: row;
        align-items: center;
        /*margin-bottom: 4px;*/
        /*        padding: 4px;*/
    }
}

.provider-item {
    display: flex;
    flex-direction: row;
    min-height: 32px;
}

.provider-item:not(:last-child) {
    margin-right: 16px;
}

.title {
    display: flex;
    flex-direction: row;
    gap: 12px;

    > div:not(:first-child) {
        display: flex;
        flex-direction: row;
        align-items: stretch;
    }
}

.el-text:first-child {
    margin-right: 12px;
}


/*noinspection CssUnusedSymbol*/
.handle-enter-active,
.handle-leave-active {
    transition: 0.3s var(--ease-out-quint);
}

/*noinspection CssUnusedSymbol*/
.handle-enter-from, .handle-leave-to {
    margin-left: -80px;
    opacity: 0;
    filter: blur(4px);
}
</style>