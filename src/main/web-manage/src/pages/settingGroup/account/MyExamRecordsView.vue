<script setup>
import UserDataInterface from "@/data/UserDataInterface.js";
import UserExamRecordsView from "@/components/user/UserExamRecordsView.vue";
</script>

<template>
    <user-exam-records-view :qq="UserDataInterface.getCurrentUser().value.qq"/>
</template>

<style scoped>

</style>