package io.github.pgatzka.projector.rest.controller;

import io.github.pgatzka.projector.jooq.enums.IssuePriority;
import io.github.pgatzka.projector.jooq.enums.IssueStatus;
import io.github.pgatzka.projector.jooq.tables.pojos.Issue;
import io.github.pgatzka.projector.jooq.tables.pojos.Project;
import io.github.pgatzka.projector.rest.dto.CreateIssueRequest;
import io.github.pgatzka.projector.rest.dto.IssueDto;
import io.github.pgatzka.projector.rest.dto.IssueListQuery;
import io.github.pgatzka.projector.rest.dto.PageDto;
import io.github.pgatzka.projector.rest.dto.UpdateIssueRequest;
import io.github.pgatzka.projector.rest.service.IssueService;
import io.github.pgatzka.projector.rest.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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
    public PageDto<IssueDto> list(
        @PathVariable String projectKey,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String priority,
        @RequestParam(required = false) String label,
        @RequestParam(required = false) String q,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size
    ) {
        var statuses = parseEnumCsv(status, IssueStatus::valueOf);
        var priorities = parseEnumCsv(priority, IssuePriority::valueOf);
        var labelIds = parseUuidCsv(label);
        return issueService.list(projectKey, IssueListQuery.of(statuses, priorities, labelIds, q, page, size));
    }

    @PostMapping
    public ResponseEntity<IssueDto> create(@PathVariable String projectKey, @Valid @RequestBody CreateIssueRequest req) {
        Project project = projectService.findByKey(projectKey);
        Issue issue = issueService.create(projectKey, req);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(IssueDto.of(issue, project, issueService.findLabelsForIssue(issue.getId())));
    }

    @GetMapping("/{number}")
    public IssueDto get(@PathVariable String projectKey, @PathVariable int number) {
        Project project = projectService.findByKey(projectKey);
        Issue issue = issueService.findByProjectKeyAndNumber(projectKey, number);
        return IssueDto.of(issue, project, issueService.findLabelsForIssue(issue.getId()));
    }

    @PatchMapping("/{number}")
    public IssueDto update(@PathVariable String projectKey, @PathVariable int number,
                           @Valid @RequestBody UpdateIssueRequest req) {
        Project project = projectService.findByKey(projectKey);
        Issue updated = issueService.update(projectKey, number, req);
        return IssueDto.of(updated, project, issueService.findLabelsForIssue(updated.getId()));
    }

    @DeleteMapping("/{number}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String projectKey, @PathVariable int number) {
        issueService.delete(projectKey, number);
    }

    private static <E> List<E> parseEnumCsv(String csv, java.util.function.Function<String, E> mapper) {
        if (csv == null || csv.isBlank()) return List.of();
        return Arrays.stream(csv.split(",")).map(String::trim).filter(s -> !s.isEmpty()).map(mapper).toList();
    }

    private static List<UUID> parseUuidCsv(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return Arrays.stream(csv.split(",")).map(String::trim).filter(s -> !s.isEmpty()).map(UUID::fromString).toList();
    }
}
