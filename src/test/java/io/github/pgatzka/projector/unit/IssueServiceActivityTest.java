package io.github.pgatzka.projector.unit;

import io.github.pgatzka.projector.data.service.IssueDataService;
import io.github.pgatzka.projector.data.service.IssueLabelDataService;
import io.github.pgatzka.projector.data.service.LabelDataService;
import io.github.pgatzka.projector.data.service.ProjectDataService;
import io.github.pgatzka.projector.jooq.enums.ActivityAction;
import io.github.pgatzka.projector.jooq.enums.IssuePriority;
import io.github.pgatzka.projector.jooq.enums.IssueStatus;
import io.github.pgatzka.projector.jooq.tables.pojos.Issue;
import io.github.pgatzka.projector.jooq.tables.pojos.Project;
import io.github.pgatzka.projector.rest.dto.CreateIssueRequest;
import io.github.pgatzka.projector.rest.dto.UpdateIssueRequest;
import io.github.pgatzka.projector.rest.service.ActivityService;
import io.github.pgatzka.projector.rest.service.IssueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class IssueServiceActivityTest {

    private static final String KEY = "ENG";

    private ProjectDataService projectData;
    private IssueDataService issueData;
    private LabelDataService labelData;
    private IssueLabelDataService issueLabelData;
    private ActivityService activityService;
    private IssueService service;

    private UUID projectId;
    private UUID issueId;

    @BeforeEach
    void setUp() {
        projectData = mock(ProjectDataService.class);
        issueData = mock(IssueDataService.class);
        labelData = mock(LabelDataService.class);
        issueLabelData = mock(IssueLabelDataService.class);
        activityService = mock(ActivityService.class);
        service = new IssueService(projectData, issueData, labelData, issueLabelData, activityService);

        projectId = UUID.randomUUID();
        issueId = UUID.randomUUID();
    }

    @Test
    void create_emitsExactlyOneIssueCreated_withEmptyPayload() {
        Project project = new Project();
        project.setId(projectId);
        project.setKey(KEY);
        when(projectData.findByKey(KEY)).thenReturn(Optional.of(project));
        when(projectData.claimNextIssueNumber(projectId)).thenReturn(1);
        when(issueData.create(any(Issue.class))).thenAnswer(inv -> {
            Issue arg = inv.getArgument(0);
            arg.setId(issueId);
            return arg;
        });

        service.create(KEY, new CreateIssueRequest("hello", null, null, null, null, null));

        ArgumentCaptor<Map<String, Object>> payload = payloadCaptor();
        verify(activityService, times(1))
            .emit(eq(issueId), eq(ActivityAction.issue_created), payload.capture());
        assertThat(payload.getValue()).isEmpty();
    }

    @Test
    void update_withNoChangedFields_emitsZeroRows() {
        Issue existing = baseIssue();
        stubFindIssue(existing);
        when(issueData.update(any(Issue.class))).thenAnswer(inv -> inv.getArgument(0));

        service.update(KEY, 1, new UpdateIssueRequest(null, null, null, null, null));

        verifyNoInteractions(activityService);
    }

    @Test
    void update_withChangedStatus_emitsOneStatusChangedRow() {
        Issue existing = baseIssue();
        existing.setStatus(IssueStatus.todo);
        stubFindIssue(existing);
        when(issueData.update(any(Issue.class))).thenAnswer(inv -> inv.getArgument(0));

        service.update(KEY, 1, new UpdateIssueRequest(null, null, IssueStatus.in_progress, null, null));

        ArgumentCaptor<Map<String, Object>> payload = payloadCaptor();
        verify(activityService, times(1))
            .emit(eq(issueId), eq(ActivityAction.status_changed), payload.capture());
        assertThat(payload.getValue()).containsEntry("before", "todo").containsEntry("after", "in_progress");
    }

    @Test
    void update_withTwoChangedFields_emitsTwoRowsInDeterministicOrder() {
        Issue existing = baseIssue();
        existing.setStatus(IssueStatus.todo);
        existing.setPriority(IssuePriority.low);
        stubFindIssue(existing);
        when(issueData.update(any(Issue.class))).thenAnswer(inv -> inv.getArgument(0));

        service.update(KEY, 1, new UpdateIssueRequest(null, null, IssueStatus.done, IssuePriority.high, null));

        InOrder order = inOrder(activityService);
        order.verify(activityService).emit(eq(issueId), eq(ActivityAction.status_changed), any());
        order.verify(activityService).emit(eq(issueId), eq(ActivityAction.priority_changed), any());
        verify(activityService, times(2)).emit(eq(issueId), any(ActivityAction.class), any());
    }

    @Test
    void update_titleEqualsOldTitle_emitsNoTitleEdited() {
        Issue existing = baseIssue();
        existing.setTitle("same title");
        stubFindIssue(existing);
        when(issueData.update(any(Issue.class))).thenAnswer(inv -> inv.getArgument(0));

        service.update(KEY, 1, new UpdateIssueRequest("same title", null, null, null, null));

        verify(activityService, never())
            .emit(any(UUID.class), eq(ActivityAction.title_edited), any());
    }

    @Test
    void update_dueDateChanged_emitsIsoFormattedPayload() {
        Issue existing = baseIssue();
        existing.setDueDate(LocalDate.of(2026, 6, 1));
        stubFindIssue(existing);
        when(issueData.update(any(Issue.class))).thenAnswer(inv -> inv.getArgument(0));

        service.update(KEY, 1, new UpdateIssueRequest(null, null, null, null, LocalDate.of(2026, 7, 15)));

        ArgumentCaptor<Map<String, Object>> payload = payloadCaptor();
        verify(activityService).emit(eq(issueId), eq(ActivityAction.due_date_changed), payload.capture());
        assertThat(payload.getValue())
            .containsEntry("before", "2026-06-01")
            .containsEntry("after", "2026-07-15");
    }

    private Issue baseIssue() {
        Issue i = new Issue();
        i.setId(issueId);
        i.setProjectId(projectId);
        i.setNumber(1);
        i.setTitle("orig");
        i.setDescriptionMd("orig body");
        i.setStatus(IssueStatus.todo);
        i.setPriority(IssuePriority.medium);
        return i;
    }

    @Test
    void create_withSingleLabel_emitsIssueCreatedThenLabelAdded() {
        UUID labelId = UUID.randomUUID();
        Project project = new Project();
        project.setId(projectId);
        project.setKey(KEY);
        when(projectData.findByKey(KEY)).thenReturn(Optional.of(project));
        when(projectData.claimNextIssueNumber(projectId)).thenReturn(1);
        when(issueData.create(any(Issue.class))).thenAnswer(inv -> {
            Issue arg = inv.getArgument(0);
            arg.setId(issueId);
            return arg;
        });
        when(labelData.findById(labelId)).thenAnswer(inv -> {
            var label = new io.github.pgatzka.projector.jooq.tables.pojos.Label();
            label.setId(labelId);
            label.setProjectId(projectId);
            label.setName("bug");
            label.setColor(io.github.pgatzka.projector.jooq.enums.LabelColor.red);
            return Optional.of(label);
        });

        service.create(KEY, new CreateIssueRequest("hello", null, null, null, null, List.of(labelId)));

        InOrder order = inOrder(activityService);
        ArgumentCaptor<Map<String, Object>> payload1 = payloadCaptor();
        order.verify(activityService).emit(eq(issueId), eq(ActivityAction.issue_created), payload1.capture());
        ArgumentCaptor<Map<String, Object>> payload2 = payloadCaptor();
        order.verify(activityService).emit(eq(issueId), eq(ActivityAction.label_added), payload2.capture());
        verify(activityService, times(2)).emit(eq(issueId), any(ActivityAction.class), any());
    }

    @Test
    void create_withMultipleLabels_emitsLabelAddedInOrder() {
        UUID label1Id = UUID.randomUUID();
        UUID label2Id = UUID.randomUUID();
        UUID label3Id = UUID.randomUUID();
        Project project = new Project();
        project.setId(projectId);
        project.setKey(KEY);
        when(projectData.findByKey(KEY)).thenReturn(Optional.of(project));
        when(projectData.claimNextIssueNumber(projectId)).thenReturn(1);
        when(issueData.create(any(Issue.class))).thenAnswer(inv -> {
            Issue arg = inv.getArgument(0);
            arg.setId(issueId);
            return arg;
        });
        when(labelData.findById(label1Id)).thenAnswer(inv -> {
            var label = new io.github.pgatzka.projector.jooq.tables.pojos.Label();
            label.setId(label1Id);
            label.setProjectId(projectId);
            label.setName("bug");
            label.setColor(io.github.pgatzka.projector.jooq.enums.LabelColor.red);
            return Optional.of(label);
        });
        when(labelData.findById(label2Id)).thenAnswer(inv -> {
            var label = new io.github.pgatzka.projector.jooq.tables.pojos.Label();
            label.setId(label2Id);
            label.setProjectId(projectId);
            label.setName("feature");
            label.setColor(io.github.pgatzka.projector.jooq.enums.LabelColor.green);
            return Optional.of(label);
        });
        when(labelData.findById(label3Id)).thenAnswer(inv -> {
            var label = new io.github.pgatzka.projector.jooq.tables.pojos.Label();
            label.setId(label3Id);
            label.setProjectId(projectId);
            label.setName("urgent");
            label.setColor(io.github.pgatzka.projector.jooq.enums.LabelColor.orange);
            return Optional.of(label);
        });

        service.create(KEY, new CreateIssueRequest("hello", null, null, null, null,
            List.of(label1Id, label2Id, label3Id)));

        ArgumentCaptor<ActivityAction> actionCaptor = ArgumentCaptor.forClass(ActivityAction.class);
        verify(activityService, times(4)).emit(eq(issueId), actionCaptor.capture(), any());
        var actions = actionCaptor.getAllValues();
        assertThat(actions).containsExactly(
            ActivityAction.issue_created,
            ActivityAction.label_added,
            ActivityAction.label_added,
            ActivityAction.label_added
        );
    }

    private void stubFindIssue(Issue issue) {
        Project project = new Project();
        project.setId(projectId);
        project.setKey(KEY);
        when(projectData.findByKey(KEY)).thenReturn(Optional.of(project));
        when(issueData.findByProjectIdAndNumber(projectId, 1)).thenReturn(Optional.of(issue));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static ArgumentCaptor<Map<String, Object>> payloadCaptor() {
        return (ArgumentCaptor) ArgumentCaptor.forClass(Map.class);
    }
}
