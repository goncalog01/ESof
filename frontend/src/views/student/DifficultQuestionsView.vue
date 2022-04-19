<template>
  <v-container fluid>
    <v-card class="table">
      <v-row>
        <v-col>
          <h1>Difficult Questions</h1>
        </v-col>
      </v-row>

      <v-data-table
        :headers="headers"
        :items="difficultQuestions"
        :sort-by="['percentage']"
        sort-desc
        :mobile-breakpoint="0"
        :items-per-page="10"
      >
      </v-data-table>
    </v-card>
  </v-container>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'vue-property-decorator';
import Question from '@/models/management/Question';
import DifficultQuestion from '@/models/dashboard/DifficultQuestion';

@Component({
  components: {},
})
export default class DifficultQuestionsView extends Vue {
  dashboardId: number | null = null;
  difficultQuestions: DifficultQuestion[] = [];

  headers: object = [
    {
      text: 'Actions',
      value: 'action',
      align: 'left',
      width: '5px',
      sortable: false,
    },
    {
      text: 'Question',
      value: 'questionAnswerDto.question.title',
      width: '80%',
      align: 'left',
      sortable: false,
    },
    {
      text: 'Percentage',
      value: 'collected',
      width: '10%',
      align: 'center',
    },
  ];
}
</script>

<style scoped></style>
