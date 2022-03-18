package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.service

import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.Dashboard
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.Student
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler
import spock.lang.Unroll

@DataJpaTest
class RemoveFailedAnswerTest extends FailedAnswersSpockTest {

    def setup() {
        createExternalCourseAndExecution()

        student = new Student(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, false, AuthUser.Type.TECNICO)
        student.addCourse(externalCourseExecution)
        userRepository.save(student)

        dashboard = new Dashboard(externalCourseExecution, student)
        dashboardRepository.save(dashboard)
    }

    @Unroll
    def 'remove a failed answer minusDays #minusDays' () {
        given:
        def quiz = createQuiz(1)
        def quizQuestion = createQuestion(1, quiz)
        def questionAnswer = answerQuiz(true, false, true, quizQuestion, quiz)
        def failedAnswer = createFailedAnswer(questionAnswer, DateHandler.now().minusDays(minusDays))

        when:
        failedAnswerService.removeFailedAnswer(failedAnswer.getId())

        then:
        failedAnswerRepository.findAll().size() == 0L
        and:
        def dashboard = dashboardRepository.findById(dashboard.getId()).get()
        dashboard.getStudent().getId() === student.getId()
        dashboard.getCourseExecution().getId() === externalCourseExecution.getId()
        dashboard.getFailedAnswers().findAll().size() == 0L

        where:
        minusDays << [8, 5]
    }

    @Unroll
    def 'cannot remove a failed answer minusDays #minusDays' () {
        given:
        def quiz = createQuiz(1)
        def quizQuestion = createQuestion(1, quiz)
        def questionAnswer = answerQuiz(true, false, true, quizQuestion, quiz)
        def failedAnswer = createFailedAnswer(questionAnswer, DateHandler.now().minusDays(minusDays))

        when:
        failedAnswerService.removeFailedAnswer(failedAnswer.getId())

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.CANNOT_REMOVE_FAILED_ANSWER
        and:
        failedAnswerRepository.findAll().size() == 1L

        where:
        minusDays << [0, 4]
    }

    @Unroll
    def "cannot remove failed answers with invalid failedAnswerId=#failedAnswerId" () {
        when:
        failedAnswerService.removeFailedAnswer(failedAnswerId)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == errorMessage

        where:
        failedAnswerId  || errorMessage
        100             || ErrorMessage.FAILED_ANSWER_NOT_FOUND
        -1              || ErrorMessage.FAILED_ANSWER_NOT_FOUND
    }

    @Unroll
    def "remove one failed answer from a set of failed answers"(){

        given:
        def quiz = createQuiz(1)
        def quizQuestion = createQuestion(1, quiz)
        def questionAnswer = answerQuiz(true, false, true, quizQuestion, quiz)
        def removedFailedAnswer = createFailedAnswer(questionAnswer, DateHandler.now().minusDays(5))

        for (int i in 0..numQuestions-1){
            def quiz1 = createQuiz(1)
            def questionAnswer1 = answerQuiz(true, false, true, quizQuestion, quiz1)
            createFailedAnswer(questionAnswer1, DateHandler.now().minusDays(5))
        }

        when:
        failedAnswerService.removeFailedAnswer(removedFailedAnswer.getId())

        then:
        failedAnswerRepository.count() == (long) numQuestions
        (removedFailedAnswer in failedAnswerRepository.findAll()) == false

        def results = []
        for (int i in 0..numQuestions-1){
            results.add(failedAnswerRepository.findAll().get(i))
        }

        for (int j in 0..numQuestions-1){
            results[j].getSameQuestion().getSameFailedAnswers().size() == (long) numQuestions-1
            (removedFailedAnswer in results[j].getSameQuestion().getSameFailedAnswers()) == false
        }

        where:
        numQuestions << [2, 5, 10, 50]

    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
