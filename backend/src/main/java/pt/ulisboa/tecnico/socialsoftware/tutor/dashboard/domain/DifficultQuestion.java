package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain;

import org.apache.commons.lang3.builder.Diff;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.SameDifficulty;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.DomainEntity;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler;


import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

@Entity
public class DifficultQuestion implements DomainEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private int percentage;

    private boolean removed = false;

    private LocalDateTime removedDate;

    @ManyToOne
    private Question question;

    @ManyToOne
    private Dashboard dashboard;

    @OneToOne(cascade=CascadeType.ALL, mappedBy="difficultQuestion", orphanRemoval=true)
    private SameDifficulty sameDifficulty;

    public DifficultQuestion(){
    }

    public DifficultQuestion(Dashboard dashboard, Question question, int percentage){
        if (percentage < 0 || percentage > 24)
            throw new TutorException(ErrorMessage.CANNOT_CREATE_DIFFICULT_QUESTION);

        if (question.getCourse() != dashboard.getCourseExecution().getCourse()) {
            throw new TutorException(ErrorMessage.CANNOT_CREATE_DIFFICULT_QUESTION);
        }

        Set<DifficultQuestion> sameDifficultyQuestions = new HashSet<>();
        for (DifficultQuestion dq : dashboard.getDifficultQuestions()){
            if (dq.getPercentage() == percentage){
                sameDifficultyQuestions.add(dq);
                dq.sameDifficulty.addSameDifficultyQuestion(this);
            }
        }

        setSameDifficulty(new SameDifficulty(this, sameDifficultyQuestions));

        setPercentage(percentage);
        setRemovedDate(null);
        setRemoved(false);
        setQuestion(question);
        setDashboard(dashboard);
    }

    public void remove() {
        if (!removed) {
            throw new TutorException(ErrorMessage.CANNOT_REMOVE_DIFFICULT_QUESTION);
        } else if (removedDate.isBefore(DateHandler.now().minusDays(7))) {
            throw new TutorException(ErrorMessage.CANNOT_REMOVE_DIFFICULT_QUESTION);
        }

        for (DifficultQuestion dq: getSameDifficulty().getSameDifficultyQuestions()) {
            dq.getSameDifficulty().getSameDifficultyQuestions().remove(this);
        }
        sameDifficulty = null;

        dashboard.getDifficultQuestions().remove(this);
        dashboard = null;
    }

    public Dashboard getDashboard() {
        return dashboard;
    }

    public void setDashboard(Dashboard dashboard) {
        this.dashboard = dashboard;
        this.dashboard.addDifficultQuestion(this);
    }

    public void setSameDifficulty(SameDifficulty sameDifficulty) {
        this.sameDifficulty = sameDifficulty;
    }

    public SameDifficulty getSameDifficulty() { return sameDifficulty; }

    public Integer getId() {
        return id;
    }

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    public LocalDateTime getRemovedDate() {
        return removedDate;
    }

    public void setRemovedDate(LocalDateTime collected) {
        this.removedDate = collected;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public void update() {
        this.setPercentage(this.getQuestion().getDifficulty());
    }

    @Override
    public void accept(Visitor visitor) {
        // TODO Auto-generated method stub
    }

    @Override
    public String toString() {
        return "DifficultQuestion{" +
                "id=" + id +
                ", percentage=" + percentage +
                ", removed=" + removed +
                ", question=" + question +
                "}";
    }

}