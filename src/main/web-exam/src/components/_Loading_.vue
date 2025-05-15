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
    border: 2px solid !important;
    border-color: var(--front-color) #0000 !important;
    animation: l1 0.8s var(--ease-in-bounce) infinite;
}
@keyframes l1 {to{transform: rotate(.5turn)}}
</style>