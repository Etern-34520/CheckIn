<script setup>
const props = defineProps({
  size: {
    type: Number,
    default: 16
  },
  filled: {
    type: Boolean,
    default: false
  }
});
</script>

<template>
  <svg class="icon" :width="size" :height="size" style="vertical-align: middle;overflow: hidden;" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="1212">
    <path class="svg-text-stroke" :class="{'svg-text-fill':filled}" d="M773.6 912.7h-1.2c-37.2-0.4-74.5-0.4-111.8-0.4h-56.9c-38 0-76 0-114.1-0.5-21.1-0.6-41.9-5-61.5-13-33.3-13-52.3-42-52.2-79.7l0.1-141.4c0-78.3 0-156.7 0.7-235 0.1-21.3 13.8-41.3 25.5-51.8 45.3-41.4 94.5-93 115.1-162.6 5.7-19.4 7.9-40.8 10.2-63.4 4.6-45 33.8-74.3 72.8-74.3 15.3 0 30.6 4.6 45.6 13.5 30.1 18.1 50.2 46.5 61.3 87 17.8 64.3 8.7 126.7-1.3 180.2v0.2c-2.3 12.5 7.1 24.1 19.9 24.1h130c22.4 0 54.5 2.8 74.3 26.8 14.4 17.5 18.5 41.1 12.4 72.3-18.6 95.9-41.4 192.6-63.2 282.7-6.8 28.1-18.1 54.1-29 79.3l-4.7 10.8c-12.4 29.2-38 45.2-72 45.2zM216.1 903.3h-11.9c-43 0-78.2-35.2-78.2-78.2V476.6c0-43 35.2-78.2 78.2-78.2h11.9c43 0 78.2 35.2 78.2 78.2V825c0.1 43.1-35.1 78.3-78.2 78.3z" p-id="1213"
          style="stroke-width: 50px"></path></svg>
</template>

<style scoped>
.icon {
    fill: rgba(128,128,128,0.1);
}
</style>