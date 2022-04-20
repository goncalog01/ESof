import Question from '@/models/management/Question';
export default class DifficultQuestion {
  id!: number;
  percentage!: number;
  removedDate!: string;
  removed!: boolean;
  questionDto!: Question;

  constructor(jsonObj?: DifficultQuestion) {
    if (jsonObj) {
      this.id = jsonObj.id;
      if (jsonObj.percentage) this.percentage = jsonObj.percentage;
      if (jsonObj.removedDate) this.removedDate = jsonObj.removedDate;
      if (jsonObj.removed) this.removed = jsonObj.removed;
      if (jsonObj.questionDto)
        this.questionDto = new Question(jsonObj.questionDto);
    }
  }
}
