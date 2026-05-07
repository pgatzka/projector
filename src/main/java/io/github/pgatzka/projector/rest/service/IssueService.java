package io.github.pgatzka.projector.rest.service;

import io.github.pgatzka.projector.data.service.IssueDataService;
import io.github.pgatzka.projector.data.service.IssueLabelDataService;
import io.github.pgatzka.projector.data.service.LabelDataService;
import io.github.pgatzka.projector.data.service.ProjectDataService;
import io.github.pgatzka.projector.jooq.enums.IssuePriority;
import io.github.pgatzka.projector.jooq.enums.IssueStatus;
import io.github.pgatzka.projector.jooq.tables.pojos.Issue;
import io.github.pgatzka.projector.jooq.tables.pojos.Label;
import io.github.pgatzka.projector.jooq.tables.pojos.Project;
import io.github.pgatzka.projector.rest.dto.CreateIssueRequest;
import io.github.pgatzka.projector.rest.dto.IssueDto;
import io.github.pgatzka.projector.rest.dto.IssueListQuery;
import io.github.pgatzka.projector.rest.dto.PageDto;
import io.github.pgatzka.projector.rest.dto.UpdateIssueRequest;
import io.github.pgatzka.projector.rest.exception.IssueNotFoundException;
import io.github.pgatzka.projector.rest.exception.LabelNotFoundException;
import io.github.pgatzka.projector.rest.exception.LabelNotInProjectException;
import io.github.pgatzka.projector.rest.exception.ProjectNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class IssueService {

    private final ProjectDataService projectData;
    private final IssueDataService issueData;
    private final LabelDataService labelData;
    private final IssueLabelDataService issueLabelData;

    public IssueService(ProjectDataService projectData,
                        IssueDataService issueData,
                        LabelDataService labelData,
                        IssueLabelDataService issueLabelData) {
        this.projectData = projectData;
        this.issueData = issueData;
        this.labelData = labelData;
        this.issueLabelData = issueLabelData;
    }

    public PageDto<IssueDto> list(String projectKey, IssueListQuery query) {
        Project project = projectData.findByKey(projectKey)
            .orElseThrow(() -> new ProjectNotFoundException(projectKey));
        PageDto<Issue> page = issueData.listForProject(project.getId(), query);
        Map<UUID, List<Label>> labelsByIssueId = issueData.loadLabelsByIssueIds(
            page.items().stream().map(Issue::getId).toList()
        );
        List<IssueDto> dtos = page.items().stream()
            .map(i -> IssueDto.of(i, project, labelsByIssueId.getOrDefault(i.getId(), List.of())))
            .toList();
        return new PageDto<>(dtos, page.total(), page.page(), page.size());
    }

    public List<Label> findLabelsForIssue(UUID issueId) {
        return issueData.loadLabelsByIssueIds(List.of(issueId)).getOrDefault(issueId, List.of());
    }

    public Issue findByProjectKeyAndNumber(String key, int number) {
        Project project = projectData.findByKey(key)
            .orElseThrow(() -> new ProjectNotFoundException(key));
        return issueData.findByProjectIdAndNumber(project.getId(), number)
            .orElseThrow(() -> new IssueNotFoundException(key + "-" + number));
    }

    @Transactional
    public Issue create(String projectKey, CreateIssueRequest req) {
        Project project = projectData.findByKey(projectKey)
            .orElseThrow(() -> new ProjectNotFoundException(projectKey));
        int number = projectData.claimNextIssueNumber(project.getId());
        Issue issue = new Issue();
        issue.setProjectId(project.getId());
        issue.setNumber(number);
        issue.setTitle(req.title());
        issue.setDescriptionMd(req.descriptionMd());
        issue.setStatus(req.status() != null ? req.status() : IssueStatus.todo);
        issue.setPriority(req.priority() != null ? req.priority() : IssuePriority.medium);
        issue.setDueDate(req.dueDate());
        Issue created = issueData.create(issue);
        if (req.labelIds() != null && !req.labelIds().isEmpty()) {
            for (UUID labelId : req.labelIds()) {
                Label l = labelData.findById(labelId)
                    .orElseThrow(() -> new LabelNotFoundException(labelId));
                if (!l.getProjectId().equals(project.getId())) {
                    throw new LabelNotInProjectException(labelId, projectKey);
                }
            }
            issueLabelData.bulkAssign(created.getId(), req.labelIds());
        }
        return created;
    }

    public Issue update(String projectKey, int number, UpdateIssueRequest req) {
        Issue issue = findByProjectKeyAndNumber(projectKey, number);
        if (req.title() != null) issue.setTitle(req.title());
        if (req.descriptionMd() != null) issue.setDescriptionMd(req.descriptionMd());
        if (req.status() != null) issue.setStatus(req.status());
        if (req.priority() != null) issue.setPriority(req.priority());
        if (req.dueDate() != null) issue.setDueDate(req.dueDate());
        return issueData.update(issue);
    }

    public void delete(String projectKey, int number) {
        Issue issue = findByProjectKeyAndNumber(projectKey, number);
        issueData.deleteById(issue.getId());
    }
}
