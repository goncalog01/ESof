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
import { Component, Prop, Vue, Watch } from 'vue-property-decorator';
import Question from '@/models/management/Question';
import DifficultQuestion from '@/models/dashboard/DifficultQuestion';
import RemoteServices from '@/services/RemoteServices';

@Component({
  components: {},
})
export default class DifficultQuestionsView extends Vue {
  difficultQuestions: DifficultQuestion[] = [];

  @Prop({ type: Number, required: true })
  readonly dashboardId!: number;

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

  async created() {
    await this.$store.dispatch('loading');
    try {
      this.difficultQuestions = await RemoteServices.getDifficultQuestions(
        this.dashboardId
      );
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }
}
</script>

<style scoped></style>
