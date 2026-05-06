package io.github.pgatzka.projector.rest.dto;

import jakarta.validation.constraints.Size;

public record UpdateProjectRequest(
    @Size(min = 1, max = 100) String name,
    @Size(max = 5000) String description
) {}
