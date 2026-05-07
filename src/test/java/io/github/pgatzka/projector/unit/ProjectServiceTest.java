package io.github.pgatzka.projector.unit;

import io.github.pgatzka.projector.data.service.ProjectDataService;
import io.github.pgatzka.projector.jooq.tables.pojos.Project;
import io.github.pgatzka.projector.rest.dto.CreateProjectRequest;
import io.github.pgatzka.projector.rest.exception.ProjectKeyTakenException;
import io.github.pgatzka.projector.rest.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProjectServiceTest {

    private ProjectDataService data;
    private ProjectService service;

    @BeforeEach
    void setUp() {
        data = mock(ProjectDataService.class);
        service = new ProjectService(data);
    }

    @Test
    void createsProjectWhenKeyAvailable() {
        when(data.existsByKey("ENG")).thenReturn(false);
        when(data.create(any(Project.class))).thenAnswer(inv -> inv.getArgument(0));

        Project created = service.create(new CreateProjectRequest("ENG", "Engineering", "Stuff"));

        assertThat(created.getKey()).isEqualTo("ENG");
        assertThat(created.getName()).isEqualTo("Engineering");
        assertThat(created.getDescription()).isEqualTo("Stuff");
    }

    @Test
    void rejectsCreateWhenKeyTaken() {
        when(data.existsByKey("ENG")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateProjectRequest("ENG", "Engineering", null)))
            .isInstanceOf(ProjectKeyTakenException.class);
    }
}
