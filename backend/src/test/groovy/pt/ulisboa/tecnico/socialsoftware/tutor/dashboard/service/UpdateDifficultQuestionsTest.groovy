package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.MultipleChoiceAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.Dashboard
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.DifficultQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.MultipleChoiceQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.Student
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler
import spock.lang.Unroll

import java.time.LocalDateTime

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.DASHBOARD_NOT_FOUND

@DataJpaTest
class UpdateDifficultQuestionsTest extends SpockTest {
    def student
    def dashboard
    def question
    def optionOK
    def optionKO
    def quiz
    def quizQuestion
    def now

    def setup() {
        given:
        createExternalCourseAndExecution()
        and:
        student = new Student(USER_1_NAME, USER_1_PASSWORD, USER_1_EMAIL, false, AuthUser.Type.EXTERNAL)
        student.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        student.addCourse(externalCourseExecution)
        userRepository.save(student)
        and:
        now = DateHandler.now()
        and:
        question = new Question()
        question.setKey(1)
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
        and:
        optionOK = new Option()
        optionOK.setContent(OPTION_1_CONTENT)
        optionOK.setCorrect(true)
        optionOK.setSequence(0)
        optionOK.setQuestionDetails(questionDetails)
        optionRepository.save(optionOK)
        and:
        optionKO = new Option()
        optionKO.setContent(OPTION_1_CONTENT)
        optionKO.setCorrect(false)
        optionKO.setSequence(1)
        optionKO.setQuestionDetails(questionDetails)
        optionRepository.save(optionKO)
        and:
        quiz = new Quiz()
        quiz.setCourseExecution(externalCourseExecution)
        quiz.setAvailableDate(now.minusHours(1))
        quiz.setConclusionDate(now)
        quizRepository.save(quiz)
        and:
        quizQuestion = new QuizQuestion()
        quizQuestion.setQuiz(quiz)
        quizQuestion.setQuestion(question)
        quizQuestionRepository.save(quizQuestion)
        and:
        dashboard = new Dashboard(externalCourseExecution, student)
        dashboardRepository.save(dashboard)
    }

    def "create one difficult question that does not exist"() {
        given:
        def quizAnswer = new QuizAnswer()
        quizAnswer.setAnswerDate(now.minusMinutes(1))
        quizAnswer.setQuiz(quiz)
        quizAnswer.setStudent(student)
        quizAnswerRepository.save(quizAnswer)
        and:
        def questionAnswer = new QuestionAnswer()
        questionAnswer.setQuizQuestion(quizQuestion)
        questionAnswer.setQuizAnswer(quizAnswer)
        questionAnswerRepository.save(questionAnswer)

        when:
        difficultQuestionService.updateDifficultQuestions(dashboard.getId())

        then:
        difficultQuestionRepository.count() == 1L
        and:
        def difficultQuestion = difficultQuestionRepository.findAll().get(0)
        difficultQuestion.getDashboard() == dashboard
        difficultQuestion.getQuestion() == question
        difficultQuestion.isRemoved() == false
        difficultQuestion.getRemovedDate() == null
        difficultQuestion.getPercentage() == 0
        difficultQuestion.getDashboard().getLastCheckDifficultQuestions().isAfter(now)
    }

    def "delete and create a difficult question that continues to be difficult"() {
        given:
        def difficultQuestion = new DifficultQuestion(dashboard, question, 24)
        difficultQuestionRepository.save(difficultQuestion)
        and:
        def quizAnswer = new QuizAnswer()
        quizAnswer.setAnswerDate(now.minusMinutes(1))
        quizAnswer.setQuiz(quiz)
        quizAnswer.setStudent(student)
        quizAnswerRepository.save(quizAnswer)
        and:
        def questionAnswer = new QuestionAnswer()
        questionAnswer.setQuizQuestion(quizQuestion)
        questionAnswer.setQuizAnswer(quizAnswer)
        questionAnswerRepository.save(questionAnswer)

        when:
        difficultQuestionService.updateDifficultQuestions(dashboard.getId())

        then:
        difficultQuestionRepository.count() == 1L
        and:
        def result = difficultQuestionRepository.findAll().get(0)
        result.getId() != difficultQuestion.getId()
        result.getQuestion() == question
        result.getPercentage() == 0
    }

    def "delete difficult question that is not difficult anymore"() {
        given:
        def difficultQuestion = new DifficultQuestion(dashboard, question, 24)
        difficultQuestionRepository.save(difficultQuestion)

        when:
        difficultQuestionService.updateDifficultQuestions(dashboard.getId())

        then:
        difficultQuestionRepository.count() == 0L
    }


    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}