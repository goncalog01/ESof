<template>
  <failed-answers-table></failed-answers-table>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import QuestionsView from '../teacher/questions/QuestionsView.vue';

@Component({
  components: {
    'failed-answers-table': QuestionsView,
  },
})
export default class FailedAnswersView extends Vue {
  dashboardId: number | null = null;
}
</script>

<style scoped>

</style>