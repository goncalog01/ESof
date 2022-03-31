package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.Dashboard;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.DifficultQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.dto.DashboardDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.dto.DifficultQuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.repository.DashboardRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.repository.DifficultQuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.repository.SameDifficultyRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Service
public class DifficultQuestionService {
    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private DashboardRepository dashboardRepository;

    @Autowired
    private DifficultQuestionRepository difficultQuestionRepository;

    @Autowired
    private SameDifficultyRepository sameDifficultyRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public DifficultQuestionDto createDifficultQuestion(int dashboardId, int questionId, int percentage) {
        Dashboard dashboard = dashboardRepository.findById(dashboardId).orElseThrow(() -> new TutorException(ErrorMessage.DASHBOARD_NOT_FOUND, dashboardId));
        Question question = questionRepository.findById(questionId).orElseThrow(() -> new TutorException(QUESTION_NOT_FOUND, questionId));

        DifficultQuestion difficultQuestion = new DifficultQuestion(dashboard, question, percentage);
        difficultQuestionRepository.save(difficultQuestion);
        sameDifficultyRepository.save(difficultQuestion.getSameDifficulty());

        return new DifficultQuestionDto(difficultQuestion);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void removeDifficultQuestion(int difficultQuestionId) {
        DifficultQuestion difficultQuestion = difficultQuestionRepository.findById(difficultQuestionId).orElseThrow(() -> new TutorException(DIFFICULT_QUESTION_NOT_FOUND, difficultQuestionId));

        difficultQuestion.remove();
        //difficultQuestionRepository.delete(difficultQuestion);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<DifficultQuestionDto> getDifficultQuestions(int dashboardId){
        Dashboard dashboard = dashboardRepository.findById(dashboardId).orElseThrow(() -> new TutorException(ErrorMessage.DASHBOARD_NOT_FOUND, dashboardId));

        List<DifficultQuestionDto> difficultQuestions = new ArrayList<>();

        for(DifficultQuestion difficultQuestion : dashboard.getDifficultQuestions()) {
            if (!difficultQuestion.isRemoved()) {
                difficultQuestions.add(new DifficultQuestionDto(difficultQuestion));
            }
        }
        return difficultQuestions;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateDifficultQuestions(int dashboardId){
        Dashboard dashboard = dashboardRepository.findById(dashboardId).orElseThrow(() -> new TutorException(ErrorMessage.DASHBOARD_NOT_FOUND, dashboardId));

        Set<DifficultQuestion> oldDifficultQuestions = dashboard.getDifficultQuestions();

        dashboard.updateDifficultQuestions();
        Set<DifficultQuestion> newDifficultQuestions = dashboard.getDifficultQuestions();

        Set<DifficultQuestion> removedDifficultQuestions = oldDifficultQuestions.stream()
                .filter(dq -> !newDifficultQuestions.contains(dq)).collect(Collectors.toSet());

        Set<DifficultQuestion> addedDifficultQuestions = newDifficultQuestions.stream()
                .filter(dq -> !oldDifficultQuestions.contains(dq)).collect(Collectors.toSet());

        removedDifficultQuestions.stream().forEach(dq -> difficultQuestionRepository.delete(dq));
        addedDifficultQuestions.stream().forEach(dq -> difficultQuestionRepository.save(dq));

    }
}