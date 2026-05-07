package io.github.pgatzka.projector.rest.dto;

import io.github.pgatzka.projector.jooq.enums.LabelColor;
import jakarta.validation.constraints.Size;

public record UpdateLabelRequest(
    @Size(min = 1, max = 50) String name,
    LabelColor color
) {}
