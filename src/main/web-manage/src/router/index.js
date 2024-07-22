import {createRouter, createWebHistory} from 'vue-router'
import HomeView from "@/pages/serverGroup/HomeView.vue";
import {ElMessageBox} from "element-plus";
import QuestionCache from "@/data/QuestionCache.js";
import UserDataInterface from "@/data/UserDataInterface.js";

const warning = {
    enabled: (from, to) => {
        return (!(from.fullPath.split("/")[2] === to.fullPath.split("/")[2])) && QuestionCache.dirty
    },
    title: "确定离开？",
    leave: "离开后将丢失未保存的内容",
    confirm: '确定离开',
    cancel: '返回保存'
};
const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/manage/',
            name: 'manage',
            component: () => import('../Manage.vue'),
            children: [
                {
                    path: '',
                    name: 'home',
                    component: HomeView
                },
                {
                    path: 'traffic/',
                    name: 'traffic',
                    component: () => import('../pages/serverGroup/TrafficView.vue')
                },
                {
                    path: 'manage-user/',
                    name: 'manage-user',
                    component: () => import('../pages/manageGroup/userManage/ManageUserView.vue'),
                    children: [
                        {
                            path: ':id/',
                            name: 'user-view-base',
                            component: () => import('../pages/manageGroup/userManage/UserViewBase.vue'),
                            children: [
                                {
                                    path: '',
                                    name: 'user-detail',
                                    component: () => import('../pages/manageGroup/userManage/UserView.vue'),
                                },
                                {
                                    path: 'user-questions/',
                                    name: 'user-questions',
                                    component: () => import('../pages/manageGroup/userManage/UserOwnQuestionsPage.vue'),
                                }
                            ]
                        }
                    ]
                },
                {
                    path: 'manage-group/',
                    name: 'manage-group',
                    component: () => import('../pages/manageGroup/groupManage/ManageGroupView.vue'),
                    children: [
                        {
                            path: ':type/',
                            name: 'group-view',
                            component: () => import('../pages/manageGroup/groupManage/UserGroupView.vue'),
                        }
                    ]
                },
                {
                    path: 'global-setting/',
                    name: 'global-setting',
                    component: () => import('../pages/settingGroup/globalSetting/GlobalSettingViewBase.vue'),
                    children: [
                        {
                            path: "",
                            name: "global-setting-base",
                            component: () => import('../pages/settingGroup/globalSetting/GlobalSettingView.vue')
                        },
                        {
                            path: "verification-setting/",
                            name: "verification-setting",
                            component: () => import('../pages/settingGroup/globalSetting/VerificationSettingView.vue')
                        }
                    ]
                },
                {
                    path: 'user-setting/',
                    name: 'user-setting',
                    component: () => import('../pages/settingGroup/userSetting/UserSettingViewBase.vue'),
                    children: [
                        {
                            path: "",
                            name: "user-setting-base",
                            component: () => import('../pages/settingGroup/userSetting/UserSettingView.vue')
                        },
                        {
                            path: "my-data/",
                            name: "my-data",
                            component: () => import('../pages/settingGroup/userSetting/MyDataTab.vue')
                        }
                    ]
                }, {
                    path: 'questions/',
                    name: 'questions',
                    component: () => import('../pages/serverGroup/QuestionsView.vue'),
                    children: [
                        {
                            path: ':id/',
                            name: 'question-detail',
                            component: () => import('../components/editor/QuestionEditor.vue'),
                            meta: {
                                warning: warning
                            }
                        }
                    ],
                    meta: {
                        warning: warning
                    }
                }
            ]
        },
        {
            path: '/login/',
            name: 'login',
            component: () => import('../Login.vue')
        }
    ]
});

router.beforeEach((to, from, next) => {
    if (from.meta.warning && from.meta.warning.enabled(from, to) && from.meta.warning.leave) {
        ElMessageBox.confirm(
            from.meta.warning.leave,
            from.meta.warning.title,
            {
                confirmButtonText: from.meta.warning.confirm,
                cancelButtonText: from.meta.warning.cancel,
                type: 'warning',
            }
        ).then(() => {
            next();
        }).catch(() => {
        });
    } else {
        next();
    }
});
router.beforeEach((to, from, next) => {
    if (to.name === 'login') {
        next();
    } else {
        if (UserDataInterface.isLogined()) {
            next();
        } else {
            next({name: 'login'});
        }
    }
});

export default router