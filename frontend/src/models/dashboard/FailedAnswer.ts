import {QuestionAnswer} from "@/models/management/QuestionAnswer";

export default class FailedAnswer {
    id!: number;
    collected!: string;
    answered!: boolean;
    questionAnswerDto!: QuestionAnswer;

    constructor(jsonObj?: FailedAnswer) {
        if (jsonObj) {
            this.id = jsonObj.id;
            if (jsonObj.collected)
                this.collected = jsonObj.collected;
            if (jsonObj.answered)
                this.answered = jsonObj.answered;
            if (jsonObj.questionAnswerDto)
                this.questionAnswerDto = new QuestionAnswer(jsonObj.questionAnswerDto)
        }
    }
}