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

import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.dto.FailedAnswerDto;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UpdateFailedAnswersWebServiceIT extends FailedAnswersSpockTest {
    @LocalServerPort
    private int port

    def response
    def quiz
    def quizQuestion
    def questionAnswer

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

    }

    def "student updates failed answers"() {
        given: "student is logged in"
        createdUserLogin(USER_1_USERNAME, USER_1_PASSWORD)

        when: "the web service is invoked"
        response = restClient.put(
                path: '/students/dashboards/' + dashboard.getId() + '/failedanswers',
                requestContentType: 'application/json'
        )

        then: "the request returns 200"
        response != null
        response.status == 200

        and: "the repository contains the failed answer"
        failedAnswerRepository.findAll().size() == 1

        and: "the failed answer must be the same that was created"
        def addedFailedAnswer = failedAnswerRepository.findAll().get(0)

        addedFailedAnswer.getQuestionAnswer().getId() == questionAnswer.getId()
        addedFailedAnswer.getQuestionAnswer().getTimeTaken() == questionAnswer.getTimeTaken()

        cleanup:
        failedAnswerRepository.deleteAll()
        questionAnswerRepository.deleteById(questionAnswer.getId())
    }

    def "teacher cant update student's failed answers"() {
        given: "demo teacher"
        demoTeacherLogin()

        when: "the web service is invoked"
        response = restClient.put(
                path: '/students/dashboards/' + dashboard.getId() + '/failedanswers',
                requestContentType: 'application/json'
        )

        then: "the request returns 403"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN
    }

    def "student cant update another students failed answers"() {

    }

    def cleanup() {
        userRepository.deleteById(student.getId())
        courseExecutionRepository.deleteById(externalCourseExecution.getId())
        courseRepository.deleteById(externalCourseExecution.getCourse().getId())
    }

}