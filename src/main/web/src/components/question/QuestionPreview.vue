<script setup>
import MultipleChoicesPreviewModule
    from "@/pages/manage/serverGroup/questions/editor/module/MultipleChoicesPreviewModule.vue";
import QuestionGroupSubQuestionPreviewModule
    from "@/pages/manage/serverGroup/questions/editor/module/QuestionGroupSubQuestionPreviewModule.vue";
import {MdPreview} from "md-editor-v3";
import UIMeta from "@/utils/UI_Meta.js";
import ImagesViewer from "@/components/viewer/ImagesViewer.vue";
import router from "@/router/index.js";
import getAvatarUrlOf from "@/utils/Avatar.js";
import {Picture} from "@element-plus/icons-vue";

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
    if (props.forceMobile && !e.shiftKey) {
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
    <div :class="{mobile:forceMobile,desktop:!forceMobile}" class="question-view-base"
         ref="preview">
        <images-viewer :images="questionInfo.question.images" v-model="imageViewerVisible" v-model:index="viewerIndex"
                       v-if="questionInfo.question && questionInfo.question.images && questionInfo.question.images.length>0"/>
        <el-scrollbar ref="imagesScrollbar" class="images-scrollbar"
                      v-if="questionInfo.question && questionInfo.question.images && questionInfo.question.images.length>0"
                      @mousewheel="transformScroll"
                      @scroll="onScroll">
            <div ref="images" class="images">
                <el-image class="image" v-for="image of questionInfo.question.images" @click="onPreview(image)"
                          :src="image.url">
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
        <div class="content" style="flex:1;margin-top: 16px">
            <div style="display: flex;flex-direction: row;flex: 1;margin-left: 32px">
                <div class="point"
                     :style="questionInfo.question.enabled?'background: var(--el-color-primary);':'background: var(--el-color-info);'"></div>
                <el-text size="small" style="align-self: center;margin-right: 24px;" type="info">
                    {{ questionInfo.question.enabled ? "已启用" : "未启用" }}
                </el-text>

                <template v-if="questionInfo.question.authorQQ">
                    <el-text type="info" size="small">作者</el-text>
                    <el-button
                            @click.stop="router.push({name:'user-detail', params: {id: questionInfo.question.authorQQ}})"
                            link class="disable-init-animate"
                            style="margin-right: 6px;padding: 4px;transition: 200ms var(--ease-in-out-quint)">
                        <el-avatar shape="circle" :size="20" :src="getAvatarUrlOf(questionInfo.question.authorQQ)"
                                   style="margin-right: 4px;"></el-avatar>
                        <el-text style="margin-left: 4px;">{{ questionInfo.question.authorQQ }}</el-text>
                    </el-button>
                </template>
            </div>
            <md-preview preview-theme="vuepress" :theme="UIMeta.colorScheme.value"
                       :show-toolbar-name="UIMeta.touch.value" :model-value="questionInfo.question.content"
                       class="preview-only" style="padding: 16px 32px"/>
            <multiple-choices-preview-module style="padding: 32px"
                                             v-if="questionInfo.question.type==='MultipleChoicesQuestion'"
                                             :question-info="questionInfo"/>
            <question-group-sub-question-preview-module v-else-if="questionInfo.question.type==='QuestionGroup'"
                                                        :question-info="questionInfo"/>
        </div>
    </div>
</template>

<style scoped>
.question-view-base {
    display: flex;
    box-sizing: border-box;
    width: 100%;
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

.point {
    width: 6px;
    height: 6px;
    align-self: center;
    border-radius: 3px;
    margin: 8px;
}
</style>

<style>
.images-scrollbar > .el-scrollbar__wrap {
    scroll-behavior: smooth;
}
</style>