<script setup>
import Collapse from "@/components/common/Collapse.vue";
import Waterfall from "@/components/common/Waterfall.vue";
import icon from "@/assets/icons/icon-accent-light.svg";

const data = [{
    type: "前端",
    projects: [
        "Vue.js",
        "Vite",
        "Vite-Plugin-PWA",
        "Vue Router",
        "Vue Draggable",
        "Vue3 Cookies",
        "Element Plus",
        "MdEditorV3",
        "Axios",
        "Splitpanes",
        "JWT Decode",
        "...",
    ]
}, {
    type: "后端",
    projects: [
        "Spring Boot & Spring series",
        "Hibernate",
        "Tomcat",
        "Jackson",
        "Caffeine",
        "Flyway",
        "Jakarta",
        "Json Web Token",
        "MYSQL (jdbc)",
        "H2 Database (jdbc)",
        "...",
    ]
}
]

const getIcon = () => {
    return icon;
}
</script>

<template>
    <el-scrollbar>
        <div style="display: flex;flex-direction: column;align-items: center;">
            <div style="max-width: 1080px; width: min(95%, 1080px); display: flex; flex-direction: column;">
                <el-text style="font-size: 24px;align-self: baseline;margin-bottom: 16px">关于</el-text>
                <div class="panel-1" style="display: flex;flex-direction: row;margin-bottom: 8px;padding: 24px">
                    <img :src="getIcon()" alt="" width="56" height="56" style="margin-right: 16px;"/>
                    <div style="display: flex;flex-direction: column;">
                        <el-text size="large" style="align-self: baseline">CheckIn</el-text>
                        <el-text type="info" style="align-self: baseline">2.1.2 beta 1</el-text>
                        <el-link type="info" style="align-self: baseline"
                                 href="https://github.com/Etern-34520/CheckIn" target="_blank">Github
                        </el-link>
                    </div>
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
                                    <el-text style="align-self: baseline;margin: 8px" size="large">{{ datum.type }}
                                    </el-text>
                                    <el-text type="info" v-for="project in datum.projects"
                                             style="align-self: baseline;">{{ project }}
                                    </el-text>
                                </div>
                            </template>
                        </waterfall>
                    </template>
                </collapse>
            </div>
        </div>
    </el-scrollbar>
</template>

<style scoped>

</style>