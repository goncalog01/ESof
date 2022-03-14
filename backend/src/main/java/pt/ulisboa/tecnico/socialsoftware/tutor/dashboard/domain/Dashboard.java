package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.DomainEntity;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;

import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.Student;
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.domain.CourseExecution;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

@Entity
public class Dashboard implements DomainEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private LocalDateTime lastCheckFailedAnswers;

    private LocalDateTime lastCheckDifficultQuestions;

    private LocalDateTime lastCheckWeeklyScores;

    @ManyToOne
    private CourseExecution courseExecution;

    @ManyToOne
    private Student student;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "dashboard", orphanRemoval = true)
    private Set<WeeklyScore> weeklyScores = new HashSet<>();

    public Dashboard() {
    }

    public Dashboard(CourseExecution courseExecution, Student student) {
        LocalDateTime currentDate = DateHandler.now();
        setLastCheckFailedAnswers(currentDate);
        setLastCheckDifficultQuestions(currentDate);
        setLastCheckWeeklyScores(currentDate);
        setCourseExecution(courseExecution);
        setStudent(student);
    }

    public Integer getId() {
        return id;
    }

    public LocalDateTime getLastCheckFailedAnswers() {
        return lastCheckFailedAnswers;
    }

    public void setLastCheckFailedAnswers(LocalDateTime lastCheckFailedAnswer) {
        this.lastCheckFailedAnswers = lastCheckFailedAnswer;
    }

    public LocalDateTime getLastCheckDifficultQuestions() {
        return lastCheckDifficultQuestions;
    }

    public void setLastCheckDifficultQuestions(LocalDateTime lastCheckDifficultAnswers) {
        this.lastCheckDifficultQuestions = lastCheckDifficultAnswers;
    }

    public LocalDateTime getLastCheckWeeklyScores() {
        return lastCheckWeeklyScores;
    }

    public void setLastCheckWeeklyScores(LocalDateTime currentWeek) {
        this.lastCheckWeeklyScores = currentWeek;
    }

    public CourseExecution getCourseExecution() {
        return courseExecution;
    }

    public void setCourseExecution(CourseExecution courseExecution) {
        this.courseExecution = courseExecution;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
        this.student.addDashboard(this);
    }

    public void remove() {
        student.getDashboards().remove(this);
        student = null;
    }

    public void accept(Visitor visitor) {
    }

    public Set<WeeklyScore> getWeeklyScores() {
        return weeklyScores;
    }

    public void addWeeklyScore(WeeklyScore weeklyScore) {
        if (weeklyScores.stream().anyMatch(weeklyScore1 -> weeklyScore1.getWeek().isEqual(weeklyScore.getWeek()))) {
            throw new TutorException(ErrorMessage.WEEKLY_SCORE_ALREADY_CREATED);
        }
        weeklyScores.add(weeklyScore);
    }

    @Override
    public String toString() {
        return "Dashboard{" +
                "id=" + id +
                ", lastCheckWeeklyScores=" + lastCheckWeeklyScores +
                ", lastCheckFailedAnswers=" + lastCheckFailedAnswers +
                ", lastCheckDifficultAnswers=" + lastCheckDifficultQuestions +
                "}";
    }
}