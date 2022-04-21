import { ISOtoString } from '@/services/ConvertDateService';

export default class WeeklyScore {
  id!: number;
  numberAnswered!: number;
  uniquelyAnswered!: number;
  percentageCorrect!: number;
  week!: string;

  constructor(jsonObj?: WeeklyScore) {
    if (jsonObj) {
      this.id = jsonObj.id;
      if (jsonObj.numberAnswered != undefined)
        this.numberAnswered = jsonObj.numberAnswered;
      if (jsonObj.uniquelyAnswered != undefined)
        this.uniquelyAnswered = jsonObj.uniquelyAnswered;
      if (jsonObj.percentageCorrect != undefined)
        this.percentageCorrect = jsonObj.percentageCorrect;
      if (jsonObj.week) this.week = ISOtoString(jsonObj.week);
    }
  }
}
