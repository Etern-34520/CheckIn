import {createRouter, createWebHistory} from 'vue-router'
import Base from "@/views/Base.vue";

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/exam/',
            name: 'base',
            component: Base,
            children: [
                {
                    path: "",
                    name: "facade",
                    component: () => import("@/views/Facade.vue")
                },
                {
                    path: 'generate/',
                    name: 'generate',
                    component: () => import("@/views/Generate.vue")
                }, {
                    path: 'examine/',
                    name: 'examine',
                    component: () => import("@/views/Exam.vue")
                }, {
                    path: 'result/',
                    name: 'result',
                    component: () => import("@/views/Result.vue")
                }, {
                    path: 'sign-up/',
                    name: 'sign-up',
                    component: () => import("@/views/SignUp.vue")
                }
            ]
        }
    ]
})

export default router;