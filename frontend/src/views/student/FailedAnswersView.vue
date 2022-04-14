<template>
  <v-container fluid>
    <v-card class="table">
      <v-row>
        <v-col>
          <h1>Failed Answers</h1>
        </v-col>
      </v-row>

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
        </template>

      </v-data-table>

    </v-card>
  </v-container>
</template>

<script lang="ts">
import {Component, Vue, Watch} from 'vue-property-decorator';
import Question from '@/models/management/Question';
import StatementQuestion from '@/models/statement/StatementQuestion';
import RemoteServices from '@/services/RemoteServices';
import FailedAnswer from "@/models/dashboard/FailedAnswer";

@Component({
  components: {
  },
})
export default class FailedAnswersView extends Vue {
  dashboardId: number | null = null;
  failedAnswers: FailedAnswer[] = [new FailedAnswer({"id":377,"collected":"2022-04-14T20:57:25.891775","answered":true,"questionAnswerDto":{"question":{"id":586,"key":null,"title":"Question Title 1","content":"Question Content 1","difficulty":null,"numberOfClarifications":0,"numberOfAnswers":0,"numberOfGeneratedQuizzes":0,"numberOfNonGeneratedQuizzes":1,"numberOfCorrect":0,"creationDate":"2022-04-14T20:57:23.776584Z","status":"AVAILABLE","image":null,"topics":[],"sequence":null,"questionDetailsDto":{"type":"multiple_choice","options":[{"id":1585,"sequence":0,"correct":true,"content":"Option A"},{"id":1586,"sequence":1,"correct":false,"content":"Option B"},{"id":1587,"sequence":2,"correct":false,"content":"Option C"},{"id":1588,"sequence":3,"correct":false,"content":"Option D"}]}},"answerDetails":{"type":"multiple_choice","option":{"id":1587,"sequence":2,"correct":false,"content":"Option C"}}}})];
  statementQuestion: StatementQuestion | null = null;
  studentViewDialog: boolean = false;

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
  }

  convertDate (date : string) {
    var y = date.replaceAll("T", " ")
    var i = y.indexOf(":", date.indexOf(":") + 1)
    return y.substring(0, i)
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