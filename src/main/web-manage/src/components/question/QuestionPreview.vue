<script setup>
import MultipleChoicesPreviewModule from "@/components/editor/module/MultipleChoicesPreviewModule.vue";
import QuestionGroupSubQuestionPreviewModule
    from "@/components/editor/module/QuestionGroupSubQuestionPreviewModule.vue";

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
</script>

<template>
    <div :class="{mobile:forceMobile,desktop:!forceMobile}" class="preview-base"
         ref="preview">
        <el-scrollbar ref="imagesScrollbar" class="images-scrollbar"
                      v-if="questionInfo.question && questionInfo.question.images && questionInfo.question.images.length>0" @mousewheel="transformScroll"
                      @scroll="onScroll">
            <div ref="images" class="images">
                <el-image class="image" v-for="image of questionInfo.question.images" :src="image.url">
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
            <v-md-preview :text="questionInfo.question.content"/>
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
</style>

<style>
    .images-scrollbar > .el-scrollbar__wrap {
        scroll-behavior: smooth;
    }
</style>