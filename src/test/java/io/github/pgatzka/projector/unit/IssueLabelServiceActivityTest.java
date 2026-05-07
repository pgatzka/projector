package io.github.pgatzka.projector.unit;

import io.github.pgatzka.projector.data.service.IssueLabelDataService;
import io.github.pgatzka.projector.data.service.LabelDataService;
import io.github.pgatzka.projector.jooq.enums.ActivityAction;
import io.github.pgatzka.projector.jooq.enums.LabelColor;
import io.github.pgatzka.projector.jooq.tables.pojos.Issue;
import io.github.pgatzka.projector.jooq.tables.pojos.IssueLabel;
import io.github.pgatzka.projector.jooq.tables.pojos.Label;
import io.github.pgatzka.projector.rest.exception.LabelNotInProjectException;
import io.github.pgatzka.projector.rest.service.ActivityService;
import io.github.pgatzka.projector.rest.service.IssueLabelService;
import io.github.pgatzka.projector.rest.service.IssueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class IssueLabelServiceActivityTest {

    private static final String KEY = "ENG";
    private static final UUID PROJECT_ID = UUID.fromString("01900000-0000-7000-8000-000000000001");
    private static final UUID OTHER_PROJECT_ID = UUID.fromString("01900000-0000-7000-8000-0000000000ff");

    private IssueService issueService;
    private LabelDataService labelData;
    private IssueLabelDataService issueLabelData;
    private ActivityService activityService;
    private IssueLabelService service;

    private UUID issueId;
    private UUID labelId;
    private Issue issue;
    private Label label;

    @BeforeEach
    void setUp() {
        issueService = mock(IssueService.class);
        labelData = mock(LabelDataService.class);
        issueLabelData = mock(IssueLabelDataService.class);
        activityService = mock(ActivityService.class);
        service = new IssueLabelService(issueService, labelData, issueLabelData, activityService);

        issueId = UUID.randomUUID();
        labelId = UUID.randomUUID();

        issue = new Issue();
        issue.setId(issueId);
        issue.setProjectId(PROJECT_ID);
        issue.setNumber(1);

        label = new Label();
        label.setId(labelId);
        label.setProjectId(PROJECT_ID);
        label.setName("bug");
        label.setColor(LabelColor.red);

        when(issueService.findByProjectKeyAndNumber(KEY, 1)).thenReturn(issue);
    }

    @Test
    void assign_newLabel_emitsLabelAddedWithSnapshot() {
        when(labelData.findById(labelId)).thenReturn(Optional.of(label));
        when(issueLabelData.findByIssueIdAndLabelId(issueId, labelId)).thenReturn(Optional.empty());

        service.assign(KEY, 1, labelId);

        ArgumentCaptor<Map<String, Object>> payload = payloadCaptor();
        verify(activityService, times(1))
            .emit(eq(issueId), eq(ActivityAction.label_added), payload.capture());
        assertThat(payload.getValue())
            .containsEntry("labelId", labelId.toString())
            .containsEntry("labelName", "bug")
            .containsEntry("labelColor", "red");
    }

    @Test
    void assign_alreadyAssigned_emitsNothing() {
        when(labelData.findById(labelId)).thenReturn(Optional.of(label));
        IssueLabel existing = new IssueLabel();
        existing.setIssueId(issueId);
        existing.setLabelId(labelId);
        when(issueLabelData.findByIssueIdAndLabelId(issueId, labelId)).thenReturn(Optional.of(existing));

        service.assign(KEY, 1, labelId);

        verifyNoInteractions(activityService);
    }

    @Test
    void unassign_existingAssignment_emitsLabelRemovedFromPreDeleteSnapshot() {
        when(labelData.findById(labelId)).thenReturn(Optional.of(label));
        when(issueLabelData.unassign(issueId, labelId)).thenReturn(1);

        service.unassign(KEY, 1, labelId);

        ArgumentCaptor<Map<String, Object>> payload = payloadCaptor();
        verify(activityService, times(1))
            .emit(eq(issueId), eq(ActivityAction.label_removed), payload.capture());
        assertThat(payload.getValue())
            .containsEntry("labelId", labelId.toString())
            .containsEntry("labelName", "bug")
            .containsEntry("labelColor", "red");
    }

    @Test
    void unassign_nonExistingAssignment_emitsNothing() {
        when(labelData.findById(labelId)).thenReturn(Optional.of(label));
        when(issueLabelData.unassign(issueId, labelId)).thenReturn(0);

        service.unassign(KEY, 1, labelId);

        verify(activityService, never())
            .emit(any(UUID.class), any(ActivityAction.class), any());
    }

    @Test
    void assign_crossProjectLabel_throwsAndEmitsNothing() {
        Label foreign = new Label();
        foreign.setId(labelId);
        foreign.setProjectId(OTHER_PROJECT_ID);
        foreign.setName("infra");
        foreign.setColor(LabelColor.blue);
        when(labelData.findById(labelId)).thenReturn(Optional.of(foreign));

        assertThatThrownBy(() -> service.assign(KEY, 1, labelId))
            .isInstanceOf(LabelNotInProjectException.class);

        verifyNoInteractions(activityService);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static ArgumentCaptor<Map<String, Object>> payloadCaptor() {
        return (ArgumentCaptor) ArgumentCaptor.forClass(Map.class);
    }
}
