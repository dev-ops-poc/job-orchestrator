package com.comprosoft.service;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.comprosoft.pojo.JobRequest;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.utils.Serialization;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JobOrchestratorService {

	private final Configuration freemarkerConfig;

	public JobOrchestratorService() {
		freemarkerConfig = new Configuration(Configuration.VERSION_2_3_32);
		freemarkerConfig.setClassForTemplateLoading(this.getClass(), "/templates");
		freemarkerConfig.setDefaultEncoding("UTF-8");
		freemarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		log.info("Freemarker template configuration initialized.");
	}

	public String renderToString(String jobName, JobRequest request) throws Exception {
		log.debug("Rendering job template for jobName: {}, image: {}", jobName, request.getImageName());

		Template template = freemarkerConfig.getTemplate("k8s-job.ftl");

		Map<String, Object> context = new HashMap<>();
		context.put("jobName", jobName);
		context.put("imageName", request.getImageName());
		context.put("envVariables", request.getEnvVariables());
		
		context.put("command", request.getCommand());
		context.put("pvcName", request.getPvcName());
		
		try (StringWriter writer = new StringWriter()) {
			template.process(context, writer);
			log.debug("Job template rendered successfully.");
			return writer.toString();
		} catch (Exception e) {
			log.error("Failed to render job template: {}", e.getMessage(), e);
			throw e;
		}
	}

	public String runJobTask(JobRequest jobRequest) throws Exception {
		log.info("Received JobRequest for image: {}", jobRequest.getImageName());

		String jobName = getK8sSafeJobName(jobRequest.getImageName());
		log.debug("Generated Kubernetes-compliant job name: {}", jobName);

		String jobTemplateYaml = renderToString(jobName, jobRequest);
		log.debug("Rendered job YAML:\n{}", jobTemplateYaml);

		try (KubernetesClient client = new KubernetesClientBuilder().build()) {
			String namespace = client.getNamespace();
			if (namespace == null) {
				namespace = "default";
			}
			log.info("Using Kubernetes namespace: {}", namespace);

			Job job = Serialization.unmarshal(jobTemplateYaml, Job.class);
			client.resource(job).inNamespace(namespace).create();

			log.info("Job '{}' successfully created in namespace '{}'", jobName, namespace);
		} catch (Exception e) {
			log.error("Failed to create job in Kubernetes: {}", e.getMessage(), e);
			throw e;
		}

		return jobTemplateYaml;
	}

	private String getK8sSafeJobName(String imageName) {
		final int MAX_TOTAL_LENGTH = 63;
		final String suffix = "-" + UUID.randomUUID().toString().substring(0, 8);

		// Extract only the image name and tag (ignore registry)
		String[] parts = imageName.split("/");
		String nameAndTag = parts[parts.length - 1]; // e.g., data-processor:1.0.0

		String base = "job-" + nameAndTag
			.replaceAll("[^a-zA-Z0-9]", "-")   // replace special characters
			.toLowerCase()
			.replaceAll("-+", "-")             // collapse multiple dashes
			.replaceAll("(^-|-$)", "");        // trim dashes

		int maxBaseLength = MAX_TOTAL_LENGTH - suffix.length();
		if (base.length() > maxBaseLength) {
			base = base.substring(0, maxBaseLength);
		}

		String finalName = base + suffix;
		log.debug("Generated job name from image '{}': {}", imageName, finalName);
		return finalName;
	}

}