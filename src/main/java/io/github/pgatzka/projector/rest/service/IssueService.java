package io.github.pgatzka.projector.rest.service;

import io.github.pgatzka.projector.data.service.IssueDataService;
import io.github.pgatzka.projector.data.service.ProjectDataService;
import io.github.pgatzka.projector.jooq.enums.IssuePriority;
import io.github.pgatzka.projector.jooq.enums.IssueStatus;
import io.github.pgatzka.projector.jooq.tables.pojos.Issue;
import io.github.pgatzka.projector.jooq.tables.pojos.Project;
import io.github.pgatzka.projector.rest.dto.CreateIssueRequest;
import io.github.pgatzka.projector.rest.dto.UpdateIssueRequest;
import io.github.pgatzka.projector.rest.exception.IssueNotFoundException;
import io.github.pgatzka.projector.rest.exception.ProjectNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class IssueService {

    private final ProjectDataService projectData;
    private final IssueDataService issueData;

    public IssueService(ProjectDataService projectData, IssueDataService issueData) {
        this.projectData = projectData;
        this.issueData = issueData;
    }

    public List<Issue> listByProjectKey(String key) {
        Project project = projectData.findByKey(key)
            .orElseThrow(() -> new ProjectNotFoundException(key));
        return issueData.findByProjectId(project.getId());
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
        return issueData.create(issue);
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
