package io.github.pgatzka.projector.rest.service;

import io.github.pgatzka.projector.data.service.IssueDataService;
import io.github.pgatzka.projector.data.service.IssueLabelDataService;
import io.github.pgatzka.projector.data.service.LabelDataService;
import io.github.pgatzka.projector.data.service.ProjectDataService;
import io.github.pgatzka.projector.jooq.enums.ActivityAction;
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
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class IssueService {

    private final ProjectDataService projectData;
    private final IssueDataService issueData;
    private final LabelDataService labelData;
    private final IssueLabelDataService issueLabelData;
    private final ActivityService activityService;

    public IssueService(ProjectDataService projectData,
                        IssueDataService issueData,
                        LabelDataService labelData,
                        IssueLabelDataService issueLabelData,
                        @Lazy ActivityService activityService) {
        this.projectData = projectData;
        this.issueData = issueData;
        this.labelData = labelData;
        this.issueLabelData = issueLabelData;
        this.activityService = activityService;
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
        activityService.emit(created.getId(), ActivityAction.issue_created, Map.of());
        if (req.labelIds() != null && !req.labelIds().isEmpty()) {
            LinkedHashMap<UUID, Label> labels = new LinkedHashMap<>();
            for (UUID labelId : req.labelIds()) {
                Label l = labelData.findById(labelId)
                    .orElseThrow(() -> new LabelNotFoundException(labelId));
                if (!l.getProjectId().equals(project.getId())) {
                    throw new LabelNotInProjectException(labelId, projectKey);
                }
                labels.put(labelId, l);
            }
            issueLabelData.bulkAssign(created.getId(), req.labelIds());
            for (Map.Entry<UUID, Label> entry : labels.entrySet()) {
                Label l = entry.getValue();
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("labelId", l.getId() == null ? null : l.getId().toString());
                payload.put("labelName", l.getName());
                payload.put("labelColor", l.getColor() == null ? null : l.getColor().name());
                activityService.emit(created.getId(), ActivityAction.label_added, payload);
            }
        }
        return created;
    }

    @Transactional
    public Issue update(String projectKey, int number, UpdateIssueRequest req) {
        Issue issue = findByProjectKeyAndNumber(projectKey, number);

        IssueStatus oldStatus = issue.getStatus();
        IssuePriority oldPriority = issue.getPriority();
        LocalDate oldDueDate = issue.getDueDate();
        String oldTitle = issue.getTitle();
        String oldDescription = issue.getDescriptionMd();

        if (req.title() != null) issue.setTitle(req.title());
        if (req.descriptionMd() != null) issue.setDescriptionMd(req.descriptionMd());
        if (req.status() != null) issue.setStatus(req.status());
        if (req.priority() != null) issue.setPriority(req.priority());
        if (req.dueDate() != null) issue.setDueDate(req.dueDate());

        Issue updated = issueData.update(issue);

        if (req.status() != null && !Objects.equals(oldStatus, req.status())) {
            activityService.emit(updated.getId(), ActivityAction.status_changed,
                beforeAfter(enumName(oldStatus), enumName(req.status())));
        }
        if (req.priority() != null && !Objects.equals(oldPriority, req.priority())) {
            activityService.emit(updated.getId(), ActivityAction.priority_changed,
                beforeAfter(enumName(oldPriority), enumName(req.priority())));
        }
        if (req.dueDate() != null && !Objects.equals(oldDueDate, req.dueDate())) {
            activityService.emit(updated.getId(), ActivityAction.due_date_changed,
                beforeAfter(oldDueDate == null ? null : oldDueDate.toString(), req.dueDate().toString()));
        }
        if (req.title() != null && !Objects.equals(oldTitle, req.title())) {
            activityService.emit(updated.getId(), ActivityAction.title_edited,
                beforeAfter(oldTitle, req.title()));
        }
        if (req.descriptionMd() != null && !Objects.equals(oldDescription, req.descriptionMd())) {
            activityService.emit(updated.getId(), ActivityAction.description_edited,
                beforeAfter(oldDescription, req.descriptionMd()));
        }

        return updated;
    }

    public void delete(String projectKey, int number) {
        Issue issue = findByProjectKeyAndNumber(projectKey, number);
        issueData.deleteById(issue.getId());
    }

    private static String enumName(Enum<?> e) {
        return e == null ? null : e.name();
    }

    private static Map<String, Object> beforeAfter(Object before, Object after) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("before", before);
        m.put("after", after);
        return m;
    }
}
