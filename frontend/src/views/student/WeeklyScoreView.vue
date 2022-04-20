<template>
  <v-container fluid>
    <v-card class="table">
      <v-row>
        <v-col>
          <h1>Weekly Scores</h1>
        </v-col>
      </v-row>
      <v-data-table
        :headers="headers"
        :items="weeklyScores"
        :items-per-page="5"
        class="elevation-1"
      ></v-data-table>
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

interface Entry {
  buttons: any;
  dto: WeeklyScore;
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
  entries: Entry[] = [];

  headers: Header[] = [
    { text: 'Actions', value: 'buttons' },
    { text: 'Week', value: 'dto.week' },
    { text: 'Number Answered', value: 'dto.numberAnswered' },
    { text: 'Uniquely Answered', value: 'dto.uniquelyAnswered' },
    { text: 'Percentage Correct', value: 'dto.percentageCorrect' },
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
}
</script>
