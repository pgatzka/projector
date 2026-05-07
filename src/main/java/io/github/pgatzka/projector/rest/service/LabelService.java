package io.github.pgatzka.projector.rest.service;

import io.github.pgatzka.projector.data.service.LabelDataService;
import io.github.pgatzka.projector.jooq.tables.pojos.Label;
import io.github.pgatzka.projector.jooq.tables.pojos.Project;
import io.github.pgatzka.projector.rest.dto.CreateLabelRequest;
import io.github.pgatzka.projector.rest.dto.UpdateLabelRequest;
import io.github.pgatzka.projector.rest.exception.LabelNameTakenException;
import io.github.pgatzka.projector.rest.exception.LabelNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LabelService {

    private final LabelDataService data;
    private final ProjectService projectService;

    public LabelService(LabelDataService data, ProjectService projectService) {
        this.data = data;
        this.projectService = projectService;
    }

    public List<Label> listForProject(String projectKey) {
        Project project = projectService.findByKey(projectKey);
        return data.findByProjectId(project.getId());
    }

    public Label findById(String projectKey, UUID labelId) {
        Project project = projectService.findByKey(projectKey);
        Label label = data.findById(labelId).orElseThrow(() -> new LabelNotFoundException(labelId));
        if (!project.getId().equals(label.getProjectId())) {
            throw new LabelNotFoundException(labelId);
        }
        return label;
    }

    public Label create(String projectKey, CreateLabelRequest req) {
        Project project = projectService.findByKey(projectKey);
        if (data.findByProjectIdAndNameIgnoreCase(project.getId(), req.name()).isPresent()) {
            throw new LabelNameTakenException(projectKey, req.name());
        }
        Label label = new Label();
        label.setProjectId(project.getId());
        label.setName(req.name());
        label.setColor(req.color());
        return data.create(label);
    }

    public Label update(String projectKey, UUID labelId, UpdateLabelRequest req) {
        Label label = findById(projectKey, labelId);
        if (req.name() != null) {
            if (!req.name().equalsIgnoreCase(label.getName())) {
                Optional<Label> existing = data.findByProjectIdAndNameIgnoreCase(label.getProjectId(), req.name());
                if (existing.isPresent() && !existing.get().getId().equals(label.getId())) {
                    throw new LabelNameTakenException(projectKey, req.name());
                }
            }
            label.setName(req.name());
        }
        if (req.color() != null) {
            label.setColor(req.color());
        }
        return data.update(label);
    }

    public void delete(String projectKey, UUID labelId) {
        Label label = findById(projectKey, labelId);
        data.deleteById(label.getId());
    }
}
