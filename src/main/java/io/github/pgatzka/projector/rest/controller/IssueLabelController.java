package io.github.pgatzka.projector.rest.controller;

import io.github.pgatzka.projector.jooq.tables.pojos.Issue;
import io.github.pgatzka.projector.jooq.tables.pojos.Label;
import io.github.pgatzka.projector.jooq.tables.pojos.Project;
import io.github.pgatzka.projector.rest.dto.AssignLabelRequest;
import io.github.pgatzka.projector.rest.dto.IssueDto;
import io.github.pgatzka.projector.rest.service.IssueLabelService;
import io.github.pgatzka.projector.rest.service.IssueService;
import io.github.pgatzka.projector.rest.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectKey}/issues/{number}/labels")
public class IssueLabelController {

    private final IssueLabelService issueLabelService;
    private final IssueService issueService;
    private final ProjectService projectService;

    public IssueLabelController(IssueLabelService issueLabelService,
                                IssueService issueService,
                                ProjectService projectService) {
        this.issueLabelService = issueLabelService;
        this.issueService = issueService;
        this.projectService = projectService;
    }

    @PostMapping
    public IssueDto assign(@PathVariable String projectKey,
                           @PathVariable int number,
                           @Valid @RequestBody AssignLabelRequest req) {
        Issue issue = issueLabelService.assign(projectKey, number, req.labelId());
        Project project = projectService.findByKey(projectKey);
        List<Label> labels = issueService.findLabelsForIssue(issue.getId());
        return IssueDto.of(issue, project, labels);
    }

    @DeleteMapping("/{labelId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unassign(@PathVariable String projectKey,
                         @PathVariable int number,
                         @PathVariable UUID labelId) {
        issueLabelService.unassign(projectKey, number, labelId);
    }
}
