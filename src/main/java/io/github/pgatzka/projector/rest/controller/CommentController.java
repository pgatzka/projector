package io.github.pgatzka.projector.rest.controller;

import io.github.pgatzka.projector.rest.dto.CommentDto;
import io.github.pgatzka.projector.rest.dto.CreateCommentRequest;
import io.github.pgatzka.projector.rest.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectKey}/issues/{number}/comments")
public class CommentController {

    private final CommentService service;

    public CommentController(CommentService service) {
        this.service = service;
    }

    @GetMapping
    public List<CommentDto> list(@PathVariable String projectKey, @PathVariable int number) {
        return service.listByIssue(projectKey, number);
    }

    @PostMapping
    public ResponseEntity<CommentDto> create(@PathVariable String projectKey,
                                             @PathVariable int number,
                                             @Valid @RequestBody CreateCommentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(service.create(projectKey, number, req.bodyMd()));
    }
}
