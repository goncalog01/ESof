package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.DomainEntity;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;

import java.util.Set;

import javax.persistence.*;

@Entity
public class SamePercentage implements DomainEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "same_percentage_id")
    private Set<WeeklyScore> weeklyScores;

    @OneToOne
    private WeeklyScore weeklyScore;

    public SamePercentage() {
    }

    public SamePercentage(WeeklyScore weeklyScore, Set<WeeklyScore> weeklyScores) {
        setWeeklyScore(weeklyScore);
        setWeeklyScores(weeklyScores);
    }

    public Integer getId() {
        return id;
    }

    public Set<WeeklyScore> getWeeklyScores() {
        return weeklyScores;
    }

    public void setWeeklyScores(Set<WeeklyScore> weeklyScores) {
        this.weeklyScores = weeklyScores;
    }

    public WeeklyScore getWeeklyScore() {
        return weeklyScore;
    }

    public void setWeeklyScore(WeeklyScore weeklyScore) {
        this.weeklyScore = weeklyScore;
    }

    public void addSameWeeklyScore(WeeklyScore weeklyScore){
        this.weeklyScores.add(weeklyScore);
    }

    public void accept(Visitor visitor) {
    }

    @Override
    public String toString() {
        return "SamePercentage{" +
                "id=" + id +
                ", sameWeeklyScores=" + weeklyScores +
                ", weeklyScore=" + weeklyScore +
                '}';
    }
}
