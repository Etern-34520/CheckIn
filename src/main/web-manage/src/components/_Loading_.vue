<script setup>
</script>

<template>
    <div class="loader"></div>
</template>

<style scoped>

/* HTML: <div class="loader"></div> */
.loader {
    width: 16px !important;
    aspect-ratio: 1;
    border-radius: 50%;
    border: 2px solid;
    border-color: var(--front-color) #0000;
    animation: l1 1s var(--ease-in-bounce-1) infinite;
}
@keyframes l1 {to{transform: rotate(.5turn)}}
</style>