package io.github.pgatzka.projector.unit;

import io.github.pgatzka.projector.data.service.ActivityDataService;
import io.github.pgatzka.projector.jooq.enums.ActivityAction;
import io.github.pgatzka.projector.jooq.tables.pojos.Activity;
import io.github.pgatzka.projector.jooq.tables.pojos.Issue;
import io.github.pgatzka.projector.rest.dto.ActivityDto;
import io.github.pgatzka.projector.rest.service.ActivityService;
import io.github.pgatzka.projector.rest.service.IssueService;
import org.jooq.JSONB;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ActivityServiceTest {

    private static final String PROJECT_KEY = "ENG";
    private static final int ISSUE_NUMBER = 1;

    private ActivityDataService activityData;
    private IssueService issueService;
    private ActivityService service;

    @BeforeEach
    void setUp() {
        activityData = mock(ActivityDataService.class);
        issueService = mock(IssueService.class);
        service = new ActivityService(activityData, issueService);
    }

    @Test
    void emit_delegatesToDataServiceWithCorrectArgs() {
        UUID issueId = UUID.randomUUID();
        Map<String, Object> payload = Map.of("before", "todo", "after", "in_progress");
        Activity returned = new Activity();
        returned.setId(UUID.randomUUID());
        when(activityData.emit(eq(issueId), eq(ActivityAction.status_changed), eq(payload))).thenReturn(returned);

        Activity result = service.emit(issueId, ActivityAction.status_changed, payload);

        assertThat(result).isSameAs(returned);
        verify(activityData).emit(issueId, ActivityAction.status_changed, payload);
    }

    @Test
    void listByIssue_resolvesIssueAndReturnsDtosInOrder() {
        UUID issueId = UUID.randomUUID();
        Issue issue = new Issue();
        issue.setId(issueId);
        when(issueService.findByProjectKeyAndNumber(PROJECT_KEY, ISSUE_NUMBER)).thenReturn(issue);

        Activity a1 = new Activity();
        a1.setId(UUID.randomUUID());
        a1.setIssueId(issueId);
        a1.setAction(ActivityAction.issue_created);
        a1.setPayload(JSONB.valueOf("{}"));
        a1.setCreatedAt(OffsetDateTime.now().minusMinutes(2));
        a1.setCreatedBy("user:abc");

        Activity a2 = new Activity();
        a2.setId(UUID.randomUUID());
        a2.setIssueId(issueId);
        a2.setAction(ActivityAction.status_changed);
        a2.setPayload(JSONB.valueOf("{\"before\":\"todo\",\"after\":\"in_progress\"}"));
        a2.setCreatedAt(OffsetDateTime.now().minusMinutes(1));
        a2.setCreatedBy("user:abc");

        when(activityData.findByIssueId(issueId)).thenReturn(List.of(a1, a2));

        List<ActivityDto> result = service.listByIssue(PROJECT_KEY, ISSUE_NUMBER);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).action()).isEqualTo("issue_created");
        assertThat(result.get(0).payload()).isEmpty();
        assertThat(result.get(1).action()).isEqualTo("status_changed");
        assertThat(result.get(1).payload()).containsEntry("before", "todo").containsEntry("after", "in_progress");
    }
}
