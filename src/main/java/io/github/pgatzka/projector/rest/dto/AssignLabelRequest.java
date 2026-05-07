package io.github.pgatzka.projector.rest.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignLabelRequest(@NotNull UUID labelId) {}
