package io.github.pgatzka.projector.unit;

import io.github.pgatzka.projector.data.service.IssueLabelDataService;
import io.github.pgatzka.projector.data.service.LabelDataService;
import io.github.pgatzka.projector.jooq.enums.LabelColor;
import io.github.pgatzka.projector.jooq.tables.pojos.Issue;
import io.github.pgatzka.projector.jooq.tables.pojos.IssueLabel;
import io.github.pgatzka.projector.jooq.tables.pojos.Label;
import io.github.pgatzka.projector.rest.exception.LabelNotFoundException;
import io.github.pgatzka.projector.rest.exception.LabelNotInProjectException;
import io.github.pgatzka.projector.rest.service.IssueLabelService;
import io.github.pgatzka.projector.rest.service.IssueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IssueLabelServiceTest {

    private static final String PROJECT_KEY = "ENG";
    private static final UUID PROJECT_ID = UUID.fromString("01900000-0000-7000-8000-000000000001");
    private static final UUID OTHER_PROJECT_ID = UUID.fromString("01900000-0000-7000-8000-0000000000ff");

    private IssueService issueService;
    private LabelDataService labelData;
    private IssueLabelDataService issueLabelData;
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
        service = new IssueLabelService(issueService, labelData, issueLabelData);

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

        when(issueService.findByProjectKeyAndNumber(PROJECT_KEY, 1)).thenReturn(issue);
    }

    @Test
    void assign_isIdempotent_whenAlreadyAssigned() {
        when(labelData.findById(labelId)).thenReturn(Optional.of(label));
        IssueLabel existing = new IssueLabel();
        existing.setIssueId(issueId);
        existing.setLabelId(labelId);
        when(issueLabelData.findByIssueIdAndLabelId(issueId, labelId)).thenReturn(Optional.of(existing));

        service.assign(PROJECT_KEY, 1, labelId);

        verify(issueLabelData, never()).assign(issueId, labelId);
    }

    @Test
    void assign_inserts_whenNotAssigned() {
        when(labelData.findById(labelId)).thenReturn(Optional.of(label));
        when(issueLabelData.findByIssueIdAndLabelId(issueId, labelId)).thenReturn(Optional.empty());

        service.assign(PROJECT_KEY, 1, labelId);

        verify(issueLabelData).assign(issueId, labelId);
    }

    @Test
    void assign_throws_whenLabelNotFound() {
        when(labelData.findById(labelId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assign(PROJECT_KEY, 1, labelId))
            .isInstanceOf(LabelNotFoundException.class);
    }

    @Test
    void assign_throws_whenLabelInDifferentProject() {
        Label foreign = new Label();
        foreign.setId(labelId);
        foreign.setProjectId(OTHER_PROJECT_ID);
        foreign.setName("infra");
        foreign.setColor(LabelColor.blue);
        when(labelData.findById(labelId)).thenReturn(Optional.of(foreign));

        assertThatThrownBy(() -> service.assign(PROJECT_KEY, 1, labelId))
            .isInstanceOf(LabelNotInProjectException.class);
    }

    @Test
    void unassign_isIdempotent() {
        when(issueLabelData.unassign(issueId, labelId)).thenReturn(0);

        service.unassign(PROJECT_KEY, 1, labelId);

        verify(issueLabelData).unassign(issueId, labelId);
    }
}
