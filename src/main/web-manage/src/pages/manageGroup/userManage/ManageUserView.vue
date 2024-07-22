<script setup>
import ResponsiveDoubleSplitpane from "@/components/common/ResponsiveDoubleSplitpane.vue";
import UserCard from "@/components/user/UserCard.vue";
import UserDataInterface from "@/data/UserDataInterface.js";
import router from "@/router/index.js";
import HarmonyOSIcon_Plus from "@/components/icons/HarmonyOSIcon_Plus.vue";
import getAvatarUrlOf from "@/utils/Avatar.js";
import WebSocketConnector from "@/api/websocket.js";

// const user = UserDataInterface.getCurrentUser();
const users = UserDataInterface.users;
const userGroups = UserDataInterface.userGroups;
const filterText = ref("");
const loading = ref(true);
const loadingGroups = ref(true);
const error = ref(false);
const showCreateUser = ref(false);
const nameOfNewUser = ref("");
const QQOfNewUser = ref(null);
const groupOfNewUser = ref(null);
const creatingError = ref(false);
const creatingTip = ref("");
const tipTransitionCaller = ref(false);
const createOver = ref(false);
const waitingResponse = ref(false);

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
    router.push("/manage/manage-user/" + user.qq + "/");
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

const responsiveSplitpane = ref(null)

router.afterEach((to, from) => {
    if (to.params.id === undefined) {
        if (responsiveSplitpane.value) {
            // noinspection JSUnresolvedReference
            responsiveSplitpane.value.showLeft();
        }
    } else {
        if (responsiveSplitpane.value) {
            // noinspection JSUnresolvedReference
            responsiveSplitpane.value.hideLeft();
        }
    }
});

watch(() => QQOfNewUser.value, (newValue) => {
    if (UserDataInterface.users[newValue]) {
        creatingError.value = true;
        doTip("用户已存在");
    } else {
        creatingError.value = false;
        doTip("");
    }
});

const doTip = (tip) => {
    tipTransitionCaller.value = !tipTransitionCaller.value;
    creatingTip.value = tip;
}

const confirmCreate = () => {
    waitingResponse.value = true;
    WebSocketConnector.send({
        type: "createUser",
        name: nameOfNewUser.value ? nameOfNewUser.value : "新用户",
        qq: QQOfNewUser.value,
        roleType: groupOfNewUser.value
    }).then((response) => {
        waitingResponse.value = false;
        createOver.value = true;
        doTip(`初始密码（只显示一次）：${response.initPassword}`);
    }, (err) => {
        waitingResponse.value = false;
    });
}

const initCreating = () => {
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
            <el-dialog title="新建用户" align-center append-to-body v-model="showCreateUser" @close="initCreating"
                       style="width:600px" draggable>
                <div style="display: flex;flex-direction: row;">
                    <div style="width: 56px;height: 56px">
                        <transition name="blur-scale" mode="out-in">
                            <el-avatar :key="QQOfNewUser" v-if="QQOfNewUser >= 10000" shape="circle" size="large"
                                       :src="getAvatarUrlOf(QQOfNewUser)" style="margin-top: 8px"/>
                        </transition>
                    </div>
                    <div style="flex:1;display: grid;grid-template-columns:auto minmax(200px, 1fr);justify-items: end;align-items:center;grid-gap: 4px 12px">
                        <el-text>用户名</el-text>
                        <el-input v-model="nameOfNewUser" placeholder="新用户"/>
                        <span>
                        <el-text>QQ</el-text>
                        <el-text type="primary" style="margin-left: 4px">*</el-text>
                    </span>
                        <el-input type="number" v-model.number="QQOfNewUser"/>
                        <span>
                        <el-text>用户组</el-text>
                        <el-text v-model="groupOfNewUser" type="primary" style="margin-left: 4px">*</el-text>
                    </span>
                        <el-select filterable v-model="groupOfNewUser">
                            <template v-for="(userGroup,i) in userGroups">
                                <el-option :value="userGroup.type" :label="userGroup.type"></el-option>
                            </template>
                        </el-select>
                    </div>
                </div>
                <template #footer>
                    <div style="display: flex;flex-direction: row;justify-content: end;margin-bottom: 12px">
                        <transition name="blur-scale" mode="out-in">
                            <el-text :key="tipTransitionCaller" style="margin-right: 16px">{{ creatingTip }}</el-text>
                        </transition>
                        <transition name="blur-scale" mode="out-in">
                            <el-button v-if="!createOver" @click="initCreating">取消</el-button>
                        </transition>
                        <el-button @click="createOver?initCreating():confirmCreate()"
                                   :disabled="creatingError||!QQOfNewUser||!groupOfNewUser"
                                   type="primary">
                            {{ createOver ? "完成" : "确定" }}
                        </el-button>
                    </div>
                </template>
            </el-dialog>
            <div style="display: flex;flex-direction: row">
                <el-input v-model="filterText" placeholder="搜索用户（以&quot;,&quot;分词）" prefix-icon="Search"
                          style="margin-bottom: 8px;flex: 1"/>
                <!--suppress JSValidateTypes -->
                <el-button :icon="HarmonyOSIcon_Plus" style="margin-left: 8px" @click="showCreateUser = true">
                    新建用户
                </el-button>
            </div>
            <el-scrollbar>
                <div class="init-animate">
                    <transition-group name="filter">
                        <template v-for="(user,i) in users" :key="user.qq">
                            <user-card class="clickable no-init-animate" :user="user" @click="openView(user)"
                                       v-if="doFilter(user)"/>
                        </template>
                    </transition-group>
                </div>
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

.filter-enter-from, .filter-leave-to {
    max-height: 0;
    transform: translateX(-100%);
}

.filter-leave-from, .filter-enter-to {
    max-height: 37px;
    transform: translateX(0);
}
</style>