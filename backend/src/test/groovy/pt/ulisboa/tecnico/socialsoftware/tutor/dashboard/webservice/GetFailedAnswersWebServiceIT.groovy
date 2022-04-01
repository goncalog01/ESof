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
class GetFailedAnswersWebServiceIT extends FailedAnswersSpockTest {
    @LocalServerPort
    private int port

    def response
    def quiz
    def quizQuestion

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
    }

    def "student gets failed answers"() {
        given: "login student create 3 failed answers"
        createdUserLogin(USER_1_USERNAME, USER_1_PASSWORD)
        def quiz2 = createQuiz(2)
        def quiz3 = createQuiz(3)

        def quizQuestion2 = createQuestion(2, quiz2)
        def quizQuestion3 = createQuestion(3, quiz3)

        def questionAnswer = answerQuiz(true, false, true, quizQuestion, quiz)
        def questionAnswer2 = answerQuiz(true, false, true, quizQuestion2, quiz2)
        def questionAnswer3 = answerQuiz(true, false, true, quizQuestion3, quiz3)

        def failedAnswer1Dto = failedAnswerService.createFailedAnswer(dashboard.getId(), questionAnswer.getId())
        def failedAnswer2Dto = failedAnswerService.createFailedAnswer(dashboard.getId(), questionAnswer2.getId())
        def failedAnswer3Dto = failedAnswerService.createFailedAnswer(dashboard.getId(), questionAnswer3.getId())

        when: "the web service is invoked"
        response = restClient.get(
                path: '/students/dashboards/' + dashboard.getId() + '/failedanswers',
                requestContentType: 'application/json'
        )

        then: "the request returns 200"
        response != null
        response.status == 200

        and: "the repository contains both failed answers"
        failedAnswerRepository.findAll().size() == 3

        and: "dashboard contains same failed answers"
        def info = response.data
        info.get(0).questionAnswerDto.question.id == failedAnswer3Dto.getQuestionAnswerDto().getQuestion().getId()
        info.get(1).questionAnswerDto.question.id == failedAnswer2Dto.getQuestionAnswerDto().getQuestion().getId()
        info.get(2).questionAnswerDto.question.id == failedAnswer1Dto.getQuestionAnswerDto().getQuestion().getId()

        info.get(0).questionAnswerDto.answerDetails.option.id == failedAnswer3Dto.getQuestionAnswerDto().getAnswerDetails().getOption().getId()
        info.get(1).questionAnswerDto.answerDetails.option.id == failedAnswer2Dto.getQuestionAnswerDto().getAnswerDetails().getOption().getId()
        info.get(2).questionAnswerDto.answerDetails.option.id == failedAnswer1Dto.getQuestionAnswerDto().getAnswerDetails().getOption().getId()

        cleanup:
        failedAnswerRepository.deleteAll()
        questionAnswerRepository.deleteById(questionAnswer.getId())
        questionAnswerRepository.deleteById(questionAnswer2.getId())
        questionAnswerRepository.deleteById(questionAnswer3.getId())
    }

    def "teacher can't get student's failed answers"() {
        given: "demo teacher"
        demoTeacherLogin()

        when: "the web service is invoked"
        response = restClient.get(
                path: '/students/dashboards/' + dashboard.getId() + '/failedanswers',
                requestContentType: 'application/json'
        )

        then: "the request returns 403"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN
    }

    def "student can't get another student's failed answers"() {
        given: "another student"
        def student2 = new Student(USER_2_NAME, USER_2_USERNAME, USER_2_EMAIL, false, AuthUser.Type.EXTERNAL)
        student2.authUser.setPassword(passwordEncoder.encode(USER_2_PASSWORD))
        student2.addCourse(externalCourseExecution)
        userRepository.save(student2)
        createdUserLogin(USER_2_USERNAME, USER_2_PASSWORD)

        and: "another dashboard for new student"
        def dashboard2 = new Dashboard(externalCourseExecution, student2)
        dashboardRepository.save(dashboard2)

        when: "the web service is invoked"
        response = restClient.get(
                path: '/students/dashboards/' + dashboard.getId() + '/failedanswers',
                requestContentType: 'application/json'
        )

        then: "the request returns 403"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN

        cleanup:
        userRepository.deleteById(student2.getId())
    }

    def cleanup() {
        userRepository.deleteById(student.getId())
        courseExecutionRepository.deleteById(externalCourseExecution.getId())
        courseRepository.deleteById(externalCourseExecution.getCourse().getId())
    }

}