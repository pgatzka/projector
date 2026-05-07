package io.github.pgatzka.projector.rest.controller;

import io.github.pgatzka.projector.rest.dto.CreateLabelRequest;
import io.github.pgatzka.projector.rest.dto.LabelDto;
import io.github.pgatzka.projector.rest.dto.UpdateLabelRequest;
import io.github.pgatzka.projector.rest.service.LabelService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{key}/labels")
public class LabelController {

    private final LabelService service;

    public LabelController(LabelService service) {
        this.service = service;
    }

    @GetMapping
    public List<LabelDto> list(@PathVariable String key) {
        return service.listForProject(key).stream().map(LabelDto::of).toList();
    }

    @PostMapping
    public ResponseEntity<LabelDto> create(@PathVariable String key, @Valid @RequestBody CreateLabelRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(LabelDto.of(service.create(key, req)));
    }

    @GetMapping("/{id}")
    public LabelDto get(@PathVariable String key, @PathVariable UUID id) {
        return LabelDto.of(service.findById(key, id));
    }

    @PatchMapping("/{id}")
    public LabelDto update(@PathVariable String key, @PathVariable UUID id, @Valid @RequestBody UpdateLabelRequest req) {
        return LabelDto.of(service.update(key, id, req));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String key, @PathVariable UUID id) {
        service.delete(key, id);
    }
}
