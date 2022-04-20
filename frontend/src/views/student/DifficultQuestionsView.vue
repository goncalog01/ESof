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
        :sort-by="['difficulty']"
        sort-desc
        :mobile-breakpoint="0"
        :items-per-page="10"
      >
        <template v-slot:top>
          <v-card-title>
            <v-spacer />
            <v-btn color="primary" dark @click="refreshDifficultQuestions"
              >Refresh</v-btn
            >
          </v-card-title>
        </template>

        <template v-slot:[`item.action`]="{ item }">
          <v-tooltip bottom>
            <template v-slot:activator="{ on }">
              <v-icon
                  class="mr-2 action-button"
                  v-on="on"
                  @click="showStudentViewDialog(item)"
              >school</v-icon
              >
            </template>
            <span>Show Question</span>
          </v-tooltip>
        </template>

      </v-data-table>
    </v-card>
    <student-view-dialog
        v-if="statementQuestion && studentViewDialog"
        v-model="studentViewDialog"
        :statementQuestion="statementQuestion"
        v-on:close-show-question-dialog="onCloseStudentViewDialog"
    />
  </v-container>
</template>



<script lang="ts">
import { Component, Prop, Vue, Watch } from 'vue-property-decorator';
import Question from '@/models/management/Question';
import DifficultQuestion from '@/models/dashboard/DifficultQuestion';
import RemoteServices from '@/services/RemoteServices';
import StatementQuestion from '@/models/statement/StatementQuestion';
import StudentViewDialog from '@/views/teacher/questions/StudentViewDialog.vue';

@Component({
  components: {
    'student-view-dialog': StudentViewDialog
  },
})
export default class DifficultQuestionsView extends Vue {
  difficultQuestions: DifficultQuestion[] = [];
  statementQuestion: StatementQuestion | null = null;
  studentViewDialog: boolean = false;

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
      value: 'questionDto.title',
      width: '80%',
      align: 'left',
      sortable: false,
    },
    {
      text: 'Percentage',
      value: 'percentage',
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

  async refreshDifficultQuestions() {
    await this.$store.dispatch('loading');
    try {
      await RemoteServices.updateDifficultQuestions(this.dashboardId);
      this.difficultQuestions = await RemoteServices.getDifficultQuestions(
        this.dashboardId
      );
      this.$emit('refresh');
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  async showStudentViewDialog(difficultQuestion: DifficultQuestion) {
    if (difficultQuestion.questionDto.id) {
      try {
        this.statementQuestion = await RemoteServices.getStatementQuestion(
            difficultQuestion.questionDto.id
        );
        this.studentViewDialog = true;
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    }
  }

  onCloseStudentViewDialog() {
    this.statementQuestion = null;
    this.studentViewDialog = false;
  }
}
</script>

<style scoped></style>
