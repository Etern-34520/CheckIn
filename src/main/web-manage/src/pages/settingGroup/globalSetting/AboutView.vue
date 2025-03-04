<script setup>
import Collapse from "@/components/common/Collapse.vue";
import Waterfall from "@/components/common/Waterfall.vue";
import iconWhite from "@/assets/icons/icon-white.svg";
import iconBlack from "@/assets/icons/icon-black.svg";
import UI_Meta from "@/utils/UI_Meta.js";

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
            "Splitpanes",
            "JWT Decode",
            "...",
        ]
    }, {
        type: "后端",
        projects: [
            "Spring Boot",
            "Hibernate",
            "Tomcat",
            "Jackson",
            "Jakarta",
            "Json Web Token",
            "MYSQL (jdbc)",
            "H2 Database (jdbc)",
            "DB2 (jdbc)",
            "...",
        ]
    }
]

const colorScheme = UI_Meta.colorScheme;
const getIcon = () => {
    if (colorScheme.value === 'light') {
        return iconBlack;
    } else {
        return iconWhite;
    }
}
</script>

<template>
    <div style="display: flex;flex-direction: column;align-items: center;">
        <div style="max-width: 1280px; width: min(75vw, 1280px); display: flex; flex-direction: column;">
            <el-text style="font-size: 24px;align-self: baseline;margin-bottom: 16px">关于</el-text>
            <div class="panel-1" style="display: flex;flex-direction: row;margin-bottom: 8px;padding: 24px">
                <img :src="getIcon()" alt="" width="48" height="48" style="margin-right: 16px;"/>
                <div style="display: flex;flex-direction: column;">
                    <el-text size="large" style="align-self: baseline">CheckIn</el-text>
                    <el-text type="info" style="align-self: baseline">2.0.0 beta 2</el-text>
                    <el-link type="info" style="align-self: baseline"
                             href="https://github.com/Etern-34520/CheckIn" target="_blank">Github</el-link>
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