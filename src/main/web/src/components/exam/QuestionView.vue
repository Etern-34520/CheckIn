<script setup>
import {MdPreview} from "md-editor-v3";
import MultipleChoicesViewModule from "@/components/exam/MultipleChoicesViewModule.vue";
import QuestionGroupSubQuestionViewModule from "@/components/exam/QuestionGroupSubQuestionViewModule.vue";
import ImagesViewer from "@/components/viewer/ImagesViewer.vue";
import UIMeta from "@/utils/UI_Meta.js";
import {Picture} from "@element-plus/icons-vue";
import customSanitizeHtml from "@/utils/sanitize.js";

const props = defineProps({
    question: {
        type: Object,
        required: true,
    },
    forceMobile: {
        type: Boolean,
        default: UIMeta.mobile.value
    }
});

const preview = ref();

const imagesScrollbar = ref();
const scrollSyncLeft = ref(0);
const imageViewerVisible = ref(false);
const viewerIndex = ref(0);
const images = computed(() => Object.values(props.question.images));
const mobile = ref(UIMeta.mobile);
if (props.forceMobile) mobile.value = true;

const onScroll = ({scrollTop, scrollLeft}) => {
    scrollSyncLeft.value = scrollLeft;
}

let delta = 0;
const transformScroll = (e) => {
    if (delta === 0)
        setTimeout(() => {
            imagesScrollbar.value.setScrollLeft(delta + scrollSyncLeft.value);
            delta = 0;
        }, 150);
    if (props.forceMobile && !e.shiftKey) {
        delta += e.deltaY * 2;
        e.preventDefault();
    }
}

const onPreview = (index) => {
    viewerIndex.value = index;
    imageViewerVisible.value = true;
}

const model = defineModel({
    type: Object
});

const sanitize = (html) => {
    if (props.question.unsafeXss) {
        return html;
    } else {
        return customSanitizeHtml(html);
    }
}
</script>

<template>
    <div :class="{mobile:mobile,desktop:!mobile}" class="question-view-base"
         ref="preview">
        <images-viewer :images="Object.values(question.images)" v-model="imageViewerVisible" v-model:index="viewerIndex"
                       v-if="question && question.images"/>
        <el-scrollbar ref="imagesScrollbar" class="images-scrollbar"
                      v-if="question && question.images" @mousewheel="transformScroll"
                      @scroll="onScroll">
            <div class="images">
                <el-image class="image" v-for="(imageBase64,name,index) in question.images" @click="onPreview(index)" :src="imageBase64">
                    <template #error>
                        <div class="image-slot">
                            <el-icon>
                                <Picture/>
                            </el-icon>
                        </div>
                    </template>
                </el-image>
            </div>
        </el-scrollbar>
        <div class="content" style="flex:1;display: flex;flex-direction: column">
            <md-preview preview-theme="vuepress" :theme="UIMeta.colorScheme.value" :show-toolbar-name="UIMeta.touch.value"
                        :model-value="question.content" :sanitize="sanitize"
                       class="preview-only" style="flex:1;overflow: visible;"/>
            <multiple-choices-view-module style="padding: 32px"
                                             v-if="question.type==='MultipleChoicesQuestion'"
                                             :question="question" v-model="model"/>
            <question-group-sub-question-view-module v-else-if="question.type==='QuestionGroup'"
                                                        :question="question" v-model="model"/>
        </div>
    </div>
</template>

<style scoped>
.question-view-base {
    display: flex;
    box-sizing: border-box;
    padding-bottom: 16px;

    > .images-scrollbar .image {
        border-radius: 8px;
    }
}

.question-view-base.desktop {
    flex-direction: row;

    > .images-scrollbar {
        order: 1;
        flex: 0.4;
        margin-right: 16px;
        margin-left: 16px;
    }

    > .images-scrollbar .image {
        padding: 32px 0;
        width: 100%;
    }
}

.question-view-base.mobile {
    flex-direction: column;

    > .images-scrollbar {
        order: 0 !important;
        height: 200px !important;
        /*        width: 100% !important;*/
        flex: none;
        margin: 16px 32px 0;
    }

    > .images-scrollbar .image {
        min-height: 200px;
        height: 200px;
        min-width: fit-content;
    }
}

.question-view-base:not(.mobile) {
    .content {
        padding: 32px 24px;
    }
}


.question-view-base > .images-scrollbar .images {
    display: flex;
}

.question-view-base.desktop > .images-scrollbar .images {
    padding: 0 10px;
    flex-direction: column;
}

.question-view-base.mobile > .images-scrollbar .images {
    padding: 0;
    flex-direction: row;
}

.question-view-base.desktop > .images-scrollbar .images > *:not(:last-child) {
    margin: 0 0 12px;
}

.question-view-base.mobile > .images-scrollbar .images > *:not(:last-child) {
    margin: 0 12px 0 0;
}

.image {
    cursor: pointer;
    transition: 0.2s var(--ease-in-out-quint);
}

.image:hover {
    opacity: 0.5;
}
</style>

<style>
.images-scrollbar > .el-scrollbar__wrap {
    scroll-behavior: smooth;
}
</style>