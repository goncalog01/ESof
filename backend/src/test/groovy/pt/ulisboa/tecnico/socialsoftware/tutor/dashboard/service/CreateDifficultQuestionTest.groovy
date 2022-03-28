package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.Dashboard
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.DifficultQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.MultipleChoiceQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.Student
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import spock.lang.Unroll
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.CANNOT_CREATE_DIFFICULT_QUESTION
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.DASHBOARD_NOT_FOUND
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUESTION_NOT_FOUND

@DataJpaTest
class CreateDifficultQuestionTest extends SpockTest {
    def student
    def dashboard
    def question

    def setup() {
        createExternalCourseAndExecution()

        student = new Student(USER_1_NAME, false)
        student.addCourse(externalCourseExecution)
        userRepository.save(student)

        question = new Question()
        question.setTitle(QUESTION_1_TITLE)
        question.setContent(QUESTION_1_CONTENT)
        question.setStatus(Question.Status.AVAILABLE)
        question.setNumberOfAnswers(2)
        question.setNumberOfCorrect(1)
        question.setCourse(externalCourse)
        def questionDetails = new MultipleChoiceQuestion()
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        dashboard = new Dashboard(externalCourseExecution, student)
        dashboardRepository.save(dashboard)
    }

    @Unroll
    def "create difficult question with difficulty #difficulty"() {
        when:
        difficultQuestionService.createDifficultQuestion(dashboard.getId(), question.getId(), difficulty)

        then:
        difficultQuestionRepository.count() == 1L
        def result = difficultQuestionRepository.findAll().get(0)
        result.getId() != null
        result.getDashboard().getId() == dashboard.getId()
        result.getQuestion().getId() == question.getId()
        result.isRemoved() == false
        result.getRemovedDate() == null
        result.getPercentage() == difficulty
        result.getSameDifficulty().getDifficultQuestions().size() == 0
        and:
        def dashboard = dashboardRepository.getById(dashboard.getId())
        dashboard.getDifficultQuestions().contains(result)
        and:
        sameDifficultyRepository.findAll().size() == 1

        where:
        difficulty << [0, 12, 24]
    }

    def "create two difficulty questions with same difficulty"() {
        given:
        def otherQuestion = new Question()
        otherQuestion.setTitle(QUESTION_1_TITLE)
        otherQuestion.setContent(QUESTION_1_CONTENT)
        otherQuestion.setStatus(Question.Status.AVAILABLE)
        otherQuestion.setNumberOfAnswers(2)
        otherQuestion.setNumberOfCorrect(1)
        otherQuestion.setCourse(externalCourse)
        def questionDetails = new MultipleChoiceQuestion()
        otherQuestion.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(otherQuestion)
        and:
        def otherDifficultQuestionDto = difficultQuestionService.createDifficultQuestion(dashboard.getId(), otherQuestion.getId(), 20)

        when:
        def difficultQuestionDto = difficultQuestionService.createDifficultQuestion(dashboard.getId(), question.getId(), 20)

        then:
        difficultQuestionRepository.count() == 2L
        def result = difficultQuestionRepository.findById(otherDifficultQuestionDto.getId()).get()
        result.getId() == otherDifficultQuestionDto.getId()
        result.getSameDifficulty().getDifficultQuestions().size() == 1
        def result2 = difficultQuestionRepository.findById(difficultQuestionDto.getId()).get()
        result2.getId() == difficultQuestionDto.getId()
        result2.getSameDifficulty().getDifficultQuestions().size() == 1
        and:
        def dashboard = dashboardRepository.getById(dashboard.getId())
        dashboard.getDifficultQuestions().contains(result)
        and:
        sameDifficultyRepository.findAll().size() == 2
        def sameDifficulty1 = sameDifficultyRepository.findAll().get(0)
        sameDifficulty1.getDifficultQuestions().size() == 1
        !sameDifficulty1.getDifficultQuestions().contains(sameDifficulty1.getDifficultQuestion())
        def sameDifficulty2 = sameDifficultyRepository.findAll().get(1)
        sameDifficulty2.getDifficultQuestions().size() == 1
        !sameDifficulty2.getDifficultQuestions().contains(sameDifficulty2.getDifficultQuestion())
    }

    def "create two difficulty questions with same difficulty but one is removed"() {
        given:
        def otherQuestion = new Question()
        otherQuestion.setTitle(QUESTION_1_TITLE)
        otherQuestion.setContent(QUESTION_1_CONTENT)
        otherQuestion.setStatus(Question.Status.AVAILABLE)
        otherQuestion.setNumberOfAnswers(2)
        otherQuestion.setNumberOfCorrect(1)
        otherQuestion.setCourse(externalCourse)
        def questionDetails = new MultipleChoiceQuestion()
        otherQuestion.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(otherQuestion)
        and:
        def otherDifficultQuestion = new DifficultQuestion(dashboard, otherQuestion, 20)
        otherDifficultQuestion.setRemovedDate(DateHandler.now().minusDays(1))
        otherDifficultQuestion.setRemoved(true)
        difficultQuestionRepository.save(otherDifficultQuestion)

        when:
        def difficultQuestionDto = difficultQuestionService.createDifficultQuestion(dashboard.getId(), question.getId(), 20)

        then:
        difficultQuestionRepository.count() == 2L
        def result = difficultQuestionRepository.findById(otherDifficultQuestion.getId()).get()
        result.getId() == otherDifficultQuestion.getId()
        result.getSameDifficulty().getDifficultQuestions().size() == 0
        def result2 = difficultQuestionRepository.findById(difficultQuestionDto.getId()).get()
        result2.getId() == difficultQuestionDto.getId()
        result2.getSameDifficulty().getDifficultQuestions().size() == 0
        and:
        def dashboard = dashboardRepository.getById(dashboard.getId())
        dashboard.getDifficultQuestions().contains(result)
        and:
        sameDifficultyRepository.findAll().size() == 2
        def sameDifficulty1 = sameDifficultyRepository.findAll().get(0)
        sameDifficulty1.getDifficultQuestions().size() == 0
        !sameDifficulty1.getDifficultQuestions().contains(sameDifficulty1.getDifficultQuestion())
        def sameDifficulty2 = sameDifficultyRepository.findAll().get(1)
        sameDifficulty2.getDifficultQuestions().size() == 0
        !sameDifficulty2.getDifficultQuestions().contains(sameDifficulty2.getDifficultQuestion())
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}