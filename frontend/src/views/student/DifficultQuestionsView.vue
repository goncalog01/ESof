<template>
  <difficult-questions-table></difficult-questions-table>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import QuestionsView from '@/views/teacher/questions/QuestionsView.vue';

@Component({
  components: {
    'difficult-questions-table': QuestionsView,
  },
})
export default class DifficultQuestionsView extends Vue {
  dashboardId: number | null = null;
}
</script>

<style scoped></style>
