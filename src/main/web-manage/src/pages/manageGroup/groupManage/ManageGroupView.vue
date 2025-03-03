<script setup>
import ResponsiveDoubleSplitpane from "@/components/common/ResponsiveDoubleSplitpane.vue";
import router from "@/router/index.js";
import UserGroupCard from "@/components/userGroup/UserGroupCard.vue";
import UserDataInterface from "@/data/UserDataInterface.js";
import HarmonyOSIcon_Plus from "@/components/icons/HarmonyOSIcon_Plus.vue";
import {Sort} from "@element-plus/icons-vue";
import HarmonyOSIcon_Handle from "@/components/icons/HarmonyOSIcon_Handle.vue";
import {VueDraggable} from "vue-draggable-plus";
import WebSocketConnector from "@/api/websocket.js";
import PartitionCache from "@/data/PartitionCache.js";
import _Loading_ from "@/components/common/_Loading_.vue";
import {ElMessage} from "element-plus";
import PermissionInfo from "@/auth/PermissionInfo.js";

const responsiveSplitpane = ref(null);
const filterText = ref("");
const userGroups = UserDataInterface.userGroups;
const userGroupsArray = ref([]);
watch(() => UserDataInterface.userGroups, (newValue) => {
    userGroupsArray.value = [];
    for (const userGroupType in userGroups) {
        userGroupsArray.value.push(userGroups[userGroupType]);
    }
}, {immediate: true, deep: true});
const loading = ref(true);

const changingLevel = ref(false);
const waitForResponse = ref(false);

const doFilter = (userGroup) => {
    if (filterText.value === "") return true;
    else {
        const filterTextItems = filterText.value.split(",");
        for (const filterTextItem of filterTextItems) {
            if (filterTextItem.length === 0) continue;
            if (userGroup.type.includes(filterTextItem)) {
                return true;
            }
        }
        return false;
    }
}

const stop = router.afterEach((to, from) => {
    if (to.params.id === undefined) {
        if (responsiveSplitpane.value)
            responsiveSplitpane.value.showLeft();
    } else {
        if (responsiveSplitpane.value)
            responsiveSplitpane.value.hideLeft();
    }
});

onUnmounted(() => {
    stop();
});

UserDataInterface.getReactiveUserGroupsAsync().then((userGroups1) => {
    loading.value = false;
}, () => {
    loading.value = false;
});

const openView = (userGroup) => {
    router.push({
        name: 'group-detail', params: {
            type: userGroup.type
        }
    });
}

const switchChangingLevel = () => {
    if (changingLevel.value) {
        waitForResponse.value = true;
        const levels = [];
        let index = 0;
        for (const userGroup of userGroupsArray.value) {
            levels[index] = userGroup.type;
            index++;
        }
        WebSocketConnector.send({
            type: "updateRoleLevels",
            levels: levels
        }).then((response) => {
            if (response.type === 'success') {
                ElMessage({
                    type: 'success',
                    message: '修改成功',
                });
            } else {
                ElMessage({
                    type: 'error',
                    message: '修改失败',
                });
            }            waitForResponse.value = false;
        }, (err) => {
            ElMessage({
                type: 'error',
                message: '修改失败',
            });
            waitForResponse.value = false;
        });
    }
    changingLevel.value = !changingLevel.value;
}

const createdGroupName = ref("");

const confirmCreating = () => {
    WebSocketConnector.send({
        type: "createRole",
        roleType: createdGroupName.value
    });
    createdGroupName.value = "";
}

const cancelCreating = () => {
    createdGroupName.value = "";
}
</script>

<template>
    <responsive-double-splitpane ref="responsiveSplitpane" :left-loading="loading" show-left-label="用户组列表">
        <template #left>
            <div style="display: flex;flex-direction: row;flex-wrap: wrap;">
                <el-input v-model="filterText" placeholder="搜索用户组（以&quot;,&quot;分词）" prefix-icon="Search"
                          style="margin-bottom: 8px;flex: 1;min-width: 200px"/>
                <el-popover trigger="click" width="400px" @after-leave="cancelCreating" style="margin-left: 8px;"
                            v-if="PermissionInfo.hasPermission('role','create role')">
                    <template #reference>
                        <!--suppress JSValidateTypes -->
                        <el-button :icon="HarmonyOSIcon_Plus" style="margin-left: 8px" class="disable-init-animate">
                            新建用户组
                        </el-button>
                    </template>
                    <template #default>
                        <div style="display: flex;flex-direction: row">
                            <el-input
                                    v-model="createdGroupName"
                                    style="flex:1;margin-right: 4px"
                                    placeholder="用户组名"
                            />
                            <el-button-group>
                                <el-button type="primary" @click="confirmCreating"
                                           :disabled="createdGroupName===''||Boolean(UserDataInterface.userGroups[createdGroupName])">
                                    确定
                                </el-button>
                            </el-button-group>
                        </div>
                    </template>
                </el-popover>
<!--                <el-button :icon="Sort" :loading="waitForResponse" :loading-icon="_Loading_" class="disable-init-animate"
                           @click="switchChangingLevel" style="margin-left: 0">
                    {{ changingLevel ? "完成" : "调整级别" }}
                </el-button>-->
            </div>
            <el-scrollbar>
                <VueDraggable
                        ref="draggable"
                        v-model="userGroupsArray"
                        :animation="150"
                        ghostClass="ghost"
                        handle=".handle">
                    <transition-group name="filter">
                        <template v-for="userGroup of userGroupsArray" :key="userGroup.type">
                            <div style="display: flex;flex-direction: row;align-items: center"
                                 v-if="doFilter(userGroup)">
                                <transition name="handle" mode="out-in">
                                    <div class="handle" style="cursor: grab">
                                        <HarmonyOSIcon_Handle v-if="changingLevel" style="margin-right: 8px"
                                                              :size="18"/>
                                    </div>
                                </transition>
                                <user-group-card class="clickable disable-init-animate" :user-group="userGroup"
                                                 @click="openView(userGroup)" style="flex: 1"/>
                            </div>
                        </template>
                    </transition-group>
                </VueDraggable>
            </el-scrollbar>
        </template>
        <template #right>
            <router-view v-slot="{ Component }">
                <transition mode="out-in" name="blur-scale">
                    <component :is="Component"/>
                </transition>
            </router-view>
        </template>
    </responsive-double-splitpane>
</template>

<style scoped>
/*noinspection CssUnusedSymbol*/
.filter-enter-active {
    overflow: hidden;
    transition: transform 0.3s var(--ease-out-quint) 0.2s,
    max-height 0.2s var(--ease-in-out-quint);
}

/*noinspection CssUnusedSymbol*/
.filter-leave-active {
    overflow: hidden;
    transition: transform 0.3s var(--ease-in-quint),
    max-height 0.2s var(--ease-in-out-quint) 0.3s;
}

/*noinspection CssUnusedSymbol*/
.filter-enter-from, .filter-leave-to {
    max-height: 0;
    transform: translateX(-100%);
}

/*noinspection CssUnusedSymbol*/
.filter-leave-from, .filter-enter-to {
    max-height: 40px;
    transform: translateX(0);
}

/*noinspection CssUnusedSymbol*/
.handle-enter-active,
.handle-leave-active {
    transition: 0.3s var(--ease-out-quint) 0.2s;
}

/*noinspection CssUnusedSymbol*/
.handle-enter-from, .handle-leave-to {
    margin-left: -26px;
}
</style>