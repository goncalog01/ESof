package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.QuestionAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.FailedAnswer;

public class FailedAnswerDto implements Serializable {

    private Integer id;

    private String collected;

    private boolean answered;

    private QuestionAnswerDto questionAnswerDto;

    public FailedAnswerDto(){
    }

    public FailedAnswerDto(FailedAnswer failedAnswer){
        setCollected(failedAnswer.getCollected());
        setAnswered(failedAnswer.getAnswered());
        setQuestionAnswerDto(new QuestionAnswerDto(failedAnswer.getQuestionAnswer()));
    }

    public Integer getId() {
        return id;
    }

    public String getCollected() { return collected; }

    public void setCollected(LocalDateTime collected) { this.collected = collected.format(DateTimeFormatter.ISO_DATE_TIME); }

    public boolean getAnswered() {
        return answered;
    }

    public void setAnswered(boolean answered) {
        this.answered = answered;
    }

    public QuestionAnswerDto getQuestionAnswerDto() {
        return questionAnswerDto;
    }

    public void setQuestionAnswerDto(QuestionAnswerDto questionAnswerDto) {
        this.questionAnswerDto = questionAnswerDto;
    }

    @Override
    public String toString() {
        return "FailedAnswerDto{" +
            "id=" + id +
            ", collected=" + collected +
            ", answered=" + answered +
            "}";
    }
}