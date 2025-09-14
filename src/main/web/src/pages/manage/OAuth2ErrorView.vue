<script setup>
const {proxy} = getCurrentInstance();
const errorMessage = proxy.$cookies.get("OAuth2ErrorMessage", "/checkIn");
const showError = ref(false);
const inOAuth2Binding = proxy.$cookies.get("inOAuth2Binding", "/checkIn");
const OAuth2FallbackRouteName = proxy.$cookies.get("OAuth2FallbackRouteName", "/checkIn");
const inOAuth2Login = proxy.$cookies.get("inOAuth2Login", "/checkIn");
if (inOAuth2Binding === "true") {
    proxy.$cookies.remove("inOAuth2Binding", "/checkIn");
    proxy.$router.push({name: "oauth2-binding"});
} else if (inOAuth2Login === "true"){
    proxy.$router.push({name: OAuth2FallbackRouteName});
} else {
    showError.value = true;
}
</script>

<template>
    <div v-if="showError" style="display: flex;flex-direction: column;justify-content: center;height: 100%">
        <el-text style="margin-bottom: 16px" size="large">OAuth2 登录失败</el-text>
        <el-text type="danger" style="margin-bottom: 32px">{{ errorMessage }}</el-text>
        <el-button type="primary" style="width: 160px;align-self: center" @click="$router.push({name: OAuth2FallbackRouteName})">
            返回
        </el-button>
    </div>
</template>

<style scoped>

</style>