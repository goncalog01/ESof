<template>
  <div class="container">
    <h2>Dashboard</h2>

    <v-card class="table">
      <v-row>
        <v-col>
          <v-btn color="primary" dark v-on:click="show = 'Global'">
            Global Statistics</v-btn
          >
        </v-col>
        <v-col>
          <v-btn color="primary" dark v-on:click="show = 'Weekly'"
            >Weekly Scores <br />
            {{
              dashboard != null
                ? dashboard.lastCheckWeeklyScores != null
                  ? dashboard.lastCheckWeeklyScores
                  : '-'
                : '-'
            }}</v-btn
          >
        </v-col>
        <v-col>
          <v-btn
            color="primary"
            dark
            v-on:click="show = 'Failed'"
            data-cy="failedAnswersMenuButton"
            >Failed Answers <br />
            {{
              dashboard
                ? dashboard.lastCheckFailedAnswers
                  ? dashboard.lastCheckFailedAnswers
                  : '-'
                : '-'
            }}</v-btn
          ></v-col
        >
        <v-col>
          <v-btn
            color="primary"
            dark
            v-on:click="show = 'Difficult'"
            data-cy="difficultQuestionsMenuButton"
            >Difficult Questions <br />
            {{
              dashboard
                ? dashboard.lastCheckDifficultQuestions
                  ? dashboard.lastCheckDifficultQuestions
                  : '-'
                : '-'
            }}</v-btn
          ></v-col
        >
      </v-row>
    </v-card>

    <div v-if="show === 'Global'" class="stats-container">
      <global-stats-view></global-stats-view>
    </div>

    <div v-if="show === 'Weekly'" class="stats-container">
      <weekly-score-view
        :dashboard-id="dashboardId"
        v-on:refresh="onWeeklyScoresRefresh"
      >
      </weekly-score-view>
    </div>

    <div v-if="show === 'Failed'" class="stats-container">
      <failed-answers-view
        :dashboardId="dashboardId"
        v-on:refresh="onFailedAnswersRefresh"
      >
      </failed-answers-view>
    </div>

    <div v-if="show === 'Difficult'" class="stats-container">
      <difficult-questions-view
        :dashboardId="dashboardId"
        v-on:refresh="onDifficultQuestionsRefresh"
      >
      </difficult-questions-view>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import GlobalStatsView from '@/views/student/GlobalStatsView.vue';
import Dashboard from '@/models/dashboard/Dashboard';
import FailedAnswersView from '@/views/student/FailedAnswersView.vue';
import DifficultQuestionsView from '@/views/student/DifficultQuestionsView.vue';
import WeeklyScoreView from '@/views/student/WeeklyScoreView.vue';

@Component({
  components: {
    'weekly-score-view': WeeklyScoreView,
    'global-stats-view': GlobalStatsView,
    'failed-answers-view': FailedAnswersView,
    'difficult-questions-view': DifficultQuestionsView,
  },
})
export default class StatsView extends Vue {
  dashboard: Dashboard | null = null;
  dashboardId: number | null = null;

  lastCheckFailedAnswers: string | null = null;
  lastCheckDifficultQuestions: string | null = null;
  lastCheckWeeklyScores: string | null = null;

  show: string = 'Global';

  async created() {
    await this.$store.dispatch('loading');
    try {
      this.dashboard = await RemoteServices.getUserDashboard();
      this.dashboardId = this.dashboard.id;
      this.lastCheckFailedAnswers = this.dashboard.lastCheckFailedAnswers;
      this.lastCheckDifficultQuestions =
        this.dashboard.lastCheckDifficultQuestions;
      this.lastCheckWeeklyScores = this.dashboard.lastCheckWeeklyScores;
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  async onFailedAnswersRefresh() {
    this.dashboard = await RemoteServices.getUserDashboard();
    this.lastCheckFailedAnswers = this.dashboard!.lastCheckFailedAnswers;
  }

  async onDifficultQuestionsRefresh() {
    this.dashboard = await RemoteServices.getUserDashboard();
    this.lastCheckDifficultQuestions =
      this.dashboard!.lastCheckDifficultQuestions;
  }

  async onWeeklyScoresRefresh() {
    this.dashboard = await RemoteServices.getUserDashboard();
    this.lastCheckWeeklyScores = this.dashboard!.lastCheckWeeklyScores;
  }
}
</script>
