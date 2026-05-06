package io.github.pgatzka.projector.e2e.support;

import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class DbReset {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Before
    public void truncate() {
        // Order matters because of FKs; cascade truncates all dependents in one call.
        jdbcTemplate.execute("truncate table issue, project, account restart identity cascade");
    }
}
