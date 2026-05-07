package io.github.pgatzka.projector.rest.dto;

import java.util.List;

public record TimelineDto(List<TimelineEntryDto> entries, int total) {}
