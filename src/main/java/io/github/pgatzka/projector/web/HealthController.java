package io.github.pgatzka.projector.web;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Minimal health endpoint used to verify the backend is up and that the
 * frontend dev proxy reaches it. Replaced/augmented by real endpoints in later steps.
 */
@RestController
public class HealthController {

	@GetMapping("/api/health")
	public Map<String, String> health() {
		return Map.of("status", "ok");
	}
}
