package io.github.pgatzka.projector.unit;

import io.github.pgatzka.projector.data.service.IssueDataService;
import io.github.pgatzka.projector.data.service.IssueLabelDataService;
import io.github.pgatzka.projector.data.service.LabelDataService;
import io.github.pgatzka.projector.data.service.ProjectDataService;
import io.github.pgatzka.projector.jooq.enums.IssuePriority;
import io.github.pgatzka.projector.jooq.enums.IssueStatus;
import io.github.pgatzka.projector.jooq.tables.pojos.Issue;
import io.github.pgatzka.projector.jooq.tables.pojos.Project;
import io.github.pgatzka.projector.rest.dto.CreateIssueRequest;
import io.github.pgatzka.projector.rest.exception.IssueNotFoundException;
import io.github.pgatzka.projector.rest.exception.ProjectNotFoundException;
import io.github.pgatzka.projector.rest.service.IssueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IssueServiceTest {

    private ProjectDataService projectData;
    private IssueDataService issueData;
    private LabelDataService labelData;
    private IssueLabelDataService issueLabelData;
    private IssueService service;

    @BeforeEach
    void setUp() {
        projectData = mock(ProjectDataService.class);
        issueData = mock(IssueDataService.class);
        labelData = mock(LabelDataService.class);
        issueLabelData = mock(IssueLabelDataService.class);
        service = new IssueService(projectData, issueData, labelData, issueLabelData);
    }

    @Test
    void createsIssueClaimingNextNumber() {
        UUID projectId = UUID.randomUUID();
        Project project = new Project();
        project.setId(projectId); project.setKey("ENG"); project.setNextIssueNumber(42);
        when(projectData.findByKey("ENG")).thenReturn(Optional.of(project));
        when(projectData.claimNextIssueNumber(projectId)).thenReturn(42);
        when(issueData.create(any(Issue.class))).thenAnswer(inv -> inv.getArgument(0));

        Issue created = service.create("ENG",
            new CreateIssueRequest("Hello", "body", IssueStatus.todo, IssuePriority.medium, null, null));

        assertThat(created.getNumber()).isEqualTo(42);
        assertThat(created.getProjectId()).isEqualTo(projectId);
        assertThat(created.getTitle()).isEqualTo("Hello");
    }

    @Test
    void rejectsCreateWhenProjectMissing() {
        when(projectData.findByKey("NOPE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create("NOPE",
            new CreateIssueRequest("x", null, null, null, null, null)))
            .isInstanceOf(ProjectNotFoundException.class);
    }

    @Test
    void findsByIdentifier() {
        UUID projectId = UUID.randomUUID();
        Project project = new Project(); project.setId(projectId); project.setKey("ENG");
        Issue issue = new Issue(); issue.setNumber(7); issue.setProjectId(projectId);
        when(projectData.findByKey("ENG")).thenReturn(Optional.of(project));
        when(issueData.findByProjectIdAndNumber(projectId, 7)).thenReturn(Optional.of(issue));

        Issue result = service.findByProjectKeyAndNumber("ENG", 7);
        assertThat(result.getNumber()).isEqualTo(7);
    }

    @Test
    void throwsWhenIssueMissing() {
        UUID projectId = UUID.randomUUID();
        Project project = new Project(); project.setId(projectId); project.setKey("ENG");
        when(projectData.findByKey("ENG")).thenReturn(Optional.of(project));
        when(issueData.findByProjectIdAndNumber(projectId, 99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByProjectKeyAndNumber("ENG", 99))
            .isInstanceOf(IssueNotFoundException.class);
    }
}
