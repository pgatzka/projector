package io.github.pgatzka.projector.rest.controller;

import io.github.pgatzka.projector.rest.dto.CreateProjectRequest;
import io.github.pgatzka.projector.rest.dto.ProjectDto;
import io.github.pgatzka.projector.rest.dto.UpdateProjectRequest;
import io.github.pgatzka.projector.rest.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProjectDto> list() {
        return service.findAll().stream().map(ProjectDto::of).toList();
    }

    @PostMapping
    public ResponseEntity<ProjectDto> create(@Valid @RequestBody CreateProjectRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ProjectDto.of(service.create(req)));
    }

    @GetMapping("/{key}")
    public ProjectDto get(@PathVariable String key) {
        return ProjectDto.of(service.findByKey(key));
    }

    @PatchMapping("/{key}")
    public ProjectDto update(@PathVariable String key, @Valid @RequestBody UpdateProjectRequest req) {
        return ProjectDto.of(service.update(key, req));
    }

    @DeleteMapping("/{key}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String key) {
        service.delete(key);
    }
}
