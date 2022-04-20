package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.DomainEntity;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;

import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.Student;
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.domain.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer;

import java.time.LocalDateTime;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.*;

@Entity
@Table(name = "dashboard")
public class Dashboard implements DomainEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private LocalDateTime lastCheckFailedAnswers;

    private LocalDateTime lastCheckDifficultQuestions = null;

    private LocalDateTime lastCheckWeeklyScores = null;

    @ManyToOne
    private CourseExecution courseExecution;

    @ManyToOne
    private Student student;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "dashboard", orphanRemoval = true)
    private Set<WeeklyScore> weeklyScores = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "dashboard", orphanRemoval = true)
    private Set<DifficultQuestion> difficultQuestions = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "dashboard", orphanRemoval = true)
    private Set<FailedAnswer> failedAnswers = new HashSet<>();

    public Dashboard() {
    }

    public Dashboard(CourseExecution courseExecution, Student student) {
        LocalDateTime currentDate = DateHandler.now();
        setLastCheckFailedAnswers(currentDate);
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


    public Set<FailedAnswer> getFailedAnswers() {
        return failedAnswers;
    }
  
    public Set<DifficultQuestion> getDifficultQuestions() {
        return difficultQuestions;
    }

    public void setDifficultQuestions(Set<DifficultQuestion> difficultQuestions) {
        this.difficultQuestions.clear();
        this.difficultQuestions.addAll(difficultQuestions);
    }

    public void remove() {
        student.getDashboards().remove(this);
        student = null;
    }

    public void addFailedAnswer(FailedAnswer failedAnswer) {
        if (failedAnswers.stream().anyMatch(failedAnswer1 -> failedAnswer1.getQuestionAnswer() == failedAnswer.getQuestionAnswer())) {
            throw new TutorException(ErrorMessage.FAILED_ANSWER_ALREADY_CREATED);
        }
        failedAnswers.add(failedAnswer);
    }
  
    public void addDifficultQuestion(DifficultQuestion difficultQuestion) {
        if (difficultQuestions.stream()
                .anyMatch(difficultQuestion1 -> difficultQuestion1.getQuestion() == difficultQuestion.getQuestion())) {
            throw new TutorException(ErrorMessage.DIFFICULT_QUESTION_ALREADY_CREATED);
        }
        difficultQuestions.add(difficultQuestion);
    }

    public void accept(Visitor visitor) {
    }

    public void updateDifficultQuestions() {

        // add difficult questions back if 7 days have passed sinced they were removed
        difficultQuestions.stream()
                .forEach(dq -> { if(dq.isRemoved() && dq.getRemovedDate().isBefore(DateHandler.now().minusDays(7)))
                                    dq.setRemoved(false); });

        // remove existing difficult questions whose difficulty has changed
        setDifficultQuestions(difficultQuestions.stream()
                .filter(df -> (df.getPercentage() == df.getQuestion().getLastWeekDifficulty() || df.isRemoved()))
                .collect(Collectors.toSet()));

        // Get all answered questions by the dashboard's student in the last 7 days
        // following the associations Dashboard -> Student ->* QuizAnswers -> Quiz ->* QuizQuestion -> Question
        Set<Question> answeredQuestions = new HashSet<Question>();
        Set<Question> markedQuestions = difficultQuestions.stream()
                .map(dq -> dq.getQuestion()).collect(Collectors.toSet());

        for (QuizAnswer qa : getStudent().getQuizAnswers()
                .stream().filter(q -> q.getAnswerDate().isAfter(DateHandler.now().minusDays(7))
                                    && q.getQuiz().getCourseExecution() == courseExecution)
                .collect(Collectors.toSet())) {
            answeredQuestions.addAll(qa.getQuiz().getQuizQuestions().stream()
                    .map(qq -> qq.getQuestion())
                    .filter(qq -> !markedQuestions.contains(qq))
                    .collect(Collectors.toSet()));
        }

        // add all answered questions that have become difficult since the last update
        Map<Question, Integer> questionDifficulties = new HashMap<Question, Integer>();
        answeredQuestions.stream().forEach(aq -> questionDifficulties.put(aq, aq.getLastWeekDifficulty()));

        Set<Question> questionsToAdd = questionDifficulties.keySet().stream()
                .filter(q -> questionDifficulties.get(q) < 25).collect(Collectors.toSet());

        difficultQuestions.addAll(questionsToAdd.stream()
                .map(qta -> new DifficultQuestion(this, qta, questionDifficulties.get(qta))).collect(Collectors.toSet()));

        setLastCheckDifficultQuestions(DateHandler.now());
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