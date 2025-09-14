import {createRouter, createWebHistory} from "vue-router"
import {ElMessageBox} from "element-plus";
import QuestionCache from "@/data/QuestionCache.js";
import {useCookies} from "vue3-cookies";

const {cookies} = useCookies();

const warning1 = {
    enabled: (from, to) => {
        return (!(from.fullPath.split("/")[2] === to.fullPath.split("/")[2])) && QuestionCache.dirty
    },
    title: "确定离开？",
    leave: "未保存的内容将暂缓于内存，刷新页面或登出后将丢失",
    confirm: "确定离开",
    cancel: "返回保存",
};
const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: "",
            name: "base",
            redirect: () => {
                if (cookies.isKey("token")) {
                    return {name: "home"};
                } else if (cookies.isKey("haveLogined")) {
                    return {name: "login"};
                } else {
                    return {name: "facade"};
                }
            }
        },
        {
            path: "/oauth2/error",
            name: "oauth2-error",
            component: () => import("@/pages/manage/OAuth2ErrorView.vue")
        },
        {
            path: "/manage/",
            name: "manage-base",
            component: () => import("@/pages/manage/ManageBase.vue"),
            children: [
                {
                    path: "",
                    name: "manage",
                    component: () => import("@/pages/manage/Manage.vue"),
                    children: [
                        {
                            path: "",
                            name: "home",
                            component: () => import("@/pages/manage/serverGroup/HomeView.vue"),
                        },
                        {
                            path: "request-record/",
                            name: "request-record",
                            component: () => import("@/pages/manage/serverGroup/requestRecord/RequestRecordView.vue"),
                            children: [
                                {
                                    path: ":id/",
                                    name: "request-record-detail",
                                    component: () => import("@/pages/manage/serverGroup/requestRecord/RequestRecordDetailView.vue"),
                                }
                            ]
                        }, {
                            path: "exam-record/",
                            name: "exam-record",
                            component: () => import("@/pages/manage/serverGroup/examRecord/ExamRecordView.vue"),
                            children: [
                                {
                                    path: ":id/",
                                    name: "exam-record-detail",
                                    component: () => import("@/pages/manage/serverGroup/examRecord/ExamRecordDetailView.vue"),
                                    children: [
                                        {
                                            path: "related-requests/",
                                            name: "related-requests",
                                            component: () => import("@/pages/manage/serverGroup/examRecord/RelatedRequestsOfExamRecordView.vue"),
                                        }
                                    ]
                                }
                            ]
                        },
                        {
                            path: "manage-user/",
                            name: "manage-user",
                            component: () => import("@/pages/manage/manageGroup/userManage/ManageUserView.vue"),
                            children: [
                                {
                                    path: ":id/",
                                    name: "user-view-base",
                                    component: () => import("@/pages/manage/manageGroup/userManage/UsersView.vue"),
                                    children: [
                                        {
                                            path: "",
                                            name: "user-detail",
                                            component: () => import("@/pages/manage/manageGroup/userManage/UserDetailView.vue"),
                                        }, {
                                            path: "user-questions/",
                                            name: "user-questions",
                                            component: () => import("@/pages/manage/manageGroup/userManage/UserOwnQuestionsPage.vue"),
                                        }, {
                                            path: "user-exam-records/",
                                            name: "user-exam-records",
                                            component: () => import("@/pages/manage/manageGroup/userManage/UserExamRecordPage.vue")
                                        }
                                    ]
                                }
                            ]
                        },
                        {
                            path: "manage-group/",
                            name: "manage-group",
                            component: () => import("@/pages/manage/manageGroup/groupManage/ManageGroupView.vue"),
                            children: [
                                {
                                    path: ":type/",
                                    name: "group-detail",
                                    component: () => import("@/pages/manage/manageGroup/groupManage/UserGroupView.vue"),
                                }
                            ]
                        },
                        {
                            path: "global-setting/",
                            name: "global-setting",
                            component: () => import("@/pages/manage/settingGroup/globalSetting/GlobalSettingViewBase.vue"),
                            children: [
                                {
                                    path: "",
                                    name: "global-setting-base",
                                    component: () => import("@/pages/manage/settingGroup/globalSetting/GlobalSettingView.vue")
                                },
                                {
                                    path: "verification-setting/",
                                    name: "verification-setting",
                                    component: () => import("@/pages/manage/settingGroup/globalSetting/VerificationSettingView.vue")
                                },
                                {
                                    path: "facade-setting/",
                                    name: "facade-setting",
                                    component: () => import("@/pages/manage/settingGroup/globalSetting/FacadeSettingView.vue")
                                },
                                {
                                    path: "advance-setting/",
                                    name: "advance-setting",
                                    component: () => import("@/pages/manage/settingGroup/globalSetting/AdvanceSettingView.vue")
                                },
                                {
                                    path: "oauth2-setting/",
                                    name: "oauth2-setting",
                                    component: () => import("@/pages/manage/settingGroup/globalSetting/OAuth2SettingView.vue")
                                },
                                {
                                    path: "generating-setting/",
                                    name: "generating-setting",
                                    component: () => import("@/pages/manage/settingGroup/globalSetting/GeneratingSettingView.vue"),
                                },
                                {
                                    path: "grading-setting/",
                                    name: "grading-setting",
                                    component: () => import("@/pages/manage/settingGroup/globalSetting/GradingSettingView.vue")
                                },
                                {
                                    path: "about/",
                                    name: "about",
                                    component: () => import("@/pages/manage/settingGroup/globalSetting/AboutView.vue")
                                }
                            ]
                        },
                        {
                            path: "account/",
                            name: "account",
                            component: () => import("@/pages/manage/settingGroup/account/AccountViewBase.vue"),
                            children: [
                                {
                                    path: "",
                                    name: "account-base",
                                    component: () => import("@/pages/manage/settingGroup/account/AccountView.vue")
                                },
                                {
                                    path: "my-data/",
                                    name: "my-data",
                                    component: () => import("@/pages/manage/settingGroup/account/MyDataTab.vue")
                                },
                                {
                                    path: "oauth2-binding",
                                    name: "oauth2-binding",
                                    component: () => import("@/pages/manage/settingGroup/account/OAuth2AccountBinding.vue")
                                }
                            ]
                        }, {
                            path: "questions/",
                            name: "questions",
                            component: () => import("@/pages/manage/serverGroup/questions/QuestionsView.vue"),
                            children: [
                                {
                                    path: ":id/",
                                    name: "question-detail",
                                    component: () => import("@/pages/manage/serverGroup/questions/editor/QuestionEditor.vue"),
                                    meta: {
                                        warning: warning1
                                    },
                                    children: [
                                        {
                                            path: 'related-exams/',
                                            name: "question-related-exams",
                                            component: () => import("@/pages/manage/serverGroup/questions/RelatedExams.vue"),
                                            meta: {
                                                warning: warning1
                                            }
                                        }
                                    ]
                                }
                            ],
                            meta: {
                                warning: warning1
                            }
                        }
                    ]
                },
                {
                    path: "/login/",
                    name: "login",
                    component: () => import("@/pages/manage/Login.vue"),
                }
            ]
        },
        {
            path: '/exam/',
            name: 'exam-base',
            component: () => import("@/pages/exam/ExamBase.vue"),
            children: [
                {
                    path: "",
                    name: "facade",
                    component: () => import("@/pages/exam/Facade.vue")
                },
                {
                    path: 'generate/',
                    name: 'generate',
                    component: () => import("@/pages/exam/Generate.vue")
                }, {
                    path: 'examine/',
                    name: 'examine',
                    component: () => import("@/pages/exam/Exam.vue")
                }, {
                    path: 'result/',
                    name: 'result',
                    component: () => import("@/pages/exam/Result.vue")
                }, {
                    path: 'sign-up/',
                    name: 'sign-up',
                    component: () => import("@/pages/exam/SignUp.vue")
                }
            ]
        }
    ]
});

router.beforeEach((to, from, next) => {
    const checkWarning = () => {
        if (from.meta.warning && from.meta.warning.enabled(from, to) && from.meta.warning.leave) {
            ElMessageBox.confirm(
                from.meta.warning.leave,
                from.meta.warning.title,
                {
                    confirmButtonText: from.meta.warning.confirm,
                    cancelButtonText: from.meta.warning.cancel,
                    type: "warning",
                    draggable: true,
                    showClose: false,
                }
            ).then(() => {
                next();
            }).catch(() => {
            });
        } else {
            next();
        }
    }

    console.log(to.matched)
    checkWarning();
});

export default router