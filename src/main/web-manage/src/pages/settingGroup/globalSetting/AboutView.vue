<script setup>
import Collapse from "@/components/common/Collapse.vue";
import Waterfall from "@/components/common/Waterfall.vue";

const data = [{
        type: "前端",
        projects: [
            "Vue.js",
            "Vite",
            "Vue Router",
            "Vue Draggable",
            "Vue3 Cookies",
            "Element Plus",
            "MdEditorV3",
            "Axios",
            "Splitpanes"
        ]
    }, {
        type: "后端",
        projects: [
            "Spring Boot",
            "Tomcat",
            "Commons IO",
            "Jackson",
            "Jakarta",
            "Json Web Token",
            "MYSQL",
        ]
    }
]
</script>

<template>
    <div style="display: flex;flex-direction: column;align-items: center;">
        <div style="max-width: 1280px; width: min(75vw, 1280px); display: flex; flex-direction: column;">
            <el-text style="font-size: 24px;align-self: baseline;margin-bottom: 16px">关于</el-text>
            <div style="display: flex;flex-direction: row;margin-bottom: 8px;">
                <el-text size="large" style="margin-right: 32px">CheckIn</el-text>
                <el-link type="info" href="https://github.com/Etern-34520/CheckIn" target="_blank">Github</el-link>
            </div>
            <collapse>
                <template #title>
                    <div style="display: flex;flex-direction: row;align-items: center;height: 100%;margin-left: 8px">
                        <el-text>使用到的开源项目</el-text>
                    </div>
                </template>
                <template #content>
                    <waterfall :data="data">
                        <template #item="{item:datum,index}">
                            <div style="display: flex;flex-direction: column;align-items: baseline;padding: 16px 16px 32px;">
                                <el-text style="align-self: baseline;margin: 8px" size="large">{{datum.type}}</el-text>
                                <el-text type="info" v-for="project in datum.projects" style="align-self: baseline;">{{project}}</el-text>
                            </div>
                        </template>
                    </waterfall>
                </template>
            </collapse>
        </div>
    </div>
</template>

<style scoped>

</style>