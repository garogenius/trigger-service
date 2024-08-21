package com.mtn.concurrentcalls.controllers;

import com.mtn.concurrentcalls.models.AmlHealthResponse;
import com.mtn.concurrentcalls.services.ConcurrentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HealthCheckController {

	private final ConcurrentService concurrentService;

	@GetMapping(value = "/health/check", produces = { MediaType.APPLICATION_JSON_VALUE })
	public AmlHealthResponse checkHealth() {
		return concurrentService.checkHealth();
	}

	@GetMapping(value = "/health/check/test", produces = { MediaType.APPLICATION_JSON_VALUE })
	public AmlHealthResponse checkHealthTest() {
		return concurrentService.checkHealthTest();
	}

}
