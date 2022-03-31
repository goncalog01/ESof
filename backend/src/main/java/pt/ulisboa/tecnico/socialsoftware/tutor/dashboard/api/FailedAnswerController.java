package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.FailedAnswerService;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.dto.FailedAnswerDto;

import java.security.Principal;
import java.util.Collection;

@RestController
public class FailedAnswerController {
    private static final Logger logger = LoggerFactory.getLogger(FailedAnswerController.class);

    @Autowired
    private FailedAnswerService failedAnswerService;

    FailedAnswerController(FailedAnswerService failedAnswerService) { this.failedAnswerService = failedAnswerService; }

    @GetMapping("/students/dashboards/{dashboardId}/failedanswers")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#dashboardId, 'DASHBOARD.ACCESS')")
    public Collection<FailedAnswerDto> getFailedAnswers(@PathVariable int dashboardId) {
        return this.failedAnswerService.getFailedAnswers(dashboardId);
    }
    
    @PutMapping("/students/dashboards/{dashboardId}/failedanswers")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public void updateFailedAnswers(@PathVariable int dashboardId) {
        this.failedAnswerService.updateFailedAnswers(dashboardId, null, null);
    }
}
