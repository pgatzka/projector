package io.github.pgatzka.projector.unit;

import io.github.pgatzka.projector.data.service.IssueDataService;
import io.github.pgatzka.projector.data.service.IssueLabelDataService;
import io.github.pgatzka.projector.data.service.LabelDataService;
import io.github.pgatzka.projector.data.service.ProjectDataService;
import io.github.pgatzka.projector.jooq.enums.IssueStatus;
import io.github.pgatzka.projector.jooq.enums.LabelColor;
import io.github.pgatzka.projector.jooq.tables.pojos.Issue;
import io.github.pgatzka.projector.jooq.tables.pojos.Label;
import io.github.pgatzka.projector.jooq.tables.pojos.Project;
import io.github.pgatzka.projector.rest.dto.IssueDto;
import io.github.pgatzka.projector.rest.dto.IssueListQuery;
import io.github.pgatzka.projector.rest.dto.PageDto;
import io.github.pgatzka.projector.rest.service.IssueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IssueServiceListTest {

    private ProjectDataService projectData;
    private IssueDataService issueData;
    private LabelDataService labelData;
    private IssueLabelDataService issueLabelData;
    private IssueService service;

    private UUID projectId;
    private Project project;

    @BeforeEach
    void setUp() {
        projectData = mock(ProjectDataService.class);
        issueData = mock(IssueDataService.class);
        labelData = mock(LabelDataService.class);
        issueLabelData = mock(IssueLabelDataService.class);
        service = new IssueService(projectData, issueData, labelData, issueLabelData);
        projectId = UUID.randomUUID();
        project = new Project();
        project.setId(projectId);
        project.setKey("ENG");
        when(projectData.findByKey("ENG")).thenReturn(Optional.of(project));
        when(issueData.listForProject(eq(projectId), any(IssueListQuery.class)))
            .thenReturn(new PageDto<>(List.of(), 0, 0, 50));
        when(issueData.loadLabelsByIssueIds(any())).thenReturn(Map.of());
    }

    @Test
    void clampsPageSizeToMax100() {
        IssueListQuery q = IssueListQuery.of(null, null, null, null, 0, 500);
        service.list("ENG", q);

        ArgumentCaptor<IssueListQuery> captor = ArgumentCaptor.forClass(IssueListQuery.class);
        verify(issueData).listForProject(eq(projectId), captor.capture());
        assertThat(captor.getValue().size()).isEqualTo(100);
    }

    @Test
    void defaultsPageSizeTo50WhenNull() {
        IssueListQuery q = IssueListQuery.of(null, null, null, null, null, null);
        service.list("ENG", q);

        ArgumentCaptor<IssueListQuery> captor = ArgumentCaptor.forClass(IssueListQuery.class);
        verify(issueData).listForProject(eq(projectId), captor.capture());
        assertThat(captor.getValue().size()).isEqualTo(50);
        assertThat(captor.getValue().page()).isEqualTo(0);
    }

    @Test
    void passesNonEmptyStatusListThrough() {
        IssueListQuery q = IssueListQuery.of(List.of(IssueStatus.todo, IssueStatus.done), null, null, null, 0, 25);
        service.list("ENG", q);

        ArgumentCaptor<IssueListQuery> captor = ArgumentCaptor.forClass(IssueListQuery.class);
        verify(issueData).listForProject(eq(projectId), captor.capture());
        assertThat(captor.getValue().statuses()).containsExactly(IssueStatus.todo, IssueStatus.done);
    }

    @Test
    void dropsNullQ() {
        IssueListQuery q1 = IssueListQuery.of(null, null, null, "   ", 0, 50);
        IssueListQuery q2 = IssueListQuery.of(null, null, null, null, 0, 50);
        assertThat(q1.q()).isNull();
        assertThat(q2.q()).isNull();
    }

    @Test
    void embedsLabelsInResultDtos() {
        UUID issueId = UUID.randomUUID();
        Issue issue = new Issue();
        issue.setId(issueId);
        issue.setProjectId(projectId);
        issue.setNumber(7);
        issue.setTitle("hello");

        Label lbl = new Label();
        lbl.setId(UUID.randomUUID());
        lbl.setProjectId(projectId);
        lbl.setName("bug");
        lbl.setColor(LabelColor.red);

        when(issueData.listForProject(eq(projectId), any(IssueListQuery.class)))
            .thenReturn(new PageDto<>(List.of(issue), 1, 0, 50));
        when(issueData.loadLabelsByIssueIds((Collection<UUID>) any()))
            .thenReturn(Map.of(issueId, List.of(lbl)));

        PageDto<IssueDto> result = service.list("ENG", IssueListQuery.of(null, null, null, null, 0, 50));

        assertThat(result.items()).hasSize(1);
        IssueDto dto = result.items().get(0);
        assertThat(dto.labels()).hasSize(1);
        assertThat(dto.labels().get(0).name()).isEqualTo("bug");
        assertThat(dto.labels().get(0).color()).isEqualTo(LabelColor.red);
    }
}
