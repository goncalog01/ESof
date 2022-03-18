package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.DomainEntity;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.FailedAnswer;

import java.util.Set;
import java.util.HashSet;

import javax.persistence.*;

@Entity
public class SameQuestion implements DomainEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "same_question_id")
    private Set<FailedAnswer> sameFailedAnswers;

    @OneToOne
    private FailedAnswer failedAnswer;

    public SameQuestion() {
    }

    public SameQuestion(FailedAnswer failedAnswer, Set<FailedAnswer> sameFailedAnswers) {
        /* add verifications */
        setFailedAnswer(failedAnswer);
        setSameFailedAnswers(sameFailedAnswers);
    }

    public void setFailedAnswer(FailedAnswer failedAnswer) {
        this.failedAnswer = failedAnswer;
    }

    public void setSameFailedAnswers(Set<FailedAnswer> sameFailedAnswers) {
        this.sameFailedAnswers = sameFailedAnswers;
    }

    public Set<FailedAnswer> getSameFailedAnswers() {
        return sameFailedAnswers;
    }

    public void addSameQuestion(FailedAnswer failedAnswer){
        this.sameFailedAnswers.add(failedAnswer);
    }

    @Override
    public void accept(Visitor visitor) {
        // TODO Auto-generated method stub
    }

    @Override
    public String toString() {
        return "SameQuestion{" +
                "id=" + id +
                ", sameFailedAnswers=" + sameFailedAnswers +
                ", failedAnswer=" + failedAnswer +
                '}';
    }
}