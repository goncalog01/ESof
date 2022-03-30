package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.DifficultQuestionService;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.dto.DifficultQuestionDto;

import java.util.List;

@RestController
public class DifficultQuestionController {
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private DifficultQuestionService difficultQuestionService;

    DifficultQuestionController(DifficultQuestionService difficultQuestionService) {
        this.difficultQuestionService = difficultQuestionService;
    }

    @GetMapping("/students/dashboards/{dashboardId}/difficultquestions")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#dashboardId, 'DASHBOARD.ACCESS')")
    public List<DifficultQuestionDto> getDifficultQuestions(@PathVariable int dashboardId) {
        return this.difficultQuestionService.getDifficultQuestions(dashboardId);
    }

    @GetMapping("/students/dashboards/{dashboardId}/updatedifficultquestion")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#dashboardId, 'DASHBOARD.ACCESS')")
    public void updateDifficultQuestions(@PathVariable int dashboardId) {
        this.difficultQuestionService.updateDifficultQuestions(dashboardId);
    }
}
