package io.github.pgatzka.projector.rest.dto;

import java.util.List;

public record PageDto<T>(List<T> items, int total, int page, int size) {}
