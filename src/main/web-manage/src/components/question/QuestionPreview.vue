<script setup>
import MultipleChoicesPreviewModule from "@/components/editor/module/MultipleChoicesPreviewModule.vue";
import QuestionGroupSubQuestionPreviewModule
    from "@/components/editor/module/QuestionGroupSubQuestionPreviewModule.vue";
import {MdCatalog, MdEditor} from "md-editor-v3";
import UIMeta from "@/utils/UI_Meta.js";
import ImagesViewer from "@/components/editor/ImagesViewer.vue";
// import 'md-editor-v3/lib/preview.css';

const props = defineProps({
    questionInfo: {
        type: Object,
        required: true
    },
    forceMobile: {
        type: Boolean,
        default: false
    }
});

const preview = ref();

const imagesScrollbar = ref();
const images = ref();
const scrollSyncLeft = ref(0);
const imageViewerVisible = ref(false);
const viewerIndex = ref(0);

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
    if (props.forceMobile && !e.shiftKey) {//TODO
        delta += e.deltaY * 2;
        e.preventDefault();
    }
}

const onPreview = (file) => {
    viewerIndex.value = props.questionInfo.question.images.findIndex(item => item.uid === file.uid);
    imageViewerVisible.value = true;
}
</script>

<template>
    <div :class="{mobile:forceMobile,desktop:!forceMobile}" class="preview-base"
         ref="preview">
        <images-viewer :images="questionInfo.question.images" v-model="imageViewerVisible" v-model:index="viewerIndex"
                       v-if="questionInfo.question && questionInfo.question.images && questionInfo.question.images.length>0"/>
        <el-scrollbar ref="imagesScrollbar" class="images-scrollbar"
                      v-if="questionInfo.question && questionInfo.question.images && questionInfo.question.images.length>0" @mousewheel="transformScroll"
                      @scroll="onScroll">
            <div ref="images" class="images">
                <el-image class="image" v-for="image of questionInfo.question.images" @click="onPreview(image)" :src="image.url">
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
        <div class="content" style="flex:1">
            <md-editor preview-theme="vuepress" :theme="UIMeta.colorScheme.value" :model-value="questionInfo.question.content" class="preview-only"/>
            <!--            <v-md-preview :text="questionInfo.question.content"/>-->
            <multiple-choices-preview-module style="padding: 32px"
                                             v-if="questionInfo.question.type==='MultipleChoicesQuestion'"
                                             :question-info="questionInfo"/>
            <question-group-sub-question-preview-module v-else-if="questionInfo.question.type==='QuestionGroup'"
                                                        :question-info="questionInfo"/>
        </div>
    </div>
</template>

<style scoped>
.preview-base {
    display: flex;
    box-sizing: border-box;
    width: 100%;
    padding-bottom: 16px;

    > .images-scrollbar .image {
        border-radius: 8px;
    }
}

.preview-base.desktop {
    flex-direction: row;

    > .images-scrollbar {
        order: 1;
        flex: 0.4;
    }

    > .images-scrollbar .image {
        width: 100%;
    }
}

.preview-base.mobile {
    flex-direction: column;

    > .images-scrollbar {
        order: 0 !important;
        height: 200px !important;
        /*        width: 100% !important;*/
        flex: none;
        margin: 0 32px;
    }

    > .images-scrollbar .image {
        min-height: 200px;
        height: 200px;
        min-width: fit-content;
    }
}


.preview-base > .images-scrollbar .images {
    display: flex;
}

.preview-base.desktop > .images-scrollbar .images {
    padding: 0 10px;
    flex-direction: column;
}

.preview-base.mobile > .images-scrollbar .images {
    padding: 0;
    flex-direction: row;
}

.preview-base.desktop > .images-scrollbar .images > *:not(:last-child) {
    margin: 0 0 12px;
}

.preview-base.mobile > .images-scrollbar .images > *:not(:last-child) {
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