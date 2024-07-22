<script setup>
import Waterfall from "@/components/common/Waterfall.vue";
import UserDataInterface from "@/data/UserDataInterface.js";
import router from "@/router/index.js";
import Collapse from "@/components/common/Collapse.vue";
import UserCard from "@/components/user/UserCard.vue";
import {Search} from "@element-plus/icons-vue";

/*
defineProps({
    userGroup: {
        type: Object,
        required: true
    }
})
*/
const userGroup = ref(undefined);
const loading = ref(true);
const users = ref([]);
const transitionCaller = ref(false);
const permissionGroups = ref([]);
const editing = ref(false);
let copyOfPermissionGroups;

const filterText = ref("");
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

watch(() => router.currentRoute.value.params.type, () => {
    loading.value = true;
    users.value = [];
    transitionCaller.value = !transitionCaller.value;
    UserDataInterface.getUserGroupAsync(router.currentRoute.value.params.type).then((userGroup1) => {
        userGroup.value = userGroup1;
        UserDataInterface.getUsersOfUserGroupSync(userGroup1.type).then((users1) => {
            users.value = users1;
        });
        loading.value = false;
        UserDataInterface.getPermissionGroupsOfGroupAsync(userGroup1.type).then((permissionGroups1) => {
            permissionGroups.value = permissionGroups1;
        });
    }, () => {
        loading.value = false;
        error.value = true;
    });
}, {immediate: true});

const startEditing = () => {
    editing.value = true;
    copyOfPermissionGroups = JSON.parse(JSON.stringify(permissionGroups.value));
}

const savePermissions = () => {
    editing.value = false;
    UserDataInterface.savePermissionsGroupOfUserGroup(userGroup.value.type, permissionGroups.value).then(() => {
    }, () => {
    });
}

const cancelEditing = () => {
    editing.value = false;
    permissionGroups.value = copyOfPermissionGroups;
}

const onPermissionCardClick = (permission) => {
    if (editing.value) {
        permission.enabled = !permission.enabled
    }
}
</script>

<template>
    <div v-loading="loading" style="height: 100%;">
        <transition name="blur-scale" mode="out-in">
            <div :key="transitionCaller" style="display: flex;flex-direction: column;padding: 8px 16px">
                <template v-if="!loading">
                    <el-text style="font-size: 32px;align-self: baseline;margin: 8px 0;">{{ userGroup.type }}</el-text>
                    <collapse>
                        <template #title>
                            <div style="display: flex;flex-direction: row;align-items: center;height: 100%;margin-left: 8px">
                                <el-text>权限</el-text>
                                <transition name="scale-blur">
                                    <div style="height: 25px;margin-left: 8px" v-if="!editing" @click.stop>
                                        <el-button type="info" link @click="startEditing">修改</el-button>
                                    </div>
                                    <el-button-group style="margin-left: 8px" v-else @click.stop>
                                        <el-button type="primary" link @click="savePermissions">保存</el-button>
                                        <el-button type="info" link @click="cancelEditing">取消</el-button>
                                    </el-button-group>
                                </transition>
                            </div>
                        </template>
                        <template #content>
                            <waterfall :data="permissionGroups" :min-row-width="300">
                                <template #item="{item:permissionGroup,index}">
                                    <div style="margin: 4px">
                                        <div style="display: flex;margin: 0 8px;">
                                            <el-text size="large">{{ permissionGroup.name }}</el-text>
                                            <div class="flex-blank-1"></div>
                                            <el-text type="info">{{ permissionGroup.description }}</el-text>
                                        </div>
                                        <waterfall :data="permissionGroup.permissions" :min-row-width="300">
                                            <template #item="{item:permission,index}">
                                                <div class="panel-1" style="margin: 2px;padding: 8px 16px"
                                                     @click="onPermissionCardClick(permission)">
                                                    <div style="display:flex;height: 20px">
                                                        <el-text size="large">
                                                            {{ permission.name }}
                                                        </el-text>
                                                        <div class="flex-blank-1"></div>
                                                        <el-switch :disabled="!editing" style="max-height: 20px;"
                                                                   @click.stop
                                                                   v-model="permission.enabled"/>
                                                    </div>
                                                    <el-text type="info">
                                                        {{ permission.description }}
                                                    </el-text>
                                                </div>
                                            </template>
                                        </waterfall>
                                    </div>
                                </template>
                            </waterfall>
                        </template>
                    </collapse>
                    <div style="display:flex;height: 48px;align-items: center">
                        <el-text style="font-size: 16px;align-self: baseline;margin-top: 12px;margin-bottom: 8px">
                            组用户
                        </el-text>
                        <div class="flex-blank-1"></div>
                        <!--suppress JSValidateTypes -->
                        <el-input style="width: 300px" :prefix-icon="Search" v-model="filterText"
                                  placeholder="搜索用户（以&quot;,&quot;分词）"/>
                    </div>
                    <waterfall :data="users">
                        <template #item="{item,index}">
                            <transition name="scale-blur">
                                <user-card class="clickable" :user="item" style="animation-delay: 0ms!important;"
                                           v-if="doFilter(item)"
                                           @click="router.push({path:`/manage/manage-user/${item.qq}/`})"/>
                            </transition>
                        </template>
                    </waterfall>
                </template>
            </div>
        </transition>
    </div>
</template>

<style scoped>

</style>