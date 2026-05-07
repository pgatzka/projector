package io.github.pgatzka.projector.rest.controller;

import io.github.pgatzka.projector.rest.dto.TimelineDto;
import io.github.pgatzka.projector.rest.service.TimelineService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects/{projectKey}/issues/{number}/timeline")
public class TimelineController {

    private final TimelineService service;

    public TimelineController(TimelineService service) {
        this.service = service;
    }

    @GetMapping
    public TimelineDto get(@PathVariable String projectKey, @PathVariable int number) {
        return service.getTimeline(projectKey, number);
    }
}
