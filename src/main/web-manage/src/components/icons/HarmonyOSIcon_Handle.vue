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
    <svg :width="size" :height="size" viewBox="0 0 24 24" >
        <defs>
            <path
                d="M2.75,9.75 C2.33578644,9.75 2,9.41421356 2,9 C2,8.58578644 2.33578644,8.25 2.75,8.25 L21.25,8.25 C21.6642136,8.25 22,8.58578644 22,9 C22,9.41421356 21.6642136,9.75 21.25,9.75 L2.75,9.75 Z M21.25,14.25 C21.6642136,14.25 22,14.5857864 22,15 C22,15.4142136 21.6642136,15.75 21.25,15.75 L2.75,15.75 C2.33578644,15.75 2,15.4142136 2,15 C2,14.5857864 2.33578644,14.25 2.75,14.25 L21.25,14.25 Z"
                id="_path-4"/>
        </defs>
        <g id="_Public/ic_public_drag_handle" stroke="none" stroke-width="1" fill="none"
           fill-rule="evenodd">
            <use id="_move" class="svg-text-fill" fill="#000000" fill-rule="nonzero"
                 xlink:href="#_path-4"/>
        </g>
    </svg>
</template>
<style scoped>
</style>