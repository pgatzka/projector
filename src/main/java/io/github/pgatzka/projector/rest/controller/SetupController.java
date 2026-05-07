package io.github.pgatzka.projector.rest.controller;

import io.github.pgatzka.projector.jooq.tables.pojos.Account;
import io.github.pgatzka.projector.rest.dto.MeResponse;
import io.github.pgatzka.projector.rest.dto.SetupRequest;
import io.github.pgatzka.projector.rest.service.SetupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SetupController {

    private final SetupService setupService;

    public SetupController(SetupService setupService) {
        this.setupService = setupService;
    }

    @PostMapping("/api/setup")
    public ResponseEntity<MeResponse> setup(@Valid @RequestBody SetupRequest request) {
        Account created = setupService.completeSetup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(MeResponse.of(created));
    }

    @GetMapping("/api/setup-required")
    public Map<String, Boolean> setupRequired() {
        return Map.of("required", setupService.isRequired());
    }
}
