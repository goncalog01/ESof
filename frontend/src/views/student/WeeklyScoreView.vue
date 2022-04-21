<template>
  <v-container fluid>
    <v-card class="table">
      <v-row>
        <v-col>
          <h1>Weekly Scores</h1>
        </v-col>
        <v-col>
          <v-spacer />
          <v-btn
            color="primary"
            dark
            @click="refreshWeeklyScores"
            data-cy="refreshWeeklyScoresMenuButton"
            >Refresh</v-btn
          >
        </v-col>
      </v-row>
      <v-data-table
        :headers="headers"
        :items="weeklyScores"
        :sort-by="['week']"
        sort-desc
        :items-per-page="10"
        class="elevation-1"
      >
        <template v-slot:[`item.buttons`]="{ item }">
          <v-tooltip bottom>
            <template v-slot:activator="{ on }">
              <v-icon
                class="mr-2 action-button"
                v-on="on"
                color="red"
                @click="deleteWeeklyScore(item.id)"
                >delete</v-icon
              >
            </template>
            <span>Delete Weekly Score</span>
          </v-tooltip>
        </template>
      </v-data-table>
    </v-card>
  </v-container>
</template>

<script lang="ts">
import { Component, Prop, Vue, Watch } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import WeeklyScore from '@/models/dashboard/WeeklyScore';

interface Header {
  text: string;
  value: string;
}

@Component({
  components: {
    'weekly-score-view': WeeklyScoreView,
  },
})
export default class WeeklyScoreView extends Vue {
  @Prop({ type: Number, required: true })
  readonly dashboardId!: string;

  weeklyScores: WeeklyScore[] = [];

  headers: Header[] = [
    { text: 'Actions', value: 'buttons' },
    { text: 'Week', value: 'week' },
    { text: 'Number Answered', value: 'numberAnswered' },
    { text: 'Uniquely Answered', value: 'uniquelyAnswered' },
    { text: 'Percentage Correct', value: 'percentageCorrect' },
  ];

  async created() {
    await this.$store.dispatch('loading');
    try {
      this.weeklyScores = await RemoteServices.getWeeklyScores(
        this.dashboardId
      );
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  async refreshWeeklyScores() {
    await this.$store.dispatch('loading');
    try {
      await RemoteServices.updateWeeklyScores(this.dashboardId);
      this.weeklyScores = await RemoteServices.getWeeklyScores(
        this.dashboardId
      );
      this.$emit('refresh');
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  async deleteWeeklyScore(weeklyScoreId: number) {
    if (confirm('Are you sure you want to delete this weekly score?'))
      try {
        await RemoteServices.deleteWeeklyScore(weeklyScoreId);
        this.weeklyScores = await RemoteServices.getWeeklyScores(
          this.dashboardId
        );
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
  }
}
</script>
