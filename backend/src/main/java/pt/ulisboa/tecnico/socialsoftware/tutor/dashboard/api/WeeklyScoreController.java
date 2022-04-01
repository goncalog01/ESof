package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.WeeklyScoreService;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.dto.WeeklyScoreDto;
import java.util.List;

@RestController
public class WeeklyScoreController {

    Logger logger = LoggerFactory.getLogger(WeeklyScoreController.class);

    @Autowired
    private WeeklyScoreService weeklyScoreService;

    public WeeklyScoreController(WeeklyScoreService weeklyScoreService) {
        this.weeklyScoreService = weeklyScoreService;
    }

    @GetMapping("/students/dashboards/{dashboardId}/weeklyscores")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#dashboardId, 'DASHBOARD.ACCESS')")
    public List<WeeklyScoreDto> getWeeklyScores(@PathVariable int dashboardId) {
        return weeklyScoreService.getWeeklyScores(dashboardId);
    }

    @DeleteMapping("/students/dashboards/weeklyscores/{weeklyScoreId}")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#weeklyScoreId, 'WEEKLY_SCORE.ACCESS')")
    public void deleteWeeklyScore(@PathVariable int weeklyScoreId) {
        weeklyScoreService.removeWeeklyScore(weeklyScoreId);
    }

    @PutMapping("/students/dashboards/{dashboardId}/weeklyscores")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#dashboardId, 'DASHBOARD.ACCESS')")
    public List<WeeklyScoreDto> updateWeeklyScores(@PathVariable int dashboardId) {
        return weeklyScoreService.updateWeeklyScores(dashboardId);
    }

}
