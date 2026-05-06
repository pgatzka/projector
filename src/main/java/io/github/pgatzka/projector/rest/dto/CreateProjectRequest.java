package io.github.pgatzka.projector.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateProjectRequest(
    @NotBlank @Pattern(regexp = "^[A-Z][A-Z]{1,9}$",
        message = "Project key must be 2-10 uppercase letters, starting with a letter") String key,
    @NotBlank @Size(max = 100) String name,
    @Size(max = 5000) String description
) {}
