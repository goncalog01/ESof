package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.Dashboard;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.WeeklyScore;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.dto.WeeklyScoreDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.repository.DashboardRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.repository.WeeklyScoreRepository;

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Service
public class WeeklyScoreService {

    @Autowired
    private WeeklyScoreRepository weeklyScoreRepository;

    @Autowired
    private DashboardRepository dashboardRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public WeeklyScoreDto createWeeklyScore(Integer dashboardId) {
        if (dashboardId == null) {
            throw new TutorException(DASHBOARD_NOT_FOUND);
        }

        Dashboard dashboard = dashboardRepository.findById(dashboardId).orElseThrow(() -> new TutorException(DASHBOARD_NOT_FOUND, dashboardId));

        TemporalAdjuster weekSunday = TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY);
        LocalDate week = DateHandler.now().with(weekSunday).toLocalDate();

        WeeklyScore weeklyScore = new WeeklyScore(dashboard, week);

        weeklyScoreRepository.save(weeklyScore);

        return new WeeklyScoreDto(weeklyScore);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void removeWeeklyScore(Integer weeklyScoreId) {
        if (weeklyScoreId == null) {
            throw new TutorException(WEEKLY_SCORE_NOT_FOUND);
        }

        WeeklyScore weeklyScore = weeklyScoreRepository.findById(weeklyScoreId).orElseThrow(() -> new TutorException(WEEKLY_SCORE_NOT_FOUND, weeklyScoreId));

        TemporalAdjuster weekSunday = TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY);
        LocalDate currentWeek = DateHandler.now().with(weekSunday).toLocalDate();

        if (weeklyScore.getWeek().isEqual(currentWeek)) {
            throw new TutorException(CANNOT_REMOVE_WEEKLY_SCORE);
        }

        weeklyScore.remove();
        weeklyScoreRepository.delete(weeklyScore);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<WeeklyScoreDto> getWeeklyScores(Integer dashboardId) {
        if (dashboardId == null) {
            throw new TutorException(DASHBOARD_NOT_FOUND);
        }

        Dashboard dashboard = dashboardRepository.findById(dashboardId).orElseThrow(() -> new TutorException(DASHBOARD_NOT_FOUND, dashboardId));

        List<WeeklyScoreDto> res = dashboard.getWeeklyScores().stream().map(WeeklyScoreDto::new).sorted(Comparator.comparing(WeeklyScoreDto::getWeek)).collect(Collectors.toList());
        Collections.reverse(res);
        return res;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateWeeklyScores(Integer dashboardId) {
        if (dashboardId == null) {
            throw new TutorException(DASHBOARD_NOT_FOUND);
        }

        Dashboard dashboard = dashboardRepository.findById(dashboardId).orElseThrow(() -> new TutorException(DASHBOARD_NOT_FOUND, dashboardId));

        TemporalAdjuster weekSunday = TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY);
        LocalDate week = DateHandler.now().with(weekSunday).toLocalDate();

        dashboard.getWeeklyScores().stream()
                .filter(WeeklyScore::isClosed)
                .forEach(weeklyScore -> {
            weeklyScore.remove();
            weeklyScoreRepository.delete(weeklyScore);
        });

        Set<LocalDate> weeks = dashboard.getStudent().getQuizAnswers().stream().map(student -> student.getAnswerDate().toLocalDate()).collect(Collectors.toSet());

        dashboard.getWeeklyScores().forEach(ws -> weeks.remove(ws.getWeek()));

        weeks.forEach(answerWeek -> weeklyScoreRepository.save(new WeeklyScore(dashboard, answerWeek)));

        dashboard.getWeeklyScores().forEach(weeklyScore -> {
            weeklyScore.computeStatistics();
            weeklyScoreRepository.save(weeklyScore);
        });

        Optional<WeeklyScore> weeklyScore = dashboard.getWeeklyScores().stream().filter(ws -> ws.getWeek()
                .isEqual(week)).findFirst();

        if (weeklyScore.isEmpty()) {
            WeeklyScore ws = new WeeklyScore(dashboard, week);
            ws.computeStatistics();
            weeklyScoreRepository.save(ws);
        }
    }
}