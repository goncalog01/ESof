package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.DomainEntity;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
public class WeeklyScore implements DomainEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private int numberAnswered;

    private int uniquelyAnswered;

    private int percentageCorrect;

    private LocalDate week;

    @ManyToOne
    private Dashboard dashboard;

    @OneToOne(cascade=CascadeType.ALL)
    private SamePercentage samePercentage;

    public WeeklyScore() {
    }

    public WeeklyScore(Dashboard dashboard, LocalDate week) {
        setWeek(week);
        setDashboard(dashboard);
        setSamePercentage(new SamePercentage(this, new HashSet<>()));
    }

    public Integer getId() {
        return id;
    }

    public int getNumberAnswered() {
        return numberAnswered;
    }

    public void setNumberAnswered(int numberAnswered) {
        this.numberAnswered = numberAnswered;
    }

    public int getUniquelyAnswered() {
        return uniquelyAnswered;
    }

    public void setUniquelyAnswered(int uniquelyAnswered) {
        this.uniquelyAnswered = uniquelyAnswered;
    }

    public int getPercentageCorrect() {
        return percentageCorrect;
    }

    public void setPercentageCorrect(int percentageCorrect) {
        this.percentageCorrect = percentageCorrect;
        samePercentage.setSameWeeklyScores(dashboard.getWeeklyScores()
                .stream().filter(ws -> ws != this && ws.getPercentageCorrect() == percentageCorrect)
                .collect(Collectors.toSet()));
        samePercentage.getSameWeeklyScores().forEach(ws -> ws.getSamePercentage().addSameWeeklyScore(this));
    }

    public LocalDate getWeek() {
        return week;
    }

    public void setWeek(LocalDate week) {
        this.week = week;
    }

    public Dashboard getDashboard() {
        return dashboard;
    }

    public void setDashboard(Dashboard dashboard) {
        this.dashboard = dashboard;
        this.dashboard.addWeeklyScore(this);
    }

    public SamePercentage getSamePercentage() {
        return samePercentage;
    }

    public void setSamePercentage(SamePercentage samePercentage) {
        this.samePercentage = samePercentage;
    }

    public void accept(Visitor visitor) {
    }

    public void remove() {
        for (WeeklyScore ws: getSamePercentage().getSameWeeklyScores()) {
            ws.getSamePercentage().getSameWeeklyScores().remove(this);
        }
        this.samePercentage = null;
        this.dashboard.getWeeklyScores().remove(this);
        this.dashboard = null;
    }

    @Override
    public String toString() {
        return "WeeklyScore{" +
                "id=" + getId() +
                ", numberAnswered=" + numberAnswered +
                ", uniquelyAnswered=" + uniquelyAnswered +
                ", percentageCorrect=" + percentageCorrect +
                ", week=" + getWeek() +
                "}";
    }
}