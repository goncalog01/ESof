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

    @OneToMany
    private Set<WeeklyScore> sameWeeklyScores;

    @OneToOne
    private WeeklyScore weeklyScore;

    public SamePercentage() {
    }

    public SamePercentage(WeeklyScore weeklyScore, Set<WeeklyScore> sameWeeklyScores) {
        setWeeklyScore(weeklyScore);
        setSameWeeklyScores(sameWeeklyScores);
    }

    public Integer getId() {
        return id;
    }

    public Set<WeeklyScore> getSameWeeklyScores() {
        return sameWeeklyScores;
    }

    public void setSameWeeklyScores(Set<WeeklyScore> sameWeeklyScores) {
        this.sameWeeklyScores = sameWeeklyScores;
    }

    public WeeklyScore getWeeklyScore() {
        return weeklyScore;
    }

    public void setWeeklyScore(WeeklyScore weeklyScore) {
        this.weeklyScore = weeklyScore;
    }

    public void accept(Visitor visitor) {
    }

    @Override
    public String toString() {
        return "SamePercentage{" +
                "id=" + id +
                ", sameWeeklyScores=" + sameWeeklyScores +
                ", weeklyScore=" + weeklyScore +
                '}';
    }
}
