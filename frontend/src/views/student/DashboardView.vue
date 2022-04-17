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
              dashboard != null ? dashboard.lastCheckWeeklyScores : '-'
            }}</v-btn
          >
        </v-col>
        <v-col>
          <v-btn color="primary" dark v-on:click="show = 'Failed'"
            >Failed Answers <br />
            {{
              dashboard != null ? dashboard.lastCheckFailedAnswers : '-'
            }}</v-btn
          ></v-col
        >
        <v-col>
          <v-btn color="primary" dark v-on:click="show = 'Difficult'"
            >Difficult Questions <br />
            {{
              dashboard != null ? dashboard.lastCheckDifficultQuestions : '-'
            }}</v-btn
          ></v-col
        >
      </v-row>
    </v-card>

    <div v-if="show === 'Global'" class="stats-container">
      <global-stats-view></global-stats-view>
    </div>

    <div v-if="show === 'Failed'" class="stats-container">
      <failed-answers-view
        :dashboardId="dashboardId"
        :lastCheckFailedAnswers="lastCheckFailedAnswers"
        v-on:refresh="onFailedAnswersRefresh"
      >
      </failed-answers-view>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import GlobalStatsView from '@/views/student/GlobalStatsView.vue';
import Dashboard from '@/models/dashboard/Dashboard';
import FailedAnswersView from '@/views/student/FailedAnswersView.vue';

@Component({
  components: {
    'global-stats-view': GlobalStatsView,
    'failed-answers-view': FailedAnswersView,
  },
})
export default class StatsView extends Vue {
  dashboard: Dashboard | null = null;
  dashboardId: number | null = null;

  lastCheckFailedAnswers: string | null = null;

  show: string = 'Global';

  async created() {
    await this.$store.dispatch('loading');
    try {
      this.dashboard = await RemoteServices.getUserDashboard();
      this.dashboardId = this.dashboard.id;
      this.lastCheckFailedAnswers = this.dashboard.lastCheckFailedAnswers;
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }
  
}
</script>
