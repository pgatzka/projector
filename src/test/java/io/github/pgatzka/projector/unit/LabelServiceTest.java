package io.github.pgatzka.projector.unit;

import io.github.pgatzka.projector.data.service.LabelDataService;
import io.github.pgatzka.projector.jooq.enums.LabelColor;
import io.github.pgatzka.projector.jooq.tables.pojos.Label;
import io.github.pgatzka.projector.jooq.tables.pojos.Project;
import io.github.pgatzka.projector.rest.dto.CreateLabelRequest;
import io.github.pgatzka.projector.rest.dto.UpdateLabelRequest;
import io.github.pgatzka.projector.rest.exception.LabelNameTakenException;
import io.github.pgatzka.projector.rest.exception.LabelNotFoundException;
import io.github.pgatzka.projector.rest.service.LabelService;
import io.github.pgatzka.projector.rest.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LabelServiceTest {

    private static final UUID PROJECT_ID = UUID.fromString("01900000-0000-7000-8000-000000000001");
    private static final String PROJECT_KEY = "ENG";

    private LabelDataService data;
    private ProjectService projectService;
    private LabelService service;

    @BeforeEach
    void setUp() {
        data = mock(LabelDataService.class);
        projectService = mock(ProjectService.class);
        service = new LabelService(data, projectService);

        Project project = new Project();
        project.setId(PROJECT_ID);
        project.setKey(PROJECT_KEY);
        project.setName("Engineering");
        when(projectService.findByKey(PROJECT_KEY)).thenReturn(project);
    }

    @Test
    void createsLabel_whenNameAvailable() {
        when(data.findByProjectIdAndNameIgnoreCase(PROJECT_ID, "bug")).thenReturn(Optional.empty());
        when(data.create(any(Label.class))).thenAnswer(inv -> inv.getArgument(0));

        Label created = service.create(PROJECT_KEY, new CreateLabelRequest("bug", LabelColor.red));

        assertThat(created.getName()).isEqualTo("bug");
        assertThat(created.getColor()).isEqualTo(LabelColor.red);
        assertThat(created.getProjectId()).isEqualTo(PROJECT_ID);
    }

    @Test
    void rejectsCreate_whenNameTakenCaseInsensitive() {
        Label existing = new Label();
        existing.setId(UUID.randomUUID());
        existing.setProjectId(PROJECT_ID);
        existing.setName("bug");
        existing.setColor(LabelColor.red);
        when(data.findByProjectIdAndNameIgnoreCase(eq(PROJECT_ID), any())).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.create(PROJECT_KEY, new CreateLabelRequest("BUG", LabelColor.blue)))
            .isInstanceOf(LabelNameTakenException.class);
    }

    @Test
    void updatesNameAndColor() {
        UUID labelId = UUID.randomUUID();
        Label existing = new Label();
        existing.setId(labelId);
        existing.setProjectId(PROJECT_ID);
        existing.setName("bug");
        existing.setColor(LabelColor.red);
        when(data.findById(labelId)).thenReturn(Optional.of(existing));
        when(data.findByProjectIdAndNameIgnoreCase(PROJECT_ID, "defect")).thenReturn(Optional.empty());
        when(data.update(any(Label.class))).thenAnswer(inv -> inv.getArgument(0));

        Label updated = service.update(PROJECT_KEY, labelId,
            new UpdateLabelRequest("defect", LabelColor.orange));

        assertThat(updated.getName()).isEqualTo("defect");
        assertThat(updated.getColor()).isEqualTo(LabelColor.orange);
    }

    @Test
    void update_skipsNullFields() {
        UUID labelId = UUID.randomUUID();
        Label existing = new Label();
        existing.setId(labelId);
        existing.setProjectId(PROJECT_ID);
        existing.setName("bug");
        existing.setColor(LabelColor.red);
        when(data.findById(labelId)).thenReturn(Optional.of(existing));
        when(data.update(any(Label.class))).thenAnswer(inv -> inv.getArgument(0));

        Label updated = service.update(PROJECT_KEY, labelId,
            new UpdateLabelRequest(null, LabelColor.green));

        assertThat(updated.getName()).isEqualTo("bug");
        assertThat(updated.getColor()).isEqualTo(LabelColor.green);
    }

    @Test
    void findById_throwsWhenLabelInDifferentProject() {
        UUID labelId = UUID.randomUUID();
        UUID otherProjectId = UUID.fromString("01900000-0000-7000-8000-0000000000ff");
        Label foreign = new Label();
        foreign.setId(labelId);
        foreign.setProjectId(otherProjectId);
        foreign.setName("bug");
        foreign.setColor(LabelColor.red);
        when(data.findById(labelId)).thenReturn(Optional.of(foreign));

        assertThatThrownBy(() -> service.findById(PROJECT_KEY, labelId))
            .isInstanceOf(LabelNotFoundException.class);
    }
}
