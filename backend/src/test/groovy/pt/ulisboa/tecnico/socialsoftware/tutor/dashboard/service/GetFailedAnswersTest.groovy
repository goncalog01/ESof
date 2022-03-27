package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.Dashboard
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.Student
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@DataJpaTest
class GetFailedAnswersTest extends FailedAnswersSpockTest {
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
    def "get failed answer answered=#answered"() {
        given:
        def questionAnswer = answerQuiz(answered, false, true, quizQuestion, quiz)
        createFailedAnswer(questionAnswer, LocalDateTime.now())

        when:
        def failedAnswerDtos = failedAnswerService.getFailedAnswers(dashboard.getId())

        then: "the return statement contains one failed answer"
        failedAnswerDtos.size() == 1
        def failedAnswerDto = failedAnswerDtos.get(0)
        failedAnswerDto.getId() != 0
        failedAnswerDto.getAnswered() == answered
        LocalDateTime.parse(failedAnswerDto.getCollected(), DateTimeFormatter.ISO_DATE_TIME).isAfter(DateHandler.now().minusMinutes(1))
        failedAnswerDto.getQuestionAnswerDto().getQuestion().getId() === questionAnswer.getQuestion().getId()
        if (answered) failedAnswerDto.getQuestionAnswerDto().getAnswerDetails().getOption().getId() == questionAnswer.getAnswerDetails().getOption().getId()

        where:
        answered << [true, false]
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}