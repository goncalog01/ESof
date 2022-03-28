package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.DomainEntity;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.DifficultQuestion;

import java.util.Set;
import java.util.HashSet;

import javax.persistence.*;

@Entity
public class SameDifficulty implements DomainEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToMany
    @JoinColumn(name = "same_difficulty_id")
    private Set<DifficultQuestion> sameDifficultyQuestions;

    @OneToOne
    private DifficultQuestion difficultQuestion;

    public SameDifficulty() {
    }

    public SameDifficulty(DifficultQuestion difficultQuestion) {
        this(difficultQuestion, new HashSet<DifficultQuestion>());
    }

    public SameDifficulty(DifficultQuestion difficultQuestion, Set<DifficultQuestion> sameDifficultyQuestions) {
        setDifficultQuestion(difficultQuestion);
        setSameDifficultyQuestions(sameDifficultyQuestions);
    }

    public void setDifficultQuestion(DifficultQuestion difficultQuestion) {
        this.difficultQuestion = difficultQuestion;
    }

    public DifficultQuestion getDifficultQuestion() {
        return difficultQuestion;
    }

    public void setSameDifficultyQuestions(Set<DifficultQuestion> sameDifficultyQuestions) {
        this.sameDifficultyQuestions = sameDifficultyQuestions;
    }

    public Set<DifficultQuestion> getDifficultQuestions() {
        return sameDifficultyQuestions;
    }

    public void addSameDifficultyQuestion(DifficultQuestion difficultQuestion){
        this.sameDifficultyQuestions.add(difficultQuestion);
    }

    @Override
    public void accept(Visitor visitor) {
        // TODO Auto-generated method stub
    }

    @Override
    public String toString() {
        return "SameDifficulty{" +
                "id=" + id +
                ", sameDifficultyQuestions=" + sameDifficultyQuestions +
                ", difficultQuestion=" + difficultQuestion +
                '}';
    }
}

