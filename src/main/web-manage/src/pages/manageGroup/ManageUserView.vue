<script setup>
import ResponsiveDoubleSplitpane from "@/components/common/ResponsiveDoubleSplitpane.vue";
import UserCard from "@/components/user/UserCard.vue";
import UserDataInterface from "@/data/UserDataInterface.js";
import router from "@/router/index.js";

// const user = UserDataInterface.getCurrentUser();
const users = ref([]);
const filterText = ref("");
const loading = ref(true);
const error = ref(false);

UserDataInterface.getUsersAsync().then((users1) => {
    users.value = users1;
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
</script>

<template>
    <responsive-double-splitpane :left-loading="loading" show-left-label="用户列表">
        <template #left>
            <el-input v-model="filterText" placeholder="搜索用户（以&quot;,&quot;分词）" prefix-icon="Search" style="margin-bottom: 8px"/>
            <transition-group name="filter">
                <template v-for="user of users" :key="user.qq">
                    <user-card class="clickable" :user="user" @click="openView(user)" v-if="doFilter(user)"/>
                </template>
            </transition-group>
        </template>
        <template #right>
            <router-view v-slot="{ Component }">
                <transition mode="out-in" name="question-editor">
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
    transition:
        transform 0.3s var(--ease-out-quint) 0.2s,
        max-height 0.2s var(--ease-in-out-quint);
}

/*noinspection CssUnusedSymbol*/
.filter-leave-active {
    overflow: hidden;
    transition:
        transform 0.3s var(--ease-in-quint),
        max-height 0.2s var(--ease-in-out-quint) 0.3s;
}

.filter-enter-from,.filter-leave-to {
    max-height: 0;
    transform: translateX(-100%);
}

.filter-leave-from,.filter-enter-to {
    max-height: 37px;
    transform: translateX(0);
}
</style>