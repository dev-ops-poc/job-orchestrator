package com.comprosoft.pojo;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobRequest {
	@NotBlank(message = "imageName is required")
	String imageName;

	Map<String, Object> envVariables; // Optional:

	List<String> command; // Optional: ["sh", "-c", "echo hello"]

	String pvcName;// Optional: claimName; if not provided, there will not be any binding
}
