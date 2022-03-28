package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.Dashboard
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.domain.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.Student
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler
import spock.lang.Unroll

import java.time.LocalDateTime

@DataJpaTest
class UpdateFailedAnswersTest extends FailedAnswersSpockTest {
    def quiz
    def quizQuestion

    def setup() {
        createExternalCourseAndExecution()

        student = new Student(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, false, AuthUser.Type.TECNICO)
        student.addCourse(externalCourseExecution)
        userRepository.save(student)

        dashboard = new Dashboard(externalCourseExecution, student)
        dashboardRepository.save(dashboard)

        quiz = createQuiz(1)
        quizQuestion = createQuestion(1, quiz)
    }

    @Unroll
    def "create failed answer answered=#answered"() {
        given:
        def questionAnswer = answerQuiz(answered, false, true, quizQuestion, quiz)

        when:
        failedAnswerService.updateFailedAnswers(dashboard.getId(), null, null)

        then:
        failedAnswerRepository.count() == 1L
        def failedAnswer = failedAnswerRepository.findAll().get(0)
        failedAnswer.getId() != 0
        failedAnswer.getDashboard().id === dashboard.getId()
        failedAnswer.getQuestionAnswer().getId() == questionAnswer.getId()
        failedAnswer.getCollected().isAfter(DateHandler.now().minusMinutes(1))
        failedAnswer.getAnswered() == answered
        and:
        def dashboard = dashboardRepository.getById(dashboard.getId())
        dashboard.getFailedAnswers().contains(failedAnswer)
        dashboard.getLastCheckFailedAnswers().isAfter(DateHandler.now().minusSeconds(1))

        where:
        answered << [true, false]
    }

    @Unroll
    def "does not create failed answer with correct=#correct and completed=#completed" () {
        given:
        answerQuiz(true, correct, completed, quizQuestion, quiz)

        when:
        failedAnswerService.updateFailedAnswers(dashboard.getId(), null, null)

        then:
        failedAnswerRepository.findAll().size() == 0L

        where:
        completed | correct
        false     | false
        false     | true
        true      | true
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
