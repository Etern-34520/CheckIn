<script setup>
import {MdEditor} from "md-editor-v3";
import MultipleChoicesViewModule from "@/components/MultipleChoicesViewModule.vue";
import QuestionGroupSubQuestionViewModule from "@/components/QuestionGroupSubQuestionViewModule.vue";
import ImagesViewer from "@/components/ImagesViewer.vue";
import UIMeta from "@/UI_Meta.js";

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
const images = computed(() => Object.values(props.question.imageBase64Strings));
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

</script>

<template>
    <div :class="{mobile:mobile,desktop:!mobile}" class="question-view-base"
         ref="preview">
        <images-viewer :images="images" v-model="imageViewerVisible" v-model:index="viewerIndex"
                       v-if="question && question.imageBase64Strings"/>
        <el-scrollbar ref="imagesScrollbar" class="images-scrollbar"
                      v-if="question && question.imageBase64Strings" @mousewheel="transformScroll"
                      @scroll="onScroll">
            <div class="images">
                <el-image class="image" v-for="(imageBase64,name,index) in question.imageBase64Strings" @click="onPreview(index)" :src="imageBase64">
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
            <md-editor preview-theme="vuepress" :theme="UIMeta.colorScheme.value" :model-value="question.content"
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
        padding: 32px 16px;
        margin-right: 16px;
    }

    > .images-scrollbar .image {
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
        margin: 0 32px;
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