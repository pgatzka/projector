package io.github.pgatzka.projector.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(
    @NotBlank @Size(max = 10000) String bodyMd
) {}
