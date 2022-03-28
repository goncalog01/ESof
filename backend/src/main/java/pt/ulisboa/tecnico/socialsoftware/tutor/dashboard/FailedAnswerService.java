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
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.DifficultQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.FailedAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.dto.DifficultQuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.dto.FailedAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.repository.DashboardRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.repository.FailedAnswerRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz;

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.time.*;

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
    public void updateFailedAnswers (int dashboardId, String localDateBefore, String localDateYesterday) {

        Dashboard dashboard = dashboardRepository.findById(dashboardId).orElseThrow(() -> new TutorException(ErrorMessage.DASHBOARD_NOT_FOUND, dashboardId));
        for (QuestionAnswer questionAnswer : questionAnswerRepository.findAll()) {
            int questionAnswerId = questionAnswer.getId();
            boolean isCompleted = questionAnswer.getQuizAnswer().isCompleted();
            boolean isCorrect = questionAnswer.isCorrect();
            // its only a failed answer if it was completed and the response is not correct
            if (isCompleted && !isCorrect) {
                QuizAnswer quizAnswer = questionAnswer.getQuizAnswer();
                Quiz quiz = quizAnswer.getQuiz();

                boolean hasBeenChecked = dashboard.getLastCheckFailedAnswers().isAfter(quizAnswer.getCreationDate());
                if (hasBeenChecked) {
                    continue;
                }

                dashboard.setLastCheckFailedAnswers(quizAnswer.getCreationDate().minusSeconds(1));

                boolean inClass = quiz.isInClass();
                // if the type of quiz is IN_CLASS and the results date is later,
                // its not considered a failed answer
                if (inClass) {
                    boolean isLater = LocalDateTime.now().isBefore(quiz.getResultsDate());
                    if (isLater) {
                        continue;
                    }
                }

                // create a new failed answer
                this.createFailedAnswer(dashboardId, questionAnswerId);
            }

        }
    }


}
