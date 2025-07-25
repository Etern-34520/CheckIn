<script setup>
import ResponsiveDoubleSplitpane from "@/components/common/ResponsiveDoubleSplitpane.vue";
import UserCard from "@/components/user/UserCard.vue";
import UserDataInterface from "@/data/UserDataInterface.js";
import router from "@/router/index.js";
import HarmonyOSIcon_Plus from "@/components/icons/HarmonyOSIcon_Plus.vue";
import getAvatarUrlOf from "@/utils/Avatar.js";
import WebSocketConnector from "@/api/websocket.js";
import CustomDialog from "@/components/common/CustomDialog.vue";
import PermissionInfo from "@/auth/PermissionInfo.js";

// const user = UserDataInterface.getCurrentUser();
const users = UserDataInterface.users;
const userGroups = UserDataInterface.roles;
const filterText = ref("");
const loading = ref(true);
const loadingGroups = ref(true);
const error = ref(false);
const showCreateUser = ref(false);
const nameOfNewUser = ref("");
const QQOfNewUser = ref(null);
const groupOfNewUser = ref(null);
const creatingError = ref(false);
const createOver = ref(false);
const waitingResponse = ref(false);
const createUserDialog = ref(null);

UserDataInterface.getReactiveUserGroupsAsync().then((userGroups1) => {
    loadingGroups.value = false;
}, () => {
    loadingGroups.value = false;
});

UserDataInterface.getUsersAsync().then((users1) => {
    // noinspection JSValidateTypes
    // users.value = users1;
    loading.value = false;
}, () => {
    loading.value = false;
    error.value = true;
});

const openView = (user) => {
    router.push({
        name: 'user-detail', params: {
            id: user.qq
        }
    });
}

const doFilter = (user) => {
    if (filterText.value === "") return true;
    else {
        const filterTextItems = filterText.value.split(",");
        for (const filterTextItem of filterTextItems) {
            if (filterTextItem.length === 0) continue;
            if (user.name.includes(filterTextItem) || user.qq.toString().includes(filterTextItem) || user.role.includes(filterTextItem)) {
                return true;
            }
        }
        return false;
    }
}

const responsiveSplitpane = ref();

const stop = router.afterEach((to, from) => {
    if (to.params.id === undefined) {
        responsiveSplitpane.value.showLeft();
    } else {
        responsiveSplitpane.value.hideLeft();
    }
});

onUnmounted(() => {
    stop();
});

watch(() => QQOfNewUser.value, (newValue) => {
    if (UserDataInterface.users[newValue]) {
        creatingError.value = true;
        createUserDialog.value.showTip("用户已存在", "error");
    } else {
        creatingError.value = false;
        createUserDialog.value.clearTip();
    }
});

const buttonsOption = ref([
    {
        id: "cancel",
        content: "取消",
        type: "info",
        onclick: () => {
            showCreateUser.value = false;
        },
        hidden: computed(() => {
            return createOver.value;
        })
    }, {
        id: "confirm",
        content: computed(() => createOver.value ? "完成" : "确定"),
        type: "primary",
        onclick: () => {
            if (createOver.value) {
                showCreateUser.value = false;
            } else {
                confirmCreate();
            }
        },
        disabled: computed(() => {
            return creatingError.value || !QQOfNewUser.value || !groupOfNewUser.value;
        })
    }
])

const confirmCreate = () => {
    waitingResponse.value = true;
    WebSocketConnector.send({
        type: "createUser",
        data: {
            name: nameOfNewUser.value ? nameOfNewUser.value : "新用户",
            qq: QQOfNewUser.value,
            roleType: groupOfNewUser.value
        }
    }).then((response) => {
        waitingResponse.value = false;
        createOver.value = true;
        createUserDialog.value.showTip(`初始密码（只显示一次）：${response.data.initPassword}`, "primary");
    }, (err) => {
        waitingResponse.value = false;
    });
}

const hideCreatingDialog = () => {
    showCreateUser.value = false;
    nameOfNewUser.value = "";
    QQOfNewUser.value = null;
    groupOfNewUser.value = null;
    createOver.value = false;
}
</script>

<template>
    <responsive-double-splitpane ref="responsiveSplitpane" :left-loading="loading" show-left-label="用户列表">
        <template #left>
            <custom-dialog v-model="showCreateUser" :buttons-option="buttonsOption" ref="createUserDialog"
                           title="新建用户" @closed="hideCreatingDialog">
                <div style="display: flex;flex-direction: column;">
                    <div style="width: 56px;height: 56px;align-self: center">
                        <transition name="blur-scale" mode="out-in">
                            <el-avatar :key="QQOfNewUser" shape="circle" size="large"
                                       :src="getAvatarUrlOf(QQOfNewUser)"/>
                        </transition>
                    </div>
                    <div class="dialog-input-label">
                        <el-text>用户名</el-text>
                    </div>
                    <el-input v-model="nameOfNewUser" placeholder="新用户"/>
                    <div class="dialog-input-label">
                        <el-text>QQ</el-text>
                        <el-text type="primary" style="margin-left: 4px">*</el-text>
                    </div>
                    <el-input type="number" v-model.number="QQOfNewUser"/>
                    <div class="dialog-input-label">
                        <el-text>用户组</el-text>
                        <el-text v-model="groupOfNewUser" type="primary" style="margin-left: 4px">*</el-text>
                    </div>
                    <el-select filterable v-model="groupOfNewUser" placeholder="选择">
                        <template v-for="(userGroup,i) in userGroups">
                            <el-option
                                    :disabled="!PermissionInfo.hasPermission('role','operate role ' + userGroup.type)"
                                    :value="userGroup.type" :label="userGroup.type"></el-option>
                        </template>
                    </el-select>
                </div>
            </custom-dialog>
            <div style="display: flex;flex-direction: row">
                <el-input v-model="filterText" placeholder="搜索用户（以&quot;,&quot;分词）" prefix-icon="Search"
                          style="margin-bottom: 8px;flex: 1"/>
                <!--suppress JSValidateTypes -->
                <el-button :icon="HarmonyOSIcon_Plus" style="margin-left: 8px" @click="showCreateUser = true"
                           v-if="PermissionInfo.hasPermission('manage user', 'create user')"
                           class="disable-init-animate">
                    新建用户
                </el-button>
            </div>
            <el-scrollbar>
                <div class="init-animate">
                    <transition-group name="filter">
                        <template v-for="(user,i) in users" :key="user.qq">
                            <user-card class="clickable disable-init-animate" :user="user" @click="openView(user)"
                                       v-if="doFilter(user)"/>
                        </template>
                    </transition-group>
                </div>
            </el-scrollbar>
        </template>
        <template #right>
            <router-view v-slot="{ Component }">
                <transition mode="out-in" name="blur-scale">
                    <div v-if="!Component"
                         style="width: 100%;height: 100%;display: flex;flex-direction: column;align-items: center;justify-content: center">
                        <el-text type="info" size="large">
                            选择用户以查看
                        </el-text>
                    </div>
                    <component v-else :is="Component"/>
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
    max-height: 37px;
    transform: translateX(0);
}

/*noinspection CssUnusedSymbol*/
.hide-cancel-enter-active,
.hide-cancel-leave-active {
    transition: opacity 200ms var(--ease-in-out-quint) 0ms,
    filter 200ms var(--ease-in-out-quint) 0ms,
    scale 400ms var(--ease-out-quint) 200ms;
}

/*noinspection CssUnusedSymbol*/
.hide-cancel-enter-from,
.hide-cancel-leave-to {
    scale: 0;
    opacity: 0;
    filter: blur(4px);
}
</style>