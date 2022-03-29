package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuestionAnswerRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.Dashboard;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.FailedAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.dto.FailedAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.repository.DashboardRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.repository.FailedAnswerRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz;

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Service
public class FailedAnswerService {

    @Autowired
    private DashboardRepository dashboardRepository;

    @Autowired
    private FailedAnswerRepository failedAnswerRepository;

    @Autowired
    private QuizAnswerRepository quizAnswerRepository;

    @Autowired
    private QuestionAnswerRepository questionAnswerRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public FailedAnswerDto createFailedAnswer(int dashboardId, int questionAnswerId) {
        Dashboard dashboard = dashboardRepository.findById(dashboardId).orElseThrow(() -> new TutorException(ErrorMessage.DASHBOARD_NOT_FOUND, dashboardId));
        QuestionAnswer questionAnswer = questionAnswerRepository.findById(questionAnswerId).orElseThrow(() -> new TutorException(QUESTION_ANSWER_NOT_FOUND, questionAnswerId));

        FailedAnswer failedAnswer = new FailedAnswer(dashboard, questionAnswer, DateHandler.now());
        failedAnswerRepository.save(failedAnswer);

        return new FailedAnswerDto(failedAnswer);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void removeFailedAnswer(int failedAnswerId) {
        FailedAnswer toRemove = failedAnswerRepository.findById(failedAnswerId).orElseThrow(() -> new TutorException(ErrorMessage.FAILED_ANSWER_NOT_FOUND, failedAnswerId));
        toRemove.remove();
        failedAnswerRepository.delete(toRemove);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Collection<FailedAnswerDto> getFailedAnswers(int dashboardId) {
        Dashboard dashboard = dashboardRepository.findById(dashboardId).orElseThrow(() -> new TutorException(DASHBOARD_NOT_FOUND, dashboardId));
        List<FailedAnswer> failedAnswers = failedAnswerRepository.findAll().stream()
                .filter(fa -> fa.getDashboard().getId() == dashboard.getId())
                .sorted(Comparator.comparing(FailedAnswer::getCollected).reversed())
                .collect(Collectors.toList());
        return failedAnswers.stream().map(FailedAnswerDto::new).collect(Collectors.toList());
    }
    
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateFailedAnswers(int dashboardId, String minDate, String maxDate) {

        Dashboard dashboard = dashboardRepository.findById(dashboardId).orElseThrow(() -> new TutorException(ErrorMessage.DASHBOARD_NOT_FOUND, dashboardId));
        for (QuestionAnswer questionAnswer : questionAnswerRepository.findAll()) {
            int questionAnswerId = questionAnswer.getId();
            QuizAnswer quizAnswer = questionAnswer.getQuizAnswer();
            Quiz quiz = quizAnswer.getQuiz();

            // Setting dashboard's last check failed answers to max question creation date
            if (dashboard.getLastCheckFailedAnswers() == null) {
                dashboard.setLastCheckFailedAnswers(quizAnswer.getCreationDate().minusSeconds(1));
            } else {
                LocalDateTime creationDate = quizAnswer.getCreationDate().minusSeconds(1);
                LocalDateTime currentDate = dashboard.getLastCheckFailedAnswers();
                LocalDateTime maxTime = (creationDate.isAfter(currentDate)) ? creationDate : currentDate;
                if (!maxTime.isEqual(currentDate)) {
                    dashboard.setLastCheckFailedAnswers(maxTime);
                }
            }

            // Boolean to check if there were given min and max dates
            boolean hasTimeInterval = (minDate != null) && (maxDate != null);

            // Check if this question has been checked before, if so ignore it (if no min and max dates were given)
            boolean hasBeenChecked = dashboard.getLastCheckFailedAnswers().isAfter(quizAnswer.getCreationDate());
            if (hasBeenChecked && !hasTimeInterval) {
                continue;
            }

            // Check if failed answer already exists, if so ignore it
            boolean isInFailedAnswers = dashboard.getFailedAnswers()
                    .stream().filter(fa -> fa.getQuestionAnswer().getId() == questionAnswerId)
                    .collect(Collectors.toList()).size() > 0;
            if (isInFailedAnswers) {
                continue;
            }

            // Check if student that responded is the same, if not ignore it
            boolean sameStudent = quizAnswer.getStudent().getId() == dashboard.getStudent().getId();
            if (!sameStudent) {
                continue;
            }

            // Check if quiz belongs to course execution, if not ignore it
            boolean inSameCourseExecution = dashboard.getCourseExecution().getId() == quiz.getCourseExecution().getId();
            if (!inSameCourseExecution) {
                continue;
            }

            // It's only a failed answer if it was completed and the response is not correct
            boolean isCompleted = questionAnswer.getQuizAnswer().isCompleted();
            boolean isCorrect = questionAnswer.isCorrect();
            if (isCompleted && !isCorrect) {
                // A given min and max time were given
                if (hasTimeInterval) {
                    LocalDateTime minTime = DateHandler.toLocalDateTime(minDate);
                    LocalDateTime maxTime = DateHandler.toLocalDateTime(maxDate);
                    boolean answerInTimeInterval = (quizAnswer.getCreationDate().isAfter(minTime)) && (quizAnswer.getCreationDate().isBefore(maxTime));
                    if (!answerInTimeInterval) {
                        continue;
                    }
                }
                // Check if quiz is type IN_CLASS
                boolean inClass = quiz.isInClass();
                if (inClass) {
                    // If so, check if quiz's results dates are later
                    // If they are, don't count it as a failed answer just yet
                    boolean resultsAreLater = DateHandler.now().isBefore(quiz.getResultsDate());
                    if (resultsAreLater) {
                        continue;
                    }
                }
                // Create a new failed answer
                createFailedAnswer(dashboardId, questionAnswerId);
            }
        }
    }


}
