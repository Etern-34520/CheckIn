<script setup>

import UserQuestionsView from "@/components/user/UserQuestionsView.vue";
import UserDataInterface from "@/data/UserDataInterface.js";
</script>

<template>
    <user-questions-view :qq="UserDataInterface.getCurrentUser().qq"/>
</template>

<style scoped>

</style>