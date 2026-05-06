package io.github.pgatzka.projector.rest.service;

import io.github.pgatzka.projector.data.service.ProjectDataService;
import io.github.pgatzka.projector.jooq.tables.pojos.Project;
import io.github.pgatzka.projector.rest.dto.CreateProjectRequest;
import io.github.pgatzka.projector.rest.dto.UpdateProjectRequest;
import io.github.pgatzka.projector.rest.exception.ProjectKeyTakenException;
import io.github.pgatzka.projector.rest.exception.ProjectNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectDataService data;

    public ProjectService(ProjectDataService data) {
        this.data = data;
    }

    public List<Project> findAll() {
        return data.findAll();
    }

    public Project findByKey(String key) {
        return data.findByKey(key).orElseThrow(() -> new ProjectNotFoundException(key));
    }

    public Project create(CreateProjectRequest req) {
        if (data.existsByKey(req.key())) {
            throw new ProjectKeyTakenException(req.key());
        }
        Project p = new Project();
        p.setKey(req.key());
        p.setName(req.name());
        p.setDescription(req.description());
        p.setNextIssueNumber(1);
        return data.create(p);
    }

    public Project update(String key, UpdateProjectRequest req) {
        Project p = findByKey(key);
        if (req.name() != null) p.setName(req.name());
        if (req.description() != null) p.setDescription(req.description());
        return data.update(p);
    }

    public void delete(String key) {
        Project p = findByKey(key);
        data.deleteById(p.getId());
    }
}
