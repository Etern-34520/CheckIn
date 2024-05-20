<template>
  <div class="clickable panel-1 error-question-info-panel">
    <div class="grid1">
      <div class="padding">
        <div class="panel-1 question-content">
          <el-text>
            {{ questionInfo.question.content }}
          </el-text>
        </div>
        <div class="choicesList">
          <el-tag v-for="choice of questionInfo.question.choices"
                  :type="choice.correct?'success':'danger'">
            {{ choice.content }}
          </el-tag>
        </div>
        <div>
          <el-tag
              v-for="partitionId of questionInfo.question.partitionIds"
              type="info">
            {{ PartitionTempStorage.getName(partitionId) }}
          </el-tag>
        </div>
        <div class="errorsDescription">
          <transition-group name="errorDescriptions">
            <el-text v-for="error of questionInfo.errors"
                     type="danger"
                     :key="error.content">
              {{ error.content }}
            </el-text>
          </transition-group>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
import PartitionTempStorage from "../data/PartitionTempStorage.js";

defineProps({
  questionInfo: Object
})
</script>
<style scoped>

.dragHover * {
  color: var(--front-color-dark) !important;
}

.error-question-info-panel {
  display: grid;
  margin-top: 2px;
  padding: 0;
  min-height: 0;
  grid-template-rows: 1fr;
}

.padding {
  padding: 8px;
}

.error-question-info-panel > .grid1 > .padding > div {
  min-height: 25px;
}

.error-question-info-panel > .grid1 {
  min-height: 0;
}

.errorsDescription > * {
  margin-right: 8px;
}

.choicesList > * {
  min-width: 40px;
  margin-right: 4px;
}

.question-content {
  padding: 4px 16px;
}

.errorDescriptions-enter-active, .errorDescriptions-leave-active {
  transition: 0.2s var(--ease-in-out-quint);
}

.errorDescriptions-enter-from, .errorDescriptions-leave-to {
  opacity: 0;
  scale: 0.9;
  filter: blur(8px);
}
</style>