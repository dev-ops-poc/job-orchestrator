package com.comprosoft.controller;

import com.comprosoft.pojo.JobRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.validation.Valid;

public interface JobControllerApi {

	@PostMapping("/run")
	@Operation(summary = "Run a Kubernetes Job", description = "Submits a Kubernetes Job with optional environment variables, PVC, and command")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Job started successfully"),
		@ApiResponse(responseCode = "400", description = "Validation error"),
		@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	ResponseEntity<String> runTasks(
		@RequestBody(
			description = "JobRequest payload including image, env vars, PVC, and command",
			required = true,
			content = @Content(
				schema = @Schema(implementation = JobRequest.class),
				examples = {
					@ExampleObject(
							name = "Alpine minimal echo Job",
							summary = "Hello World Job",
							value = """
							{
							  "imageName": "alpine",
							  "command": [
							    "sh",
							    "-c",
							    "echo Hello, World"
							  ]
							}
							"""
						),						
					@ExampleObject(
						name = "Minimal Job",
						summary = "Only image name",
						value = """
						{
						  "imageName": "ghcr.io/yourorg/minimal-job:latest"
						}
						"""
					),
					@ExampleObject(
						name = "Job with Env Vars",
						summary = "Image with TABLE_NAME and FILTER_ID",
						value = """
						{
						  "imageName": "ghcr.io/yourorg/env-job:1.2.3",
						  "envVariables": {
						    "TABLE_NAME": "orders",
						    "FILTER_ID": "eu-west"
						  }
						}
						"""
					),
					@ExampleObject(
						name = "Full Job with PVC and Command",
						summary = "Image + Env + PVC + Command",
						value = """
						{
						  "imageName": "ghcr.io/yourorg/data-processor:1.0.0",
						  "envVariables": {
						    "TABLE_NAME": "customer",
						    "FILTER_ID": "region-eu"
						  },
						  "pvcName": "data-volume-pvc",
						  "command": ["java", "-jar", "/app/app.jar"]
						}
						"""
					)
				}
			)
		)
		@Valid JobRequest request);
}
