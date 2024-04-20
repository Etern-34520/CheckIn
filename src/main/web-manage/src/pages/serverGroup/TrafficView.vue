<script setup>
import {Splitpanes, Pane} from 'splitpanes'
import 'splitpanes/dist/splitpanes.css'
</script>

<template>
    <div style="display: flex;flex-direction: row;align-content: stretch;align-items: stretch">
        <splitpanes style="flex: 1">
            <pane min-size="20" size="30">
                <div class="panel"></div>
            </pane>
            <pane min-size="50">
                <div class="panel">
                    Traffic
                </div>
            </pane>
        </splitpanes>
    </div>
</template>

<style scoped>

</style>