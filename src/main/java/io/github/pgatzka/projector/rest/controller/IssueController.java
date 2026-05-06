package io.github.pgatzka.projector.rest.controller;

import io.github.pgatzka.projector.jooq.tables.pojos.Issue;
import io.github.pgatzka.projector.jooq.tables.pojos.Project;
import io.github.pgatzka.projector.rest.dto.CreateIssueRequest;
import io.github.pgatzka.projector.rest.dto.IssueDto;
import io.github.pgatzka.projector.rest.dto.UpdateIssueRequest;
import io.github.pgatzka.projector.rest.service.IssueService;
import io.github.pgatzka.projector.rest.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectKey}/issues")
public class IssueController {

    private final IssueService issueService;
    private final ProjectService projectService;

    public IssueController(IssueService issueService, ProjectService projectService) {
        this.issueService = issueService;
        this.projectService = projectService;
    }

    @GetMapping
    public List<IssueDto> list(@PathVariable String projectKey) {
        Project project = projectService.findByKey(projectKey);
        List<Issue> issues = issueService.listByProjectKey(projectKey);
        return issues.stream().map(i -> IssueDto.of(i, project)).toList();
    }

    @PostMapping
    public ResponseEntity<IssueDto> create(@PathVariable String projectKey, @Valid @RequestBody CreateIssueRequest req) {
        Project project = projectService.findByKey(projectKey);
        Issue issue = issueService.create(projectKey, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(IssueDto.of(issue, project));
    }

    @GetMapping("/{number}")
    public IssueDto get(@PathVariable String projectKey, @PathVariable int number) {
        Project project = projectService.findByKey(projectKey);
        Issue issue = issueService.findByProjectKeyAndNumber(projectKey, number);
        return IssueDto.of(issue, project);
    }

    @PatchMapping("/{number}")
    public IssueDto update(@PathVariable String projectKey, @PathVariable int number,
                           @Valid @RequestBody UpdateIssueRequest req) {
        Project project = projectService.findByKey(projectKey);
        Issue updated = issueService.update(projectKey, number, req);
        return IssueDto.of(updated, project);
    }

    @DeleteMapping("/{number}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String projectKey, @PathVariable int number) {
        issueService.delete(projectKey, number);
    }
}
