<template>
  <v-container fluid>
    <v-card class="table">
      <v-row>
        <v-col>
          <h1>Failed Answers</h1>
        </v-col>
      </v-row>

      <v-col class="text-right">
        <v-btn color="primary" dark @click="refreshFailedAnswers">
          Refresh</v-btn
        >
      </v-col>

      <v-data-table
        :headers="headers"
        :items="failedAnswers"
        :sort-by="['collected']"
        sort-desc
        :mobile-breakpoint="0"
        :items-per-page="10"
      >
        <template v-slot:[`item.answered`]="{ item }">
          <span>
            {{ item.answered ? 'Yes' : 'No' }}
          </span>
        </template>

        <template v-slot:[`item.collected`]="{ item }">
          <span>
            {{ convertDate(item.collected) }}
          </span>
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
            <span>Student View</span>
          </v-tooltip>

          <v-tooltip bottom>
            <template v-slot:activator="{ on }">
              <v-icon
                class="mr-2 action-button"
                v-on="on"
                data-cy="deleteQuestionButton"
                @click="deleteFailedAnswer(item)"
                color="red"
                >delete</v-icon
              >
            </template>
            <span>Delete Question</span>
          </v-tooltip>
        </template>
      </v-data-table>

      <student-view-dialog
        v-if="statementQuestion && studentViewDialog"
        v-model="studentViewDialog"
        :statementQuestion="statementQuestion"
        v-on:close-show-question-dialog="onCloseStudentViewDialog"
      />
    </v-card>
  </v-container>
</template>

<script lang="ts">
import { Component, Prop, Vue, Emit } from 'vue-property-decorator';
import Question from '@/models/management/Question';
import StatementQuestion from '@/models/statement/StatementQuestion';
import RemoteServices from '@/services/RemoteServices';
import FailedAnswer from '@/models/dashboard/FailedAnswer';
import StudentViewDialog from '@/views/teacher/questions/StudentViewDialog.vue';

@Component({
  components: { 'student-view-dialog': StudentViewDialog },
})
export default class FailedAnswersView extends Vue {
  failedAnswers: FailedAnswer[] = [];
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
      value: 'questionAnswerDto.question.title',
      width: '80%',
      align: 'left',
      sortable: false,
    },
    {
      text: 'Answered',
      value: 'answered',
      width: '10%',
      align: 'right',
      sortable: false,
    },
    {
      text: 'Collected',
      value: 'collected',
      width: '10%',
      align: 'center',
    },
  ];

  async created() {
    await this.$store.dispatch('loading');
    try {
      this.failedAnswers = await RemoteServices.getFailedAnswers(
        this.dashboardId
      );
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  convertDate(date: string) {
    const y = date.replaceAll('T', ' ');
    const i = y.indexOf(':', date.indexOf(':') + 1);
    return y.substring(0, i);
  }

  async showStudentViewDialog(failedAnswer: FailedAnswer) {
    if (failedAnswer.id && failedAnswer.questionAnswerDto.question.id) {
      try {
        this.statementQuestion = await RemoteServices.getStatementQuestion(
          failedAnswer.questionAnswerDto.question.id
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

  async deleteFailedAnswer(toRemoveFailedAnswer: FailedAnswer) {
    if (
      toRemoveFailedAnswer.id &&
      confirm('Are you sure you want to delete this question?')
    ) {
      try {
        await RemoteServices.removeFailedAnswer(toRemoveFailedAnswer.id);
        this.failedAnswers = this.failedAnswers.filter(
          (failedAnswer) => failedAnswer.id != toRemoveFailedAnswer.id
        );
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    }
  }

  async refreshFailedAnswers() {
    await this.$store.dispatch('loading');
    try {
      await RemoteServices.updateFailedAnswers(this.dashboardId);
      this.failedAnswers = await RemoteServices.getFailedAnswers(
        this.dashboardId
      );
      this.$emit('refresh');
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }
}
</script>

<style lang="scss" scoped>
.question-textarea {
  text-align: left;

  .CodeMirror,
  .CodeMirror-scroll {
    min-height: 200px !important;
  }
}
.option-textarea {
  text-align: left;

  .CodeMirror,
  .CodeMirror-scroll {
    min-height: 100px !important;
  }
}
</style>
