import {createRouter, createWebHistory} from 'vue-router'
import HomeView from "@/pages/serverGroup/HomeView.vue";
import {ElMessageBox} from "element-plus";
import QuestionTempStorage from "@/data/QuestionTempStorage.js";

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
                    component: () => import('../pages/manageGroup/ManageUserView.vue')
                },
                {
                    path: 'manage-group/',
                    name: 'manage-group',
                    component: () => import('../pages/manageGroup/ManageGroupView.vue')
                },
                {
                    path: 'global-setting/',
                    name: 'global-setting',
                    component: () => import('../pages/settingGroup/GlobalSettingView.vue')
                },
                {
                    path: 'user-setting/',
                    name: 'user-setting',
                    component: () => import('../pages/settingGroup/UserSettingView.vue')
                },
                {
                    path: 'questions/',
                    name: 'questions',
                    component: () => import('../pages/serverGroup/QuestionsView.vue'),
                    children: [
                        {
                            path: ':id/',
                            name: 'question-detail',
                            component: () => import('../components/QuestionEditor.vue'),
                            meta: {
                                warning: {
                                    enabled: (from, to) => {
                                        return (!(from.fullPath.split("/")[2] === to.fullPath.split("/")[2])) && QuestionTempStorage.dirty
                                    },
                                    leave: "离开后将丢失未保存的内容"
                                }
                            }
                        }
                    ],
                    meta: {
                        warning: {
                            enabled: (from, to) => {
                                return (!(from.fullPath.split("/")[2] === to.fullPath.split("/")[2])) && QuestionTempStorage.dirty
                            },
                            leave: "离开后将丢失未保存的内容"
                        }
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
            '警告',
            {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning',
            }
        ).then(() => {
            next();
        })
            .catch(() => {
            })
    } else {
        next();
    }
});

export default router