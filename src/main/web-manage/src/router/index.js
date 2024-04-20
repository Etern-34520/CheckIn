import {createRouter, createWebHistory} from 'vue-router'
import HomeView from "@/pages/serverGroup/HomeView.vue";

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
                            component: () => import('../components/QuestionEditor.vue')
                        }
                    ]
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

export default router