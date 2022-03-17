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

    @OneToMany(mappedBy = "sameQuestion", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FailedAnswer> sameQuestion = new HashSet<>();

    @OneToOne
    private FailedAnswer failedAnswer;

    public SameQuestion() {
    }

    public SameQuestion(FailedAnswer failedAnswer, Set<FailedAnswer> sameQuestion) {
        /* add verifications */
        setFailedAnswer(failedAnswer);
        setSameQuestion(sameQuestion);
    }

    public void setFailedAnswer(FailedAnswer failedAnswer) {
        this.failedAnswer = failedAnswer;
    }

    public void setSameQuestion(Set<FailedAnswer> sameQuestion) {
        this.sameQuestion = sameQuestion;
    }

    public Set<FailedAnswer> getSameQuestion() {
        return sameQuestion;
    }

    public void addToSameQuestion(FailedAnswer failedAnswer){
        this.sameQuestion.add(failedAnswer);
    }

    @Override
    public void accept(Visitor visitor) {
        // TODO Auto-generated method stub
    }

    @Override
    public String toString() {
        return "SameQuestion{" +
                "id=" + id +
                ", sameQuestion=" + sameQuestion +
                ", failedAnswer=" + failedAnswer +
                '}';
    }
}