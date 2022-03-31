package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.webservice

import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.apache.http.HttpStatus
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.Dashboard
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.service.FailedAnswersSpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.Student

import java.time.LocalDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RemoveFailedAnswersWebServiceIT extends FailedAnswersSpockTest {
    @LocalServerPort
    private int port

    def response
    def courseExecution
    def quiz
    def quizQuestion
    def questionAnswer
    def failedAnswer

    def setup() {
        given:
        restClient = new RESTClient("http://localhost:" + port)
        and:
        createExternalCourseAndExecution()
        and:
        student = new Student(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, false, AuthUser.Type.EXTERNAL)
        student.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        student.addCourse(externalCourseExecution)
        userRepository.save(student)
        and:
        dashboard = new Dashboard(externalCourseExecution, student)
        dashboardRepository.save(dashboard)
        and:
        quiz = createQuiz(1)
        quizQuestion = createQuestion(1, quiz)
        questionAnswer = answerQuiz(true, false, true, quizQuestion, quiz)
        failedAnswer = createFailedAnswer(questionAnswer, LocalDateTime.now().minusDays(8))
    }

    def "student gets failed answers from dashboard then removes it"() {
        given: "student is logged in"
        createdUserLogin(USER_1_USERNAME, USER_1_PASSWORD)

        when: "the web service is invoked"
        response = restClient.delete(
                path: '/students/failedanswers/' + failedAnswer.getId(),
                requestContentType: 'application/json'
        )

        then: "the request returns 200"
        response != null
        response.status == 200

        and: "there should not be any failed answers in the repository"
        failedAnswerRepository.findAll().size() == 0

        and: "the student should not have any failed answers in his dashboard"
        //dashboardRepository.findAll().get(0).getFailedAnswers().findAll().size() == 0
        
    }

    def "teacher can't get remove student's failed answers from dashboard"() {
        given: "demo teacher"
        demoTeacherLogin()

        when: "the web service is invoked"
        response = restClient.delete(
                path: '/students/failedanswers/' + failedAnswer.getId(),
                requestContentType: 'application/json'
        )

        then: "the request returns 403"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN
    }

    def "student can't get another student's failed answers from dashboard"() {

    }

    def cleanup () {
        failedAnswerRepository.deleteAll()
        questionAnswerRepository.deleteById(questionAnswer.getId())
        userRepository.deleteById(student.getId())
        courseExecutionRepository.deleteById(externalCourseExecution.getId())
        courseRepository.deleteById(externalCourseExecution.getCourse().getId())
    }

}