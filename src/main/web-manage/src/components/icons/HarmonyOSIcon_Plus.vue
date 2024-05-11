<script setup>
// import { defineProps } from 'vue';

const props = defineProps({
    size: {
        type: Number,
        default: 16
    }
});
</script>
<template>
    <svg :width="size" :height="size" viewBox="0 0 24 24" style="margin: 4px;">
        <defs>
            <path
                d="M12,2 C12.5522847,2 13,2.44771525 13,3 L13,21 C13,21.5522847 12.5522847,22 12,22 C11.4477153,22 11,21.5522847 11,21 L11,13 L3,13 C2.44771525,13 2,12.5522847 2,12 C2,11.4477153 2.44771525,11 3,11 L11,11 L11,3 C11,2.44771525 11.4477153,2 12,2 Z M21,11 C21.5522847,11 22,11.4477153 22,12 C22,12.5522847 21.5522847,13 21,13 L13.999,13 L13.999,11 L21,11 Z"
                id="_path-6"/>
        </defs>
        <g id="_Public/ic_public_add_filled" stroke="none"
           stroke-width="1" fill="none"
           fill-rule="evenodd">
            <mask id="_mask-6" fill="white">
                <use xlink:href="#_path-6"/>
            </mask>
            <use id="_Combined-Shape" class="svg-text-fill"
                 fill="#000000" fill-rule="nonzero"
                 xlink:href="#_path-6"/>
        </g>
    </svg>
</template>
<style scoped>
</style>