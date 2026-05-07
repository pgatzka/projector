package io.github.pgatzka.projector.rest.service;

import io.github.pgatzka.projector.data.service.ActivityDataService;
import io.github.pgatzka.projector.jooq.enums.ActivityAction;
import io.github.pgatzka.projector.jooq.tables.pojos.Activity;
import io.github.pgatzka.projector.jooq.tables.pojos.Issue;
import io.github.pgatzka.projector.rest.dto.ActivityDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ActivityService {

    private final ActivityDataService activityData;
    private final IssueService issueService;

    public ActivityService(ActivityDataService activityData, IssueService issueService) {
        this.activityData = activityData;
        this.issueService = issueService;
    }

    public Activity emit(UUID issueId, ActivityAction action, Map<String, Object> payload) {
        return activityData.emit(issueId, action, payload);
    }

    @Transactional(readOnly = true)
    public List<ActivityDto> listByIssue(String projectKey, int issueNumber) {
        Issue issue = issueService.findByProjectKeyAndNumber(projectKey, issueNumber);
        return activityData.findByIssueId(issue.getId()).stream()
            .map(ActivityDto::of)
            .toList();
    }
}
