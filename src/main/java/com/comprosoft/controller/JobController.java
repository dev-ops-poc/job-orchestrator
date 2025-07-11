package com.comprosoft.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.comprosoft.pojo.JobRequest;
import com.comprosoft.service.JobOrchestratorService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/jobs")
public class JobController implements JobControllerApi {

	private final JobOrchestratorService jobOrchestratorService;

	public JobController(JobOrchestratorService jobOrchestratorService) {
		this.jobOrchestratorService = jobOrchestratorService;
	}

	@Override
	public ResponseEntity<String> runTasks(@Valid @RequestBody JobRequest request) {
		try {
			return ResponseEntity.ok(jobOrchestratorService.runJobTask(request));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
		}
	}
}
