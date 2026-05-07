package io.github.pgatzka.projector.rest.dto;

import io.github.pgatzka.projector.jooq.enums.LabelColor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateLabelRequest(
    @NotBlank @Size(max = 50) String name,
    @NotNull LabelColor color
) {}
