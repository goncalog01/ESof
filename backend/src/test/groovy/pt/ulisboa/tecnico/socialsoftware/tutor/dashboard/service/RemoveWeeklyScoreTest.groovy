package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.Dashboard
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.WeeklyScore
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.Student
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler
import spock.lang.Unroll

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjuster
import java.time.temporal.TemporalAdjusters

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.WEEKLY_SCORE_NOT_FOUND

@DataJpaTest
class RemoveWeeklyScoreTest extends SpockTest {
    def student
    def dashboard

    def setup() {
        createExternalCourseAndExecution()

        student = new Student(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, false, AuthUser.Type.EXTERNAL)
        student.addCourse(externalCourseExecution)
        userRepository.save(student)

        dashboard = new Dashboard(externalCourseExecution, student)
        dashboardRepository.save(dashboard)
    }

    def "remove past weekly score"() {
        given:
        TemporalAdjuster weekSunday = TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY);
        LocalDate week = DateHandler.now().minusDays(30).with(weekSunday).toLocalDate();
        WeeklyScore weeklyScore = new WeeklyScore(dashboard, week)
        weeklyScoreRepository.save(weeklyScore)

        when:
        weeklyScoreService.removeWeeklyScore(weeklyScore.getId())

        then:
        weeklyScoreRepository.count() == 0L
        and:
        def dashboard = dashboardRepository.getById(dashboard.getId())
        dashboard.getWeeklyScores().size() == 0
    }

    def "cannot remove current weekly score"() {
        given:
        TemporalAdjuster weekSunday = TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY);
        LocalDate week = DateHandler.now().with(weekSunday).toLocalDate();
        WeeklyScore weeklyScore = new WeeklyScore(dashboard, week)
        weeklyScoreRepository.save(weeklyScore)

        when:
        weeklyScoreService.removeWeeklyScore(weeklyScore.getId())

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.CANNOT_REMOVE_WEEKLY_SCORE
        and:
        weeklyScoreRepository.count() == 1
    }

    @Unroll
    def "cannot remove WeeklyScore with invalid weeklyScore=#weeklyScoreId"() {
        when:
        weeklyScoreService.removeWeeklyScore(weeklyScoreId)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == errorMessage

        where:
        weeklyScoreId || errorMessage
        null          || WEEKLY_SCORE_NOT_FOUND
        100           || WEEKLY_SCORE_NOT_FOUND
    }

    def "delete a weekly score from a set of weekly scores with the same percentage"() {
        given:
        def weeklyScore1 = new WeeklyScore(dashboard, LocalDate.of(2021, 12, 23))
        weeklyScore1.setPercentageCorrect(50)
        def weeklyScore2 = new WeeklyScore(dashboard, LocalDate.of(2021, 11, 23))
        weeklyScore2.setPercentageCorrect(50)

        when:
        weeklyScoreRepository.save(weeklyScore1)
        weeklyScoreRepository.save(weeklyScore2)
        weeklyScoreService.removeWeeklyScore(weeklyScore1.getId())

        then:
        weeklyScoreRepository.count() == 1L
        weeklyScoreRepository.findAll().get(0).getSamePercentage().getSameWeeklyScores().isEmpty()

    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}